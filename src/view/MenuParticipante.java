package view;

import controller.*;
import model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;


public class MenuParticipante {
    private Controle sistemaOriginal;
    private Evento eventoOriginal;
    private Participante participanteTela;
    private MenuEvento telaOrigem;
    private JFrame painel;
    private JPanel painelConteudo;
    private JPanel menuVertical;
    private JLabel tituloLabel;
    private JLabel labelParticipante;
    private JLabel idLabel;
    private JLabel nomeLabel;
    private JLabel quantidadeDespesasLabel;
    private JLabel statusLabel;
    private JLabel orcamentoLabel;
    private JLabel dicaEditar;
    private JTextField idTexto;
    private JTextField nomeTexto;
    private JTextField orcamentoTexto;
    private JTextField quantidadeDespesasTexto;
    private JTextField statusTexto;
    private JButton editarParticipanteButton;
    private JButton voltarEventoButton;
    private JButton salvarParticipanteButton;
    private JButton excluirParticipanteButton;
    private JTabbedPane abasDespesas;
    private JScrollPane abaDespesas;
    private JTable listaDespesas;
    private JButton descartarAlteracoesButton;

    public MenuParticipante() {
        //Criar a janela
        painel = new JFrame("Relatório do Participante");
        painel.setContentPane(this.painelConteudo);
        painel.pack();
        painel.setLocationRelativeTo(null);
        painel.setVisible(true);

        // Bloqueia o redimensionamento da janela
        painel.setResizable(false);

        //Ao fechar a janela gerar uma confirmação para salvar o sistema em arquivo
        painel.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        sairSalvar();

        //Bloqueia os campos para edição
        idTexto.setEditable(false);
        nomeTexto.setEditable(false);
        quantidadeDespesasTexto.setEditable(false);
        statusTexto.setEditable(false);
        orcamentoTexto.setEditable(false);
        //Ocultar os botões salvar e excluir participante
        salvarParticipanteButton.setVisible(false);
        excluirParticipanteButton.setVisible(false);
        descartarAlteracoesButton.setVisible(false);
        //Ocultar a dica de marcar a checkbox para adicionar
        dicaEditar.setVisible(false);


        //Botão para liberar a edição da janela do participante
        editarParticipanteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarParticipante();
            }
        });

        //Botão para salvar os dados do participante
        salvarParticipanteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarParticipante();
            }
        });

        //Botão para excluir o participante
        excluirParticipanteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirParticipante();
            }
        });

        //Botão para descartar as alterações no participantes e voltar ao relatorio do participante
        descartarAlteracoesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                descartarAlteracoes();
            }
        });

        //Botão para fecha a tela atual e volta para o menu de evento
        voltarEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                voltarEvento();
            }
        });

    }

    // Metodo para exibir a janela
    public void exibir(Controle sistemaTela, MenuEvento origem, Evento eventoMenu, int idParticipanteTela) {
        //Atribuir os dados do sistema, evento, tela origem e participante
        sistemaOriginal = sistemaTela;
        telaOrigem = origem;
        eventoOriginal = eventoMenu;
        participanteTela = eventoOriginal.getListaParticipantes().get(idParticipanteTela);

        //Atualizar a tela e a lista de despesas do participante
        atualizarTela();
        atualizarListaDespesas();

    }

    // Metodo para exibir a janela para cadastrar um novo participante
    public void exibirNovo(Controle sistemaTela, MenuEvento origem, Evento eventoMenu) {
        //Atribuir os dados do sistema, evento e tela origem
        sistemaOriginal = sistemaTela;
        telaOrigem = origem;
        eventoOriginal = eventoMenu;

        //Gera um id com o primeiro valor livre da lista de participantes do evento e adicionar ao texto do id
        int novoIdParticipante = sistemaOriginal.verificarIdLivre(eventoOriginal.getListaParticipantes());
        idTexto.setText(String.valueOf(novoIdParticipante));

        //Chama o metodo para editar o view participante
        editarParticipante();

        //Oculta o botão excluir participante
        excluirParticipanteButton.setVisible(false);

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelParticipante.setText("Cadastro de Novo Participante");
        painel.setTitle("Cadastro de Novo Participante");

    }

    //Atualiza os dados do participante da janela
    public void atualizarTela() {

        //Define variáveis para preencher os dados da janela
        BigDecimal orcamento = participanteTela.getOrcamento();
        BigDecimal valorOcamento = sistemaOriginal.calcularOrcamento(eventoOriginal.getId(), participanteTela.getId());
        String statusOrcamento = sistemaOriginal.statusOrcamento(valorOcamento, orcamento);

        //Defini os valores dos campos de texto da janela
        idTexto.setText(String.valueOf(this.participanteTela.getId()));
        nomeTexto.setText(this.participanteTela.getNome());
        quantidadeDespesasTexto.setText(String.valueOf(this.participanteTela.getListaDespesas().size()));
        statusTexto.setText(statusOrcamento);
        orcamentoTexto.setText(String.format("R$ %.2f", orcamento));

    }

    //Atualiza a lista de despesas da janela com as dados das despesas
    public void atualizarListaDespesas() {

        //Criar a lista de despesas do participante
        var dadosDespesas = participanteTela.getListaDespesas();

        //Verificar se os dados das Despesas não é nulo
        if (dadosDespesas != null) {
            //Criar os títulos das colunas da tabela
            String[] colunas = {"ID", "Nome", "Tipo", "Valor Total", "Quant. Participantes", "Essencial", "Prioridade"};

            //Criar o modelo da tabela
            DefaultTableModel modeloDadosDespesas = new DefaultTableModel(colunas, 0) {
                @Override
                //Bloqueia a linha para edição
                public boolean isCellEditable(int row, int column) {
                    // Retorna false para todas as células da tabela
                    return false;
                }
            };

            //Percorre a lista de despesas é adicionar a linha para desenha a tabela
            for (Despesas despesa : dadosDespesas.values()) {
                //Definir os valores da linha
                String id = String.valueOf(despesa.getId());
                String nome = despesa.getNome();
                String valorTotal = String.format("R$ %.2f", despesa.getValor());
                String qParticipante = despesa.getListaParticipantes().size() + " Participantes";
                String prioridade = String.valueOf(despesa.getPrioridade());

                //Define o texto de acordo com o tipo
                String tipo;
                if (despesa.getTipo() == 0) {
                    tipo = "Individual";
                } else {
                    tipo = "Grupo";
                }

                //Define o texto de acordo se é essencial
                String essencial;
                if (despesa.isEssencial()) {
                    essencial = "Sim";
                } else {
                    essencial = "Não";
                }

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{id, nome, tipo, valorTotal, qParticipante, essencial, prioridade};
                modeloDadosDespesas.addRow(linha);
            }
            //Adicionar o modelo a tabela da lista de despesas
            listaDespesas.setModel(modeloDadosDespesas);

            //Criar o modelo de alinhamento ao centro
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);

            //Setar o tamanho máximo das colunas
            listaDespesas.getColumnModel().getColumn(0).setMaxWidth(50);
            listaDespesas.getColumnModel().getColumn(1).setMaxWidth(300);
            listaDespesas.getColumnModel().getColumn(2).setMaxWidth(80);
            listaDespesas.getColumnModel().getColumn(3).setMaxWidth(150);
            listaDespesas.getColumnModel().getColumn(4).setMaxWidth(150);
            listaDespesas.getColumnModel().getColumn(5).setMaxWidth(80);
            listaDespesas.getColumnModel().getColumn(6).setMaxWidth(80);

            //Aplicar o alinhamento ao centro das colunas
            listaDespesas.getColumnModel().getColumn(0).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(2).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(3).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(4).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(5).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(6).setCellRenderer(centro);

        }
    }

    //Atualiza lista de despesas da janela com as dados das despesas com modo de seleção
    public void editarListaDespesas() {
        //Criar a lista de despesas do participante
        var dadosDespesas = eventoOriginal.getListaDespesas();

        //Verificar se os dados da Despesas não é nulo
        if (dadosDespesas != null) {
            //Criar os títulos das colunas da tabela
            String[] colunas = {"Adicionar", "ID", "Nome", "Tipo", "Valor Total", "Quant. Participantes", "Essencial", "Prioridade"};

            //Criar o modelo da tabela
            DefaultTableModel modeloDadosDespesas = new DefaultTableModel(colunas, 0) {
                @Override
                // Adicionar uma checkBox na primeira coluna
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    }
                    return super.getColumnClass(columnIndex);
                }

                @Override
                //Desbloqueia apenas a primeira coluna para edição se a despesa da linha não é essencial
                public boolean isCellEditable(int row, int column) {
                    //Verificar se a coluna é 0 ( checkbox para adicionar)
                    if (column == 0) {
                        // Pega se a despesa é essencial (coluna 6)
                        String essencial = (String) getValueAt(row, 6);
                        // Condição de Desbloqueio: Se o essencial é "Sim", retorna false (não editável)
                        if (essencial.equals("Sim")) {
                            return false;
                        }
                    }
                    // Para as outras linhas/colunas, mantém o comportamento padrão
                    return column == 0;
                }
            };

            //Percorre a lista de despesas é adicionar a linha para desenha a tabela
            for (Despesas despesa : dadosDespesas.values()) {
                //Definir os valores da linha
                boolean adicionar = false;
                String id = String.valueOf(despesa.getId());
                String nome = despesa.getNome();
                String valorTotal = String.format("R$ %.2f", despesa.getValor());
                String qParticipante = despesa.getListaParticipantes().size() + " Participantes";
                String prioridade = String.valueOf(despesa.getPrioridade());

                //Verificar se a despesa é essencial
                if (despesa.isEssencial()) {
                    adicionar = true;
                    //Verificar se o participante tela é nulo(novo participante) ou se é valido(editando um participantes ja cadastrado)
                } else if (participanteTela != null) {
                    //Verificar se o participante ja esta adicionado a despesas para marcar a checkbox adicionar com true
                    if (participanteTela.getListaDespesas().containsKey(despesa.getId())) {
                        adicionar = true;
                    }
                }

                //Define o texto de acordo com o tipo
                String tipo;
                if (despesa.getTipo() == 0) {
                    tipo = "Individual";
                } else {
                    tipo = "Grupo";
                }

                //Define o texto de acordo se é essencial
                String essencial;
                if (despesa.isEssencial()) {
                    essencial = "Sim";
                } else {
                    essencial = "Não";
                }

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{adicionar, id, nome, tipo, valorTotal, qParticipante, essencial, prioridade};
                modeloDadosDespesas.addRow(linha);
            }

            //Adicionar o modelo a tabela da lista de despesas
            listaDespesas.setModel(modeloDadosDespesas);

            //Criar o modelo de alinhamento ao centro
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);

            //Setar o tamanho máximo das colunas
            listaDespesas.getColumnModel().getColumn(0).setMaxWidth(80);
            listaDespesas.getColumnModel().getColumn(1).setMaxWidth(50);
            listaDespesas.getColumnModel().getColumn(2).setMaxWidth(300);
            listaDespesas.getColumnModel().getColumn(3).setMaxWidth(80);
            listaDespesas.getColumnModel().getColumn(4).setMaxWidth(150);
            listaDespesas.getColumnModel().getColumn(5).setMaxWidth(150);
            listaDespesas.getColumnModel().getColumn(6).setMaxWidth(80);
            listaDespesas.getColumnModel().getColumn(7).setMaxWidth(80);

            //Aplicar o alinhamento ao centro das colunas
            listaDespesas.getColumnModel().getColumn(1).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(3).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(4).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(5).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(6).setCellRenderer(centro);
            listaDespesas.getColumnModel().getColumn(7).setCellRenderer(centro);

        }

    }

    //Metodo para editar o participante
    public void editarParticipante() {
        //Desbloquear os campos para edição
        nomeTexto.setEditable(true);
        orcamentoTexto.setEditable(true);

        //Chamar o metodo para atualizar a lista de despesas com todas as despesas do evento com uma checkbox para adicionar ao participante
        editarListaDespesas();

        //Oculta ou revelar os botão necessários para edição
        editarParticipanteButton.setVisible(false);
        voltarEventoButton.setVisible(false);
        salvarParticipanteButton.setVisible(true);
        excluirParticipanteButton.setVisible(true);
        descartarAlteracoesButton.setVisible(true);

        //Revelar a dica de marcar a checkbox para adicionar
        dicaEditar.setVisible(true);

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelParticipante.setText("Editar Participante");
        painel.setTitle("Editar Participante");

        //Desabilitar os campos desnecessários
        idTexto.setEnabled(false);
        statusTexto.setEnabled(false);
        quantidadeDespesasTexto.setEnabled(false);

        //Foca no valor a editar
        nomeTexto.requestFocus();

    }

    //Metodo para salvar os dados do participante
    public void salvarParticipante() {
        //Criar variáveis para verificação
        String nome = nomeTexto.getText();
        String orcamentoValorTexto = orcamentoTexto.getText().trim();
        BigDecimal orcamento;

        //Verificar se o nome do participante é vazio e mostra uma mensagem de erro e volta para edição
        if (nome.isEmpty()) {
            //Verificar se o participante na tela não e nulo(novo participante) para carregar o nome original do participante
            if (participanteTela != null) {
                nomeTexto.setText(participanteTela.getNome());
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, "Nome do participante não pode ser vazio!!", "Nome vazio ou nulo!", JOptionPane.ERROR_MESSAGE);
            nomeTexto.requestFocus();
            return;
        }

        //Verificar se o total da despesa é vazio
        if (orcamentoValorTexto.isEmpty()) {
            //Verificar se o participante na tela não é nulo(novo participante) para carregar o orçamento original do participante
            if (participanteTela != null) {
                orcamentoTexto.setText(String.format("R$ %.2f", participanteTela.getOrcamento()));
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, "Orçamento do participante não pode ser vazio!!", "Orçamento vazio!", JOptionPane.ERROR_MESSAGE);
            orcamentoTexto.requestFocus();
            return;
        }

        try {
            //Converte o texto ("70,00", "70.00" ou "R$ 1.234,56") em bigDecimal pelo controller
            orcamento = Controle.converterValor(orcamentoValorTexto);

            //Captura o erro de numero invalido
        } catch (RegraNegocioException e) {
            //Verificar se o participante na tela não é nulo(novo participante) para carregar o orçamento original do participante
            if (participanteTela != null) {
                orcamentoTexto.setText(String.format("R$ %.2f", participanteTela.getOrcamento()));
            }

            //Mensagem do erro
            JOptionPane.showMessageDialog(null, e.getMessage(), "Orçamento inválido!", JOptionPane.ERROR_MESSAGE);
            orcamentoTexto.requestFocus();
            return;
        }

        //Criar os valores dos atributos do participante
        int idParticipante = Integer.parseInt(idTexto.getText());

        //Criar lista das despesas marcadas para o participante
        ArrayList<Integer> listaDespesasAdicionar = new ArrayList<>();

        //Confirmar a edição de uma checkbox que ainda esteja ativa na tabela
        if (listaDespesas.isEditing()) {
            listaDespesas.getCellEditor().stopCellEditing();
        }

        //Verificar o total de linhas da tabela lista de despesas
        int totalLinhasTabela = listaDespesas.getRowCount();

        //Percorrer as linha da lista de despesas e verificar a checkbox para colocar na lista de adicionar ou remove do participantes
        for (int i = 0; i < totalLinhasTabela; i++) {
            boolean adicionar = (Boolean) listaDespesas.getValueAt(i, 0);
            String idDespesaTexto = (String) listaDespesas.getValueAt(i, 1);

            //Colocar na lista de acordo com o boolean adicionar
            if (adicionar) {
                listaDespesasAdicionar.add(Integer.parseInt(idDespesaTexto));
            }
        }

        //Enviar os dados ao controller, que valida as regras antes de alterar o evento
        try {
            sistemaOriginal.salvarParticipante(eventoOriginal.getId(), idParticipante, nome, orcamento, listaDespesasAdicionar, participanteTela == null);
        } catch (RegraNegocioException e) {
            //Mensagem do erro e continua na edição sem alterar os dados
            JOptionPane.showMessageDialog(null, e.getMessage(), "Dados inválidos!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Voltar para o menu de evento
        voltarEvento();
    }

    //Metodo para excluir o participante
    public void excluirParticipante() {
        // Chama a janela para confirmar excluir o participante
        int confirmacao = JOptionPane.showConfirmDialog(
                painel,
                "Deseja excluir o Participante: " + nomeTexto.getText() + "?",
                "Confirmar excluir",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        //Verificar a confirmação para excluir o participante
        if (confirmacao == JOptionPane.YES_OPTION) {
            //Criação de boolean de confirmação para excluir o participante
            boolean retornoConfirmacao;
            try {
                retornoConfirmacao = sistemaOriginal.excluirParticipante(eventoOriginal.getId(), participanteTela.getId());
            } catch (RegraNegocioException e) {
                //Exclusão impedida por haver despesas vinculadas: mostra o motivo e continua na tela
                JOptionPane.showMessageDialog(null, e.getMessage(), "Exclusão não permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //Verificar o retorno do metodo para excluir participante e mostra uma mensagem na tela
            if (retornoConfirmacao) {
                JOptionPane.showMessageDialog(null, "Participante " + nomeTexto.getText() + " excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Erro a excluir o participante " + nomeTexto.getText(), "Erro", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            //Se opção não, sair do metodo
            return;
        }

        //Volta para a tela de evento
        voltarEvento();
    }

    //Metodo para descartar alterações no participante
    public void descartarAlteracoes() {
        //Verificar se o participante na tela não e nulo(Novo Participante), se nulo volta para a tela de evento
        if (participanteTela != null) {
            //Recuperar os dados do participante na tela
            atualizarTela();
            atualizarListaDespesas();

            //Bloqueia os campos para edição
            idTexto.setEditable(false);
            nomeTexto.setEditable(false);
            quantidadeDespesasTexto.setEditable(false);
            statusTexto.setEditable(false);
            orcamentoTexto.setEditable(false);

            //Oculta ou revelar os botão necessários
            editarParticipanteButton.setVisible(true);
            voltarEventoButton.setVisible(true);
            salvarParticipanteButton.setVisible(false);
            excluirParticipanteButton.setVisible(false);
            descartarAlteracoesButton.setVisible(false);

            //Ocultar a dica de marcar a checkbox para adicionar
            dicaEditar.setVisible(false);

            //Mudar o nome da label do menu e o titulo da janela com o nome da operação
            labelParticipante.setText("Relatório do Participante");
            painel.setTitle("Relatório do Participante");

            //Habilitar os campos
            idTexto.setEnabled(true);
            statusTexto.setEnabled(true);
            quantidadeDespesasTexto.setEnabled(true);

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
        menuVertical = new JPanel();
        menuVertical.setLayout(new GridBagLayout());
        menuVertical.setBackground(new Color(-9259781));
        menuVertical.setMaximumSize(new Dimension(-1, -1));
        menuVertical.setMinimumSize(new Dimension(-1, -1));
        menuVertical.setPreferredSize(new Dimension(-1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 10, 5);
        painelConteudo.add(menuVertical, gbc);
        tituloLabel = new JLabel();
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setMaximumSize(new Dimension(-1, -1));
        panel1.setMinimumSize(new Dimension(-1, -1));
        panel1.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 20, 20, 20);
        menuVertical.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        voltarEventoButton = new JButton();
        voltarEventoButton.setMargin(new Insets(0, 10, 0, 10));
        voltarEventoButton.setMaximumSize(new Dimension(-1, -1));
        voltarEventoButton.setMinimumSize(new Dimension(50, 40));
        voltarEventoButton.setPreferredSize(new Dimension(50, 40));
        voltarEventoButton.setText("Voltar ao Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(voltarEventoButton, gbc);
        salvarParticipanteButton = new JButton();
        salvarParticipanteButton.setMargin(new Insets(0, 10, 0, 10));
        salvarParticipanteButton.setMaximumSize(new Dimension(-1, -1));
        salvarParticipanteButton.setMinimumSize(new Dimension(500, 40));
        salvarParticipanteButton.setPreferredSize(new Dimension(50, 40));
        salvarParticipanteButton.setText("Salvar Participante");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(salvarParticipanteButton, gbc);
        excluirParticipanteButton = new JButton();
        excluirParticipanteButton.setMargin(new Insets(0, 10, 0, 10));
        excluirParticipanteButton.setMaximumSize(new Dimension(-1, -1));
        excluirParticipanteButton.setMinimumSize(new Dimension(50, 40));
        excluirParticipanteButton.setPreferredSize(new Dimension(50, 40));
        excluirParticipanteButton.setText("Excluir Participante");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(excluirParticipanteButton, gbc);
        descartarAlteracoesButton = new JButton();
        descartarAlteracoesButton.setMargin(new Insets(0, 10, 0, 10));
        descartarAlteracoesButton.setMaximumSize(new Dimension(-1, -1));
        descartarAlteracoesButton.setMinimumSize(new Dimension(50, 40));
        descartarAlteracoesButton.setPreferredSize(new Dimension(50, 40));
        descartarAlteracoesButton.setText("Descartar Alterações");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(descartarAlteracoesButton, gbc);
        editarParticipanteButton = new JButton();
        editarParticipanteButton.setMargin(new Insets(0, 10, 0, 10));
        editarParticipanteButton.setMaximumSize(new Dimension(-1, -1));
        editarParticipanteButton.setMinimumSize(new Dimension(50, 40));
        editarParticipanteButton.setPreferredSize(new Dimension(50, 40));
        editarParticipanteButton.setText("Editar Participante");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(editarParticipanteButton, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel2.setMaximumSize(new Dimension(-1, -1));
        panel2.setMinimumSize(new Dimension(500, -1));
        panel2.setOpaque(true);
        panel2.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 10, 10);
        painelConteudo.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        labelParticipante = new JLabel();
        labelParticipante.setHorizontalAlignment(0);
        labelParticipante.setHorizontalTextPosition(0);
        labelParticipante.setMaximumSize(new Dimension(123, 17));
        labelParticipante.setMinimumSize(new Dimension(123, 17));
        labelParticipante.setPreferredSize(new Dimension(123, 17));
        labelParticipante.setText("Relatorio do Participante");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel2.add(labelParticipante, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel3.setMaximumSize(new Dimension(-1, -1));
        panel3.setMinimumSize(new Dimension(500, 50));
        panel3.setPreferredSize(new Dimension(500, 50));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(panel3, gbc);
        idLabel = new JLabel();
        idLabel.setMaximumSize(new Dimension(-1, -1));
        idLabel.setMinimumSize(new Dimension(-1, -1));
        idLabel.setPreferredSize(new Dimension(100, 25));
        idLabel.setText("Id:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(idLabel, gbc);
        idTexto = new JTextField();
        idTexto.setMaximumSize(new Dimension(-1, -1));
        idTexto.setMinimumSize(new Dimension(-1, -1));
        idTexto.setPreferredSize(new Dimension(10, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(idTexto, gbc);
        nomeTexto = new JTextField();
        nomeTexto.setMaximumSize(new Dimension(-1, -1));
        nomeTexto.setMinimumSize(new Dimension(-1, -1));
        nomeTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(nomeTexto, gbc);
        orcamentoLabel = new JLabel();
        orcamentoLabel.setMaximumSize(new Dimension(-1, -1));
        orcamentoLabel.setMinimumSize(new Dimension(-1, -1));
        orcamentoLabel.setPreferredSize(new Dimension(100, 25));
        orcamentoLabel.setText("Orçamento:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(orcamentoLabel, gbc);
        orcamentoTexto = new JTextField();
        orcamentoTexto.setMaximumSize(new Dimension(-1, -1));
        orcamentoTexto.setMinimumSize(new Dimension(-1, -1));
        orcamentoTexto.setPreferredSize(new Dimension(10, 25));
        orcamentoTexto.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(orcamentoTexto, gbc);
        quantidadeDespesasLabel = new JLabel();
        quantidadeDespesasLabel.setMaximumSize(new Dimension(-1, -1));
        quantidadeDespesasLabel.setMinimumSize(new Dimension(-1, -1));
        quantidadeDespesasLabel.setPreferredSize(new Dimension(145, 25));
        quantidadeDespesasLabel.setText("Quantidade de Despesas:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(quantidadeDespesasLabel, gbc);
        quantidadeDespesasTexto = new JTextField();
        quantidadeDespesasTexto.setMaximumSize(new Dimension(-1, -1));
        quantidadeDespesasTexto.setMinimumSize(new Dimension(-1, -1));
        quantidadeDespesasTexto.setPreferredSize(new Dimension(10, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(quantidadeDespesasTexto, gbc);
        statusLabel = new JLabel();
        statusLabel.setMaximumSize(new Dimension(-1, -1));
        statusLabel.setMinimumSize(new Dimension(-1, -1));
        statusLabel.setPreferredSize(new Dimension(130, 25));
        statusLabel.setText("Status do orçamento:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(statusLabel, gbc);
        statusTexto = new JTextField();
        statusTexto.setMaximumSize(new Dimension(-1, -1));
        statusTexto.setMinimumSize(new Dimension(-1, -1));
        statusTexto.setPreferredSize(new Dimension(100, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(statusTexto, gbc);
        nomeLabel = new JLabel();
        nomeLabel.setMaximumSize(new Dimension(-1, -1));
        nomeLabel.setMinimumSize(new Dimension(-1, -1));
        nomeLabel.setPreferredSize(new Dimension(100, 25));
        nomeLabel.setText("Nome do Participante:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel3.add(nomeLabel, gbc);
        abasDespesas = new JTabbedPane();
        abasDespesas.setMaximumSize(new Dimension(-1, -1));
        abasDespesas.setMinimumSize(new Dimension(-1, 200));
        abasDespesas.setPreferredSize(new Dimension(500, 400));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(abasDespesas, gbc);
        abaDespesas = new JScrollPane();
        abaDespesas.setPreferredSize(new Dimension(500, 400));
        abasDespesas.addTab("Despesas", abaDespesas);
        listaDespesas = new JTable();
        abaDespesas.setViewportView(listaDespesas);
        dicaEditar = new JLabel();
        dicaEditar.setMaximumSize(new Dimension(166, 17));
        dicaEditar.setMinimumSize(new Dimension(500, 17));
        dicaEditar.setPreferredSize(new Dimension(166, 17));
        dicaEditar.setText("Marque para Adicionar ou Remover (despesas essenciais não podem ser modificadas)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 10, 0);
        panel2.add(dicaEditar, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
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
