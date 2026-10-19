# Thaumic Tinkerer: Reforged

Порт Thaumic Tinkerer 2.5 (MC 1.7.10, Vazkii, pixlepix, nekosune) как аддона к Thaumcraft 4: Reforged (`~/projects/thaumcraft-4-reforged`, далее база) на Minecraft 1.21.1 и NeoForge. Ветка `mc/1.21.1` переносит весь контент `main`. Логика, числа, рецепты, тексты и ассеты оригинала переносятся 1:1, техническая часть пишется на современных API.

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
- MC 1.21.1, NeoForge 21.1.256, ModDevGradle 2.0.148, JEI 19.57.0.451, TerraBlender 1.21.1-4.1.0.1, Curios 9.5.1+1.21.1, тулчейн Java 21. Gradle запускается на Java 25. Версии совпадают с базой.
- JDK: `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`, системной `java` нет.
- Python 3, скрипты `tools/` без внешних зависимостей.
- База подключена composite build: `includeBuild("../thaumcraft-4-reforged")` в `settings.gradle`, зависимость `implementation "thaumcraft:thaumcraft-4-reforged:1.0"`. Репозиторий базы должен лежать рядом. Gradle собирает его jar сам, в `runClient`, `runServer`, `runGameTestServer` оба мода видны как моды. Изменения необходимого API оформляются отдельными коммитами в ветке базы `mc/1.21.1`.
- `neoforge.mods.toml`: зависимость от `thaumcraft` обязательная, `ordering="AFTER"`, поэтому конструктор аддона выполняется после конструктора базы. Исследования и рецепты регистрируются из конструктора аддона.
- `runGameTestServer` фильтрует namespace `thaumictinkerer`; тесты базы не идут. В 1.21.1 фильтр генераторов смотрит namespace структуры: генератор пишет копию платформы базы как `thaumictinkerer:test/platform`. `TtGameTestHelper` компенсирует сдвиг Y на 1, координаты сценариев остаются как в main.
- Каталоги клиента, сервера и тестов раздельные: `run/client`, `run/server`, `run/gametest`. Для обычного сервера нужен `run/server/eula.txt` с `eula=true`. Тестовый мир в `build/gametestworld`.

## Справочники (gitignore)

- `reference/tt-src/` исходники TT, `tools/fetch_tt.py` качает архив в `tools/cache/` и распаковывает без верхней папки.
- Исходники MC, NeoForge и декомпилят TC4 лежат в `~/projects/thaumcraft-4-reforged/reference`.

## API базы для аддонов

- `AspectSync.addRegistrar(Consumer<AspectRegistry>)`: вызывается в `rebuild` после `ConfigAspects`, до `registerDerived`.
- `WandRegistry.registerCap`/`registerRod`: явные `item` и `texture` ResourceLocation любого namespace, дубликат тега бросает исключение. Ключи исследования `CAP_<tag>`/`ROD_<tag>`, lang `item.Wand.<tag>.cap`/`.rod` аддон добавляет сам.
- `ArcaneCrafting.registerDynamic(DynamicArcaneRecipe)`: добавляется после рецептов жезла и скипетра.
- `Warp.registerItem(ResourceLocation, int)`: изменяемая карта `WARP_ITEMS`.
- `TcArmorMaterial.assetId` принимает полный ResourceLocation ассета брони.
- `TcStack.registerDataHandler(key, BiConsumer<ItemStack, String>)`: `create()` вызывает обработчик для ключа `data`, результат инфузии с таким ключом создаётся через `create()`, а не копией центрального предмета.
- Из базы напрямую используются `thaumcraft.platform.Menus`, `Registration`, `Baubles`, `Transfer`. Остальные швы базы со своими DeferredRegister в её namespace не подходят, у аддона они свои.

## Архитектура

