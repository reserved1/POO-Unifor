package view;

import controller.Controle;
import controller.PersistenciaException;
import model.Despesas;
import model.Evento;
import model.Participante;
import model.RegraNegocioException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;


public class MenuDespesa {
    private Controle sistemaOriginal;
    private Evento eventoOriginal;
    private Despesas despesaTela;
    private MenuEvento telaOrigem;
    private JFrame painel;
    private JPanel painelConteudo;
    private JLabel labelDespesa;
    private JLabel idDespesa;
    private JLabel nomeDespesa;
    private JLabel quantidadeParticipantes;
    private JLabel totalDespesa;
    private JLabel tipoDespesa;
    private JLabel essencialDespesa;
    private JLabel prioridadeDespesa;
    private JTextField idTexto;
    private JTextField nomeTexto;
    private JTextField quantParticipanteTexto;
    private JTextField totalTexto;
    private JComboBox<String> tipoComboBox;
    private JComboBox<String> essencialComboBox;
    private JComboBox<String> prioridadeComboBox;
    private JButton editarDespesaButton;
    private JButton salvarDespesaButton;
    private JButton excluirDespesaButton;
    private JButton voltarEventoButton;
    private JTabbedPane abasParticipante;
    private JScrollPane abaParticipante;
    private JTable listaParticipantes;
    private JLabel dicaEditar;
    private JButton descartarAlteracoesButton;
    private JPanel menuVertical;
    private JLabel tituloLabel;


