package view;

import dao.AuditoriaDAO;
import dao.UsuarioDAO;
import model.Auditoria;
import model.Usuario;
import security.Permission;
import security.SecurityService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TelaAuditoria
 */
public class TelaAuditoria extends JFrame {

    private final Usuario usuarioLogado;
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final SecurityService security = new SecurityService();

    private JTable tabela;
    private DefaultTableModel model;

    private JComboBox<String> cbUsuario;
    private JComboBox<String> cbAcao;
    private JTextField txtDataInicio;
    private JTextField txtDataFim;
    private JTextField txtBusca;

    private JButton btnAplicar;
    private JButton btnLimpar;
    private JButton btnAtualizar;
    private JButton btnExportCsv;

    // cache
    private List<Auditoria> todosRegistros = new ArrayList<>();
    private Map<Integer, String> usuarioNomeCache = new HashMap<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TelaAuditoria(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        // Permissão
        if (!security.roleHasPermission(usuarioLogado.getRole(), Permission.VIEW_AUDIT)) {
            JOptionPane.showMessageDialog(null,
                    "Você não tem permissão para acessar a Auditoria.",
                    "Acesso Negado",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setTitle("Auditoria Avançada");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        carregarTodosRegistros();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // HEADER
        JLabel titulo = new JLabel("Auditoria");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel subtitulo = new JLabel("Filtre, pesquise e analise as ações realizadas no sistema");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(subtitulo);
        root.add(header, BorderLayout.NORTH);

        // CONTENT (sidebar + tabela)
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        root.add(content, BorderLayout.CENTER);

        JPanel sidebar = criarSidebar();
        sidebar.setPreferredSize(new Dimension(320, 0));
        content.add(sidebar, BorderLayout.WEST);

        JPanel painelTabela = criarPainelTabela();
        content.add(painelTabela, BorderLayout.CENTER);
    }

    private JPanel criarSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(250, 250, 250));
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblFiltros = new JLabel("Filtros de Auditoria");
        lblFiltros.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(lblFiltros);
        side.add(Box.createRigidArea(new Dimension(0, 12)));

        // Usuário
        side.add(new JLabel("Usuário:"));
        cbUsuario = new JComboBox<>();
        cbUsuario.addItem("Todos");
        cbUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        side.add(cbUsuario);
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        // Ação
        side.add(new JLabel("Tipo de Ação:"));
        cbAcao = new JComboBox<>();
        cbAcao.addItem("Todos");
        String[] acoes = new String[]{
                "CRIAR_MAQUINA",
                "EDITAR_MAQUINA",
                "EXCLUIR_MAQUINA",
                "CRIAR_MANUTENCAO",
                "EDITAR_MANUTENCAO",
                "EXCLUIR_MANUTENCAO",
                "LOGIN",
                "LOGOFF",
                "RESETAR_SENHA",
                "CRIAR_USUARIO"
        };
        for (String a : acoes) cbAcao.addItem(a);
        cbAcao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        side.add(cbAcao);
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        // Data início / fim
        side.add(new JLabel("Data início (yyyy-MM-dd):"));
        txtDataInicio = new JTextField();
        txtDataInicio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        side.add(txtDataInicio);
        side.add(Box.createRigidArea(new Dimension(0, 6)));

        side.add(new JLabel("Data fim (yyyy-MM-dd):"));
        txtDataFim = new JTextField();
        txtDataFim.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        side.add(txtDataFim);
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        // Texto livre
        side.add(new JLabel("Busca (texto livre em detalhes ou ação):"));
        txtBusca = new JTextField();
        txtBusca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        side.add(txtBusca);
        side.add(Box.createRigidArea(new Dimension(0, 12)));

        // Botões
        btnAplicar = new JButton("Aplicar Filtro");
        btnAplicar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAplicar.setBackground(new Color(0, 120, 215));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setFocusPainted(false);
        side.add(btnAplicar);
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        btnLimpar = new JButton("Limpar Filtros");
        btnLimpar.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(btnLimpar);
        side.add(Box.createRigidArea(new Dimension(0, 12)));

        btnExportCsv = new JButton("Exportar CSV");
        btnExportCsv.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(btnExportCsv);
        side.add(Box.createVerticalGlue());

        // ações
        btnAplicar.addActionListener(e -> aplicarFiltros());
        btnLimpar.addActionListener(e -> limparFiltros());
        btnExportCsv.addActionListener(e -> exportarCsv());

        return side;
    }

