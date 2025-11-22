package dao;

import conexao.ConexaoMySQL;
import model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // ======================
    // MAPEAR RESULTSET
    // ======================
    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();

        u.setId(rs.getInt("id_usuario"));
        u.setLogin(rs.getString("login"));
        u.setNomeCompleto(rs.getString("nome_completo"));

        // senha é exatamente como no banco
        u.setSenhaHash(rs.getString("senha"));

        u.setRole(rs.getString("role"));
        u.setTentativasLogin(rs.getInt("tentativas_login"));
        u.setBloqueado("1".equals(rs.getString("bloqueado")));

        Timestamp ts = rs.getTimestamp("ultimo_login");
        u.setUltimoLogin(ts != null ? ts.toLocalDateTime() : null);

        return u;
    }

    // ======================
    // BUSCAR LOGIN
    // ======================
    public Usuario findByLogin(String login) throws SQLException {
        String sql = """
            SELECT * FROM usuarios
            WHERE login = ?
            LIMIT 1
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUsuario(rs);
            }
        }
        return null;
    }

    // ======================
    // BUSCAR POR ID
    // ======================
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT * FROM usuarios
            WHERE id_usuario = ?
            LIMIT 1
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUsuario(rs);
            }
        }
        return null;
    }

    // ======================
    // LISTAR TODOS
    // ======================
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM usuarios ORDER BY nome_completo";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapUsuario(rs));
        }

        return lista;
    }

    // ======================
    // INSERIR NOVO USUÁRIO
    // ======================
    public void inserirUsuario(Usuario u) throws SQLException {
        String sql = """
            INSERT INTO usuarios
            (nome_completo, login, senha, role, tentativas_login, bloqueado)
            VALUES (?, ?, ?, ?, 0, 0)
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNomeCompleto());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getSenhaHash());
            ps.setString(4, u.getRole());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
    }

    // ======================
    // RESETAR SENHA
    // ======================
    public void resetarSenha(int id, String senhaHash) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ? WHERE id_usuario = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, senhaHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ======================
    // INCREMENTAR TENTATIVAS
    // ======================
    public void incrementarTentativa(int id) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = tentativas_login + 1 WHERE id_usuario = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ======================
    // RESETAR TENTATIVAS
    // ======================
    public void resetarTentativas(int id) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = 0 WHERE id_usuario = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ======================
    // BLOQUEAR USUÁRIO
    // ======================
    public void bloquearUsuario(int id) throws SQLException {
        String sql = "UPDATE usuarios SET bloqueado = 1 WHERE id_usuario = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ======================
    // REGISTRAR ÚLTIMO LOGIN
    // ======================
    public void registrarUltimoLogin(int id) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_login = NOW() WHERE id_usuario = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ======================
    // ATUALIZAR PERFIL
    // ======================
    public void atualizarDadosBasicos(Usuario u) throws SQLException {
        String sql = """
            UPDATE usuarios
            SET nome_completo = ?, login = ?, role = ?, bloqueado = ?
            WHERE id_usuario = ?
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getNomeCompleto());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getRole());
            ps.setString(4, u.isBloqueado() ? "1" : "0");
            ps.setInt(5, u.getId());

            ps.executeUpdate();
        }
    }
}
