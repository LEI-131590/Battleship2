# Oportunidades de refabricação

Cada membro refabrica as classes que testou na Parte 1 e o código que desenvolveu no sprint 2.

## Beatriz

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| `Fleet::colisionRisk` | Mysterious Name (erro ortográfico) | Rename | 122668 |
| `Ship::isInsideBoard` | Duplicated Code (`Game.BOARD_SIZE - 1` repetido) | Introduce Variable | 122668 |
| `Caravel`, `Carrack`, `Frigate` (construtores) | Duplicated Code | Pull Up Method (para `Ship`) | 122668 |
| `VoyageWindow::drawLand` | Long Method | Extract Method | 122668 |
| `VoyageWindow::buildScene` | Magic Number (coordenadas dos marcadores) | Introduce Constant | 122668 |
| `VoyageWindow::show` | Long Parameter List | Introduce Parameter Object | 122668 |

##  Lara

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|

## Jessica

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|

## Catarina

| Local | Cheiro no código | Refabricação   | Nº aluno |
|---|---|----------------|----------|
| `Game::printBoard, printShips, printBoardShots, printBoardFrame` | Large Class / responsabilidades diferentes | Extract Class  | 131590   |
| `Game::fireShots` | Método com várias responsabilidades | Extract Method | 131590   |
| `Game::readEnemyFire` | Complex Method | Decompose Conditional | 131590   |
| `Game::getRemainingShips` | Temporary Variable desnecessária | Inline Variable | 131590   |
| `Game::jsonShots` | Redundant Initialization | Remove Redundant Initialization| 131590   |