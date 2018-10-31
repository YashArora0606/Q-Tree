/**
 * BounceTester.java
 * Version 1.0
 * @author Yash Arora
 * October 30th, 2018
 * The following code is the information for a bouncing ball simulation with a QuadTree for collision detection
 */

// Graphics & GUI imports
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;

// Keyboard imports
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Utilities
import java.util.ArrayList;

// This is the class that holds all of the information for the actual simulation part of the code
class BounceTester extends JFrame {

	// These are static variables that dictate how the code should be run
	static int STARTING_NUM_BALLS = 100000;
	static int CAPACITY_BEFORE_SPLITTING = 4;
	static int BALLS_TO_ADD_ON_KEYPRESS = 10;
	static int SCREEN_RESOLUTION_CONSTANT = 23;
	static boolean COLOR_MODE = true;
	static int MAX_DIVISIONS = 10;
	static double VELOCITY_MAXIMUM = 1;
	static boolean VISUALIZATION_MODE = true;
	
	// These variables are used in the calculation of the frames per second
	double currentTime;
	double previousFrameTime;
	int count = 0;
	double fps = 0;

	// These are used as the initial screen dimensions, but change if the user resizes the screen
	int currentDimensionX = 800;
	int currentDimensionY = 800;

	// ArrayList that stores all of the balls that exist
	ArrayList<BouncingBall> balls = new ArrayList<BouncingBall>();
	
	// QuadTree that will be used to make collision detection more efficient
	QuadTree qTree;
	
	// Total boundary of the initial QuadTree
	Rectangle boundary = new Rectangle(currentDimensionX / 2, currentDimensionY / 2, currentDimensionX / 2, currentDimensionY / 2);
	
	// Required variables for displaying to screen through a window in java
	static JFrame window;
	JPanel gamePanel;

	// Main method simply creates a window
	public static void main(String[] args) {
		window = new BounceTester();
	}

	/**
	 * BounceTester
	 * Constructor that sets the required attributes to the window
	 */
	BounceTester() {
		
		// All these are required attributes for the window and KeyListner
		super("Bouncing Ball Battle Royale");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocation(0, 0);
		this.setSize(currentDimensionX + 1, currentDimensionY + SCREEN_RESOLUTION_CONSTANT);
		gamePanel = new GameAreaPanel();
		this.add(new GameAreaPanel());
		MyKeyListener keyListener = new MyKeyListener();
		this.addKeyListener(keyListener);
		this.requestFocusInWindow();
		this.setResizable(true);
		this.setVisible(true);
		
		// Create the QuadTree with the given boundary, capacity before splitting, and initial level of 1
		qTree = new QuadTree(boundary, CAPACITY_BEFORE_SPLITTING, 1);
	}
	
	// Inner class that allows things to be drawn to the screen in java
	private class GameAreaPanel extends JPanel {
		
		/**
		 * paintComponent
		 * Method that is called every frame to draw all of the objects onto the screen and perform actions per frame
		 * @param a Graphics object to draw to the screen
		 */
		public void paintComponent(Graphics g) {
			
			// Calculates FPS
	        currentTime = System.nanoTime();
	        if (count % 60 == 0) {
	        	fps = (1/((currentTime-previousFrameTime)))*Math.pow(10, 9);
	        }
	        previousFrameTime = currentTime;
	        
			// Generating the balls in this method allows tens of thousands of balls to be generated without error
			count++;
			if (count == 1) {
				for (int i = 0; i < STARTING_NUM_BALLS; i++) {
					balls.add(new BouncingBall(currentDimensionX, currentDimensionY));
				}
			}
			
			// Clears the existing QuadTree
			qTree.clear();

			// Add all the balls into the QuadTree
			for (int i = 0; i < balls.size(); i++) {
				qTree.insert(balls.get(i));
				
				// Balls are white in visualization mode
				if(VISUALIZATION_MODE) {
					balls.get(i).setColor(Color.WHITE);
				}
			}
			
			// Visualization mode slows everything down
			if (VISUALIZATION_MODE) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Allow the user to resize the screen and dynamically change the QuadTree
			enableResizing();

			// This is required to draw to the screen
			super.paintComponent(g);
			setDoubleBuffered(true);
			
			// Checks through each smallest existing QuadTree node for collisions
			iterateThroughBallsInTree(qTree);
			
			// Call method to draw the background
			drawBackground(g);

			// Call method to draw all the balls
			drawBalls(g);

			// Call method to draw the boundaries of the QuadTree
			drawBounds(g, qTree);
			
			// Show FPS
			g.drawString("FPS: " + (int) fps, 20, 20);
			
			// Update the positions of the balls
			updatePositions();
			
			// Display again to the screen
			repaint();
		}
		
		/**
		 * drawBackground
		 * Method to draw the background
		 * @param a Graphics object to draw to the screen
		 */
		public void drawBackground(Graphics g) {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, currentDimensionX, currentDimensionY);
		}
		
