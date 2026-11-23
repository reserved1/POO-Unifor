import controller.*;
import model.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

//Testes automatizados dos cenários obrigatórios da atividade (seção 4) e do objetivo extra (seção 7).
//Não usa bibliotecas externas: cada verificação imprime OK/FALHOU com o valor esperado e o obtido.
public class TestesPlanejamento {

    private static int aprovados = 0;
    private static int reprovados = 0;

    // Ids do caso de referência
    private static final int ANA = 1, BRUNO = 2, CARLA = 3;
    private static final int TRANSPORTE = 1, ALMOCO = 2, ATIVIDADE = 3, SOBREMESA = 4;
    private static final int POR_PESSOA = 0, FIXA = 1;

    public static void main(String[] args) throws Exception {
        testeCasoReferencia();
        testeCentavosNoRateio();
        testeEntradaOuVinculoInvalido();
        testeAlteracaoDeOrcamento();
        testeExclusaoDeDespesa();
        testeSalvarEReiniciar();
        testeProblemaNoArquivo();
        testeExclusaoDeParticipante();
        testeEdicaoDeDespesa();
        testeConversaoDeValores();
        testeSimuladorCasoReferencia();
        testeSimuladorSemSolucao();
        testeSimuladorLimites();

        System.out.println();
        System.out.println("Resultado: " + aprovados + " verificações aprovadas, " + reprovados + " reprovadas.");
        if (reprovados > 0) {
            System.exit(1);
        }
    }

    // ----------------------------------------------------------------- cenários

    static void testeCasoReferencia() {
        titulo("1. Caso de referência do passeio da turma");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        Evento evento = sistema.getListaEventos().get(ev);

        String[][] esperado = {
                // pessoa, transporte, almoço, atividade, sobremesa, total
                {"1", "30.00", "20.00", "15.00", "10.00", "75.00"},
                {"2", "30.00", "20.00", "0.00", "10.00", "60.00"},
                {"3", "30.00", "20.00", "15.00", "10.00", "75.00"}};
        for (String[] linha : esperado) {
            int id = Integer.parseInt(linha[0]);
            String nome = evento.getListaParticipantes().get(id).getNome();
            for (int d = 1; d <= 4; d++) {
                Despesas despesa = evento.getListaDespesas().get(d);
                igual(nome + " - " + despesa.getNome(), linha[d], despesa.getValorParticipante(id));
            }
            igual(nome + " - total", linha[5], sistema.calcularOrcamento(ev, id));
        }
        igual("Custo total", "210.00", sistema.calcularTotalEvento(ev));
        igual("Ana excede o orçamento em", "5.00", evento.calcularExcedente(ANA));
        igual("Carla excede o orçamento em", "5.00", evento.calcularExcedente(CARLA));
        igual("Bruno excede o orçamento em", "0.00", evento.calcularExcedente(BRUNO));
        verdadeiro("Status de Bruno é 'Dentro do Orçamento' (total igual ao limite)",
                sistema.statusOrcamento(sistema.calcularOrcamento(ev, BRUNO), evento.getListaParticipantes().get(BRUNO).getOrcamento()).equals("Dentro do Orçamento"));
    }

