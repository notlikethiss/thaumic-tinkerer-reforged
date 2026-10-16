# Thaumic Tinkerer: Reforged

Порт Thaumic Tinkerer 2.5 (MC 1.7.10, Vazkii, pixlepix, nekosune) как аддона к Thaumcraft 4: Reforged (`~/projects/thaumcraft-4-reforged`, далее база) на Minecraft 26.3 и NeoForge. Логика, числа, рецепты, тексты и ассеты оригинала переносятся 1:1, техническая часть пишется на современных API.

Файл описывает, как развивать и портировать мод. Статусы фаз, счётчики тестов, даты прогонов и пути к логам сюда не пишутся. Новая строка добавляется только если без неё агент сделает ошибку: неочевидное правило API, обязательный порядок шагов или намеренное отступление от TT, которое иначе «починят» обратно.

## Принципы

- Источник истины по поведению: исходники TT 2.5 в `reference/tt-src` (ветка `1.7-final`, `tools/fetch_tt.py`). Код TT читается как данные, из него ничего не запускается.
- KAMI входит в порт. Конфиг `general.kami.forceenabled` по умолчанию `true`.
- Измерение Bedrock исключено, `bedrockPortal` в `id_map.json` помечен skip. Блоки только для ComputerCraft (`aspectAnalyzer`, `golemConnector`) исключены, `remotePlacer` и `mobDisplay` отключены в самом TT.
- Интеграции с другими модами исключены, кроме JEI.
- Баги TT чиним только если они ломают игру (дюп, потеря предметов, NPE, рассинхрон, краш сервера). Игровые особенности оставляем.
- Сервер проверяет то, чему TT верил с клиента. Клиентские классы на сервер не протекают, всё должно работать на выделенном сервере.
- Ресурсы делают Python-генераторы в `tools/`, datagen не используется.
- Лицензия оригинала CC BY-NC-SA 3.0, у порта `All Rights Reserved` для личного использования.

## Окружение

- Мод `thaumictinkerer`, пакет `thaumictinkerer`, namespace ассетов `thaumictinkerer`, архив `thaumic-tinkerer-reforged`, версия 1.0.
- MC 26.3, NeoForge 26.3.0.57-beta, ModDevGradle 2.0.148, JEI 31.9.0.58, TerraBlender 26.3-26.3.0.0.9, Java 25. Версии в `gradle.properties` совпадают с базой.
- JDK: `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`, системной `java` нет.
- Python 3, скрипты `tools/` без внешних зависимостей.
- База подключена composite build: `includeBuild("../thaumcraft-4-reforged")` в `settings.gradle`, зависимость `implementation "thaumcraft:thaumcraft-4-reforged:1.0"`. Репозиторий базы должен лежать рядом. Gradle собирает его jar сам, в `runClient`, `runServer`, `runGameTestServer` оба мода видны как моды. Базу из аддона не правим.
- `neoforge.mods.toml`: зависимость от `thaumcraft` обязательная, `ordering="AFTER"`, поэтому конструктор аддона выполняется после конструктора базы. Исследования и рецепты регистрируются из конструктора аддона.
- `runGameTestServer` запускается с `--tests thaumictinkerer:*`, тесты базы не идут. Структура тестов `thaumcraft:test/platform` берётся из базы.
- В `run/` нужен `eula.txt` с `eula=true` (в gitignore).

## Справочники (gitignore)

- `reference/tt-src/` исходники TT, `tools/fetch_tt.py` качает архив в `tools/cache/` и распаковывает без верхней папки.
- Исходники MC, NeoForge и декомпилят TC4 лежат в `~/projects/thaumcraft-4-reforged/reference`.

## API базы для аддонов

