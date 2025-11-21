package telas;

import conexao.ConexaoMySQL;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TelaTrocaSenha extends JFrame {
    private JPanel painel;
    private JLabel lblNovaSenha, lblConfirmarSenha;
    private JPasswordField txtNovaSenha, txtConfirmarSenha;
    private JButton btnSalvar;
    private int idUsuario;

    public TelaTrocaSenha(int idUsuario) {
        this.idUsuario = idUsuario;
        setTitle("Trocar Senha");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        painel = new JPanel();
        painel.setLayout(null);
        add(painel);

        lblNovaSenha = new JLabel("Nova Senha:");
        lblNovaSenha.setBounds(50, 50, 120, 25);
        painel.add(lblNovaSenha);

        txtNovaSenha = new JPasswordField();
        txtNovaSenha.setBounds(180, 50, 150, 25);
        painel.add(txtNovaSenha);

        lblConfirmarSenha = new JLabel("Confirmar Senha:");
        lblConfirmarSenha.setBounds(50, 90, 120, 25);
        painel.add(lblConfirmarSenha);

        txtConfirmarSenha = new JPasswordField();
        txtConfirmarSenha.setBounds(180, 90, 150, 25);
        painel.add(txtConfirmarSenha);

        btnSalvar = new JButton("Salvar");
        btnSalvar.setBounds(140, 140, 100, 30);
        painel.add(btnSalvar);

        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trocarSenha();
            }
        });
    }

    private void trocarSenha() {
        String novaSenha = new String(txtNovaSenha.getPassword()).trim();
        String confirmarSenha = new String(txtConfirmarSenha.getPassword()).trim();

        if (novaSenha.isEmpty() || confirmarSenha.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE usuarios SET senha = ?, primeiro_acesso = FALSE WHERE id_usuario = ?";

        try (Connection conexao = ConexaoMySQL.getConexao();
             PreparedStatement pstmt = conexao.prepareStatement(sql)) {

            pstmt.setString(1, novaSenha);
            pstmt.setInt(2, idUsuario);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Senha alterada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Voltar para a tela principal
            TelaGestaoMaquinas tela = new TelaGestaoMaquinas();
            tela.setVisible(true);
            this.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar senha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

}
