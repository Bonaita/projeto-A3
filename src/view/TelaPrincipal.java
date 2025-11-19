package view;

import conexao.ConexaoMySQL;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TelaPrincipal extends JFrame {

    private final String usuarioLogado;
    private final String role;

    private JLabel lblTotalMaquinas;
    private JLabel lblTotalManutencoes;
    private JLabel lblManutencoesAgendadas;
    private JLabel lblManutencoesConcluidas;

    private JPanel chartPiePanelContainer;
    private JPanel chartBarPanelContainer;

    private JTable tabelaUltimas;
    private DefaultTableModel ultimasModel;

    /** CONSTRUTOR CORRETO */
    public TelaPrincipal(String usuarioLogado, String role) {
        this.usuarioLogado = usuarioLogado;
        this.role = role;

        setTitle("Sistema de Gestão - Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        initTopBar();
        initSidebar();
        initMainDashboard();

        refreshDashboardData();
    }

    // =====================================================================================
    // 1. TOP BAR
    // =====================================================================================
    private void initTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel titulo = new JLabel("Painel Administrativo");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topBar.add(titulo, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel lblUser = new JLabel("Usuário: " + usuarioLogado);

        JButton btnLogout = new JButton("Logout");
        JButton btnSair = new JButton("Sair");

        btnLogout.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });

        btnSair.addActionListener(e -> System.exit(0));

        right.add(lblUser);
        right.add(btnLogout);
        right.add(btnSair);

        topBar.add(right, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }

    // =====================================================================================
    // 2. SIDEBAR
    // =====================================================================================
    private void initSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));

        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(12));

        JButton btnDashboard = createMenuButton("Dashboard");
        JButton btnMaquinas = createMenuButton("Gestão de Máquinas");
        JButton btnManutencoes = createMenuButton("Gestão de Manutenções");
        JButton btnTrocarSenha = createMenuButton("Trocar Senha");

        sidebar.add(btnDashboard);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnMaquinas);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnManutencoes);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnTrocarSenha);
        sidebar.add(Box.createVerticalStrut(8));

        // AUDITORIA — somente ADMIN
        if ("ADMIN".equalsIgnoreCase(role)) {
            JButton btnAuditoria = createMenuButton("Auditoria do Sistema");
            btnAuditoria.addActionListener(e -> new TelaAuditoria(usuarioLogado, role).setVisible(true));
            sidebar.add(btnAuditoria);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        // AÇÕES (corrigidas)
        btnMaquinas.addActionListener(e -> {
            new TelaGestaoMaquinas(usuarioLogado, role).setVisible(true);
            dispose();
        });

        btnManutencoes.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Abra Gestão de Máquinas e selecione uma máquina para ver manutenções."));

        btnTrocarSenha.addActionListener(e -> {
            new TelaTrocaSenha(usuarioLogado, role).setVisible(true);
            dispose();
        });

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createMenuButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return b;
    }

    // =====================================================================================
    // 3. MAIN DASHBOARD
    // =====================================================================================
    private void initMainDashboard() {

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 12));

        lblTotalMaquinas = createCard("Máquinas Cadastradas", "0", new Color(33,150,243));
        lblTotalManutencoes = createCard("Manutenções Totais", "0", new Color(255,152,0));
        lblManutencoesAgendadas = createCard("Agendadas", "0", new Color(76,175,80));
        lblManutencoesConcluidas = createCard("Concluídas", "0", new Color(156,39,176));

        cardsPanel.add(wrapCard(lblTotalMaquinas));
        cardsPanel.add(wrapCard(lblTotalManutencoes));
        cardsPanel.add(wrapCard(lblManutencoesAgendadas));
        cardsPanel.add(wrapCard(lblManutencoesConcluidas));

        main.add(cardsPanel, BorderLayout.NORTH);

        // Gráficos
        JPanel charts = new JPanel(new GridLayout(1, 2, 12, 12));

        chartPiePanelContainer = new JPanel(new BorderLayout());
        chartPiePanelContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        chartBarPanelContainer = new JPanel(new BorderLayout());
        chartBarPanelContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        charts.add(chartPiePanelContainer);
        charts.add(chartBarPanelContainer);

        main.add(charts, BorderLayout.CENTER);

        // Últimas manutenções
        ultimasModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Tipo", "Status", "Obs"}, 0
        );
        tabelaUltimas = new JTable(ultimasModel);

        JScrollPane sp = new JScrollPane(tabelaUltimas);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Últimas 5 Manutenções"));
        bottom.add(sp, BorderLayout.CENTER);

        main.add(bottom, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
    }

    private JLabel createCard(String title, String value, Color color) {
        JLabel label = new JLabel("<html><center>" +
                "<div style='font-size:12px;color:#444'>" + title + "</div>" +
                "<div style='font-size:28px;color:" + toHex(color) + "'><b>" + value + "</b></div>" +
                "</center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel wrapCard(JLabel label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // =====================================================================================
    // 4. CARREGAMENTO ASSÍNCRONO
    // =====================================================================================
    private void refreshDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            int totalMaquinas;
            int totalManut;
            int agendadas;
            int concluidas;

            Map<String, Integer> porTipo = new HashMap<>();
            Map<String, Integer> porStatus = new HashMap<>();

            @Override
            protected Void doInBackground() {

                try (Connection c = ConexaoMySQL.getConexao()) {

                    totalMaquinas = getInt(c, "SELECT COUNT(*) FROM maquinas");
                    totalManut = getInt(c, "SELECT COUNT(*) FROM manutencoes");
                    agendadas = getInt(c, "SELECT COUNT(*) FROM manutencoes WHERE status = 'Agendada'");
                    concluidas = getInt(c, "SELECT COUNT(*) FROM manutencoes WHERE status = 'Concluída'");

                    // Tipo
                    PreparedStatement psTipo = c.prepareStatement(
                            "SELECT tipo_manutencao, COUNT(*) FROM manutencoes GROUP BY tipo_manutencao"
                    );
                    ResultSet rsTipo = psTipo.executeQuery();
                    while (rsTipo.next()) {
                        porTipo.put(rsTipo.getString(1), rsTipo.getInt(2));
                    }

                    // Status
                    PreparedStatement psStatus = c.prepareStatement(
                            "SELECT status, COUNT(*) FROM manutencoes GROUP BY status"
                    );
                    ResultSet rsStatus = psStatus.executeQuery();
                    while (rsStatus.next()) {
                        porStatus.put(rsStatus.getString(1), rsStatus.getInt(2));
                    }

                    // Últimas
                    ultimasModel.setRowCount(0);
                    PreparedStatement psUltimas = c.prepareStatement(
                            "SELECT id_manutencao, data_agendada, tipo_manutencao, status, observacoes " +
                                    "FROM manutencoes ORDER BY data_agendada DESC LIMIT 5"
                    );
                    ResultSet rsUltimas = psUltimas.executeQuery();
                    while (rsUltimas.next()) {
                        ultimasModel.addRow(new Object[]{
                                rsUltimas.getInt(1),
                                rsUltimas.getString(2),
                                rsUltimas.getString(3),
                                rsUltimas.getString(4),
                                rsUltimas.getString(5)
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void done() {
                lblTotalMaquinas.setText(createCardHtml("Máquinas Cadastradas", ""+totalMaquinas, "#2196f3"));
                lblTotalManutencoes.setText(createCardHtml("Manutenções Totais", ""+totalManut, "#ff9800"));
                lblManutencoesAgendadas.setText(createCardHtml("Manutenções Agendadas", ""+agendadas, "#4caf50"));
                lblManutencoesConcluidas.setText(createCardHtml("Manutenções Concluídas", ""+concluidas, "#9c27b0"));

                renderPieChart(porTipo);
                renderBarChart(porStatus);
            }
        };

        worker.execute();
    }

    private int getInt(Connection c, String sql) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    // =====================================================================================
    // 5. GRÁFICOS
    // =====================================================================================
    private void renderPieChart(Map<String, Integer> porTipo) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (porTipo.isEmpty()) dataset.setValue("Nenhum", 1);
        else porTipo.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Manutenções por Tipo", dataset, true, true, false);

        ChartPanel panel = new ChartPanel(chart);

        chartPiePanelContainer.removeAll();
        chartPiePanelContainer.add(panel, BorderLayout.CENTER);
        chartPiePanelContainer.revalidate();
        chartPiePanelContainer.repaint();
    }

    private void renderBarChart(Map<String, Integer> porStatus) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (porStatus.isEmpty())
            dataset.addValue(0, "Status", "Nenhum");
        else
            porStatus.forEach((k, v) -> dataset.addValue(v, "Qtd", k));

        JFreeChart chart = ChartFactory.createBarChart(
                "Manutenções por Status",
                "Status",
                "Quantidade",
                dataset
        );

        ChartPanel panel = new ChartPanel(chart);

        chartBarPanelContainer.removeAll();
        chartBarPanelContainer.add(panel, BorderLayout.CENTER);
        chartBarPanelContainer.revalidate();
        chartBarPanelContainer.repaint();
    }

    // =====================================================================================
    // 6. HTML CARD HELPER
    // =====================================================================================
    private String createCardHtml(String title, String value, String hexColor) {
        return "<html><center>"
                + "<div style='font-size:12px;color:#444'>" + title + "</div>"
                + "<div style='font-size:28px;color:" + hexColor + "'><b>" + value + "</b></div>"
                + "</center></html>";
    }

    // =====================================================================================
    // 7. MAIN DE TESTE — CORRIGIDO
    // =====================================================================================
    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() ->
                new TelaPrincipal("usuario_exemplo", "ADMIN").setVisible(true)
        );
    }
}