- `platform/` и `client/platform/` содержат всё, где API версий расходится. У аддона свои швы с DeferredRegister в namespace `thaumictinkerer`: `TtStackData`/`TtStackKey` (data components), `TtAttachments`/`TtDataKey`, `TtNetwork` (свой `RegisterPayloadHandlersEvent`), `client/platform/TtClientNetwork`. Игровая логика ходит только через них и через швы базы. `tools/check_seams.py` падает на запрещённых импортах вне швов и на импорте `thaumcraft.client.*` вне `client/`.
- Регистрация: `registry/` (`ModItems`, `ModBlocks` с `withItem`, `ModBlockEntities`, `ModMenus`, `ModEntities`, `ModEffects`, `ModSounds`, `ModCreativeTabs`), пакеты в `network/ModPayloads`, клиентские хендлеры в `client/ClientPayloadHandlers`, клиентская точка входа `client/ThaumicTinkererClient`.
- Конфиг `Config` только SERVER (синхронизация NeoForge), ключи и умолчания как в `ConfigHandler` TT: `general.kami.forceenabled`, `general.imbuedFire.enabled`, `general.imbuedFire.cake.enabled`, `general.shareTome.survival.enabled`, `general.cropsAllowBonemeal.enabled` (false), `general.modFlight.enabled`, `general.placementMirror.blocks.show`, `general.tooltipIndicators.enabled`.
- Meta TT раскладывается в отдельные id или свойства blockstate по `tools/id_map.json`.

## Раскладка по фичам

Образец: `darkquartz/DarkQuartzFeature`, `test/DarkQuartzGameTests`, `tools/gen_dark_quartz.py`, `tools/lang/dark_quartz/`.

- Фича живёт в `thaumictinkerer/<feature>/` и отдаёт один класс `<Feature>Feature`: статические `DeferredHolder` в общие реестры (`ModItems.ITEMS`, `ModBlocks.BLOCKS` через `ModBlocks.withItem`, `ModBlockEntities` и т.д.), методы `load()` (пустой, нужен для загрузки класса), `research()`, `recipes()`, при необходимости `aspects(AspectRegistry)`.
- В общих файлах фича добавляет по одной строке: `Features.java` (в `load`, `research`, `recipes`, `aspects`, `tests`), `client/ClientFeatures.java` (экраны, рендереры), `tools/features.txt` (имя генератора). Эти файлы с `merge=union` в `.gitattributes`, строки писать отдельными операторами. Клиентские хуки фичи в `client/<feature>/`, прочие клиентские события через собственный `@EventBusSubscriber(value = Dist.CLIENT)`.
- `Features.load()` вызывается в конструкторе до привязки реестров к шине, `recipes()` и `research()` после (рецепты первыми, как в базе), aspects через `AspectSync.addRegistrar(Features::aspects)`.
- Генератор `tools/gen_<feature>.py` экспортирует `generate(ctx)` (`ctx` это модуль `gen_resources`: `generated_item`, `block_item`, `cube_all`, `cube_column`, `cube_bottom_top`, `simple_state`, `blockstate`, `self_drop`, `mineable`, `shaped`, `shapeless`, `smelting`, `tag_both`, `stairs(name, top, side)`, `slab(name, top, side, double_model)`). Ручной lang в `tools/lang/<feature>/{en_us,ru_ru}.json`, ru_ru берётся из переведённого TT, недостающее переводится самостоятельно.
- GameTest фичи в `test/<Feature>GameTests.java` на `TtGameTestSuite` (реализация через `@GameTestGenerator`) (`new TtGameTestSuite("name").add("test", body)`, регистрация `register(bus)` строкой в `Features.tests`). Общие проверки инфраструктуры в `CoreGameTests`.
- Новая фича не правит `ModItems`/`ModBlocks`/`gen_resources.py`/`convert_assets.py`, если нужного хелпера нет, его добавляют отдельным коммитом.

## Исследования

