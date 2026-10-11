import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * Player.java
 * Manages player stats, level progression, and core abilities.
 */
public class Player {
    
    public double x, y;
    public int size = 40; 
    
    public double maxHp;
    public double hp;
    public int damage;
    public double speed;
    public int regenAmount;
    
    public int normalBulletSize;
    public int sniperBulletSize;
    public int shieldBulletSize;
    public int rapidBulletSize;
    public int explosiveBulletSize;
    
    public int unlockLevel = 0; 
    private int regenTimer = 0; 
    
    public double lastDirX = 1.0;  
    public double lastDirY = 0.0;  
    
    public boolean isCharging = false; 
    public int chargeTimer = 0;
    
    // Skill Cooldowns & Properties
    private int dashTimer = 0;
    private double dashAngle = 0;
    public int dashCooldown = 0;
    public int dashCooldownTime = 120;       
    public double dashSpeedMultiplier = 3.0; 
    public int dashDuration = 15;            
    
    public int shieldCooldown = 0;
    public int shieldCooldownTime = 300;     
    public int shieldBulletsLeft = 0;
    public int shieldTotalBullets = 0;
    public int shieldBulletHp = 0;
    public int shieldFireTimer = 0;
    
    public int rapidCooldown = 0;      
    public int rapidFireTimer = 0;
    public double rapidTargetX = 0;
    public double rapidTargetY = 0;
    