		/**
		 * iterateThroughBallsInTree
		 * Method that recursively iterates through the tree or all its nodes and calls method to check collisions
		 * @param a QuadTree that holds a certain number of balls or nodes that lead to more balls
		 */
		public void iterateThroughBallsInTree(QuadTree qTree) {
			
			// If the QuadTree or node has not divided, check for collisions in that segment
			if (!qTree.isDivided()) {
				checkCollisions(qTree.getBallsInTree());
				
				// Otherwise, repeat this with all the subsegments
			} else {
				iterateThroughBallsInTree(qTree.northeast);
				iterateThroughBallsInTree(qTree.northwest);
				iterateThroughBallsInTree(qTree.southeast);
				iterateThroughBallsInTree(qTree.southwest);
			}
		}
		
		/**
		 * checkCollisions
		 * Checks collisions ONLY with balls within the same smallest rectangle
		 * @param an ArrayList of all the balls in a certain smallest subsegment
		 */
		public void checkCollisions(ArrayList<BouncingBall> ballsThatCanCollide) {
						
			// Efficiently checks the collisions given only the balls in the segment
			for (int i = 0; i < ballsThatCanCollide.size(); i++) {
				for (int j = i; j < ballsThatCanCollide.size(); j++) {
					
					// Balls can collide if seeIfCollided returns true, and if they are not the same ball
					if (ballsThatCanCollide.get(i) != ballsThatCanCollide.get(j) && 
							seeIfCollided(ballsThatCanCollide.get(i), ballsThatCanCollide.get(j))) {
						
						// Call method to commit action on ball collision
						collideBalls(ballsThatCanCollide.get(i), ballsThatCanCollide.get(j));
					}
				}
			}
		}
		
		/**
		 * collideBalls
		 * Method to perform an action upon ball collision, given two balls
		 * @param the first ball in the collision (a) and the second ball in the collision (b)
		 */
		private void collideBalls(BouncingBall a, BouncingBall b) {
			
			// Use TLAP to calculate required variables
			double xDiff = b.getX() - a.getX();
			double yDiff = b.getY() - a.getY();
			double length = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
			if (length == 0) {
				length = 1;
			}
			double cosTheta = xDiff/length;
			double sinTheta = yDiff/length;
			double radiusSum = ((double)a.getDiameter())/2 + ((double)b.getDiameter())/2;
			
			// Reset the position of one of the balls so that they are no longer colliding
			b.setX((int)(a.getX() + ((radiusSum + 1) * cosTheta)));
			b.setY((int)(a.getY() + ((radiusSum + 1) * sinTheta)));

			// Swap the balls' angles and recalculate velocities using TLAP
			
			// Find old x and y velocities
			double oldVay = a.getyChange();
			double oldVax = a.getxChange();
			double oldVby = b.getyChange();
			double oldVbx = b.getxChange();

			// Calculate the magnitudes of the new x and y velocities
			double newVay = Math.sqrt(Math.pow(oldVay, 2) + Math.pow(oldVax, 2)) * Math.sin(Math.atan((oldVby) / (oldVbx)));
			double newVax = Math.sqrt(Math.pow(oldVay, 2) + Math.pow(oldVax, 2)) * Math.cos(Math.atan((oldVby) / (oldVbx)));
			double newVby = Math.sqrt(Math.pow(oldVby, 2) + Math.pow(oldVbx, 2)) * Math.sin(Math.atan((oldVay) / (oldVax)));
			double newVbx = Math.sqrt(Math.pow(oldVby, 2) + Math.pow(oldVbx, 2)) * Math.cos(Math.atan((oldVay) / (oldVax)));

			// Check to ensure that the balls are going in the correct direction
			if (((oldVay > 0) && (newVby < 0)) || ((oldVay < 0) && (newVby > 0))) {
				newVby = newVby * (-1);
			}
			if (((oldVax > 0) && (newVbx < 0)) || ((oldVax < 0) && (newVbx > 0))) {
				newVbx = newVbx * (-1);
			}
			if (((oldVby > 0) && (newVay < 0)) || ((oldVby < 0) && (newVay > 0))) {
				newVay = newVay * (-1);
			}
			if (((oldVbx > 0) && (newVax < 0)) || ((oldVbx < 0) && (newVax > 0))) {
				newVax = newVax * (-1);
			}
			
			// Set the balls' x and y velocities
			a.setyChange(newVay);
			a.setxChange(newVax);
			b.setyChange(newVby);
			b.setxChange(newVbx);
		}

		/**
		 * seeIfCollided
		 * Method that checks if two balls are colliding
		 * @param the first ball in the collision (a) and the second ball in the collision (b)
		 * @return boolean true if the balls have collided, false if not 
		 */
		public boolean seeIfCollided(BouncingBall a, BouncingBall b) {
			
			// Use TLAP to calculate necessary variables
			double xDiff = b.getX() - a.getX();
			double yDiff = b.getY() - a.getY();
			double length = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
			
			// If the distance between them is less than or equal to the sum of their radii, then a collision has occurred
			if (length <= ((double)a.getDiameter())/2 + ((double) b.getDiameter())/2) {	
				
				if (VISUALIZATION_MODE) {
					a.setColor(Color.GREEN);
					b.setColor(Color.GREEN);
				}
				
				return true;
			
				// Otherwise there is no collision
			} else {
				return false;
			}
			
		}	
		
