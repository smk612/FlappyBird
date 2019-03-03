package flappyBird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{
    /*Creating an object of the class*/
    public static FlappyBird flappyBird;
    /*Setting Height and Width Variables of the frame; Choose them so that they fit your screen perfectly(DON'T)*/
    public final int WIDTH = 800, HEIGHT = 800;
    /*Creating an instance of the Renderer class*/
    public Renderer renderer;
    /*Creating the playable character*/
    public Rectangle bird;
    /*We store all the columns here*/
    public ArrayList<Rectangle> columns;
    /*Variables to keep track of stuff*/
    public int ticks, yMotion, score, highscore;
    /*Variables to keep track of Game State*/
    public boolean gameOver, started;
    /*Used later to generate random column heights*/
    public Random rand;
    
    /*Default Constructor*/
    public FlappyBird()
    {
    	/*Creating a new JFrame*/
        JFrame jframe = new JFrame();
    	Timer timer = new Timer(20, this);//Using the Java swing timer and not the util one
        
	renderer = new Renderer();//Initiating the renderer
	rand = new Random();

	jframe.add(renderer);
	jframe.setTitle("Flappy Bird");//Setting the title of the frame
	jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//Setting the program to terminate on close
	jframe.setSize(WIDTH, HEIGHT);//Setting the size of the frame
	jframe.addMouseListener(this);//Adding both mouse and key listener for interaction
	jframe.addKeyListener(this);
	jframe.setResizable(false);//We don't want the user to change the height & width of the frame
	jframe.setVisible(true);//Displaying the frame at last

	bird = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);//Making the bird appear a little off the centre of the frame
	columns = new ArrayList<Rectangle>();

	addColumn(true);
	addColumn(true);
	addColumn(true);
	addColumn(true);

	timer.start();
    }

    /*Creating one new column randomly*/
    public void addColumn(boolean start)
    {
    	int space = 300;//This indicates the space that the bird will have to fly through
        int width = 100;//This indicates the width of each column
    	int height = 50 + rand.nextInt(300);//The height of each column is generated randomly; 50<height<350
        
        /*If it is true, that means we are going to start the game*/
    	if (start)
    	{
            /*Lower pipe; the 300 scoots this to the right side of the screen;
            the height is so that it comes down to the grass*/
            columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height));
            /*Upper pipe*/
            columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space));
	}
	else
	{
            /*Getting the last column and placing the next lower pipe at 600 pixel(to the right) from it*/
            columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height));
            /*Dont have to add anything for the upper pipe as the column created above is now the last one for this*/
            columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space));
            /*Doing -1 from columns.size to get the index; .x gives us the starting x-coordinate*/
	}
    }
    
    /*Painting the columns on the frame*/
    public void paintColumn(Graphics g, Rectangle column)
    {
	g.setColor(Color.green.darker());
	g.fillRect(column.x, column.y, column.width, column.height);
    }

    public void jump()
    {
	
        /*If game is over, reset everything*/
        if (gameOver)
	{
            bird = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);//Reset bird position
            columns.clear();//Clear list of columns
            yMotion = 0;//Reset yMotion(duh)
            score = 0;//Reset Score

            addColumn(true);
            addColumn(true);
            addColumn(true);
            addColumn(true);

            gameOver = false;//So that on next click it starts again
	}
        
        /*Starts the game on first click*/
	if (!started)
	{
            started = true;
	}
        /*Game has started but it's not over*/
        else if (!gameOver)
	{
            /*Everytime you click, it is set to 0 so that gravity doesn't pull it down much*/
            if (yMotion > 0)
            {
		yMotion = 0;
            }
            /*Gravity at work, pulling the bird down*/
            yMotion -= 10;
	}
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
	int speed = 10;
        
        ticks++;
        
        /*Movement starts only when game has started*/
	if (started)
	{
            
            /*Decreasing x-coordinate by speed so that it seems as if it is moving left*/
            for (int i = 0; i < columns.size(); i++)
            {
		Rectangle column = columns.get(i);
		column.x -= speed;
            }
            
            /*Increase the y-axis-motion of the bird so that it seems as if it jumps when an action is performed*/
            if (ticks % 2 == 0 && yMotion < 15)
            {
		yMotion += 2;
            }

            /*Checking all columns to remove ones to the left*/
            for (int i = 0; i < columns.size(); i++)
            {
		Rectangle column = columns.get(i);
                /*If column is out of the left of the frame, remove it from the ArrayList*/
		if (column.x + column.width < 0)
		{
                    columns.remove(column);
                    
                    /*We add 2 pipes for each upper pipe that is removed; We don't want to repeat calling addColumn 2 times*/
                    if (column.y == 0)
                    {
                    	addColumn(false);
                    }
		}
            }
            
            /*Increase the y coordinate so as to give motion*/
            bird.y += yMotion;

            /*Collision Detection and Score Keeping*/
            for (Rectangle column : columns)
            {
		/*If bird reaches center of the column, add 1 to score; checks only for the upper pipe; -10 to account for the x-axis-speed*/
                if (column.y == 0 && bird.x + bird.width / 2 > column.x + column.width / 2 - 10 && bird.x + bird.width / 2 < column.x + column.width / 2 + 10)
		{
                    score++;
		}
                
                /*Collision Detection with the columns*/
		if (column.intersects(bird))
		{
                    gameOver = true;
                    /*If the bird hits the column wall, then it stays before it*/
                    if (bird.x <= column.x)
                    {
			bird.x = column.x - bird.width;
                    }
                    else
                    {
                        /*If bird hits lower pipe top then it stays on top of it*/
			if (column.y != 0)
			{
                            bird.y = column.y - bird.height;
			}
                        /*If bird hits upper pipe bottom so that it doesn't look as if it's sliding through it*/
			else if (bird.y < column.height)
			{
                            bird.y = column.height;
			}
                    }
		}
            }

            /*Collision Detection for when the bird touches the ground OR it flies away*/
            if (bird.y > HEIGHT - 120 || bird.y < 0)
            {
		gameOver = true;
            }
            /*Condition ensures that the bird gradually falls down instead of at once*/
            if (bird.y + yMotion >= HEIGHT - 120)
            {
		bird.y = HEIGHT - 120 - bird.height;
		gameOver = true;
            }
	}

	renderer.repaint();
    }

    /*Repainting the frame*/
    public void repaint(Graphics g)
    {
	/*Creating the sky*/
        g.setColor(Color.cyan);
	g.fillRect(0, 0, WIDTH, HEIGHT);
        
        /*Creating the ground*/
	g.setColor(Color.orange);
	g.fillRect(0, HEIGHT - 120, WIDTH, 120);
        
        /*Planting some grass on top of the ground*/
	g.setColor(Color.green);
	g.fillRect(0, HEIGHT - 120, WIDTH, 20);
        
        /*Coloring the bird red*/
	g.setColor(Color.red);
	g.fillRect(bird.x, bird.y, bird.width, bird.height);

        /*For each rectangle in columns ArrayList, paint it*/
	for (Rectangle column : columns)
	{
            paintColumn(g, column);
	}

	g.setColor(Color.white);
	g.setFont(new Font("Arial", 1, 100));
        
        /*Shows for the 1st time*/
	if (!started)
	{
            g.drawString("Click to start!", 75, HEIGHT / 2 - 50);
	}

        /*Shows when the game is over*/
	if (gameOver)
	{
            g.drawString("Game Over!", 100, HEIGHT / 2 - 50);
        }
        
        /*Shows the score at the top when game is ongoing*/
	if (!gameOver && started)
	{
            /*Used to keep track of the highscore in the current session*/
            if(score>highscore)
                highscore=score;
            /*The 75*.. is used so that it always displayed in the center of the frame*/
            g.drawString(String.valueOf(score)+"|"+String.valueOf(highscore), WIDTH / 2 - 50*(String.valueOf(highscore).length()), 100);
	}
    }

    /*Main function(at last!)*/
    public static void main(String[] args)
    {
        /*Creating a new instance of the class object*/
        flappyBird = new FlappyBird();
    }
    
    /*When mouse is clicked, jump*/
    @Override
    public void mouseClicked(MouseEvent e)
    {
	jump();
    }

    /*When space is clicked, jump*/
    @Override
    public void keyReleased(KeyEvent e)
    {
	if (e.getKeyCode() == KeyEvent.VK_SPACE)
	{
            jump();
	}
    }

    /*All these actions are not used in the program*/
    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

}