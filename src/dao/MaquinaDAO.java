package dao;

import conexao.ConexaoMySQL;
import model.Maquina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaquinaDAO {

    public List<Maquina> listarTodas() throws SQLException {
        String sql = "SELECT id_maquina, nome_equipamento, setor, data_aquisicao FROM maquinas";
        List<Maquina> lista = new ArrayList<>();

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Maquina m = new Maquina();
                m.setId(rs.getInt("id_maquina"));
                m.setNome(rs.getString("nome_equipamento"));
                m.setLocal(rs.getString("setor"));
                m.setDataAquisicao(rs.getDate("data_aquisicao").toLocalDate());
                lista.add(m);
            }
        }
        return lista;
    }

    public void inserir(Maquina m) throws SQLException {
        String sql = "INSERT INTO maquinas (nome_equipamento, setor, data_aquisicao) VALUES (?, ?, ?)";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNome());
            ps.setString(2, m.getLocal());
            ps.setDate(3, Date.valueOf(m.getDataAquisicao()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setId(keys.getInt(1));
                }
            }
        }
    }

    public void atualizar(Maquina m) throws SQLException {
        String sql = """
                UPDATE maquinas 
                SET nome_equipamento = ?, setor = ?, data_aquisicao = ?
                WHERE id_maquina = ?
        """;

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, m.getNome());
            ps.setString(2, m.getLocal());
            ps.setDate(3, Date.valueOf(m.getDataAquisicao()));
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM maquinas WHERE id_maquina = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Maquina buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_maquina, nome_equipamento, setor, data_aquisicao FROM maquinas WHERE id_maquina = ?";

        try (Connection c = ConexaoMySQL.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Maquina m = new Maquina();
                    m.setId(rs.getInt("id_maquina"));
                    m.setNome(rs.getString("nome_equipamento"));
                    m.setLocal(rs.getString("setor"));
                    m.setDataAquisicao(rs.getDate("data_aquisicao").toLocalDate());
                    return m;
                }
            }
        }

        return null;
    }
}