    static void testeCentavosNoRateio() {
        titulo("2. Centavos no rateio");
        Controle sistema = new Controle();
        int ev = sistema.addEvento("Centavos").getId();
        for (int i = 1; i <= 3; i++) {
            sistema.addParticipante(ev, i, "P" + i, new BigDecimal("500"));
        }
        //Participantes informados fora de ordem para conferir que a ordem usada é a dos códigos
        sistema.salvarDespesa(ev, 1, "Fixa de 100", new BigDecimal("100.00"), false, 0, FIXA, Arrays.asList(3, 1, 2));
        Despesas despesa = sistema.getListaEventos().get(ev).getListaDespesas().get(1);
        igual("Código 1", "33.34", despesa.getValorParticipante(1));
        igual("Código 2", "33.33", despesa.getValorParticipante(2));
        igual("Código 3", "33.33", despesa.getValorParticipante(3));
        igual("Soma das parcelas", "100.00", despesa.getTotalParcelas());

        //Dois centavos restantes: um para cada um dos dois menores códigos
        sistema.salvarDespesa(ev, 2, "Fixa de 100,01", new BigDecimal("100.01"), false, 0, FIXA, Arrays.asList(1, 2, 3));
        Despesas despesa2 = sistema.getListaEventos().get(ev).getListaDespesas().get(2);
        igual("100,01 - código 1", "33.34", despesa2.getValorParticipante(1));
        igual("100,01 - código 2", "33.34", despesa2.getValorParticipante(2));
        igual("100,01 - código 3", "33.33", despesa2.getValorParticipante(3));
        igual("100,01 - soma", "100.01", despesa2.getTotalParcelas());
    }

    static void testeEntradaOuVinculoInvalido() {
        titulo("3. Entrada ou vínculo inválido (recusar sem alterar os dados)");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        String antes = fotografia(sistema, ev);

        recusa("Despesa sem participantes", () ->
                sistema.salvarDespesa(ev, 5, "Lanche", new BigDecimal("10"), false, 0, FIXA, List.of()));
        recusa("Despesa com valor negativo", () ->
                sistema.salvarDespesa(ev, 5, "Lanche", new BigDecimal("-10"), false, 0, FIXA, List.of(ANA)));
        recusa("Despesa com valor zero", () ->
                sistema.salvarDespesa(ev, 5, "Lanche", BigDecimal.ZERO, false, 0, FIXA, List.of(ANA)));
        recusa("Despesa com descrição vazia", () ->
                sistema.salvarDespesa(ev, 5, "   ", new BigDecimal("10"), false, 0, FIXA, List.of(ANA)));
        recusa("Despesa com participante não cadastrado", () ->
                sistema.salvarDespesa(ev, 5, "Lanche", new BigDecimal("10"), false, 0, FIXA, List.of(99)));
        recusa("Editar despesa retirando todos os participantes", () ->
                sistema.salvarDespesa(ev, TRANSPORTE, "Transporte", new BigDecimal("90"), false, 0, FIXA, List.of()));
        recusa("Participante com nome vazio", () ->
                sistema.addParticipante(ev, 4, "", new BigDecimal("10")));
        recusa("Participante com orçamento negativo", () ->
                sistema.addParticipante(ev, 4, "Davi", new BigDecimal("-1")));
        recusa("Participante com código duplicado", () ->
                sistema.addParticipante(ev, ANA, "Outra Ana", new BigDecimal("10")));
        recusa("Retirar o último participante de uma despesa", () -> {
            sistema.excluirParticipanteDespesa(ev, ATIVIDADE, ANA);
            sistema.excluirParticipanteDespesa(ev, ATIVIDADE, CARLA);
        });
        //A primeira remoção acima é válida (Ana sai da atividade); desfaz para comparar com o estado inicial
        sistema.addParticipanteDespesas(ev, ATIVIDADE, ANA);
        recusa("Valor digitado inválido (\"abc\")", () -> Controle.converterValor("abc"));
        recusa("Valor digitado vazio", () -> Controle.converterValor("  "));

        igual("Estado do planejamento depois das recusas", antes, fotografia(sistema, ev));

        //Orçamento zero é aceito (maior ou igual a zero)
        sistema.addParticipante(ev, 4, "Davi", new BigDecimal("0"));
        verdadeiro("Orçamento igual a zero é aceito", sistema.getListaEventos().get(ev).getListaParticipantes().containsKey(4));
        //Quem não participa de nenhuma despesa tem contribuição zero
        igual("Contribuição de quem não participa", "0.00", sistema.calcularOrcamento(ev, 4));
    }

