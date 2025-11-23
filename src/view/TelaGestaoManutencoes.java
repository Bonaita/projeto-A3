package view;

import controller.ManutencaoController;
import model.Manutencao;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TelaGestaoManutencoes extends JFrame {

    private final ManutencaoController controller = new ManutencaoController();
    private JTable tabela;
    private DefaultTableModel model;
    private JTextField txtIdMaquina;

    private final Usuario usuario;

    public TelaGestaoManutencoes(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Gestão de Manutenções");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ROOT
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 245, 245));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        // ===== CABEÇALHO =====
        JLabel titulo = new JLabel("Gestão de Manutenções");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(new Color(0, 120, 215));

        JLabel subtitulo = new JLabel("Gerencie as manutenções da sua frota");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 90, 90));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(titulo);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(subtitulo);

        root.add(header, BorderLayout.NORTH);

        // ===== TOPO (FILTRO) =====
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topo.setOpaque(false);

        topo.add(new JLabel("ID Máquina:"));
        txtIdMaquina = new JTextField(8);
        estilizarCampo(txtIdMaquina);
        topo.add(txtIdMaquina);

        JButton btnCarregar = criarBotao("Carregar");
        topo.add(btnCarregar);

        root.add(topo, BorderLayout.BEFORE_FIRST_LINE);

        // ===== TABELA =====
        model = new DefaultTableModel(new Object[]{
                "ID", "Data", "Tipo", "Status", "Observações"
        }, 0);

        tabela = new JTable(model);
        tabela.setRowHeight(28);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setSelectionBackground(new Color(0, 120, 215));
        tabela.setSelectionForeground(Color.WHITE);

        JTableHeader headerTb = tabela.getTableHeader();
        headerTb.setBackground(new Color(230, 230, 230));
        headerTb.setForeground(new Color(50, 50, 50));
        headerTb.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        root.add(scroll, BorderLayout.CENTER);

        // ===== BOTÕES INFERIORES =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);

        JButton btnNovo = criarBotao("Novo");
        JButton btnEditar = criarBotao("Editar");
        JButton btnExcluir = criarBotao("Excluir");

        bottom.add(btnNovo);
        bottom.add(btnEditar);
        bottom.add(btnExcluir);

        root.add(bottom, BorderLayout.SOUTH);

        // ===== AÇÕES =====
        btnCarregar.addActionListener(e -> carregar());
        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
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

    // ======== ESTILO DE CAMPOS ==========
    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(new Color(245, 245, 245));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    // ======== LÓGICA ========

    private void carregar() {
        model.setRowCount(0);

        try {
            List<Manutencao> lista;

            if (txtIdMaquina.getText().isBlank()) {
                lista = controller.listarTodas();
            } else {
                int id = Integer.parseInt(txtIdMaquina.getText());
                lista = controller.listarPorMaquina(id);
            }

            for (Manutencao m : lista) {
                model.addRow(new Object[]{
                        m.getId(),
                        m.getDataAgendada(),
                        m.getTipo(),
                        m.getStatus(),
                        m.getObservacoes()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
        }
    }

    private void novo() {
        // Combos para menus suspensos
        String[] tipos = {"Preventiva", "Corretiva", "Limpeza", "Atualização", "Ajuste"};
        JComboBox<String> cbTipo = new JComboBox<>(tipos);

        String[] status = {"Pendente", "Agendada", "Em andamento", "Concluída", "Cancelada"};
        JComboBox<String> cbStatus = new JComboBox<>(status);

        JTextField txtData = new JTextField(LocalDate.now().toString());
        JTextArea txtObs = new JTextArea(5, 20);

        Object[] campos = {
                "Data (yyyy-mm-dd):", txtData,
                "Tipo:", cbTipo,
                "Status:", cbStatus,
                "Observações:", new JScrollPane(txtObs)
        };

        int op = JOptionPane.showConfirmDialog(
                this, campos, "Nova Manutenção", JOptionPane.OK_CANCEL_OPTION
        );

        if (op == JOptionPane.OK_OPTION) {
            try {
                Manutencao m = new Manutencao();
                m.setIdMaquina(Integer.parseInt(txtIdMaquina.getText()));
                m.setDataAgendada(LocalDate.parse(txtData.getText()));
                m.setTipo(cbTipo.getSelectedItem().toString());
                m.setStatus(cbStatus.getSelectedItem().toString());
                m.setObservacoes(txtObs.getText());

                controller.inserir(m, usuario.getId());
                carregar();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        }
    }

    private void editar() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção.");
            return;
        }

        int id = (int) tabela.getValueAt(row, 0);

        JTextField txtData = new JTextField(tabela.getValueAt(row, 1).toString());

        // Combo para tipo
        String[] tipos = {"Preventiva", "Corretiva", "Limpeza", "Atualização", "Ajuste"};
        JComboBox<String> cbTipo = new JComboBox<>(tipos);
        cbTipo.setSelectedItem(tabela.getValueAt(row, 2).toString());

        // Combo para status
        String[] status = {"Pendente", "Agendada", "Em andamento", "Concluída", "Cancelada"};
        JComboBox<String> cbStatus = new JComboBox<>(status);
        cbStatus.setSelectedItem(tabela.getValueAt(row, 3).toString());

        JTextArea txtObs = new JTextArea(tabela.getValueAt(row, 4).toString());

        Object[] campos = {
                "Data:", txtData,
                "Tipo:", cbTipo,
                "Status:", cbStatus,
                "Observações:", new JScrollPane(txtObs)
        };

        int op = JOptionPane.showConfirmDialog(
                this, campos, "Editar Manutenção", JOptionPane.OK_CANCEL_OPTION
        );

        if (op == JOptionPane.OK_OPTION) {
            try {
                Manutencao m = new Manutencao();
                m.setId(id);
                m.setIdMaquina(Integer.parseInt(txtIdMaquina.getText()));
                m.setDataAgendada(LocalDate.parse(txtData.getText()));
                m.setTipo(cbTipo.getSelectedItem().toString());
                m.setStatus(cbStatus.getSelectedItem().toString());
                m.setObservacoes(txtObs.getText());

                controller.atualizar(m, usuario.getId());
                carregar();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        }
    }

    private void excluir() {
        int row = tabela.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma manutenção.");
            return;
        }

        int id = (int) tabela.getValueAt(row, 0);

        int op = JOptionPane.showConfirmDialog(
                this, "Excluir manutenção?", "Confirmar", JOptionPane.YES_NO_OPTION
        );

        if (op == JOptionPane.YES_OPTION) {
            try {
                controller.excluir(id, usuario.getId());
                carregar();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        }
    }
}
