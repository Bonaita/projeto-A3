package conexao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConexaoMySQL com HikariCP
 */
public class ConexaoMySQL {

    private static final HikariDataSource dataSource;

    static {

        HikariConfig config = new HikariConfig();

        // ===== CONFIGURAÇÕES DO BANCO =====
        config.setJdbcUrl("jdbc:mysql://localhost:3306/db_manutencao?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("123456");

        // ===== CONFIGURAÇÕES DO HIKARI =====
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(600000);
        config.setLeakDetectionThreshold(5000);

        // Teste automático para garantir saúde do pool
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);

        System.out.println("HikariCP inicializado com sucesso!");
    }

    /**
     * Retorna uma conexão do pool (rápida e confiável).
     */
    public static Connection getConexao() throws SQLException {
        return dataSource.getConnection();
    }
}
