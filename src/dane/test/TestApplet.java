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
package dane.test;

import dane.media3d.Graphics3D;
import dane.scene.Model;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JApplet;

/**
 *
 * @author Dane
 */
public abstract class TestApplet extends JApplet implements Runnable, KeyListener, FocusListener, MouseMotionListener, MouseListener {

	public Graphics graphics;
	private BufferedImage image;
	private int[] pixels;

	public int deltime = 20;
	public int fps;
	public double frameTime = 0.0;
	public boolean active;

	public int width, height;

	private final long[] optim = new long[10];

	private Map<Integer, Boolean> keysDown = new HashMap<>();
	private Map<Integer, Boolean> keysPressed = new HashMap<>();

	public int mouseButton;
	public int dragButton;
	public int mouseX, mouseY;
	public int clickX, clickY;

	public void initialize(int w, int h) {
		setSize(w, h);
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		setMaximumSize(getSize());

		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		graphics = image.getGraphics();
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		width = w;
		height = h;

		dane.media2d.Graphics2D.setTarget(pixels, width, height);
		Graphics3D.setOffsets();
		Graphics3D.setZBuffer(width, height);
		Graphics3D.createPalette(1.0);
		Model.allowInput = true;

		active = true;
		startThread(this, Thread.MIN_PRIORITY);
	}

	public boolean keyIsDown(int key) {
		return keysDown.containsKey(key) && keysDown.get(key);
	}

	public boolean wasKeyPressed(int key) {
		return keysPressed.containsKey(key) && keysPressed.get(key);
	}

	public void startThread(Runnable r, int priority) {
		Thread t = new Thread(r);
		t.setPriority(priority);
		t.start();
	}

	@Override
	public void addNotify() {
		super.addNotify(); //To change body of generated methods, choose Tools | Templates.
		requestFocus();
	}

	@Override
	public void run() {
		Graphics appletGraphics = getGraphics();

		while (appletGraphics == null) {
			appletGraphics = getGraphics();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addFocusListener(this);

		int currentFrame = 0;
		int accumulator = 0;
		int ratio = 256;
		int delay = 1;

		for (int n = 0; n < 10; n++) {
			this.optim[n] = System.currentTimeMillis();
		}

		long currentTime;

		while (this.active) {
			int lastRatio = ratio;
			int lastDelta = delay;

			ratio = 300;
			delay = 1;
			currentTime = System.currentTimeMillis();

			if (this.optim[currentFrame] == 0L) {
				ratio = lastRatio;
				delay = lastDelta;
			} else if (currentTime > this.optim[currentFrame]) {
				ratio = (int) ((long) (this.deltime * 2560) / (currentTime - this.optim[currentFrame]));
			}

			if (ratio < 25) {
				ratio = 25;
			}

			if (ratio > 256) {
				ratio = 256;
				delay = (int) ((long) this.deltime - (currentTime - this.optim[currentFrame]) / 10L);
			}

			this.optim[currentFrame] = currentTime;
			currentFrame = (currentFrame + 1) % 10;

			if (delay > 1) {
				for (int n = 0; n < 10; n++) {
					if (this.optim[n] != 0L) {
						this.optim[n] += (long) delay;
					}
				}
			}

			if (delay < 1) {
				delay = 1;
			}

			try {
				Thread.sleep((long) delay);
			} catch (InterruptedException e) {

			}

			long nano = System.nanoTime();

			for (; accumulator < 256; accumulator += ratio) {
				update();

				if (!keysPressed.isEmpty()) {
					keysPressed.clear();
				}
			}

			accumulator &= 0xFF;

			if (this.deltime > 0) {
				this.fps = (ratio * 1000) / (this.deltime * 256);
			}

			draw();
			appletGraphics.drawImage(image, 0, 0, null);

			if (this.frameTime == 0) {
				this.frameTime = (System.nanoTime() - nano) / 1_000_000.0;
			} else {
				this.frameTime += (System.nanoTime() - nano) / 1_000_000.0;
				this.frameTime /= 2;
			}
		}
	}

	public abstract void update();

	public abstract void draw();

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (!keyIsDown(key)) {
			keysPressed.put(key, true);
		}
		keysDown.put(key, true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysDown.put(e.getKeyCode(), false);
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
		keysDown.clear();
		dragButton = 0;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getXOnScreen();
		int y = e.getYOnScreen();

		mouseX = x;
		mouseY = y;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getXOnScreen();
		int y = e.getYOnScreen();

		mouseX = x;
		mouseY = y;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getXOnScreen();
		int y = e.getYOnScreen();

		clickX = x;
		clickY = y;

		if (e.isMetaDown()) {
			mouseButton = 2;
			dragButton = 2;
		} else {
			mouseButton = 1;
			dragButton = 1;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragButton = 0;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

}
