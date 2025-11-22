package services;

import dao.UsuarioDAO;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * AuthService
 *
 * Responsável por:
 * - autenticação de usuários
 * - verificação de senha
 * - geração de hash seguro (bcrypt)
 * - controle de tentativas
 * - bloqueio automático
 * - registro de auditoria
 *
 * NÃO acessa o banco diretamente — sempre usa o DAO.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final int MAX_TENTATIVAS = 3;  // pode ajustar

    /**
     * Gera hash seguro usando Bcrypt.
     */
    public String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }

    /**
     * Verifica senha usando Bcrypt.
     */
    public boolean verificarSenha(String senhaDigitada, String hashNoBanco) {
        if (senhaDigitada == null || hashNoBanco == null) {
            return false;
        }
        return BCrypt.checkpw(senhaDigitada, hashNoBanco);
    }

    /**
     * Autentica o usuário:
     * - busca por login
     * - verifica bloqueio
     * - verifica senha
     * - registra tentativas
     * - registra sucesso no login
     */
    public Usuario autenticar(String login, String senhaDigitada) throws SQLException {

        Usuario user = usuarioDAO.findByLogin(login);

        // 1. Verificar usuário
        if (user == null) {
            logger.info("Login falhou: usuário não encontrado (" + login + ")");
            return null;
        }

        // 2. Verificar se está bloqueado
        if (user.isBloqueado()) {
            logger.warning("Tentativa de login em conta bloqueada: " + login);
            auditoria.registrar(user.getId(), "LOGIN_BLOCKED", "Conta bloqueada tentou login.");
            return null;
        }

        // 3. Verificar senha
        boolean senhaOk = verificarSenha(senhaDigitada, user.getSenhaHash());

        if (!senhaOk) {
            usuarioDAO.incrementarTentativa(user.getId());
            auditoria.registrar(user.getId(), "LOGIN_FAILURE", "Senha incorreta para " + login);

            int novasTentativas = user.getTentativasLogin() + 1;

            if (novasTentativas >= MAX_TENTATIVAS) {
                usuarioDAO.bloquearUsuario(user.getId());
                auditoria.registrar(user.getId(), "USER_BLOCKED",
                        "Usuário bloqueado após " + MAX_TENTATIVAS + " tentativas.");
            }

            return null;
        }

        // 4. Login OK → resetar tentativas
        usuarioDAO.resetarTentativas(user.getId());
        usuarioDAO.registrarUltimoLogin(user.getId());

        auditoria.registrar(user.getId(), "LOGIN_SUCCESS", "Login realizado com sucesso.");

        return user;
    }
}
