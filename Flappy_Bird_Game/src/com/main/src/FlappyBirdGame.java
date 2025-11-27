package com.main.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FlappyBirdGame extends JPanel implements ActionListener, KeyListener {

    private int width = 800;
    private int height = 600;

    // Bird properties
    private int birdX = 100;
    private int birdY = 300;
    private int birdSize = 30;
    private int velocity = 0;
    private int gravity = 1;

    // Pipe properties
    private ArrayList<Rectangle> pipes;
    private int pipeWidth = 80;
    private int pipeGap = 200;
    private int pipeSpeed = 5;

    // Game state
    private enum GameState { MENU, PLAYING, GAME_OVER }
    private GameState gameState = GameState.MENU;

    private boolean gameOver = false;
    private boolean started = false;
    private int score = 0;

    // High score & dashboard
    private int highScore = 0;
    private boolean highScoreBroken = false;
    private List<Integer> topScores = new ArrayList<>(); // session-only leaderboard

    private Timer timer;
    private Random random;

    public FlappyBirdGame() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.CYAN);
        setFocusable(true);
        addKeyListener(this);

        pipes = new ArrayList<>();
        random = new Random();
        timer = new Timer(20, this);
    }

    // --- Core game control ---

    public void startGame() {
        birdY = 300;
        velocity = 0;
        score = 0;
        gameOver = false;
        started = true;
        highScoreBroken = false;
        pipes.clear();
        gameState = GameState.PLAYING;
        timer.start();
    }

    public void goToMenu() {
        // Reset basic things but keep scores
        gameOver = false;
        started = false;
        velocity = 0;
        birdY = 300;
        pipes.clear();
        gameState = GameState.MENU;
        timer.stop();
        repaint();
    }

    public void addPipe() {
        int pipeHeight = 100 + random.nextInt(250);
        pipes.add(new Rectangle(width, 0, pipeWidth, pipeHeight));
        pipes.add(new Rectangle(width, pipeHeight + pipeGap, pipeWidth, height - pipeHeight - pipeGap));
    }

    // --- Rendering ---

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Common background stuff can go here in future if needed

        switch (gameState) {
            case MENU:
                drawMenu(g);
                break;
            case PLAYING:
                drawGame(g);
                break;
            case GAME_OVER:
                drawGame(g);  // Draw final frame
                drawGameOverOverlay(g);
                break;
        }
    }

    private void drawGame(Graphics g) {
        // Draw bird
        g.setColor(Color.YELLOW);
        g.fillOval(birdX, birdY, birdSize, birdSize);
        g.setColor(Color.ORANGE);
        g.fillOval(birdX + 20, birdY + 10, 8, 8); // Eye
        g.setColor(Color.RED);
        g.fillPolygon(new int[]{birdX + 25, birdX + 35, birdX + 25},
                new int[]{birdY + 15, birdY + 20, birdY + 25}, 3); // Beak

        // Draw pipes
        g.setColor(Color.GREEN);
        for (Rectangle pipe : pipes) {
            g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(pipe.x, pipe.y, pipe.width, pipe.height);
            g.setColor(Color.GREEN);
        }

        // Draw score (top-left)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Score: " + score, 20, 40);

        // Draw high score (top-right)
        String hsText = "High: " + highScore;
        FontMetrics fm = g.getFontMetrics();
        int hsWidth = fm.stringWidth(hsText);
        g.drawString(hsText, width - hsWidth - 20, 40);

        // High score broken banner
        if (highScoreBroken) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            String msg = "NEW HIGH SCORE!";
            FontMetrics fm2 = g.getFontMetrics();
            int msgWidth = fm2.stringWidth(msg);
            g.drawString(msg, (width - msgWidth) / 2, 40);
        }
    }

    private void drawMenu(Graphics g) {
        // Background text for menu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Flappy Bird";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, 150);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        String instr = "Press SPACE to Start";
        int instrWidth = g.getFontMetrics().stringWidth(instr);
        g.drawString(instr, (width - instrWidth) / 2, 220);

        // High score at top-right even on menu
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String hsText = "High: " + highScore;
        int hsWidth = g.getFontMetrics().stringWidth(hsText);
        g.drawString(hsText, width - hsWidth - 20, 40);

        // Top Score Dashboard
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("Top Score Dashboard", 40, 280);

        g.setFont(new Font("Arial", Font.PLAIN, 22));
        if (topScores.isEmpty()) {
            g.drawString("No scores yet. Play a game!", 40, 320);
        } else {
            int y = 320;
            int rank = 1;
            for (Integer s : topScores) {
                g.drawString(rank + ". " + s, 60, y);
                y += 30;
                rank++;
                if (rank > 5) break; // show top 5 scores
            }
        }
    }

    private void drawGameOverOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String over = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int overWidth = fm.stringWidth(over);
        g.drawString(over, (width - overWidth) / 2, height / 2 - 60);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String retry = "Press SPACE to Try Again";
        String mainMenu = "Press ESC to Main Menu";
        int retryWidth = g.getFontMetrics().stringWidth(retry);
        int mainWidth = g.getFontMetrics().stringWidth(mainMenu);

        g.drawString(retry, (width - retryWidth) / 2, height / 2 + 10);
        g.drawString(mainMenu, (width - mainWidth) / 2, height / 2 + 50);
    }

    // --- Game loop ---

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING && !gameOver && started) {
            // Bird physics
            velocity += gravity;
            birdY += velocity;

            // Add new pipes
            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < width - 300) {
                addPipe();
            }

            // Move pipes and check for scoring
            for (int i = 0; i < pipes.size(); i++) {
                Rectangle pipe = pipes.get(i);
                pipe.x -= pipeSpeed;

                // Score when passing the upper pipe edge (simple check)
                if (pipe.y == 0 && pipe.x + pipeWidth == birdX) {
                    score++;

                    // Check high score break
                    if (score > highScore) {
                        if (score > highScore) {
                            highScoreBroken = true;
                        }
                        highScore = score;
                    }
                }

                // Remove off-screen pipes
                if (pipe.x + pipeWidth < 0) {
                    pipes.remove(pipe);
                    i--;
                }
            }

            // Check collisions
            Rectangle birdRect = new Rectangle(birdX, birdY, birdSize, birdSize);
            for (Rectangle pipe : pipes) {
                if (birdRect.intersects(pipe)) {
                    triggerGameOver();
                    break;
                }
            }

            // Check boundaries
            if (birdY > height - birdSize || birdY < 0) {
                triggerGameOver();
            }
        }

        repaint();
    }

    private void triggerGameOver() {
        gameOver = true;
        gameState = GameState.GAME_OVER;
        timer.stop();

        // Update top scores dashboard
        if (score > 0) {
            topScores.add(score);
            // Sort descending
            Collections.sort(topScores, Collections.reverseOrder());
            // Optional: limit list size
            if (topScores.size() > 10) {
                topScores = new ArrayList<>(topScores.subList(0, 10));
            }
        }
    }

    // --- Input ---

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE) {
            switch (gameState) {
                case MENU:
                    startGame();
                    break;
                case PLAYING:
                    velocity = -15; // Flap
                    break;
                case GAME_OVER:
                    startGame(); // Try again
                    break;
            }
        } else if (key == KeyEvent.VK_ESCAPE) {
            if (gameState == GameState.GAME_OVER || gameState == GameState.PLAYING) {
                goToMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    // --- Bootstrapping ---

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBirdGame game = new FlappyBirdGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
