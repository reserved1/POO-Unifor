package view;

import controller.Controle;
import model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.Locale;

public class MenuEvento {
    private Controle sistemaOriginal;
    private Evento eventoTela;
    private JFrame painel;
    private JPanel painelConteudo;
    private JLabel labelEvento;
    private JLabel idEvento;
    private JLabel nomeEvento;
    private JLabel totalEvento;
    private JLabel quantidadeDespesas;
    private JLabel quantidadeParticipantes;
    private JTextField idEventoTexto;
    private JTextField nomeEventoTexto;
    private JTextField totalEventoTexto;
    private JTextField quantidadeParticipantesTexto;
    private JTextField quantidadeDespesasTexto;
    private JButton fecharEventoButton;
    private JButton novoParticipanteButton;
    private JTabbedPane abasEvento;
    private JScrollPane abaParticipantes;
    private JScrollPane abaDespesas;
    private JTable listaParticipantes;
    private JTable listaDespesas;
    private JButton novaDespesaButton;
    private JButton editarEventoButton;
    private JButton salvarEventoButton;
    private JButton excluirEventoButton;
    private JButton descartarAlteracoesButton;
    private JLabel dicaSelecao;
    private JPanel menuVertical;
    private JLabel label;


    public MenuEvento() {
        //Criar a janela
        painel = new JFrame("Relatório do Evento");
        painel.setContentPane(this.painelConteudo);
        painel.pack();
        painel.setLocationRelativeTo(null);
        painel.setVisible(true);

        // Bloqueia o redimensionamento da janela
        painel.setResizable(false);

        //Ao fechar a janela encerrar o programa
        painel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //chama metodo para bloquear edição do evento
        bloquearEdicao();

        //Botão para habilitar a edição do evento da janela
        editarEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarEvento();
            }
        });

        //Botão para salva os dados do evento
        salvarEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarEvento();
            }

        });

        //Botão de excluir o evento da janela e voltar ao menu principal
        excluirEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirEvento();
            }
        });

        //Botão para descartar as alterações do evento e voltar ao relatorio do evento
        descartarAlteracoesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                descartarAlteracoes();
            }
        });

        //Fecha o evento e volta para o menu principal
        fecharEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fecharEvento();
            }
        });

    }

    // Metodo para exibir a janela
    public void exibir(Controle sistemaTela, Evento eventoTela) {
        //Atribuir os dados do sistema e evento da tela
        sistemaOriginal = sistemaTela;
        this.eventoTela = eventoTela;

        //Atualizar a tela de evento
        atualizarTela();

    }

    //Atualiza os dados do evento da janela
    public void atualizarTela() {
        //Chamar o metodo para calcular o total do evento e colocar em uma variável
        BigDecimal orcamentoEvento = sistemaOriginal.calcularTotalEvento(eventoTela.getId());

        //Defini os valores dos campos de texto da janela
        idEventoTexto.setText(String.valueOf(eventoTela.getId()));
        nomeEventoTexto.setText(eventoTela.getNome());
        quantidadeParticipantesTexto.setText(String.valueOf(eventoTela.getListaParticipantes().size()));
        quantidadeDespesasTexto.setText(String.valueOf(eventoTela.getListaDespesas().size()));
        totalEventoTexto.setText(String.format("R$ %.2f", orcamentoEvento));

        //Atualiza as tabelas da lista de participante e da lista de despesas
        atualizarListaParticipante();
        atualizarListaDespesas();
    }

    //Atualiza a lista de participantes da janela
    public void atualizarListaParticipante() {

        //Criar a lista de despesas do evento
        var dadosParticipantes = eventoTela.getListaParticipantes();

        //Verificar se os dados dos participante não estão nulo
        if (dadosParticipantes != null) {
            //Criar os títulos das colunas da tabela
            String[] colunas = {"ID", "Nome", "Orçamento", "Orcamento Final", "Status Orçamento", "Quant. Despesas"};

            //Criar o modelo da tabelar
            DefaultTableModel modeloDadosParticipante = new DefaultTableModel(colunas, 0) {
                @Override
                //Bloqueia a linha para edição
                public boolean isCellEditable(int row, int column) {
                    // Retorna false todas as células da tabela
                    return false;
                }
            };

            //Percorre a lista de participante é adicionar a linha para desenha a tabela
            for (Participante participante : dadosParticipantes.values()) {
                //definir variáveis para o status do orçamento
                BigDecimal orcamento = participante.getOrcamento();
                BigDecimal orcamentoFinal = sistemaOriginal.calcularOrcamento(eventoTela.getId(), participante.getId());

                //Definir os valores da linha
                String id = String.valueOf(participante.getId());
                String nome = participante.getNome();
                String orcamentoTexto = String.format("R$ %.2f", orcamento);
                String orcamentoFinalTexto = String.format("R$ %.2f", orcamentoFinal);
                String statusOrcamento = sistemaOriginal.statusOrcamento(orcamentoFinal, orcamento);
                String qDespesas = participante.getListaDespesas().size() + " Despesas";

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{id, nome, orcamentoTexto, orcamentoFinalTexto, statusOrcamento, qDespesas};
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

    //Atualiza a lista de despesas da janela
    public void atualizarListaDespesas() {
        //Criar a lista de despesas do evento
        var dadosDespesas = eventoTela.getListaDespesas();

        //Verificar se os dados da Despesas não estão nulo
        if (dadosDespesas != null) {
            //Criar os títulos das colunas das despesas
            String[] colunas = {"ID", "Nome", "Tipo", "Valor Total", "Quant. Participantes", "Essencial", "Prioridade"};

            //Criar o modelo da tabelar
            DefaultTableModel modeloDadosDespesas = new DefaultTableModel(colunas, 0) {
                @Override
                //Bloqueia a linha para edição
                public boolean isCellEditable(int row, int column) {
                    // Retorna false todas as células da tabela
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

    // Metodo para editar o evento
    public void editarEvento() {
        //Desbloquear o campo de nome do evento para edição
        nomeEventoTexto.setEditable(true);

        //Revelar os botão de salvar e excluir
        salvarEventoButton.setVisible(true);
        excluirEventoButton.setVisible(true);
        descartarAlteracoesButton.setVisible(true);

        //Ocultar os botão de novo participante, nova despesa e editar evento
        novoParticipanteButton.setVisible(false);
        novaDespesaButton.setVisible(false);
        editarEventoButton.setVisible(false);
        fecharEventoButton.setVisible(false);

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelEvento.setText("Editar Evento");
        painel.setTitle("Editar Evento");

        //Desabilitar os campos desnecessários
        idEventoTexto.setEnabled(false);
        quantidadeParticipantesTexto.setEnabled(false);
        quantidadeDespesasTexto.setEnabled(false);
        totalEventoTexto.setEnabled(false);

        //Foca no valor a editar
        nomeEventoTexto.requestFocus();

    }

    //Metodo para salvar a edição do evento
    public void salvarEvento() {
        //Criar uma variável com o nome salvo da janela
        String nomeEventoSalvo = nomeEventoTexto.getText();

        //Verificar se o nome do evento salvo não e nulo ou vazio
        if (nomeEventoSalvo != null && !nomeEventoSalvo.isEmpty()) {
            //Compara o nome salvo com o nome do evento da tela, se diferente adicionar o nome salvo ao evento
            if (!nomeEventoSalvo.equals(eventoTela.getNome())) {
                eventoTela.setNome(nomeEventoSalvo);
            }

            //Se o nome for vazio ou nulo mostra uma mensagem de erro e volta para edição
        } else {
            nomeEventoTexto.setText(eventoTela.getNome());
            JOptionPane.showMessageDialog(null, "Nome não pode ser vazio!!", "Nome vazio!", JOptionPane.ERROR_MESSAGE);
            nomeEventoTexto.requestFocus();
            return;
        }

        //chama metodo para bloquear edição do evento
        bloquearEdicao();

    }

    // Metodo para excluir o evento
    public void excluirEvento() {
        // Chama a janela para confirmar excluir o evento
        int confirmacao = JOptionPane.showConfirmDialog(
                painel,
                "Deseja excluir o Evento: " + nomeEventoTexto.getText() + "?",
                "Confirmar excluir",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        //Verificar a confirmação para excluir participante
        if (confirmacao == JOptionPane.YES_OPTION) {
            //Criar uma variável de confirmação da exclusão
            boolean retornoConfirmacao = sistemaOriginal.excluirEvento(eventoTela.getId());

            //Mostra uma mensagem de confirmação da exclusão
            if (retornoConfirmacao) {
                JOptionPane.showMessageDialog(null, "Evento " + nomeEventoTexto.getText() + " excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Erro a excluir o evento " + nomeEventoTexto.getText(), "Erro", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            //Se opção não, sair do metodo
            return;
        }

        //Fecha o evento e volta para o menu principal
        fecharEvento();

    }

    // Metodo para descartar alterações no evento
    public void descartarAlteracoes() {
        //Redefinir o nome do evento para o original
        nomeEventoTexto.setText(eventoTela.getNome());

        //chama metodo para bloquear edição do evento
        bloquearEdicao();

        //Mudar o nome da label do menu e o titulo da janela com o nome da operação
        labelEvento.setText("Relatório do Evento");
        painel.setTitle("Relatório do Evento");

    }

    //Metodo para bloquear edição do evento
    public void bloquearEdicao() {
        //Bloqueia os campos para edição
        idEventoTexto.setEditable(false);
        nomeEventoTexto.setEditable(false);
        quantidadeParticipantesTexto.setEditable(false);
        quantidadeDespesasTexto.setEditable(false);
        totalEventoTexto.setEditable(false);

        //Ocultar os botões salvar, excluir e descartar alteração do evento
        salvarEventoButton.setVisible(false);
        excluirEventoButton.setVisible(false);
        descartarAlteracoesButton.setVisible(false);

        //Revelar os botão do evento
        novoParticipanteButton.setVisible(true);
        novaDespesaButton.setVisible(true);
        editarEventoButton.setVisible(true);
        fecharEventoButton.setVisible(true);

        //Habilita os campos desnecessários
        idEventoTexto.setEnabled(true);
        quantidadeParticipantesTexto.setEnabled(true);
        quantidadeDespesasTexto.setEnabled(true);
        totalEventoTexto.setEnabled(true);

    }

    //Metodo para fechar o evento e voltar para o menu principal
    public void fecharEvento() {
        //Pega a janela onde o painel está inserido
        Window janelaAtual = SwingUtilities.windowForComponent(this.painelConteudo);

        // Fecha a janela atual e libera memória
        if (janelaAtual != null) {
            janelaAtual.dispose();
        }

        //Abrir a Janela do menu principal
        MenuPrincipal proximaJanela = new MenuPrincipal();
        proximaJanela.exibir(sistemaOriginal);
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
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 10, 10);
        painelConteudo.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        labelEvento = new JLabel();
        labelEvento.setHorizontalAlignment(0);
        labelEvento.setHorizontalTextPosition(0);
        labelEvento.setText("Relatorio do Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel1.add(labelEvento, gbc);
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
        idEvento = new JLabel();
        idEvento.setMaximumSize(new Dimension(-1, -1));
        idEvento.setMinimumSize(new Dimension(-1, -1));
        idEvento.setOpaque(false);
        idEvento.setPreferredSize(new Dimension(100, 25));
        idEvento.setText("Id do Evento:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(idEvento, gbc);
        nomeEvento = new JLabel();
        nomeEvento.setText("Nome do Evento:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(nomeEvento, gbc);
        nomeEventoTexto = new JTextField();
        nomeEventoTexto.setMaximumSize(new Dimension(-1, -1));
        nomeEventoTexto.setMinimumSize(new Dimension(-1, -1));
        nomeEventoTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(nomeEventoTexto, gbc);
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
        quantidadeParticipantesTexto = new JTextField();
        quantidadeParticipantesTexto.setMaximumSize(new Dimension(-1, -1));
        quantidadeParticipantesTexto.setMinimumSize(new Dimension(-1, -1));
        quantidadeParticipantesTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(quantidadeParticipantesTexto, gbc);
        quantidadeDespesas = new JLabel();
        quantidadeDespesas.setText("Quant. de Despesas:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(quantidadeDespesas, gbc);
        quantidadeDespesasTexto = new JTextField();
        quantidadeDespesasTexto.setMaximumSize(new Dimension(-1, -1));
        quantidadeDespesasTexto.setMinimumSize(new Dimension(-1, -1));
        quantidadeDespesasTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(quantidadeDespesasTexto, gbc);
        totalEvento = new JLabel();
        totalEvento.setText("Total do Evento:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(totalEvento, gbc);
        totalEventoTexto = new JTextField();
        totalEventoTexto.setMaximumSize(new Dimension(-1, -1));
        totalEventoTexto.setMinimumSize(new Dimension(-1, -1));
        totalEventoTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(totalEventoTexto, gbc);
        idEventoTexto = new JTextField();
        idEventoTexto.setMaximumSize(new Dimension(-1, -1));
        idEventoTexto.setMinimumSize(new Dimension(-1, -1));
        idEventoTexto.setPreferredSize(new Dimension(20, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel2.add(idEventoTexto, gbc);
        abasEvento = new JTabbedPane();
        abasEvento.setMaximumSize(new Dimension(-1, -1));
        abasEvento.setMinimumSize(new Dimension(-1, 200));
        abasEvento.setPreferredSize(new Dimension(500, 400));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(abasEvento, gbc);
        abaParticipantes = new JScrollPane();
        abaParticipantes.setPreferredSize(new Dimension(500, 400));
        abasEvento.addTab("Participantes", abaParticipantes);
        listaParticipantes = new JTable();
        abaParticipantes.setViewportView(listaParticipantes);
        abaDespesas = new JScrollPane();
        abaDespesas.setPreferredSize(new Dimension(500, 400));
        abasEvento.addTab("Despesas", abaDespesas);
        listaDespesas = new JTable();
        abaDespesas.setViewportView(listaDespesas);
        dicaSelecao = new JLabel();
        dicaSelecao.setText("Duplo clique para detalhes");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 10, 0);
        panel1.add(dicaSelecao, gbc);
        menuVertical = new JPanel();
        menuVertical.setLayout(new GridBagLayout());
        menuVertical.setBackground(new Color(-9259781));
        menuVertical.setMaximumSize(new Dimension(-1, -1));
        menuVertical.setMinimumSize(new Dimension(-1, -1));
        menuVertical.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 10, 5);
        painelConteudo.add(menuVertical, gbc);
        label = new JLabel();
        label.setFocusable(false);
        Font labelFont = this.$$$getFont$$$(null, Font.BOLD, 20, label.getFont());
        if (labelFont != null) label.setFont(labelFont);
        label.setHorizontalAlignment(0);
        label.setHorizontalTextPosition(0);
        label.setIconTextGap(1);
        label.setMaximumSize(new Dimension(-1, -1));
        label.setMinimumSize(new Dimension(-1, -1));
        label.setPreferredSize(new Dimension(-1, 50));
        label.setText("Gerenciador de Eventos");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        menuVertical.add(label, gbc);
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
        novaDespesaButton = new JButton();
        novaDespesaButton.setLabel("Cadastrar Nova Despesa");
        novaDespesaButton.setMargin(new Insets(0, 10, 0, 10));
        novaDespesaButton.setMaximumSize(new Dimension(-1, -1));
        novaDespesaButton.setMinimumSize(new Dimension(50, 40));
        novaDespesaButton.setPreferredSize(new Dimension(50, 40));
        novaDespesaButton.setText("Cadastrar Nova Despesa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(novaDespesaButton, gbc);
        editarEventoButton = new JButton();
        editarEventoButton.setMaximumSize(new Dimension(-1, -1));
        editarEventoButton.setMinimumSize(new Dimension(50, 40));
        editarEventoButton.setPreferredSize(new Dimension(50, 40));
        editarEventoButton.setText("Editar Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(editarEventoButton, gbc);
        salvarEventoButton = new JButton();
        salvarEventoButton.setMaximumSize(new Dimension(-1, -1));
        salvarEventoButton.setMinimumSize(new Dimension(50, 40));
        salvarEventoButton.setPreferredSize(new Dimension(50, 40));
        salvarEventoButton.setText("Salvar Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(salvarEventoButton, gbc);
        fecharEventoButton = new JButton();
        fecharEventoButton.setMaximumSize(new Dimension(-1, -1));
        fecharEventoButton.setMinimumSize(new Dimension(50, 40));
        fecharEventoButton.setPreferredSize(new Dimension(50, 40));
        fecharEventoButton.setText("Fechar Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(fecharEventoButton, gbc);
        excluirEventoButton = new JButton();
        excluirEventoButton.setMaximumSize(new Dimension(-1, -1));
        excluirEventoButton.setMinimumSize(new Dimension(50, 40));
        excluirEventoButton.setPreferredSize(new Dimension(50, 40));
        excluirEventoButton.setText("Excluir Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(excluirEventoButton, gbc);
        descartarAlteracoesButton = new JButton();
        descartarAlteracoesButton.setMargin(new Insets(0, 0, 0, 0));
        descartarAlteracoesButton.setMaximumSize(new Dimension(-1, -1));
        descartarAlteracoesButton.setMinimumSize(new Dimension(50, 40));
        descartarAlteracoesButton.setPreferredSize(new Dimension(50, 40));
        descartarAlteracoesButton.setText("Descartar Alterações");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(descartarAlteracoesButton, gbc);
        novoParticipanteButton = new JButton();
        novoParticipanteButton.setMargin(new Insets(0, 0, 0, 0));
        novoParticipanteButton.setMaximumSize(new Dimension(-1, -1));
        novoParticipanteButton.setMinimumSize(new Dimension(50, 40));
        novoParticipanteButton.setPreferredSize(new Dimension(50, 40));
        novoParticipanteButton.setText("Cadastrar Novo Participante");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(novoParticipanteButton, gbc);
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
