import com.formdev.flatlaf.FlatLightLaf;
import view.TelaLogin;

import javax.swing.*;



public class Main {

    public static void main(String[] args) {
        // -----------------------------------------------------------
        // 1️⃣ Aplica o Look and Feel moderno FlatLaf (tema claro)
        //    Caso queira o tema escuro, troque para FlatDarkLaf.
        // -----------------------------------------------------------
        try {
            FlatLightLaf.setup();  // Aplica o FlatLaf Light
            // FlatDarkLaf.setup();  // (opção) Tema escuro
        } catch (Exception ex) {
            System.out.println("Não foi possível carregar o FlatLaf.");
        }

        // -----------------------------------------------------------
        // 2️⃣ Abre a TelaLogin como primeira tela do sistema
        // -----------------------------------------------------------
        SwingUtilities.invokeLater(() -> {
            TelaLogin login = new TelaLogin();
            login.setVisible(true);
        });
    }
}
