package dao;

import conexao.ConexaoMySQL;
import model.Manutencao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoDAO {

    public List<Manutencao> listarTodas() throws SQLException {

        String sql = """
        SELECT id_manutencao, id_maquina, data_agendada, 
               tipo_manutencao, status, observacoes
        FROM manutencoes
        ORDER BY data_agendada DESC
    """;

        List<Manutencao> lista = new ArrayList<>();

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Manutencao m = new Manutencao();
                m.setId(rs.getInt("id_manutencao"));
                m.setIdMaquina(rs.getInt("id_maquina"));
                m.setDataAgendada(rs.getDate("data_agendada").toLocalDate());
                m.setTipo(rs.getString("tipo_manutencao"));
                m.setStatus(rs.getString("status"));
                m.setObservacoes(rs.getString("observacoes"));

                lista.add(m);
            }
        }

        return lista;
    }
    public List<Manutencao> listarPorMaquina(int idMaquina) throws SQLException {

        String sql = """
        SELECT id_manutencao, id_maquina, data_agendada,
               tipo_manutencao, status, observacoes
        FROM manutencoes
        WHERE id_maquina = ?
        ORDER BY data_agendada DESC
    """;

        List<Manutencao> lista = new ArrayList<>();

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idMaquina);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Manutencao m = new Manutencao();
                    m.setId(rs.getInt("id_manutencao"));
                    m.setIdMaquina(rs.getInt("id_maquina"));
                    m.setDataAgendada(rs.getDate("data_agendada").toLocalDate());
                    m.setTipo(rs.getString("tipo_manutencao"));
                    m.setStatus(rs.getString("status"));
                    m.setObservacoes(rs.getString("observacoes"));
                    lista.add(m);
                }
            }
        }

        return lista;
    }

    public void inserir(Manutencao m) throws SQLException {
        String sql = """
            INSERT INTO manutencoes (id_maquina, data_agendada, tipo_manutencao, status, observacoes)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, m.getIdMaquina());
            ps.setDate(2, Date.valueOf(m.getDataAgendada()));
            ps.setString(3, m.getTipo());
            ps.setString(4, m.getStatus());
            ps.setString(5, m.getObservacoes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
        }
    }

    public void atualizar(Manutencao m) throws SQLException {
        String sql = """
            UPDATE manutencoes
            SET id_maquina = ?, data_agendada = ?, tipo_manutencao = ?, 
                status = ?, observacoes = ?
            WHERE id_manutencao = ?
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, m.getIdMaquina());
            ps.setDate(2, Date.valueOf(m.getDataAgendada()));
            ps.setString(3, m.getTipo());
            ps.setString(4, m.getStatus());
            ps.setString(5, m.getObservacoes());
            ps.setInt(6, m.getId());

            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM manutencoes WHERE id_manutencao = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
