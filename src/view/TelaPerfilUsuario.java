package view;

import controller.UsuarioController;
import model.Usuario;
import security.Permission;
import security.SecurityService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TelaPerfilUsuario extends JFrame {

    private final Usuario usuario;
    private final UsuarioController controller = new UsuarioController();
    private final SecurityService security = new SecurityService();

    // Componentes
    private JTextField txtNome;
    private JTextField txtLogin;
    private JComboBox<String> cbRole;
    private JCheckBox chkBloqueado;
    private JLabel lblUltimoLogin;
    private JLabel lblTentativas;

    public TelaPerfilUsuario(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Meu Perfil");
        setSize(480, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // === ROOT ===
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // === TÍTULO ===
        JLabel titulo = new JLabel("Meu Perfil");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel subtitulo = new JLabel("Gerencie suas informações pessoais");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subtitulo);

        root.add(header, BorderLayout.NORTH);

        // === CARD CENTRAL ===
        JPanel card = new JPanel(new GridLayout(0, 2, 10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Campos estilizados
        txtNome = criarCampo(usuario.getNomeCompleto());
        txtLogin = criarCampo(usuario.getLogin());

        cbRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        cbRole.setSelectedItem(usuario.getRole());
        estilizarCombo(cbRole);

        chkBloqueado = new JCheckBox("Bloqueado");
        chkBloqueado.setSelected(usuario.isBloqueado());
        chkBloqueado.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblTentativas = new JLabel(String.valueOf(usuario.getTentativasLogin()));
        lblTentativas.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblUltimoLogin = new JLabel(
                usuario.getUltimoLogin() != null ?
                        usuario.getUltimoLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                        "Nunca logou"
        );
        lblUltimoLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Montagem do card
        card.add(new JLabel("Nome Completo:"));
        card.add(txtNome);

        card.add(new JLabel("Login:"));
        card.add(txtLogin);

        card.add(new JLabel("Perfil (Role):"));
        card.add(cbRole);

        card.add(new JLabel("Situação:"));
        card.add(chkBloqueado);

        card.add(new JLabel("Tentativas de Login:"));
        card.add(lblTentativas);

        card.add(new JLabel("Último Login:"));
        card.add(lblUltimoLogin);

        root.add(card, BorderLayout.CENTER);

        // === BOTÕES ===
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        botoes.setOpaque(false);

        JButton btnSalvar = criarBotao("Salvar");
        JButton btnAlterarSenha = criarBotao("Alterar Senha");
        JButton btnFechar = criarBotaoCinza("Fechar");

        botoes.add(btnSalvar);
        botoes.add(btnAlterarSenha);
        botoes.add(btnFechar);

        root.add(botoes, BorderLayout.SOUTH);

        // === RBAC ===
        boolean isAdmin = security.roleHasPermission(usuario.getRole(), Permission.MANAGE_USERS);
        cbRole.setEnabled(isAdmin);
        chkBloqueado.setEnabled(isAdmin);

        // === AÇÕES ===
        btnSalvar.addActionListener(e -> salvar());
        btnAlterarSenha.addActionListener(e -> new TelaTrocaSenha(usuario).setVisible(true));
        btnFechar.addActionListener(e -> dispose());
    }

    // ========= CAMPOS ==========
    private JTextField criarCampo(String texto) {
        JTextField campo = new JTextField(texto);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(new Color(245, 245, 245));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return campo;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(245, 245, 245));
        combo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    }

    // ========= BOTÕES ==========
    private JButton criarBotao(String texto) {
        JButton btn = new JButton(texto);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 120, 215));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(20, 140, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 120, 215));
            }
        });

        return btn;
    }

    private JButton criarBotaoCinza(String texto) {
        JButton btn = new JButton(texto);
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(230, 230, 230));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(210, 210, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(230, 230, 230));
            }
        });

        return btn;
    }

    // ========= SALVAR ==========
    private void salvar() {
        try {
            usuario.setNomeCompleto(txtNome.getText());
            usuario.setLogin(txtLogin.getText());

            boolean isAdmin = security.roleHasPermission(usuario.getRole(), Permission.MANAGE_USERS);

            if (isAdmin) {
                usuario.setRole(cbRole.getSelectedItem().toString());
                usuario.setBloqueado(chkBloqueado.isSelected());
            }

            controller.atualizarDadosBasicos(usuario);

            JOptionPane.showMessageDialog(
                    this,
                    "Dados atualizados com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao salvar: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