    static void testeAlteracaoDeOrcamento() {
        titulo("4. Alteração de orçamento");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        //Mesma operação feita pela tela: edita o participante mantendo as despesas marcadas
        sistema.salvarParticipante(ev, ANA, "Ana", new BigDecimal("80.00"), List.of(TRANSPORTE, ALMOCO, ATIVIDADE, SOBREMESA), false);
        Evento evento = sistema.getListaEventos().get(ev);
        igual("Orçamento de Ana", "80.00", evento.getListaParticipantes().get(ANA).getOrcamento());
        igual("Custo de Ana", "75.00", sistema.calcularOrcamento(ev, ANA));
        igual("Excedente de Ana", "0.00", evento.calcularExcedente(ANA));
        verdadeiro("Status de Ana indica dentro do orçamento",
                sistema.statusOrcamento(sistema.calcularOrcamento(ev, ANA), new BigDecimal("80.00")).startsWith("Dentro do Orçamento"));
        igual("Custo total sem mudança", "210.00", sistema.calcularTotalEvento(ev));
    }

    static void testeExclusaoDeDespesa() {
        titulo("5. Exclusão de despesa");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        verdadeiro("Excluir 'Atividade recreativa'", sistema.excluirDespesa(ev, ATIVIDADE));
        igual("Ana", "60.00", sistema.calcularOrcamento(ev, ANA));
        igual("Bruno", "60.00", sistema.calcularOrcamento(ev, BRUNO));
        igual("Carla", "60.00", sistema.calcularOrcamento(ev, CARLA));
        igual("Custo total", "180.00", sistema.calcularTotalEvento(ev));
        verdadeiro("Despesa retirada também da lista de Ana",
                !sistema.getListaEventos().get(ev).getListaParticipantes().get(ANA).getListaDespesas().containsKey(ATIVIDADE));
    }

    static void testeSalvarEReiniciar() throws Exception {
        titulo("6. Salvar e reiniciar");
        File arquivo = File.createTempFile("planejamento", ".dat");
        Persistencia persistencia = new GerenciadorArquivo(arquivo.getPath());

        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        String antes = fotografia(sistema, ev);
        persistencia.salvar(sistema);
        //Salvar duas vezes não pode duplicar registros
        persistencia.salvar(sistema);

        Controle recuperado = persistencia.carregar();
        Evento evento = recuperado.getListaEventos().get(ev);
        verdadeiro("Objeto recuperado é uma nova instância", recuperado != sistema);
        igual("Quantidade de eventos", "1", recuperado.getListaEventos().size());
        igual("Quantidade de participantes", "3", evento.getListaParticipantes().size());
        igual("Quantidade de despesas", "4", evento.getListaDespesas().size());
        igual("Dados, vínculos e resultados", antes, fotografia(recuperado, ev));
        verdadeiro("Vínculo despesa-participante preservado (mesmo objeto nas duas listas)",
                evento.getListaDespesas().get(TRANSPORTE) == evento.getListaParticipantes().get(ANA).getListaDespesas().get(TRANSPORTE));
        arquivo.delete();
    }

    static void testeProblemaNoArquivo() throws Exception {
        titulo("7. Problema no arquivo");
        Controle emUso = new Controle();
        int ev = montarCasoReferencia(emUso);
        String antes = fotografia(emUso, ev);

        //Arquivo ausente
        File ausente = new File(System.getProperty("java.io.tmpdir"), "nao-existe-" + System.nanoTime() + ".dat");
        try {
            new GerenciadorArquivo(ausente.getPath()).carregar();
            falhou("Arquivo ausente deveria lançar PersistenciaException");
        } catch (PersistenciaException e) {
            verdadeiro("Arquivo ausente gera mensagem: \"" + e.getMessage() + "\"", e.isArquivoAusente());
        }

        //Arquivo inválido
        File invalido = File.createTempFile("invalido", ".dat");
        Files.writeString(invalido.toPath(), "isto não é um planejamento");
        Persistencia persistencia = new GerenciadorArquivo(invalido.getPath());
        Controle carregado = emUso;
        try {
            carregado = persistencia.carregar();
            falhou("Arquivo inválido deveria lançar PersistenciaException");
        } catch (PersistenciaException e) {
            verdadeiro("Arquivo inválido gera mensagem: \"" + e.getMessage() + "\"", !e.isArquivoAusente());
        }
        verdadeiro("Falha ao carregar preserva o planejamento em uso (mesma referência)", carregado == emUso);
        igual("Dados do planejamento em uso após a falha", antes, fotografia(emUso, ev));

        //Iniciar planejamento vazio
        Controle vazio = new Controle();
        igual("Planejamento vazio sem eventos", "0", vazio.getListaEventos().size());
        invalido.delete();
    }

