package view;

import model.Usuario;
import view.components.ShadowCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;

/**
 * TelaPrincipal - versão final com:
 * - Menu Conta (Perfil, Trocar Senha, Sair) para todos
 * - Menu Administração (apenas para ADMIN)
 * - Reorganização automática: 3 cards para ADMIN, 2 cards centralizados para USER
 * - Ícones SVG via FlatSVGIcon quando disponível, com fallback
 */
public class TelaPrincipal extends JFrame {

    private Usuario usuario;

    // === CONSTRUTOR COM USUÁRIO ===
    public TelaPrincipal(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Painel Principal - Bem-vindo" + (usuario != null && usuario.getNomeCompleto() != null ? ", " + usuario.getNomeCompleto() : ""));
        initLookAndFeel();
        initUI(usuario != null && usuario.getNomeCompleto() != null ? "Bem-vindo, " + usuario.getNomeCompleto() : "Bem-vindo ao sistema");

        // aplicar o menu *depois* de setContentPane (evita sumir com alguns LAFs)
        setJMenuBar(criarMenuSuperior());

        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // === CONSTRUTOR PADRÃO ===
    public TelaPrincipal() {
        this(null);
    }

    // Look & Feel (tente FlatLaf)
    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
        } catch (Exception ignored) {}
    }

    /**
     * Cria e retorna o JMenuBar. Adiciona menu Administração apenas quando usuario.isAdmin() == true.
     */
    private JMenuBar criarMenuSuperior() {

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(245, 245, 245));

        // ===== MENU CONTA (visível para todos) =====
        JMenu menuConta = new JMenu("Conta");
        menuConta.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JMenuItem itemPerfil = new JMenuItem("Perfil do Usuário");
        JMenuItem itemTrocarSenha = new JMenuItem("Trocar Senha");
        JMenuItem itemLogout = new JMenuItem("Sair");

        itemPerfil.addActionListener(e -> {
            // abre tela de perfil (presumindo que existe)
            try {
                new TelaPerfilUsuario(usuario).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tela de perfil não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        itemTrocarSenha.addActionListener(e -> {
            try {
                new TelaTrocaSenha(usuario).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tela de troca de senha não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        itemLogout.addActionListener(e -> {
            // logoff: fecha painel e volta para login
            new TelaLogin().setVisible(true);
            dispose();
        });

        menuConta.add(itemPerfil);
        menuConta.add(itemTrocarSenha);
        menuConta.addSeparator();
        menuConta.add(itemLogout);

        menuBar.add(menuConta);

        // ===== MENU ADMIN (apenas admins) =====
        if (usuario != null && usuario.isAdmin()) {
            JMenu adminMenu = new JMenu("Administração");
            adminMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JMenuItem itemUsuarios = new JMenuItem("Gerenciar Usuários");
            JMenuItem itemLogs = new JMenuItem("Logs do Sistema");
            JMenuItem itemPermissoes = new JMenuItem("Permissões");

            itemUsuarios.addActionListener(e -> {
                try {
                    new TelaAdminUsuarios(usuario).setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Tela de usuários não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            itemLogs.addActionListener(e -> {
                new TelaAuditoria(usuario).setVisible(true);
            });

            itemPermissoes.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Gerenciar permissões (implementar).");
            });

            adminMenu.add(itemUsuarios);
            adminMenu.add(itemLogs);
            adminMenu.add(itemPermissoes);

            menuBar.add(adminMenu);
        }

        return menuBar;
    }

    /**
     * Inicializa a UI principal (conteúdo). Recebe o texto do subtítulo.
     * Este método chama setContentPane(root) internamente.
     */
    private void initUI(String subtitleText) {

        // ROOT
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(242, 242, 242));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // TOP BAR (título + subtítulo)
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("Painel Principal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(0, 120, 215));

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(80, 80, 80));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createRigidArea(new Dimension(0, 5)));
        titleBox.add(subtitle);

        top.add(titleBox, BorderLayout.WEST);
        root.add(top, BorderLayout.NORTH);

        // CENTER: painel com cards. Vamos decidir colunas dependendo do role.
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        // carregue ícones (com fallback)
        JComponent iconeMaquinas = loadIconSafe("view/icons/maquinas.svg", 80, 80);
        JComponent iconeManutencoes = loadIconSafe("view/icons/manutencoes.svg", 80, 80);
        JComponent iconeUsuarios = loadIconSafe("view/icons/users.svg", 80, 80);

        // cria cards
        ShadowCard card1 = new ShadowCard("Máquinas", "Gerenciar todas as máquinas", iconeMaquinas);
        ShadowCard card2 = new ShadowCard("Manutenções", "Gerenciar serviços e registros", iconeManutencoes);
        ShadowCard card3 = new ShadowCard("Usuários", "Controle administrativo", iconeUsuarios);

        // eventos (passando usuario)
        card1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new TelaGestaoMaquinas(usuario).setVisible(true);
            }
        });

        card2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new TelaGestaoManutencoes(usuario).setVisible(true);
            }
        });

        card3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // somente admins devem acessar; caso um USER consiga clicar, verificamos novamente:
                if (usuario != null && usuario.isAdmin()) {
                    new TelaAdminUsuarios(usuario).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(TelaPrincipal.this, "Acesso negado. Você não tem permissão para acessar esta área.", "Acesso negado", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // decide layout: ADMIN -> 3 colunas; USER -> 2 colunas centralizados
        boolean isAdmin = usuario != null && usuario.isAdmin();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        if (isAdmin) {
            // 3 colunas (0,1,2)
            gbc.gridx = 0;
            center.add(card1, gbc);
            gbc.gridx = 1;
            center.add(card2, gbc);
            gbc.gridx = 2;
            center.add(card3, gbc);
        } else {
            // USER: apenas 2 cards, centralizados. Não adicionamos card3.
            // Para centralizar usamos um panel interno com GridBagLayout e 3 colunas vazio-card-card-vazio
            JPanel inner = new JPanel(new GridBagLayout());
            inner.setOpaque(false);

            GridBagConstraints ig = new GridBagConstraints();
            ig.insets = new Insets(20, 20, 20, 20);
            ig.fill = GridBagConstraints.BOTH;
            ig.weightx = 1;
            ig.weighty = 1;

            // coluna 0: filler
            ig.gridx = 0;
            inner.add(Box.createHorizontalStrut(20), ig);

            // coluna 1: card1
            ig.gridx = 1;
            inner.add(card1, ig);

            // coluna 2: card2
            ig.gridx = 2;
            inner.add(card2, ig);

            // coluna 3: filler
            ig.gridx = 3;
            inner.add(Box.createHorizontalStrut(20), ig);

            // adiciona inner ao center, ajustando constraints para centralização
            gbc.gridx = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            center.add(inner, gbc);
        }

        root.add(center, BorderLayout.CENTER);

        // FOOTER
        JLabel footer = new JLabel("© " + java.time.Year.now().getValue() + " - Sistema de Gestão");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footer.setForeground(new Color(110, 110, 110));
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.setOpaque(false);
        foot.add(footer);
        root.add(foot, BorderLayout.SOUTH);

        // finalmente aplica o content pane
        setContentPane(root);
    }

    /**
     * Tenta carregar SVG via FlatSVGIcon (se disponível no classpath) ou um PNG via ImageIO.
     * Se tudo falhar, retorna JLabel com emoji como fallback.
     *
     * Para robustez, recomendamos colocar os SVGs em src/view/icons/ e usar nomes sem acento:
     * view/icons/maquinas.svg, view/icons/manutencoes.svg, view/icons/users.svg
     */
    private JComponent loadIconSafe(String resourcePath, int w, int h) {
        // 1) tenta FlatSVGIcon (se o flatlaf-extras estiver no classpath)
        try {
            Class<?> svgClass = Class.forName("com.formdev.flatlaf.extras.FlatSVGIcon");
            java.lang.reflect.Constructor<?> ctor = svgClass.getConstructor(String.class, int.class, int.class);
            Object svgIcon = ctor.newInstance(resourcePath, w, h);
            return new JLabel((Icon) svgIcon);
        } catch (Throwable ignored) {
        }

        // 2) tenta carregar PNG/JPG correspondente (mesmo nome)
        try {
            String altPath = "/" + resourcePath;
            InputStream is = getClass().getResourceAsStream(altPath);
            if (is != null) {
                Image img = javax.imageio.ImageIO.read(is);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {
        }

        // 3) fallback: emoji pequeno
        JLabel fallback = new JLabel("\uD83D\uDEE0"); // ícone de ferramenta
        fallback.setFont(fallback.getFont().deriveFont((float) w - 20));
        fallback.setHorizontalAlignment(SwingConstants.CENTER);
        return fallback;
    }

    // Para quick test
    public static void main(String[] args) {
        // exemplo de teste
        Usuario adminEx = new Usuario();
        adminEx.setNomeCompleto("Administrador");
        adminEx.setRole("ADMIN");

        SwingUtilities.invokeLater(() -> new TelaPrincipal(adminEx).setVisible(true));
    }
}