		/**
		 * drawBounds
		 * Method that draws the rectangles for the QuadTree bounds
		 * @param a Graphics object that draws to the screen and a QuadTree that will be displayed to the screen
		 */
		public void drawBounds(Graphics g, QuadTree newTree) {
			g.setColor(Color.LIGHT_GRAY);
			
			// These are required variables based on the boundary of the QuadTree/node
			int xCord = (int)(newTree.getBoundary().getX() - newTree.getBoundary().getWidth());
			int yCord = (int)(newTree.getBoundary().getY() - newTree.getBoundary().getHeight());
			int totalWidth = (int)(newTree.getBoundary().getWidth() * 2);
			int totalHeight = (int)(newTree.getBoundary().getHeight() * 2);
			
			// Draw the rectangle
			g.drawRect(xCord, yCord, totalWidth, totalHeight);

			// If the tree has been divided, then the method must be called again on all subsegments
			if (newTree.isDivided()) {
				drawBounds(g, newTree.northeast);
				drawBounds(g, newTree.northwest);
				drawBounds(g, newTree.southeast);
				drawBounds(g, newTree.southwest);
			}
		}

		/**
		 * drawBalls
		 * Method that draws all the balls
		 * @param a Graphics object that draws to the screen
		 */
		public void drawBalls(Graphics g) {
			g.setColor(Color.WHITE);
			
			/// Iterate through all balls
			for (int i = 0; i < balls.size(); i++) {
				
				// If color mode has been turned on, the color must be set to the ball's color
				if (COLOR_MODE || VISUALIZATION_MODE) {
					g.setColor(balls.get(i).getColor());
				}
				
				// Draw a circle with the ball's specification
				g.fillOval((int) balls.get(i).getX(), (int) balls.get(i).getY(), balls.get(i).getDiameter(), balls.get(i).getDiameter());
			}
		}

		/**
		 * updatePositions
		 * Method for updating the positions of the balls
		 */
		public void updatePositions() {

			// Iterate through all balls
			for (int i = 0; i < balls.size(); i++) {
				
				// Ball must bounce off top and bottom of screen
				if (balls.get(i).getX() <= balls.get(i).getDiameter()/2) {
					balls.get(i).setxChange(-1 * balls.get(i).getxChange());
					balls.get(i).setX(balls.get(i).getDiameter()/2);
				}
				if (balls.get(i).getX() + balls.get(i).getDiameter()/2 >= currentDimensionX) {
					balls.get(i).setxChange(-1 * balls.get(i).getxChange());
					balls.get(i).setX(currentDimensionX - balls.get(i).getDiameter()/2);
				}
				
				// Ball must bounce off left and right of screen
				if (balls.get(i).getY() <= balls.get(i).getDiameter()/2) {
					balls.get(i).setyChange(-1 * balls.get(i).getyChange());
					balls.get(i).setY(balls.get(i).getDiameter()/2);
				}
				if (balls.get(i).getY() + balls.get(i).getDiameter()/2 >= currentDimensionY) {
					balls.get(i).setyChange(-1 * balls.get(i).getyChange());
					balls.get(i).setY(currentDimensionY - balls.get(i).getDiameter()/2);
				}
				
				// The x and y coordinates of the ball must change dependent on their x and y velocities
				balls.get(i).setX(balls.get(i).getX() + balls.get(i).getxChange());
				balls.get(i).setY(balls.get(i).getY() + balls.get(i).getyChange());
			}
		}
		
		// I have no clue what this does but Eclipse said it was a good idea
		@SuppressWarnings("deprecation")
		
		/**
		 * enableResizing
		 * Method that allows the user to resize the screen
		 */
		public void enableResizing() {
			
			// Changes the dimensions
			currentDimensionX = (int) window.size().getWidth() - 1;
			currentDimensionY = (int) window.size().getHeight() - SCREEN_RESOLUTION_CONSTANT;
			
			// Changes the QuadTree's boundaries based on these new dimensions
			qTree.changeBoundary(new Rectangle(currentDimensionX / 2, currentDimensionY / 2, currentDimensionX / 2,
					currentDimensionY / 2));
		}
	} // End of GameAreaPanel

	//This is the inner class for the keyboard listener that detects key presses and runs the corresponding code
	private class MyKeyListener implements KeyListener {

		// These methods are mandatory and are not used
		public void keyTyped(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {}

		/**
		 * keyPressed
		 * Method that does an action upon a key being pressed
		 * @param a KeyEvent object of a key being pressed
		 */
		public void keyPressed(KeyEvent e) {

			// If the space key is pressed
			if (e.getKeyCode() == KeyEvent.VK_SPACE) { 

				// Balls are added that will also be displayed
				for (int i = 0; i < BALLS_TO_ADD_ON_KEYPRESS; i++) {
					balls.add(new BouncingBall(currentDimensionX, currentDimensionY));
				}
				
				// If the escape key is pressed
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { 
				
				// The window closes
				window.dispose();

			}
		}

	} // End of MyKeyListener

} // End of BounceTester 