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


public class TelaGestaoMaquinas extends JFrame {

    // --- Componentes da Tela ---
    private JPanel painelPrincipal;
    private JTable tabelaMaquinas;
    private JScrollPane scrollPainel;
    private DefaultTableModel tableModel;

    private JTextField txtNomeEquipamento;
    private JLabel lblSetor;
    private JTextField txtSetor;
    private JLabel lblDataAquisicao;
    private JTextField txtDataAquisicao; // (Ex: AAAA-MM-DD)

    private JButton btnSalvar;
    private JButton btnAtualizar;
    private JButton btnExcluir;
    private JButton btnVerManutencoes;

    // Armazena o ID da máquina selecionada na tabela
    private int idMaquinaSelecionada = -1;

    // --- Construtor ---
    public TelaGestaoMaquinas() {
        setTitle("Gestão de Máquinas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        painelPrincipal = new JPanel();
        painelPrincipal.setLayout(null); // Layout nulo novamente
        add(painelPrincipal);

        // --- Tabela para listar as máquinas ---
        String[] colunas = {"ID", "Nome Equipamento", "Setor", "Data Aquisição"};
        tableModel = new DefaultTableModel(colunas, 0); // 0 linhas iniciais
        tabelaMaquinas = new JTable(tableModel);

        scrollPainel = new JScrollPane(tabelaMaquinas);
        scrollPainel.setBounds(50, 40, 700, 250);
        painelPrincipal.add(scrollPainel);

        // --- Campos de Formulário (para C e U do CRUD) ---
        JLabel lblNome = new JLabel("Nome Equip.:");
        lblNome.setBounds(50, 320, 100, 25);
        painelPrincipal.add(lblNome);

        txtNomeEquipamento = new JTextField();
        txtNomeEquipamento.setBounds(160, 320, 250, 25);
        painelPrincipal.add(txtNomeEquipamento);

        lblSetor = new JLabel("Setor:");
        lblSetor.setBounds(50, 360, 100, 25);
        painelPrincipal.add(lblSetor);

        txtSetor = new JTextField();
        txtSetor.setBounds(160, 360, 250, 25);
        painelPrincipal.add(txtSetor);

        lblDataAquisicao = new JLabel("Data Aquisição:");
        lblDataAquisicao.setBounds(50, 400, 100, 25);
        painelPrincipal.add(lblDataAquisicao);

        txtDataAquisicao = new JTextField("AAAA-MM-DD");
        txtDataAquisicao.setBounds(160, 400, 250, 25);
        painelPrincipal.add(txtDataAquisicao);

        // --- Botões do CRUD ---
        btnSalvar = new JButton("Salvar Nova");
        btnSalvar.setBounds(50, 460, 120, 30);
        painelPrincipal.add(btnSalvar);

        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setBounds(180, 460, 120, 30);
        painelPrincipal.add(btnAtualizar);

        btnExcluir = new JButton("Excluir");
        btnExcluir.setBounds(310, 460, 120, 30);
        painelPrincipal.add(btnExcluir);

        btnVerManutencoes = new JButton("Ver Manutenções");
        btnVerManutencoes.setBounds(580, 320, 170, 50);
        painelPrincipal.add(btnVerManutencoes);

        // --- Carrega os dados do banco na tabela ---
        carregarDadosTabela();

        // --- Action Listeners (Ações dos Botões) ---

        // Ação: SALVAR (Create)
        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarMaquina();
            }
        });

        // Ação: ATUALIZAR (Update)
        btnAtualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarMaquina();
            }
        });

        // Ação: EXCLUIR (Delete)
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirMaquina();
            }
        });

        // Ação: VER MANUTENÇÕES (Navegação)
        btnVerManutencoes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirTelaManutencoes();
            }
        });

        // Ação: Clique na Tabela (para selecionar um item)
        tabelaMaquinas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int linhaSelecionada = tabelaMaquinas.getSelectedRow();
                if (linhaSelecionada >= 0) {
                    // Pega os dados da linha clicada
                    idMaquinaSelecionada = (int) tableModel.getValueAt(linhaSelecionada, 0);
                    String nome = (String) tableModel.getValueAt(linhaSelecionada, 1);
                    String setor = (String) tableModel.getValueAt(linhaSelecionada, 2);
                    String data = (String) tableModel.getValueAt(linhaSelecionada, 3);

                    // Coloca os dados nos campos de texto
                    txtNomeEquipamento.setText(nome);
                    txtSetor.setText(setor);
                    txtDataAquisicao.setText(data);
                }
            }
        });
    }

    // --- Métodos do CRUD ---

    /**
     * READ: Busca todos os dados da tabela 'maquinas' e popula a JTable
     */
    private void carregarDadosTabela() {
        // Limpa a tabela antes de carregar (evita duplicatas)
        tableModel.setRowCount(0);

        String sql = "SELECT * FROM maquinas";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_maquina");
                String nome = rs.getString("nome_equipamento");
                String setor = rs.getString("setor");
                String data = rs.getString("data_aquisicao");

                // Adiciona os dados como uma nova linha na JTable
                tableModel.addRow(new Object[]{id, nome, setor, data});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CREATE: Insere uma nova máquina no banco de dados
     */
    private void salvarMaquina() {
        String nome = txtNomeEquipamento.getText();
        String setor = txtSetor.getText();
        String data = txtDataAquisicao.getText(); // (Idealmente, você deve validar este formato)

        String sql = "INSERT INTO maquinas (nome_equipamento, setor, data_aquisicao) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.setString(2, setor);
            pstmt.setString(3, data); // O MySQL converte a string 'AAAA-MM-DD' para DATE

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Máquina salva com sucesso!");
                carregarDadosTabela(); // Atualiza a tabela com o novo item
                limparCampos();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar máquina: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * UPDATE: Atualiza uma máquina existente
     */
    private void atualizarMaquina() {
        if (idMaquinaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma máquina na tabela para atualizar.");
            return;
        }

        String nome = txtNomeEquipamento.getText();
        String setor = txtSetor.getText();
        String data = txtDataAquisicao.getText();

        String sql = "UPDATE maquinas SET nome_equipamento = ?, setor = ?, data_aquisicao = ? WHERE id_maquina = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.setString(2, setor);
            pstmt.setString(3, data);
            pstmt.setInt(4, idMaquinaSelecionada); // Usa o ID salvo quando clicamos na tabela

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Máquina atualizada com sucesso!");
                carregarDadosTabela(); // Recarrega a tabela
                limparCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao atualizar. Máquina não encontrada.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar máquina: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DELETE: Exclui uma máquina do banco
     */
    private void excluirMaquina() {
        if (idMaquinaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma máquina na tabela para excluir.");
            return;
        }

        // Confirmação de exclusão
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir esta máquina? (Isso excluirá TODAS as suas manutenções)",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (resposta != JOptionPane.YES_OPTION) {
            return; // Usuário cancelou
        }

        String sql = "DELETE FROM maquinas WHERE id_maquina = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setInt(1, idMaquinaSelecionada);

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(this, "Máquina excluída com sucesso!");
                carregarDadosTabela();
                limparCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao excluir. Máquina não encontrada.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir máquina: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navegação: Abre a tela de manutenções
     */
    private void abrirTelaManutencoes() {
        if (idMaquinaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma máquina para ver suas manutenções.");
            return;
        }

        // Pega o nome da máquina para exibir na próxima tela
        String nomeMaquina = txtNomeEquipamento.getText();

        // (Esta tela ainda não criamos)
        TelaGestaoManutencoes telaManutencoes = new TelaGestaoManutencoes(idMaquinaSelecionada, nomeMaquina);
        telaManutencoes.setVisible(true);

        // Opcional: fechar esta tela ou mantê-la aberta
        // this.dispose();
    }

    // Método utilitário para limpar os campos após uma ação
    private void limparCampos() {
        txtNomeEquipamento.setText("");
        txtSetor.setText("");
        txtDataAquisicao.setText("AAAA-MM-DD");
        idMaquinaSelecionada = -1; // Reseta o ID selecionado
        tabelaMaquinas.clearSelection(); // Remove a seleção da tabela
    }

    // (Opcional) Método 'main' para testar esta tela individualmente
    public static void main(String[] args) {
        TelaGestaoMaquinas tela = new TelaGestaoMaquinas();
        tela.setVisible(true);
    }
}