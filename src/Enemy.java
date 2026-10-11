import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * Enemy.java
 * Core boss AI class. Handles dynamic stat scaling, movement behavior, and attack patterns.
 */
public class Enemy {
    public double x, y;
    public int size;
    public double hp;       
    public double maxHp;    
    public String name;
    
    public double baseSpeed;
    public int attackDamage;
    public int contactDamage;
    
    // Attack Variables
    public int normalBulletSize = 12;    
    public double normalSpeed = 8.0;     
    public int normalHp = 1;             

    public int expBulletCount = 1;       
    public int expRadius = 150;          
    public int expDelay = 60;            
    public int expHp = 50;               
    public int expDamage = 20;           
    public double expSpeed = 5.0;        
    public int expBulletSize = 30;       

    public int rapidBulletSize = 12;     
    public double rapidSpeed = 10.0;     
    public int rapidHp = 1;              
    public double rapidSpreadAngle = 20.0; 

    public int scatterCount = 20;        
    public double scatterSpreadAngle = 40.0; 
    public int scatterHp = 1;            
    public int scatterDamage = 10;       
    public double scatterSpeed = 6.0;    
    public int scatterBulletSize = 12;   
    
    public int sniperBulletSize = 25;    
    public double sniperSpeed = 20.0;    
    public int sniperHp = 50;             

    public int homingCount = 3;          
    public double homingSpeed = 5.0;     
    public int homingDamage = 15;        
    public int homingHp = 2;             
    public int homingBulletSize = 16;    

    public int shieldBulletCount = 12;   
    public double shieldSpeed = 2.0;     
    public int shieldDamage = 20;        
    public int shieldHp = 5;             
    public int shieldDuration = 120;     
    public int shieldBulletSize = 15;    

    public int circleBulletCount = 16;   
    public double circleSpeed = 5.0;     
    public int circleDamage = 15;        
    public int circleHp = 1;             
    public int circleBulletSize = 12;    

    // State Tracking
    public int currentAttackSet;
    public int intendedAttackSet; 
    public int currentMovePattern;
    public int activeAttack = 6;         
    
    public int maxDashCount = 1;         
    private int currentDashCount = 0;    
    public double enemyDashSpeed = 3.5;  
    public int enemyDashDuration = 60;   
    
    private int moveChangeCooldown;
    private int attackChangeCooldown;
    private int attackTimer = 0;    
    private double attackSubTimer = 0; 
    private int moveTimer = 0;
    private int aliveTimer = 0; 
    
    private boolean isFirstAttack = true;
    private int startDelayTimer = 0;
    
    private double randomMoveAngle = 0;
    private double targetDistance = 200; 
    private int orbitDirection = 1;      
    private double dashAngle = 0; 

    Player target; 
    ArrayList<Bullet> gameBullets;

    public Enemy(double x, double y, String name, Player target, ArrayList<Bullet> gameBullets) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.target = target;
        this.gameBullets = gameBullets;
        
