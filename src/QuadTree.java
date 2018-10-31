/**
 * QuadTree.java
 * Version 1.0
 * @author Yash Arora
 * October 30th, 2018
 * The following code is the information for a QuadTree/node that stores balls within it
 */

// Import rectangle for boundaries and ArrayList for storage
import java.awt.Rectangle;   
import java.util.ArrayList;

// Class that holds divisions of 4 to increase efficiency in the collision detection
public class QuadTree {
	
	// Boundary is a rectangle with x, y as center and w, h as half of the width and height
	private Rectangle boundary;
	
	// Capacity that the node must reach before it divides
	private int capacityBeforeSplitting;
	
	// ArrayList of all the balls in the given tree
	private ArrayList<BouncingBall> ballsInTree;
	
	// The level of divisions that the tree has gone through at this point
	private int level;
	
	// The tree begins undivided
	private boolean divided = false;
	
	// Whether or not balls can be added to the tree
	private boolean ableToTakePoints = true;

	// Each of the subsegments of the node
	QuadTree northeast;
	QuadTree northwest;
	QuadTree southeast;
	QuadTree southwest;
		
	/**
	 * QuadTree
	 * Constructor that creates the QuadTree/node
	 * @param Rectangle of the QuadTree's jurisdiction, the integer capacity before the tree splits, and the integer level of divisions it is at
	 */
	QuadTree(Rectangle boundary, int capacityBeforeSplitting, int level) {
		
		// These are all required variables for the QuadTree/node
		this.boundary = boundary;
		this.capacityBeforeSplitting = capacityBeforeSplitting;
		this.ballsInTree = new ArrayList<BouncingBall>();
		this.divided = false;
		this.ableToTakePoints = true;
		this.level = level;
	}
	
	/**
	 * clear
	 * Method that clears the QuadTree
	 */
	public void clear() {
		
		// Clears ArrayList of balls in the tree
		ballsInTree.clear();
		
		// Eliminates all the subsegments
		northeast = null;
		northwest = null;
		southeast = null;
		southwest = null;
		
		// Set the divided variable to false
		divided = false;
	}

	/**
	 * insert
	 * Method that recursively inserts a bouncing ball into the QuadTree
	 * @param a bouncing ball object to insert into the tree
	 * @return boolean true if the ball was inserted, false otherwise
	 */
	public boolean insert(BouncingBall bouncingBall) {
		
		// If the ball is not in the boundary, it cannot be inserted
		if(!bouncingBall.isInside(boundary)) {
			return false;
		}
		
		// If the current number of divisions exceeds the maximum, the ball should be added to the tree
		if (level > BounceTester.MAX_DIVISIONS) {
			ballsInTree.add(bouncingBall);
			return true;
		}
		
		// If the number of balls have exceeded the capacity threshold and it can take points, it adds to the tree
		if(ballsInTree.size() < capacityBeforeSplitting && ableToTakePoints) {
			ballsInTree.add(bouncingBall);
			return true;

			// Otherwise a subdivision must occur or ball must be placed in subdivision
		} else {			
			
			// If the QuadTree has not been divided, it should divide
			if (!isDivided()) {
				subdivide();
			}
			
			// Ball is placed into the correct subdivision of a tree
			if (northeast.insert(bouncingBall)) {
				return true;
			} else if (northwest.insert(bouncingBall)){
				return true;
			} else if (southeast.insert(bouncingBall)) {
				return true;
			} else if (southwest.insert(bouncingBall)) {
				return true;
			}
		}
		
		// If nothing happened the ball was not inserted
		return false;
	}

	/**
	 * subdivide
	 * Method that divides a QuadTree/node into subsegments
	 */
	private void subdivide() {
		
		// These are the variables for the boundaries of the QuadTree/node
		int x = (int) boundary.getX();
		int y = (int) boundary.getY();
		int h = (int) boundary.getHeight();
		int w = (int) boundary.getWidth();
		
		// Creates new rectangle boundaries for each of the subdivisions
		Rectangle ne = new Rectangle(x + w/2, y - h/2, w/2, h/2);
		Rectangle nw = new Rectangle(x - w/2, y - h/2, w/2, h/2);
		Rectangle se = new Rectangle(x + w/2, y + h/2, w/2, h/2);
		Rectangle sw = new Rectangle(x - w/2, y + h/2, w/2, h/2);
		
		// The nodes are created with new boundaries and another level
		northeast = new QuadTree(ne, capacityBeforeSplitting, level+1);
		northwest = new QuadTree(nw, capacityBeforeSplitting, level+1);
		southeast = new QuadTree(se, capacityBeforeSplitting, level+1);
		southwest = new QuadTree(sw, capacityBeforeSplitting, level+1);
		
		// The tree has been divided
		divided = true;
		
		// Once a tree has been divided it should no longer be able to take balls into the bigger segment
		ableToTakePoints = false;
		
		// Once a subdivision occurs the existing balls must be moved into their respective areas
		for (int i = 0; i < ballsInTree.size(); i++) {
			insert(ballsInTree.get(i));
		}
		
		// There are no longer any more balls in the bigger segment
		ballsInTree.clear();
	}
	/**
	 * changeBoundary
	 * Method that changes the boundaries of the QuadTree/node if the user resizes
	 * @param a Rectangle object of the new bounds of the tree
	 */
	public void changeBoundary(Rectangle newBoundary) {
		this.boundary = newBoundary;
	}
	
	/**
	 * getBoundary
	 * Method that returns the QuadTree/node boundary rectangle
	 * @return a Rectangle object of the tree's boundary
	 */
	public Rectangle getBoundary() {
		return boundary;
	}
	
	/**
	 * getBallsInTree
	 * Method that returns an ArrayList of all the balls in a tree
	 * @return an ArrayList of all the balls in the tree
	 */
	public ArrayList<BouncingBall> getBallsInTree() {
		return ballsInTree;
	}

	/**
	 * isDivided
	 * Method that returns if the tree has been divided
	 * @return boolean true if the tree has been divided, false otherwise
	 */
	public boolean isDivided() {
		return divided;
	}
}