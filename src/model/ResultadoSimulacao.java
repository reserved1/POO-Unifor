package model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Resultado do simulador de alternativas (não altera o planejamento original)
public class ResultadoSimulacao {

    private final boolean possuiSolucao;
    private final List<Despesas> despesasMantidas;
    private final List<Despesas> opcionaisMantidas;
    private final List<Despesas> despesasRetiradas;
    private final Map<Integer, BigDecimal> totaisPorParticipante;
    private final BigDecimal custoTotal;
    private final int somaPrioridades;
    private final Map<Integer, BigDecimal> excessoEssenciais;

    public ResultadoSimulacao(boolean possuiSolucao, List<Despesas> despesasMantidas, List<Despesas> opcionaisMantidas,
                              List<Despesas> despesasRetiradas, Map<Integer, BigDecimal> totaisPorParticipante,
                              BigDecimal custoTotal, int somaPrioridades, Map<Integer, BigDecimal> excessoEssenciais) {
        this.possuiSolucao = possuiSolucao;
        this.despesasMantidas = despesasMantidas;
        this.opcionaisMantidas = opcionaisMantidas;
        this.despesasRetiradas = despesasRetiradas;
        this.totaisPorParticipante = totaisPorParticipante;
        this.custoTotal = custoTotal;
        this.somaPrioridades = somaPrioridades;
        this.excessoEssenciais = excessoEssenciais;
    }

    public boolean isPossuiSolucao() {
        return possuiSolucao;
    }

    public List<Despesas> getDespesasMantidas() {
        return Collections.unmodifiableList(despesasMantidas);
    }

    public List<Despesas> getOpcionaisMantidas() {
        return Collections.unmodifiableList(opcionaisMantidas);
    }

    public List<Despesas> getDespesasRetiradas() {
        return Collections.unmodifiableList(despesasRetiradas);
    }

    //Totais por participante: com solução são os novos totais; sem solução são os totais só das essenciais
    public Map<Integer, BigDecimal> getTotaisPorParticipante() {
        return Collections.unmodifiableMap(totaisPorParticipante);
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public int getSomaPrioridades() {
        return somaPrioridades;
    }

    //Participantes cujas despesas essenciais já passam do orçamento e o valor excedido
    public Map<Integer, BigDecimal> getExcessoEssenciais() {
        return Collections.unmodifiableMap(excessoEssenciais);
    }

    //Gera um texto com o resultado para exibição
    public String descrever(Evento evento) {
        StringBuilder texto = new StringBuilder();
        if (!possuiSolucao) {
            texto.append("Não há solução: as despesas essenciais não cabem no orçamento.\n");
            for (Map.Entry<Integer, BigDecimal> entrada : excessoEssenciais.entrySet()) {
                Participante participante = evento.getListaParticipantes().get(entrada.getKey());
                texto.append(String.format("  %s: essenciais R$ %.2f, orçamento R$ %.2f, excesso R$ %.2f%n",
                        participante.getNome(), totaisPorParticipante.get(entrada.getKey()), participante.getOrcamento(), entrada.getValue()));
            }
            return texto.toString();
        }
        texto.append("Despesas mantidas: ").append(nomes(despesasMantidas)).append('\n');
        texto.append("Despesas retiradas: ").append(nomes(despesasRetiradas)).append('\n');
        texto.append("Soma das prioridades mantidas: ").append(somaPrioridades).append('\n');
        for (Map.Entry<Integer, BigDecimal> entrada : totaisPorParticipante.entrySet()) {
            Participante participante = evento.getListaParticipantes().get(entrada.getKey());
            texto.append(String.format("  %s: R$ %.2f (orçamento R$ %.2f)%n", participante.getNome(), entrada.getValue(), participante.getOrcamento()));
        }
        texto.append(String.format("Novo custo total: R$ %.2f%n", custoTotal));
        return texto.toString();
    }

    //Metodo interno para juntar os nomes das despesas
    private String nomes(List<Despesas> despesas) {
        if (despesas.isEmpty()) {
            return "nenhuma";
        }
        StringBuilder texto = new StringBuilder();
        for (Despesas despesa : despesas) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(despesa.getNome());
        }
        return texto.toString();
    }
}
