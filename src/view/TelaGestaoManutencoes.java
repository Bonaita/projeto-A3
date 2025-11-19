package view;

import conexao.ConexaoMySQL;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TelaGestaoManutencoes (versão moderna)
 *
 * Tela que exibe, cria, atualiza e exclui manutenções de uma máquina específica.
 * Estrutura:
 *  - TopBar (Voltar / Tela Principal / Logout / Sair)
 *  - Painel superior: informações da máquina
 *  - Tabela responsiva
 *  - Formulário organizado (GridBagLayout)
 *  - Botões de ação (CRUD)
 */
public class TelaGestaoManutencoes extends JFrame {

    private String usuarioLogado;
    private int idMaquinaPai;
    private String nomeMaquinaPai;

    // Controle da manutenção selecionada
    private int idManutencaoSelecionada = -1;

    // Componentes
    private JTable tabelaManutencoes;
    private DefaultTableModel tableModel;

    private JTextField txtDataAgendada;
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboStatus;
    private JTextArea txtObservacoes;

    public TelaGestaoManutencoes(String usuarioLogado, String role, int idMaquina, String nomeMaquina) {

        this.usuarioLogado = usuarioLogado;
        this.idMaquinaPai = idMaquina;
        this.nomeMaquinaPai = nomeMaquina;

        setTitle("Manutenções - " + nomeMaquina);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        // =====================================================================
        // 🔷 TOPBAR COM BOTÕES DE NAVEGAÇÃO
        // =====================================================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel tituloTop = new JLabel("Manutenções da Máquina");
        tituloTop.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topBar.add(tituloTop, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnVoltar = new JButton("Voltar");
        JButton btnPrincipal = new JButton("Tela Principal");
        JButton btnLogout = new JButton("Logout");
        JButton btnSair = new JButton("Sair");

        topButtons.add(btnVoltar);
        topButtons.add(btnPrincipal);
        topButtons.add(btnLogout);
        topButtons.add(btnSair);

        topBar.add(topButtons, BorderLayout.EAST);

        // Adiciona a barra no topo da janela
        add(topBar, BorderLayout.NORTH);

        // ======================= AÇÕES DOS BOTÕES =============================

        btnVoltar.addActionListener(e -> {
            new TelaGestaoMaquinas(usuarioLogado, role).setVisible(true);
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

        // =====================================================================
        // 🔵 TABELA DE MANUTENÇÕES
        // =====================================================================
        String[] colunas = {"ID", "Data", "Tipo", "Status", "Observações"};
        tableModel = new DefaultTableModel(colunas, 0);

        tabelaManutencoes = new JTable(tableModel);
        tabelaManutencoes.setRowHeight(25);
        tabelaManutencoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollTabela = new JScrollPane(tabelaManutencoes);
        scrollTabela.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollTabela, BorderLayout.CENTER);

        // =====================================================================
        // 🟩 FORMULÁRIO (GridBagLayout para layout limpo e organizado)
        // =====================================================================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo Data
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Data Agendada:"), gbc);

        gbc.gridx = 1;
        txtDataAgendada = new JTextField("AAAA-MM-DD");
        formPanel.add(txtDataAgendada, gbc);

        // Campo Tipo
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        comboTipo = new JComboBox<>(new String[]{"Preventiva", "Corretiva", "Limpeza", "Inspeção"});
        formPanel.add(comboTipo, gbc);

        // Campo Status
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        comboStatus = new JComboBox<>(new String[]{"Agendada", "Concluída", "Cancelada"});
        formPanel.add(comboStatus, gbc);

        // Campo Observações
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Observações:"), gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        txtObservacoes = new JTextArea(4, 20);
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        formPanel.add(scrollObs, gbc);

        // Botões (CRUD)
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JButton btnAgendar = new JButton("Agendar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnExcluir = new JButton("Excluir");

        botoesPanel.add(btnAgendar);
        botoesPanel.add(btnAtualizar);
        botoesPanel.add(btnExcluir);

        gbc.gridheight = 1;
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(botoesPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);

        // =====================================================================
        // 🔻 AÇÕES DOS BOTÕES
        // =====================================================================
        btnVoltar.addActionListener(e -> {
            new TelaGestaoMaquinas(usuarioLogado, role).setVisible(true);
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

        btnAgendar.addActionListener(e -> agendarManutencao());
        btnAtualizar.addActionListener(e -> atualizarManutencao());
        btnExcluir.addActionListener(e -> excluirManutencao());

        tabelaManutencoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                carregarCamposDoClique();
            }
        });

        carregarDadosManutencoes();
    }

    // =====================================================================
    // 🔧 CRUD
    // =====================================================================

    private void carregarCamposDoClique() {
        int linha = tabelaManutencoes.getSelectedRow();
        if (linha == -1) return;

        idManutencaoSelecionada = (int) tabelaManutencoes.getValueAt(linha, 0);
        txtDataAgendada.setText((String) tabelaManutencoes.getValueAt(linha, 1));
        comboTipo.setSelectedItem(tabelaManutencoes.getValueAt(linha, 2));
        comboStatus.setSelectedItem(tabelaManutencoes.getValueAt(linha, 3));
        txtObservacoes.setText((String) tabelaManutencoes.getValueAt(linha, 4));
    }

    private void carregarDadosManutencoes() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM manutencoes WHERE id_maquina = ?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMaquinaPai);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_manutencao"),
                        rs.getString("data_agendada"),
                        rs.getString("tipo_manutencao"),
                        rs.getString("status"),
                        rs.getString("observacoes")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar manutenções: " + e.getMessage());
        }
    }

    private void agendarManutencao() {
        String data = txtDataAgendada.getText();
        String tipo = comboTipo.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();
        String obs = txtObservacoes.getText();

        String sql = "INSERT INTO manutencoes (id_maquina, data_agendada, tipo_manutencao, status, observacoes) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMaquinaPai);
            ps.setString(2, data);
            ps.setString(3, tipo);
            ps.setString(4, status);
            ps.setString(5, obs);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Manutenção agendada!");

            carregarDadosManutencoes();
            limparCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao agendar: " + e.getMessage());
        }
    }

    private void atualizarManutencao() {
        if (idManutencaoSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção!");
            return;
        }

        String sql = "UPDATE manutencoes SET data_agendada=?, tipo_manutencao=?, status=?, observacoes=? WHERE id_manutencao=?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtDataAgendada.getText());
            ps.setString(2, comboTipo.getSelectedItem().toString());
            ps.setString(3, comboStatus.getSelectedItem().toString());
            ps.setString(4, txtObservacoes.getText());
            ps.setInt(5, idManutencaoSelecionada);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Atualizado!");

            carregarDadosManutencoes();
            limparCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirManutencao() {
        if (idManutencaoSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM manutencoes WHERE id_manutencao=?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idManutencaoSelecionada);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Excluída!");

            carregarDadosManutencoes();
            limparCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
        }
    }

    private void limparCampos() {
        txtDataAgendada.setText("AAAA-MM-DD");
        comboTipo.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        txtObservacoes.setText("");
        tabelaManutencoes.clearSelection();
        idManutencaoSelecionada = -1;
    }
}
