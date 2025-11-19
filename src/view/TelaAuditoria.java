package view;

import conexao.ConexaoMySQL;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class TelaAuditoria extends JFrame {

    private JTextField txtUsuario;
    private JTextField txtAcao;
    private JTextField txtDataDe;
    private JTextField txtDataAte;
    private JButton btnFiltrar;
    private JButton btnExportar;
    private JButton btnRefresh;
    private JTable tabela;
    private DefaultTableModel model;

    private final int MAX_ROWS = 5000;

    public TelaAuditoria(String usuarioLogado, String role) {
        setTitle("Auditoria - Eventos do Sistema");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        carregarDados(null, null, null, null);
    }

    private void initUI() {

        JPanel filtro = new JPanel(new GridBagLayout());
        filtro.setBorder(BorderFactory.createTitledBorder("Filtros"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        filtro.add(new JLabel("Usuário:"), gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField();
        filtro.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filtro.add(new JLabel("Ação:"), gbc);

        gbc.gridx = 1;
        txtAcao = new JTextField();
        filtro.add(txtAcao, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        filtro.add(new JLabel("Data De (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        txtDataDe = new JTextField();
        filtro.add(txtDataDe, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        filtro.add(new JLabel("Data Até:"), gbc);

        gbc.gridx = 3;
        txtDataAte = new JTextField();
        filtro.add(txtDataAte, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnFiltrar = new JButton("Filtrar");
        btnRefresh = new JButton("Limpar / Atualizar");
        btnExportar = new JButton("Exportar CSV");

        botoes.add(btnFiltrar);
        botoes.add(btnRefresh);
        botoes.add(btnExportar);

        filtro.add(botoes, gbc);

        add(filtro, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Login", "Ação", "Detalhes", "Timestamp"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(model);
        tabela.setAutoCreateRowSorter(true);

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnRefresh.addActionListener(e -> {
            txtUsuario.setText("");
            txtAcao.setText("");
            txtDataDe.setText("");
            txtDataAte.setText("");
            carregarDados(null, null, null, null);
        });

        btnExportar.addActionListener(e -> exportarCsv());
    }

    private void aplicarFiltros() {
        String u = txtUsuario.getText().trim();
        String a = txtAcao.getText().trim();
        String de = txtDataDe.getText().trim();
        String ate = txtDataAte.getText().trim();

        if (!de.isEmpty() && !isValidDate(de)) {
            JOptionPane.showMessageDialog(this, "Data De inválida.");
            return;
        }
        if (!ate.isEmpty() && !isValidDate(ate)) {
            JOptionPane.showMessageDialog(this, "Data Até inválida.");
            return;
        }

        carregarDados(
                u.isEmpty() ? null : u,
                a.isEmpty() ? null : a,
                de.isEmpty() ? null : de,
                ate.isEmpty() ? null : ate
        );
    }

    private boolean isValidDate(String s) {
        try { java.time.LocalDate.parse(s); return true; }
        catch (Exception ex) { return false; }
    }

    private void carregarDados(String usuario, String acao, String de, String ate) {

        model.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT a.id_auditoria, u.login, a.acao, a.detalhes, a.ts " +
                        "FROM auditoria a LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario "
        );

        ArrayList<Object> params = new ArrayList<>();
        boolean where = false;

        if (usuario != null) {
            sql.append(where ? " AND " : " WHERE ").append("u.login LIKE ?");
            params.add("%" + usuario + "%");
            where = true;
        }

        if (acao != null) {
            sql.append(where ? " AND " : " WHERE ").append("a.acao LIKE ?");
            params.add("%" + acao + "%");
            where = true;
        }

        if (de != null && ate != null) {
            sql.append(where ? " AND " : " WHERE ").append("DATE(a.ts) BETWEEN ? AND ?");
            params.add(de);
            params.add(ate);
        } else if (de != null) {
            sql.append(where ? " AND " : " WHERE ").append("DATE(a.ts) >= ?");
            params.add(de);
        } else if (ate != null) {
            sql.append(where ? " AND " : " WHERE ").append("DATE(a.ts) <= ?");
            params.add(ate);
        }

        sql.append(" ORDER BY a.ts DESC LIMIT ").append(MAX_ROWS);

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_auditoria"),
                        rs.getString("login"),
                        rs.getString("acao"),
                        rs.getString("detalhes"),
                        rs.getTimestamp("ts")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar auditoria: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void exportarCsv() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("auditoria.csv"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {

                for (int c = 0; c < model.getColumnCount(); c++) {
                    pw.print(model.getColumnName(c));
                    if (c < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        pw.print(model.getValueAt(r, c));
                        if (c < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
            }

            JOptionPane.showMessageDialog(this, "CSV exportado com sucesso!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar: " + ex.getMessage());
        }
    }
}
