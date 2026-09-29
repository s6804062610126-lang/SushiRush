import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class FX {
    static class FloatText {
        String text;
        double x;
        double y;
        double life;
        Color color;

        FloatText(String text, double x, double y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.life = 0.9;
            this.color = color;
        }
    }

    static class Spark {
        double x;
        double y;
        double vx;
        double vy;
        double life;
        Color color;

        Spark(double x, double y, Color color, Random rand) {
            this.x = x;
            this.y = y;
            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 40 + rand.nextDouble() * 90;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed - 40;
            this.life = 0.35 + rand.nextDouble() * 0.35;
            this.color = color;
        }
    }

    private final ArrayList<FloatText> texts = new ArrayList<>();
    private final ArrayList<Spark> sparks = new ArrayList<>();
    private final Random rand = new Random();

    public void burst(int x, int y, boolean good) {
        Color color = good ? new Color(120, 230, 120) : new Color(255, 90, 90);
        int n = good ? 16 : 10;
        for (int i = 0; i < n; i++) {
            sparks.add(new Spark(x, y, color, rand));
        }
    }

    public void popup(String text, int x, int y, boolean good) {
        Color color = good ? new Color(255, 240, 140) : new Color(255, 160, 160);
        texts.add(new FloatText(text, x, y, color));
    }

    public void update(double dt) {
        Iterator<FloatText> ti = texts.iterator();
        while (ti.hasNext()) {
            FloatText t = ti.next();
            t.y -= 55 * dt;
            t.life -= dt;
            if (t.life <= 0) {
                ti.remove();
            }
        }
        Iterator<Spark> si = sparks.iterator();
        while (si.hasNext()) {
            Spark s = si.next();
            s.x += s.vx * dt;
            s.y += s.vy * dt;
            s.vy += 180 * dt;
            s.life -= dt;
            if (s.life <= 0) {
                si.remove();
            }
        }
    }

    public void draw(Graphics g, Font font) {
        for (Spark s : sparks) {
            int a = (int) Math.max(0, Math.min(255, s.life * 500));
            g.setColor(new Color(s.color.getRed(), s.color.getGreen(), s.color.getBlue(), a));
            g.fillOval((int) s.x, (int) s.y, 6, 6);
        }
        g.setFont(font);
        for (FloatText t : texts) {
            int a = (int) Math.max(0, Math.min(255, t.life * 280));
            g.setColor(new Color(t.color.getRed(), t.color.getGreen(), t.color.getBlue(), a));
            g.drawString(t.text, (int) t.x, (int) t.y);
        }
    }

    public void clear() {
        texts.clear();
        sparks.clear();
    }
}
