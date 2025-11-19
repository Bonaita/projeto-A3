package services;

import conexao.ConexaoMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Serviço responsável por registrar todas as ações importantes do sistema.
 * Ele permite rastrear:
 * - Login bem-sucedido
 * - Login falho
 * - Usuário bloqueado
 * - Troca de senha
 * - Desbloqueio por administrador
 * - Reset de tentativas
 */
public class AuditoriaService {

    /**
     * Registra uma ação no banco de auditoria.
     *
     * @param idUsuario  ID do usuário que originou a ação (pode ser null)
     * @param acao       Nome da ação (ex: LOGIN_SUCCESS)
     * @param detalhes   Descrição detalhada do evento
     */
    public void registrar(Integer idUsuario, String acao, String detalhes) {
        String sql = "INSERT INTO auditoria (id_usuario, acao, detalhes) VALUES (?, ?, ?)";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (idUsuario == null)
                ps.setNull(1, java.sql.Types.INTEGER);
            else
                ps.setInt(1, idUsuario);

            ps.setString(2, acao);
            ps.setString(3, detalhes);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
