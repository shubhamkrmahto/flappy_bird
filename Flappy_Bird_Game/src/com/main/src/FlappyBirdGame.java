package com.main.src;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
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
    private boolean gameOver = false;
    private boolean started = false;
    private int score = 0;
    
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

    public void startGame() {
        birdY = 300;
        velocity = 0;
        score = 0;
        gameOver = false;
        started = true;
        pipes.clear();
        timer.start();
    }

    public void addPipe() {
        int pipeHeight = 100 + random.nextInt(250);
        pipes.add(new Rectangle(width, 0, pipeWidth, pipeHeight));
        pipes.add(new Rectangle(width, pipeHeight + pipeGap, pipeWidth, height - pipeHeight - pipeGap));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
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
        
        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Score: " + score, 20, 40);
        
        // Draw instructions or game over
        if (!started) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Press SPACE to Start", width/2 - 200, height/2);
        }
        
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", width/2 - 150, height/2 - 50);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Press SPACE to Restart", width/2 - 180, height/2 + 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && started) {
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
                
                // Score when passing through pipe gap
                if (pipe.x + pipeWidth == birdX && pipe.y == 0) {
                    score++;
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
                    gameOver = true;
                    timer.stop();
                }
            }
            
            // Check boundaries
            if (birdY > height - birdSize || birdY < 0) {
                gameOver = true;
                timer.stop();
            }
        }
        
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!started || gameOver) {
                startGame();
            } else {
                velocity = -15; // Flap
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

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