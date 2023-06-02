import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GamePanel extends JPanel implements Runnable {
    int Width = 1000;
    int Height = (int) (Width * (0.555));
    Dimension screen = new Dimension(Width, Height);

    int Paddle_Width = 25;
    int Paddle_Height = 100;

    int ball_diameter = 20;

    Paddle p1, p2;
    Ball ball;
    Score score = new Score(Width, Height);

    Thread gameThread;

    private final JButton playAgainButton;
    private boolean gameOver; // Track game state

    GamePanel() {
        setPreferredSize(screen);
        setBackground(Color.BLACK);
        gameThread = new Thread(this);
        addKeyListener(new AL());

        setFocusable(true);

        gameThread.start();
        newPaddle();
        newBall();

        // Create "Play Again" button
        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds(Width / 2 - 75, Height / 2 + 50, 150, 50);
        playAgainButton.addActionListener(e -> resetGame());
        playAgainButton.setVisible(false);
        add(playAgainButton);

        gameOver = false; // Initialize gameOver to false
    }

    private void newBall() {
        Random random = new Random();
        ball = new Ball(Width / 2, random.nextInt(Height - ball_diameter), ball_diameter, ball_diameter);
    }

    private void newPaddle() {
        p1 = new Paddle(0, (Height - Paddle_Height) / 2, Paddle_Width, Paddle_Height, 1);
        p2 = new Paddle(Width - Paddle_Width, (Height - Paddle_Height) / 2, Paddle_Width, Paddle_Height, 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);

        if (score.player1 >= 5 || score.player2 >= 5) {
            String message;
            if (score.player1 > score.player2) {
                message = "Red Player 1 wins!";
            } else {
                message = "Blue Player 2 wins!";
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            FontMetrics fm = g.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            int messageHeight = fm.getHeight();
            int x = (getWidth() - messageWidth) / 2;
            int y = (getHeight() - messageHeight) / 2;
            g.drawString(message, x, y);

            playAgainButton.setBounds(Width / 2 - 75, y + messageHeight + 50, 150, 50);
            playAgainButton.setVisible(true);
        }
    }

    private void draw(Graphics g) {
        p1.draw(g);
        p2.draw(g);
        ball.draw(g);
        score.draw(g);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1 && !gameOver) {
                move();
                checkCollision();
                delta--;
                repaint();
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCollision() {
        if (ball.y <= 0) {
            ball.setYDirection(-ball.yVelocity);
        }
        if (ball.y >= Height - ball_diameter) {
            ball.setYDirection(-ball.yVelocity);
        }
        if (ball.intersects(p1)) {
            ball.xVelocity = -ball.xVelocity;
            ball.xVelocity++;
            if (ball.yVelocity > 0) {
                ball.yVelocity++;
            } else {
                ball.yVelocity--;
            }
            ball.setYDirection(ball.yVelocity);
            ball.setXDirection(ball.xVelocity);
        }

        if (ball.intersects(p2)) {
            ball.xVelocity = -ball.xVelocity;
            ball.xVelocity--;
            if (ball.yVelocity > 0) {
                ball.yVelocity++;
            } else {
                ball.yVelocity--;
            }
            ball.setYDirection(ball.yVelocity);
            ball.setXDirection(ball.xVelocity);
        }

        if (p1.y <= 0) {
            p1.y = 0;
        }
        if (p1.y >= Height - Paddle_Height) {
            p1.y = Height - Paddle_Height;
        }
        if (p2.y <= 0) {
            p2.y = 0;
        }
        if (p2.y >= Height - Paddle_Height) {
            p2.y = Height - Paddle_Height;
        }
        if (ball.x >= Width - ball_diameter) {
            newBall();
            newPaddle();
            score.player1++;
        }
        if (ball.x <= 0) {
            newPaddle();
            newBall();
            score.player2++;

        }

        if(score.player1 == 5 || score.player2 == 5){
            gameOver = true;
        }
    }

    private void move() {
        p1.move();
        p2.move();
        ball.move();
    }

    private void resetGame() {
        gameOver = false;
        score.player1 = 0;
        score.player2 = 0;
        newPaddle();
        newBall();
        playAgainButton.setVisible(false);
        requestFocusInWindow();  // Ensure keyboard focus is maintained
    }

    public class AL extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            p1.KeyPressed(e);
            p2.KeyPressed(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            p1.KeyReleased(e);
            p2.KeyReleased(e);
        }
    }
}
