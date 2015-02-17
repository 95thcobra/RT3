package dane;

import dane.Graphics2D;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

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
/**
 *
 * @author Dane
 */
public class ImageTest extends JApplet implements Runnable, MouseMotionListener {

	private static final Logger logger = Logger.getLogger(ImageTest.class.getName());

	static {
		new Thread(() -> {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}).start();
	}

	public static void main(String[] args) throws Throwable {
		JFrame f = new JFrame();
		ImageTest i = new ImageTest(800, 600);
		f.add(i);
		f.setResizable(false);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	int width, height;

	Graphics graphics;
	BufferedImage image;
	int[] pixels;

	int deltime = 20;
	int fps = 50;
	double ft = 0.0;

	Model model;
	int rotation = 0;

	int cameraPitch = 128;
	int cameraYaw;
	int cameraX;
	int cameraY = 0;
	int cameraZ = 512;

	boolean running;

	private final long[] optim = new long[10];

	public ImageTest(int width, int height) {
		setSize(width, height);
		setPreferredSize(getSize());
		setMinimumSize(getSize());
		setMaximumSize(getSize());

		addMouseMotionListener(this);

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = image.getGraphics();
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		this.width = width;
		this.height = height;

		Graphics2D.setTarget(pixels, width, height);
		Graphics3D.setOffsets();
		Graphics3D.setZBuffer(width, height);
		Graphics3D.createPalette(1.0);
		Graphics3D.texturedShading = false;
		Model.allowInput = true;

		System.out.println("Creating grid");
		model = new Cube(256);//new Grid(4096, 64, 64);
		System.out.println("Grid created");

		// we need bounds before we center
		model.calculateBoundaries();
		model.translate(-model.maxBoundX / 2, -model.maxBoundY / 2, -model.maxBoundZ / 2);

		for (int i = 0; i < model.vertexCount; i++) {
			model.vertexY[i] += (int) (Math.random() * 64);
		}

		model.triangleColor = new int[model.triangleCount];

		for (int i = 0; i < model.triangleCount; i++) {
			model.triangleColor[i] = (17 << 10) | (3 << 7) | 48;
		}

		model.colorA = new int[model.triangleCount];
		model.colorB = new int[model.triangleCount];
		model.colorC = new int[model.triangleCount];

		model.triangleType = new int[model.triangleCount];
		Arrays.fill(model.triangleType, 1);
		model.calculateNormals();
		model.applyLighting(64, 768, -50, -50, -30, true);

		initializeQueue(30, 30, 30, 25);

		System.gc();
		running = true;
		new Thread(this).start();
	}

	@Override
	public void addNotify() {
		super.addNotify(); //To change body of generated methods, choose Tools | Templates.
	}

	public int[] queueX = new int[3000];
	public int[] queueY = new int[3000];
	public int queueSize = 0;
	public int queueTick = 0;

	public final void initializeQueue(int fromX, int fromY, int fromZ, int radius) {
		queueSize = 0;

		int minX = fromX - radius;
		int minY = fromY - radius;
		int minZ = fromZ;

		int maxX = fromX + radius;
		int maxY = fromY + radius;
		int maxZ = fromZ + 1;

		if (minX < 0) {
			minX = 0;
		}

		if (minY < 0) {
			minY = 0;
		}

		if (minZ < 0) {
			minZ = 0;
		}

		long startTime = System.nanoTime();

		for (int z = -radius; z <= 0; z++) {
			int z0 = fromZ + z;
			int z1 = fromZ - z;

			if (z0 < minZ && z1 >= maxZ) {
				continue;
			}

			for (int x = -radius; x <= 0; x++) {
				int x0 = fromX + x;
				int x1 = fromX - x;

				if (x0 < minX && x1 >= maxX) {
					continue;
				}

				for (int y = -radius; y <= 0; y++) {
					int y0 = fromY + y;
					int y1 = fromY - y;

					if (x0 >= minX) {
						if (y0 >= minY) {
							queueX[queueSize] = x0;
							queueY[queueSize++] = y0;
						}

						if (y1 < maxY) {
							queueX[queueSize] = x0;
							queueY[queueSize++] = y1;
						}
					}

					if (x1 < maxX) {
						if (y0 >= minY) {
							queueX[queueSize] = x1;
							queueY[queueSize++] = y0;
						}
						if (y1 < maxY) {
							queueX[queueSize] = x1;
							queueY[queueSize++] = y1;
						}
					}
				}
			}
		}

		startTime = System.nanoTime() - startTime;

		System.out.println("Queue size: " + queueSize);
		System.out.println("Took " + (startTime / 1_000_000.0) + "ms to create queue");
	}

	public void draw() {
		Graphics3D.clear(0x303030);
		Graphics3D.clearZBuffer();

		int cameraPitchSine = Model.sin[cameraPitch];
		int cameraPitchCosine = Model.cos[cameraPitch];
		int cameraYawSine = Model.sin[cameraYaw];
		int cameraYawCosine = Model.cos[cameraYaw];

		final int DRAW_MODEL = (1 << 0);
		final int DRAW_ORIGIN_DOT = (1 << 1);
		final int DRAW_DEBUG = (1 << 2);
		int flags = DRAW_MODEL | DRAW_DEBUG;

		//cameraYaw = (256 * Model.sin[rotation]) >> 16;
		//cameraYaw &= 0x7FF;
		if ((flags & DRAW_MODEL) != 0) {
			model.draw(0, rotation, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ, 1);
			rotation += 4;
			rotation &= 0x7FF;
		}

		// I just wanted a reason to show vector rotation so anybody can see how to translate a 3d point to a point on the screen.
		if ((flags & DRAW_ORIGIN_DOT) != 0) {
			int x = 0 - cameraX;
			int y = 0 - cameraY;
			int z = 0 - cameraZ;

			int w = z * cameraYawSine + x * cameraYawCosine >> 16;
			z = z * cameraYawCosine - x * cameraYawSine >> 16;
			x = w;

			w = y * cameraPitchCosine - z * cameraPitchSine >> 16;
			z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
			y = w;

			if (z != 0) {
				int sx = Graphics3D.halfWidth + (x << 9) / z;
				int sy = Graphics3D.halfHeight + (y << 9) / z;

				// no need to worry about clipping
				Graphics2D.fillRect(sx - 1, sy - 1, 3, 3, 0xFF0000);
			}
		}

		if ((flags & DRAW_DEBUG) != 0) {
			Runtime r = Runtime.getRuntime();

			graphics.drawString("Mem: " + (r.totalMemory() - r.freeMemory()) / 1024 + "k", 16, 16);
			graphics.drawString("Drawing model: " + Model.drawingModel, 16, 32);
			graphics.drawString("Triangles: " + model.triangleCount, 16, 48);
			graphics.drawString("Vertices: " + model.vertexCount, 16, 64);
			graphics.drawString("Fps: " + fps, 16, 80);
			graphics.drawString("Ft: " + ft, 16, 96);
			graphics.drawString(cameraX + ", " + cameraY + ", " + cameraZ, 16, 112);
		}
	}

	@Override
	public void run() {
		Graphics g = getGraphics();

		while (g == null) {
			g = getGraphics();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}

		int currentFrame = 0;
		int ratio = 256;
		int delay = 1;

		for (int n = 0; n < 10; n++) {
			this.optim[n] = System.currentTimeMillis();
		}

		long currentTime;

		while (this.running) {
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

			if (this.deltime > 0) {
				this.fps = (ratio * 1000) / (this.deltime * 256);
			}

			long nano = System.nanoTime();
			draw();
			g.drawImage(image, 0, 0, null);
			this.ft = (System.nanoTime() - nano) / 1_000_000.0;
		}
	}

	int lastDragX, lastDragY;

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

}
