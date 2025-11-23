package services;

import dao.UsuarioDAO;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * AuthService
 *
 * Responsável por:
 *  - autenticar usuários
 *  - verificar/generar hash de senha (BCrypt)
 *  - controlar tentativas de login e bloqueio automático
 *
 * Dependências:
 *  - jBCrypt (org.mindrot.jbcrypt.BCrypt)
 *  - UsuarioDAO com métodos: findByLogin, updateTentativas, bloquearUsuario
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Limite de tentativas antes de bloquear o usuário
    private static final int MAX_TENTATIVAS = 3;

    /**
     * Autentica um usuário pelo login e senha em texto puro.
     *
     * Comportamento:
     *  - Se usuário não existir -> retorna null
     *  - Se usuário bloqueado -> lança Exception
     *  - Se senha inválida -> incrementa tentativas; bloqueia se atingir MAX_TENTATIVAS; retorna null
     *  - Se senha válida -> zera tentativas e retorna o objeto Usuario
     *
     * @param login login
     * @param senha senha em texto puro
     * @return Usuario autenticado ou null (se credenciais inválidas)
     * @throws Exception em caso de usuário bloqueado ou erro crítico
     */
    public Usuario autenticar(String login, String senha) throws Exception {
        try {
            Usuario user = usuarioDAO.findByLogin(login);

            // usuário não existe
            if (user == null) {
                return null;
            }

            // usuário bloqueado
            if (user.isBloqueado()) {
                throw new Exception("Usuário bloqueado. Entre em contato com o administrador.");
            }

            // validar senha
            boolean senhaOk = verificarSenha(senha, user.getSenhaHash());

            if (!senhaOk) {
                // incrementar tentativas
                int tentativasAtuais = user.getTentativasLogin();
                int novasTentativas = tentativasAtuais + 1;

                usuarioDAO.updateTentativas(user.getId(), novasTentativas);

                // bloquear se ultrapassar limite
                if (novasTentativas >= MAX_TENTATIVAS) {
                    usuarioDAO.bloquearUsuario(user.getId());
                    throw new Exception("Usuário bloqueado após " + novasTentativas + " tentativas inválidas.");
                }

                // senha inválida, sem exceção (retorna null)
                return null;
            }

            // login com sucesso -> zerar tentativas
            usuarioDAO.updateTentativas(user.getId(), 0);

            return user;

        } catch (SQLException sqle) {
            // encapsular SQLException em Exception para o controller tratar
            throw new Exception("Erro ao autenticar: " + sqle.getMessage(), sqle);
        }
    }

    /**
     * Gera hash BCrypt para a senha em texto claro.
     *
     * @param senhaPlana senha em texto claro
     * @return hash BCrypt
     */
    public String gerarHash(String senhaPlana) {
        // work factor padrão 10
        return BCrypt.hashpw(senhaPlana, BCrypt.gensalt(10));
    }

    /**
     * Verifica se a senha em texto simples corresponde ao hash armazenado.
     *
     * @param senhaPlana senha em texto claro
     * @param hash armazenado
     * @return true se bater
     */
    public boolean verificarSenha(String senhaPlana, String hash) {
        if (hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(senhaPlana, hash);
        } catch (Exception e) {
            // qualquer problema ao checar o hash -> considerar inválido
            return false;
        }
    }
}