    static void testeExclusaoDeParticipante() {
        titulo("8. Exclusão de participante com despesas vinculadas");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        String antes = fotografia(sistema, ev);
        recusa("Excluir Bruno (participa de 3 despesas)", () -> sistema.excluirParticipante(ev, BRUNO));
        igual("Dados após a recusa", antes, fotografia(sistema, ev));

        sistema.addParticipante(ev, 4, "Davi", new BigDecimal("50"));
        verdadeiro("Excluir participante sem despesas é permitido", sistema.excluirParticipante(ev, 4));
    }

    static void testeEdicaoDeDespesa() {
        titulo("9. Edição de despesa recalcula os totais");
        Controle sistema = new Controle();
        int ev = montarCasoReferencia(sistema);
        //Atividade recreativa passa a incluir Bruno: 30 / 3 = 10 para cada
        sistema.salvarDespesa(ev, ATIVIDADE, "Atividade recreativa", new BigDecimal("30.00"), false, 0, FIXA, List.of(ANA, BRUNO, CARLA));
        igual("Ana", "70.00", sistema.calcularOrcamento(ev, ANA));
        igual("Bruno", "70.00", sistema.calcularOrcamento(ev, BRUNO));
        igual("Custo total", "210.00", sistema.calcularTotalEvento(ev));
        //Mudar o tipo de fixa para por pessoa: cada um paga 30
        sistema.salvarDespesa(ev, ATIVIDADE, "Atividade recreativa", new BigDecimal("30.00"), false, 0, POR_PESSOA, List.of(ANA, BRUNO, CARLA));
        igual("Ana após mudar o tipo", "90.00", sistema.calcularOrcamento(ev, ANA));
        igual("Custo total após mudar o tipo", "270.00", sistema.calcularTotalEvento(ev));
        verdadeiro("Lista de Ana aponta para a despesa atualizada",
                sistema.getListaEventos().get(ev).getListaParticipantes().get(ANA).getListaDespesas().get(ATIVIDADE) instanceof DespesasIndividual);
    }

    static void testeConversaoDeValores() {
        titulo("10. Conversão do valor digitado na tela");
        igual("\"70,00\"", "70.00", Controle.converterValor("70,00"));
        igual("\"70.00\"", "70.00", Controle.converterValor("70.00"));
        igual("\"R$ 1.234,56\"", "1234.56", Controle.converterValor("R$ 1.234,56"));
        igual("\"1.234\"", "1234", Controle.converterValor("1.234"));
        igual("\"70,5\"", "70.5", Controle.converterValor("70,5"));
        recusa("\"10,123\" (três casas decimais)", () -> Controle.converterValor("10,123"));
    }

    static void testeSimuladorCasoReferencia() {
        titulo("11. Simulador de alternativas - caso de referência");
        Controle sistema = new Controle();
        int ev = montarCasoReferenciaSimulador(sistema, new BigDecimal("70.00"));
        String antes = fotografia(sistema, ev);
        ResultadoSimulacao resultado = sistema.simularAlternativas(ev);
        System.out.print(indentar(resultado.descrever(sistema.getListaEventos().get(ev))));
        verdadeiro("Possui solução", resultado.isPossuiSolucao());
        igual("Opcionais mantidas", "[Atividade recreativa]", nomes(resultado.getOpcionaisMantidas()));
        igual("Despesas retiradas", "[Sobremesa]", nomes(resultado.getDespesasRetiradas()));
        igual("Soma das prioridades", "3", resultado.getSomaPrioridades());
        igual("Ana", "65.00", resultado.getTotaisPorParticipante().get(ANA));
        igual("Bruno", "50.00", resultado.getTotaisPorParticipante().get(BRUNO));
        igual("Carla", "65.00", resultado.getTotaisPorParticipante().get(CARLA));
        igual("Novo custo total", "180.00", resultado.getCustoTotal());
        igual("Planejamento original preservado", antes, fotografia(sistema, ev));
    }

