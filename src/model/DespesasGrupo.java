package model;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DespesasGrupo extends Despesas {

    @Serial
    //Implementar a Serializable da classe
    private static final long serialVersionUID = 1L;

    //Sobrescreve o metodo calcular despesa para o grupo com a divisão em rateio:
    //cada participante recebe a parcela truncada e os centavos restantes são distribuídos,
    //um centavo por vez, pela ordem crescente dos ids dos participantes
    public void calcularDespesas() {
        //Verificar se a lista de participantes não e nula ou vazia (evita divisão por zero)
        if (participatesDespesas != null && !participatesDespesas.isEmpty()) {
            //Criação das variáveis utilizadas
            BigDecimal valorPorParticipante;
            BigDecimal quantidadeParticipantes = BigDecimal.valueOf(participatesDespesas.size());

            //Divide o valor total da despesa pelo numero de participate dela
            valorPorParticipante = getValor().divide(quantidadeParticipantes, 2, RoundingMode.DOWN);

            //Calcular a diferença da divisão pelo total da despesa e converter em centavos
            BigDecimal diferencaDivisao = getValor().subtract(valorPorParticipante.multiply(quantidadeParticipantes));
            int centavosRestantes = diferencaDivisao.movePointRight(2).intValueExact();

            //Ordenar os ids dos participantes em ordem crescente
            List<Integer> idsOrdenados = new ArrayList<>(participatesDespesas.keySet());
            Collections.sort(idsOrdenados);

            //Adicionar o valor de cada participante e um centavo a mais enquanto houver centavos restantes
            for (int idParticipante : idsOrdenados) {
                BigDecimal parcelaRateio = valorPorParticipante;
                if (centavosRestantes > 0) {
                    parcelaRateio = parcelaRateio.add(new BigDecimal("0.01"));
                    centavosRestantes--;
                }
                participatesDespesas.put(idParticipante, parcelaRateio);
            }
        }

    }

    //Sobrescreve o metodo de adicionar um participante a despesas e calcular os valores de cada participante da lista de despesa
    @Override
    public void adicionarParticipante(int idParticipanteAdicionar) {
        participatesDespesas.put(idParticipanteAdicionar, new BigDecimal("0"));
        calcularDespesas();
    }

    //Sobrescreve o metodo de remover um participante das despesas e calcular os valores de cada participante da lista de despesa
    @Override
    public boolean removerParticipante(int idParticipanteRemover) {
        if (participatesDespesas.containsKey(idParticipanteRemover)){
            boolean confirmacao = participatesDespesas.remove(idParticipanteRemover) != null;
            calcularDespesas();
            return confirmacao;
        } else {
            return true;
        }
    }

    // construtor de despesas em grupo
    public DespesasGrupo(int id, String nome, BigDecimal valor, boolean essencial, int prioridade, int tipo) {
        super(id, nome, valor, essencial, prioridade, tipo);
    }

}
