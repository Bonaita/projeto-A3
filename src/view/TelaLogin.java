package view;

import controller.UsuarioController;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TelaLogin extends JFrame {

    private final UsuarioController controller = new UsuarioController();

    public TelaLogin() {

        setTitle("Login - Sistema de Manutenções");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Fundo (Windows 11)
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(240, 240, 240));

        // Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 30, 30, 30)
        ));
        card.setPreferredSize(new Dimension(330, 300));

        // Layout interno
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        // Título
        JLabel lblTitulo = new JLabel("Acesso ao Sistema", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(0, 120, 215));

        c.gridy = 0;
        card.add(lblTitulo, c);

        // Label usuário
        JLabel lblLogin = new JLabel("Usuário:");
        lblLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLogin.setForeground(Color.BLACK);

        c.gridy = 1;
        card.add(lblLogin, c);

        // Campo usuário
        JTextField txtLogin = new JTextField();
        estilizarCampo(txtLogin);
        txtLogin.setToolTipText("Digite seu usuário");

        c.gridy = 2;
        card.add(txtLogin, c);

        // Label senha
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSenha.setForeground(Color.BLACK);

        c.gridy = 3;
        card.add(lblSenha, c);

        // Campo senha
        JPasswordField txtSenha = new JPasswordField();
        estilizarCampo(txtSenha);
        txtSenha.setToolTipText("Digite sua senha");

        c.gridy = 4;
        card.add(txtSenha, c);

        // Botão Entrar (Windows 11)
        JButton btnEntrar = new JButton("Entrar");
        estilizarBotao(btnEntrar);

        c.gridy = 5;
        card.add(btnEntrar, c);

        // === ENTER PARA LOGAR ===
        KeyAdapter pressionarEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    autenticar(txtLogin, txtSenha);
                }
            }
        };

        txtLogin.addKeyListener(pressionarEnter);
        txtSenha.addKeyListener(pressionarEnter);

        btnEntrar.addActionListener(e -> autenticar(txtLogin, txtSenha));

        // Adiciona tudo ao fundo
        background.add(card);

        setContentPane(background);
    }

    // === Estilo dos campos ===
    private void estilizarCampo(JTextField campo) {
        campo.setBackground(new Color(245, 245, 245));
        campo.setForeground(Color.BLACK);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    // === Botão moderno estilo Windows 11 ===
    private void estilizarBotao(JButton botao) {

        botao.setText("Entrar");
        botao.setFocusPainted(false);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(0, 40));

        // UI personalizada
        botao.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fundo do botão
                g2.setColor(new Color(0, 120, 215)); // Azul Microsoft
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);

                // Texto
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(botao.getText());
                int x = (c.getWidth() - textWidth) / 2;
                int y = (c.getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(botao.getText(), x, y);

                g2.dispose();
            }
        });

        // Hover
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(20, 140, 235));
                botao.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(new Color(0, 120, 215));
                botao.repaint();
            }
        });
    }

    // === Login ===
    private void autenticar(JTextField txtLogin, JPasswordField txtSenha) {

        if (txtLogin.getText().trim().isEmpty() || txtSenha.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario user = controller.login(
                txtLogin.getText().trim(),
                new String(txtSenha.getPassword())
        );

        if (user != null) {

            // ====== PRIMEIRO ACESSO (USANDO A COLUNA primeIro_acesso) ======
            if (user.getPrimeiroAcesso() == 1) {
                JOptionPane.showMessageDialog(this,
                        "Este é seu primeiro acesso. Defina uma nova senha.",
                        "Primeiro acesso",
                        JOptionPane.INFORMATION_MESSAGE);

                new TelaTrocaSenha(user).setVisible(true);
                dispose();
                return;
            }

            // ====== ACESSO NORMAL ======
            new TelaPrincipal(user).setVisible(true);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