    static void testeSimuladorSemSolucao() {
        titulo("12. Simulador de alternativas - sem solução (orçamento de Ana em R$ 40,00)");
        Controle sistema = new Controle();
        int ev = montarCasoReferenciaSimulador(sistema, new BigDecimal("40.00"));
        ResultadoSimulacao resultado = sistema.simularAlternativas(ev);
        System.out.print(indentar(resultado.descrever(sistema.getListaEventos().get(ev))));
        verdadeiro("Não possui solução", !resultado.isPossuiSolucao());
        igual("Essenciais de Ana", "50.00", resultado.getTotaisPorParticipante().get(ANA));
        igual("Excesso de Ana", "10.00", resultado.getExcessoEssenciais().get(ANA));
        igual("Somente Ana excede", "[1]", resultado.getExcessoEssenciais().keySet().toString());
    }

    static void testeSimuladorLimites() {
        titulo("13. Simulador de alternativas - limites");
        Controle sistema = new Controle();
        int ev = sistema.addEvento("Muitas opcionais").getId();
        sistema.addParticipante(ev, 1, "Ana", new BigDecimal("1000"));
        for (int d = 1; d <= 11; d++) {
            sistema.salvarDespesa(ev, d, "Opcional " + d, new BigDecimal("1"), false, 1, FIXA, List.of(1));
        }
        recusa("Mais de 10 despesas opcionais", () -> sistema.simularAlternativas(ev));
        sistema.excluirDespesa(ev, 11);
        verdadeiro("Com 10 opcionais que cabem, mantém todas", sistema.simularAlternativas(ev).getDespesasRetiradas().isEmpty());
        sistema.salvarDespesa(ev, 1, "Opcional 1", new BigDecimal("1"), false, 0, FIXA, List.of(1));
        recusa("Opcional com prioridade 0", () -> sistema.simularAlternativas(ev));
    }

    // ----------------------------------------------------------------- dados

    static int montarCasoReferencia(Controle sistema) {
        int ev = sistema.addEvento("Passeio da turma").getId();
        sistema.addParticipante(ev, ANA, "Ana", new BigDecimal("70.00"));
        sistema.addParticipante(ev, BRUNO, "Bruno", new BigDecimal("60.00"));
        sistema.addParticipante(ev, CARLA, "Carla", new BigDecimal("70.00"));
        sistema.salvarDespesa(ev, TRANSPORTE, "Transporte", new BigDecimal("90.00"), false, 0, FIXA, List.of(ANA, BRUNO, CARLA));
        sistema.salvarDespesa(ev, ALMOCO, "Almoço", new BigDecimal("20.00"), false, 0, POR_PESSOA, List.of(ANA, BRUNO, CARLA));
        sistema.salvarDespesa(ev, ATIVIDADE, "Atividade recreativa", new BigDecimal("30.00"), false, 0, FIXA, List.of(ANA, CARLA));
        sistema.salvarDespesa(ev, SOBREMESA, "Sobremesa", new BigDecimal("10.00"), false, 0, POR_PESSOA, List.of(ANA, BRUNO, CARLA));
        return ev;
    }