    private JPanel criarPainelTabela() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 12, 6, 6));

        // topo com botão atualizar
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JPanel topoDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topoDireita.setOpaque(false);

        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setBackground(new Color(0, 120, 215));
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.addActionListener(e -> carregarTodosRegistros());

        topoDireita.add(btnAtualizar);
        topo.add(topoDireita, BorderLayout.EAST);
        p.add(topo, BorderLayout.NORTH);

        // tabela
        model = new DefaultTableModel(new Object[]{
                "ID", "Usuário", "Ação", "Detalhes", "Data/Hora"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(model);
        tabela.setRowHeight(28);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabela.setSelectionBackground(new Color(0, 120, 215));
        tabela.setSelectionForeground(Color.WHITE);

        JTableHeader th = tabela.getTableHeader();
        th.setBackground(new Color(230, 230, 230));
        th.setForeground(new Color(50, 50, 50));
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void carregarTodosRegistros() {
        btnAtualizar.setEnabled(false);
        btnAplicar.setEnabled(false);
        btnLimpar.setEnabled(false);

        SwingWorker<List<Auditoria>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Auditoria> doInBackground() throws Exception {
                return auditoriaDAO.listarTodas();
            }

            @Override
            protected void done() {
                try {
                    todosRegistros = get();
                    popularUsuarioCache();
                    popularComboUsuarios();
                    aplicarFiltros();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TelaAuditoria.this,
                            "Erro ao carregar auditoria: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnAtualizar.setEnabled(true);
                    btnAplicar.setEnabled(true);
                    btnLimpar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Preenche map idUsuario->nome usando UsuarioDAO.listarTodos().
     * Fallback para mostrar o id caso nome não esteja disponível.
     */
    private void popularUsuarioCache() {
        usuarioNomeCache.clear();
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            for (Usuario u : usuarios) {
                usuarioNomeCache.put(u.getId(), u.getNomeCompleto() == null || u.getNomeCompleto().isBlank()
                        ? u.getLogin()
                        : u.getNomeCompleto());
            }
        } catch (Exception ex) {
            // fallback: preencher somente com ids presentes nos registros
            for (Auditoria a : todosRegistros) {
                usuarioNomeCache.putIfAbsent(a.getUsuarioId(), String.valueOf(a.getUsuarioId()));
            }
        }

        // garante que qualquer id presente nos registros esteja no cache
        for (Auditoria a : todosRegistros) {
            usuarioNomeCache.putIfAbsent(a.getUsuarioId(), String.valueOf(a.getUsuarioId()));
        }
    }

    private void popularComboUsuarios() {
        cbUsuario.removeAllItems();
        cbUsuario.addItem("Todos");

        List<Map.Entry<Integer, String>> entries = new ArrayList<>(usuarioNomeCache.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getValue));

        for (Map.Entry<Integer, String> e : entries) {
            cbUsuario.addItem(e.getKey() + " - " + e.getValue());
        }
    }

    private void aplicarFiltros() {

        // usuario
        String usuarioSelecionado = (String) cbUsuario.getSelectedItem();
        Integer usuarioIdFiltro = null;
        if (usuarioSelecionado != null && !usuarioSelecionado.equals("Todos")) {
            String[] parts = usuarioSelecionado.split(" - ");
            try {
                usuarioIdFiltro = Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException ignored) {}
        }

        // ação
        String acaoSelecionada = (String) cbAcao.getSelectedItem();
        if ("Todos".equals(acaoSelecionada)) {
            acaoSelecionada = null;
        }

        // datas
        LocalDate dataInicio = null;
        LocalDate dataFim = null;

        try {
            if (!txtDataInicio.getText().trim().isBlank())
                dataInicio = LocalDate.parse(txtDataInicio.getText().trim(), DATE_FMT);

            if (!txtDataFim.getText().trim().isBlank())
                dataFim = LocalDate.parse(txtDataFim.getText().trim(), DATE_FMT);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Datas devem estar no formato yyyy-MM-dd",
                    "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String busca = txtBusca.getText().trim().toLowerCase();

        // =====================================================
        // CRIAÇÃO DE VARIÁVEIS FINAIS (SOLUÇÃO DO ERRO)
        // =====================================================
        final Integer fUsuarioIdFiltro = usuarioIdFiltro;
        final String fAcaoSelecionada = acaoSelecionada;
        final LocalDate fDataInicio = dataInicio;
        final LocalDate fDataFim = dataFim;
        final String fBusca = busca;

        List<Auditoria> filtrados = todosRegistros.stream().filter(a -> {

            // usuário
            if (fUsuarioIdFiltro != null && a.getUsuarioId() != fUsuarioIdFiltro)
                return false;

            // ação
            if (fAcaoSelecionada != null &&
                    (a.getAcao() == null || !a.getAcao().equalsIgnoreCase(fAcaoSelecionada)))
                return false;

            // data
            if (fDataInicio != null || fDataFim != null) {
                if (a.getDataHora() == null) return false;
                LocalDate dt = a.getDataHora().toLocalDate();
                if (fDataInicio != null && dt.isBefore(fDataInicio)) return false;
                if (fDataFim != null && dt.isAfter(fDataFim)) return false;
            }

            // busca
            if (!fBusca.isBlank()) {
                boolean inDetalhes =
                        a.getDetalhes() != null &&
                                a.getDetalhes().toLowerCase().contains(fBusca);

                boolean inAcao =
                        a.getAcao() != null &&
                                a.getAcao().toLowerCase().contains(fBusca);

                if (!inDetalhes && !inAcao) return false;
            }

            return true;

        }).collect(Collectors.toList());

        popularTabela(filtrados);
    }

    private void popularTabela(List<Auditoria> lista) {
        model.setRowCount(0);

        for (Auditoria a : lista) {
            String usuarioExibicao = usuarioNomeCache.getOrDefault(a.getUsuarioId(),
                    a.getUsuarioId() == 0 ? "—" : String.valueOf(a.getUsuarioId()));

            String detalhes = a.getDetalhes() == null ? "" : a.getDetalhes();
            String when = a.getDataHora() == null ? "" : a.getDataHora().format(DATETIME_FMT);

            Object[] row = new Object[]{
                    a.getId(),
                    usuarioExibicao,
                    a.getAcao(),
                    detalhes,
                    when
            };
            model.addRow(row);
        }
    }

    private void limparFiltros() {
        cbUsuario.setSelectedIndex(0);
        cbAcao.setSelectedIndex(0);
        txtDataInicio.setText("");
        txtDataFim.setText("");
        txtBusca.setText("");
        aplicarFiltros();
    }

    private void exportarCsv() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Não há registros para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("auditoria_export.csv"));
        int opt = chooser.showSaveDialog(this);
        if (opt != JFileChooser.APPROVE_OPTION) return;

        java.io.File f = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(f, "UTF-8")) {
            // header
            for (int c = 0; c < model.getColumnCount(); c++) {
                pw.print(model.getColumnName(c));
                if (c < model.getColumnCount() - 1) pw.print(";");
            }
            pw.println();

            // rows
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    String out = val == null ? "" : val.toString().replace(";", ",");
                    pw.print(out);
                    if (c < model.getColumnCount() - 1) pw.print(";");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Exportado com sucesso para: " + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar CSV: " + ex.getMessage());
        }
    }
}
