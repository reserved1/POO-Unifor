package controller;

import model.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Controle implements Serializable {
    @Serial
    //Implementar a Serializable da classe
    private static final long serialVersionUID = 1L;

    //Cria uma lista de eventos
    private Map<Integer, Evento> listaEventos = new HashMap<>();

    //Contrato de persistência usado para salvar o sistema (transient: não faz parte dos dados salvos)
    private transient Persistencia persistencia;

    //Metodo para criar um novo evento
    public Evento addEvento(String nomeEvento) {
        //Verificar e adicionar um id livre ao novo evento
        int idEvento = verificarIdLivre(listaEventos);

        //Criar o objeto evento
        Evento evento = new Evento(idEvento, nomeEvento);

        //Verificar se a lista de eventos não é vazio
        if (listaEventos != null) {
            //Verificar se o evento esta na lista, senão adicionar a lista
            if (!listaEventos.containsKey(evento.getId())) {
                listaEventos.putIfAbsent(evento.getId(), evento);
            }
        }
        //Retorna o evento criado
        return evento;

    }

    //Metodo para cadastrar um novo participante em um evento (recusa código duplicado)
    public void addParticipante(int idEvento, int idParticipante, String nomeParticipante, BigDecimal orcamentoParticipante) {
        salvarParticipante(idEvento, idParticipante, nomeParticipante, orcamentoParticipante, new ArrayList<>(), true);
    }

    //Metodo para cadastrar ou editar um participante e definir as despesas em que ele participa.
    //Todas as regras são validadas antes de qualquer alteração, assim uma entrada inválida não muda o estado anterior.
    public void salvarParticipante(int idEvento, int idParticipante, String nome, BigDecimal orcamento, List<Integer> despesasSelecionadas, boolean novoParticipante) {
        Evento evento = obterEvento(idEvento);

        //Validar os dados do participante
        Participante.validarNome(nome);
        Participante.validarOrcamento(orcamento);
        Participante existente = evento.getListaParticipantes().get(idParticipante);
        if (novoParticipante && existente != null) {
            throw new RegraNegocioException("Já existe um participante com o código " + idParticipante + ".");
        }
        if (!novoParticipante && existente == null) {
            throw new RegraNegocioException("Participante " + idParticipante + " não encontrado.");
        }

        //Montar o conjunto final de despesas do participante (as essenciais incluem todos os participantes)
        Set<Integer> despesasFinais = new HashSet<>();
        if (despesasSelecionadas != null) {
            despesasFinais.addAll(despesasSelecionadas);
        }
        for (Despesas despesa : evento.getListaDespesas().values()) {
            if (despesa.isEssencial()) {
                despesasFinais.add(despesa.getId());
            }
        }
        for (int idDespesa : despesasFinais) {
            if (!evento.getListaDespesas().containsKey(idDespesa)) {
                throw new RegraNegocioException("Despesa " + idDespesa + " não encontrada.");
            }
        }

        //Verificar se alguma despesa ficaria sem participantes
        for (Despesas despesa : evento.getListaDespesas().values()) {
            boolean participa = despesa.getListaParticipantes().containsKey(idParticipante);
            if (participa && !despesasFinais.contains(despesa.getId()) && despesa.getListaParticipantes().size() == 1) {
                throw new RegraNegocioException("A despesa \"" + despesa.getNome() + "\" ficaria sem participantes. "
                        + "Cada despesa precisa de pelo menos um participante.");
            }
        }

        //Aplicar as alterações (dados já validados)
        Participante participante;
        if (novoParticipante) {
            participante = new Participante(idParticipante, nome, orcamento);
            evento.adicionarParticipante(participante);
        } else {
            participante = existente;
            participante.setNome(nome);
            participante.setOrcamento(orcamento);
        }

        for (Despesas despesa : evento.getListaDespesas().values()) {
            if (despesasFinais.contains(despesa.getId())) {
                addParticipanteDespesas(idEvento, despesa.getId(), idParticipante);
            } else {
                desvincular(despesa, participante);
            }
        }
    }

    //Metodo para cadastrar ou editar uma despesa com o tipo de despesa (0 despesas individual e 1 despesas em grupo)
    //e os participantes envolvidos. Tudo é validado antes de alterar o evento.
    public void salvarDespesa(int idEvento, int idDespesas, String nomeDespesas, BigDecimal valorDespesas, boolean essencial, int prioridadeDespesas, int tipoDespesas, List<Integer> participantesSelecionados) {
        Evento evento = obterEvento(idEvento);

        //Montar o conjunto de participantes sem repetições (essencial inclui todos os participantes)
        Set<Integer> participantesFinais = new TreeSet<>();
        if (essencial) {
            participantesFinais.addAll(evento.getListaParticipantes().keySet());
        } else if (participantesSelecionados != null) {
            participantesFinais.addAll(participantesSelecionados);
        }

        //Verificar se a despesa tem pelo menos um participante cadastrado
        if (participantesFinais.isEmpty()) {
            if (essencial) {
                throw new RegraNegocioException("Cadastre pelo menos um participante antes de incluir uma despesa essencial.");
            }
            throw new RegraNegocioException("Selecione pelo menos um participante para a despesa.");
        }
        for (int idParticipante : participantesFinais) {
            if (!evento.getListaParticipantes().containsKey(idParticipante)) {
                throw new RegraNegocioException("Participante " + idParticipante + " não está cadastrado.");
            }
        }

        //Criar a nova despesa (o construtor valida descrição, valor e prioridade) e calcular as parcelas
        Despesas despesaNova = criarDespesa(idDespesas, nomeDespesas, valorDespesas, essencial, prioridadeDespesas, tipoDespesas);
        for (int idParticipante : participantesFinais) {
            despesaNova.adicionarParticipante(idParticipante);
        }

        //Aplicar: retirar a versão anterior da despesa dos participantes e do evento e adicionar a nova
        for (Participante participante : evento.getListaParticipantes().values()) {
            participante.removerDespesas(idDespesas);
        }
        evento.removerDespesa(idDespesas);
        evento.adicionarDespesa(despesaNova);
        for (int idParticipante : participantesFinais) {
            evento.getListaParticipantes().get(idParticipante).adicionarDespesas(despesaNova);
        }
    }

    //Metodo para criar o objeto despesa a partir do tipo (retorna a referência do tipo abstrato Despesas)
    public Despesas criarDespesa(int idDespesas, String nomeDespesas, BigDecimal valorDespesas, boolean essencial, int prioridadeDespesas, int tipoDespesas) {
        if (tipoDespesas == 0) {
            return new DespesasIndividual(idDespesas, nomeDespesas, valorDespesas, essencial, prioridadeDespesas, tipoDespesas);
        } else if (tipoDespesas == 1) {
            return new DespesasGrupo(idDespesas, nomeDespesas, valorDespesas, essencial, prioridadeDespesas, tipoDespesas);
        }
        throw new RegraNegocioException("Tipo de despesa inválido.");
    }

    //Metodo para adicionar um participante a uma despesa
    public void addParticipanteDespesas(int idEvento, int idDespesa, int idParticipanteDespesa) {
        //Criar a variável para o participante e a despesa
        Evento evento = obterEvento(idEvento);
        Despesas despesas = evento.getListaDespesas().get(idDespesa);
        Participante participanteDespesa = evento.getListaParticipantes().get(idParticipanteDespesa);
        if (despesas == null || participanteDespesa == null) {
            throw new RegraNegocioException("Despesa ou participante não encontrado.");
        }

        //Verificar se o participante esta na lista da despesa, senão adicionar a lista
        if (!despesas.getListaParticipantes().containsKey(participanteDespesa.getId())) {
            despesas.adicionarParticipante(idParticipanteDespesa);
        }

        //Verificar se a despesa esta na lista do participante, senão adicionar a lista
        if (!participanteDespesa.getListaDespesas().containsKey(despesas.getId())) {
            participanteDespesa.adicionarDespesas(despesas);
        }

    }

    //Metodo para excluir um evento da lista de evento
    public boolean excluirEvento(int idEvento) {
        return listaEventos.remove(idEvento) != null;

    }

    //Metodo para excluir uma despesa da lista de um evento
    public boolean excluirDespesa(int idEvento, int idDespesa) {
        Evento evento = getListaEventos().get(idEvento);

        //Verificar se o evento não e nulo
        if (evento != null) {
            //Excluir a despesa e salva um retorno na variável
            boolean confirmacao = evento.removerDespesa(idDespesa);

            //Se a variável é verdadeira excluir a despesa da lista de despesas dos participantes
            if (confirmacao) {
                var listaParticipantes = evento.getListaParticipantes();
                for (Participante participante : listaParticipantes.values()) {
                    participante.removerDespesas(idDespesa);
                }
            }
            //Retorna a confirmação
            return confirmacao;
        } else return false;
    }

    //Metodo para excluir um participante da lista de um evento.
    //A exclusão é impedida enquanto houver despesas vinculadas ao participante.
    public boolean excluirParticipante(int idEvento, int idParticipante) {
        Evento evento = getListaEventos().get(idEvento);

        //Verificar se o evento e o participante existem
        if (evento == null || !evento.getListaParticipantes().containsKey(idParticipante)) {
            return false;
        }

        //Verificar se existem despesas vinculadas ao participante
        List<String> despesasVinculadas = new ArrayList<>();
        for (Despesas despesas : evento.getListaDespesas().values()) {
            if (despesas.getListaParticipantes().containsKey(idParticipante)) {
                despesasVinculadas.add(despesas.getNome());
            }
        }
        if (!despesasVinculadas.isEmpty()) {
            String nome = evento.getListaParticipantes().get(idParticipante).getNome();
            throw new RegraNegocioException("Não é possível excluir " + nome + ": há despesas vinculadas ("
                    + String.join(", ", despesasVinculadas) + ").\nRemova o participante dessas despesas antes de excluí-lo.");
        }

        //Excluir o participante e retornar a confirmação
        return evento.removerParticipante(idParticipante);

    }

    //Metodo para excluir um participante de uma despesas (recusa se a despesa ficar sem participantes)
    public void excluirParticipanteDespesa(int idEvento, int idDespesas, int excluirParticipante) {
        //Criar a variável para o evento, participante e a despesa
        Evento evento = obterEvento(idEvento);
        Participante participante = evento.getListaParticipantes().get(excluirParticipante);
        Despesas despesa = evento.getListaDespesas().get(idDespesas);
        if (participante == null || despesa == null) {
            throw new RegraNegocioException("Despesa ou participante não encontrado.");
        }

        //Verificar se a despesa ficaria sem participantes
        if (despesa.getListaParticipantes().containsKey(excluirParticipante) && despesa.getListaParticipantes().size() == 1) {
            throw new RegraNegocioException("A despesa \"" + despesa.getNome() + "\" ficaria sem participantes.");
        }

        desvincular(despesa, participante);
    }

    //Metodo interno para remover o vínculo entre despesa e participante nos dois lados da associação
    private void desvincular(Despesas despesa, Participante participante) {
        //Verificar se o participante esta na lista de participantes da despesa para excluir
        if (despesa.getListaParticipantes().containsKey(participante.getId())) {
            despesa.removerParticipante(participante.getId());
        }
        //Verificar se a despesa esta na lista de despesas do participante para excluir
        if (participante.getListaDespesas().containsKey(despesa.getId())) {
            participante.removerDespesas(despesa.getId());
        }
    }

    //Metodo para adicionar a todos os participantes uma despesa essencial
    public void despesaEssencialTodos(int idEvento, int idDespesasAdicionar) {
        var listaParticipantes = obterEvento(idEvento).getListaParticipantes();
        Despesas despesasAdicionar = obterEvento(idEvento).getListaDespesas().get(idDespesasAdicionar);

        //Verificar se a despesa é essencial para adicionar a todos os participantes
        if (despesasAdicionar != null && despesasAdicionar.isEssencial()) {
            //Percorre a lista de participantes e adiciona a despesa a cada um
            for (Participante participante : listaParticipantes.values()) {
                addParticipanteDespesas(idEvento, despesasAdicionar.getId(), participante.getId());
            }
        }

    }

    //Metodo para calcular o total do evento (o calculo é feito pelo model)
    public BigDecimal calcularTotalEvento(int idEvento) {
        return obterEvento(idEvento).calcularCustoTotal();
    }

    //Metodo para calcula a soma das despesa de um participante (o calculo é feito pelo model)
    public BigDecimal calcularOrcamento(int idEvento, int idParticipante) {
        return obterEvento(idEvento).calcularTotalParticipante(idParticipante);
    }

    //Metodo para gerar uma string com o status do orcamento recebendo o orçamento final e o orçamento do participante
    public String statusOrcamento(BigDecimal orcamentoFinal, BigDecimal orcamentoParticipante) {
        BigDecimal diferencaOrcamento;
        //Comprara o orcamento final com o orcamento do participante
        int comparador = orcamentoFinal.compareTo(orcamentoParticipante);
        //Switch case para retornar o status do orçamento
        return switch (comparador) {
            case 1 -> {
                //Se o orcamento final maior que o orcamento do participante
                diferencaOrcamento = orcamentoFinal.subtract(orcamentoParticipante);
                yield String.format("R$ %.2f", diferencaOrcamento.setScale(2, RoundingMode.HALF_UP)) + " Acima do Orçamento";
            }
            case -1 -> {
                //Se o orcamento final menor que o orcamento do participante (também está dentro do orçamento)
                diferencaOrcamento = orcamentoParticipante.subtract(orcamentoFinal);
                yield "Dentro do Orçamento (sobra " + String.format("R$ %.2f", diferencaOrcamento.setScale(2, RoundingMode.HALF_UP)) + ")";
            }
            //Se o orcamento final igual ao o orcamento do participante
            case 0 -> "Dentro do Orçamento";
            //Caso padrão
            default -> "Erro no calcular o status";
        };

    }

    //Metodo para converter o texto digitado na tela em valor (aceita "70,00", "70.00", "R$ 1.234,56")
    public static BigDecimal converterValor(String texto) {
        //Verificar se o texto não é vazio
        if (texto == null || texto.trim().isEmpty()) {
            throw new RegraNegocioException("O valor não pode ser vazio.");
        }
        String valorTexto = texto.replace("R$", "").replace(" ", "").replace(" ", "").trim();

        //Com vírgula: formato brasileiro (ponto separa milhar e vírgula separa centavos)
        if (valorTexto.contains(",")) {
            valorTexto = valorTexto.replace(".", "").replace(",", ".");
        } else if (valorTexto.matches("\\d{1,3}(\\.\\d{3})+")) {
            //Somente pontos separando grupos de 3 dígitos: separador de milhar (ex.: 1.234)
            valorTexto = valorTexto.replace(".", "");
        }

        try {
            BigDecimal valor = new BigDecimal(valorTexto);
            if (valor.stripTrailingZeros().scale() > 2) {
                throw new RegraNegocioException("Use no máximo duas casas decimais.");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Valor inválido: \"" + texto.trim() + "\". Digite um número como 70,00.");
        }
    }

    //Metodo para definir o contrato de persistência utilizado pelo sistema
    public void setPersistencia(Persistencia persistencia) {
        this.persistencia = persistencia;
    }

    //Metodo para salvar o sistema usando o contrato de persistência
    public void salvar() throws PersistenciaException {
        if (persistencia == null) {
            persistencia = new GerenciadorArquivo();
        }
        persistencia.salvar(this);
    }

    //Metodo para verificar id livre do map informado
    public <T> int verificarIdLivre(Map<Integer,T> listaVerificar){
        //Menor do id do sistema
        int idLivre=1;

        //Verificar a lista para localizar o primeiro livre
        if (listaVerificar != null){
            while (listaVerificar.containsKey(idLivre)) {
                idLivre++;
            }
        }
        //Retorna o primeiro livre
        return idLivre;
    }

    //Metodo interno para obter um evento ou informar que ele não existe
    private Evento obterEvento(int idEvento) {
        Evento evento = listaEventos.get(idEvento);
        if (evento == null) {
            throw new RegraNegocioException("Evento " + idEvento + " não encontrado.");
        }
        return evento;
    }

    //Get lista de Eventos (somente leitura)
    public Map<Integer,Evento> getListaEventos() {
        return Collections.unmodifiableMap(listaEventos);
    }

}
