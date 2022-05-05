//******************************************************************************
// Copyright (C) 2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:32:08 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190227 [weaver]:	Original file.
//
//******************************************************************************
//
// The model manages all of the user-adjustable variables utilized in the scene.
// (You can store non-user-adjustable scene data here too, if you want.)
//
// For each variable that you want to make interactive:
//
//   1. Add a member of the right type
//   2. Initialize it to a reasonable default value in the constructor.
//   3. Add a method to access a copy of the variable's current value.
//   4. Add a method to modify the variable.
//
// Concurrency management is important because the JOGL and the Java AWT run on
// different threads. The modify methods use the GLAutoDrawable.invoke() method
// so that all changes to variables take place on the JOGL thread. Because this
// happens at the END of GLEventListener.display(), all changes will be visible
// to the View.update() and render() methods in the next animation cycle.
//
//******************************************************************************

package edu.ou.cs.cg.assignment.lightProject;

import java.lang.*;
import java.awt.Point;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.*;
import com.jogamp.opengl.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View					view;

	// Model variables
	private Point2D.Double				cursor;	// Current cursor coords

	// Light project model variables
	private Point2D.Double[] lightPoints;
	private String status;
	private Deque<LightElement> lightElements;
	private boolean light;
	private boolean control ; 
	private static final Color[]	COLORS = new Color[]
	{
			new Color(0xc7c7c7),
			new Color(0x804080),
			new Color(0x400080),
			new Color(0x0000ff),
			new Color(0x008000),
			new Color(0xffff00),
			new Color(0xff8000),
			new Color(0x800000),
	};

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		cursor = null;
		
		// Initialize light project variables
		status = "Lightbox";
		lightElements = new ArrayDeque<LightElement>();
		light = false;
		control  = true ; 
		lightPoints = new Point2D.Double[5];
		for(int i = 0; i < lightPoints.length; i++)
		{
			lightPoints[i] = new Point2D.Double(0.0, 0.0);
		}
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Point2D.Double	getCursor()
	{
		if (cursor == null)
			return null;
		else
			return new Point2D.Double(cursor.x, cursor.y);
	}
	
	// Light project getter methods
	
	// Get the light points
	public Point2D.Double[] getLightPoints()
	{
		return lightPoints;
	}
	
	// Get the type of object being placed
	public String getStatus()
	{
		return status;
	}
	
	// Get all objects in the scene
	public Deque<LightElement> getLightElements()
	{
		return lightElements;
	}
	
	// Get the point the light beam follows
	public boolean getLight()
	{
		return light;
	}
	
	public boolean getControl()
	{
		return control;
	}
	public void toogleControl()
	{
		control = !control ;

	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void	setCursorInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				cursor = new Point2D.Double(p[0], p[1]);
			}
		});;
	}

	public void	turnCursorOff()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				cursor = null;
			}
		});;
	}

	// Light project setter methods
	
	// Add a light element into the scene
	public void	addLightElementInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				switch(status)
				{
					case "Lightbox":
						setLightBox(q);
						break;
					case "Mirror":
						addMirror(q);
						break;
					case "Prism":
						addPrism(q);
						break;
					case "Convex":
						addLense(q, true);
						break;
					case "Concave":
						addLense(q, false);
						break;
					default:
						break;
				}
			}
		});;
	}
	
	// Clear the scene of all objects
	public void clearScene(boolean fullClear)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void update(GL2 gl) {
				if(fullClear) {
					lightElements.clear();
				}
				else {
					lightElements.pollLast();
				}
				toggleLight(true);
			}
		});;
	}
	
	// Sets the type of object being placed
	public void setStatus(String s)
	{
		status = new String(s);
	}
	
	// Creates lightbox or changes its location, only one lightbox should exist in the scene
	public void setLightBox(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				boolean noLightbox = true;
				// Set the location of the lightbox if one already exists
				for (LightElement le : lightElements)
				{
					if(le.getType().equals("Lightbox")) {
						noLightbox = false;
						le.setLightbox(new Point2D.Double(p[0] - 25, p[1] - 25),
									new Point2D.Double(p[0] + 25, p[1] - 25),
									new Point2D.Double(p[0] + 25, p[1] + 25),
									new Point2D.Double(p[0] - 25, p[1] + 25),
									new Point2D.Double(p[0], p[1]));
						break;
					}
				}
				
				// Add a lightbox to the scene if one does not exist
				if(noLightbox)
				{
					lightElements.add(new LightElement("Lightbox", 
							new Point2D.Double(p[0] - 25, p[1] - 25), 
							new Point2D.Double(p[0] + 25, p[1] - 25), 
							new Point2D.Double(p[0] + 25, p[1] + 25), 
							new Point2D.Double(p[0] - 25, p[1] + 25), 
							null, new Point2D.Double(p[0], p[1]), null, null));
				}
				toggleLight(true);
			}
		});;
	}
	
	// Creates a mirror
	public void addMirror(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void update(double[] p) {
				lightElements.add(new LightElement("Mirror", 
						new Point2D.Double(p[0] - 5, p[1] - 30), 
						new Point2D.Double(p[0] + 5, p[1] - 30), 
						new Point2D.Double(p[0] + 5, p[1] + 30),
						new Point2D.Double(p[0] - 5, p[1] + 30), 
						null, new Point2D.Double(p[0], p[1]), null, null));
				toggleLight(true);
			}
		});;
	}
	
	// Creates a prism
	public void addPrism(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void update(double[] p) {
				lightElements.add(new LightElement("Prism", 
						new Point2D.Double(p[0] - 25, p[1] - 25), 
						new Point2D.Double(p[0] + 25, p[1] - 25), 
						null, null, new Point2D.Double(p[0], p[1] + 25), 
						new Point2D.Double(p[0], p[1]), null, null));
				toggleLight(true);
			}
		});;
	}
	
	// Creates a lense (either convex or concave depending on passed boolean)
	public void addLense(Point q, boolean convex)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void update(double[] p) {
				// Check if the lens is convex or concave
				if(convex) {
					lightElements.add(new LightElement("Convex", 
							new Point2D.Double(p[0] - 5, p[1] - 30), 
							new Point2D.Double(p[0] + 5, p[1] - 30), 
							new Point2D.Double(p[0] + 5, p[1] + 30),
							new Point2D.Double(p[0] - 5, p[1] + 30), 
							null, new Point2D.Double(p[0], p[1]), 
							new Point2D.Double(p[0] - 15, p[1]), 
							new Point2D.Double(p[0] + 15, p[1])));
				}
				else {
					lightElements.add(new LightElement("Concave", 
							new Point2D.Double(p[0] - 10, p[1] - 30),
							new Point2D.Double(p[0] + 10, p[1] - 30),
							new Point2D.Double(p[0] + 10, p[1] + 30),
							new Point2D.Double(p[0] - 10, p[1] + 30),
							null, new Point2D.Double(p[0], p[1]), null, null));
				}
				toggleLight(true);
			}
		});;
	}
	
	// Toggles drawing the light (if object is placed/deleted light is turned off, otherwise toggle)
	public void toggleLight(boolean override)
	{
		// Checks if object is placed/deleted
		if(override) {
			light = false;
		}
		else {
			light = !light;
		}
		
		boolean noLightBox = true;
		// Sets the lightpoint back to the lightbox and clear the trace
		for(LightElement le : lightElements)
		{
			if(le.getType().equals("Lightbox"))
			{
				double rotation = le.getRotation();
				
				for(int i = 0; i < lightPoints.length; i++)
				{
					double x = Math.cos(Math.toRadians(rotation))*25.0 -
								Math.sin(Math.toRadians(rotation))*(25 - 10 * i - 5) + le.getCenter().x;
					double y = Math.sin(Math.toRadians(rotation))*25.0 +
								Math.cos(Math.toRadians(rotation))*(25 - 10 * i - 5) + le.getCenter().y;
					lightPoints[i].setLocation(x, y);
				}
				noLightBox = false;
				break;
			}
		}
		
		if(noLightBox)
		{
			for(Point2D.Double lp : lightPoints)
			{
				lp.setLocation(0.0, 0.0);
			}
		}
		
		view.clearLight();
	}
	
	// Cycles through the current objects in the scene (cycle direction based on passed boolean)
	public void cycleElements(boolean left)
	{
		// Checks the case when there are no elements in the scene
		if(lightElements.size() == 0)
		{
			return;
		}
		
		if(left) {
			lightElements.offerFirst(lightElements.pollLast());
		}
		else {
			lightElements.offerLast(lightElements.pollFirst());
		}
	}
	
	// Rotate the current selected object
	public void rotateElement(double rotation)
	{
		lightElements.peekLast().setRotation(rotation);
		toggleLight(true);
	}
	
	public void colorCycle()
	{
		switch(lightElements.peekLast().getType())
		{
			case "Convex":
			case "Concave":
			case "Mirror":
				lightElements.peekLast().cycleColor();
				break;
		}
	}

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;

		public ViewPointUpdater(Point q)
		{
			this.q = q;
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);

			update(p);
		}

		public abstract void	update(double[] p);
	}
	
	//**********************************************************************
	// Object Classes
	//**********************************************************************
	
	// Object class that encapsulates all the object types is for the light project
	public class LightElement {
		Point2D.Double bl;
		Point2D.Double br;
		Point2D.Double tr;
		Point2D.Double tl;
		Point2D.Double t;
		Point2D.Double center;
		Point2D.Double[] rCurve;
		Point2D.Double[] lCurve;
		Point2D.Double[] defaultRCurve;
		Point2D.Double[] defaultLCurve;
		double rotation;
		String type;
		Color color;
		int colorIndex;
		
		public LightElement(String type, Point2D.Double bl, Point2D.Double br, Point2D.Double tr,
						Point2D.Double tl, Point2D.Double t, Point2D.Double center, 
						Point2D.Double leftCtrl, Point2D.Double rightCtrl)
		{
			switch(type)
			{
				case "Lightbox":
					this.bl = bl;
					this.br = br;
					this.tr = tr;
					this.tl = tl;
					this.center = center;
					rotation = 0.0;
					this.type = new String(type);
					color = new Color(0x5dc9f4);
					break;
				case "Mirror":
					this.bl = bl;
					this.br = br;
					this.tr = tr;
					this.tl = tl;
					this.center = center;
					rotation = 0.0;
					this.type = new String(type);
					color = COLORS[0];
					colorIndex = 0;
					break;
				case "Prism":
					this.bl = bl;
					this.br = br;
					this.t = t;
					this.center = center;
					rotation = 0.0;
					this.type = new String(type);
					color = COLORS[0];
					colorIndex = 0;
					break;
				case "Convex":
					this.bl = bl;
					this.br = br;
					this.tr = tr;
					this.tl = tl;
					this.center = center;
					rotation = 0.0;
					this.type = new String(type);
					color = COLORS[0];
					colorIndex = 0;
					
					createConvex(leftCtrl, rightCtrl);
					break;
				case "Concave":
					this.bl = bl;
					this.br = br;
					this.tr = tr;
					this.tl = tl;
					this.center = center;
					rotation = 0.0;
					this.type = new String(type);
					color = COLORS[0];
					colorIndex = 0;
					
					createConcave();
					break;
			}
		}
		
		// Get the type of the element
		public String getType() {
			return type;
		}
		
		// Get bottom left point
		public Point2D.Double getBl() {
			return bl;
		}
		
		// Get bottom right point
		public Point2D.Double getBr() {
			return br;
		}
		
		// Get top right point
		public Point2D.Double getTr() {
			return tr;
		}
		
		// Get top left point
		public Point2D.Double getTl() {
			return tl;
		}
		
		// Get top point (for the prism)
		public Point2D.Double getT() {
			return t;
		}
		
		// Get the center point
		public Point2D.Double getCenter() {
			return center;
		}
		
		// Get right curve
		public Point2D.Double[] getRCurve() {
			return rCurve;
		}
		
		// Get left curve
		public Point2D.Double[] getLCurve() {
			return lCurve;
		}
		
		// Get default right curve
		public Point2D.Double[] getDefaultRCurve() {
			return defaultRCurve;
		}
		
		// Get default left curve
		public Point2D.Double[] getDefaultLCurve() {
			return defaultLCurve;
		}

		// Get rotation of the element
		public double getRotation() {
			return rotation;
		}
		
		// Set the rotation of the element
		public void setRotation(double rotation) {
			this.rotation += rotation;
			
			this.rotation = this.rotation % 360 ;
		}
		
		public Color getColor() {
			return color;
		}
		
		public void cycleColor() {
			colorIndex += 1;
			if(colorIndex > 7)
				colorIndex = 0;
			color = COLORS[colorIndex];
		}
		
		// Sets the location of the lightbox element
		public void setLightbox(Point2D.Double bl, Point2D.Double br, Point2D.Double tr, 
				Point2D.Double tl, Point2D.Double center) 
		{
			this.bl = bl;
			this.br = br;
			this.tr = tr;
			this.tl = tl;
			this.center = center;
			rotation = 0.0;
		}
		
		// Creates the curves for the convex lens
		private void createConvex(Point2D.Double leftCtrl, Point2D.Double rightCtrl) {
			int i;
			double t;
			
			rCurve = new Point2D.Double[11];
			lCurve = new Point2D.Double[11];
			defaultRCurve = new Point2D.Double[11];
			defaultLCurve = new Point2D.Double[11];
			
			for(i = 0, t = 0; i < 11 && t < 1.1; i++, t = t + 0.1) {
				rCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*br.x + 2*t*(1-t)*rightCtrl.x + Math.pow(t, 2)*tr.x),
						(Math.pow((1-t), 2)*br.y + 2*t*(1-t)*rightCtrl.y + Math.pow(t, 2)*tr.y));
				lCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*tl.x + 2*t*(1-t)*leftCtrl.x + Math.pow(t, 2)*bl.x),
						(Math.pow((1-t), 2)*tl.y + 2*t*(1-t)*leftCtrl.y + Math.pow(t, 2)*bl.y));
				defaultRCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*5.0 + 2*t*(1-t)*15.0 + Math.pow(t, 2)*5.0),
						(Math.pow((1-t), 2)*(-30.0) + 2*t*(1-t)*0.0 + Math.pow(t, 2)*30.0));
				defaultLCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*(-5.0) + 2*t*(1-t)*(-15.0) + Math.pow(t, 2)*(-5.0)),
						(Math.pow((1-t), 2)*30.0 + 2*t*(1-t)*0.0 + Math.pow(t, 2)*(-30.0)));
			}
		}
		
		// Creates the curves for the concave lens
		private void createConcave() {
			int i;
			double t;
			
			rCurve = new Point2D.Double[11];
			lCurve = new Point2D.Double[11];
			defaultRCurve = new Point2D.Double[11];
			defaultLCurve = new Point2D.Double[11];
			
			for(i = 0, t = 0; i < 11 && t < 1.1; i++, t = t + 0.1) {
				rCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*br.x + 2*t*(1-t)*center.x + Math.pow(t, 2)*tr.x),
						(Math.pow((1-t), 2)*br.y + 2*t*(1-t)*center.y + Math.pow(t, 2)*tr.y));
				lCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*tl.x + 2*t*(1-t)*center.x + Math.pow(t, 2)*bl.x),
						(Math.pow((1-t), 2)*tl.y + 2*t*(1-t)*center.y + Math.pow(t, 2)*bl.y));
				defaultRCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*10.0 + 2*t*(1-t)*0.0 + Math.pow(t,2)*10.0),
						(Math.pow((1-t), 2)*(-30.0) + 2*t*(1-t)*0.0 + Math.pow(t, 2)*30.0));
				defaultLCurve[i] = new Point2D.Double((Math.pow((1-t), 2)*(-10.0) + 2*t*(1-t)*0.0 + Math.pow(t, 2)*(-10.0)),
						(Math.pow((1-t), 2)*30.0 + 2*t*(1-t)*0.0 + Math.pow(t, 2)*(-30.0)));
			}
		}
	}
}

//******************************************************************************
