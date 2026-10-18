import gzip
import io
import struct
from pathlib import Path



TAG_BYTE, TAG_INT, TAG_DOUBLE, TAG_STRING, TAG_LIST, TAG_COMPOUND = 1, 3, 6, 8, 9, 10


class Byte(int):
    pass


class Int(int):
    pass


class Double(float):
    pass


class TagList(list):
    def __init__(self, items, kind=None):
        super().__init__(items)
        self.kind = kind


def tag_type(value):
    if isinstance(value, Byte):
        return TAG_BYTE
    if isinstance(value, Int):
        return TAG_INT
    if isinstance(value, Double):
        return TAG_DOUBLE
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, TagList):
        return TAG_LIST
    if isinstance(value, dict):
        return TAG_COMPOUND
    raise TypeError(type(value))


def write_string(out, value):
    encoded = value.encode("utf-8")
    out.append(struct.pack(">H", len(encoded)))
    out.append(encoded)


def write_payload(out, value):
    kind = tag_type(value)
    if kind == TAG_BYTE:
        out.append(struct.pack(">b", value))
    elif kind == TAG_INT:
        out.append(struct.pack(">i", value))
    elif kind == TAG_DOUBLE:
        out.append(struct.pack(">d", value))
    elif kind == TAG_STRING:
        write_string(out, value)
    elif kind == TAG_LIST:
        item_kind = value.kind if value.kind is not None else (tag_type(value[0]) if value else 0)
        out.append(struct.pack(">bi", item_kind, len(value)))
        for item in value:
            write_payload(out, item)
    else:
        for key, item in value.items():
            out.append(struct.pack(">b", tag_type(item)))
            write_string(out, key)
            write_payload(out, item)
        out.append(b"\x00")


def to_nbt(root):
    out = [struct.pack(">b", TAG_COMPOUND)]
    write_string(out, "")
    write_payload(out, root)
    buffer = io.BytesIO()
    with gzip.GzipFile(fileobj=buffer, mode="wb", filename="", mtime=0) as stream:
        stream.write(b"".join(out))
    return buffer.getvalue()