- `research/TtResearch`: категория `TT_CATEGORY` (регистрируется в `TtResearch.init()`), фабрики `item(...)`/`kami(...)` и страницы `text(i)`, `recipe`, `arcane`, `infusion(name[, count])`, `enchant`, `crucible`. Порт кода TT почти построчный: `new ResearchPage("0")` это `TtResearch.text(0)`, `ResearchHelper.recipePage(KEY)` это `TtResearch.recipe(KEY)`.
- Ключи страниц в базе нумеруются с 1, в TT с 0. `convert_assets.py` сдвигает `ttresearch.page.KEY.N` в `tc.research_page.KEY.N+1`, а `TtResearch.text(i)` в Java принимает индекс TT и сам ставит `i+1`. Нечисловые страницы TT (`FOCUS_ENDER_CHEST.ES`, вариант Ender Storage) остаются под `ttinkerer.`.
- `TtResearchItem` при странице инфузии добавляет скрытого родителя `INFUSION` (кроме KAMI), `.when(BooleanSupplier)` прячет исследование по конфигу, `.setWarp(n)` из базы. Сложность передаётся как в TT (5), база сама ограничивает до 3.
- `KamiResearchItem`: concealed, скрытые родители считаются лениво в `parentsHidden()` (все исследования `TT_CATEGORY`, BASICS, GOLEMANCY, ARTIFICE, ALCHEMY, THAUMATURGY без потерянных, виртуальных, триггерных, без родителей, KAMI, MINILITH*, *KAMI), потому что конструктор аддона идёт до тегов и фильтр `isAvailable` в конструкторе даёт неверный результат. Исследование доступно только при `Config.kamiEnabled()`, KAMI-контент регистрируется всегда.
- Конфиг в конструкторе недоступен: условия читаются в момент запроса (`Config.kamiEnabled()`, `imbuedFire()` и т.д. возвращают умолчание, пока `SPEC` не загружен). Блоки и предметы регистрируются всегда, отключается доступность исследований и поведение.
- Префиксы `[TT]`/`[TTKami]` в тексте исследования воспроизведены ключами `ttinkerer.research_text(_kami).KEY`, которые `convert_assets.py` строит из `tc.research_text.KEY`; `textKey()` выбирает их при `Config.tooltipIndicators()`.
- `tools/extract_tt_research.py` строит `tools/tt_research.json` из исходников TT (74 исследования), GameTest `research_matches_tt_table` сверяет с ним реестр: позиции, родители, аспекты, флаги, warp, страницы. Сложность в таблице уже ограничена до 1..3, ключи рецептов страниц сопоставляются с суффиксом `_<...>` (SUMMON, DARK_QUARTZ0_COAL). После правки исследования таблицу не менять руками, а сверять с TT.
- `TtResearch.kamiResearch()`/`setKamiResearch` хранит исследование KAMI для команды `KamiUnlocked`.

## Рецепты

- `crafting/TtRecipes` поверх `TcRecipes.register`: `arcane(key, research, result, aspects, pattern, 'A', ingredient, ...)`, `arcaneShapeless`, `infusion(key[, research], result, instability, aspects, central, components...)`, `crucible(key, result, catalyst, aspects)`, `vanilla(key, research, recipePath, result)` (`CraftingRecipeRef` на обычный рецепт из `data/thaumictinkerer/recipe`). Ключи TT сохраняются как есть, повтор ключа (в том числе с базой) бросает `IllegalStateException`.
- Обычные рецепты верстака пишет генератор (`ctx.shaped`), зеркальные варианты TT (лестницы) не дублируются, ваниль зеркалит сама. Если у TT два рецепта с одним ключом, регистрируются оба ключа (`DARK_QUARTZ0`, `DARK_QUARTZ0_COAL`) и обе ссылки идут в одну страницу.
- `python3 tools/extract_tt_recipes.py --compare <дамп>` строит `tools/tt_recipes.json` из TT и сверяет с дампом рецептов `TcRecipes.all()` (дамп пишет временный GameTest в JSON-формате `tools/compare_recipes.py`). Рецепты тигля `INFUSED_POTIONSPOT0..3` в TT привязаны к несуществующему исследованию и в TC4 никогда не срабатывают, поэтому у них исследование `INFUSED_POTIONS`. `INFUSED_INKWELL` без исследования намеренно как в TT.
- `python3 tools/tc4_ref.py ConfigItems.itemResource 14` печатает id и состояние базы (`ConfigBlocks.x meta`, `Items.x meta`, `Blocks.x`, имя oredict, мета `*` для всех). `--all` пишет `tools/tc4_refs.json` (коммитится). Oredict даёт теги `#c:...` как в `convert_recipes.py` базы.

## Прочее

