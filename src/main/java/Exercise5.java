import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author u244353 (Sabria Karim)
 * @since 10/13/2025
 */
public class Exercise5 {

    static int i = 0;

    public static void main(String[] args) {
        System.out.println("test");

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                g.setColor(Color.BLACK);
                g.clearRect(0, 0, this.getWidth(), this.getHeight());
                g.drawPolygon(new int[]{50, 100, 200}, new int[]{50, 100, 50 + i}, 3);
                repaint();
                if (i > 50) {
                    i -= 50;
                } else {
                    i += 50;
                }
            }
        };

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);


    }
}