    static int montarCasoReferenciaSimulador(Controle sistema, BigDecimal orcamentoAna) {
        int ev = sistema.addEvento("Passeio da turma - simulador").getId();
        sistema.addParticipante(ev, ANA, "Ana", orcamentoAna);
        sistema.addParticipante(ev, BRUNO, "Bruno", new BigDecimal("60.00"));
        sistema.addParticipante(ev, CARLA, "Carla", new BigDecimal("70.00"));
        sistema.salvarDespesa(ev, TRANSPORTE, "Transporte", new BigDecimal("90.00"), true, 0, FIXA, null);
        sistema.salvarDespesa(ev, ALMOCO, "Almoço", new BigDecimal("20.00"), true, 0, POR_PESSOA, null);
        sistema.salvarDespesa(ev, ATIVIDADE, "Atividade recreativa", new BigDecimal("30.00"), false, 3, FIXA, List.of(ANA, CARLA));
        sistema.salvarDespesa(ev, SOBREMESA, "Sobremesa", new BigDecimal("10.00"), false, 1, POR_PESSOA, List.of(ANA, BRUNO, CARLA));
        return ev;
    }

    //Texto que resume todo o estado de um evento (dados, vínculos, parcelas e totais) para comparar antes/depois
    static String fotografia(Controle sistema, int ev) {
        Evento evento = sistema.getListaEventos().get(ev);
        StringBuilder texto = new StringBuilder();
        new java.util.TreeMap<>(evento.getListaParticipantes()).forEach((id, p) -> texto.append("P").append(id).append(':')
                .append(p.getNome()).append(':').append(p.getOrcamento().stripTrailingZeros().toPlainString())
                .append(":despesas=").append(new java.util.TreeSet<>(p.getListaDespesas().keySet()))
                .append(":total=").append(sistema.calcularOrcamento(ev, id)).append(' '));
        new java.util.TreeMap<>(evento.getListaDespesas()).forEach((id, d) -> texto.append("D").append(id).append(':')
                .append(d.getNome()).append(':').append(d.getClass().getSimpleName()).append(':').append(d.getValor())
                .append(":parcelas=").append(new java.util.TreeMap<>(d.getListaParticipantes())).append(' '));
        texto.append("custo=").append(sistema.calcularTotalEvento(ev));
        return texto.toString();
    }

    // ----------------------------------------------------------------- utilitários

    interface Acao {
        void executar() throws Exception;
    }

    static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto);
    }

    static void igual(String descricao, String esperado, Object obtido) {
        String textoObtido = obtido instanceof BigDecimal ? ((BigDecimal) obtido).toPlainString() : String.valueOf(obtido);
        if (esperado.equals(textoObtido)) {
            aprovados++;
            System.out.println("  OK      " + descricao + ": " + resumir(textoObtido));
        } else {
            reprovados++;
            System.out.println("  FALHOU  " + descricao + ": esperado " + esperado + ", obtido " + textoObtido);
        }
    }

    static void verdadeiro(String descricao, boolean condicao) {
        if (condicao) {
            aprovados++;
            System.out.println("  OK      " + descricao);
        } else {
            reprovados++;
            System.out.println("  FALHOU  " + descricao);
        }
    }

    static void recusa(String descricao, Acao acao) {
        try {
            acao.executar();
            reprovados++;
            System.out.println("  FALHOU  " + descricao + ": a operação foi aceita");
        } catch (RegraNegocioException e) {
            aprovados++;
            System.out.println("  OK      " + descricao + " -> recusado: \"" + e.getMessage().replace('\n', ' ') + "\"");
        } catch (Exception e) {
            reprovados++;
            System.out.println("  FALHOU  " + descricao + ": erro inesperado " + e);
        }
    }

    static void falhou(String descricao) {
        reprovados++;
        System.out.println("  FALHOU  " + descricao);
    }

    static String nomes(List<Despesas> despesas) {
        return despesas.stream().map(Despesas::getNome).toList().toString();
    }

    static String resumir(String texto) {
        return texto.length() > 60 ? "(estado igual ao esperado)" : texto;
    }

    static String indentar(String texto) {
        return texto.lines().map(l -> "    | " + l).reduce("", (a, b) -> a + b + System.lineSeparator());
    }
}
