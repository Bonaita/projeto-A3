# 🏭 Sistema de Controle de Manutenção Preventiva — ODS 9

Este projeto é uma aplicação **desktop em Java (Swing)** desenvolvida como parte do tema da **ODS 9 — Indústria, Inovação e Infraestrutura**.  
O sistema tem como objetivo auxiliar empresas e indústrias no **controle de manutenções preventivas de máquinas**, aumentando a **eficiência, produtividade e sustentabilidade** dos processos industriais.

---

## 📋 Sumário

- [Objetivo — ODS 9](#-objetivo---ods-9)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Banco de Dados — `db_manutencao`](#-banco-de-dados---db_manutencao)
- [Configuração da Conexão com o MySQL](#-configuração-da-conexão-com-o-mysql)
- [Como adicionar o driver JDBC no IntelliJ IDEA](#-como-adicionar-o-driver-jdbc-no-intellij-idea)
- [Execução do Projeto](#-execução-do-projeto)
- [Tela de Login](#-tela-de-login)
- [Funcionalidades](#-funcionalidades)
- [Classe Principal](#-classe-principal)
- [Erros Comuns e Soluções](#-erros-comuns-e-soluções)
- [Inserindo Usuário Inicial (para teste)](#-inserindo-usuário-inicial-para-teste)
- [Autor](#-autor)
- [Licença](#-licença)
- [Justificativa e Impacto](#-justificativa-e-impacto)

---

## 🌍 Objetivo — ODS 9

> **ODS 9 - Indústria, Inovação e Infraestrutura**  
> “Construir infraestruturas resilientes, promover a industrialização inclusiva e sustentável e fomentar a inovação.”

Este projeto contribui com a ODS 9 ao propor uma solução tecnológica que **reduz falhas não planejadas em equipamentos**, melhora a **gestão de ativos** e incentiva o uso de **ferramentas digitais** na indústria.

---

## 🧭 Estrutura do Projeto

```
projeto-A3/
 ├─ src/
 │   └─ com/bonaita/
 │       ├─ ConexaoMySQL.java
 │       ├─ TelaLogin.java
 │       ├─ TelaGestaoMaquinas.java
 │       ├─ TelaGestaoManutencoes.java
 │       └─ Main.java
 ├─ .gitignore
 └─ README.md
```

**Arquivos principais (pacote `com.bonaita`):**

- `ConexaoMySQL.java` — classe de conexão com o banco MySQL.
- `TelaLogin.java` — JFrame de login; valida usuário na tabela `usuarios`.
- `TelaGestaoMaquinas.java` — CRUD de máquinas (cadastrar, listar, editar, excluir).
- `TelaGestaoManutencoes.java` — CRUD de manutenções para uma máquina específica.
- `Main.java` — ponto de entrada que inicia a `TelaLogin`.

---

## ⚙️ Tecnologias Utilizadas

| Tecnologia | Finalidade |
|---|---|
| **Java 17+** | Linguagem principal |
| **Swing (javax.swing)** | Interface gráfica (JFrame, JTable, etc.) |
| **MySQL 8+** | Banco de dados local |
| **JDBC** | Comunicação Java ↔ MySQL |
| **IntelliJ IDEA** | IDE utilizada (projeto sem Maven) |

---

## 🗄️ Banco de Dados — `db_manutencao`

Execute os comandos abaixo no MySQL local para criar o banco e as tabelas:

```sql
CREATE DATABASE db_manutencao;
USE db_manutencao;

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
    id_maquina INT NOT NULL,
    data DATE,
    descricao VARCHAR(255),
    FOREIGN KEY (id_maquina) REFERENCES maquinas(id)
);
```

---

## 🔌 Configuração da Conexão com o MySQL

Edite `src/com/bonaita/ConexaoMySQL.java` com suas credenciais locais:

```java
package com.bonaita;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/db_manutencao?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "sua_senha";

    public static Connection getConexao() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver"); // Carrega o driver JDBC
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

---

## 📦 Como adicionar o driver JDBC no IntelliJ IDEA

> **(Projeto sem Maven — adicionar JAR manualmente)**

1. Baixe o driver MySQL Connector/J:  
   https://dev.mysql.com/downloads/connector/j/

2. Extraia o ZIP e localize o arquivo `.jar` (ex: `mysql-connector-j-8.0.33.jar`).

3. No IntelliJ:
    - `File` → `Project Structure` (ou pressione `Ctrl+Alt+Shift+S`)
    - Selecione `Modules` → aba `Dependencies`
    - Clique no botão `+` → `JARs or directories`
    - Selecione o `.jar` do connector
    - Clique em `Apply` → `OK`

4. Recompile o projeto: `Build` → `Rebuild Project`

---

## 🚀 Execução do Projeto

**Pré-requisitos:**
- Java JDK 17+ instalado
- MySQL rodando localmente (com o banco `db_manutencao` criado)
- IntelliJ IDEA configurado com o JAR do connector

**Passos para execução:**

1. Abra o projeto no IntelliJ.
2. Verifique/edite `ConexaoMySQL.java` com usuário/senha do MySQL.
3. Crie usuário de teste (veja abaixo).
4. Execute a classe `Main.java`:
    - Clique com o botão direito > `Run 'Main.main()'`
    - Ou crie uma *Run Configuration* para `com.bonaita.Main`.

A aplicação abrirá a **Tela de Login**; ao autenticar, você acessa a **TelaGestaoMaquinas**.

---

## 🔐 Tela de Login

A validação do login (no `TelaLogin.java`) deve usar `PreparedStatement` com a query:

```sql
SELECT * FROM usuarios WHERE login = ? AND senha = ?;
```

---

## ⚙️ Funcionalidades

### 🧰 Gestão de Máquinas (`TelaGestaoMaquinas.java`)
- Cadastrar máquina (nome, modelo, status)
- Listar máquinas em tabela (JTable)
- Editar registro selecionado
- Excluir registro

### 🧾 Gestão de Manutenções (`TelaGestaoManutencoes.java`)
- Registrar manutenção preventiva vinculada a uma máquina
- Listar histórico de manutenções da máquina
- Editar/Excluir registros de manutenção

---

## 🧩 Classe Principal

```java
package com.bonaita;

public class Main {
    public static void main(String[] args) {
        new TelaLogin(); // inicia a aplicação exibindo a tela de login
    }
}
```

---

## 🧠 Erros Comuns e Soluções

| Erro | Causa provável | Solução |
|---:|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Driver JDBC não adicionado ao projeto | Baixe o connector JAR e adicione como dependência no IntelliJ |
| `Cannot invoke prepareStatement() because "conexao" is null` | `getConexao()` falhou — `conexao` é nula | Verifique URL, usuário, senha; trate exceções e verifique retorno da conexão |
| `Access denied for user 'root'@'localhost'` | Usuário/senha incorretos ou permissões | Corrija credenciais ou crie/ajuste usuário no MySQL |
| `No suitable driver found for jdbc:mysql://...` | Driver não carregado | Confirme `Class.forName("com.mysql.cj.jdbc.Driver")` e presença do JAR no classpath |

---

## 🧪 Inserindo Usuário Inicial (para teste)

```sql
INSERT INTO usuarios (nome, login, senha)
VALUES ('Administrador', 'admin', '1234');
```

---

## 👨‍💻 Autor

**Bruno Bonaita dos Santos**  
📅 Novembro de 2025  
🎓 Projeto acadêmico — Avaliação A3  
💡 Tema: *ODS 9 — Indústria, Inovação e Infraestrutura*  
📚 Sistema de Controle de Manutenção Preventiva

---

## 🪪 Licença

Este projeto é de uso **educacional** e pode ser modificado livremente, desde que mantidos os créditos originais.

**Licença sugerida:** MIT (opcional — adicione um `LICENSE` se desejar).

---

## 💡 Justificativa e Impacto

> O sistema visa reduzir paradas não planejadas em processos industriais por meio de um controle simples e eficiente de manutenções preventivas.  
> Ao facilitar o agendamento e o registro das intervenções em máquinas, o projeto contribui para a **confiabilidade das infraestruturas industriais**, reduz custos com manutenção corretiva e apoia a transformação digital de pequenas e médias indústrias — tudo alinhado à **ODS 9**.
