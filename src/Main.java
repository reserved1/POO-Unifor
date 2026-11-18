import controller.*;
import view.MenuPrincipal;

import javax.swing.*;

void main() {

    //Criar o sistema que guarda os eventos
    Controle sistema = new Controle();

    // Exibir a tela inicial do programa com os dados do sistema
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            MenuPrincipal Tela = new MenuPrincipal();
            Tela.exibir(sistema);

        }
    });

}
