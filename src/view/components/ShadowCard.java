package view.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ShadowCard - estilo Windows 11
 * Card branco, borda azul, sombra leve e hover azul suave.
 */
public class ShadowCard extends JPanel {

    private Color backgroundColor = Color.WHITE;
    private Color borderColor = new Color(0, 120, 215); // Azul Microsoft
    private Color hoverColor = new Color(229, 243, 255); // Azul bem claro
    private Color shadowColor = new Color(0, 0, 0, 40);

    private boolean hovered = false;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JComponent iconComponent;

    public ShadowCard(String title, String subtitle, JComponent icon) {

        this.iconComponent = icon;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(300, 360));

        // Título
        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.BLACK);

        // Subtítulo
        subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(80, 80, 80));

        // Wrap central
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        if (iconComponent != null) {
            iconComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(iconComponent);
            center.add(Box.createRigidArea(new Dimension(0, 18)));
        }

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(titleLabel);
        center.add(Box.createVerticalGlue());

        add(center, BorderLayout.CENTER);

        subtitleLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        add(subtitleLabel, BorderLayout.SOUTH);

        // Efeitos de hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        int arc = 20;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sombra
        g2.setColor(shadowColor);
        g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, arc, arc);

        // Fundo do card
        g2.setColor(hovered ? hoverColor : backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

        // Borda azul
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
