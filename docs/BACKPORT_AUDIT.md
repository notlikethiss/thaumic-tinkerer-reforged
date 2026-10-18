# Матрица аудита бэкпорта

Объём соответствует `CLAUDE.md`. Источники относятся к `reference/tt-src/src/main/java/thaumic/tinkerer/common/`. Эталонные таблицы исследований и рецептов повторно извлечены без изменений. Тесты подтверждают свои сценарии, а не все возможные игровые состояния; ручная приёмка остаётся открытой.

| Модуль | Эталон TT | Автоматические сценарии | Покрываемое поведение |
| --- | --- | --- | --- |
| `core` | `ConfigHandler`, `IMultiTileEntityBlock`, `ITTinkererBlock`, `ITTinkererItem` и связанные классы | `CoreGameTests` (7) | Регистрация, конфигурация и API базы |
| `dark_quartz` | `BlockDarkQuartz`, `BlockDarkQuartzSlab`, `BlockDarkQuartzStairs` | `DarkQuartzGameTests` (4) | Варианты, плиты, лестницы и крафт |
| `decor` | `BlockTravelSlab`, `BlockTravelStairs`, `BlockWardSlab` | `DecorGameTests` (5) | Ускорение и ward-защита |
| `gas` | `BlockGaseousLight`, `BlockGaseousShadow`, `BlockNitorGas`, `ItemBrightNitor` | `GasGameTests` (8) | Газ, свет, тьма и рассеивание |
| `transvector` | `BlockCamo`, `BlockPlatform`, `TileTransvector`, `TileTransvectorDislocator` и связанные классы | `TransvectorGameTests` (16) | Привязка, камуфляж, redstone и перенос |
| `funnel` | `BlockFunnel`, `TileFunnel` | `FunnelGameTests` (7) | Эссенция и банки |
| `mobilizer` | `BlockMobilizer`, `BlockMobilizerRelay`, `TileEntityMobilizer` | `MobilizerGameTests` (7) | Relay, разворот, пассажиры и сохранение |
| `repairer` | `BlockRepairer`, `TileRepairer` | `RepairerGameTests` (7) | Ремонт, vis и сохранение |
| `foci` | `ItemFocusDeflect`, `ItemFocusDislocation`, `ItemFocusEnderChest`, `ItemFocusFlight` и связанные классы | `FociGameTests` (12) | Стоимость, улучшения и механика обычных фокусов |
| `enchanting` | `BlockEnchanter`, `TileEnchanter`, `ContainerEnchanter`, `EnchantmentAscentBoost` и связанные классы | `EnchantingGameTests` (12) | Зачарования, станок, стоимость и cloth |
| `crops` | `BlockInfusedFarmland`, `BlockInfusedGrain`, `TileInfusedFarmland`, `TileInfusedGrain` и связанные классы | `CropsGameTests` (18) | Аспекты, тенденции, мутации и сериализация |
| `elements` | `BlockForcefield`, `BlockFireAir`, `BlockFireBase`, `BlockFireChaos` и связанные классы | `ElementsGameTests` (19) | Огонь, трансформации, эффекты и gamerule |
| `attractor` | `BlockMagnet`, `TileMagnet`, `TileMobMagnet`, `ContainerMobMagnet` и связанные классы | `AttractorGameTests` (9) | Сила redstone, режимы и фильтры |
| `tablet` | `BlockAnimationTablet`, `ContainerAnimationTablet`, `TabletFakePlayer`, `TileAnimationTablet` | `TabletGameTests` (7) | Fake player, действия и redstone |
| `gear` | `ItemBloodSword`, `ItemCleansingTalisman`, `ItemInfusedInkwell`, `ItemRevealingHelm` и связанные классы | `GearGameTests` (11) | XP, эффекты, меч, знания и экипировка |
| `necromancy` | `BlockSummon`, `TileSummon`, `EnumMobAspect`, `ItemMobAspect` | `NecromancyGameTests` (9) | 17 аспектов душ, condensed/infused и призыв |
| `kami` | `ItemKamiResource`, `ItemIchorclothArmor`, `ItemIchorclothArmorAdv`, `ItemIchorAxe` и связанные классы | `KamiGameTests` (8) | Материалы, shards, жезлы, инструменты и броня |
| `awakened_tools` | `IAdvancedTool`, `ItemIchorAxe`, `ItemIchorAxeAdv`, `ItemIchorPick` и связанные классы | `AwakenedToolsGameTests` (15) | Режимы, permissions, души и дропы |
| `awakened_armor` | `KamiArmorHandler`, `ItemGemBoots`, `ItemGemChest`, `ItemGemHelm` и связанные классы | `AwakenedArmorGameTests` (12) | Комплект, полёт, выключение и синхронизация |
| `trinkets` | `ContainerIchorPouch`, `ItemBlockTalisman`, `ItemCatAmulet`, `ItemIchorPouch` и связанные классы | `TrinketsGameTests` (14) | Блоки, строительство, инструменты и мешочек |
| `kami_gateway` | `BlockWarpGate`, `ContainerWarpGate`, `SlotSkyPearl`, `TileWarpGate` и связанные классы | `GatewayGameTests` (10) | Назначения, телепортация, блокировка и persistence |
| `kami_foci` | `ItemFocusRecall`, `ItemFocusShadowbeam`, `ItemFocusXPDrain` | `KamiFociGameTests` (10) | Луч, опыт, отражения и возврат |

