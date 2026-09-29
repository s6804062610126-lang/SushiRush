import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import javax.imageio.ImageIO;

public class Sprite {
    public final Image image;
    public final int sx;
    public final int sy;
    public final int sw;
    public final int sh;

    public Sprite(Image image, int sx, int sy, int sw, int sh) {
        this.image = image;
        this.sx = sx;
        this.sy = sy;
        this.sw = Math.max(1, sw);
        this.sh = Math.max(1, sh);
    }

    public static Sprite load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            File png = new File(path.replace(".jpg", ".png"));
            if (png.exists()) {
                file = png;
            } else {
                file = new File("..", path);
            }
        }
        try {
            BufferedImage raw = ImageIO.read(file);
            if (raw == null) {
                return new Sprite(null, 0, 0, 1, 1);
            }
            int[] box = contentBox(raw);
            return new Sprite(raw, box[0], box[1], box[2], box[3]);
        } catch (Exception e) {
            System.out.println("โหลดรูปไม่สำเร็จ: " + path);
            return new Sprite(null, 0, 0, 1, 1);
        }
    }

    public void draw(Graphics g, ImageObserver obs, int x, int y, int w, int h) {
        if (image == null) {
            return;
        }
        g.drawImage(image, x, y, x + w, y + h, sx, sy, sx + sw, sy + sh, obs);
    }

    public void drawFull(Graphics g, ImageObserver obs, int x, int y, int w, int h) {
        if (image == null) {
            return;
        }
        g.drawImage(image, x, y, w, h, obs);
    }

    private static int[] contentBox(BufferedImage im) {
        int w = im.getWidth();
        int h = im.getHeight();
        int minX = w;
        int minY = h;
        int maxX = 0;
        int maxY = 0;
        int step = 4;
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int a = (im.getRGB(x, y) >>> 24) & 0xFF;
                if (a > 12) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
        if (maxX <= minX || maxY <= minY) {
            return new int[] {0, 0, w, h};
        }
        int pad = 6;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);
        return new int[] {minX, minY, maxX - minX, maxY - minY};
    }
}
