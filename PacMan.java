//red and orange ghosts target pacman
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.swing.*;
import javax.sound.sampled.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            int speed = tileSize / 4; // Set the same speed for all entities
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -speed;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = speed;
            } else if (this.direction == 'L') {
                this.velocityX = -speed;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = speed;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image cherryImage;
    private Image scaredGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private Clip wakaSound;
    private Clip eatGhostSound;
    private Clip powerDotSound;
    private Clip gameOverSound;
    Timer cherryTimer;
    Timer scaredTimer;
    Timer gameTimer;
    int timeLeft = 80; // 80 seconds
    private boolean isPaused = false;
   
   
    //X = wall, O = skip, P = pac man, ' ' = food, C = cherry
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "XXXX X       X XXXX",
        "XXXX X XXrXX X XXXX",
        "X       bpo       X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "X  X X  XXXX X X XX",
        "X  C      X   X   X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadSounds();
        loadMap();
        for (Block ghost : ghosts) {
            if (ghost.image == redGhostImage) {
                targetPacmanWithAStar(ghost);
            } else {
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
        cherryTimer = new Timer(15000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeCherry();
            }
        });
        cherryTimer.start();

        try {
            gameOverSound = AudioSystem.getClip();
            AudioInputStream gameOverSoundStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/gameOver.wav"));
            gameOverSound.open(gameOverSoundStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                if (timeLeft <= 0) {
                    gameOver = true;
                    lives = 0;
                    gameLoop.stop();
                    gameTimer.stop();
                    gameOverSound.setFramePosition(0); // Reset sound clip
                    gameOverSound.start();
                    showGameOverPage();
                }
                repaint();
            }
        });
        gameTimer.start();
    }

    private void loadSounds() {
        try {
            wakaSound = AudioSystem.getClip();
            AudioInputStream wakaSoundStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/waka.wav"));
            wakaSound.open(wakaSoundStream);

            eatGhostSound = AudioSystem.getClip();
            AudioInputStream eatGhostSoundStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/eat_ghost.wav"));
            eatGhostSound.open(eatGhostSoundStream);

            powerDotSound = AudioSystem.getClip();
            AudioInputStream powerDotSoundStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/power_dot.wav"));
            powerDotSound.open(powerDotSoundStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                else if (tileMapChar == 'C') { // cherry
                    Block cherry = new Block(cherryImage, x, y, tileSize, tileSize);
                    foods.add(cherry);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            if (food.image != null) {
                g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
            } else {
                g.fillRect(food.x, food.y, food.width, food.height);
            }
        }
        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }

        // Draw timer
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Time Left: " + timeLeft + "s", boardWidth - 150, tileSize / 2);
    }

    private class Node implements Comparable<Node> {
        int x, y, g, h;
        Node parent;

        Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        int f() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f(), other.f());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private List<Node> findPath(int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashSet<Node> closedList = new HashSet<>();
        openList.add(new Node(startX, startY, 0, Math.abs(goalX - startX) + Math.abs(goalY - startY), null));

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            if (current.x == goalX && current.y == goalY) {
                List<Node> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedList.add(current);

            for (char direction : directions) {
                int newX = current.x;
                int newY = current.y;
                if (direction == 'U') newY -= tileSize;
                if (direction == 'D') newY += tileSize;
                if (direction == 'L') newX -= tileSize;
                if (direction == 'R') newX += tileSize;

                if (newX < 0 || newY < 0 || newX >= boardWidth || newY >= boardHeight) continue;

                boolean isWall = false;
                for (Block wall : walls) {
                    if (collision(new Block(null, newX, newY, tileSize, tileSize), wall)) {
                        isWall = true;
                        break;
                    }
                }
                if (isWall) continue;

                Node neighbor = new Node(newX, newY, current.g + 1, Math.abs(goalX - newX) + Math.abs(goalY - newY), current);
                if (closedList.contains(neighbor)) continue;

                boolean inOpenList = false;
                for (Node node : openList) {
                    if (node.equals(neighbor) && node.g <= neighbor.g) {
                        inOpenList = true;
                        break;
                    }
                }
                if (!inOpenList) {
                    openList.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private void targetPacmanWithAStar(Block ghost) {
        List<Node> path = findPath(ghost.x, ghost.y, pacman.x, pacman.y);
        if (path != null && path.size() > 1) {
            Node nextStep = path.get(1);
            if (nextStep.x > ghost.x) ghost.updateDirection('R');
            else if (nextStep.x < ghost.x) ghost.updateDirection('L');
            else if (nextStep.y > ghost.y) ghost.updateDirection('D');
            else if (nextStep.y < ghost.y) ghost.updateDirection('U');
        }
    }

    private boolean ghostsCollide(Block ghost1, Block ghost2) {
        return ghost1.x == ghost2.x && ghost1.y == ghost2.y;
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collisions
        Block ghostEaten = null;
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (ghost.image == scaredGhostImage) {
                    score += 200;
                    ghostEaten = ghost;
                    eatGhostSound.setFramePosition(0); // Reset sound clip
                    eatGhostSound.start();
                } else {
                    lives--;
                    if (lives == 0) {
                        gameOver = true;
                        gameLoop.stop();
                        gameTimer.stop();
                        gameOverSound.setFramePosition(0); // Reset sound clip
                        gameOverSound.start();
                        showGameOverPage();
                        return;
                    }
                    eatGhostSound.stop(); // Ensure the sound is stopped before resetting
                    eatGhostSound.setFramePosition(0); // Reset sound clip
                    eatGhostSound.start();
                    resetPositions();
                    break; // Ensure to break the loop after collision
                }
            }

            if (ghost.image == redGhostImage) {
                targetPacmanWithAStar(ghost);
                ghost.x += ghost.velocityX;
                ghost.y += ghost.velocityY;
                for (Block wall : walls) {
                    if (collision(ghost, wall)) {
                        ghost.x -= ghost.velocityX;
                        ghost.y -= ghost.velocityY;
                        char newDirection = directions[random.nextInt(4)];
                        ghost.updateDirection(newDirection);
                    }
                }
            } else {
                if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                    ghost.updateDirection('U');
                }
                ghost.x += ghost.velocityX;
                ghost.y += ghost.velocityY;
                for (Block wall : walls) {
                    if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                        ghost.x -= ghost.velocityX;
                        ghost.y -= ghost.velocityY;
                        char newDirection = directions[random.nextInt(4)];
                        ghost.updateDirection(newDirection);
                    }
                }
            }
        }

        // Ensure red and orange ghosts do not coincide
        Block redGhost = null;
        Block orangeGhost = null;
        for (Block ghost : ghosts) {
            if (ghost.image == redGhostImage) {
                redGhost = ghost;
            } else if (ghost.image == orangeGhostImage) {
                orangeGhost = ghost;
            }
        }
        if (redGhost != null && orangeGhost != null && ghostsCollide(redGhost, orangeGhost)) {
            // Move red ghost in a random direction
            char newDirection = directions[random.nextInt(4)];
            redGhost.updateDirection(newDirection);
            redGhost.x += redGhost.velocityX;
            redGhost.y += redGhost.velocityY;
            for (Block wall : walls) {
                if (collision(redGhost, wall)) {
                    redGhost.x -= redGhost.velocityX;
                    redGhost.y -= redGhost.velocityY;
                    break;
                }
            }

            // Move orange ghost in a random direction
            newDirection = directions[random.nextInt(4)];
            orangeGhost.updateDirection(newDirection);
            orangeGhost.x += orangeGhost.velocityX;
            orangeGhost.y += orangeGhost.velocityY;
            for (Block wall : walls) {
                if (collision(orangeGhost, wall)) {
                    orangeGhost.x -= orangeGhost.velocityX;
                    orangeGhost.y -= orangeGhost.velocityY;
                    break;
                }
            }
        }

        ghosts.remove(ghostEaten);

        //check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                if (food.image == cherryImage) {
                    score += 50;
                    powerDotSound.setFramePosition(0); // Reset sound clip
                    powerDotSound.start();
                    scareGhosts();
                } else {
                    score += 10;
                    wakaSound.setFramePosition(0); // Reset sound clip
                    wakaSound.start();
                }
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
            cherryTimer.restart();
        }
    }

    private void showGameOverPage() {
        JFrame gameOverFrame = new JFrame("Game Over");
        gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameOverFrame.add(new GameOverPage(score));
        gameOverFrame.pack();
        gameOverFrame.setLocationRelativeTo(null);
        gameOverFrame.setVisible(true);
        ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    public void placeCherry() {
        int emptyTileCount = 0;
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (tileMap[r].charAt(c) == ' ') {
                    emptyTileCount++;
                }
            }
        }
        if (emptyTileCount == 0) return;

        int targetEmptyTile = random.nextInt(emptyTileCount);
        int currentEmptyTile = 0;
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c <columnCount; c++) {
                if (tileMap[r].charAt(c) == ' ') {
                    if (currentEmptyTile == targetEmptyTile) {
                        int x = c * tileSize;
                        int y = r * tileSize;
                        Block cherry = new Block(cherryImage, x, y, tileSize, tileSize);
                        foods.add(cherry);
                        return;
                    }
                    currentEmptyTile++;
                }
            }
        }
    }

    public void scareGhosts() {
        for (Block ghost : ghosts) {
            ghost.image = scaredGhostImage;
        }
        if (scaredTimer != null) {
            scaredTimer.stop();
        }
        scaredTimer = new Timer(7000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGhostAppearance();
            }
        });
        scaredTimer.setRepeats(false);
        scaredTimer.start();
    }

    public void resetGhostAppearance() {
        for (Block ghost : ghosts) {
            if (ghost.image == scaredGhostImage) {
                if (tileMap[ghost.startY / tileSize].charAt(ghost.startX / tileSize) == 'b') {
                    ghost.image = blueGhostImage;
                } else if (tileMap[ghost.startY / tileSize].charAt(ghost.startX / tileSize) == 'o') {
                    ghost.image = orangeGhostImage;
                } else if (tileMap[ghost.startY / tileSize].charAt(ghost.startX / tileSize) == 'p') {
                    ghost.image = pinkGhostImage;
                } else if (tileMap[ghost.startY / tileSize].charAt(ghost.startX / tileSize) == 'r') {
                    ghost.image = redGhostImage;
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            move();
            repaint();
            if (gameOver) {
                gameLoop.stop();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            timeLeft = 80; // Reset timer
            gameOver = false;
            gameLoop.start();
            gameTimer.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isPaused = !isPaused;
            if (isPaused) {
                gameLoop.stop();
                gameTimer.stop();
            } else {
                gameLoop.start();
                gameTimer.start();
            }
        }
    }
}







