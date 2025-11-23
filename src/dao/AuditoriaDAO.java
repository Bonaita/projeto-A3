package dao;

import conexao.ConexaoMySQL;
import model.Auditoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    public void inserir(int usuarioId, String acao, String detalhes) throws SQLException {

        String sql = """
            INSERT INTO auditoria (id_usuario, acao, detalhes)
            VALUES (?, ?, ?)
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, acao);
            ps.setString(3, detalhes);
            ps.executeUpdate();
        }
    }

    public List<Auditoria> listarTodas() throws SQLException {

        String sql = """
            SELECT id_auditoria, id_usuario, acao, detalhes, ts
            FROM auditoria
            ORDER BY ts DESC
        """;

        List<Auditoria> lista = new ArrayList<>();

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Auditoria a = new Auditoria();
                a.setId(rs.getInt("id_auditoria"));
                a.setUsuarioId(rs.getInt("id_usuario"));
                a.setAcao(rs.getString("acao"));
                a.setDetalhes(rs.getString("detalhes"));

                Timestamp ts = rs.getTimestamp("ts");
                if (ts != null) {
                    a.setDataHora(ts.toLocalDateTime());
                }

                lista.add(a);
            }
        }

        return lista;
    }
}
