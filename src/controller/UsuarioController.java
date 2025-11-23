package controller;

import dao.UsuarioDAO;
import model.Usuario;
import services.AuditoriaService;
import services.AuthService;

import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuthService authService = new AuthService();
    private final AuditoriaService auditoria = new AuditoriaService();

    // Login delega ao AuthService (que cuida de tentativas/bloqueio)
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
        usuario.setPrimeiroAcesso(1);
        usuario.setTentativasLogin(0);
        usuario.setBloqueado(false);
        usuarioDAO.inserirUsuario(usuario);
        auditoria.registrar(usuario.getId(), "CRIAR_USUARIO", "Usuário criado: " + usuario.getLogin());
    }

    // Atualização quando o usuário muda sua senha (troca)
    public void atualizarSenha(Usuario usuario) throws SQLException {
        usuarioDAO.resetarSenha(usuario.getId(), usuario.getSenhaHash());
        usuarioDAO.marcarPrimeiroAcessoConcluido(usuario.getId());
        auditoria.registrar(usuario.getId(), "ALTERAR_SENHA", "Senha alterada pelo usuário.");
    }

    // Reset de senha feito pelo admin
    public void resetarSenha(int idUsuario) throws SQLException {
        String novaSenha = "123456";
        String hash = authService.gerarHash(novaSenha);
        usuarioDAO.resetarSenha(idUsuario, hash);
        auditoria.registrar(idUsuario, "RESETAR_SENHA", "Senha resetada para padrão pelo admin.");
    }

    // Permite a UI (admin) bloquear/desbloquear
    public void alterarBloqueio(int idUsuario, boolean bloqueado) throws SQLException {
        usuarioDAO.atualizarBloqueio(idUsuario, bloqueado);
        auditoria.registrar(idUsuario, bloqueado ? "BLOQUEAR_USUARIO" : "DESBLOQUEAR_USUARIO",
                "Admin alterou bloqueio para: " + (bloqueado ? "1" : "0"));
    }

    // Atualiza dados basicos (já existente no seu projeto)
    public void atualizarDadosBasicos(Usuario u) throws SQLException {
        usuarioDAO.atualizarDadosBasicos(u);

        auditoria.registrar(
                u.getId(),
                "ATUALIZAR_DADOS",
                "Usuário alterou nome/login/role/bloqueio"
        );
}}
