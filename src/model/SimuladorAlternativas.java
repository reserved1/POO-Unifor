package model;

import java.math.BigDecimal;
import java.util.*;

//Simulador de alternativas (objetivo extra):
//mantém todas as despesas essenciais e escolhe o conjunto de despesas opcionais com a maior soma de
//prioridades que respeite o orçamento de cada participante. O evento original não é alterado.
//
//Algoritmo: busca exaustiva (força bruta) sobre os 2^n subconjuntos de opcionais (n <= 10, no máximo 1024
//combinações). Para cada subconjunto soma as parcelas das essenciais e das opcionais escolhidas por
//participante; o subconjunto é viável se nenhum total passar do orçamento. Entre os viáveis fica o de maior
//soma de prioridades (empate: o de menor custo total).
public class SimuladorAlternativas {

    //Quantidade máxima de despesas opcionais aceitas
    public static final int MAXIMO_OPCIONAIS = 10;

    public ResultadoSimulacao simular(Evento evento) {
        //Separar despesas essenciais e opcionais em ordem de id
        List<Despesas> essenciais = new ArrayList<>();
        List<Despesas> opcionais = new ArrayList<>();
        List<Despesas> todas = new ArrayList<>(evento.getListaDespesas().values());
        todas.sort(Comparator.comparingInt(Despesas::getId));
        for (Despesas despesa : todas) {
            if (despesa.isEssencial()) {
                essenciais.add(despesa);
            } else {
                opcionais.add(despesa);
            }
        }

        //Validar as entradas do simulador
        if (opcionais.size() > MAXIMO_OPCIONAIS) {
            throw new RegraNegocioException("O simulador aceita no máximo " + MAXIMO_OPCIONAIS + " despesas opcionais.");
        }
        for (Despesas opcional : opcionais) {
            if (opcional.getPrioridade() < 1) {
                throw new RegraNegocioException("A despesa opcional \"" + opcional.getNome() + "\" precisa de prioridade inteira positiva (1 a 9).");
            }
        }

        //Calcular o total das despesas essenciais de cada participante
        Map<Integer, Participante> participantes = new TreeMap<>(evento.getListaParticipantes());
        Map<Integer, BigDecimal> totaisEssenciais = new TreeMap<>();
        for (int idParticipante : participantes.keySet()) {
            BigDecimal total = new BigDecimal("0.00");
            for (Despesas essencial : essenciais) {
                total = total.add(essencial.getValorParticipante(idParticipante));
            }
            totaisEssenciais.put(idParticipante, total);
        }

        //Verificar se as essenciais cabem no orçamento de cada participante
        Map<Integer, BigDecimal> excessoEssenciais = new TreeMap<>();
        for (Participante participante : participantes.values()) {
            BigDecimal excesso = totaisEssenciais.get(participante.getId()).subtract(participante.getOrcamento());
            if (excesso.compareTo(BigDecimal.ZERO) > 0) {
                excessoEssenciais.put(participante.getId(), excesso);
            }
        }
        if (!excessoEssenciais.isEmpty()) {
            return new ResultadoSimulacao(false, essenciais, new ArrayList<>(), opcionais, totaisEssenciais, somar(totaisEssenciais), 0, excessoEssenciais);
        }

        //Busca exaustiva sobre todos os subconjuntos de opcionais
        int quantidade = opcionais.size();
        int melhorMascara = -1;
        int melhorPrioridade = -1;
        BigDecimal melhorCusto = null;
        Map<Integer, BigDecimal> melhoresTotais = null;

        for (int mascara = 0; mascara < (1 << quantidade); mascara++) {
            Map<Integer, BigDecimal> totais = new TreeMap<>(totaisEssenciais);
            int somaPrioridades = 0;
            for (int i = 0; i < quantidade; i++) {
                if ((mascara & (1 << i)) != 0) {
                    Despesas opcional = opcionais.get(i);
                    somaPrioridades += opcional.getPrioridade();
                    for (int idParticipante : totais.keySet()) {
                        totais.put(idParticipante, totais.get(idParticipante).add(opcional.getValorParticipante(idParticipante)));
                    }
                }
            }

            //Verificar se todos os participantes ficam dentro do orçamento
            boolean viavel = true;
            for (Participante participante : participantes.values()) {
                if (totais.get(participante.getId()).compareTo(participante.getOrcamento()) > 0) {
                    viavel = false;
                    break;
                }
            }

            //Guardar a melhor combinação (maior prioridade; empate pelo menor custo)
            if (viavel) {
                BigDecimal custo = somar(totais);
                if (somaPrioridades > melhorPrioridade || (somaPrioridades == melhorPrioridade && custo.compareTo(melhorCusto) < 0)) {
                    melhorMascara = mascara;
                    melhorPrioridade = somaPrioridades;
                    melhorCusto = custo;
                    melhoresTotais = totais;
                }
            }
        }

        //Montar o resultado com as despesas mantidas e retiradas
        List<Despesas> mantidas = new ArrayList<>(essenciais);
        List<Despesas> retiradas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            if ((melhorMascara & (1 << i)) != 0) {
                mantidas.add(opcionais.get(i));
            } else {
                retiradas.add(opcionais.get(i));
            }
        }
        mantidas.sort(Comparator.comparingInt(Despesas::getId));
        return new ResultadoSimulacao(true, mantidas, opcionaisMantidas(mantidas), retiradas,
                melhoresTotais, melhorCusto, melhorPrioridade, new TreeMap<>());
    }

    //Metodo interno para listar somente as opcionais mantidas
    private List<Despesas> opcionaisMantidas(List<Despesas> mantidas) {
        List<Despesas> lista = new ArrayList<>();
        for (Despesas despesa : mantidas) {
            if (!despesa.isEssencial()) {
                lista.add(despesa);
            }
        }
        return lista;
    }

    //Metodo interno para somar os valores de um mapa
    private BigDecimal somar(Map<Integer, BigDecimal> valores) {
        BigDecimal total = new BigDecimal("0.00");
        for (BigDecimal valor : valores.values()) {
            total = total.add(valor);
        }
        return total;
    }
}
