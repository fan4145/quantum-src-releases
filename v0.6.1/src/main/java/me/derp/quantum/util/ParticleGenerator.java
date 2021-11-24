package me.derp.quantum.util;

import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Random;

/**
 * cred: https://github.com/zPeanut/Hydrogen/blob/master/src/main/java/tk/peanut/hydrogen/utils/ParticleGenerator.java
 * <p>
 * not from ozark - kambing
 */
public class ParticleGenerator {

    private final int width;
    private final int height;
    private final ArrayList<Particle> particles = new ArrayList<>();
    int state = 0;
    int a = 255;
    int r = 255;
    int g = 255;
    int b = 255;

    public ParticleGenerator(int count, int width, int height) {
        this.width = width;
        this.height = height;
        for (int i = 0; i < count; i++) {
            Random random = new Random();
            this.particles.add(new Particle(random.nextInt(width), random.nextInt(height)));
        }
    }

    public void drawParticles(int mouseX, int mouseY) {
        for (Particle p : this.particles) {
            if (p.reset) {
                p.resetPosSize();
                p.reset = false;
            }
            p.draw(mouseX, mouseY);

        }
    }

    public class Particle {
        private int x;
        private int y;
        private int k;
        private float size;
        private boolean reset;
        private final Random random = new Random();

        public Particle(int x, int y) {
            this.x = x;
            this.y = y;
            this.size = genRandom(1.0F, 3.0F);
        }

        public void draw(int mouseX, int mouseY) {
            if (this.size <= 0.0F) {
                this.reset = true;
            }
            this.size -= 0.05F;
            this.k += 1;
            int xx = (int) (MathHelper.cos(0.1F * (this.x + this.k)) * 10.0F);
            int yy = (int) (MathHelper.cos(0.1F * (this.y + this.k)) * 10.0F);
            drawBorderedCircle(this.x + xx, this.y + yy, this.size, 0, 553648127);


            float distance = (float) distance(this.x + xx, this.y + yy, mouseX, mouseY);

            if (distance < 50) {
                float alpha1 = Math.min(1.0f, Math.min(1.0f, 1.0f - distance / 50));

                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(255F, 255F, 255F, 255F);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glLineWidth(0.1F);
                GL11.glBegin(GL11.GL_LINES);

                GL11.glVertex2f(this.x + xx, this.y + yy);
                GL11.glVertex2f(mouseX, mouseY);
                GL11.glEnd();
            }
        }

        public void resetPosSize() {
            this.x = this.random.nextInt(ParticleGenerator.this.width);
            this.y = this.random.nextInt(ParticleGenerator.this.height);
            this.size = genRandom(1.0F, 3.0F);
        }

        public float genRandom(float min, float max) {
            return (float) (min + Math.random() * (max - min + 1.0F));
        }
    }

    public static void drawBorderedCircle(int x, int y, float radius, int outsideC, int insideC) {
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        float scale = 0.1F;
        GL11.glScalef(scale, scale, scale);
        x = (int) (x * (1.0F / scale));
        y = (int) (y * (1.0F / scale));
        radius *= 1.0F / scale;
        drawCircle(x, y, radius, insideC);
        drawUnfilledCircle(x, y, radius, 1.0F, outsideC);
        GL11.glScalef(1.0F / scale, 1.0F / scale, 1.0F / scale);
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void drawCircle(int x, int y, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin(i * 3.141526D / 180.0D) * radius, y + Math.cos(i * 3.141526D / 180.0D) * radius);
        }
        GL11.glEnd();
    }

    public static void drawUnfilledCircle(int x, int y, float radius, float lineWidth, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(2848);
        GL11.glBegin(2);
        for (int i = 0; i <= 360; i++) {
            GL11.glVertex2d(x + Math.sin(i * 3.141526D / 180.0D) * radius, y + Math.cos(i * 3.141526D / 180.0D) * radius);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
    }

    public static double distance(float x, float y, float x1, float y1) {
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
    }
}
