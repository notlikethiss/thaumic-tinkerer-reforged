import re
from pathlib import Path

TOKEN = re.compile(
    r"""\s*(?:
    (?P<str>"(?:\\.|[^"\\])*")
    |(?P<chr>'(?:\\.|[^'\\])')
    |(?P<num>\d+(?:\.\d+)?[fFdDlL]?)
    |(?P<id>[A-Za-z_$][A-Za-z_$0-9]*)
    |(?P<sym>[^\sA-Za-z_$0-9"])
    )""",
    re.VERBOSE,
)


def strip_comments(source):
    out = []
    index = 0
    length = len(source)
    while index < length:
        char = source[index]
        if char == '"':
            end = index + 1
            while end < length and source[end] != '"':
                end += 2 if source[end] == "\\" else 1
            out.append(source[index:end + 1])
            index = end + 1
        elif char == "'":
            end = index + 1
            while end < length and source[end] != "'":
                end += 2 if source[end] == "\\" else 1
            out.append(source[index:end + 1])
            index = end + 1
        elif source.startswith("//", index):
            end = source.find("\n", index)
            index = length if end < 0 else end
        elif source.startswith("/*", index):
            end = source.find("*/", index)
            index = length if end < 0 else end + 2
        else:
            out.append(char)
            index += 1
    return "".join(out)


def tokenize(source):
    tokens = []
    position = 0
    length = len(source)
    while position < length:
        match = TOKEN.match(source, position)
        if not match:
            break
        kind = match.lastgroup
        tokens.append((kind, match.group(kind), match.start(kind), match.end(kind)))
        position = match.end()
    return tokens


class Parser:
    def __init__(self, source, tokens):
        self.source = source
        self.tokens = tokens
        self.index = 0

    def peek(self, offset=0):
        position = self.index + offset
        return self.tokens[position] if position < len(self.tokens) else ("eof", "", len(self.source), len(self.source))

    def is_sym(self, value, offset=0):
        token = self.peek(offset)
        return token[0] == "sym" and token[1] == value

    def expect_sym(self, value):
        token = self.peek()
        if token[0] != "sym" or token[1] != value:
            raise ValueError("expected " + value + " got " + token[1] + " at " + self.source[token[2]:token[2] + 60])
        self.index += 1

    def text(self, start_index, end_index):
        return self.source[self.tokens[start_index][2]:self.tokens[end_index - 1][3]]

    def parse_args(self):
        args = []
        self.expect_sym("(")
        if self.is_sym(")"):
            self.index += 1
            return args
        while True:
            args.append(self.parse_expr())
            if self.is_sym(","):
                self.index += 1
                continue
            self.expect_sym(")")
            return args

    def parse_expr(self):
        start = self.index
        left = self.parse_unary()
        while self.peek()[0] == "sym" and self.peek()[1] in "+-*/%":
            op = self.peek()[1]
            self.index += 1
            right = self.parse_unary()
            left = {"t": "bin", "op": op, "l": left, "r": right, "raw": self.text(start, self.index)}
        if self.is_sym("?"):
            self.index += 1
            yes = self.parse_expr()
            self.expect_sym(":")
            no = self.parse_expr()
            left = {"t": "cond", "cond": left, "yes": yes, "no": no, "raw": self.text(start, self.index)}
        return left

    def parse_unary(self):
        start = self.index
        if self.is_sym("-"):
            self.index += 1
            operand = self.parse_unary()
            return {"t": "neg", "v": operand, "raw": self.text(start, self.index)}
        if self.is_sym("!"):
            self.index += 1
            operand = self.parse_unary()
            return {"t": "not", "v": operand, "raw": self.text(start, self.index)}
        if self.is_sym("(") and self.peek(1)[0] == "id" and self.looks_like_cast():
            while not self.is_sym(")"):
                self.index += 1
            self.index += 1
            return self.parse_unary()
        return self.parse_postfix()

    def looks_like_cast(self):
        offset = 1
        while self.peek(offset)[0] == "id" and self.is_sym(".", offset + 1):
            offset += 2
        if self.peek(offset)[0] != "id":
            return False
        offset += 1
        while self.is_sym("[", offset) and self.is_sym("]", offset + 1):
            offset += 2
        if not self.is_sym(")", offset):
            return False
        following = self.peek(offset + 1)
        return following[0] in ("id", "str", "num") or (following[0] == "sym" and following[1] == "(")

    def parse_postfix(self):
        start = self.index
        node = self.parse_primary()
        while True:
            if self.is_sym(".") and self.peek(1)[0] == "id":
                name = self.peek(1)[1]
                self.index += 2
                if self.is_sym("("):
                    args = self.parse_args()
                    node = {"t": "call", "target": node, "name": name, "args": args, "raw": self.text(start, self.index)}
                else:
                    node = {"t": "field", "target": node, "name": name, "raw": self.text(start, self.index)}
            elif self.is_sym("["):
                self.index += 1
                index_expr = self.parse_expr()
                self.expect_sym("]")
                node = {"t": "index", "target": node, "index": index_expr, "raw": self.text(start, self.index)}
            else:
                return node

    def parse_primary(self):
        start = self.index
        kind, value, _, _ = self.peek()
        if kind == "str":
            self.index += 1
            return {"t": "str", "v": bytes(value[1:-1], "utf-8").decode("unicode_escape"), "raw": value}
        if kind == "chr":
            self.index += 1
            return {"t": "chr", "v": value[1:-1], "raw": value}
        if kind == "num":
            self.index += 1
            return {"t": "num", "v": float(value.rstrip("fFdDlL")) if "." in value else int(value.rstrip("fFdDlL")), "raw": value}
        if kind == "sym" and value == "(":
            self.index += 1
            inner = self.parse_expr()
            self.expect_sym(")")
            return inner
        if kind == "sym" and value == "{":
            self.index += 1
            items = []
            while not self.is_sym("}"):
                items.append(self.parse_expr())
                if self.is_sym(","):
                    self.index += 1
            self.index += 1
            return {"t": "array", "items": items, "raw": self.text(start, self.index)}
        if kind == "id" and value == "new":
            self.index += 1
            cls = self.peek()[1]
            self.index += 1
            while self.is_sym(".") and self.peek(1)[0] == "id":
                cls = self.peek(1)[1]
                self.index += 2
            if self.is_sym("["):
                while self.is_sym("["):
                    self.index += 1
                    if not self.is_sym("]"):
                        self.parse_expr()
                    self.expect_sym("]")
                items = []
                if self.is_sym("{"):
                    self.index += 1
                    while not self.is_sym("}"):
                        items.append(self.parse_expr())
                        if self.is_sym(","):
                            self.index += 1
                    self.index += 1
                return {"t": "array", "items": items, "raw": self.text(start, self.index)}
            args = self.parse_args()
            return {"t": "new", "cls": cls, "args": args, "raw": self.text(start, self.index)}
        if kind == "id":
            self.index += 1
            if self.is_sym("("):
                args = self.parse_args()
                return {"t": "call", "target": None, "name": value, "args": args, "raw": self.text(start, self.index)}
            return {"t": "name", "v": value, "raw": value}
        raise ValueError("unexpected token " + value + " at " + self.source[self.peek()[2]:self.peek()[2] + 60])


