package services;

import dao.AuditoriaDAO;

public class AuditoriaService {

    private final AuditoriaDAO dao = new AuditoriaDAO();

    /**
     * Registra auditoria de qualquer ação do sistema.
     *
     * @param usuarioId id do usuário que executou a ação
     * @param acao título curto do evento
     * @param detalhes texto explicando o que aconteceu
     */
    public void registrar(int usuarioId, String acao, String detalhes) {
        try {
            dao.inserir(usuarioId, acao, detalhes);
        } catch (Exception e) {
            System.err.println("[AUDITORIA] Erro ao registrar auditoria: " + e.getMessage());
        }
    }
}
