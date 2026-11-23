import controller.*;
import view.MenuPrincipal;

import javax.swing.*;

void main() {

    //Definir a persistência pelo contrato (interface) implementado pela classe de arquivo
    Persistencia persistencia = new GerenciadorArquivo();

    //Carregar os dados salvo em um sistema
    Controle sistema = carregarSistema(persistencia);
    sistema.setPersistencia(persistencia);

    // Exibir a tela inicial do programa com os dados do sistema
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            MenuPrincipal Tela = new MenuPrincipal();
            Tela.exibir(sistema);

        }
    });

}

//Metodo para carregar o sistema tratando arquivo ausente ou inválido
Controle carregarSistema(Persistencia persistencia) {
    try {
        return persistencia.carregar();
    } catch (PersistenciaException e) {
        //Arquivo ausente: informa e inicia um planejamento vazio
        if (e.isArquivoAusente()) {
            JOptionPane.showMessageDialog(null, e.getMessage() + "\nUm planejamento vazio será iniciado.", "Arquivo não encontrado", JOptionPane.INFORMATION_MESSAGE);
            return new Controle();
        }

        //Arquivo inválido: informa e pergunta se deseja iniciar um planejamento vazio
        int opcao = JOptionPane.showConfirmDialog(null,
                e.getMessage() + "\n\nDeseja iniciar um planejamento vazio?\n(O arquivo inválido será substituído quando o sistema for salvo.)",
                "Erro ao carregar os dados", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        return new Controle();
    }
}
