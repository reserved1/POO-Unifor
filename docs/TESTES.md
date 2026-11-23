# Testes e evidências

Os testes automatizados estão em `test/TestesPlanejamento.java` (Java puro, sem bibliotecas externas).
Cada verificação imprime `OK` ou `FALHOU` com o valor obtido. A saída completa da última execução está em
[`evidencias/saida-testes.txt`](evidencias/saida-testes.txt) (**96 verificações aprovadas, 0 reprovadas**).
As capturas das telas com o caso de referência estão em [`capturas/`](capturas/) (01 a 06: telas; 07: resultado do botão *Simular otimização do evento*; 08 a 13: testes executados na interface — centavos, valor negativo, despesa sem participantes, alteração de orçamento, exclusão de despesa e arquivo inválido).

Como executar: veja a seção *Como reproduzir os testes* do [README](../README.md).

## Caso de referência (seção 4)

Entrada: participantes 1 Ana (R$ 70,00), 2 Bruno (R$ 60,00), 3 Carla (R$ 70,00); despesas Transporte (fixa, R$ 90,00, todos),
Almoço (por pessoa, R$ 20,00, todos), Atividade recreativa (fixa, R$ 30,00, Ana e Carla), Sobremesa (por pessoa, R$ 10,00, todos).

| Pessoa | Transporte | Almoço | Atividade | Sobremesa | Total esperado | Total obtido | Situação obtida |
|---|---|---|---|---|---|---|---|
| Ana | 30,00 | 20,00 | 15,00 | 10,00 | 75,00 | 75,00 | R$ 5,00 Acima do Orçamento |
| Bruno | 30,00 | 20,00 | 0,00 | 10,00 | 60,00 | 60,00 | Dentro do Orçamento |
| Carla | 30,00 | 20,00 | 15,00 | 10,00 | 75,00 | 75,00 | R$ 5,00 Acima do Orçamento |

Custo total esperado R$ 210,00 — obtido R$ 210,00. Evidência: teste 1 em `saida-testes.txt` e captura `capturas/02-relatorio-evento.png`.

## Testes obrigatórios

| # | Cenário | Entrada | Resultado esperado | Resultado obtido | Evidência |
|---|---|---|---|---|---|
| 2 | Centavos no rateio | Despesa fixa de R$ 100,00 para os códigos 3, 1, 2 (fora de ordem) | 33,34 / 33,33 / 33,33; soma 100,00 | 33,34 / 33,33 / 33,33; soma 100,00 | Teste 2 |
| 2b | Centavos (2 centavos restantes) | Despesa fixa de R$ 100,01 para 1, 2, 3 | 33,34 / 33,34 / 33,33 | 33,34 / 33,34 / 33,33 | Teste 2 |
| 3 | Entrada ou vínculo inválido | Despesa sem participantes; valor −10; valor 0; descrição vazia; participante inexistente; nome vazio; orçamento −1; código duplicado; texto "abc" | Recusar com mensagem, sem alterar os dados | Todas recusadas com mensagem; estado igual ao anterior | Teste 3 |
| 4 | Alteração de orçamento | Orçamento de Ana de 70,00 para 80,00 | Custo de Ana 75,00, dentro do limite | 75,00; "Dentro do Orçamento (sobra R$ 5,00)" | Teste 4 |
| 5 | Exclusão de despesa | Excluir "Atividade recreativa" no caso original | 60,00 por pessoa; total 180,00 | 60,00 / 60,00 / 60,00; total 180,00 | Teste 5 |
| 6 | Salvar e reiniciar | Salvar o caso (duas vezes) e carregar em um novo objeto | Recuperar dados, vínculos e resultados sem duplicar | 1 evento, 3 participantes, 4 despesas; mesmos valores; vínculos preservados | Teste 6 |
| 7 | Problema no arquivo | Arquivo ausente; arquivo com texto qualquer | Mensagem; permitir planejamento vazio; falha preserva o planejamento em uso | Mensagens claras; planejamento em uso inalterado; planejamento vazio criado | Teste 7 |

## Testes complementares

| # | Cenário | Resultado obtido |
|---|---|---|
| 8 | Excluir participante com despesas vinculadas | Recusado com a lista das despesas; participante sem despesas pode ser excluído |
| 9 | Editar despesa (incluir Bruno na atividade; mudar de fixa para por pessoa) | Totais recalculados (70,00 cada; depois 90,00 / total 270,00) |
| 10 | Conversão do valor digitado | "70,00" e "70.00" → 70,00; "R$ 1.234,56" → 1234,56; "10,123" recusado |
| 11 | Simulador (seção 7) | Mantém Transporte, Almoço e Atividade; retira Sobremesa; Ana e Carla 65,00, Bruno 50,00, total 180,00; original preservado. Na interface, o botão *Simular otimização do evento* mostra o mesmo resultado (captura `capturas/07-simulacao-otimizacao.png`) |
| 12 | Simulador sem solução (Ana com R$ 40,00) | "Não há solução"; essenciais de Ana 50,00, excesso 10,00 |
| 13 | Limites do simulador | Mais de 10 opcionais e opcional com prioridade 0 são recusados |

## Roteiro manual pela interface (para capturas do relatório)

1. Iniciar o programa com o arquivo `dados/dados.dat` de exemplo (ou copiar `dados/caso-referencia.dat` para `dados/dados.dat`).
   O exemplo já vem classificado para o simulador (transporte e almoço essenciais; atividade prioridade 3; sobremesa prioridade 1), o que não muda os totais.
2. No *Menu inicial*, dar duplo clique em **Passeio da turma**. Conferir na aba *Participantes* os totais 75,00 / 60,00 / 75,00 e o *Total do Evento* R$ 210,00.
3. **Simulador (objetivo extra):** clicar em *Simular otimização do evento*. A mensagem mostra Transporte, Almoço e Atividade recreativa mantidas,
   Sobremesa retirada, Ana e Carla R$ 65,00, Bruno R$ 50,00 e novo custo total R$ 180,00; ao fechar, o relatório continua com R$ 210,00.
   Para o caso sem solução, editar o orçamento de Ana para `40,00` e simular de novo: aparece "Não há solução" com excesso de R$ 10,00 (depois voltar o orçamento para `70,00`).
4. **Alteração de orçamento:** duplo clique em Ana → *Editar Participante* → orçamento `80,00` → *Salvar*. O status de Ana passa a "Dentro do Orçamento (sobra R$ 5,00)".
5. **Exclusão de despesa:** aba *Despesas* → duplo clique em *Atividade recreativa* → *Editar Despesa* → *Excluir Despesa*. Totais passam a 60,00 e total do evento R$ 180,00.
6. **Entrada inválida:** *Cadastrar Nova Despesa* sem marcar participantes, ou com total `-10`, ou com nome vazio → mensagem de erro e nada é alterado.
7. **Exclusão de participante:** abrir Bruno → *Editar Participante* → *Excluir Participante* → mensagem "Não é possível excluir Bruno: há despesas vinculadas".
8. **Salvar e reiniciar:** fechar o programa respondendo *Sim* para salvar e abrir novamente; os dados aparecem iguais.
9. **Problema no arquivo:** com o programa fechado, renomear `dados/dados.dat` (arquivo ausente) ou trocar seu conteúdo por um texto qualquer (arquivo inválido) e abrir o programa: aparece a mensagem e é possível iniciar um planejamento vazio.
