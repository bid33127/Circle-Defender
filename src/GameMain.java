import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * CustomButton UI Component
 */
class CustomButton extends JButton {
    public CustomButton(String text) {
        super(text);
        setFont(new Font("Impact", Font.BOLD, 48));
        setForeground(Color.WHITE);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String t = getText();
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(t)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g2d.setFont(getFont());

        g2d.setColor(Color.GRAY);
        g2d.drawString(t, x + 4, y + 4);

        g2d.setColor(Color.BLACK);
        g2d.drawString(t, x - 2, y - 2);
        g2d.drawString(t, x + 2, y - 2);
        g2d.drawString(t, x - 2, y + 2);
        g2d.drawString(t, x + 2, y + 2);

        if (getModel().isRollover()) g2d.setColor(Color.CYAN);
        else g2d.setColor(Color.WHITE);
        g2d.drawString(t, x, y);
    }
}

/**
 * GameMain.java
 * Main execution class and window manager.
 */
public class GameMain extends JFrame {

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel mainMenuPanel;
    private GamePanel gamePanel;
    
    private CustomButton continueButton;
    private CustomButton newGamePlusButton;

    public GameMain() {
        setTitle("Circle Defender");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        gamePanel = new GamePanel(this);

        createMainMenu();

        cardPanel.add(mainMenuPanel, "MainMenu");
        cardPanel.add(gamePanel, "Game");

        add(cardPanel);
        showMainMenu(); 
    }

    private void createMainMenu() {
        mainMenuPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(15, 15, 15));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int maxRadius = (int) Math.hypot(cx, cy);
                
                g2d.setColor(new Color(255, 255, 255, 120)); 
                for (int i = 0; i < 24; i++) {
                    double angle = i * (Math.PI * 2 / 24);
                    int endX = (int) (cx + Math.cos(angle) * maxRadius);
                    int endY = (int) (cy + Math.sin(angle) * maxRadius);
                    g2d.drawLine(cx, cy, endX, endY);
                }
                
                for (int i = 1; i <= 35; i++) {
                    int radius = (int) (Math.pow(1.25, i) * 15); 
                    if (radius > maxRadius * 1.5) break; 
                    g2d.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
                }
                
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRoundRect(cx - 400, cy - 250, 800, 100, 30, 30);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Impact", Font.BOLD, 90));
                String title = "CIRCLE DEFENDER";
                int tw = g2d.getFontMetrics().stringWidth(title);
                g2d.drawString(title, cx - tw/2, cy - 170);
            }
        };
        
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(150, 0, 0, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        CustomButton playButton = new CustomButton("New Game");
        newGamePlusButton = new CustomButton("New Game+");
        continueButton = new CustomButton("Continue"); 
        CustomButton quitButton = new CustomButton("Quit");

        playButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Game");
            gamePanel.startGame(false); 
            gamePanel.requestFocusInWindow(); 
        });

        newGamePlusButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Game");
            gamePanel.startGame(true); 
            gamePanel.requestFocusInWindow(); 
        });
        
        continueButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Game");
            gamePanel.continueGame(); 
            gamePanel.requestFocusInWindow(); 
        });
        
        quitButton.addActionListener(e -> System.exit(0));

        gbc.gridy = 0; mainMenuPanel.add(playButton, gbc);
        gbc.gridy = 1; mainMenuPanel.add(newGamePlusButton, gbc);
        gbc.gridy = 2; mainMenuPanel.add(continueButton, gbc);
        gbc.gridy = 3; mainMenuPanel.add(quitButton, gbc);
    }

    public void showMainMenu() {
        continueButton.setVisible(gamePanel.getHighestStage() > 0 && gamePanel.getHighestStage() < 12);
        newGamePlusButton.setVisible(gamePanel.getHighestStage() >= 12);
        cardLayout.show(cardPanel, "MainMenu");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameMain game = new GameMain();
            game.setVisible(true);
        });
    }
}

/**
 * GamePanel class
 * Core game loop, collision detection, entity management, and rendering.
 */
