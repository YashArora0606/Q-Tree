/**
 * BouncingBall.java
 * Version 1.0
 * @author Yash Arora
 * October 30th, 2018
 * The following code is the information of one bouncing ball
 */

// Required imports
import java.awt.Color; 
import java.awt.Rectangle;
import java.util.Random;

// This is the class that holds all the information for a single bouncing ball
public class BouncingBall {

	// These are all required variables specific to a ball
	private double velocity;
	private double angle;
	private double xChange;
	private double yChange;
	private int diameter;
	private double x;
	private double y;
	private Color color;

	/**
	 * BouncingBall
	 * Constructor that creates a bouncing ball
	 * @param two numbers that represent the maximum spawn x and y of a ball so that it is within the screen
	 */
	BouncingBall(int maxSpawnX, int maxSpawnY) {

		// Random is used to create a variety of ball variables
		Random rand = new Random();
		
		// Diameter is between 5 and 20
		this.diameter = rand.nextInt(10) + 5;
		
		// Ball is spawned based such that it doesn't go out of the screen
		this.x = (int) (rand.nextInt((maxSpawnX - diameter) + 1)) + diameter;
		this.y = (int) (rand.nextInt((maxSpawnY - diameter) + 1)) + diameter;

		// Ball angle is randomized and velocities are determined using TLAP
		this.angle = Math.random() * 2 * Math.PI;
		this.velocity = rand.nextDouble() * BounceTester.VELOCITY_MAXIMUM;
		this.xChange = (velocity * Math.cos(angle)) + 1;
		this.yChange = (velocity * Math.sin(angle)) + 1;

		// Randomly chooses the direction of x and y velocities
		if (Math.random() > 0.5) {
			xChange = xChange * -1;
		}
		if (Math.random() > 0.5) {
			yChange = yChange * -1;
		}

		// Makes sure that none of the velocities are 0
		if ((int) xChange == 0) {
			xChange++;
		}
		if ((int) yChange == 0) {
			yChange++;
		}

		// Randomly sets a color for a ball
		double random = Math.random();
		double red = 0 + Math.floor((255 - 0) * random);
		double green = 0 + Math.floor((182 - 0) * random);
		double blue = 0 + Math.floor((193 - 0) * random);
		this.color = new Color((int) red, (int) green, (int) blue);
	}
	
	/**
	 * getColor
	 * Method that returns the ball's color
	 * @return color of the ball
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * setColor
	 * Method that sets the ball's color
	 * @param color that the ball will be set to
	 */
	public void setColor(Color c) {
		this.color = c;
	}

	/**
	 * getxChange
	 * Method that returns the ball's x velocity
	 * @return the x velocity of the ball
	 */
	public double getxChange() {
		return xChange;
	}
	
	/**
	 * setxChange
	 * Method that sets the ball's x velocity
	 * @param double that represents the new x velocity
	 */
	public void setxChange(double d) {
		this.xChange = d;
	}

	/**
	 * getyChange
	 * Method that returns the ball's y velocity
	 * @return the y velocity of the ball
	 */
	public double getyChange() {
		return yChange;
	}

	/**
	 * setyChange
	 * Method that sets the ball's y velocity
	 * @param double that represents the new y velocity
	 */
	public void setyChange(double d) {
		this.yChange = d;
	}

	/**
	 * getX
	 * Method that returns the ball's x position
	 * @return a double that is the ball's x position
	 */
	public double getX() {
		return x;
	}

	/**
	 * setX
	 * Method that sets the ball's x position
	 * @param a double that represents the desired x position
	 */
	public void setX(double d) {
		this.x = d;
	}

	/**
	 * getY
	 * Method that returns the ball's y position
	 * @return a double that is the ball's y position
	 */
	public double getY() {
		return y;
	}

	/**
	 * setY
	 * Method that sets the ball's y position
	 * @param a double that represents the desired y position
	 */
	public void setY(double d) {
		this.y = d;
	}

	/**
	 * getDiameter
	 * Method that returns the ball's diameter
	 * @return the ball's diameter
	 */
	public int getDiameter() {
		return diameter;
	}

	/**
	 * isInside
	 * Method that checks if a ball is within a given boundary
	 * @param a Rectangle boundary that the ball may be within
	 * @return boolean true if the ball is within the bounds given, false otherwise
	 */
	public boolean isInside(Rectangle boundary) {
		
		// Use TLAP to see if it is or it is not
		if (x + diameter / 2 >= boundary.getX() - boundary.getWidth() && x + diameter / 2 <= boundary.getX() + boundary.getWidth()
				&& y + diameter / 2 >= boundary.getY() - boundary.getHeight() && y + diameter / 2 <= boundary.getY() + boundary.getHeight()) {
			return true;
		} else {
			return false;
		}
	}
}
