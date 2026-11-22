# Oportunidades de refabricação

Cada membro refabrica as classes que testou na Parte 1 e o código que desenvolveu no sprint 2.

## Beatriz

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| `Fleet::colisionRisk` | Mysterious Name (erro ortográfico) | Rename | 122668 |
| `Ship::isInsideBoard` | Complex Conditional (`Game.BOARD_SIZE - 1` repetido) | Introduce Variable | 122668 |
| `Caravel`, `Carrack`, `Frigate` (construtores) | Duplicated Code | Extract Method + Pull Members Up (para `Ship`) | 122668 |
| `VoyageWindow::drawLand` | Long Method | Extract Method | 122668 |
| `Fleet::printShips` | Feature Envy (`MethodMayBeStatic`: não usa o estado da `Fleet`) | Make Static | 122668 |
| `VoyageWindow::show` | Long Parameter List | Introduce Parameter Object | 122668 |

##  Lara

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| `HistoryDB::saveMove` | Magic String (query SQL hardcoded) | Introduce Constant | 129896 |
| `HistoryDB::initializeDatabase` | Mysterious Name (variável `sql` pouco descritiva) | Rename | 129896 |
| `Tasks::menu` | Long Method | Extract Method | 129896 |
| `Tasks::buildFleet` | Redundant Local Variable (`success` usada apenas uma vez) | Inline | 129896 |
| `Tasks::readClassicPosition` | Complex Conditional (`input.matches("[A-Z]\\d+")`) | Introduce Variable | 129896 |
| `Tasks::exportPdfReport` | Hardcoded Parameter (caminho do ficheiro PDF) | Introduce Parameter | 129896 |

## Jessica

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|

## Catarina

| Local | Cheiro no código | Refabricação   | Nº aluno |
|---|---|---|---|
| `Game::printBoard, printShips, printBoardShots, printBoardFrame` | Large Class / responsabilidades diferentes | Extract Class  | 131590 |
| `Game::fireShots` | Método com várias responsabilidades | Extract Method | 131590 |
| `Game::readEnemyFire` | Complex Method | Decompose Conditional | 131590 |
| `Game::getRemainingShips` | Temporary Variable desnecessária | Inline Variable | 131590 |
| `Game::jsonShots` | Redundant Initialization | Remove Redundant Initialization| 131590 |
| `Game::fireSingleShot` | Nested Conditional | Replace Nested Conditional with Guard Clauses| 131590 |