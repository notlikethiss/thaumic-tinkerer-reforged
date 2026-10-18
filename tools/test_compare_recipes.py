import unittest

from compare_recipes import infusion_component, same
from extract_tt_enchanter import compare


class RecipeComparisonTests(unittest.TestCase):
    def compare(self, truth, actual, infusion=False):
        problems, soft = [], []
        check = infusion_component if infusion else same
        check(truth, actual, soft, "fixture", problems)
        return problems, soft

    def test_wrong_count_is_rejected(self):
        problems, _ = self.compare({"ids": ["minecraft:diamond"], "count": 8}, "minecraft:diamond x1")
        self.assertEqual(len(problems), 1)

    def test_tc4_infusion_count_semantics_are_reported(self):
        problems, soft = self.compare({"ids": ["minecraft:diamond"], "count": 8}, "minecraft:diamond x1", infusion=True)
        self.assertEqual(problems, [])
        self.assertEqual(len(soft), 1)
        problems, _ = self.compare({"ids": ["minecraft:diamond"], "count": 8}, "minecraft:emerald x1", infusion=True)
        self.assertEqual(len(problems), 1)

    def test_potion_metadata_is_required(self):
        truth = {"ids": ["minecraft:potion"], "count": 1, "meta": 8198}
        problems, _ = self.compare(truth, "minecraft:potion x1 {tt_potion=minecraft:fire_resistance}")
        self.assertEqual(len(problems), 1)
        problems, _ = self.compare(truth, "minecraft:potion x1 {tt_potion=minecraft:night_vision}")
        self.assertEqual(problems, [])

    def test_enchanter_research_and_level_costs_are_checked(self):
        truth = {"id": "fixture", "texture": "fixture", "research": "VAMPIRISM", "vanilla": False, "aspects": {"ORDER": 12}}
        actual = {**truth, "aspects": "{ORDER=12}", "levels": {"1": "{ORDER=14}", "2": "{ORDER=33}"}}
        self.assertEqual(compare([truth], [actual]), [])
        actual["research"] = "FOCUSED_STRIKE"
        self.assertEqual(len(compare([truth], [actual])), 1)
        actual["levels"]["2"] = "{ORDER=32}"
        self.assertEqual(len(compare([truth], [actual])), 2)


if __name__ == "__main__":
    unittest.main()