- `AspectSync.addRegistrar(Consumer<AspectRegistry>)`: вызывается в `rebuild` после `ConfigAspects`, до `registerDerived`.
- `WandRegistry.registerCap`/`registerRod`: явные `item` и `texture` Identifier любого namespace, дубликат тега бросает исключение. Ключи исследования `CAP_<tag>`/`ROD_<tag>`, lang `item.Wand.<tag>.cap`/`.rod` аддон добавляет сам.
- `ArcaneCrafting.registerDynamic(DynamicArcaneRecipe)`: добавляется после рецептов жезла и скипетра.
- `Warp.registerItem(Identifier, int)`: изменяемая карта `WARP_ITEMS`.
- `TcArmorMaterial.assetId` принимает полный Identifier ассета брони.
- `TcStack.registerDataHandler(key, BiConsumer<ItemStack, String>)`: `create()` вызывает обработчик для ключа `data`, результат инфузии с таким ключом создаётся через `create()`, а не копией центрального предмета.
- Из базы напрямую используются `thaumcraft.platform.Menus`, `Registration`, `Baubles`, `Transfer`. Остальные швы базы со своими DeferredRegister в её namespace не подходят, у аддона они свои.

## Архитектура

- `platform/` и `client/platform/` содержат всё, где API версий расходится. У аддона свои швы с DeferredRegister в namespace `thaumictinkerer`: `TtStackData`/`TtStackKey` (data components), `TtAttachments`/`TtDataKey`, `TtNetwork` (свой `RegisterPayloadHandlersEvent`), `client/platform/TtClientNetwork`. Игровая логика ходит только через них и через швы базы. `tools/check_seams.py` падает на запрещённых импортах вне швов и на импорте `thaumcraft.client.*` вне `client/`.
- Регистрация: `registry/` (`ModItems`, `ModBlocks` с `withItem`, `ModBlockEntities`, `ModMenus`, `ModEntities`, `ModEffects`, `ModSounds`, `ModCreativeTabs`), пакеты в `network/ModPayloads`, клиентские хендлеры в `client/ClientPayloadHandlers`, клиентская точка входа `client/ThaumicTinkererClient`.
- Конфиг `Config` только SYNCED, ключи и умолчания как в `ConfigHandler` TT: `general.kami.forceenabled`, `general.imbuedFire.enabled`, `general.imbuedFire.cake.enabled`, `general.shareTome.survival.enabled`, `general.cropsAllowBonemeal.enabled` (false), `general.modFlight.enabled`, `general.placementMirror.blocks.show`, `general.tooltipIndicators.enabled`.
- Meta TT раскладывается в отдельные id или свойства blockstate по `tools/id_map.json`.

## Раскладка по фичам

- Каждая фича живёт в `thaumictinkerer/<feature>/` со своим `tools/gen_<feature>.py` и `tools/lang/<feature>/{en_us,ru_ru}.json`, чтобы параллельная работа не конфликтовала.
- В общих реестрах (`ModItems`, `ModBlocks`, `ModBlockEntities`, `ModMenus`, `ModEntities`, `ModEffects`, `ModSounds`, `ModPayloads`) фича добавляет по одной строке.
- Генератор фичи подключается строкой в `FEATURES` в `tools/gen_resources.py`. Модуль экспортирует `generate(ctx)`, где `ctx` это модуль `gen_resources` с хелперами (`generated_item`, `block_item`, `cube_all`, `simple_state`, `self_drop`, `mineable`, `shaped`, `shapeless`, `smelting`, `tag_both`, `stairs`, `slab`).
- Исследования и рецепты фичи регистрируются из её `init()`, вызываемого из конструктора `ThaumicTinkerer`.

## Генерируемые файлы (руками не править)

| Файл | Генератор |
|---|---|
| `src/main/resources/assets/thaumictinkerer` (текстуры, lang) | `tools/convert_assets.py` (+ `tools/assets_extra/`, `tools/lang/`) |
| `src/generated/resources` (перезаписывается целиком) | `tools/gen_resources.py` |
| `reference/tt-src` | `tools/fetch_tt.py` |

