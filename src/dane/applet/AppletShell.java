/*
 * Copyright (C) 2015 Dane.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package dane.applet;

import dane.input.Keyboard;
import dane.input.Mouse;
import dane.timer.NanoTimer;
import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;

/**
 *
 * @author Dane
 */
public abstract class AppletShell extends JApplet implements Runnable {

	private static final Logger logger = Logger.getLogger(AppletShell.class.getName());

	private final Mouse mouse = new Mouse();
	private final Keyboard keyboard = new Keyboard();

	private BufferedImage image;
	private int cycleInterval;
	private int state;
	private int fps;
	private double frameTime;

	public void initialize(int width, int height) {
		this.setSize(width, height);
		this.setPreferredSize(this.getSize());
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
		this.enableInputMethods(true);
		this.startThread(this, Thread.MIN_PRIORITY);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		this.requestFocus();
	}

	/**
	 * Starts a new thread for the provided runnable with the given priority.
	 *
	 * @param runnable the runnable.
	 * @param priority the thread priority.
	 */
	public void startThread(Runnable runnable, int priority) {
		Thread t = new Thread(runnable);
		t.setPriority(priority);
		t.start();
	}

	/**
	 * Causes the thread to continuously attempt to get the graphics context of this applet before continuing.
	 * (<b>Warning:</b> loops infinitely until g != null)
	 *
	 * @return the graphics.
	 */
	private final Graphics grabGraphics() {
		Graphics g = this.getGraphics();

		while (g == null) {
			g = this.getGraphics();

			// gives the thread back to swing/awt
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				// ignored
			}
		}

		return g;
	}

	/**
	 * Returns the amount of time in milliseconds each cycle is expected to finish within.
	 *
	 * @return the interval.
	 */
	public int getCycleInterval() {
		return this.cycleInterval;
	}

	/**
	 * Returns the last indicated framerate. (The amount of times the applet was updated and drawn in the last second.)
	 *
	 * @return the framerate.
	 */
	public int getFPS() {
		return this.fps;
	}

	/**
	 * Returns the last indicated frametime. (The amount of time in milliseconds it took to update and draw the game in
	 * the last frame.)
	 *
	 * @return
	 */
	public double getFrameTime() {
		return this.frameTime;
	}

	public Mouse getMouse() {
		return this.mouse;
	}

	public Keyboard getKeyboard() {
		return this.keyboard;
	}

	@Override
	public void start() {
		if (this.state > 0) {
			this.state = 0;
		}
	}

	/**
	 * Causes a forceful shutdown after 4 seconds worth of cycles.
	 */
	@Override
	public void stop() {
		if (this.state >= 0) {
			this.state = 4000 / this.cycleInterval;
		}
	}

	@Override
	public void run() {
		Graphics appletGraphics = this.grabGraphics();
		Graphics imageGraphics = this.image.getGraphics();

		NanoTimer timer = new NanoTimer();

		final int width = this.getWidth();
		final int height = this.getHeight();

		int frame = 0;
		long fpsUpdateTime = System.currentTimeMillis() + 1000;

		while (this.state >= 0) {
			if (this.state > 0) {
				this.state--;

				if (this.state == 0) {
					break;
				}
			}

			int cycles = timer.getCycleCount(20, 1);

			for (int i = 0; i < cycles; i++) {
				this.update();

				// reset frame based variables
				this.mouse.reset();
				this.keyboard.reset();
			}

			long time = System.nanoTime();

			this.draw(imageGraphics, width, height);
			appletGraphics.drawImage(this.image, 0, 0, width, height, null);

			if (this.frameTime == 0) {
				this.frameTime = (System.nanoTime() - time) / 1_000_000.0;
			} else {
				this.frameTime += (System.nanoTime() - time) / 1_000_000.0;
				this.frameTime /= 2;
			}

			frame++;

			if (time >= fpsUpdateTime) {
				this.fps = frame;
				frame = 0;
				fpsUpdateTime = time + 1_000_000_000;
			}

		}

		imageGraphics.dispose();
		appletGraphics.dispose();

		this.forceShutdown();
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		this.mouse.consumeEvent(e);
	}

	@Override
	protected void processMouseWheelEvent(MouseWheelEvent e) {
		super.processMouseWheelEvent(e);
		this.mouse.consumeEvent(e);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);
		this.mouse.consumeEvent(e);
	}

	@Override
	protected void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);
		this.keyboard.consumeEvent(e);
	}

	/**
	 * Causes a grace period of 5 seconds to initiate for the applet to close, before it is delicately killed with
	 * kindness and love.
	 */
	@Override
	public void destroy() {
		this.state = -1;

		try {
			Thread.sleep(5000L);
		} catch (Exception e) {
		}

		if (this.state == -1) {
			logger.log(Level.WARNING, "5 seconds expired, forcing kill.");
			forceShutdown();
		}
	}

	public void forceShutdown() {
		logger.log(Level.INFO, "Closing program");

		this.state = -2;
		this.shutdown();

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			//ignore
		}

		try {
			System.exit(0);
		} catch (Throwable t) {
			// ignore
		}
	}

	public abstract void update();

	public abstract void draw(Graphics g, int width, int height);

	public abstract void shutdown();

}
