package telas;

import conexao.ConexaoMySQL;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



// 1. A classe TelaLogin é um 'JFrame' (uma janela)
public class TelaLogin extends JFrame {

    // --- Componentes da Tela ---
    private JPanel painelPrincipal;
    private JLabel lblLogin;
    private JTextField txtLogin;
    private JLabel lblSenha;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnSair;

    // --- Construtor da Classe ---
    // É executado quando a tela é "criada" (ex: new TelaLogin())
    public TelaLogin() {
        // --- Configurações básicas da Janela ---
        setTitle("Login - Sistema de Manutenção");
        setSize(400, 250); // Largura e altura da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao clicar no 'X'
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // --- Painel Principal ---
        // Usaremos layout 'null' para posicionar os componentes manualmente (com setBounds)
        painelPrincipal = new JPanel();
        painelPrincipal.setLayout(null);
        add(painelPrincipal); // Adiciona o painel à janela

        // --- Componente: Rótulo "Login:" ---
        lblLogin = new JLabel("Login:");
        lblLogin.setBounds(50, 40, 80, 25); // (x, y, largura, altura)
        painelPrincipal.add(lblLogin);

        // --- Componente: Campo de Texto "Login" ---
        txtLogin = new JTextField();
        txtLogin.setBounds(130, 40, 200, 25);
        painelPrincipal.add(txtLogin);

        // --- Componente: Rótulo "Senha:" ---
        lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(50, 80, 80, 25);
        painelPrincipal.add(lblSenha);

        // --- Componente: Campo de Senha "Senha" ---
        txtSenha = new JPasswordField();
        txtSenha.setBounds(130, 80, 200, 25);
        painelPrincipal.add(txtSenha);

        // --- Componente: Botão "Entrar" ---
        btnEntrar = new JButton("Entrar");
        btnEntrar.setBounds(130, 130, 90, 30);
        painelPrincipal.add(btnEntrar);

        // --- Componente: Botão "Sair" ---
        btnSair = new JButton("Sair");
        btnSair.setBounds(240, 130, 90, 30);
        painelPrincipal.add(btnSair);

        // --- 2. Action Listener: O que acontece quando os botões são clicados ---

        // Ação do Botão ENTRAR
        btnEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Chama o método que faz a verificação no banco
                verificarLogin();
            }
        });

        // Ação do Botão SAIR
        btnSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fecha a aplicação
                System.exit(0);
            }
        });
    }

    // --- 3. Método para verificar o login no Banco de Dados ---
    private void verificarLogin() {
        // Pega o que foi digitado pelo usuário
        String loginDigitado = txtLogin.getText();
        String senhaDigitada = new String(txtSenha.getPassword());

        // Prepara a consulta SQL
        // Usamos 'PreparedStatement' (com '?') para evitar SQL Injection
        String sql = "SELECT * FROM usuarios WHERE login = ? AND senha = ?";

        Connection conexao = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Pega uma conexão do nosso arquivo ConexaoMySQL.java

            conexao = ConexaoMySQL.getConexao();

            // Prepara a consulta
            pstmt = conexao.prepareStatement(sql);
            pstmt.setString(1, loginDigitado); // Substitui o primeiro '?' pelo login
            pstmt.setString(2, senhaDigitada); // Substitui o segundo '?' pela senha

            // Executa a consulta
            rs = pstmt.executeQuery();

            // Verifica se o banco de dados retornou algum resultado
            if (rs.next()) {
                // --- LOGIN BEM-SUCEDIDO ---
                // Pega o nome do usuário no banco
                String nomeUsuario = rs.getString("nome_completo");

                JOptionPane.showMessageDialog(this, "Login bem-sucedido! Bem-vindo, " + nomeUsuario);

                // Abre a Tela 2 (Gestão de Máquinas)
                // (Esta tela ainda não criamos, mas já deixamos a chamada aqui)
                TelaGestaoMaquinas TelaMaquinas = new TelaGestaoMaquinas();
                TelaMaquinas.setVisible(true);

                // Fecha esta tela de login
                this.dispose();

            } else {
                // --- LOGIN FALHOU ---
                JOptionPane.showMessageDialog(this, "Login ou senha inválidos.", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            // Erro ao tentar conectar ou executar a consulta
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco de dados: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            // --- 4. Fecha todos os recursos (conexão, statement, resultset) ---
            // Isso é MUITO importante para não consumir recursos do banco
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conexao != null) conexao.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    // --- 5. Método 'main' para executar esta tela ---
    // (Opcional, mas bom para testar a tela individualmente)
    public static void main(String[] args) {
        // Cria e exibe a tela de login
        TelaLogin tela = new TelaLogin();
        tela.setVisible(true);
    }
}