        setupEnemyProfile(); 
        this.currentAttackSet = 6;
        switchMovePattern();
    }

    private void setStats(double hp, int size, double speed, int damage, int moveCooldown, int attackCooldown) {
        this.maxHp = hp;
        this.hp = hp;                 
        this.size = size;             
        this.baseSpeed = speed;       
        this.attackDamage = damage;   
        this.contactDamage = damage;  
        this.moveChangeCooldown = moveCooldown;       
        this.attackChangeCooldown = attackCooldown;   
        
        this.expDamage = damage * 2;
        this.scatterDamage = damage;
        this.homingDamage = (int)(damage * 1.5);
        this.shieldDamage = damage * 2;
        this.circleDamage = (int)(damage * 1.5);
    }

    private void setupEnemyProfile() {
        setStats(50, 40, 2.0, 10, 180, 180); 
        intendedAttackSet = 1; 
        currentMovePattern = 1;
        maxDashCount = 1; 
        
        switch (name) {
            case "Dying_Virus":
                setStats(150, 40, 0.5, 5, 99999, 99999);
                contactDamage = 10; 
                intendedAttackSet = 1; currentMovePattern = 3;
                break;
            case "Ordinary_Virus":
                setStats(750, 60, 2.5, 15, 240, 240);
                sniperSpeed = 25.0;
                sniperHp = 20;
                break;
            case "Extraordinary_Virus":
                setStats(1200, 65, 3.5, 10, 180, 210);
                contactDamage = 20; 
                maxDashCount = 3; 
                enemyDashDuration = 45; 
                enemyDashSpeed = 3.5;
                break;
            case "Sir_Virus":
                setStats(2000, 70, 3.0, 30, 120, 120);
                scatterCount = 60; 
                scatterSpreadAngle = 360.0; 
                scatterSpeed = 8.0;
                circleBulletCount = 20;
                break;
            case "Baron_Virus":
                setStats(2500, 75, 3.0, 30, 150, 150);
                rapidSpreadAngle = 30.0;
                rapidSpeed = 30.0;
                break;
            case "Viscount_Virus":
                setStats(3000, 80, 3.0, 25, 120, 120);
                expBulletCount = 3; 
                expRadius = 200; 
                expDelay = 70;
                break;
            case "Count_Virus":
                setStats(4000, 90, 3.0, 20, 90, 90);
                expBulletCount = 5; 
                expRadius = 300; 
                expDelay = 120;
                break;
            case "Duke_Virus":
                setStats(8000, 100, 3.0, 15, 80, 80);
                circleBulletCount = 24;
                circleSpeed = 6.0;
                scatterCount = 60;
                scatterSpreadAngle = 360.0;
                rapidSpeed = 14.0;
                break;
            case "Grand_Duke_Virus":
                setStats(12000, 110, 3.2, 15, 70, 70);
                maxDashCount = 2; 
                enemyDashSpeed = 5.0;
                sniperBulletSize = 45; 
                sniperSpeed = 80.0;
                sniperHp = 50;
                break;
            case "King_Virus":
                setStats(16000, 120, 3.5, 15, 70, 70);
                expBulletCount = 6; 
                expRadius = 350;
                expDamage = 120;
                scatterCount = 80;
                scatterSpreadAngle = 360.0;
                circleBulletCount = 30;
                break;
            case "Emperor_Virus":
                setStats(22000, 135, 3.8, 20, 60, 60);
                maxDashCount = 7; 
                enemyDashSpeed = 6.0;
                enemyDashDuration = 50;
                scatterCount = 100; 
                scatterSpreadAngle = 360.0;
                contactDamage = 40;
                break;
            case "God_Virus":
                setStats(30000, 150, 4.2, 20, 40, 40);
                contactDamage = 50;
                sniperSpeed = 60.0;
                maxDashCount = 3; 
                enemyDashSpeed = 5.0;
                homingCount = 6;
                shieldBulletCount = 32;
                circleBulletCount = 32;
                scatterCount = 120;
                scatterSpreadAngle = 360.0;
                expBulletCount = 5;
                expRadius = 250;
                sniperBulletSize = 35;
                break;
        }
    }

    public void update(int screenWidth, int screenHeight) {
        aliveTimer++;

        if (name.equals("Dying_Virus")) {
            if (aliveTimer <= 1200) this.hp -= this.maxHp / 1200.0;
            else this.hp = 0; 
        }

        // Attack AI
        if (isFirstAttack) {
            startDelayTimer++;
            if (startDelayTimer >= 60) {
                currentAttackSet = intendedAttackSet; 
                isFirstAttack = false;
                attackSubTimer = 0;
            }
        } else {
            attackTimer++;
            if (attackTimer >= attackChangeCooldown) {
                switchAttackSet();
                attackTimer = 0;
                attackSubTimer = 0; 
            }
        }

        // Movement AI
        moveTimer++;
        int currentMoveLimit = (currentMovePattern >= 7 && currentMovePattern <= 10) ? enemyDashDuration : moveChangeCooldown;

        if (moveTimer >= currentMoveLimit) {
            if (currentMovePattern >= 7 && currentMovePattern <= 10) {
                currentDashCount++;
                if (currentDashCount >= maxDashCount) {
                    currentDashCount = 0;
                    switchMovePattern(); 
                } else {
                    if (currentMovePattern == 7 || currentMovePattern == 8) dashAngle = Math.atan2(target.y - y, target.x - x);
                    else if (currentMovePattern == 10) dashAngle = Math.random() * Math.PI * 2;
                }
            } else {
                switchMovePattern();
            }
            moveTimer = 0;
        }

        double fireRateMultiplier = Math.min(2.0, 1.0 + (aliveTimer / 1800.0));
        double lastFrame = attackSubTimer;
        attackSubTimer += fireRateMultiplier;
        double currFrame = attackSubTimer;

        double angleToPlayer = Math.atan2(target.y - this.y, target.x - this.x);
        double distToPlayer = Math.hypot(target.x - this.x, target.y - this.y);
        
        double moveAngle = 0;
        double attackSpeedModifier = 1.0; 

        activeAttack = currentAttackSet;
        if (currentMovePattern == 7) {
            activeAttack = 6; 
        }

        // Execute Attack Sequence
        switch (activeAttack) {
            case 1:
                if (Math.floor(lastFrame / 60.0) < Math.floor(currFrame / 60.0)) {
                    Bullet b = new Bullet(x, y, angleToPlayer, attackDamage, normalSpeed, false);
                    b.size = normalBulletSize;
                    b.hp = normalHp;
                    gameBullets.add(b);
                }
                break;
            case 2:
                attackSpeedModifier = 0.3; 
                if (lastFrame < 30 && currFrame >= 30) { 
                    for (int i = 0; i < expBulletCount; i++) {
                        double spreadAngle = (expBulletCount > 1) ? Math.toRadians((Math.random() * 40) - 20) : 0;
                        Bullet bigExplosive = new Bullet(x, y, angleToPlayer + spreadAngle, expDamage, expSpeed, false);
                        bigExplosive.isExplosive = true; 
                        bigExplosive.size = expBulletSize; 
                        bigExplosive.hp = expHp; 
                        bigExplosive.explosionRadius = expRadius; 
                        bigExplosive.explosionDelay = expDelay + (int)(Math.random() * 60); 
                        gameBullets.add(bigExplosive);
                    }
                }
                break;
            case 3:
                attackSpeedModifier = 0.5;
                if (Math.floor(lastFrame / 15.0) < Math.floor(currFrame / 15.0)) {
                    double randomAngle = Math.toRadians((Math.random() * rapidSpreadAngle) - (rapidSpreadAngle / 2.0));
                    Bullet b = new Bullet(x, y, angleToPlayer + randomAngle, attackDamage/2, rapidSpeed, false);
                    b.size = rapidBulletSize;
                    b.hp = rapidHp;
                    gameBullets.add(b);
                }
                break;
            case 4:
                attackSpeedModifier = 0.8;
                if (currFrame <= 120.0) {
                    double fireRate = 120.0 / scatterCount; 
                    if (Math.floor(lastFrame / fireRate) < Math.floor(currFrame / fireRate)) {
                        double randomAngle = Math.toRadians((Math.random() * scatterSpreadAngle) - (scatterSpreadAngle / 2.0));
                        Bullet scatterBullet = new Bullet(x, y, angleToPlayer + randomAngle, scatterDamage, scatterSpeed, false);
                        scatterBullet.hp = scatterHp;
                        scatterBullet.size = scatterBulletSize;
                        gameBullets.add(scatterBullet);
                    }
                }
                break;
            case 5:
                attackSpeedModifier = 0; 
                if (lastFrame < 50 && currFrame >= 50) { 
                    Bullet sniper = new Bullet(x, y, angleToPlayer, attackDamage * 3, sniperSpeed, false);
                    sniper.size = sniperBulletSize; 
                    sniper.hp = sniperHp;
                    gameBullets.add(sniper);
                }
                break;
            case 6:
                break;
            case 7:
                if (lastFrame < 30 && currFrame >= 30) {
                    double spreadBase = Math.toRadians(15); 
                    double startAngle = angleToPlayer - (spreadBase * (homingCount - 1) / 2.0);
                    
                    for (int i = 0; i < homingCount; i++) {
                        double angle = startAngle + (spreadBase * i);
                        Bullet homingB = new Bullet(x, y, angle, homingDamage, homingSpeed, false);
                        homingB.hp = homingHp;
                        homingB.size = homingBulletSize; 
                        homingB.isHoming = true;
                        homingB.target = this.target; 
                        gameBullets.add(homingB);
                    }
                }
                break;
            case 8:
                if (lastFrame < 5 && currFrame >= 5) {
                    for (int i = 0; i < shieldBulletCount; i++) {
                        double angle = i * (Math.PI * 2 / shieldBulletCount);
                        Bullet shieldB = new Bullet(x, y, angle, shieldDamage, shieldSpeed, false);
                        shieldB.hp = shieldHp;
                        shieldB.size = shieldBulletSize; 
                        shieldB.maxLife = shieldDuration; 
                        gameBullets.add(shieldB);
                    }
                    this.attackTimer = attackChangeCooldown; 
                }
                break;
            case 9:
                attackSpeedModifier = 0.5;
                if (lastFrame < 30 && currFrame >= 30) {
                    for (int i = 0; i < circleBulletCount; i++) {
                        double angle = i * (Math.PI * 2 / circleBulletCount);
                        Bullet circleB = new Bullet(x, y, angle, circleDamage, circleSpeed, false);
                        circleB.hp = circleHp;
                        circleB.size = circleBulletSize; 
                        gameBullets.add(circleB);
                    }
                }
                break;
        }

        // Execute Movement Sequence
        switch (currentMovePattern) {
            case 1: attackSpeedModifier = 0; break;
            case 2: moveAngle = angleToPlayer + Math.PI; break;
            case 3: moveAngle = angleToPlayer; break;
            case 4: moveAngle = randomMoveAngle; break;
            case 5: moveAngle = angleToPlayer + (Math.PI / 2 * orbitDirection); break;
            case 6: 
                if (distToPlayer > targetDistance + 30) moveAngle = angleToPlayer; 
                else if (distToPlayer < targetDistance - 30) moveAngle = angleToPlayer + Math.PI; 
                else moveAngle = angleToPlayer + (Math.PI / 2 * orbitDirection); 
                break;
            case 7: moveAngle = dashAngle; attackSpeedModifier = enemyDashSpeed; break;
            case 8: moveAngle = dashAngle + Math.PI; attackSpeedModifier = enemyDashSpeed; break;
            case 9: moveAngle = angleToPlayer + (Math.PI / 2 * orbitDirection); attackSpeedModifier = enemyDashSpeed; break;
            case 10: moveAngle = dashAngle; attackSpeedModifier = enemyDashSpeed; break;
        }

        this.x += Math.cos(moveAngle) * (baseSpeed * attackSpeedModifier);
        this.y += Math.sin(moveAngle) * (baseSpeed * attackSpeedModifier);

        // Screen Boundaries
        boolean hitX = false, hitY = false;
        if (this.x < this.size / 2.0) { this.x = this.size / 2.0; hitX = true; }
        if (this.x > screenWidth - this.size / 2.0) { this.x = screenWidth - this.size / 2.0; hitX = true; }
        if (this.y < this.size / 2.0) { this.y = this.size / 2.0; hitY = true; }
        if (this.y > screenHeight - this.size / 2.0) { this.y = screenHeight - this.size / 2.0; hitY = true; }

        if (hitX || hitY) {
            if (hitX) {
                randomMoveAngle = Math.PI - randomMoveAngle;
                dashAngle = Math.PI - dashAngle;
            }
            if (hitY) {
                randomMoveAngle = -randomMoveAngle;
                dashAngle = -dashAngle;
            }
            orbitDirection *= -1; 
            moveTimer += 10;      
        }
    }

    private void switchAttackSet() {
        int[] attackSetBox = {1}; 
        
        switch (name) {
            case "Ordinary_Virus": attackSetBox = new int[]{5, 5, 5, 1}; break; 
            case "Extraordinary_Virus": attackSetBox = new int[]{1, 6}; break; 
            case "Sir_Virus": attackSetBox = new int[]{4, 4, 8, 9}; break; 
            case "Baron_Virus": attackSetBox = new int[]{3, 3, 3, 1}; break; 
            case "Viscount_Virus": attackSetBox = new int[]{2, 2, 2, 1}; break; 
            case "Count_Virus": attackSetBox = new int[]{2, 2, 2, 1}; break; 
            case "Duke_Virus": attackSetBox = new int[]{3, 3, 4, 4, 9, 8}; break; 
            case "Grand_Duke_Virus": attackSetBox = new int[]{5, 5, 5}; break; 
            case "King_Virus": attackSetBox = new int[]{2, 2, 4, 4, 8, 9}; break; 
            case "Emperor_Virus": attackSetBox = new int[]{4, 4, 6}; break; 
            case "God_Virus": attackSetBox = new int[]{1, 2, 3, 4, 5, 7, 8, 9}; break; 
            default: if(name.endsWith("_Virus")) attackSetBox = new int[]{1, 2, 3, 4, 5, 7, 8, 9}; break;
        }
        
        if (!name.equals("Dying_Virus")) {
            intendedAttackSet = attackSetBox[(int)(Math.random() * attackSetBox.length)];
            currentAttackSet = intendedAttackSet;
        }
    }

    private void switchMovePattern() {
        int[] movePatternBox = {1};
        
        switch (name) {
            case "Ordinary_Virus": movePatternBox = new int[]{4, 5, 6}; break; 
            case "Extraordinary_Virus": movePatternBox = new int[]{7, 7, 8, 9, 10}; break; 
            case "Sir_Virus": movePatternBox = new int[]{3, 4, 5, 6}; break; 
            case "Baron_Virus": movePatternBox = new int[]{3, 4, 5, 6}; break;
            case "Viscount_Virus": movePatternBox = new int[]{3, 5, 6}; break;
            case "Count_Virus": movePatternBox = new int[]{3, 5, 9, 10}; break;
            case "Duke_Virus": movePatternBox = new int[]{4, 5, 6}; break;
            case "Grand_Duke_Virus": movePatternBox = new int[]{7, 8, 9, 10, 10}; break; 
            case "King_Virus": movePatternBox = new int[]{4, 5, 6, 9}; break;
            case "Emperor_Virus": movePatternBox = new int[]{7, 7, 7, 8, 9, 10, 10}; break; 
            case "God_Virus": movePatternBox = new int[]{4, 5, 6, 7, 8, 9, 10}; break;
            default: if(name.endsWith("_Virus")) movePatternBox = new int[]{3, 4, 5, 6, 7, 8, 9, 10}; break;
        }
        
        if (!name.equals("Dying_Virus")) {
            currentMovePattern = movePatternBox[(int)(Math.random() * movePatternBox.length)];
            currentDashCount = 0; 
            
            if (currentMovePattern == 6 || currentMovePattern == 5 || currentMovePattern == 9) {
                targetDistance = 150 + Math.random() * 200;
                orbitDirection = (Math.random() < 0.5) ? 1 : -1;
            } else if (currentMovePattern == 4) {
                randomMoveAngle = Math.random() * Math.PI * 2; 
            } else if (currentMovePattern == 10) {
                dashAngle = Math.random() * Math.PI * 2;
            } else if (currentMovePattern == 7 || currentMovePattern == 8) {
                dashAngle = Math.atan2(target.y - y, target.x - x);
            }
        }
    }

    public void draw(Graphics2D g2d) {
        if (name.equals("God_Virus")) g2d.setColor(new Color(138, 43, 226));
        else if (name.equals("Count_Virus")) g2d.setColor(new Color(138, 43, 226));
        else g2d.setColor(Color.RED);
        
        g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        g2d.setColor(Color.WHITE);
        g2d.drawString(name, (int) x - 20, (int) y - size/2 - 15);
        
        g2d.setColor(Color.RED);
        g2d.fillRect((int) x - 20, (int) y - size / 2 - 10, 40, 5);
        g2d.setColor(Color.GREEN);
        g2d.fillRect((int) x - 20, (int) y - size / 2 - 10, (int) (40 * (Math.max(0, hp) / maxHp)), 5);
        
        if (activeAttack == 5 && attackSubTimer < 60) {
             g2d.setColor(new Color(255, 0, 0, 100)); 
             double angle = Math.atan2(target.y - y, target.x - x);
             java.awt.Stroke oldStroke = g2d.getStroke();
             g2d.setStroke(new java.awt.BasicStroke(sniperBulletSize, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
             g2d.drawLine((int)x, (int)y, (int)(x + Math.cos(angle)*2500), (int)(y + Math.sin(angle)*2500));
             g2d.setStroke(oldStroke);
        }
    }
    
    public void randomizeStats() {
        this.name = "Mutated_Virus";
        this.maxHp = 2000 + Math.random() * 10000; 
        this.hp = this.maxHp;
        this.baseSpeed = 2.0 + Math.random() * 3.5;
        this.attackDamage = 50 + (int)(Math.random() * 80);
        this.size = 50 + (int)(Math.random() * 60);
        
        this.intendedAttackSet = 1 + (int)(Math.random() * 9); 
        this.currentMovePattern = 1 + (int)(Math.random() * 10); 
        
        this.moveChangeCooldown = 60 + (int)(Math.random() * 120);
        this.attackChangeCooldown = 60 + (int)(Math.random() * 120);
    }
    
    public boolean isDead() { return this.hp <= 0; }
}