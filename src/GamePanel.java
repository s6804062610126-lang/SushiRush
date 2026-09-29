import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    static final int MENU = 0;
    static final int PLAYING = 1;
    static final int GAME_OVER = 2;

    static final int MAX_CUSTOMERS = 3;
    static final int STAND_Y = 150;
    static final int[] SLOT_X = {100, 300, 500};

    static final int PLATE_X = 592;
    static final int PLATE_Y = 508;
    static final int PLATE_W = 68;
    static final int PLATE_H = 46;

    final int screenWidth = 800;
    final int screenHeight = 600;

    Sprite bgImage;
    Sprite tableImage;
    Sprite matImage;
    Sprite plateImage;
    Sprite riceImage;
    Sprite fishTrayImage;
    Sprite seaweedImage;
    Sprite trashImage;
    Sprite carpetImage;
    Sprite knifeImage;
    Sprite wasabiImage;
    Sprite shoyuImage;
    Sprite gingerImage;
    Sprite sushiSalmonImage;
    Sprite sushiTunaImage;
    Sprite sushiTamagoImage;
    Sprite sushiMakiImage;
    Sprite riceBallImage;
    Sprite matSeaweedImage;
    Sprite matSeaweedRiceImage;
    Sprite startButton;
    Sprite playAgainButton;
    Sprite checkIcon;
    Sprite warnIcon;
    Sprite[] girlSprites;
    Sprite[] guySprites;
    Sprite[] orderBubbles;
    Sprite[] patienceBars;

    Font pixelFont;
    Font pixelSmall;

    Customer[] customers = new Customer[MAX_CUSTOMERS];

    boolean hasRice = false;
    boolean hasSalmon = false;
    boolean hasTuna = false;
    boolean hasTamago = false;
    boolean hasSeaweed = false;

    int gameState = MENU;
    int score = 0;
    int lives = 3;
    int feedbackTicks = 0;
    boolean lastServeCorrect = false;
    double spawnCooldown = 0;

    int combo = 0;
    int bestCombo = 0;
    double animTime = 0;
    double sushiPulse = 0;
    double flashWrong = 0;
    FX fx = new FX();
    boolean dragging = false;
    int dragType = Sushi.NONE;
    int dragX;
    int dragY;

    Timer timer;
    long lastNanos;

    public GamePanel() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        loadAssets();

        timer = new Timer(16, this);
        lastNanos = System.nanoTime();
        timer.start();
    }

    private double currentPatience() {
        double patience = 16.5 - (score / 120.0);
        if (patience < 6.5) {
            patience = 6.5;
        }
        return patience;
    }

    private void loadAssets() {
        bgImage = Sprite.load("assets/images/background.png");
        tableImage = Sprite.load("assets/images/table.png");
        matImage = Sprite.load("assets/images/18.png");
        plateImage = Sprite.load("assets/images/6.png");
        riceImage = Sprite.load("assets/images/2.png");
        fishTrayImage = Sprite.load("assets/images/3.png");
        seaweedImage = Sprite.load("assets/images/11.png");
        trashImage = Sprite.load("assets/images/Trash.png");
        carpetImage = Sprite.load("assets/images/carpet.png");
        knifeImage = Sprite.load("assets/images/knife.png");
        wasabiImage = Sprite.load("assets/images/wasabi.png");
        shoyuImage = Sprite.load("assets/images/shoyu.png");
        gingerImage = Sprite.load("assets/images/ginger.png");
        sushiSalmonImage = Sprite.load("assets/images/salmonsushi.png");
        sushiTunaImage = Sprite.load("assets/images/13.png");
        sushiTamagoImage = Sprite.load("assets/images/14.png");
        sushiMakiImage = Sprite.load("assets/images/15.png");
        riceBallImage = Sprite.load("assets/images/16.png");
        matSeaweedImage = Sprite.load("assets/images/17.png");
        matSeaweedRiceImage = Sprite.load("assets/images/19.png");
        startButton = Sprite.load("assets/images/27.png");
        playAgainButton = Sprite.load("assets/images/28.png");
        checkIcon = Sprite.load("assets/images/29.png");
        warnIcon = Sprite.load("assets/images/30.png");

        girlSprites = new Sprite[] {
            Sprite.load("assets/images/20.png"),
            Sprite.load("assets/images/21.png"),
            Sprite.load("assets/images/22.png")
        };
        guySprites = new Sprite[] {
            Sprite.load("assets/images/23.png"),
            Sprite.load("assets/images/24.png"),
            Sprite.load("assets/images/25.png")
        };
        orderBubbles = new Sprite[] {
            Sprite.load("assets/images/46.png"),
            Sprite.load("assets/images/47.png"),
            Sprite.load("assets/images/48.png"),
            Sprite.load("assets/images/49.png")
        };
        patienceBars = new Sprite[11];
        for (int i = 0; i < 11; i++) {
            patienceBars[i] = Sprite.load("assets/images/" + (31 + i) + ".png");
        }

        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonds/PressStart2P-Regular.ttf")).deriveFont(16f);
            pixelSmall = pixelFont.deriveFont(11f);
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.BOLD, 18);
            pixelSmall = new Font("Monospaced", Font.BOLD, 13);
        }
    }

    private void startGame() {
        gameState = PLAYING;
        score = 0;
        lives = 3;
        combo = 0;
        bestCombo = 0;
        feedbackTicks = 0;
        flashWrong = 0;
        fx.clear();
        dragging = false;
        spawnCooldown = 0.25;
        clearStation();
        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            customers[i] = null;
        }
        spawnCustomer();
    }

    private int occupiedCount() {
        int n = 0;
        for (Customer c : customers) {
            if (c != null) {
                n++;
            }
        }
        return n;
    }

    private void spawnCustomer() {
        if (occupiedCount() >= MAX_CUSTOMERS) {
            return;
        }
        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            if (customers[i] == null) {
                spawnInSlot(i);
                return;
            }
        }
    }

    private void spawnInSlot(int slot) {
        customers[slot] = new Customer(slot, SLOT_X[slot], STAND_Y, currentPatience(),
                girlSprites, guySprites, orderBubbles, patienceBars);
    }

    private void clearStation() {
        hasRice = false;
        hasSalmon = false;
        hasTuna = false;
        hasTamago = false;
        hasSeaweed = false;
        dragging = false;
        dragType = Sushi.NONE;
    }

    private int madeType() {
        return Sushi.typeFromIngredients(hasRice, hasSeaweed, hasSalmon, hasTuna, hasTamago);
    }

    private Sprite sushiSprite(int type) {
        if (type == Sushi.MAKI) {
            return sushiMakiImage;
        }
        if (type == Sushi.SALMON) {
            return sushiSalmonImage;
        }
        if (type == Sushi.TUNA) {
            return sushiTunaImage;
        }
        if (type == Sushi.TAMAGO) {
            return sushiTamagoImage;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;
        if (dt > 0.05) {
            dt = 0.05;
        }
        updateGame(dt);
        repaint();
    }

    private void updateGame(double dt) {
        animTime += dt;
        fx.update(dt);
        if (flashWrong > 0) {
            flashWrong -= dt;
        }
        sushiPulse += dt;
        if (feedbackTicks > 0) {
            feedbackTicks--;
        }
        if (gameState == MENU) {
            return;
        }
        if (gameState != PLAYING) {
            return;
        }

        spawnCooldown -= dt;
        if (spawnCooldown <= 0 && occupiedCount() < MAX_CUSTOMERS) {
            spawnCustomer();
            spawnCooldown = 0.9;
        }

        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            Customer c = customers[i];
            if (c == null) {
                continue;
            }
            c.update(dt);
            if (c.timedOut) {
                c.timedOut = false;
                combo = 0;
                lives--;
                lastServeCorrect = false;
                feedbackTicks = 40;
                flashWrong = 0.25;
                fx.burst(c.drawX() + 70, c.drawY() + 80, false);
                fx.popup("LEFT!", c.drawX() + 20, c.drawY() + 40, false);
                if (lives <= 0) {
                    gameState = GAME_OVER;
                }
            }
            if (c.isGone()) {
                spawnInSlot(i);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (bgImage != null) {
            bgImage.draw(g, this, 0, 0, screenWidth, 430);
        }

        if (gameState != MENU) {
            Customer hover = dragging ? customerAt(dragX, dragY) : null;
            for (Customer c : customers) {
                if (c != null) {
                    c.draw(g, this, c == hover);
                }
            }
        }

        if (tableImage != null) {
            tableImage.drawFull(g, this, -70, 100, screenWidth + 200, 830);
        }
        if (carpetImage != null) {
            carpetImage.drawFull(g, this, 290, 450, 520, 160);
        }

        int made = madeType();
        boolean sushiDone = made != Sushi.NONE;

        if (hasSeaweed && hasRice && !sushiDone) {
            matSeaweedRiceImage.drawFull(g, this, 340, 440, 300, 180);
        } else if (hasSeaweed && !hasRice && !sushiDone) {
            matSeaweedImage.drawFull(g, this, 340, 440, 300, 180);
        } else {
            matImage.drawFull(g, this, 340, 440, 300, 180);
        }

        if (hasRice && !hasSeaweed && !sushiDone && riceBallImage != null) {
            riceBallImage.drawFull(g, this, 420, 480, 120, 90);
        }
        if (plateImage != null) {
            plateImage.drawFull(g, this, 470, 440, 320, 180);
        }
        if (knifeImage != null) {
            knifeImage.drawFull(g, this, 420, 440, 320, 180);
        }
        if (riceImage != null) {
            riceImage.drawFull(g, this, -170, 290, 530, 300);
        }
        if (wasabiImage != null) {
            wasabiImage.drawFull(g, this, 235, 400, 320, 180);
        }
        if (shoyuImage != null) {
            shoyuImage.drawFull(g, this, 235, 450, 320, 180);
        }
        if (gingerImage != null) {
            gingerImage.drawFull(g, this, 580, 425, 220, 130);
        }
        if (fishTrayImage != null) {
            fishTrayImage.drawFull(g, this, 80, 440, 300, 160);
        }
        if (seaweedImage != null) {
            seaweedImage.drawFull(g, this, 175, 440, 300, 160);
        }
        if (trashImage != null) {
            trashImage.drawFull(g, this, 570, 500, 400, 200);
        }

        if (sushiDone && !dragging) {
            Sprite spr = sushiSprite(made);
            if (spr != null) {
                double pulse = 1 + Math.sin(sushiPulse * 6) * 0.08;
                int w = (int) (PLATE_W * pulse);
                int h = (int) (PLATE_H * pulse);
                spr.draw(g, this, PLATE_X - (w - PLATE_W) / 2, PLATE_Y - (h - PLATE_H) / 2, w, h);
            }
        }

        if (dragging) {
            Sprite spr = sushiSprite(dragType);
            if (spr != null) {
                spr.draw(g, this, dragX - 34, dragY - 23, 68, 46);
            }
        }

        if (feedbackTicks > 0) {
            Sprite icon = lastServeCorrect ? checkIcon : warnIcon;
            if (icon != null) {
                double pop = 1 + Math.sin(feedbackTicks / 6.0) * 0.12;
                int s = (int) (64 * pop);
                icon.draw(g, this, 400 - s / 2, 220 - s / 2, s, s);
            }
        }

        fx.draw(g, pixelSmall);

        if (flashWrong > 0) {
            g.setColor(new Color(180, 20, 20, (int) (90 * flashWrong / 0.25)));
            g.fillRect(0, 0, screenWidth, screenHeight);
        }

        if (gameState == PLAYING) {
            drawHud(g);
        }
        if (gameState == MENU) {
            drawMenu(g);
        } else if (gameState == GAME_OVER) {
            drawEnd(g, "GAME OVER", true);
        }
    }

    private void drawHud(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(10, 8, 250, 58, 16, 16);
        g.setFont(pixelSmall);
        g.setColor(Color.WHITE);
        g.drawString("SCORE " + score, 22, 32);
        if (combo >= 2) {
            g.setColor(new Color(255, 220, 90));
            g.drawString("COMBO x" + combo, 22, 52);
        } else {
            g.setColor(new Color(200, 200, 200));
            g.drawString("COMBO x" + combo, 22, 52);
        }

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(620, 8, 168, 42, 16, 16);
        for (int i = 0; i < 3; i++) {
            double beat = 1 + (i < lives ? Math.sin(animTime * 4 + i) * 0.08 : 0);
            int s = (int) (20 * beat);
            g.setColor(i < lives ? new Color(220, 60, 70) : new Color(70, 70, 70));
            g.fillOval(644 + i * 42 - (s - 20) / 2, 18 - (s - 20) / 2, s, s);
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 155));
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setFont(pixelFont);
        g.setColor(new Color(255, 230, 160));
        int titleBob = (int) (Math.sin(animTime * 2.4) * 6);
        drawCentered(g, "SUSHI RUSH", 120 + titleBob);
        g.setFont(pixelSmall);
        g.setColor(Color.WHITE);
        drawCentered(g, "Keep serving. Customers never stop.", 158);
        int btnBob = (int) (Math.sin(animTime * 3) * 4);
        if (startButton != null) {
            startButton.draw(g, this, 255, 200 + btnBob, 290, 82);
        }
        g.setColor(new Color(230, 230, 230));
        drawCentered(g, "Max 3 customers  |  Combo for bonus", 320);
        drawCentered(g, "Rice + topping = nigiri", 350);
        drawCentered(g, "Seaweed + rice + topping = maki", 374);
        drawCentered(g, "Drag finished sushi onto a customer", 398);
        drawCentered(g, "Slow or wrong orders make them leave", 422);
        drawCentered(g, "Survive until you fall behind", 446);
    }

    private void drawEnd(Graphics g, String title, boolean showReplay) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setFont(pixelFont);
        g.setColor(new Color(255, 230, 160));
        drawCentered(g, title, 170);
        g.setFont(pixelSmall);
        g.setColor(Color.WHITE);
        drawCentered(g, "SCORE " + score, 220);
        if (bestCombo > 1) {
            drawCentered(g, "BEST COMBO x" + bestCombo, 246);
        }
        if (showReplay && playAgainButton != null) {
            playAgainButton.draw(g, this, 250, 270, 300, 90);
        }
    }

    private void drawCentered(Graphics g, String text, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (screenWidth - w) / 2, y);
    }

    private boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private Customer customerAt(int mx, int my) {
        for (Customer c : customers) {
            if (c != null && c.isWaiting() && c.contains(mx, my)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (gameState == MENU) {
            if (inBox(mouseX, mouseY, 255, 200, 290, 82)) {
                startGame();
            }
            return;
        }
        if (gameState == GAME_OVER) {
            if (inBox(mouseX, mouseY, 250, 270, 300, 90)) {
                startGame();
            }
            return;
        }
        if (gameState != PLAYING) {
            return;
        }

        int made = madeType();
        if (made != Sushi.NONE && inBox(mouseX, mouseY, PLATE_X, PLATE_Y, PLATE_W, PLATE_H)) {
            dragging = true;
            dragType = made;
            dragX = mouseX;
            dragY = mouseY;
            return;
        }

        if (inBox(mouseX, mouseY, 700, 500, 100, 100)) {
            clearStation();
            return;
        }
        if (inBox(mouseX, mouseY, 0, 290, 150, 300)) {
            if (!hasRice && made == Sushi.NONE) {
                hasRice = true;
            }
            return;
        }
        if (mouseX >= 80 && mouseX <= 300 && mouseY >= 440 && mouseY <= 600) {
            if (hasRice && made == Sushi.NONE) {
                if (mouseY <= 490) {
                    hasSalmon = true;
                } else if (mouseY <= 545) {
                    hasTuna = true;
                } else {
                    hasTamago = true;
                }
            }
            return;
        }
        if (mouseX > 300 && mouseX <= 475 && mouseY >= 440 && mouseY <= 600) {
            if (!hasSeaweed && !hasRice && made == Sushi.NONE) {
                hasSeaweed = true;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            dragX = e.getX();
            dragY = e.getY();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!dragging || gameState != PLAYING) {
            dragging = false;
            return;
        }
        int mx = e.getX();
        int my = e.getY();
        dragging = false;

        if (inBox(mx, my, 700, 500, 100, 100)) {
            clearStation();
            return;
        }

        Customer target = customerAt(mx, my);
        if (target != null) {
            tryServe(target, dragType);
        }
        dragType = Sushi.NONE;
    }

    private void tryServe(Customer customer, int sushiType) {
        if (sushiType == Sushi.NONE || customer == null || !customer.isWaiting()) {
            return;
        }
        if (sushiType == customer.orderType) {
            combo++;
            if (combo > bestCombo) {
                bestCombo = combo;
            }
            int bonus = (int) (customer.patience / customer.maxPatience * 50);
            int comboBonus = Math.max(0, (combo - 1) * 15);
            int gained = 100 + bonus + comboBonus;
            score += gained;
            lastServeCorrect = true;
            customer.serveCorrect();
            fx.burst(customer.drawX() + 70, customer.drawY() + 70, true);
            String pop = combo >= 2 ? "+" + gained + "  x" + combo : "+" + gained;
            fx.popup(pop, customer.drawX() + 10, customer.drawY() + 30, true);
        } else {
            combo = 0;
            lives--;
            lastServeCorrect = false;
            customer.serveWrong();
            flashWrong = 0.25;
            fx.burst(customer.drawX() + 70, customer.drawY() + 70, false);
            fx.popup("WRONG", customer.drawX() + 20, customer.drawY() + 40, false);
            if (lives <= 0) {
                gameState = GAME_OVER;
            }
        }
        feedbackTicks = 40;
        clearStation();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
