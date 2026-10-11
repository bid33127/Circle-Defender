import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * Bullet.java
 * Core projectile class. Handles trajectory, homing mechanics, and explosive triggers.
 */
public class Bullet {
    public double x, y;
    public int size;
    private double dx, dy;
    private double speed; 

    public int damage;
    public int hp; 
    public boolean isPlayerOwned; 
    
    public boolean isExplosive = false; 
    public boolean hasParticles = false; 
    
    public int timeAlive = 0;           
    public int maxLife = -1; 
    public int explosionRadius = 80; 
    public int explosionDelay = 60; 

    public boolean isHoming = false;
    public Player target = null;
    public double turnRate = 0.01; 

    // Player bullet constructor
    public Bullet(double startX, double startY, double angle, double speed, int size, int damage, int hp, boolean isPlayerOwned) {
        this.x = startX;
        this.y = startY;
        this.size = size;
        this.damage = damage;
        this.hp = hp;
        this.isPlayerOwned = isPlayerOwned;
        this.speed = speed;
        this.dx = Math.cos(angle) * this.speed;
        this.dy = Math.sin(angle) * this.speed;
    }

    // Enemy bullet constructor
    public Bullet(double startX, double startY, double angle, int damage, double speed, boolean isPlayerOwned) {
        this.x = startX;
        this.y = startY;
        this.size = 12; 
        this.damage = damage;
        this.hp = 1;    
        this.isPlayerOwned = isPlayerOwned;
        this.speed = speed;
        this.dx = Math.cos(angle) * this.speed;
        this.dy = Math.sin(angle) * this.speed;
    }

    public void update(int screenWidth, int screenHeight) {
        // Homing steering logic
        if (isHoming && target != null && target.hp > 0) {
            double desiredAngle = Math.atan2(target.y - this.y, target.x - this.x);
            double currentAngle = Math.atan2(this.dy, this.dx);
            
            double angleDiff = desiredAngle - currentAngle;
            while (angleDiff > Math.PI) angleDiff -= Math.PI * 2;
            while (angleDiff < -Math.PI) angleDiff += Math.PI * 2;
            
            currentAngle += Math.max(-turnRate, Math.min(turnRate, angleDiff));
            
            this.dx = Math.cos(currentAngle) * this.speed;
            this.dy = Math.sin(currentAngle) * this.speed;
        }

        this.x += dx;
        this.y += dy;
        this.timeAlive++;

        if (maxLife > 0 && timeAlive >= maxLife) this.hp = 0; 
        
        if (isExplosive) {
            if (!isPlayerOwned && timeAlive >= explosionDelay) this.hp = 0; 
        }

        // Out of bounds cleanup
        if (this.x < 0 || this.x > screenWidth || this.y < 0 || this.y > screenHeight) {
            this.hp = 0; 
        }
    }

    public void draw(Graphics2D g2d) {
        if (isHoming) {
            g2d.setColor(Color.RED);
            double currentAngle = Math.atan2(dy, dx);
            
            AffineTransform old = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(currentAngle);
            
            int[] xPoints = {size, -size / 2, -size / 2};
            int[] yPoints = {0, size / 2, -size / 2};
            
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, 3);
            
            g2d.setTransform(old);
        } else {
            if (isPlayerOwned) g2d.setColor(isExplosive ? Color.YELLOW : Color.CYAN); 
            else g2d.setColor(isExplosive ? Color.ORANGE : Color.RED); 
            
            g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
        }

        if (isExplosive) {
            g2d.setColor(isPlayerOwned ? new Color(255, 255, 0, 50) : new Color(255, 165, 0, 50)); 
            g2d.fillOval((int) x - explosionRadius / 2, (int) y - explosionRadius / 2, explosionRadius, explosionRadius);
        }
    }

    public boolean isDead() { return this.hp <= 0; }
    
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }
}