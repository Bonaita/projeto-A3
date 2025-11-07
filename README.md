💻 Projeto A3 — Sistema de Gestão de Máquinas e Manutenções

Este projeto é uma aplicação desktop desenvolvida em Java (Swing) com integração ao banco de dados MySQL.
O sistema permite o cadastro, consulta e gerenciamento de máquinas e manutenções, além de autenticação de usuários via tela de login.

📋 Sumário

Estrutura do Projeto

Tecnologias Utilizadas

Banco de Dados

Configuração da Conexão

Dependência Maven

Instalação e Execução

Erros Comuns e Soluções

Capturas de Tela (opcional)

Autor

Licença

🗂️ Estrutura do Projeto
projeto-A3/
 ├─ src/
 │   ├─ conexao/
 │   │   └─ ConexaoMySQL.java          # Classe de conexão com o banco
 │   └─ telas/
 │       ├─ TelaLogin.java             # Tela de login
 │       ├─ TelaGestaoMaquinas.java    # Tela de gerenciamento de máquinas
 │       └─ TelaGestaoManutencoes.java # Tela de gerenciamento de manutenções
 │
 ├─ pom.xml                            # Arquivo Maven (dependências)
 ├─ README.md                          # Este documento
 └─ .gitignore

⚙️ Tecnologias Utilizadas
Tecnologia	Função
Java 17+	Linguagem principal
Swing (javax.swing)	Interface gráfica
MySQL 8+	Banco de dados relacional
JDBC (Java Database Connectivity)	Conexão entre Java e MySQL
Maven	Gerenciamento de dependências
🗄️ Banco de Dados

Crie um banco de dados no MySQL:

CREATE DATABASE projeto_a3;
USE projeto_a3;


Crie as tabelas utilizadas:

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL
);

CREATE TABLE maquinas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    modelo VARCHAR(100),
    status VARCHAR(50)
);

CREATE TABLE manutencoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_maquina INT,
    data DATE,
    descricao VARCHAR(255),
    FOREIGN KEY (id_maquina) REFERENCES maquinas(id)
);

🔌 Configuração da Conexão

Edite o arquivo src/conexao/ConexaoMySQL.java conforme seu ambiente local:

package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/projeto_a3?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "sua_senha";

    public static Connection getConexao() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver"); // Carrega o driver
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

📦 Dependência Maven

Verifique se o MySQL Connector está declarado no pom.xml:

<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>


Depois, atualize o projeto no IntelliJ:

Maven → Reload Project
ou no terminal:

mvn clean install

🚀 Instalação e Execução
🔧 Pré-requisitos

Java JDK 17+ instalado

MySQL Server rodando localmente

IntelliJ IDEA ou VSCode com Extensão Java

Driver MySQL Connector (automático via Maven)

▶️ Executando o projeto

Clone o repositório:

git clone https://github.com/SEU_USUARIO/projeto-A3.git


Abra o projeto no IntelliJ.

Certifique-se que o Maven sincronizou corretamente.

Execute a classe principal de login:

src/telas/TelaLogin.java


Após logar, acesse as demais telas pelo menu da aplicação.

🧠 Erros Comuns e Soluções
Erro	Causa	Solução
ClassNotFoundException: com.mysql.cj.jdbc.Driver	Driver JDBC ausente	Verifique dependência Maven ou adicione manualmente o mysql-connector-j.jar
Cannot invoke prepareStatement() because "conexao" is null	Conexão falhou	Cheque URL, usuário e senha no ConexaoMySQL.java
Cannot find symbol variable ConexaoMySQL	Falta de import ou nome incorreto	Verifique import conexao.ConexaoMySQL; e se o nome do arquivo/classe começa com maiúscula
Access denied for user 'root'@'localhost'	Senha incorreta ou sem permissão	Corrija credenciais no código ou ajuste permissões do usuário no MySQL
No suitable driver found for jdbc:mysql://...	Driver não carregado	Use Class.forName("com.mysql.cj.jdbc.Driver") antes da conexão

👨‍💻 Autores

Ana Monteiro
Bruno Bonaita dos Santos
📅 Novembro de 2025
🎓 Projeto acadêmico — Avaliação A3
