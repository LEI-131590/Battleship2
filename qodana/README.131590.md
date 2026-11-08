# Qodana Analysis - Long Parameter List

* **Aluno:** Catarina Barata (131590)
* **Curso:** Licenciatura em Engenharia Informática (LEI) — ISCTE-IUL

---

## 1. Tabela de Mapeamento

| Cheiro (Fowler) | Inspeção no IDE | ID da inspeção | Métrica / Limiar | Limitações |
| :--- | :--- | :--- | :--- | :--- |
| **Long Parameter List** | Method with too many parameters | `MethodWithTooManyParameters` | `NOPM ≥ 4` | Limiar absoluto e fixo no Qodana, ao contrário da abordagem estatística relativa (média e desvio-padrão de um conjunto de sistemas) descrita no livro *Object-Oriented Metrics in Practice*. |

---

## 2. Ligações para as Execuções do GitHub Actions

* **Execução com falha (Cheiro provocado - Pull Request):** (https://github.com/LEI-131590/Battleship2/actions/runs/42513328916/job/128830884907?pr=19)
* **Execução com sucesso (Código corrigido - PR após correção):** https://github.com/LEI-131590/Battleship2/actions/runs/42514644791/job/128834883863?pr=21

---

## 3. Reflexão Crítica

A ferramenta Qodana demonstrou ser eficaz na deteção estática de métodos com excesso de parâmetros no projeto BattleShip2, integrando-se de forma fluida no GitHub Actions através de um quality gate. Observou-se que a parametrização se baseia em limiares absolutos (neste caso, `maxParameterCount: 4`), o que difere ligeiramente da estratégia original proposta por Lanza e Marinescu no livro *Object-Oriented Metrics in Practice*, onde os limiares são calculados de forma relativa face à média do sistema. Não se verificaram falsos negativos nos testes direcionados, mas a definição rigorosa do escopo e das dependências JDK no ficheiro `qodana.yaml` revelou-se fundamental para evitar falhas de compilação na pipeline.