- После правки lang: `convert_assets.py`, затем `gen_resources.py`. Ручные переводы только en_us и ru_ru в `tools/lang/<feature>/`, общие в `tools/lang/<lang>.json`.
- Ключи lang: блоки `block.thaumictinkerer.<id>`, предметы `item.thaumictinkerer.<id>`, исследования `tc.research_name.KEY`, `tc.research_text.KEY`, `tc.research_page.KEY.N`, категории `tc.research_category.TT_*`, компоненты жезлов `item.Wand.<TAG>.cap`/`.rod`. Остальные ключи TT (`ttmisc`, `ttwaila`, `ttresearch.prefix`) сохраняются с префиксом `ttinkerer.`, `convert_assets.py` печатает их сводку.
- Текстуры копируются с переименованием `blocks` в `block`, `items` в `item`, имена в нижнем регистре. `model`, `gui`, `misc`, `enchants` сохраняют имена папок.
- Профиль формата ресурсов в `tools/mcformat.py` (только 26.3).

## id_map.json

- Правится руками. Строка: `old` (имя регистрации TT), `meta`, `lang` (ключ en_US.lang или null), `id`, `state` (только блоки), `skip` (причина или null).
- Id это snake_case имени регистрации TT, если оно говорящее. Общие имена (`interface`, `spawner`, `magnet`, `platform`) заменены на имена из lang (`transvector_interface`, `necromancy_tablet`, `kinetic_attractor`, `ethereal_platform`).
- Meta, означающая другую вещь, получает свой id (`kamiResource` 0..7 это `ichor`, `ichor_cloth`, `ichorium_ingot`, `ichorium_nugget`, `ichorium_cap`, `ichorcloth_rod`, `nether_shard`, `ender_shard`; `darkQuartz` 0..2 это блок, резной блок, колонна). Meta, означающая состояние той же вещи, становится компонентом или свойством (`xpTalisman` 0/1 это один `xp_talisman`).
- `mobAspect`: мета 0..16 это `<aspect>_soul_aspect`, 21..37 `condensed_<aspect>_soul_aspect`, 42..58 `infused_<aspect>_soul_aspect`, где aspect это тег аспекта TC4 (`aqua`, `humanus`, `aer`, `volatus`, `ignis`, `praecantatio`, `exanimis`, `corpus`, `bestia`, `venenum`, `terra`, `alienis`, `iter`, `metallum`, `limus`, `pannus`, `messis`).
- Двойные плиты это та же плита со `state: {"type": "double"}`.
- Несколько строк могут делить один lang-ключ (`mobAspect`, `infusedGrain`), `convert_assets.py` раздаёт текст всем id.
- Skip с причиной у отключённого, исключённого или служебного. Каждый tile/item `.name` ключ en_US.lang должен быть покрыт строкой, это проверяет `inventory.py --check`.

## Порт механики из TT

1. Найти класс в `reference/tt-src`, id и состояния взять из `tools/id_map.json`.
2. Поведение базы смотреть в её `CLAUDE.md` и коде, расширять только через API базы выше.
3. Версионно-зависимые вызовы спрятать в шов.
4. Ресурсы через генератор фичи, тексты по ключам lang (строки, захардкоженные в TT, выносить в `tools/lang/<feature>`).
5. GameTest на ключевую логику в `test/`, регистрация по образцу `SmokeGameTests` (`DeferredRegister` на `Registries.TEST_FUNCTION`, своё окружение, структура `thaumcraft:test/platform`).

## Проверки

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@25
python3 tools/fetch_tt.py
python3 tools/convert_assets.py
python3 tools/gen_resources.py && git status --short src/generated
python3 tools/check_seams.py
python3 tools/inventory.py --check
./gradlew build
./gradlew runGameTestServer
```

- Повторная генерация не должна менять файлы.
- `inventory.py --check` печатает, чего не хватает (регистрации, модели, lang), и завершается с 0. `--strict` падает при любой нехватке, схема `id_map.json` проверяется всегда. `--all` печатает полные списки.
- Сервер: `tail -f cmds | ./gradlew runServer` (`cmds` в gitignore).
- Визуальную проверку в `runClient` и игру двумя клиентами делает пользователь.

## Код-стайл

- Java: 4 пробела, без комментариев.
- Python: 4 пробела, двойные кавычки, без комментариев, `pathlib`.
- Коммиты на английском `feat:`/`fix:`, без строк соавторства.
