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

public class TelaGestaoMaquinas extends JFrame {

    private final String usuarioLogado;
    private final String role;

    private int idMaquinaSelecionada = -1;

    private JTable tabelaMaquinas;
    private DefaultTableModel tableModel;

    private JTextField txtNomeEquipamento;
    private JTextField txtSetor;
    private JTextField txtDataAquisicao;

    public TelaGestaoMaquinas(String usuarioLogado, String role) {
        this.usuarioLogado = usuarioLogado;
        this.role = role;

        setTitle("Gestão de Máquinas");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(criarTopBar(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);
        add(criarFormulario(), BorderLayout.SOUTH);

        carregarDadosTabela();
    }

    // =====================================================================
    // 🔷 TOPBAR
    // =====================================================================
    private JPanel criarTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("Gestão de Máquinas");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topBar.add(titulo, BorderLayout.WEST);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnVoltar = new JButton("Voltar");
        JButton btnPrincipal = new JButton("Tela Principal");
        JButton btnLogout = new JButton("Logout");
        JButton btnSair = new JButton("Sair");

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

        botoes.add(btnVoltar);
        botoes.add(btnPrincipal);
        botoes.add(btnLogout);
        botoes.add(btnSair);

        topBar.add(botoes, BorderLayout.EAST);
        return topBar;
    }

    // =====================================================================
    // 🟦 TABELA
    // =====================================================================
    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome Equipamento", "Setor", "Data Aquisição"};

        tableModel = new DefaultTableModel(colunas, 0);
        tabelaMaquinas = new JTable(tableModel);
        tabelaMaquinas.setRowHeight(25);
        tabelaMaquinas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelaMaquinas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                carregarCamposDoClique();
            }
        });

        JScrollPane scroll = new JScrollPane(tabelaMaquinas);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        return scroll;
    }

    // =====================================================================
    // 🟩 FORMULÁRIO
    // =====================================================================
    private JPanel criarFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Nome Equipamento:"), gbc);

        gbc.gridx = 1;
        txtNomeEquipamento = new JTextField();
        form.add(txtNomeEquipamento, gbc);

        // Setor
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Setor:"), gbc);

        gbc.gridx = 1;
        txtSetor = new JTextField();
        form.add(txtSetor, gbc);

        // Data
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Data Aquisição:"), gbc);

        gbc.gridx = 1;
        txtDataAquisicao = new JTextField("AAAA-MM-DD");
        form.add(txtDataAquisicao, gbc);

        // Botões CRUD
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JButton btnSalvar = new JButton("Salvar");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnVerManutencoes = new JButton("Ver Manutenções");

        btnSalvar.addActionListener(e -> salvarMaquina());
        btnAtualizar.addActionListener(e -> atualizarMaquina());
        btnExcluir.addActionListener(e -> excluirMaquina());
        btnVerManutencoes.addActionListener(e -> abrirTelaManutencoes());

        botoes.add(btnSalvar);
        botoes.add(btnAtualizar);
        botoes.add(btnExcluir);
        botoes.add(btnVerManutencoes);

        gbc.gridx = 1; gbc.gridy = 3;
        form.add(botoes, gbc);

        return form;
    }

    // =====================================================================
    // 🔧 CRUD
    // =====================================================================
    private void carregarCamposDoClique() {
        int linha = tabelaMaquinas.getSelectedRow();
        if (linha == -1) return;

        idMaquinaSelecionada = (int) tabelaMaquinas.getValueAt(linha, 0);
        txtNomeEquipamento.setText(tabelaMaquinas.getValueAt(linha, 1).toString());
        txtSetor.setText(tabelaMaquinas.getValueAt(linha, 2).toString());
        txtDataAquisicao.setText(tabelaMaquinas.getValueAt(linha, 3).toString());
    }

    private void carregarDadosTabela() {
        tableModel.setRowCount(0);

        String sql = "SELECT * FROM maquinas";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_maquina"),
                        rs.getString("nome_equipamento"),
                        rs.getString("setor"),
                        rs.getString("data_aquisicao")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar máquinas: " + e.getMessage());
        }
    }

    private void salvarMaquina() {
        String sql = "INSERT INTO maquinas (nome_equipamento, setor, data_aquisicao) VALUES (?, ?, ?)";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNomeEquipamento.getText());
            ps.setString(2, txtSetor.getText());
            ps.setString(3, txtDataAquisicao.getText());
            ps.executeUpdate();

            mensagem("Máquina salva!");
            recarregar();

        } catch (SQLException e) {
            mensagem("Erro ao salvar: " + e.getMessage());
        }
    }

    private void atualizarMaquina() {
        if (idMaquinaSelecionada == -1) {
            mensagem("Selecione uma máquina!");
            return;
        }

        String sql = "UPDATE maquinas SET nome_equipamento=?, setor=?, data_aquisicao=? WHERE id_maquina=?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNomeEquipamento.getText());
            ps.setString(2, txtSetor.getText());
            ps.setString(3, txtDataAquisicao.getText());
            ps.setInt(4, idMaquinaSelecionada);

            ps.executeUpdate();
            mensagem("Atualizada!");
            recarregar();

        } catch (SQLException e) {
            mensagem("Erro ao atualizar: " + e.getMessage());
        }
    }

    private void excluirMaquina() {
        if (idMaquinaSelecionada == -1) {
            mensagem("Selecione uma máquina!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir esta máquina?",
                "Excluir",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM maquinas WHERE id_maquina=?";

        try (Connection con = ConexaoMySQL.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMaquinaSelecionada);
            ps.executeUpdate();

            mensagem("Máquina excluída!");
            recarregar();

        } catch (SQLException e) {
            mensagem("Erro ao excluir: " + e.getMessage());
        }
    }

    private void abrirTelaManutencoes() {
        if (idMaquinaSelecionada == -1) {
            mensagem("Selecione uma máquina primeiro!");
            return;
        }

        String nome = txtNomeEquipamento.getText();

        TelaGestaoManutencoes t =
                new TelaGestaoManutencoes(usuarioLogado, role, idMaquinaSelecionada, nome);

        t.setVisible(true);
        dispose();
    }

    // =====================================================================
    // UTILITÁRIOS
    // =====================================================================
    private void mensagem(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void recarregar() {
        carregarDadosTabela();
        limparCampos();
    }

    private void limparCampos() {
        txtNomeEquipamento.setText("");
        txtSetor.setText("");
        txtDataAquisicao.setText("AAAA-MM-DD");
        tabelaMaquinas.clearSelection();
        idMaquinaSelecionada = -1;
    }
}
