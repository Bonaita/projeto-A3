package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {

    // --- CONFIGURE SEUS DADOS AQUI ---
    private static final String DB_NAME = "db_manutencao";
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String USER = "root";
    private static final String PASS = "123456";

    // --- NÃO PRECISA MUDAR DAQUI PARA BAIXO ---
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useTimezone=true&serverTimezone=UTC";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";


    public static Connection getConexao() {
        try {
            // 1. Carrega o driver (que o Maven baixou)
            Class.forName(DRIVER_CLASS);

            // 2. Tenta obter a conexão
            return DriverManager.getConnection(URL, USER, PASS);

        } catch (ClassNotFoundException e) {
            System.err.println("ERRO: Driver JDBC do MySQL não encontrado!");
            System.err.println("Verifique se a dependência do Maven no 'pom.xml' está correta.");
            e.printStackTrace();
            return null;

        } catch (SQLException e) {
            System.err.println("ERRO: Falha ao conectar ao banco.");
            System.err.println("Verifique se o MySQL (XAMPP) está ligado.");
            System.err.println("Verifique se USER e PASS estão corretos.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * MÉTODO DE TESTE: Execute este arquivo para testar a conexão
     */
    public static void main(String[] args) {
        System.out.println("Testando conexão com o banco...");
        Connection conn = getConexao();

        if (conn != null) {
            System.out.println("==========================================");
            System.out.println("SUCESSO! Conexão estabelecida.");
            System.out.println("==========================================");
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("==========================================");
            System.err.println("FALHA! Não foi possível conectar.");
            System.err.println("==========================================");
        }
    }
}