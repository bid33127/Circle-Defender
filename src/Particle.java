import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Particle.java
 * Handles core VFX
 */
public class Particle {
    public double x, y;
    public double dx, dy;
    public int life, maxLife;
    public Color color;
    public int size;
    
    public boolean isBlastRing = false;
    public double targetSize;

    // Standard debris particle initialization
    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = (int)(Math.random() * 4) + 2;
        
        double angle = Math.random() * Math.PI * 2;
        double speed = Math.random() * 3 + 1;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
        
        this.maxLife = (int)(Math.random() * 20) + 10;
        this.life = this.maxLife;
    }

    // Blast ring particle initialization
    public Particle(double x, double y, Color color, double targetRadius) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = 0; 
        this.targetSize = targetRadius;
        this.dx = 0;
        this.dy = 0;
        this.maxLife = 20; 
        this.life = maxLife;
        this.isBlastRing = true;
    }

    public void update() {
        x += dx;
        y += dy;
        
        if (isBlastRing) {
            size += (targetSize - size) * 0.2;
        }
        life--;
    }

    public void draw(Graphics2D g2d) {
        int alpha = (int)(255 * ((double)life / maxLife));
        alpha = Math.max(0, Math.min(255, alpha)); 
        
        if (isBlastRing) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha / 3));
            g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
            
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.setStroke(new BasicStroke(3)); 
            g2d.drawOval((int)x - size/2, (int)y - size/2, size, size);
            g2d.setStroke(new BasicStroke(1)); 
        } else {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }
    }
}