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

    // --- Contador de tentativas ---
    private int tentativas = 0;
    private final int LIMITE_TENTATIVAS = 3;

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

        // Permitir ENTER para logar
        getRootPane().setDefaultButton(btnEntrar);

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

        // 2. Validar campos
        if (loginDigitado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O campo 'Login' não pode estar vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtLogin.requestFocus();
            return;
        }

        if (senhaDigitada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O campo 'Senha' não pode estar vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtSenha.requestFocus();
            return;
        }

        // 3. Verificar se ainda há tentativas
        if (tentativas >= LIMITE_TENTATIVAS) {
            JOptionPane.showMessageDialog(this,
                    "Número máximo de tentativas atingido. O sistema será encerrado.",
                    "Acesso Bloqueado",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // 4. Consultar no banco
        String sql = "SELECT * FROM usuarios WHERE login = ? AND senha = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setString(1, loginDigitado);
            pstmt.setString(2, senhaDigitada);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    String nomeUsuario = rs.getString("nome_completo");
                    boolean primeiroAcesso = rs.getBoolean("primeiro_acesso");

                    if (primeiroAcesso) {
                        JOptionPane.showMessageDialog(this,
                                "Olá, " + nomeUsuario + "!\nEsse é seu primeiro acesso.\nPor favor, altere sua senha.",
                                "Primeiro Acesso", JOptionPane.INFORMATION_MESSAGE);

                        TelaTrocaSenha telaTroca = new TelaTrocaSenha(rs.getInt("id_usuario"));
                        telaTroca.setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Login bem-sucedido! Bem-vindo, " + nomeUsuario,
                                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                        TelaGestaoMaquinas TelaMaquinas = new TelaGestaoMaquinas();
                        TelaMaquinas.setVisible(true);
                        this.dispose();
                    }
                } else {
                    // Falha
                    tentativas++;
                    int tentativasRestantes = LIMITE_TENTATIVAS - tentativas;

                    if (tentativasRestantes > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Usuário ou senha inválidos.\nTentativas restantes: " + tentativasRestantes,
                                "Erro de Login",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Número máximo de tentativas atingido.\nO sistema será encerrado.",
                                "Acesso Bloqueado",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar ao banco de dados:\n" + ex.getMessage(),
                    "Erro de Conexão",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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