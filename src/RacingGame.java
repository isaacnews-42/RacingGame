import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;

public class RacingGame extends JPanel implements KeyListener {
    private int playerX = 250;
    private int playerY = 450; // Starting Y position of the car
    private int playerSpeed = 5;
    private int score = 0;
    private int highScore = 0;
    private int obstacleSpeed = 3;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel gamePanel;
    private JPanel instructionsPanel;

    private Image Car;
    private Image Obstacle;
    private Image Road;

    private int[] obstacleXs = new int[5];
    private int[] obstacleYs = new int[5];
    private int activeObstacles = 1;

    private Clip engineClip;
    private Clip crashClip;

    public RacingGame() {
        // Load images
        Car = new ImageIcon("lamboveneno/lamboveneno.png").getImage();
        Obstacle = new ImageIcon("brickwall/brickwall.png").getImage();
        Road = new ImageIcon("road/road.png").getImage();

        loadSounds();

        addKeyListener(this);
        setFocusable(true);
        requestFocus();
        setBackground(Color.GREEN);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        gamePanel = new JPanel(new BorderLayout());
        instructionsPanel = new JPanel(new BorderLayout());

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            gameStarted = true;
            requestFocusInWindow();
            engineClip.loop(Clip.LOOP_CONTINUOUSLY); //engine sound plays on loop once you hit "Start" button
        });

        JButton tryAgainButton = new JButton("Try Again");
        tryAgainButton.addActionListener(e -> {
            resetGame();
            requestFocusInWindow();
        });

        JButton endGameButton = new JButton("End Game");
        endGameButton.addActionListener(e -> System.exit(0));

        JButton howToPlayButton = new JButton("How to Play");
        howToPlayButton.addActionListener(e -> cardLayout.show(mainPanel, "Instructions"));

        JButton backToGameButton = new JButton("Back to Game");
        backToGameButton.addActionListener(e -> cardLayout.show(mainPanel, "Game"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(tryAgainButton);
        buttonPanel.add(endGameButton);
        buttonPanel.add(howToPlayButton);

        gamePanel.add(buttonPanel, BorderLayout.SOUTH);
        gamePanel.add(this, BorderLayout.CENTER);

        JTextArea instructionsText = new JTextArea(
                "How to Play:\n" +
                        "- Use the LEFT and RIGHT arrow keys to move the car.\n" +
                        "- Avoid hitting obstacles to keep the game going.\n" +
                        "- Try to score as many points as possible!\n" +
                        "- For every brick wall you dodge you get 1 point\n" +
                        "- The game gets harder for each 5 points you score.\n" +
                        "- Press 'Try Again' to restart the game if you crash.\n" +
                        "- Have fun!"
        );
        instructionsText.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        instructionsText.setForeground(Color.WHITE);
        instructionsText.setBackground(Color.BLACK);
        instructionsText.setEditable(false);
        instructionsPanel.add(new JScrollPane(instructionsText), BorderLayout.CENTER);
        instructionsPanel.add(backToGameButton, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, "Game");
        mainPanel.add(instructionsPanel, "Instructions");

        cardLayout.show(mainPanel, "Game");

        JFrame frame = new JFrame("Racing Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mainPanel);
        frame.setSize(700, 700);
        frame.setVisible(true);

        Timer timer = new Timer(1000 / 60, e -> {
            if (gameStarted) {
                updateGame();
                repaint();
            }
        });
        timer.start();
    }

    private void loadSounds() {
        try {
            // Lambo sound
            File engineFile = new File("soundeffects/Lamborghini-Aventador-Sound-Effect.wav");
            AudioInputStream engineStream = AudioSystem.getAudioInputStream(engineFile);
            engineClip = AudioSystem.getClip();
            engineClip.open(engineStream);

            // Crash sound
            File crashFile = new File("soundeffects/Car-Crash-Sound-Effect.wav");
            AudioInputStream crashStream = AudioSystem.getAudioInputStream(crashFile);
            crashClip = AudioSystem.getClip();
            crashClip.open(crashStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGame() {
        if (!gameOver) {
            obstacleSpeed = 3 + (score / 5);
            activeObstacles = 1 + (score / 5);

            for (int i = 0; i < activeObstacles; i++) {
                obstacleYs[i] += obstacleSpeed;

                if (obstacleYs[i] > 600) {
                    obstacleYs[i] = -50;
                    obstacleXs[i] = new Random().nextInt(500);
                    score++;
                }

                Rectangle carHitbox = new Rectangle(playerX + 20, playerY + 20, 110, 160);
                Rectangle obstacleHitbox = new Rectangle(obstacleXs[i], obstacleYs[i], 150, 150);

                if (carHitbox.intersects(obstacleHitbox)) {
                    gameOver = true;
                    if (score > highScore) {
                        highScore = score;
                    }
                    engineClip.stop();
                    crashClip.start();
                }
            }

            if (playerX < 0) playerX = 0;
            if (playerX + 150 > 700) playerX = 700 - 150;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.drawImage(Road, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(Car, playerX, playerY, 150, 200, this);
        for (int i = 0; i < activeObstacles; i++) {
            g.drawImage(Obstacle, obstacleXs[i], obstacleYs[i], 150, 150, this);
        }

        g.setFont(new Font("Times New Roman", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 20, 40);
        g.drawString("High Score: " + highScore, 20, 70);

        if (gameOver) {
            g.setFont(new Font("Times New Roman", Font.BOLD, 36));
            g.setColor(Color.RED);
            g.drawString("Game Over!", 200, 300);
        }
    }

    private void resetGame() {
        playerX = 250;
        playerY = 450; // Reset to default position
        score = 0;
        obstacleSpeed = 3;
        gameOver = false;
        gameStarted = false;

        for (int i = 0; i < obstacleXs.length; i++) {
            obstacleXs[i] = new Random().nextInt(500);
            obstacleYs[i] = -50;
        }

        crashClip.stop();
        crashClip.setFramePosition(0); // Resets crash sound
        engineClip.stop(); // Stop engine sound
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || !gameStarted) {
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            playerX -= playerSpeed;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            playerX += playerSpeed;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new RacingGame();
    }
}




