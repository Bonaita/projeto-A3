package view;

import dao.UsuarioDAO;
import model.Usuario;
import security.Permission;
import security.SecurityService;
import services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TelaAdminUsuarios extends JFrame {

    private final Usuario usuario;
    private JTable tabela;
    private DefaultTableModel model;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuthService authService = new AuthService();
    private final SecurityService security = new SecurityService();

    public TelaAdminUsuarios(Usuario usuario) {
        this.usuario = usuario;

        // ======= RBAC - SOMENTE ADMIN =======
        if (!security.roleHasPermission(usuario.getRole(), Permission.MANAGE_USERS)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Você não tem permissão para acessar Administração de Usuários.",
                    "Acesso Negado",
                    JOptionPane.WARNING_MESSAGE
            );
            dispose();
            return;
        }

        setTitle("Administração de Usuários");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ROOT
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // ===== CABEÇALHO =====
        JLabel titulo = new JLabel("Administração de Usuários");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel subtitulo = new JLabel("Gerencie contas, redefina senhas e controle acessos");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitulo);

        root.add(header, BorderLayout.NORTH);

        // ===== TABELA =====
        model = new DefaultTableModel(new Object[]{"ID", "Login", "Nome", "Bloqueado"}, 0);

        tabela = new JTable(model);
        tabela.setRowHeight(28);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setSelectionBackground(new Color(0, 120, 215));
        tabela.setSelectionForeground(Color.WHITE);

        JTableHeader headerTb = tabela.getTableHeader();
        headerTb.setBackground(new Color(230, 230, 230));
        headerTb.setForeground(new Color(50, 50, 50));
        headerTb.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        root.add(scroll, BorderLayout.CENTER);

        // ===== BOTÕES INFERIORES =====
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botoes.setOpaque(false);

        JButton btnNovo = criarBotao("Novo Usuário");
        JButton btnAtualizar = criarBotao("Atualizar Lista");
        JButton btnResetSenha = criarBotao("Resetar Senha");

        botoes.add(btnNovo);
        botoes.add(btnAtualizar);
        botoes.add(btnResetSenha);

        root.add(botoes, BorderLayout.SOUTH);

        // ===== AÇÕES =====
        btnNovo.addActionListener(e -> cadastrarUsuario());
        btnAtualizar.addActionListener(e -> carregarUsuarios());
        btnResetSenha.addActionListener(e -> resetarSenha());

        carregarUsuarios();
    }

    // ========= BOTÃO WINDOWS 11 ==========
    private JButton criarBotao(String texto) {
        JButton btn = new JButton(texto);

        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 120, 215));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    // ========= LÓGICA ==========
    private void carregarUsuarios() {
        model.setRowCount(0);

        try {
            List<Usuario> lista = usuarioDAO.listarTodos();

            for (Usuario u : lista) {
                model.addRow(new Object[]{
                        u.getId(),
                        u.getLogin(),
                        u.getNomeCompleto(),
                        u.isBloqueado() ? "Sim" : "Não"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage());
        }
    }

    private void cadastrarUsuario() {
        JTextField txtLogin = new JTextField();
        JTextField txtNome = new JTextField();
        JPasswordField txtSenha = new JPasswordField();

        Object[] campos = {
                "Login:", txtLogin,
                "Nome completo:", txtNome,
                "Senha inicial:", txtSenha
        };

        int op = JOptionPane.showConfirmDialog(
                this, campos, "Novo Usuário", JOptionPane.OK_CANCEL_OPTION
        );

        if (op == JOptionPane.OK_OPTION) {
            try {
                Usuario novo = new Usuario();
                novo.setLogin(txtLogin.getText());
                novo.setNomeCompleto(txtNome.getText());
                novo.setSenhaHash(authService.gerarHash(new String(txtSenha.getPassword())));
                novo.setRole("USER");

                usuarioDAO.inserirUsuario(novo);

                JOptionPane.showMessageDialog(this, "Usuário criado com sucesso!");
                carregarUsuarios();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao criar usuário: " + e.getMessage());
            }
        }
    }

    private void resetarSenha() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }

        int id = (int) tabela.getValueAt(row, 0);
        String novaSenha = JOptionPane.showInputDialog(this, "Nova senha:");

        if (novaSenha == null || novaSenha.isBlank()) return;

        try {
            String hash = authService.gerarHash(novaSenha);
            usuarioDAO.resetarSenha(id, hash);

            JOptionPane.showMessageDialog(this, "Senha redefinida com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao redefinir senha: " + e.getMessage());
        }
    }
}