class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener {

    private GameMain mainFrame;
    private Thread gameThread;
    
    private boolean running = false;
    private boolean isPaused = false; 
    private boolean isGameOver = false;
    private int gameOverTimer = 0;
    
    private int mouseX = 0, mouseY = 0;
    private boolean isLeftClicking = false;
    private boolean godMode = false;
    
    private final int FPS = 60;
    private final long TARGET_TIME = 1000 / FPS;

    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Particle> particles = new ArrayList<>(); 
    Player player; 

    private boolean up, down, left, right;
    
    private int currentStage = 0;
    private int highestStage = 0; 
    
    private boolean isTransitioning = false;
    private boolean waitingForF = false; 
    private int transitionTimer = 0;
    private double scrollTextY = 0; 
    
    private JPanel pauseMenuUI;
    private JPanel howToPlayUI;
    private JPanel defeatMenuUI;
    
    private final String[] creditLines = {
        "Evolution complete. You have transcended. ",
        "\n",
        " No virus can defeat you anymore.",
        "\n","\n","\n",
        "Thank you for defending the system.",
        "", "",
    };

    public GamePanel(GameMain mainFrame) {
        this.mainFrame = mainFrame; 
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
            @Override
            public void mouseDragged(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });
        
        setupUI();
    }
    
    private void setupUI() {
        this.setLayout(new GridBagLayout()); 
        
        pauseMenuUI = new JPanel(new GridLayout(4, 1, 15, 15));
        pauseMenuUI.setOpaque(false);
        CustomButton resumeBtn = new CustomButton("Resume");
        CustomButton htpBtn = new CustomButton("How to Play");
        CustomButton lobbyBtn = new CustomButton("Return to Lobby");
        CustomButton quitBtn = new CustomButton("Quit");
        pauseMenuUI.add(resumeBtn); pauseMenuUI.add(htpBtn);
        pauseMenuUI.add(lobbyBtn); pauseMenuUI.add(quitBtn);
        
        defeatMenuUI = new JPanel(new GridLayout(4, 1, 15, 15));
        defeatMenuUI.setOpaque(false);
        defeatMenuUI.setBorder(BorderFactory.createEmptyBorder(200, 0, 0, 0));
        CustomButton retryBtn = new CustomButton("Retry");
        CustomButton htpBtnDefeat = new CustomButton("How to Play");
        CustomButton lobbyBtnDefeat = new CustomButton("Return to Lobby");
        CustomButton quitBtnDefeat = new CustomButton("Quit");
        defeatMenuUI.add(retryBtn); defeatMenuUI.add(htpBtnDefeat);
        defeatMenuUI.add(lobbyBtnDefeat); defeatMenuUI.add(quitBtnDefeat);
        
        howToPlayUI = new JPanel(new BorderLayout(10, 10));
        howToPlayUI.setOpaque(false);
        JTextArea htpText = new JTextArea(
            "==== HOW TO PLAY ====\n\n" +
            "WASD : Move (Lv0)\n" +
            "Left Click : Normal Shot (Lv1) / Sniper Charge (Lv2)\n" +
            "Spacebar : Dash (Lv3)\n" +
            "E : Shield (Lv4)\n" +
            "Right Click : Rapid Fire (Lv5)\n" +
            "Q : Explosive Shot (Lv6)\n\n" +
            "F : Next Stage\n" +
            "ESC : Pause Game\n" +
            "I : Toggle God Mode\n" +
            "K : Insta-Kill All Enemies"
        );
        htpText.setEditable(false); htpText.setOpaque(false);
        htpText.setForeground(Color.WHITE); htpText.setFont(new Font("Tahoma", Font.BOLD, 18)); 
        CustomButton backBtn = new CustomButton("Back");
        howToPlayUI.add(htpText, BorderLayout.CENTER); howToPlayUI.add(backBtn, BorderLayout.SOUTH);
        
        this.add(pauseMenuUI); this.add(defeatMenuUI); this.add(howToPlayUI);
        pauseMenuUI.setVisible(false); defeatMenuUI.setVisible(false); howToPlayUI.setVisible(false);
        
        resumeBtn.addActionListener(e -> { isPaused = false; pauseMenuUI.setVisible(false); requestFocusInWindow(); });
        htpBtn.addActionListener(e -> { pauseMenuUI.setVisible(false); howToPlayUI.setVisible(true); });
        lobbyBtn.addActionListener(e -> { stopForLobby(); mainFrame.showMainMenu(); });
        quitBtn.addActionListener(e -> System.exit(0));
        
        retryBtn.addActionListener(e -> { 
            isGameOver = false; defeatMenuUI.setVisible(false); 
            startFromStage(currentStage, player.unlockLevel); requestFocusInWindow(); 
        });
        htpBtnDefeat.addActionListener(e -> { defeatMenuUI.setVisible(false); howToPlayUI.setVisible(true); });
        lobbyBtnDefeat.addActionListener(e -> { stopForLobby(); mainFrame.showMainMenu(); });
        quitBtnDefeat.addActionListener(e -> System.exit(0));
        
        backBtn.addActionListener(e -> { 
            howToPlayUI.setVisible(false); 
            if (isGameOver) defeatMenuUI.setVisible(true);
            else pauseMenuUI.setVisible(true);
        });
    }
    
    public int getHighestStage() { return highestStage; }

    public void stopForLobby() {
        running = false; isPaused = false;
        pauseMenuUI.setVisible(false); defeatMenuUI.setVisible(false); howToPlayUI.setVisible(false);
    }

    public void startGame(boolean keepLevel) { 
        highestStage = 0;
        int savedLvl = keepLevel && player != null ? player.unlockLevel : 0;
        startFromStage(0, savedLvl);
    }
    
    public void continueGame() { 
        startFromStage(highestStage, player != null ? player.unlockLevel : 0);
    }

    private void startFromStage(int stage, int startLevel) {
        player = new Player(Toolkit.getDefaultToolkit().getScreenSize().width / 2.0, 
                            Toolkit.getDefaultToolkit().getScreenSize().height - 150.0); 
        
        int targetLevel = Math.max(stage, startLevel);
        for(int i = 0; i < targetLevel; i++) {
            if (player.unlockLevel < 11) player.levelUp();
        }
        
        player.hp = player.maxHp; 
        currentStage = stage; 
        bullets.clear(); enemies.clear(); particles.clear();
        isGameOver = false;
        gameOverTimer = 0;
        
        if (currentStage >= 12) {
            godMode = true;
            scrollTextY = Toolkit.getDefaultToolkit().getScreenSize().height + 50;
            waitingForF = false;
        } else {
            waitingForF = true; 
        }
        
        running = true; isPaused = false; godMode = (currentStage >= 12);
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this); gameThread.start();
        }
    }

    private void spawnStageEnemies() {
        enemies.clear(); 
        int spawnX = getWidth() / 2;
        int spawnY = 150; 
        if (currentStage <= 11) {
            String bossName = "";
            switch (currentStage) {
                case 0: bossName = "Dying_Virus"; break;
                case 1: bossName = "Ordinary_Virus"; break;
                case 2: bossName = "Extraordinary_Virus"; break;
                case 3: bossName = "Sir_Virus"; break;
                case 4: bossName = "Baron_Virus"; break;
                case 5: bossName = "Viscount_Virus"; break;
                case 6: bossName = "Count_Virus"; break;
                case 7: bossName = "Duke_Virus"; break;
                case 8: bossName = "Grand_Duke_Virus"; break;
                case 9: bossName = "King_Virus"; break;
                case 10: bossName = "Emperor_Virus"; break;
                case 11: bossName = "God_Virus"; break;
            }
            enemies.add(new Enemy(spawnX, spawnY, bossName, player, bullets));
        }
    }

    private void spawnParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) particles.add(new Particle(x, y, color));
    }
    private void spawnBlastWave(double x, double y, Color color, double radius) {
        particles.add(new Particle(x, y, color, radius));
    }
    
    private String getSkillMessage(int stage) {
        switch (stage) {
            case 1: return "Unlocked: Left Click (Normal Shot)";
            case 2: return "Unlocked: Hold Left Click (Sniper Shot)";
            case 3: return "Unlocked: Spacebar (Dash)";
            case 4: return "Unlocked: E (BulletShield)";
            case 5: return "Unlocked: Right Click (Rapid Fire)";
            case 6: return "Unlocked: Q (Explosive Shot)";
            case 7: return "Upgrade: Normal Shot (Larger, Faster, Piercing)";
            case 8: return "Upgrade: Sniper Shot (Massive Damage)";
            case 9: return "Upgrade: Rapid Fire (Faster, Stronger)";
            case 10: return "Upgrade: Shield (More Bullets)";
            case 11: return "Upgrade: Explosive Shot (Massive Radius)";
            default: return "Stats Upgraded! Enemies are getting stronger.";
        }
    }

    @Override
    public void run() {
        long startTime, waitTime, elapsedTime;
        while (running) {
            startTime = System.nanoTime();
            if (!isPaused && !waitingForF) update(); 
            repaint(); 
            elapsedTime = System.nanoTime() - startTime;
            waitTime = TARGET_TIME - elapsedTime / 1000000;
            if (waitTime < 0) waitTime = 5;
            try { Thread.sleep(waitTime); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void update() {
        if (player == null || waitingForF) return; 
        
        if (player.hp <= 0 && !isGameOver && !godMode) {
            isGameOver = true;
            gameOverTimer = 60; 
        }
        
        if (isGameOver) {
            if (gameOverTimer > 0) {
                gameOverTimer--;
                if (gameOverTimer <= 0) defeatMenuUI.setVisible(true);
            }
            return;
        }
        
        if (currentStage >= 12) {
            updateEndgameBot();
        } else {
            if (isLeftClicking && player.unlockLevel >= 2) {
                player.chargeTimer++;
                player.isCharging = true;
                if (player.chargeTimer >= 30) {
                    player.shootSniper(mouseX, mouseY, bullets);
                    player.chargeTimer = 0; isLeftClicking = false; player.isCharging = false;
                }
            } else if (!isLeftClicking) {
                player.chargeTimer = 0; player.isCharging = false;
            }
        }

        player.update(up, down, left, right, getWidth(), getHeight(), bullets);
        
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update(getWidth(), getHeight());
            if (b.hasParticles && !b.isDead() && Math.random() < 0.3) {
                spawnParticles(b.x, b.y, b.isExplosive ? Color.MAGENTA : Color.CYAN, 1);
            }
        }
        for (int i = 0; i < particles.size(); i++) particles.get(i).update();
        for (int i = 0; i < enemies.size(); i++) enemies.get(i).update(getWidth(), getHeight());

        // --- Core Collision Detectors ---
        for (Enemy e : enemies) {
            if (e.hp > 0) {
                if (Math.hypot(player.x - e.x, player.y - e.y) < (player.size / 2.0 + e.size / 2.0)) {
                    if (player.invincibleTimer <= 0) { 
                        player.hp -= e.contactDamage; 
                        if (godMode && player.hp < 1) player.hp = 1; 
                        player.invincibleTimer = 60;   
                        spawnParticles(player.x, player.y, Color.RED, 15);
                        e.hp -= player.damage;
                        spawnParticles(e.x, e.y, Color.ORANGE, 10);
                    }
                }
            }
        }

        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (b.hp > 0) {
                if (b.isPlayerOwned) {
                    for (Enemy e : enemies) {
                        if (e.hp > 0 && Math.hypot(b.x - e.x, b.y - e.y) < (b.size / 2.0 + e.size / 2.0)) {
                            e.hp -= b.damage; b.hp = b.isExplosive ? 0 : b.hp - 1; 
                            spawnParticles(b.x, b.y, Color.ORANGE, 5);
                        }
                    }
                } else {
                    if (Math.hypot(b.x - player.x, b.y - player.y) < (b.size / 2.0 + player.size / 2.0)) {
                        if (player.invincibleTimer <= 0) { 
                            player.hp -= b.damage; 
                            if (godMode && player.hp < 1) player.hp = 1; 
                            b.hp = 0; spawnParticles(b.x, b.y, Color.RED, 5);
                        }
                    }
                }
                
                for (int j = i + 1; j < bullets.size(); j++) {
                    Bullet b2 = bullets.get(j);
                    if (b2.hp > 0 && b.isPlayerOwned != b2.isPlayerOwned) {
                        if (Math.hypot(b.x - b2.x, b.y - b2.y) < (b.size / 2.0 + b2.size / 2.0)) {
                            int tempHp = b.hp;
                            b.hp -= b2.damage;
                            b2.hp -= tempHp;
                            spawnParticles(b.x, b.y, Color.YELLOW, 3);
                        }
                    }
                }
            }
            
            if (b.isDead() && b.isExplosive) {
                Color blastColor = b.isPlayerOwned ? Color.YELLOW : Color.ORANGE;
                spawnBlastWave(b.x, b.y, blastColor, b.explosionRadius);
                spawnParticles(b.x, b.y, Color.ORANGE, 20); spawnParticles(b.x, b.y, Color.WHITE, 10);
                
                if (b.isPlayerOwned) {
                    for (Enemy e : enemies) {
                        if (Math.hypot(b.x - e.x, b.y - e.y) <= b.explosionRadius/2.0 + e.size/2.0) {
                            e.hp -= b.damage;
                        }
                    }
                    
                    for (Bullet enemyB : bullets) {
                        if (!enemyB.isPlayerOwned && enemyB.hp > 0) {
                            if (Math.hypot(b.x - enemyB.x, b.y - enemyB.y) <= b.explosionRadius/2.0 + enemyB.size/2.0) {
                                enemyB.hp = 0; 
                                spawnParticles(enemyB.x, enemyB.y, Color.GRAY, 3); 
                            }
                        }
                    }
                } else {
                    if (Math.hypot(b.x - player.x, b.y - player.y) <= b.explosionRadius/2.0 + player.size/2.0 && player.invincibleTimer <= 0) {
                        player.hp -= b.damage; if (godMode && player.hp < 1) player.hp = 1;
                    }
                }
            }
        }

        // Entity Cleanup
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (b.isDead() || b.x < -100 || b.x > getWidth() + 100 || b.y < -100 || b.y > getHeight() + 100) {
                bullets.remove(i);
            }
        }
        
        for (int i = particles.size() - 1; i >= 0; i--) {
            if (particles.get(i).life <= 0) {
                particles.remove(i);
            }
        }
        
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (e.hp <= 0) { spawnParticles(e.x, e.y, Color.GRAY, 20); enemies.remove(i); }
        }

        // --- Stage Transition Trigger ---
        if (enemies.isEmpty() && !isTransitioning && !waitingForF && currentStage <= 11) {
            isTransitioning = true;
            transitionTimer = 60; 
            bullets.clear();   
        }

        if (isTransitioning) {
            transitionTimer--;
            if (transitionTimer <= 0) {
                isTransitioning = false; 
                currentStage++;
                highestStage = Math.max(highestStage, currentStage); 
                
                if (currentStage <= 11) {
                    player.levelUp(); 
                    waitingForF = true;
                    
                    player.x = getWidth() / 2.0;       
                    player.y = getHeight() - 150.0;
                    up = down = left = right = false;  
                    isLeftClicking = false; player.isCharging = false; player.chargeTimer = 0;
                } else {
                    scrollTextY = getHeight() + 50;
                    godMode = true;
                }
            }
        }
    }
    
    private void updateEndgameBot() {
        if (enemies.isEmpty()) {
            int count = 1 + (int)(Math.random() * 3); 
            for(int i = 0; i < count; i++) {
                Enemy e = new Enemy(getWidth()/2 + (Math.random()*400 - 200), 150, "random_virus", player, bullets);
                e.randomizeStats(); 
                enemies.add(e);
            }
        }
        
        Enemy target = null; double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            double d = Math.hypot(e.x - player.x, e.y - player.y);
            if (d < minDist && e.hp > 0) { minDist = d; target = e; }
        }
        
        if (target != null) {
            mouseX = (int)target.x; mouseY = (int)target.y;
            if (minDist > 250) { up = player.y > target.y; down = player.y < target.y; left = player.x > target.x; right = player.x < target.x; } 
            else if (minDist < 150) { up = player.y < target.y; down = player.y > target.y; left = player.x < target.x; right = player.x > target.x; }
            
            isLeftClicking = true; 
            if (Math.random() < 0.05) player.dash();
            if (Math.random() < 0.02) player.castShield(bullets);
            if (Math.random() < 0.02) player.shootExplosive(mouseX, mouseY, bullets);
            if (Math.random() < 0.02) player.shootRapid(mouseX, mouseY, bullets);
        }
        
        scrollTextY -= 1.5; 
        if(scrollTextY < -(creditLines.length * 60 + 200)) scrollTextY = getHeight() + 100; 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(10, 5, 15)); 
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        Color gridColor;
        if (currentStage >= 12) gridColor = new Color(255, 255, 255, 150); 
        else if (currentStage <= 5) gridColor = new Color(0, 255, 0, 120); 
        else if (currentStage <= 10) gridColor = new Color(255, 120, 0, 120); 
        else gridColor = new Color(255, 0, 50, 120); 

        int cx = getWidth() / 2; int cy = getHeight() / 2;
        int maxRadius = (int) Math.hypot(cx, cy);
        
        for (int i = 0; i < 24; i++) {
            double angle = i * (Math.PI * 2 / 24);
            g2d.setColor(gridColor);
            g2d.drawLine(cx, cy, (int)(cx + Math.cos(angle)*maxRadius), (int)(cy + Math.sin(angle)*maxRadius));
        }   
        for (int i = 1; i <= 35; i++) {
            int radius = (int) (Math.pow(1.22, i) * 15); 
            if (radius > maxRadius * 1.5) break; 
            g2d.setColor(gridColor);
            g2d.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }
        
        if (player != null && !waitingForF && !isGameOver) {
            player.draw(g2d);
            if (player.isCharging && player.chargeTimer >= 9) {
                g2d.setColor(new Color(255, 0, 0, 150)); 
                double aimAngle = Math.atan2(mouseY - player.y, mouseX - player.x);
                g2d.drawLine((int)player.x, (int)player.y, (int)(player.x + Math.cos(aimAngle)*2000), (int)(player.y + Math.sin(aimAngle)*2000));
            }
        }

        try { for (int i = 0; i < enemies.size(); i++) enemies.get(i).draw(g2d); } catch (Exception ignored) {}
        try { for (int i = 0; i < bullets.size(); i++) bullets.get(i).draw(g2d); } catch (Exception ignored) {}
        try { for (int i = 0; i < particles.size(); i++) particles.get(i).draw(g2d); } catch (Exception ignored) {}
        
        if (isGameOver) {
            int alpha = (int)(255 * (1.0 - (gameOverTimer / 60.0)));
            alpha = Math.max(0, Math.min(255, alpha));
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (gameOverTimer <= 0) {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Impact", Font.BOLD, 100));
                String goText = "YOU BEEN DEFEATED";
                int goWidth = g2d.getFontMetrics().stringWidth(goText);
                g2d.drawString(goText, getWidth()/2 - goWidth/2, getHeight()/2 - 180);
            }
        }
        
        if (isTransitioning) {
            int alpha = (int)(255 * (1.0 - (transitionTimer / 60.0)));
            alpha = Math.max(0, Math.min(255, alpha));
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        if (waitingForF && !isTransitioning) {
            g2d.setColor(new Color(0, 0, 0, 200)); 
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(Color.WHITE);
            if (currentStage == 0) {
                g2d.setFont(new Font("Tahoma", Font.BOLD, 30));
                String text1 = "You are an anti-virus program, but you are no ordinary program."; 
                String text2 = "You were built with an Evolution Engine that allows you to analyze and learn.";
                String text3 = "Let's run, destroy, and evolve.";
                g2d.drawString(text1, getWidth()/2 - g2d.getFontMetrics().stringWidth(text1)/2, getHeight()/2 - 40);
                g2d.drawString(text2, getWidth()/2 - g2d.getFontMetrics().stringWidth(text2)/2, getHeight()/2 + 10);
                g2d.drawString(text3, getWidth()/2 - g2d.getFontMetrics().stringWidth(text3)/2, getHeight()/2 + 60);
            } else {
                g2d.setFont(new Font("Arial", Font.BOLD, 50));
                String text1 = "LEVEL UP! - STAGE " + currentStage + " CLEARED";
                g2d.drawString(text1, getWidth()/2 - g2d.getFontMetrics().stringWidth(text1)/2, getHeight()/2 - 60);
                
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                String text2 = getSkillMessage(currentStage);
                g2d.drawString(text2, getWidth()/2 - g2d.getFontMetrics().stringWidth(text2)/2, getHeight()/2);
            }
            
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String text3 = ">>> Press 'F' to Continue <<<";
            g2d.drawString(text3, getWidth()/2 - g2d.getFontMetrics().stringWidth(text3)/2, getHeight()/2 + 100);
        }
        
        if (currentStage >= 12 && !isTransitioning) {
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Tahoma", Font.BOLD, 40));
            for (int i = 0; i < creditLines.length; i++) {
                int stringWidth = g2d.getFontMetrics().stringWidth(creditLines[i]);
                g2d.drawString(creditLines[i], getWidth()/2 - stringWidth/2, (int)scrollTextY + (i * 60));
            }
        }

        if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_ESCAPE) {
            if (!isPaused && running && !waitingForF && !isGameOver) {
                isPaused = true;            
                pauseMenuUI.setVisible(true); 
                howToPlayUI.setVisible(false);
            } else if (isPaused) {
                isPaused = false;
                pauseMenuUI.setVisible(false);
                howToPlayUI.setVisible(false);
            }
            return;
        }
        
        if (key == KeyEvent.VK_F && waitingForF && currentStage <= 11) {
            waitingForF = false;
            player.hp = player.maxHp; 
            spawnStageEnemies();
        }
        
        if (isPaused || currentStage >= 12 || waitingForF || isGameOver) return; 

        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_D) right = true;
        
        if (player != null) {
            if (key == KeyEvent.VK_SPACE && player.unlockLevel >= 3) player.dash(); 
            if (key == KeyEvent.VK_E && player.unlockLevel >= 4) player.castShield(bullets); 
            if (key == KeyEvent.VK_Q && player.unlockLevel >= 6) player.shootExplosive(mouseX, mouseY, bullets); 
        }
        
        if (key == KeyEvent.VK_I) godMode = !godMode;
        if (key == KeyEvent.VK_K) for (Enemy enemy : enemies) enemy.hp = 0;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_D) right = false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (player == null || isPaused || currentStage >= 12 || waitingForF || isGameOver) return;
        
        if (e.getButton() == MouseEvent.BUTTON1) { 
            if (player.unlockLevel >= 1) { 
                isLeftClicking = true;
                if (player.unlockLevel >= 2) { 
                    player.isCharging = true; 
                    player.chargeTimer = 0;
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3 && player.unlockLevel >= 5) { 
            player.shootRapid(mouseX, mouseY, bullets); 
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (player == null || isPaused || currentStage >= 12 || waitingForF || isGameOver) {
            isLeftClicking = false;
            if (player != null) player.isCharging = false;
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            isLeftClicking = false; player.isCharging = false;
            if (player.unlockLevel >= 1) {
                if (player.unlockLevel == 1 || player.chargeTimer < 30) {
                    player.shootNormal(mouseX, mouseY, bullets);
                }
                player.chargeTimer = 0;
            }
        }
    }
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}