def load_constants(path):
    source = strip_comments(Path(path).read_text(encoding="utf-8", errors="replace"))
    constants = {}
    for match in re.finditer(r"static\s+final\s+String\s+(\w+)\s*=\s*([^;]+);", source):
        constants[match.group(1)] = match.group(2).strip()
    resolved = {}

    def resolve(name):
        if name in resolved:
            return resolved[name]
        expression = constants[name]
        parts = [part.strip() for part in re.split(r"\+", expression)]
        value = ""
        for part in parts:
            if part.startswith('"'):
                value += part[1:-1]
            elif part in constants:
                value += resolve(part)
            else:
                value += part
        resolved[name] = value
        return value

    for name in constants:
        resolve(name)
    return resolved


def find_statements(source, class_names):
    tokens = tokenize(source)
    parser = Parser(source, tokens)
    results = []
    for index, token in enumerate(tokens):
        if token[0] == "id" and token[1] == "new" and index + 1 < len(tokens) and tokens[index + 1][1] in class_names:
            variable = None
            back = index - 1
            while back >= 0 and tokens[back][0] == "sym" and tokens[back][1] == ")":
                while not (tokens[back][0] == "sym" and tokens[back][1] == "("):
                    back -= 1
                back -= 1
            if back >= 1 and tokens[back][1] == "=" and tokens[back - 1][0] == "id":
                variable = tokens[back - 1][1]
            parser.index = index
            try:
                node = parser.parse_expr()
                followups = []
                while variable and parser.is_sym(";"):
                    names = [parser.peek(offset)[1] for offset in range(1, 7)]
                    direct = names[0] == variable and names[1] == "."
                    cast = names[0] == "(" and names[1] == "(" and names[3] == ")" and names[4] == variable and names[5] == ")"
                    if not (direct or cast):
                        break
                    parser.index += 1
                    followups.append(parser.parse_expr())
                results.append((node, followups))
            except ValueError as error:
                results.append(({"t": "error", "error": str(error), "raw": source[token[2]:token[2] + 80]}, []))
    return results


def find_new(source, class_names):
    tokens = tokenize(source)
    parser = Parser(source, tokens)
    results = []
    for index, token in enumerate(tokens):
        if token[0] == "id" and token[1] == "new" and index + 1 < len(tokens) and tokens[index + 1][1] in class_names:
            parser.index = index
            try:
                results.append(parser.parse_expr())
            except ValueError as error:
                results.append({"t": "error", "error": str(error), "raw": source[token[2]:token[2] + 80]})
    return results
