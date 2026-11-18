package view;

import controller.*;
import model.Evento;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;


public class MenuPrincipal {
    private Controle sistemaOriginal;
    private JFrame painel;
    private JPanel painelConteudo;
    private JPanel menuVertical;
    private JLabel tituloLabel;
    private JLabel labelMenu;
    private JButton novoEventoButton;
    private JButton fecharProgramaButton;
    public JTable listaEventos;
    private JLabel dicaSelecao;

    public MenuPrincipal() {

        //Criar a janela
        painel = new JFrame("Menu inicial");
        painel.setContentPane(this.painelConteudo);
        painel.pack();
        painel.setLocationRelativeTo(null);
        painel.setVisible(true);

        // Bloqueia o redimensionamento da janela
        painel.setResizable(false);

        //Ao fechar a janela encerrar o programa
        painel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Botão para cadastra um novo evento
        novoEventoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                novoEvento();

            }

        });

        //Botão para fechar programa
        fecharProgramaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Encerrar o sistema
                System.exit(0);
            }
        });

        //Ação de clique do mouse na lista de evento
        listaEventos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Verifica se foi duplo clique
                if (e.getClickCount() == 2) {
                    //salvar a linha selecionada
                    int linhaSelecionada = listaEventos.rowAtPoint(e.getPoint());

                    // Verificar se o usuário clicou em uma linha válida
                    if (linhaSelecionada != -1) {
                        //Obtém o item selecionado
                        String idEvento = (String) listaEventos.getValueAt(linhaSelecionada, 0);
                        Evento eventoSelecionado = sistemaOriginal.getListaEventos().get(Integer.parseInt(idEvento));

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                // Muda para a tela de relatorio de evento com os dados do evento selecionado
                                MenuEvento TelaEvento = new MenuEvento();
                                TelaEvento.exibir(sistemaOriginal, eventoSelecionado);

                                // Fecha a janela atual e libera memória
                                Window janelaAtual = SwingUtilities.windowForComponent(listaEventos);
                                if (janelaAtual != null) {
                                    janelaAtual.dispose();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    // Metodo para exibir a janela
    public void exibir(Controle sistemaTela) {
        //Atribuir os dados do sistema
        sistemaOriginal = sistemaTela;

        //Atualizar a lista de eventos do sistema
        AtualizarListaEvento();

    }

    //Atualiza a lista de evento da janela
    public void AtualizarListaEvento() {
        //Criar os títulos das colunas da tabela
        String[] colunas = {"ID", "Nome Evento", "Quant. Participantes", "Quant. Despesas"};

        //Criar o modelo da tabelar
        DefaultTableModel modeloDadosEvento = new DefaultTableModel(colunas, 0) {
            @Override
            //Bloqueia a linha para edição
            public boolean isCellEditable(int row, int column) {
                // Retorna false para todas as células da tabela
                return false;
            }
        };

        //Verificar se o sistema não estão nulo
        if (sistemaOriginal != null) {
            //Criar uma variável com a lista de eventos do sistema
            var dadosEvento = sistemaOriginal.getListaEventos();

            //Percorre o mapa de despesas e desenha a linha para adicionar a tabela
            for (Evento evento : dadosEvento.values()) {
                //Definir os valores da linha
                String id = String.valueOf(evento.getId());
                String nome = evento.getNome();
                String qParticipante = evento.getListaParticipantes().size() + " Participantes";
                String qDespesas = evento.getListaDespesas().size() + " Despesas";

                //Criar o objeto linha e adicionar ao modelo
                Object[] linha = new Object[]{id, nome, qParticipante, qDespesas};
                modeloDadosEvento.addRow(linha);
            }
        }

        //Adicionar o modelo a tabela da lista de eventos
        listaEventos.setModel(modeloDadosEvento);

        //Criar o modelo de alinhamento ao centro
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);

        //Setar o tamanho máximo da coluna 0 (Id evento)
        listaEventos.getColumnModel().getColumn(0).setMaxWidth(50);

        //Aplicando o alinhamento ao centro das colunas
        listaEventos.getColumnModel().getColumn(0).setCellRenderer(centro);
        listaEventos.getColumnModel().getColumn(1).setCellRenderer(centro);
        listaEventos.getColumnModel().getColumn(2).setCellRenderer(centro);
        listaEventos.getColumnModel().getColumn(3).setCellRenderer(centro);

    }

    //Metodo para criar um novo evento
    public void novoEvento() {
        //Abre uma janela para digitar o nome do novo evento
        String nome = JOptionPane.showInputDialog(
                null,
                "Digite o nome do evento:",
                "Entrada de Dados do Evento",
                JOptionPane.QUESTION_MESSAGE
        );

        //Verifica se o usuário digitou algo
        if (nome != null && !nome.trim().isEmpty()) {
            //Adicionar o evento ao sistema
            Evento EventoAdicionado = sistemaOriginal.addEvento(nome);

            //Muda para a tela de relatorio do novo evento
            MenuEvento TelaEvento = new MenuEvento();
            TelaEvento.exibir(sistemaOriginal, EventoAdicionado);

            //Fecha a janela atual e libera memória
            Window janelaAtual = SwingUtilities.windowForComponent(listaEventos);
            if (janelaAtual != null) {
                janelaAtual.dispose();
            }
        } else {
            //Mostra uma mensagem se nada for digitado
            JOptionPane.showMessageDialog(
                    null,
                    "Nenhum nome foi informado.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE

            );
        }

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
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 5);
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setMaximumSize(new Dimension(-1, -1));
        panel1.setMinimumSize(new Dimension(-1, -1));
        panel1.setOpaque(true);
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
        fecharProgramaButton = new JButton();
        fecharProgramaButton.setLabel("Fechar Programa");
        fecharProgramaButton.setMargin(new Insets(0, 10, 0, 10));
        fecharProgramaButton.setMaximumSize(new Dimension(-1, -1));
        fecharProgramaButton.setMinimumSize(new Dimension(50, 40));
        fecharProgramaButton.setPreferredSize(new Dimension(50, 40));
        fecharProgramaButton.setText("Fechar Programa");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 5, 5);
        panel1.add(fecharProgramaButton, gbc);
        novoEventoButton = new JButton();
        novoEventoButton.setLabel("Cadastrar Novo Evento");
        novoEventoButton.setMargin(new Insets(0, 10, 0, 10));
        novoEventoButton.setMaximumSize(new Dimension(-1, -1));
        novoEventoButton.setMinimumSize(new Dimension(50, 40));
        novoEventoButton.setPreferredSize(new Dimension(50, 40));
        novoEventoButton.setText("Cadastrar Novo Evento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(novoEventoButton, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel2.setMaximumSize(new Dimension(-1, -1));
        panel2.setMinimumSize(new Dimension(500, -1));
        panel2.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 10, 10);
        painelConteudo.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        labelMenu = new JLabel();
        labelMenu.setHorizontalAlignment(0);
        labelMenu.setHorizontalTextPosition(0);
        labelMenu.setText("Lista de Eventos Recentes");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel2.add(labelMenu, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(scrollPane1, gbc);
        listaEventos = new JTable();
        scrollPane1.setViewportView(listaEventos);
        dicaSelecao = new JLabel();
        dicaSelecao.setText("Duplo clique no evento para detalhes");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 10, 0);
        panel2.add(dicaSelecao, gbc);
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
