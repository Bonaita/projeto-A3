package view;

import model.Usuario;
import security.Permission;
import security.SecurityService;

import javax.swing.*;

/**
 * JFrame base que verifica permissão no construtor.
 * Use estendendo esta classe para telas restritas (ex: TelaAuditoria, TelaAdminUsuarios).
 */
public abstract class GuardedFrame extends JFrame {

    protected final Usuario usuario;
    private final SecurityService security = new SecurityService();

    /**
     * @param usuario usuário logado
     * @param requiredPerm permissão necessária para abrir a tela (null = qualquer usuário)
     */
    public GuardedFrame(Usuario usuario, Permission requiredPerm) {
        this.usuario = usuario;

        if (requiredPerm != null) {
            try {
                security.requirePermission(usuario.getRole(), requiredPerm);
            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(null,
                        "Acesso negado. Você não possui permissão para acessar esta área.",
                        "Acesso negado",
                        JOptionPane.WARNING_MESSAGE);
                // garante que a tela não será exibida
                dispose();
                return;
            }
        }
    }
}
