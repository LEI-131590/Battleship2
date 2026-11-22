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
| `PdfReport::addReportContent` | Long Method (gera as estatísticas, o tabuleiro e a legenda do relatório) | Extract Method | 129885 |
| `PdfReport::addCell` | Magic Number (altura da célula definida pelo valor 30f) | Introduce Constant | 129885 |
| `Position::isInside` | Complex Conditional (várias condições para verificar os limites das coordenadas) | Introduce Variable | 129885 |
| `Move::processEnemyFire` | Complex Conditional (condições aninhadas na construção da mensagem de resultados dos tiros) | Decompose Conditional | 129885 |
| `IFleet::getShipsLike`/`Fleet::getShipsLike` | Mysterious Name (nome pouco claro sobre o critério utilizado para selecionar os navios) | Rename | 129885 |

## Catarina

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
