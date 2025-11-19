package view;

import conexao.ConexaoMySQL;
import services.AuditoriaService;
import services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TelaTrocaSenha com auditoria, BCrypt e suporte a ROLE (ADMIN / USER)
 */
public class TelaTrocaSenha extends JFrame {

    private int idUsuario;               // Usado no primeiro acesso
    private String usuarioLogado;        // Nome do usuário
    private boolean primeiroAcesso;      // Define comportamento ao salvar
    private String role;                 // ADMIN ou USER

    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmarSenha;

    // ===============================================================
    // ✔ Construtor 1: Primeiro Acesso
    // ===============================================================
    public TelaTrocaSenha(int idUsuario, String usuarioLogado, String role) {
        this.idUsuario = idUsuario;
        this.usuarioLogado = usuarioLogado;
        this.role = role;
        this.primeiroAcesso = true;
        initUI();
    }

    // ===============================================================
    // ✔ Construtor 2: Troca normal (vem da TelaPrincipal)
    // ===============================================================
    public TelaTrocaSenha(String usuarioLogado, String role) {
        this.usuarioLogado = usuarioLogado;
        this.role = role;
        this.primeiroAcesso = false;
        initUI();
    }

    // ===============================================================
    // 🔧 Interface Moderna
    // ===============================================================
    private void initUI() {

        setTitle("Trocar Senha");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // ------------------------------------------------------------
        // 🔷 TopBar
        // ------------------------------------------------------------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("Trocar Senha");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(titulo, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnVoltar = new JButton("Voltar");
        JButton btnPrincipal = new JButton("Tela Principal");
        JButton btnLogout = new JButton("Logout");
        JButton btnSair = new JButton("Sair");

        if (!primeiroAcesso) {
            topButtons.add(btnVoltar);
            topButtons.add(btnPrincipal);
        }

        topButtons.add(btnLogout);
        topButtons.add(btnSair);

        topBar.add(topButtons, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ------------------------------------------------------------
        // Formulário
        // ------------------------------------------------------------
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nova senha
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Nova Senha:"), gbc);

        gbc.gridx = 1;
        txtNovaSenha = new JPasswordField();
        txtNovaSenha.setPreferredSize(new Dimension(250, 30));
        mainPanel.add(txtNovaSenha, gbc);

        // Confirmar senha
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Confirmar Senha:"), gbc);

        gbc.gridx = 1;
        txtConfirmarSenha = new JPasswordField();
        txtConfirmarSenha.setPreferredSize(new Dimension(250, 30));
        mainPanel.add(txtConfirmarSenha, gbc);

        // Botão salvar
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnSalvar = new JButton("Salvar Senha");
        btnSalvar.setPreferredSize(new Dimension(180, 40));
        mainPanel.add(btnSalvar, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ------------------------------------------------------------
        // AÇÕES
        // ------------------------------------------------------------
        btnSalvar.addActionListener(e -> trocarSenha());

        btnVoltar.addActionListener(e -> {
            new TelaPrincipal(usuarioLogado, role).setVisible(true);
            dispose();
        });

        btnPrincipal.addActionListener(e -> {
            new TelaPrincipal(usuarioLogado, role).setVisible(true);
            dispose();
        });

        btnLogout.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });

        btnSair.addActionListener(e -> System.exit(0));
    }

    // ===============================================================
    // 🔐 Lógica da troca de senha
    // ===============================================================
    private void trocarSenha() {

        String nova = new String(txtNovaSenha.getPassword()).trim();
        String confirmar = new String(txtConfirmarSenha.getPassword()).trim();

        if (nova.isEmpty() || confirmar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nova.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE usuarios SET senha = ?, primeiro_acesso = FALSE WHERE id_usuario = ?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idReal = primeiroAcesso
                    ? idUsuario
                    : buscarIdUsuario(usuarioLogado);

            ps.setInt(2, idReal);

            // Hash BCrypt
            AuthService auth = new AuthService();
            String hash = auth.gerarHash(nova);

            ps.setString(1, hash);
            ps.executeUpdate();

            // Auditoria
            new AuditoriaService().registrar(
                    idReal,
                    "PASSWORD_CHANGE",
                    "Senha alterada pelo usuário: " + usuarioLogado
            );

            JOptionPane.showMessageDialog(this,
                    "Senha alterada com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

            if (primeiroAcesso) {
                new TelaLogin().setVisible(true);
            } else {
                new TelaPrincipal(usuarioLogado, role).setVisible(true);
            }

            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao alterar senha: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===============================================================
    // 🔧 Obtém ID pelo nome do usuário
    // ===============================================================
    private int buscarIdUsuario(String nome) throws SQLException {

        String sql = "SELECT id_usuario FROM usuarios WHERE nome_completo = ? LIMIT 1";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_usuario");

            throw new SQLException("Usuário não encontrado.");
        }
    }
}