- `TtRarity.kami()` это `Rarity` KAMI (светло-фиолетовый) через расширение enum NeoForge: `META-INF/enumextensions.json`, ключ `enumExtensions` в `neoforge.mods.toml` обязателен. Вызывать только как метод, поле `Rarity` в статике нельзя (циклическая инициализация).
- Пробуждённая кирка не ломает бедрок (измерение Bedrock исключено), текст второй страницы ICHOR_PICK_GEM переопределён в `tools/lang/awakened_tools`. Режим пробуждённого инструмента хранится в `CUSTOM_MODEL_DATA` (целое число), от него же выбирается модель через legacy overrides. Души хранит вложение `soul_hearts` с синхронизацией владельцу.
- `IchorPouchItem` наследует `FocusPouchItem` базы и переопределяет `pouchSize` (117) и `canStore` (всё, кроме мешочков), как `getInventory` у TT: все слоты в `FocusKeys.POUCH`, переключатель жезла видит фокусы в любом слоте.
- Вкладка творчества своя, иконка `dark_quartz` пока, потом ичор.
- Аддон-регистратор аспектов один (`AspectSync.addRegistrar(Features::aspects)`), фича добавляет строку в `Features.aspects`. Для предметов без аспектов TT база выводит аспекты по рецептам сама.
- У кристаллов измерений аспектов в TT нет; по решению пользователя порт задаёт их явно в `KamiFeature.aspects`: обоим Vitreus 1 и Praecantatio 1, кристаллу Нижнего мира Ignis 2 и Perditio 2, кристаллу Края Alienis 2 и Iter 2.

## Генерируемые файлы (руками не править)

| Файл | Генератор |
|---|---|
| `src/main/resources/assets/thaumictinkerer` (текстуры, lang) | `tools/convert_assets.py` (+ `tools/assets_extra/`, `tools/lang/`) |
| `src/generated/resources` (перезаписывается целиком) | `tools/gen_resources.py` |
| `reference/tt-src` | `tools/fetch_tt.py` |

- После правки lang: `convert_assets.py`, затем `gen_resources.py`. Ручные переводы только en_us и ru_ru в `tools/lang/<feature>/`, общие в `tools/lang/<lang>.json`.
- Ключи lang: блоки `block.thaumictinkerer.<id>`, предметы `item.thaumictinkerer.<id>`, исследования `tc.research_name.KEY`, `tc.research_text.KEY`, `tc.research_page.KEY.N`, категории `tc.research_category.TT_*`, компоненты жезлов `item.Wand.<TAG>.cap`/`.rod`. Остальные ключи TT (`ttmisc`, `ttwaila`, `ttresearch.prefix`) сохраняются с префиксом `ttinkerer.`, `convert_assets.py` печатает их сводку.
- Текстуры копируются с переименованием `blocks` в `block`, `items` в `item`, имена в нижнем регистре. `model`, `gui`, `misc`, `enchants` сохраняют имена папок.
- Профили формата ресурсов 26.3 и 1.21.1 в `tools/mcformat.py`; ресурсы ветки генерируются для 1.21.1.

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
5. GameTest на ключевую логику в `test/`, регистрация через `TtGameTestSuite`, который сохраняет имена и изоляцию сценариев на API 1.21.1.

## Проверки

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@25
python3 tools/fetch_tt.py
python3 tools/convert_assets.py
python3 tools/gen_resources.py && git status --short src/generated
python3 tools/check_seams.py
python3 tools/inventory.py --check --strict
python3 tools/validate_backport.py
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

## Особенности API 1.21.1

- Предметы и блоки регистрируются через `TtRegistration`; обработчик свойств вычисляется внутри supplier регистрации. Раннее вычисление обращается к ещё не привязанным эффектам.
- `TtBlock.onRemove` вызывает `TcBlockEntity.beforeRemove`; без этого теряется содержимое механизмов. При перемещении блоков сначала отсоединяется их block entity.
- `TtCamoBlockEntity` и `TtCropBlockEntity` содержат два безопасных серверных типа `ModelData`/`ModelProperty`, несмотря на `client` в пакете NeoForge. Только эти импорты в этих двух файлах разрешены проверкой швов.
- `TtAttachments` синхронизирует вложения собственным payload; адресаты self/all и повторная отправка при входе, возрождении и смене измерения сохраняются.
- База 1.21.1 не имеет настройки отключения зеркал; `TtBaseConfig.allowMirrors()` возвращает true, как фактическое поведение базы.
- Модели предметов через `models/item`, predicates и overrides, окраска через `RegisterColorHandlersEvent`. Специальные предметы через `TtSpecialItems`, крылья через слой `PlayerRenderer`.
- JEI необязателен. `client/jei` задаёт subtype семян по аспекту, чтобы варианты не схлопывались. `-PwithoutJei` убирает JEI из dev runtime.
- Процессы Gradle базы и аддона, использующие один checkout базы, не запускать одновременно: их общий output может быть пересобран во время упаковки JAR.