## Реализация, исследования и рецепты

Машиночитаемая матрица `tools/backport/audit.json` связывает каждый модуль с исходниками TT, файлами реализации, ключами всех 74 исследований, исходными записями рецептов и именами сценариев. `python3 tools/backport/update_audit.py` проверяет существование файлов и покрытие всех включённых записей эталонных таблиц. Привязка модулей к исходникам проверяется отдельно; наличие записи не доказывает эквивалентность любого поведения.

| Модуль | Реализация | Исследования | Записи рецептов TT |
| --- | --- | --- | --- |
| `core` | `Config`, `Features`, `ThaumicTinkerer` | `TTENCH_ASCENT_BOOST`, `TTENCH_SLOW_FALL`, `TTENCH_AUTO_SMELT`, `TTENCH_DESINTEGRATE`, `TTENCH_QUICK_DRAW`, `TTENCH_VAMPIRISM`, `TTENCH_FOCUSED`, `TTENCH_DISPERSED`, `TTENCH_FINAL`, `TTENCH_POUNCE`, `TTENCH_SHATTER`, `TTENCH_SHOCKWAVE`, `TTENCH_TUNNEL`, `TTENCH_VALIANCE` | 0 |
| `dark_quartz` | `DarkQuartzFeature` | `DARK_QUARTZ` | 8 |
| `decor` | `DecorFeature` | — | 3 |
| `gas` | `GasFeature` | `BRIGHT_NITOR`, `GASEOUS_SHADOW`, `GASEOUS_LIGHT`, `GAS_REMOVER` | 4 |
| `transvector` | `TransvectorFeature` | `PLATFORM`, `DISLOCATOR`, `INTERFACE` | 4 |
| `funnel` | `FunnelFeature` | `FUNNEL` | 1 |
| `mobilizer` | `MobilizerFeature` | `LEVITATOR_LOCOMOTIVE` | 2 |
| `repairer` | `RepairerFeature` | `REPAIRER` | 1 |
| `foci` | `FociFeature` | `FOCUS_DEFLECT`, `FOCUS_DISLOCATION`, `FOCUS_ENDER_CHEST`, `FOCUS_FLIGHT`, `FOCUS_HEAL`, `FOCUS_SMELT`, `FOCUS_TELEKINESIS` | 7 |
| `enchanting` | `EnchantingFeature` | `ENCHANTER`, `SPELL_CLOTH` | 2 |
| `crops` | `CropsFeature` | — | 6 |
| `elements` | `ElementsFeature` | `FIRE_AER`, `FIRE_PERDITIO`, `FIRE_TERRA`, `FIRE_IGNIS`, `FIRE_ORDO`, `FIRE_AQUA`, `INFUSED_POTIONS` | 10 |
| `attractor` | `AttractorFeature` | `MAGNETS` | 3 |
| `tablet` | `TabletFeature` | `ANIMATION_TABLET` | 1 |
| `gear` | `GearFeature` | `BLOOD_SWORD`, `CLEANSING_TALISMAN`, `REVEALING_HELM`, `SHARE_TOME`, `XP_TALISMAN` | 7 |
| `necromancy` | `NecromancyFeature` | `SUMMON` | 35 |
| `kami` | `KamiFeature` | `DIMENSION_SHARDS`, `ICHOR`, `ICHOR_CLOTH`, `ICHORIUM`, `CAP_ICHOR`, `ROD_ICHORCLOTH`, `ICHORCLOTH_ARMOR`, `ICHOR_TOOLS` | 14 |
| `awakened_tools` | `AwakenedToolsFeature` | `ICHOR_AXE_GEM`, `ICHOR_TOOLS`, `ICHOR_PICK_GEM`, `ICHOR_SHOVEL_GEM`, `ICHOR_SWORD_GEM` | 8 |
| `awakened_armor` | `AwakenedArmorFeature` | `ICHORCLOTH_BOOTS_GEM`, `ICHORCLOTH_CHEST_GEM`, `ICHORCLOTH_HELM_GEM`, `ICHORCLOTH_LEGS_GEM`, `ICHORCLOTH_ARMOR` | 8 |
| `trinkets` | `TrinketsFeature` | `BLOCK_TALISMAN`, `CAT_AMULET`, `ICHOR_POUCH`, `PLACEMENT_MIRROR`, `PROTOCLAY` | 5 |
| `kami_gateway` | `GatewayFeature` | `WARP_GATE` | 2 |
| `kami_foci` | `KamiFociFeature` | `FOCUS_RECALL`, `FOCUS_SHADOWBEAM`, `FOCUS_XP_DRAIN` | 3 |

