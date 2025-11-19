package view;

import dao.UserDAO;
import model.Usuario;
import services.AuditoriaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class TelaAdminUsuarios extends JFrame {

    private JTable tabela;
    private DefaultTableModel model;

    public TelaAdminUsuarios(String adminNome) {
        setTitle("Admin - Gestão de Usuários");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
                new String[]{"ID", "Login", "Nome", "Tentativas", "Bloqueado"},
                0
        );

        tabela = new JTable(model);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton btnDesbloquear = new JButton("Desbloquear Usuário");
        JButton btnResetTentativas = new JButton("Resetar Tentativas");
        JButton btnAtualizar = new JButton("Atualizar Lista");

        buttons.add(btnAtualizar);
        buttons.add(btnDesbloquear);
        buttons.add(btnResetTentativas);

        add(buttons, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarUsuarios());
        btnDesbloquear.addActionListener(e -> desbloquearUsuario());
        btnResetTentativas.addActionListener(e -> resetarTentativas());

        carregarUsuarios();
    }

    private void carregarUsuarios() {
        model.setRowCount(0);

        try {
            var lista = listarTodosUsuarios();

            for (Usuario u : lista) {
                model.addRow(new Object[]{
                        u.getId(),
                        u.getLogin(),
                        u.getNomeCompleto(),
                        u.getTentativasLogin(),
                        u.isBloqueado() ? "Sim" : "Não"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Usuario> listarTodosUsuarios() throws SQLException {
        var lista = new ArrayList<Usuario>();

        String sql = "SELECT * FROM usuarios";

        try (var c = conexao.ConexaoMySQL.getConexao();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id_usuario"));
                u.setLogin(rs.getString("login"));
                u.setNomeCompleto(rs.getString("nome_completo"));
                u.setTentativasLogin(rs.getInt("tentativas_login"));
                u.setBloqueado(rs.getBoolean("bloqueado"));

                lista.add(u);
            }
        }
        return lista;
    }

    private void desbloquearUsuario() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        try {
            var dao = new UserDAO();
            dao.resetarTentativas(id);
            dao.bloquearUsuario(id); // mas esse bloqueia, então criamos desbloquear

            String sql = "UPDATE usuarios SET bloqueado = FALSE WHERE id_usuario = ?";
            try (var c = conexao.ConexaoMySQL.getConexao();
                 var ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            new AuditoriaService().registrar(id, "USER_UNLOCKED", "Desbloqueado por admin");
            JOptionPane.showMessageDialog(this, "Usuário desbloqueado!");
            carregarUsuarios();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetarTentativas() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        try {
            var dao = new UserDAO();
            dao.resetarTentativas(id);
            JOptionPane.showMessageDialog(this, "Tentativas resetadas!");
            new AuditoriaService().registrar(id, "RESET_ATTEMPTS", "Resetado por admin");

            carregarUsuarios();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
