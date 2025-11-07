package telas;

import conexao.ConexaoMySQL;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelaGestaoManutencoes extends JFrame {

    // --- Variáveis para guardar a máquina que "abriu" esta tela ---
    private int idMaquinaPai;
    private String nomeMaquinaPai;

    // --- ID da manutenção selecionada na tabela ---
    private int idManutencaoSelecionada = -1;

    // --- Componentes da Tela ---
    private JPanel painelPrincipal;
    private JTable tabelaManutencoes;
    private JScrollPane scrollPainel;
    private DefaultTableModel tableModel;

    private JLabel lblDataAgendada;
    private JTextField txtDataAgendada; // (AAAA-MM-DD)
    private JLabel lblTipo;
    private JComboBox<String> comboTipo;
    private JLabel lblStatus;
    private JComboBox<String> comboStatus;
    private JLabel lblObservacoes;
    private JTextArea txtObservacoes;
    private JScrollPane scrollObs; // Para a área de texto

    private JButton btnAgendar;
    private JButton btnAtualizar;
    private JButton btnExcluir;

    /**
     * CONSTRUTOR: Recebe o ID e o Nome da máquina da tela anterior
     */
    public TelaGestaoManutencoes(int idMaquina, String nomeMaquina) {
        this.idMaquinaPai = idMaquina;
        this.nomeMaquinaPai = nomeMaquina;

        setTitle("Manutenções da Máquina: " + nomeMaquinaPai);
        setSize(750, 600);
        // DISPOSE_ON_CLOSE: Fecha apenas esta janela, não a aplicação inteira
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza

        painelPrincipal = new JPanel();
        painelPrincipal.setLayout(null);
        add(painelPrincipal);

        // --- Tabela de Manutenções ---
        String[] colunas = {"ID", "Data Agendada", "Tipo", "Status", "Observações"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabelaManutencoes = new JTable(tableModel);

        scrollPainel = new JScrollPane(tabelaManutencoes);
        scrollPainel.setBounds(30, 30, 670, 250);
        painelPrincipal.add(scrollPainel);

        // --- Formulário de Cadastro/Edição ---
        lblDataAgendada = new JLabel("Data Agendada:");
        lblDataAgendada.setBounds(30, 300, 100, 25);
        painelPrincipal.add(lblDataAgendada);

        txtDataAgendada = new JTextField("AAAA-MM-DD");
        txtDataAgendada.setBounds(140, 300, 150, 25);
        painelPrincipal.add(txtDataAgendada);

        lblTipo = new JLabel("Tipo:");
        lblTipo.setBounds(30, 340, 100, 25);
        painelPrincipal.add(lblTipo);

        String[] tipos = {"Preventiva", "Corretiva", "Limpeza", "Inspeção"};
        comboTipo = new JComboBox<>(tipos);
        comboTipo.setBounds(140, 340, 150, 25);
        painelPrincipal.add(comboTipo);

        lblStatus = new JLabel("Status:");
        lblStatus.setBounds(30, 380, 100, 25);
        painelPrincipal.add(lblStatus);

        String[] status = {"Agendada", "Concluída", "Cancelada"};
        comboStatus = new JComboBox<>(status);
        comboStatus.setBounds(140, 380, 150, 25);
        painelPrincipal.add(comboStatus);

        lblObservacoes = new JLabel("Observações:");
        lblObservacoes.setBounds(320, 300, 100, 25);
        painelPrincipal.add(lblObservacoes);

        txtObservacoes = new JTextArea();
        scrollObs = new JScrollPane(txtObservacoes);
        scrollObs.setBounds(320, 330, 380, 105);
        painelPrincipal.add(scrollObs);

        // --- Botões ---
        btnAgendar = new JButton("Agendar Nova");
        btnAgendar.setBounds(30, 480, 140, 30);
        painelPrincipal.add(btnAgendar);

        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setBounds(180, 480, 140, 30);
        painelPrincipal.add(btnAtualizar);

        btnExcluir = new JButton("Excluir");
        btnExcluir.setBounds(330, 480, 140, 30);
        painelPrincipal.add(btnExcluir);

        // --- Carrega os dados iniciais ---
        carregarDadosManutencoes();

        // --- Action Listeners ---

        // Ação: AGENDAR (Create)
        btnAgendar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agendarManutencao();
            }
        });

        // Ação: ATUALIZAR (Update)
        btnAtualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarManutencao();
            }
        });

        // Ação: EXCLUIR (Delete)
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirManutencao();
            }
        });

        // Ação: Clique na Tabela
        tabelaManutencoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int linha = tabelaManutencoes.getSelectedRow();
                if (linha >= 0) {
                    // Pega os dados da linha
                    idManutencaoSelecionada = (int) tableModel.getValueAt(linha, 0);
                    String data = (String) tableModel.getValueAt(linha, 1);
                    String tipo = (String) tableModel.getValueAt(linha, 2);
                    String status = (String) tableModel.getValueAt(linha, 3);
                    String obs = (String) tableModel.getValueAt(linha, 4);

                    // Preenche o formulário
                    txtDataAgendada.setText(data);
                    comboTipo.setSelectedItem(tipo);
                    comboStatus.setSelectedItem(status);
                    txtObservacoes.setText(obs);
                }
            }
        });
    }

    // --- Métodos do CRUD ---

    /**
     * READ: Carrega apenas as manutenções da máquina-pai
     */
    private void carregarDadosManutencoes() {
        tableModel.setRowCount(0); // Limpa a tabela

        // SQL com WHERE para filtrar pela máquina correta
        String sql = "SELECT * FROM manutencoes WHERE id_maquina = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setInt(1, this.idMaquinaPai); // Filtra pelo ID da máquina

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_manutencao");
                    String data = rs.getString("data_agendada");
                    String tipo = rs.getString("tipo_manutencao");
                    String status = rs.getString("status");
                    String obs = rs.getString("observacoes");

                    tableModel.addRow(new Object[]{id, data, tipo, status, obs});
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar manutenções: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CREATE: Agenda uma nova manutenção
     */
    private void agendarManutencao() {
        String data = txtDataAgendada.getText();
        String tipo = (String) comboTipo.getSelectedItem();
        String status = (String) comboStatus.getSelectedItem();
        String obs = txtObservacoes.getText();

        String sql = "INSERT INTO manutencoes (id_maquina, data_agendada, tipo_manutencao, status, observacoes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setInt(1, idMaquinaPai); // **Importante: Vincula à máquina-pai**
            pstmt.setString(2, data);
            pstmt.setString(3, tipo);
            pstmt.setString(4, status);
            pstmt.setString(5, obs);

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Manutenção agendada com sucesso!");
                carregarDadosManutencoes(); // Atualiza a tabela
                limparCampos();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao agendar manutenção: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * UPDATE: Atualiza uma manutenção selecionada
     */
    private void atualizarManutencao() {
        if (idManutencaoSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção na tabela para atualizar.");
            return;
        }

        String data = txtDataAgendada.getText();
        String tipo = (String) comboTipo.getSelectedItem();
        String status = (String) comboStatus.getSelectedItem();
        String obs = txtObservacoes.getText();

        String sql = "UPDATE manutencoes SET data_agendada = ?, tipo_manutencao = ?, status = ?, observacoes = ? WHERE id_manutencao = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setString(1, data);
            pstmt.setString(2, tipo);
            pstmt.setString(3, status);
            pstmt.setString(4, obs);
            pstmt.setInt(5, idManutencaoSelecionada); // Filtra pelo ID da manutenção

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Manutenção atualizada com sucesso!");
                carregarDadosManutencoes();
                limparCampos();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar manutenção: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DELETE: Exclui uma manutenção selecionada
     */
    private void excluirManutencao() {
        if (idManutencaoSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção na tabela para excluir.");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir este agendamento?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (resposta != JOptionPane.YES_OPTION) {
            return; // Usuário cancelou
        }

        String sql = "DELETE FROM manutencoes WHERE id_manutencao = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setInt(1, idManutencaoSelecionada);

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Manutenção excluída com sucesso!");
                carregarDadosManutencoes();
                limparCampos();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir manutenção: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método utilitário para limpar o formulário
    private void limparCampos() {
        txtDataAgendada.setText("AAAA-MM-DD");
        comboTipo.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        txtObservacoes.setText("");
        idManutencaoSelecionada = -1;
        tabelaManutencoes.clearSelection();
    }
}