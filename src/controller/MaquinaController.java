package controller;

import dao.MaquinaDAO;
import model.Maquina;
import model.Usuario;
import security.Permission;
import security.SecurityService;
import services.AuditoriaService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Controller para Maquina. Recebe Usuario quando precisa auditar/verificar permissão.
 */
public class MaquinaController {

    private final MaquinaDAO dao = new MaquinaDAO();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final SecurityService security = new SecurityService();

    public List<Maquina> listarTodas() {
        try {
            return dao.listarTodas();
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Maquina buscarPorId(int id) {
        try {
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void inserir(Maquina m, Usuario usuario) throws SQLException {
        // autorização
        security.requirePermission(usuario.getRole(), Permission.EDIT_MACHINE);

        dao.inserir(m);
        auditoria.registrar(usuario.getId(), "CRIAR_MAQUINA", "Máquina criada: " + m.getNome());
    }

    public void atualizar(Maquina m, Usuario usuario) throws SQLException {
        security.requirePermission(usuario.getRole(), Permission.EDIT_MACHINE);

        dao.atualizar(m);
        auditoria.registrar(usuario.getId(), "EDITAR_MAQUINA", "Máquina ID " + m.getId() + " atualizada.");
    }

    public void excluir(int id, Usuario usuario) throws SQLException {
        security.requirePermission(usuario.getRole(), Permission.DELETE_MACHINE);

        dao.excluir(id);
        auditoria.registrar(usuario.getId(), "EXCLUIR_MAQUINA", "Máquina ID " + id + " excluída.");
    }
}
