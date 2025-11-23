package dao;

import conexao.ConexaoMySQL;
import model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario findByLogin(String login) throws SQLException {
        String sql = "SELECT id_usuario, nome_completo, login, senha_hash, role, bloqueado, tentativas_login, primeiro_acesso, ultimo_login FROM usuarios WHERE login = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNomeCompleto(rs.getString("nome_completo"));
        u.setLogin(rs.getString("login"));
        u.setSenhaHash(rs.getString("senha_hash")); // usa coluna 'senha' do DB
        u.setRole(rs.getString("role"));
        u.setTentativasLogin(rs.getInt("tentativas_login"));
        u.setPrimeiroAcesso(rs.getInt("primeiro_acesso"));
        String bloqueadoVal = rs.getString("bloqueado");
        u.setBloqueado("1".equals(bloqueadoVal));
        Timestamp ts = rs.getTimestamp("ultimo_login");
        if (ts != null) u.setUltimoLogin(ts.toLocalDateTime());
        return u;
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id_usuario, nome_completo, login, senha_hash, role, bloqueado, tentativas_login, primeiro_acesso, ultimo_login FROM usuarios ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapUsuario(rs));
            }
        }
        return lista;
    }

    public void inserirUsuario(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nome_completo, login, senha_hash, role, bloqueado, tentativas_login, primeiro_acesso, ultimo_login) VALUES (?, ?, ?, ?, ?, ?, ?, NULL)";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNomeCompleto());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getSenhaHash()); // já é hash
            ps.setString(4, u.getRole());
            ps.setString(5, u.isBloqueado() ? "1" : "0");
            ps.setInt(6, u.getTentativasLogin());
            ps.setInt(7, u.getPrimeiroAcesso());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
        }
    }

    public void resetarSenha(int idUsuario, String hash) throws SQLException {
        String sql = "UPDATE usuarios SET senha_hash = ?, primeiro_acesso = 1, tentativas_login = 0 WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void atualizarTentativas(int idUsuario, int tentativas) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = ? WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tentativas);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void registrarLoginSucesso(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = 0, ultimo_login = NOW() WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void atualizarBloqueio(int idUsuario, boolean bloqueado) throws SQLException {
        String sql = "UPDATE usuarios SET bloqueado = ? WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bloqueado ? "1" : "0");
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void marcarPrimeiroAcessoConcluido(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET primeiro_acesso = 1 WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

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

    // Zerar / atualizar tentativas de login
    public void updateTentativas(int idUsuario, int tentativas) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = ? WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tentativas);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    // Bloquear usuário (por excesso de tentativas)
    public void bloquearUsuario(int idUsuario) throws SQLException {
        atualizarBloqueio(idUsuario, true);
    }
}
