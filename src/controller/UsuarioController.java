package controller;

import conexao.ConexaoMySQL;
import dao.UsuarioDAO;
import model.Usuario;
import services.AuditoriaService;
import services.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuthService authService = new AuthService();
    private final AuditoriaService auditoria = new AuditoriaService();

    public Usuario login(String login, String senha) {
        try {
            return authService.autenticar(login, senha);
        } catch (Exception e) {
            auditoria.registrar(0, "ERRO_LOGIN", e.getMessage());
            return null;
        }
    }

    public List<Usuario> listarTodos() {
        try {
            return usuarioDAO.listarTodos();
        } catch (Exception e) {
            auditoria.registrar(0, "ERRO_LISTAR_USUARIOS", e.getMessage());
            return List.of();
        }
    }

    public void criarUsuario(Usuario usuario, String senhaPura) throws SQLException {

        String hash = authService.gerarHash(senhaPura);
        usuario.setSenhaHash(hash);

        usuarioDAO.inserirUsuario(usuario);

        auditoria.registrar(usuario.getId(),
                "CRIAR_USUARIO",
                "Usuário criado: " + usuario.getLogin()
        );
    }

    public void atualizarSenha(Usuario usuario) throws SQLException {
        usuarioDAO.resetarSenha(usuario.getId(), usuario.getSenhaHash());
        auditoria.registrar(usuario.getId(), "ALTERAR_SENHA", "Senha alterada.");
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
}
