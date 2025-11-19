package services;

import dao.UserDAO;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * AuthService: camada de autenticação.
 * - gera hashes bcrypt
 * - verifica credenciais e aplica política de lockout
 *
 * Observação: o valor MAX_TENTATIVAS pode ser ajustado.
 */
public class AuthService {

    private final AuditoriaService auditoria = new AuditoriaService();
    private final UserDAO userDAO = new UserDAO();
    private final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final int MAX_TENTATIVAS = 3;

    /**
     * Gera um hash BCrypt para uma senha em texto.
     */
    public String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(12));
    }

    /**
     * Valida a senha digitada contra o hash do banco.
     */
    public boolean verificarSenha(String senhaDigitada, String hashNoBanco) {
        if (hashNoBanco == null) return false;
        return BCrypt.checkpw(senhaDigitada, hashNoBanco);
    }

    /**
     * Tenta autenticar um usuário pelo login e senha.
     *
     * Retorna o objeto Usuario em caso de sucesso.
     * Retorna null em caso de falha (senha incorreta ou usuário não existe ou bloqueado).
     *
     * Além disso:
     * - incrementa tentativas em caso de falha
     * - bloqueia o usuário se exceder MAX_TENTATIVAS
     * - reseta tentativas e registra ultimo_login em caso de sucesso
     *
     * Lança SQLException em caso de problema no banco.
     */
    public Usuario autenticar(String login, String senhaDigitada) throws SQLException {
        Usuario u = userDAO.findByLogin(login);
        if (u == null) {
            logger.info("Tentativa de login com usuário inexistente: " + login);
            return null;
        }

        if (u.isBloqueado()) {
            logger.warning("Tentativa de login em conta bloqueada: " + login);
            return null;
        }

        // Verifica senha
        if (verificarSenha(senhaDigitada, u.getSenhaHash())) {
            // sucesso: resetar tentativas e registrar último login
            userDAO.resetarTentativas(u.getId());
            userDAO.registrarUltimoLogin(u.getId());

            auditoria.registrar(u.getId(), "LOGIN_SUCCESS", "Login bem-sucedido para " + login);

            return u;
        } else {
            // falha: incrementar tentativas e bloquear se necessário

            userDAO.incrementarTentativa(u.getId());
            int tent = u.getTentativasLogin() + 1; // nota: valor anterior; para 100% correto reconsultar do DB
            auditoria.registrar(u.getId(), "LOGIN_FAILURE", "Senha incorreta para " + login);

            // Se já excedeu (podemos reconsultar o valor real, mas bloquear se a tentativa antiga +1 >= MAX)
            if (tent >= MAX_TENTATIVAS) {

                userDAO.bloquearUsuario(u.getId());
                auditoria.registrar(u.getId(), "USER_BLOCKED",
                        "Usuário bloqueado após exceder tentativas de login");
            }
            return null;
        }
    }
}
