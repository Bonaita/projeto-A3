
import com.formdev.flatlaf.FlatLightLaf;
import view.TelaLogin;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            FlatLightLaf.setup(); // tema dark moderno
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);
            UIManager.put("TextComponent.arc", 20);
        } catch (Exception e) {
            System.out.println("Erro ao iniciar FlatLaf: " + e.getMessage());
        }

        // ===== INICIA O SISTEMA =====
        SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
    }
}
