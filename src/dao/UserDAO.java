package dao;

import conexao.ConexaoMySQL;
import model.Usuario;

import java.sql.*;

/**
 * DAO responsável por operações básicas na tabela usuarios.
 * - findByLogin
 * - updateSenhaHash
 * - incrementar/resetar tentativas
 * - bloquear/desbloquear
 * - registrarUltimoLogin
 */
public class UserDAO {

    public Usuario findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE login = ? LIMIT 1";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id_usuario"));
                    u.setLogin(rs.getString("login"));
                    u.setNomeCompleto(rs.getString("nome_completo"));
                    u.setSenhaHash(rs.getString("senha"));
                    u.setRole(rs.getString("role"));
                    u.setPrimeiroAcesso(rs.getBoolean("primeiro_acesso"));
                    u.setTentativasLogin(rs.getInt("tentativas_login"));
                    u.setBloqueado(rs.getBoolean("bloqueado"));
                    return u;
                }
            }
        }
        return null;
    }

    public void updateSenhaHash(int idUsuario, String hash) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ?, primeiro_acesso = FALSE WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void incrementarTentativa(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = tentativas_login + 1 WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void resetarTentativas(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET tentativas_login = 0 WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void bloquearUsuario(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET bloqueado = TRUE WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void registrarUltimoLogin(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_login = NOW() WHERE id_usuario = ?";
        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }
}
