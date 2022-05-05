//******************************************************************************
// Copyright (C) 2016-2019 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Wed Feb 27 17:33:04 2019 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.lightProject;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Point2D;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model	model;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	public void		keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			// Set mode to lightbox
			case KeyEvent.VK_NUMPAD1:
			case KeyEvent.VK_1:
				model.setStatus("Lightbox");
				break;
			
			// Set mode to mirror
			case KeyEvent.VK_NUMPAD2:
			case KeyEvent.VK_2:
				model.setStatus("Mirror");
				break;
			
			// Set mode to prism
			case KeyEvent.VK_NUMPAD3:
			case KeyEvent.VK_3:
				model.setStatus("Prism");
				break;
				
			// Set mode to convex
			case KeyEvent.VK_NUMPAD4:
			case KeyEvent.VK_4:
				model.setStatus("Convex");
				break;
				
			// Set mode to concave
			case KeyEvent.VK_NUMPAD5:
			case KeyEvent.VK_5:
				model.setStatus("Concave");
				break;
				
			// Toggle light
			case KeyEvent.VK_ENTER:
				model.toggleLight(false);
				break;

			// Cycle left through objects in the scene
			case KeyEvent.VK_COMMA:
				model.cycleElements(true);
				break;
				
			// Cycle right through objects in the scene
			case KeyEvent.VK_PERIOD:
				model.cycleElements(false);
				break;
				
			case KeyEvent.VK_LEFT:
				if(Utilities.isShiftDown(e)) {
					model.rotateElement(-1.0);					
				}
				else {
					model.rotateElement(-10.0);
				}
				break;
			case KeyEvent.VK_RIGHT:
				if(Utilities.isShiftDown(e)) {
					model.rotateElement(1.0);
				}
				else {
					model.rotateElement(10.0);
				}
				break;
			case KeyEvent.VK_C:
				model.colorCycle();
				break;
			case KeyEvent.VK_D:
				model.clearScene(true);
				break;
				
			case KeyEvent.VK_M:
				model.toogleControl();
				break;
			case KeyEvent.VK_CLEAR:
			case KeyEvent.VK_DELETE:
				model.clearScene(false);
				
			
				return;
				
		}
	}
}

//******************************************************************************