    public MenuDespesa() {
        //Criar a janela
        painel = new JFrame("Relatório da Despesa");
        painel.setContentPane(this.painelConteudo);
        painel.pack();
        painel.setLocationRelativeTo(null);
        painel.setVisible(true);

        // Bloqueia o redimensionamento da janela
        painel.setResizable(false);

        //Adicionar o metodo para ao fechar a janela gerar um janela para confirmar o salvamento do sistema
        painel.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        sairSalvar();

        //Bloqueia os campos para edição
        idTexto.setEditable(false);
        nomeTexto.setEditable(false);
        quantParticipanteTexto.setEditable(false);
        totalTexto.setEditable(false);

        //Criar o modelo e adicionar a comboBox de tipo, essencial e prioridade da janela e bloqueia edição
        DefaultComboBoxModel<String> modeloTipo = new DefaultComboBoxModel<>(new String[]{"Individual", "Grupo"});
        tipoComboBox.setModel(modeloTipo);
        tipoComboBox.setEnabled(false);
        DefaultComboBoxModel<String> modeloEssencial = new DefaultComboBoxModel<>(new String[]{"Não", "Sim"});
        essencialComboBox.setModel(modeloEssencial);
        essencialComboBox.setEnabled(false);
        DefaultComboBoxModel<String> modeloPrioridade = new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
        prioridadeComboBox.setModel(modeloPrioridade);
        prioridadeComboBox.setEnabled(false);

        //Ocultar os botões salvar, excluir e descartar alteração participante
        salvarDespesaButton.setVisible(false);
        excluirDespesaButton.setVisible(false);
        descartarAlteracoesButton.setVisible(false);

        //Ocultar a dica de marcar a checkbox para adicionar
        dicaEditar.setVisible(false);

        //Botão para editar a despesa na tela
        editarDespesaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarDespesa();
            }
        });

        //Botão para salvar a despesa
        salvarDespesaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarDespesa();
            }
        });

        //Botão para excluir a despesa
        excluirDespesaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirDespesa();
            }
        });

        //Botão para descartar alteração da despesa
        descartarAlteracoesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                descartarAlteracoes();
            }
        });

        //Fecha a tela atual e volta para o menu de evento
        voltarEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                voltarEvento();
            }
        });

    }

    //Metodo para exibir a janela
    public void exibir(Controle sistemaTela, MenuEvento origem, Evento eventoMenu, int idDespesasTela) {
        //Atribuir os dados do sistema, evento, tela origem e despesa
        sistemaOriginal = sistemaTela;
        telaOrigem = origem;
        eventoOriginal = eventoMenu;
        despesaTela = eventoOriginal.getListaDespesas().get(idDespesasTela);

        //Atualizar a tela e a lista de participantes da despesa
        atualizarTela();
        atualizarListaParticipante();

    }

    //Metodo para exibir uma nova janela de despesas
    public void exibirNovo(Controle sistemaTela, MenuEvento origem, Evento eventoMenu) {
        //Atribuir os dados do sistema, evento, tela origem
        sistemaOriginal = sistemaTela;
        telaOrigem = origem;
        eventoOriginal = eventoMenu;

        //Gera um id com o primeiro valor livre da lista de despesas do evento e adicionar ao texto do id
        int novoIdDespesa = sistemaOriginal.verificarIdLivre(eventoOriginal.getListaDespesas());
        idTexto.setText(String.valueOf(novoIdDespesa));

        //Chama o metodo para editar o view despesa
        editarDespesa();

        //Oculta o botão excluir despesa
        excluirDespesaButton.setVisible(false);

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelDespesa.setText("Cadastro de Nova Despesa");
        painel.setTitle("Cadastro de Nova Despesa");

    }

    //Atualiza os dados da despesa da janela
    public void atualizarTela() {

        //Defini os valores dos campos de texto da janela
        idTexto.setText(String.valueOf(despesaTela.getId()));
        nomeTexto.setText(despesaTela.getNome());
        quantParticipanteTexto.setText(String.valueOf(despesaTela.getListaParticipantes().size()));
        totalTexto.setText(String.format("R$ %.2f", despesaTela.getValor()));
        prioridadeComboBox.setSelectedIndex(despesaTela.getPrioridade());
        tipoComboBox.setSelectedIndex(despesaTela.getTipo());

        //Definir o index do combobox essencial
        if (despesaTela.isEssencial()) {
            essencialComboBox.setSelectedIndex(1);
        } else {
            essencialComboBox.setSelectedIndex(0);
        }
    }

    //Atualiza a lista de participantes da janela
    public void atualizarListaParticipante() {

        //Criar a lista de participante da despesa
        var dadosParticipantes = despesaTela.getListaParticipantes();

        //Verificar se os dados dos participantes não estão nulo
        if (dadosParticipantes != null) {
            //Criar os títulos das colunas da tabela de acordo com o tipo
            String[] colunas;
            if (despesaTela.getTipo() == 0) {
                colunas = new String[]{"ID", "Nome", "Orçamento", "Orcamento Final", "Status Orçamento", "Valor Individual"};
            } else {
                colunas = new String[]{"ID", "Nome", "Orçamento", "Orcamento Final", "Status Orçamento", "Valor Parcela"};
            }
            //Criar o modelo da tabelar
            DefaultTableModel modeloDadosParticipante = new DefaultTableModel(colunas, 0) {
                @Override
                //Bloqueia a linha para edição
                public boolean isCellEditable(int row, int column) {
                    // Retorna false para todas as células da tabela
                    return false;
                }
            };

            //Percorre a lista de id participante e adicionar a linha para desenha a tabela
            for (Map.Entry<Integer, BigDecimal> entry : dadosParticipantes.entrySet()) {
                //Definir variáveis para o valor do orcamento e orcamento final
                int idParticipante = entry.getKey();
                Participante participanteLinha = eventoOriginal.getListaParticipantes().get(idParticipante);
                BigDecimal orcamentoValor = participanteLinha.getOrcamento();
                BigDecimal orcamentoFinal = sistemaOriginal.calcularOrcamento(eventoOriginal.getId(), participanteLinha.getId());

                //Definir os valores da linha
                String id = String.valueOf(participanteLinha.getId());
                String nome = participanteLinha.getNome();
                String orcamento = String.format("R$ %.2f", orcamentoValor);
                String orcamentoFinalTexto = String.format("R$ %.2f", orcamentoFinal);
                String statusOrcamento = sistemaOriginal.statusOrcamento(orcamentoFinal, orcamentoValor);
                String valorParcela = String.format("R$ %.2f", despesaTela.getListaParticipantes().get(idParticipante));

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{id, nome, orcamento, orcamentoFinalTexto, statusOrcamento, valorParcela};
                modeloDadosParticipante.addRow(linha);
            }
            //Adicionar o modelo a tabela da lista de despesas
            listaParticipantes.setModel(modeloDadosParticipante);

            //Criar o modelo de alinhamento ao centro
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);

            //Setar o tamanho máximo das colunas
            listaParticipantes.getColumnModel().getColumn(0).setMaxWidth(50);
            listaParticipantes.getColumnModel().getColumn(1).setMaxWidth(300);
            listaParticipantes.getColumnModel().getColumn(2).setMaxWidth(150);
            listaParticipantes.getColumnModel().getColumn(3).setMaxWidth(150);
            listaParticipantes.getColumnModel().getColumn(4).setMaxWidth(250);
            listaParticipantes.getColumnModel().getColumn(5).setMaxWidth(150);

            //Aplicar o alinhamento ao centro das colunas
            listaParticipantes.getColumnModel().getColumn(0).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(2).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(3).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(4).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(5).setCellRenderer(centro);

        }
    }

    //Atualiza lista de participantes da janela com as dados dos participantes com modo de seleção
    public void editarListaParticipante() {

        //Criar a lista de participante
        var dadosParticipantes = eventoOriginal.getListaParticipantes();

        //Verificar se os dados dos participante não é nulo
        if (dadosParticipantes != null) {
            //Criar os títulos das colunas da tabela
            String[] colunas = {"Adicionar", "ID", "Nome", "Orçamento", "Orcamento Final", "Status Orçamento"};

            //Criar o modelo da tabelar
            DefaultTableModel modeloDadosParticipante = new DefaultTableModel(colunas, 0) {
                @Override
                // Adicionar uma checkBox na primeira coluna
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    }
                    return super.getColumnClass(columnIndex);
                }

                @Override
                //Desbloqueia apenas a primeira coluna para edição se o campo essencial da despesa não é falso
                public boolean isCellEditable(int row, int column) {
                    //Se a despesa na tela não for nula e seu campo essencial for verdadeiro bloqueia toda a checkbox(0) para edição
                    if (despesaTela != null && despesaTela.isEssencial()) {
                        return false;
                    } else {
                        //Se não libera a coluna do checkbox(0) para edição
                        return column == 0;
                    }
                }
            };

            //Percorre a lista de participante da despesa e adicionar a linha para desenha a tabela
            for (Participante participante : dadosParticipantes.values()) {
                //Variáveis para definir o status do orçamento
                int idParticipante = participante.getId();
                Participante participanteLinha = eventoOriginal.getListaParticipantes().get(idParticipante);
                BigDecimal orcamentoValor = participanteLinha.getOrcamento();
                BigDecimal orcamentoFinal = sistemaOriginal.calcularOrcamento(eventoOriginal.getId(), participanteLinha.getId());

                //Definir os valores da linha
                boolean adicionar = false;
                String id = String.valueOf(participante.getId());
                String nome = participanteLinha.getNome();
                String orcamento = String.format("R$ %.2f", orcamentoValor);
                String orcamentoFinalTexto = String.format("R$ %.2f", orcamentoFinal);
                String statusOrcamento = sistemaOriginal.statusOrcamento(orcamentoFinal, orcamentoValor);

                //Verificar se a despesa tela e nulo(nova despesa) ou se e valido(editando uma despesa ja cadastrado)
                if (despesaTela != null) {
                    //Verificar se o participante esta na lista de participante da despesa para marcar a checkbox adicionar com true
                    if (despesaTela.getListaParticipantes().containsKey(participante.getId())) {
                        adicionar = true;
                    }
                }

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{adicionar, id, nome, orcamento, orcamentoFinalTexto, statusOrcamento};
                modeloDadosParticipante.addRow(linha);
            }
            //Adicionar o modelo a tabela da lista de despesas
            listaParticipantes.setModel(modeloDadosParticipante);

            //Criar o modelo de alinhamento ao centro
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);

            //Setar o tamanho máximo das colunas
            listaParticipantes.getColumnModel().getColumn(0).setMaxWidth(80);
            listaParticipantes.getColumnModel().getColumn(1).setMaxWidth(50);
            listaParticipantes.getColumnModel().getColumn(2).setMaxWidth(300);
            listaParticipantes.getColumnModel().getColumn(3).setMaxWidth(150);
            listaParticipantes.getColumnModel().getColumn(4).setMaxWidth(150);
            listaParticipantes.getColumnModel().getColumn(5).setMaxWidth(250);

            //Aplicar o alinhamento ao centro das colunas
            listaParticipantes.getColumnModel().getColumn(1).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(3).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(4).setCellRenderer(centro);
            listaParticipantes.getColumnModel().getColumn(5).setCellRenderer(centro);

        }
    }

    //Metodo para salvar os dados da despesa
    public void salvarDespesa() {

        //Criar variáveis para verificação
        String nome = nomeTexto.getText().trim();
        String valorTexto = totalTexto.getText().trim();
        BigDecimal valor;

        //Verificar se o nome da despesa é vazio
        if (nome.isEmpty()) {
            //Verificar se a despesa na tela não e nulo(nova despesa) para carregar o nome original da despesa
            if (despesaTela != null) {
                nomeTexto.setText(despesaTela.getNome());
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, "Nome da despesa não pode ser vazio!!", "Nome vazio ou nulo!", JOptionPane.ERROR_MESSAGE);
            nomeTexto.requestFocus();
            return;
        }

        //Verificar se o total da despesa é vazio
        if (valorTexto.isEmpty()) {
            //Verificar se a despesa na tela não é nulo(nova despesa) para carregar o total original da despesa
            if (despesaTela != null) {
                totalTexto.setText(String.format("R$ %.2f", despesaTela.getValor()));
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, "Total da despesa não pode ser vazio!!", "Total vazio!", JOptionPane.ERROR_MESSAGE);
            totalTexto.requestFocus();
            return;
        }

        try {
            //Converte o texto ("70,00", "70.00" ou "R$ 1.234,56") em bigDecimal pelo controller
            valor = Controle.converterValor(valorTexto);

            //Captura o erro de numero invalido
        } catch (RegraNegocioException e) {
            //Verificar se a despesa na tela não e nulo(nova despesa) para carregar o total original da despesa
            if (despesaTela != null) {
                totalTexto.setText(String.format("R$ %.2f", despesaTela.getValor()));
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, e.getMessage(), "Total invalido!", JOptionPane.ERROR_MESSAGE);
            totalTexto.requestFocus();
            return;
        }

        //Criar os valores dos atributos da despesa
        int idDespesa = Integer.parseInt(idTexto.getText());
        boolean essencial;
        int prioridade = prioridadeComboBox.getSelectedIndex();
        int tipo = tipoComboBox.getSelectedIndex();

        //Criar lista dos participantes marcados para a despesa
        ArrayList<Integer> listaParticipantesAdicionar = new ArrayList<>();

        //Verificar o item selecionado do combobox essencial é igual ao texto sim
        essencial = "Sim".equals(essencialComboBox.getSelectedItem());

        //Confirmar a edição de uma checkbox que ainda esteja ativa na tabela
        if (listaParticipantes.isEditing()) {
            listaParticipantes.getCellEditor().stopCellEditing();
        }

        //Verificar se a despesa não é essencial para ler os participantes marcados (essencial inclui todos)
        if (!essencial) {
            //Verificar o total de linhas da tabela lista de despesas
            int totalLinhasTabela = listaParticipantes.getRowCount();

            //Percorrer as linha da lista de participantes e verificar a checkbox para colocar na lista de adicionar ou remove da despesa
            for (int i = 0; i < totalLinhasTabela; i++) {
                boolean adicionar = (Boolean) listaParticipantes.getValueAt(i, 0);
                int idParticipante = Integer.parseInt((String) listaParticipantes.getValueAt(i, 1));

                //Adicionar a lista de acordo com o boolean adicionar
                if (adicionar) {
                    listaParticipantesAdicionar.add(idParticipante);
                }
            }
        }

        //Enviar os dados ao controller, que valida as regras antes de alterar o evento
        try {
            sistemaOriginal.salvarDespesa(eventoOriginal.getId(), idDespesa, nome, valor, essencial, prioridade, tipo, listaParticipantesAdicionar);
        } catch (RegraNegocioException e) {
            //Mensagem do erro e continua na edição sem alterar os dados
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dados inválidos!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Voltar para o menu de evento
        voltarEvento();
    }

    //Metodo para editar a despesa
    public void editarDespesa() {
        //Desbloquear os campos para edição
        nomeTexto.setEditable(true);
        totalTexto.setEditable(true);
        prioridadeComboBox.setEditable(true);
        tipoComboBox.setEnabled(true);
        essencialComboBox.setEnabled(true);
        prioridadeComboBox.setEnabled(true);

        //Chamar o metodo para atualizar a lista de participante com todos os participantes do evento com uma checkbox para adicionar a despesa
        editarListaParticipante();

        //Oculta ou revelar os botão necessários para edição
        salvarDespesaButton.setVisible(true);
        excluirDespesaButton.setVisible(true);
        descartarAlteracoesButton.setVisible(true);
        editarDespesaButton.setVisible(false);
        voltarEventoButton.setVisible(false);

        //Revelar a dica de marcar a checkbox para adicionar
        dicaEditar.setVisible(true);

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelDespesa.setText("Editar Despesa");
        painel.setTitle("Editar Despesa");

        //Desabilitar os campos desnecessários
        idTexto.setEnabled(false);
        quantParticipanteTexto.setEnabled(false);

        //Foca no valor a editar
        nomeTexto.requestFocus();

    }

    //Metodo para excluir a despesa
    public void excluirDespesa() {
        // Chama a janela para confirmar excluir a despesa
        int confirmacao = JOptionPane.showConfirmDialog(
                painel,
                "Deseja excluir a Despesa: " + nomeTexto.getText() + "?",
                "Confirmar excluir",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        //Verificar a confirmação para excluir a despesa
        if (confirmacao == JOptionPane.YES_OPTION) {
            //Criação de boolean de confirmação para excluir o Despesa
            boolean retornoConfirmacao = sistemaOriginal.excluirDespesa(eventoOriginal.getId(), Integer.parseInt(idTexto.getText()));

            //Verificar a confirmação e mostra uma mensagem na tela com o retorno da exclusão
            if (retornoConfirmacao) {
                JOptionPane.showMessageDialog(null, "Despesa " + nomeTexto.getText() + " excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Erro a excluir a Despesa " + nomeTexto.getText(), "Erro", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            //Se opção não, sair do metodo
            return;
        }

        //Volta para a tela de evento
        voltarEvento();

    }

    //Metodo para descartar alterações na despesa
    public void descartarAlteracoes() {
        //Verificar se a despesas na tela não é nulo(Nova despesa), se nulo volta para a tela de evento
        if (despesaTela != null) {
            //Recuperar os dados da despesa na tela
            atualizarTela();
            atualizarListaParticipante();

            //Bloqueia os campos para edição
            idTexto.setEditable(false);
            nomeTexto.setEditable(false);
            quantParticipanteTexto.setEditable(false);
            totalTexto.setEditable(false);
            tipoComboBox.setEnabled(false);
            essencialComboBox.setEnabled(false);
            prioridadeComboBox.setEnabled(false);

            //Oculta ou revelar os botão da janela
            salvarDespesaButton.setVisible(false);
            excluirDespesaButton.setVisible(false);
            descartarAlteracoesButton.setVisible(false);
            editarDespesaButton.setVisible(true);
            voltarEventoButton.setVisible(true);

            //Ocultar a dica de marcar a checkbox para adicionar
            dicaEditar.setVisible(false);

            //Mudar o nome da label do menu e o titulo da janela com o nome da operação
            labelDespesa.setText("Relatório da Despesa");
            painel.setTitle("Relatório da Despesa");

            //Habilita os campos
            idTexto.setEnabled(true);
            quantParticipanteTexto.setEnabled(true);

        } else {
            voltarEvento();
        }

    }

    //Metodo para salvar o sistema em arquivo
    public void salvarDados() {
        try {
            sistemaOriginal.salvar();
        } catch (PersistenciaException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Metodo para confirmar salvar o sistema em arquivo ao fechar a janela
    public void sairSalvar() {
        painel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Chama a janela de confirmação e colocar o valor dela na variável confirmação
                int confirmacao = JOptionPane.showConfirmDialog(
                        painel,
                        "Deseja salvar o sistema?",
                        "Confirmar fechamento",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                //Se selecionar sim na janela de confirmação executar o metodo de salvar o sistema
                if (confirmacao == JOptionPane.YES_OPTION) {
                    //Executa o metodo para salvar os dados
                    salvarDados();
                }

                //Encerra o programa após salvar ou selecionar não na janelar de confirmação
                System.exit(0);
            }
        });
    }

    //Metodo para retorna a view de evento
    public void voltarEvento() {
        //Executa o metodo para salvar os dados em arquivo
        salvarDados();

        // Pega a janela onde o painel conteúdo está inserido
        Window janelaAtual = SwingUtilities.windowForComponent(this.painelConteudo);

        // Fecha a janela atual e libera memória
        if (janelaAtual != null) {
            janelaAtual.dispose();
        }

        // Muda o estado do menu de evento para true
        telaOrigem.mudaExibir(true);

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        painelConteudo = new JPanel();
        painelConteudo.setLayout(new GridBagLayout());
        painelConteudo.setBackground(new Color(-1904641));
        painelConteudo.setMaximumSize(new Dimension(-1, -1));
        painelConteudo.setMinimumSize(new Dimension(-1, -1));
        painelConteudo.setPreferredSize(new Dimension(1280, 720));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setMaximumSize(new Dimension(-1, -1));
        panel1.setMinimumSize(new Dimension(500, -1));
        panel1.setPreferredSize(new Dimension(-1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 10, 10);
        painelConteudo.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        labelDespesa = new JLabel();
        labelDespesa.setHorizontalAlignment(0);
        labelDespesa.setHorizontalTextPosition(0);
        labelDespesa.setMaximumSize(new Dimension(167, 17));
        labelDespesa.setMinimumSize(new Dimension(167, 17));
        labelDespesa.setPreferredSize(new Dimension(167, 17));
        labelDespesa.setText("Relatorio de Despesa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel1.add(labelDespesa, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel2.setMinimumSize(new Dimension(500, 50));
        panel2.setPreferredSize(new Dimension(500, 50));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(panel2, gbc);
        idDespesa = new JLabel();
        idDespesa.setMaximumSize(new Dimension(-1, -1));
        idDespesa.setMinimumSize(new Dimension(-1, -1));
        idDespesa.setPreferredSize(new Dimension(100, 25));
        idDespesa.setText("Id da Despesa:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(idDespesa, gbc);
        idTexto = new JTextField();
        idTexto.setMaximumSize(new Dimension(-1, -1));
        idTexto.setMinimumSize(new Dimension(-1, -1));
        idTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(idTexto, gbc);
        quantParticipanteTexto = new JTextField();
        quantParticipanteTexto.setMaximumSize(new Dimension(-1, -1));
        quantParticipanteTexto.setMinimumSize(new Dimension(-1, -1));
        quantParticipanteTexto.setPreferredSize(new Dimension(20, 25));
        quantParticipanteTexto.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(quantParticipanteTexto, gbc);
        quantidadeParticipantes = new JLabel();
        quantidadeParticipantes.setMaximumSize(new Dimension(-1, -1));
        quantidadeParticipantes.setMinimumSize(new Dimension(-1, -1));
        quantidadeParticipantes.setPreferredSize(new Dimension(150, 25));
        quantidadeParticipantes.setText("Quant. de Participantes:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(quantidadeParticipantes, gbc);
        tipoDespesa = new JLabel();
        tipoDespesa.setMaximumSize(new Dimension(-1, -1));
        tipoDespesa.setMinimumSize(new Dimension(-1, -1));
        tipoDespesa.setPreferredSize(new Dimension(40, 25));
        tipoDespesa.setText("Tipo:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(tipoDespesa, gbc);
        essencialDespesa = new JLabel();
        essencialDespesa.setMaximumSize(new Dimension(-1, -1));
        essencialDespesa.setMinimumSize(new Dimension(-1, -1));
        essencialDespesa.setPreferredSize(new Dimension(70, 25));
        essencialDespesa.setText("Essencial:");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(essencialDespesa, gbc);
        prioridadeDespesa = new JLabel();
        prioridadeDespesa.setMaximumSize(new Dimension(-1, -1));
        prioridadeDespesa.setMinimumSize(new Dimension(-1, -1));
        prioridadeDespesa.setPreferredSize(new Dimension(70, 25));
        prioridadeDespesa.setText("Prioridade:");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(prioridadeDespesa, gbc);
        totalDespesa = new JLabel();
        totalDespesa.setMaximumSize(new Dimension(-1, -1));
        totalDespesa.setMinimumSize(new Dimension(-1, -1));
        totalDespesa.setPreferredSize(new Dimension(120, 25));
        totalDespesa.setText("Total da Despesa:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(totalDespesa, gbc);
        totalTexto = new JTextField();
        totalTexto.setMaximumSize(new Dimension(-1, -1));
        totalTexto.setMinimumSize(new Dimension(-1, -1));
        totalTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(totalTexto, gbc);
        nomeDespesa = new JLabel();
        nomeDespesa.setMaximumSize(new Dimension(-1, -1));
        nomeDespesa.setMinimumSize(new Dimension(-1, -1));
        nomeDespesa.setPreferredSize(new Dimension(120, 25));
        nomeDespesa.setText("Nome da Despesa:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(nomeDespesa, gbc);
        nomeTexto = new JTextField();
        nomeTexto.setMaximumSize(new Dimension(-1, -1));
        nomeTexto.setMinimumSize(new Dimension(-1, -1));
        nomeTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(nomeTexto, gbc);
        essencialComboBox = new JComboBox();
        essencialComboBox.setMaximumSize(new Dimension(-1, -1));
        essencialComboBox.setMinimumSize(new Dimension(-1, -1));
        essencialComboBox.setPreferredSize(new Dimension(50, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(essencialComboBox, gbc);
        tipoComboBox = new JComboBox();
        tipoComboBox.setPreferredSize(new Dimension(80, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(tipoComboBox, gbc);
        prioridadeComboBox = new JComboBox();
        prioridadeComboBox.setPreferredSize(new Dimension(50, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(prioridadeComboBox, gbc);
        abasParticipante = new JTabbedPane();
        abasParticipante.setMaximumSize(new Dimension(-1, -1));
        abasParticipante.setMinimumSize(new Dimension(-1, 200));
        abasParticipante.setPreferredSize(new Dimension(500, 400));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(abasParticipante, gbc);
        abaParticipante = new JScrollPane();
        abaParticipante.setPreferredSize(new Dimension(500, 400));
        abasParticipante.addTab("Participantes", abaParticipante);
        listaParticipantes = new JTable();
        abaParticipante.setViewportView(listaParticipantes);
        dicaEditar = new JLabel();
        dicaEditar.setPreferredSize(new Dimension(500, 17));
        dicaEditar.setText("Marque para Adicionar ou Remover (despesas essenciais são atribuídas a todos os participantes)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 10, 0);
        panel1.add(dicaEditar, gbc);
        menuVertical = new JPanel();
        menuVertical.setLayout(new GridBagLayout());
        menuVertical.setBackground(new Color(-9259781));
        menuVertical.setMaximumSize(new Dimension(-1, -1));
        menuVertical.setMinimumSize(new Dimension(-1, -1));
        menuVertical.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 10, 5);
        painelConteudo.add(menuVertical, gbc);
        tituloLabel = new JLabel();
        tituloLabel.setFocusable(false);
        Font tituloLabelFont = this.$$$getFont$$$(null, Font.BOLD, 20, tituloLabel.getFont());
        if (tituloLabelFont != null) tituloLabel.setFont(tituloLabelFont);
        tituloLabel.setHorizontalAlignment(0);
        tituloLabel.setHorizontalTextPosition(0);
        tituloLabel.setIconTextGap(1);
        tituloLabel.setMaximumSize(new Dimension(-1, -1));
        tituloLabel.setMinimumSize(new Dimension(-1, -1));
        tituloLabel.setPreferredSize(new Dimension(-1, 50));
        tituloLabel.setText("Gerenciador de Eventos");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        menuVertical.add(tituloLabel, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel3.setMaximumSize(new Dimension(-1, -1));
        panel3.setMinimumSize(new Dimension(-1, -1));
        panel3.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 20, 20, 20);
        menuVertical.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        salvarDespesaButton = new JButton();
        salvarDespesaButton.setMargin(new Insets(0, 10, 0, 10));
        salvarDespesaButton.setMaximumSize(new Dimension(-1, -1));
        salvarDespesaButton.setMinimumSize(new Dimension(50, 40));
        salvarDespesaButton.setPreferredSize(new Dimension(50, 40));
        salvarDespesaButton.setText("Salvar Despesa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(salvarDespesaButton, gbc);
        excluirDespesaButton = new JButton();
        excluirDespesaButton.setMargin(new Insets(0, 10, 0, 10));
        excluirDespesaButton.setMaximumSize(new Dimension(-1, -1));
        excluirDespesaButton.setMinimumSize(new Dimension(50, 40));
        excluirDespesaButton.setPreferredSize(new Dimension(50, 40));
        excluirDespesaButton.setText("Excluir Despesa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(excluirDespesaButton, gbc);
        descartarAlteracoesButton = new JButton();
        descartarAlteracoesButton.setMargin(new Insets(0, 10, 0, 10));
        descartarAlteracoesButton.setMaximumSize(new Dimension(-1, -1));
        descartarAlteracoesButton.setMinimumSize(new Dimension(50, 40));
        descartarAlteracoesButton.setPreferredSize(new Dimension(50, 40));
        descartarAlteracoesButton.setText("Descartar Alterações");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(descartarAlteracoesButton, gbc);
        voltarEventoButton = new JButton();
        voltarEventoButton.setMargin(new Insets(0, 10, 0, 10));
        voltarEventoButton.setMaximumSize(new Dimension(-1, -1));
        voltarEventoButton.setMinimumSize(new Dimension(50, 40));
        voltarEventoButton.setPreferredSize(new Dimension(50, 40));
        voltarEventoButton.setText("Voltar para o Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(voltarEventoButton, gbc);
        editarDespesaButton = new JButton();
        editarDespesaButton.setMargin(new Insets(0, 10, 0, 10));
        editarDespesaButton.setMaximumSize(new Dimension(-1, -1));
        editarDespesaButton.setMinimumSize(new Dimension(50, 40));
        editarDespesaButton.setPreferredSize(new Dimension(50, 40));
        editarDespesaButton.setText("Editar Despesa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(editarDespesaButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 500, 0, 0);
        painelConteudo.add(spacer1, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return painelConteudo;
    }
}
