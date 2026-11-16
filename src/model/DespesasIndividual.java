package model;

import java.math.BigDecimal;
import java.util.Map;

public class DespesasIndividual extends Despesas {

    // Sobrescreve o metodo calcular a despesa individual adicionando o valor da despesas a lista de participantes da despesa
    public void calcularDespesas() {
        if (participatesDespesas != null) {
            for (Map.Entry<Integer, BigDecimal> Entry : participatesDespesas.entrySet()) {
                Entry.setValue(getValor());
            }
        }
    }

    //Sobrescreve o metodo de adicionar um participante a despesas e calcular os valores da despesas da lista de participantes
    @Override
    public void adicionarParticipante(int idParticipanteAdicionar) {
        participatesDespesas.put(idParticipanteAdicionar, new BigDecimal("0"));
        calcularDespesas();
    }

    //Sobrescreve o metodo de remover um participante das despesas e calcular os valores da despesas da lista de participantes
    @Override
    public boolean removerParticipante(int idParticipanteRemover) {
        if (participatesDespesas.containsKey(idParticipanteRemover)) {
            boolean confirmacao = participatesDespesas.remove(idParticipanteRemover) != null;
            calcularDespesas();
            return confirmacao;
        } else {
            return true;
        }
    }

    // construtor de despesas individual
    public DespesasIndividual(int id, String nome, BigDecimal valor, boolean essencial, int prioridade, int tipo) {
        super(id, nome, valor, essencial, prioridade, tipo);
    }
}