    public int explosiveCooldown = 0;  
    public int invincibleTimer = 0;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.unlockLevel = 0;
        setupPlayerProfile();
        this.hp = this.maxHp; 
    }

    private void setStats(double maxHp, int damage, double speed, int regenAmount, 
                          int size, int dashCooldownTime, double dashSpeedMultiplier, int dashDuration, int shieldCooldownTime) {
        double hpDiff = maxHp - this.maxHp;
        if (hpDiff > 0 && this.maxHp > 0) this.hp += hpDiff; 
        
        this.maxHp = maxHp;
        this.damage = damage;
        this.speed = speed;
        this.regenAmount = regenAmount; 
        
        this.size = size;
        this.dashCooldownTime = dashCooldownTime;
        this.dashSpeedMultiplier = dashSpeedMultiplier;
        this.dashDuration = dashDuration;
        this.shieldCooldownTime = shieldCooldownTime;
    }

    private void setupPlayerProfile() {
        switch (unlockLevel) {
            case 0: setStats(100, 10, 4.0, 2, 40, 60, 3.0, 15, 300); break; 
            case 1: setStats(120, 12, 4.5, 2, 40, 60, 3.0, 15, 300); break;
            case 2: setStats(150, 15, 5.0, 4, 40, 60, 3.0, 15, 300); break;
            case 3: setStats(180, 15, 5.2, 4, 40, 60, 3.0, 15, 300); break;
            case 4: setStats(200, 18, 5.4, 6, 40, 60, 3.0, 15, 300); break;
            case 5: setStats(220, 20, 5.6, 6, 40, 60, 3.5, 15, 300); break;
            case 6: setStats(250, 22, 5.8, 8, 40, 45, 3.5, 15, 300); break;
            case 7: setStats(280, 25, 6.0, 8, 40, 45, 3.5, 15, 300); break;
            case 8: setStats(300, 28, 6.1, 8, 40, 45, 3.5, 20, 300); break;
            case 9: setStats(350, 30, 6.2, 8, 40, 30, 3.5, 20, 300); break;
            case 10: setStats(400, 35, 6.3, 10, 40, 30, 3.5, 20, 240); break;
            case 11: setStats(500, 40, 6.5, 15, 40, 30, 4.0, 25, 240); break;
            default: if(unlockLevel > 11) setStats(500, 40, 6.0, 50, 40, 25, 4.0, 25, 180); break;
        }
        
        normalBulletSize = (unlockLevel >= 7) ? 20 : 12;
        sniperBulletSize = (unlockLevel >= 8) ? 25 : 18;
        shieldBulletSize = 15;
        rapidBulletSize = (unlockLevel >= 9) ? 16 : 12;
        explosiveBulletSize = 25;
    }

    public void levelUp() {
        if (unlockLevel < 11) {
            unlockLevel++;
            setupPlayerProfile();
        }
    }

    public void update(boolean up, boolean down, boolean left, boolean right, int screenWidth, int screenHeight, List<Bullet> bullets) {
        
        if (invincibleTimer > 0) invincibleTimer--;
        if (dashCooldown > 0) dashCooldown--;
        if (shieldCooldown > 0) shieldCooldown--;
        if (rapidCooldown > 0) rapidCooldown--;
        if (explosiveCooldown > 0) explosiveCooldown--;

        // Shield Execution
        if (shieldBulletsLeft > 0) {
            if (shieldFireTimer <= 0) {
                int currentIndex = shieldTotalBullets - shieldBulletsLeft;
                double angle = currentIndex * (Math.PI * 2 / shieldTotalBullets);
                Bullet b = new Bullet(this.x, this.y, angle, 1.25, shieldBulletSize, this.damage, shieldBulletHp, true);
                b.maxLife = 90; 
                bullets.add(b);
                shieldBulletsLeft--;
                shieldFireTimer = 0;
            } else {
                shieldFireTimer--;
            }
        }

        // Rapid Fire Execution
        if (rapidFireTimer > 0) {
            rapidFireTimer--;
            boolean isUpgraded = (unlockLevel >= 9);
            int fireRate = isUpgraded ? 3 : 6; 
            
            if (rapidFireTimer % fireRate == 0) {
                double baseAngle = Math.atan2(rapidTargetY - this.y, rapidTargetX - this.x);
                double spread = isUpgraded ? Math.toRadians(40) : Math.toRadians(80);
                double randomAngle = baseAngle + (Math.random() * spread) - (spread / 2.0);
                
                double bSpeed = isUpgraded ? 16.0 : 12.0;
                int dmg = isUpgraded ? (int)(this.damage * 1.5) : this.damage;
                int bulletHp = isUpgraded ? 3 : 1;
                
                Bullet b = new Bullet(this.x, this.y, randomAngle, bSpeed, rapidBulletSize, dmg, bulletHp, true);
                b.hasParticles = isUpgraded;
                bullets.add(b);
            }
        }

        // Dash Execution
        if (dashTimer > 0) {
            this.x += Math.cos(dashAngle) * (this.speed * dashSpeedMultiplier);
            this.y += Math.sin(dashAngle) * (this.speed * dashSpeedMultiplier);
            dashTimer--;
            
            boolean hitX = false, hitY = false;
            if (this.x < size / 2.0) { this.x = size / 2.0; hitX = true; }
            if (this.x > screenWidth - size / 2.0) { this.x = screenWidth - size / 2.0; hitX = true; }
            if (this.y < size / 2.0) { this.y = size / 2.0; hitY = true; }
            if (this.y > screenHeight - size / 2.0) { this.y = screenHeight - size / 2.0; hitY = true; }

            if (hitX || hitY) {
                if (hitX) dashAngle = Math.PI - dashAngle; 
                if (hitY) dashAngle = -dashAngle;          
                lastDirX = Math.cos(dashAngle);
                lastDirY = Math.sin(dashAngle);
            }
            return; 
        }

        // Normal Movement
        double dx = 0; double dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length; dy /= length;
            lastDirX = dx; lastDirY = dy;
        }

        double currentSpeed = this.speed;
        if (isCharging && unlockLevel >= 2 && chargeTimer >= 9) {
            currentSpeed *= 0.3; 
        }

        this.x += dx * currentSpeed;
        this.y += dy * currentSpeed;
        clampToScreen(screenWidth, screenHeight);

        // Auto HP Regen
        if (hp < maxHp) {
            regenTimer++;
            if (regenTimer >= 60) {
                hp += regenAmount; 
                if (hp > maxHp) hp = maxHp;
                regenTimer = 0;
            }
        }
    }

    private void clampToScreen(int screenWidth, int screenHeight) {
        if (this.x < size / 2.0) this.x = size / 2.0;
        if (this.x > screenWidth - size / 2.0) this.x = screenWidth - size / 2.0;
        if (this.y < size / 2.0) this.y = size / 2.0;
        if (this.y > screenHeight - size / 2.0) this.y = screenHeight - size / 2.0;
    }

    public void shootNormal(double targetX, double targetY, List<Bullet> bullets) {
        double angle = Math.atan2(targetY - this.y, targetX - this.x);
        if (unlockLevel >= 7) { 
            Bullet b = new Bullet(this.x, this.y, angle, 24.0, normalBulletSize, this.damage * 2, 3, true);
            b.hasParticles = true;
            bullets.add(b);
        } else { 
            bullets.add(new Bullet(this.x, this.y, angle, 16.0, normalBulletSize, this.damage, 1, true));
        }
    }

    public void shootSniper(double targetX, double targetY, List<Bullet> bullets) {
        double angle = Math.atan2(targetY - this.y, targetX - this.x);
        if (unlockLevel >= 8) { 
            Bullet b = new Bullet(this.x, this.y, angle, 35.0, sniperBulletSize, this.damage * 5, 50, true);
            b.hasParticles = true;
            bullets.add(b);
        } else { 
            bullets.add(new Bullet(this.x, this.y, angle, 30.0, sniperBulletSize, this.damage * 3, 30, true));
        }
    }

    public void dash() {
        if (dashCooldown > 0) return;
        this.dashAngle = Math.atan2(lastDirY, lastDirX); 
        this.dashTimer = dashDuration;
        this.dashCooldown = dashCooldownTime; 
        this.invincibleTimer = dashDuration; 
    }

    public void castShield(List<Bullet> bullets) {
        if (shieldCooldown > 0) return;
        this.shieldTotalBullets = (unlockLevel >= 10) ? 18 : 12;
        this.shieldBulletsLeft = shieldTotalBullets;
        this.shieldBulletHp = (unlockLevel >= 10) ? 64 : 32;
        this.shieldFireTimer = 0; 
        this.shieldCooldown = shieldCooldownTime; 
    }

    public void shootRapid(double targetX, double targetY, List<Bullet> bullets) {
        if (rapidCooldown > 0) return;
        this.rapidTargetX = targetX;
        this.rapidTargetY = targetY;
        boolean isUpgraded = (unlockLevel >= 9);
        this.rapidFireTimer = isUpgraded ? 100 : 75; 
        this.rapidCooldown = isUpgraded ? 180 : 240;  
    }

    public void shootExplosive(double targetX, double targetY, List<Bullet> bullets) {
        if (explosiveCooldown > 0) return;
        double baseAngle = Math.atan2(targetY - this.y, targetX - this.x);
        boolean isUpgraded = (unlockLevel >= 11);
        int count = isUpgraded ? 3 : 1;
        int radius = isUpgraded ? 350 : 250; 
        double speed = isUpgraded ? 10.0 : 6.0;
        int dmg = isUpgraded ? this.damage * 4 : this.damage * 2;
        int bulletHp = isUpgraded ? 15 : 8;
        
        for (int i = 0; i < count; i++) {
            double angle = baseAngle + (isUpgraded ? (i - 1) * 0.2 : 0);
            Bullet b = new Bullet(this.x, this.y, angle, speed, explosiveBulletSize, dmg, bulletHp, true);
            b.isExplosive = true;
            b.explosionRadius = radius;
            bullets.add(b);
        }
        this.explosiveCooldown = isUpgraded ? 90 : 180;
    }

    public void draw(Graphics2D g2d) {
        if (invincibleTimer == 0 || invincibleTimer % 10 < 5) {
            g2d.setColor(new Color(51, 153, 255));
            g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String label = "Player (Lv " + unlockLevel + ")";
        int textWidth = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, (int) x - textWidth / 2, (int) y - size / 2 - 15);
        
        int barWidth = 50;
        int barHeight = 6;
        int barX = (int) x - barWidth / 2;
        int barY = (int) y - size / 2 - 10;
        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(Color.GREEN);
        int currentHpWidth = (int) (barWidth * (Math.max(0, hp) / maxHp));
        g2d.fillRect(barX, barY, currentHpWidth, barHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);

        if (isCharging) {
            g2d.setColor(Color.ORANGE);
            int chargeWidth = Math.min(50, (int)(50 * (chargeTimer / 30.0))); 
            g2d.fillRect(barX, barY + 8, chargeWidth, 4);
        }
    }
    
    public Rectangle getBounds() { return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size); }
    public boolean isDead() { return this.hp <= 0; }
}