## Сквозные проверки

- `SmokeGameTests`: загрузка обоих модов и категория базы.
- `ResearchTableGameTests`: 74 исследования, родители, флаги, warp, страницы и разрешение рецептов.
- Строгая инвентаризация: 33 уникальных блока и 114 предметов; без пропусков регистраций, моделей и переводов.
- Runtime-дамп `TcRecipes` сопоставлен со 129 исходными записями. Исключённые интеграционные рецепты не входят в обязательную приёмку.
- Полный перечень исходников и имён сценариев: `tools/backport/audit.json`.

- Независимо извлечены 40 записей станка зачарования (`tools/tt_enchanter.json`): проверяются ID, текстуры, vanilla-флаг, исследования, базовые аспекты и стоимость всех допустимых уровней по формуле TT. Runtime-экспорт — `runtime-enchanter.json`.

## Разобранные отличия

- `ICHOR`: исходные записи содержат x8 у двух компонентов. `InfusionRecipe.areItemStacksEqual` TC4 не сравнивает размер стека, пьедестал ограничен одним предметом; эффективное требование — один осколок каждого вида. Таблица сохраняет x8; сравнение отдельно сообщает эти два случая. GameTest проверяет подбор с единичными осколками, отсутствие одного из них и результат x8.
- Две записи `MOB_MAGNET`: `RecipeHelper.oreDictOrStack` выбирает медь при наличии соответствующего ore dictionary. В 1.21.1 медь существует, поэтому используется `#c:ingots/copper`; золотой fallback не активен.
- Шесть `INFUSED_POTIONS0..5`: metadata оригинальных семян хранится в компоненте `seed_aspect`. Создание данных через `TcStack` проверяется сценариями рецептов и инфузии.
- `INFUSED_POTIONSPOT0..3` используют работающее исследование `INFUSED_POTIONS`, как установлено в `CLAUDE.md`.
- Аргумент типа блока в оригинальном `removeBlocksInIteration` не используется. Column продолжает добывать все подходящие лопате материалы, включая блоки за промежуточным камнем.
- Старшие зачарования станка (Focused/Dispersed/Final Strike, Valiance, Tunnel, Shatter, Shockwave, Pounce) в оригинале требуют только `TTENCH_VAMPIRISM`. Порт ошибочно требовал отдельные исследования. Требования восстановлены по TT, включая повторную одинаковую регистрацию Dispersed Strike, которая не меняет итоговую карту. Регрессионный тест проверяет доступность с одним Vampirism; более младшие зачарования сохраняют свои требования.
- Tree увеличивает `tickCount` собранных предметов на 20 и сохраняет pickup delay. Унаследованное снятие задержки исправлено и покрыто тестом.
- Дополнительный тест подтверждает уже существующий сбор массовых дропов к игроку и сохранение чужих дропов вне области.
- JEI получает subtype семян по аспекту; без него варианты схлопывались в интерфейсе. Отсутствие JEI проверяется отдельным запуском.

## Ручная приёмка

Визуальная сверка моделей и анимаций, полноценное Survival-прохождение, совместная игра двумя клиентами, длительная нагрузка и сравнительные FPS/TPS агентом не подтверждены. Используется `PORTING_CHECKLIST.md`; эти пункты не отмечаются автоматически.
