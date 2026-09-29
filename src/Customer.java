import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Random;

public class Customer {
    static final int WALKING_IN = 0;
    static final int WAITING = 1;
    static final int HAPPY = 2;
    static final int ANGRY = 3;
    static final int WALKING_OUT = 4;

    static final int DOOR_X = 760;
    static final int DRAW_W = 138;
    static final int DRAW_H = 290;

    int slot;
    double x;
    double y;
    int standY;
    int targetX;
    int orderType;
    int state;
    int skin;
    double patience;
    double maxPatience;
    int reactionTicks;
    boolean timedOut;

    double walkPhase;
    double bubbleScale;
    double shake;
    double hop;

    Sprite normalFace;
    Sprite angryFace;
    Sprite happyFace;
    Sprite orderBubble;
    Sprite[] patienceBars;

    public Customer(int slot, int targetX, int standY, double patienceSeconds,
            Sprite[] girl, Sprite[] guy, Sprite[] bubbles, Sprite[] bars) {
        this.slot = slot;
        this.targetX = targetX;
        this.x = DOOR_X;
        this.y = standY;
        this.standY = standY;
        this.state = WALKING_IN;
        this.reactionTicks = 0;
        this.timedOut = false;
        this.patienceBars = bars;
        this.walkPhase = Math.random() * 6;
        this.bubbleScale = 0;

        Random rand = new Random();
        this.skin = rand.nextInt(2);
        this.orderType = rand.nextInt(4);
        this.maxPatience = skin == 0 ? Math.max(8, patienceSeconds - 2) : patienceSeconds + 3;
        this.patience = this.maxPatience;

        if (skin == 0) {
            normalFace = girl[0];
            angryFace = girl[1];
            happyFace = girl[2];
        } else {
            normalFace = guy[0];
            angryFace = guy[1];
            happyFace = guy[2];
        }
        orderBubble = bubbles[orderType];
    }

    public int drawX() {
        return (int) Math.round(x + shake);
    }

    public int drawY() {
        return (int) Math.round(y - hop);
    }

    public boolean isWaiting() {
        return state == WAITING;
    }

    public boolean isGone() {
        return state == WALKING_OUT && x >= DOOR_X;
    }

    public boolean contains(int mx, int my) {
        int dx = drawX();
        int dy = drawY();
        return mx >= dx && mx <= dx + DRAW_W && my >= dy && my <= dy + DRAW_H;
    }

    public void serveCorrect() {
        state = HAPPY;
        reactionTicks = 34;
        hop = 18;
    }

    public void serveWrong() {
        state = ANGRY;
        reactionTicks = 34;
        patience = 0;
        shake = 8;
    }

    public void update(double dt) {
        walkPhase += dt * 10;
        if (state == WALKING_IN) {
            x -= 260 * dt;
            y = standY + Math.sin(walkPhase) * 5;
            bubbleScale = Math.min(1, bubbleScale + dt * 2.5);
            if (x <= targetX) {
                x = targetX;
                y = standY;
                state = WAITING;
                bubbleScale = 1;
            }
        } else if (state == WAITING) {
            y = standY + Math.sin(walkPhase * 0.45) * 2.2;
            if (patience / maxPatience < 0.28) {
                shake = Math.sin(walkPhase * 3) * 3;
            } else {
                shake *= Math.max(0, 1 - dt * 8);
            }
            patience -= dt;
            if (patience <= 0) {
                patience = 0;
                timedOut = true;
                state = ANGRY;
                reactionTicks = 28;
                shake = 10;
            }
        } else if (state == HAPPY || state == ANGRY) {
            reactionTicks--;
            if (state == HAPPY) {
                hop = Math.abs(Math.sin(walkPhase * 1.4)) * 16;
            } else {
                shake = Math.sin(walkPhase * 8) * 7;
            }
            if (reactionTicks <= 0) {
                state = WALKING_OUT;
                hop = 0;
            }
        } else if (state == WALKING_OUT) {
            x += 320 * dt;
            y = standY + Math.sin(walkPhase) * 5;
            shake *= Math.max(0, 1 - dt * 6);
            hop *= Math.max(0, 1 - dt * 8);
        }
    }

    public void draw(Graphics g, Component c, boolean highlight) {
        int dx = drawX();
        int dy = drawY();

        if (highlight && isWaiting()) {
            g.setColor(new Color(255, 230, 120, 80));
            g.fillRoundRect(dx - 8, dy - 8, DRAW_W + 16, DRAW_H + 16, 18, 18);
        }

        Sprite face = normalFace;
        if (state == HAPPY) {
            face = happyFace;
        } else if (state == ANGRY) {
            face = angryFace;
        }
        if (face != null) {
            face.draw(g, c, dx, dy, DRAW_W, DRAW_H);
        }

        boolean showOrder = state == WAITING || state == WALKING_IN;
        if (showOrder && orderBubble != null && bubbleScale > 0.05) {
            int bw = (int) (108 * bubbleScale);
            int bh = (int) (70 * bubbleScale);
            orderBubble.draw(g, c, dx + DRAW_W - 22, dy + 8, bw, bh);
        }
        if (state == WAITING && patienceBars != null) {
            int bars = (int) Math.round((patience / maxPatience) * 10.0);
            if (bars < 0) {
                bars = 0;
            }
            if (bars > 10) {
                bars = 10;
            }
            Sprite bar = patienceBars[10 - bars];
            if (bar != null) {
                bar.draw(g, c, dx + 6, dy - 20, DRAW_W - 12, 16);
            }
        }
    }
}
