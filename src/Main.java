import telas.TelaLogin;

import javax.swing.*;



public class Main {

    public static void main(String[] args) {
        // Isso garante que a interface gráfica rode na "thread" correta
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 1. Cria a tela de login
                TelaLogin telaDeLogin = new TelaLogin();
                // 2. Torna a tela visível
                telaDeLogin.setVisible(true);
            }
        });
    }
}