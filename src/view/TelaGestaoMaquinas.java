package view;

import controller.MaquinaController;
import model.Maquina;
import model.Usuario;
import security.Permission;
import security.SecurityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TelaGestaoMaquinas extends JFrame {

    private final Usuario usuario;
    private final MaquinaController controller = new MaquinaController();

    private JTable tabela;
    private DefaultTableModel model;

    public TelaGestaoMaquinas(Usuario usuario) {
        this.usuario = usuario;

        SecurityService sec = new SecurityService();
        if (!sec.roleHasPermission(usuario.getRole(), Permission.VIEW_MACHINES)) {
            JOptionPane.showMessageDialog(null,
                    "Você não tem permissão para acessar Gestão de Máquinas.",
                    "Acesso Negado",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setTitle("Gestão de Máquinas");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // === ROOT (Fundo Windows 11) ===
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === TÍTULO ===
        JLabel titulo = new JLabel("Gestão de Máquinas");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel sub = new JLabel("Visualize, cadastre e atualize suas máquinas");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        header.add(sub);

        root.add(header, BorderLayout.NORTH);

        // === PAINEL DE BOTÕES ===
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        botoes.setOpaque(false);

        JButton btnNovo = criarBotao("Nova Máquina");
        JButton btnEditar = criarBotao("Editar");
        JButton btnExcluir = criarBotao("Excluir");

        botoes.add(btnNovo);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);

        root.add(botoes, BorderLayout.SOUTH);

        // Permissões
        boolean canEdit = sec.roleHasPermission(usuario.getRole(), Permission.EDIT_MACHINE);
        boolean canDelete = sec.roleHasPermission(usuario.getRole(), Permission.DELETE_MACHINE);

        btnEditar.setEnabled(canEdit);
        btnExcluir.setEnabled(canDelete);
        btnNovo.setEnabled(canEdit || sec.roleHasPermission(usuario.getRole(), Permission.CREATE_USER));

        // === TABELA ===
        model = new DefaultTableModel(new Object[]{
                "ID", "Equipamento", "Setor", "Data de Aquisição"
        }, 0);

        tabela = new JTable(model);
        tabela.setRowHeight(28);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setSelectionBackground(new Color(0, 120, 215));
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setGridColor(new Color(210, 210, 210));

        // Estilo do cabeçalho
        JTableHeader headerTb = tabela.getTableHeader();
        headerTb.setBackground(new Color(230, 230, 230));
        headerTb.setForeground(new Color(50, 50, 50));
        headerTb.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        root.add(scroll, BorderLayout.CENTER);

        // === EVENTOS ===
        btnNovo.addActionListener(e -> novaMaquina());
        btnEditar.addActionListener(e -> editarMaquina());
        btnExcluir.addActionListener(e -> excluirMaquina());

        carregarTabela();

        setContentPane(root);
    }

    // ======== CRIA BOTÃO WINDOWS 11 ==========
    private JButton criarBotao(String texto) {
        JButton btn = new JButton(texto);

        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 120, 215));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(20, 140, 235));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 120, 215));
            }
        });

        return btn;
    }

    private void carregarTabela() {
        model.setRowCount(0);

        List<Maquina> lista = controller.listarTodas();

        for (Maquina m : lista) {
            model.addRow(new Object[]{
                    m.getId(),
                    m.getNome(),
                    m.getLocal(),
                    m.getDataAquisicao()
            });
        }
    }

    private void novaMaquina() {
        SecurityService sec = new SecurityService();
        if (!sec.roleHasPermission(usuario.getRole(), Permission.EDIT_MACHINE)
                && !sec.roleHasPermission(usuario.getRole(), Permission.CREATE_USER)) {

            JOptionPane.showMessageDialog(this, "Você não tem permissão para criar máquinas.");
            return;
        }

        JTextField txtNome = new JTextField();
        JTextField txtSetor = new JTextField();
        JTextField txtData = new JTextField(LocalDate.now().toString());

        Object[] campos = {
                "Nome do Equipamento:", txtNome,
                "Setor:", txtSetor,
                "Data de Aquisição:", txtData
        };

        int op = JOptionPane.showConfirmDialog(this, campos, "Nova Máquina", JOptionPane.OK_CANCEL_OPTION);

        if (op == JOptionPane.OK_OPTION) {
            try {
                Maquina m = new Maquina();
                m.setNome(txtNome.getText());
                m.setLocal(txtSetor.getText());
                m.setDataAquisicao(LocalDate.parse(txtData.getText()));

                controller.inserir(m, usuario);
                carregarTabela();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir: " + e.getMessage());
            }
        }
    }

    private void editarMaquina() {
        int row = tabela.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma máquina.");
            return;
        }

        int id = (int) tabela.getValueAt(row, 0);
        String nomeAtual = tabela.getValueAt(row, 1).toString();
        String setorAtual = tabela.getValueAt(row, 2).toString();
        String dataAtual = tabela.getValueAt(row, 3).toString();

        JTextField txtNome = new JTextField(nomeAtual);
        JTextField txtSetor = new JTextField(setorAtual);
        JTextField txtData = new JTextField(dataAtual);

        Object[] campos = {
                "Nome do Equipamento:", txtNome,
                "Setor:", txtSetor,
                "Data de Aquisição:", txtData
        };

        int op = JOptionPane.showConfirmDialog(this, campos, "Editar Máquina", JOptionPane.OK_CANCEL_OPTION);

        if (op == JOptionPane.OK_OPTION) {
            try {
                Maquina m = new Maquina();
                m.setId(id);
                m.setNome(txtNome.getText());
                m.setLocal(txtSetor.getText());
                m.setDataAquisicao(LocalDate.parse(txtData.getText()));

                controller.atualizar(m, usuario);
                carregarTabela();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());
            }
        }
    }

    private void excluirMaquina() {
        int row = tabela.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma máquina.");
            return;
        }

        int id = (int) tabela.getValueAt(row, 0);

        int op = JOptionPane.showConfirmDialog(this, "Deseja excluir esta máquina?", "Excluir", JOptionPane.YES_NO_OPTION);

        if (op == JOptionPane.YES_OPTION) {
            try {
                controller.excluir(id, usuario);
                carregarTabela();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage());
            }
        }
    }
}
