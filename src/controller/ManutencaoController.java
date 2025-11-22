package controller;

import dao.ManutencaoDAO;
import model.Manutencao;
import services.AuditoriaService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ManutencaoController {

    private final ManutencaoDAO dao = new ManutencaoDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    public List<Manutencao> listarPorMaquina(int id) {
        try {
            return dao.listarPorMaquina(id);
        } catch (Exception e) {
            auditoria.registrar(0, "ERRO_LISTAR_MANUTENCOES", e.getMessage());
            return Collections.emptyList();
        }
    }
    public List<Manutencao> listarTodas() {
        try {
            return dao.listarTodas();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void inserir(Manutencao m, int usuarioId) throws SQLException {
        dao.inserir(m);
        auditoria.registrar(usuarioId,
                "CRIAR_MANUTENCAO",
                "Manutenção criada para máquina ID " + m.getIdMaquina());
    }

    public void atualizar(Manutencao m, int usuarioId) throws SQLException {
        dao.atualizar(m);
        auditoria.registrar(usuarioId,
                "EDITAR_MANUTENCAO",
                "Manutenção ID " + m.getId() + " atualizada.");
    }

    public void excluir(int id, int usuarioId) throws SQLException {
        dao.excluir(id);
        auditoria.registrar(usuarioId,
                "EXCLUIR_MANUTENCAO",
                "Manutenção ID " + id + " excluída.");
    }
}
