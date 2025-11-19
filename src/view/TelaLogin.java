package view;

import model.Usuario;
import services.AuthService;
import dao.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * TelaLogin atualizada com suporte:
 *  - BCrypt
 *  - Auditoria futura
 *  - Tentativas de login com bloqueio
 *  - ROLE (ADMIN/USER)
 */
public class TelaLogin extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;

    public TelaLogin() {
        setTitle("Login - Sistema de Gestão");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Painel esquerdo (branding)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(370, getHeight()));
        leftPanel.setBackground(new Color(33, 150, 243));
        JLabel sistemaTitulo = new JLabel("<html><center><h1 style='color:white;'>Sistema de Gestão</h1></center></html>");
        leftPanel.add(sistemaTitulo);
        add(leftPanel, BorderLayout.WEST);

        // Painel direito com formulário
        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        add(rightPanel, BorderLayout.CENTER);

        JLabel lblTituloLogin = new JLabel("Acesse sua conta");
        lblTituloLogin.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTituloLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(lblTituloLogin);
        rightPanel.add(Box.createVerticalStrut(40));

        JLabel lblLogin = new JLabel("Usuário");
        lblLogin.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtLogin = new JTextField();
        txtLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        rightPanel.add(lblLogin);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(txtLogin);
        rightPanel.add(Box.createVerticalStrut(25));

        JLabel lblSenha = new JLabel("Senha");
        lblSenha.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSenha = new JPasswordField();
        txtSenha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        rightPanel.add(lblSenha);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(txtSenha);
        rightPanel.add(Box.createVerticalStrut(35));

        btnEntrar = new JButton("Entrar");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(200, 45));
        btnEntrar.setBackground(new Color(33, 150, 243));
        btnEntrar.setForeground(Color.WHITE);

        rightPanel.add(btnEntrar);
        rightPanel.add(Box.createVerticalStrut(20));

        getRootPane().setDefaultButton(btnEntrar);

        btnEntrar.addActionListener(e -> verificarLogin());
    }

    private void verificarLogin() {

        String loginDigitado = txtLogin.getText().trim();
        String senhaDigitada = new String(txtSenha.getPassword());

        if (loginDigitado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o login.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtLogin.requestFocus();
            return;
        }

        if (senhaDigitada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite a senha.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtSenha.requestFocus();
            return;
        }

        AuthService auth = new AuthService();
        UserDAO userDAO = new UserDAO();

        try {

            // =============================
            // 1️⃣ TENTAR AUTENTICAR
            // =============================
            Usuario u = auth.autenticar(loginDigitado, senhaDigitada);

            if (u != null) {

                // Primeiro acesso → FORÇAR troca de senha
                if (u.isPrimeiroAcesso()) {
                    new TelaTrocaSenha(u.getId(), u.getNomeCompleto(), u.getRole())
                            .setVisible(true);
                } else {
                    new TelaPrincipal(u.getNomeCompleto(), u.getRole())
                            .setVisible(true);
                }

                dispose();
                return;
            }

            // =============================
            // 2️⃣ LOGIN FALHOU
            // =============================
            Usuario u2 = userDAO.findByLogin(loginDigitado);

            if (u2 == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuário ou senha inválidos.",
                        "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (u2.isBloqueado()) {
                JOptionPane.showMessageDialog(this,
                        "Conta BLOQUEADA. Contate o administrador.",
                        "Conta Bloqueada",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Mostrar tentativas restantes
            int tent = u2.getTentativasLogin();
            int rest = Math.max(0, 3 - tent);

            JOptionPane.showMessageDialog(this,
                    "Usuário ou senha inválidos.\nTentativas restantes: " + rest,
                    "Erro de Login",
                    JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar ao banco: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try { com.formdev.flatlaf.FlatLightLaf.setup(); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}
