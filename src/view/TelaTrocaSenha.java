package view;

import controller.UsuarioController;
import model.Usuario;
import services.AuthService;

import javax.swing.*;
import java.awt.*;

public class TelaTrocaSenha extends JFrame {

    private final Usuario usuarioLogado;
    private final AuthService authService = new AuthService();
    private final UsuarioController controller = new UsuarioController();

    public TelaTrocaSenha(Usuario usuario) {
        this.usuarioLogado = usuario;

        setTitle("Alterar Senha");
        setSize(420, 310);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== ROOT =====
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // ===== CABEÇALHO =====
        JLabel titulo = new JLabel("Alterar Senha");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel subtitulo = new JLabel("Mantenha sua conta protegida");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subtitulo);

        root.add(header, BorderLayout.NORTH);

        // ===== CARD =====
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblAtual = new JLabel("Senha atual:");
        JLabel lblNova = new JLabel("Nova senha:");
        JLabel lblConfirmar = new JLabel("Confirmar nova senha:");

        lblAtual.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNova.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPasswordField txtAtual = criarCampoSenha();
        JPasswordField txtNova = criarCampoSenha();
        JPasswordField txtConfirmar = criarCampoSenha();

        // ==== Layout do Card ====
        c.gridx = 0; c.gridy = 0; card.add(lblAtual, c);
        c.gridy = 1; card.add(txtAtual, c);

        c.gridy = 2; card.add(lblNova, c);
        c.gridy = 3; card.add(txtNova, c);

        c.gridy = 4; card.add(lblConfirmar, c);
        c.gridy = 5; card.add(txtConfirmar, c);

        root.add(card, BorderLayout.CENTER);

        // ===== BOTÕES =====
        JButton btnSalvar = criarBotaoAzul("Salvar");
        JButton btnFechar = criarBotaoCinza("Cancelar");

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botoes.setOpaque(false);
        botoes.add(btnSalvar);
        botoes.add(btnFechar);

        root.add(botoes, BorderLayout.SOUTH);

        // ===== AÇÕES =====
        btnSalvar.addActionListener(e -> {
            String atual = new String(txtAtual.getPassword());
            String nova = new String(txtNova.getPassword());
            String confirmar = new String(txtConfirmar.getPassword());

            // Validações
            if (atual.isBlank() || nova.isBlank() || confirmar.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Preencha todos os campos.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!nova.equals(confirmar)) {
                JOptionPane.showMessageDialog(this,
                        "As senhas informadas não conferem.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Verificar senha atual
                if (!authService.verificarSenha(atual, usuarioLogado.getSenhaHash())) {
                    JOptionPane.showMessageDialog(this,
                            "Senha atual incorreta.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Atualizar senha no banco
                usuarioLogado.setSenhaHash(authService.gerarHash(nova));
                controller.atualizarSenha(usuarioLogado);

                // *** CORREÇÃO IMPORTANTE ***
                controller.atualizarSenha(usuarioLogado);

                JOptionPane.showMessageDialog(this,
                        "Senha atualizada com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao trocar senha: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnFechar.addActionListener(e -> dispose());
    }


    // ===== COMPONENTES ESTILIZADOS =====
    private JPasswordField criarCampoSenha() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(new Color(245, 245, 245));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return campo;
    }

    private JButton criarBotaoAzul(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(0, 120, 215));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(25, 140, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 120, 215));
            }
        });

        return btn;
    }

    private JButton criarBotaoCinza(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(230, 230, 230));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
}
