package dane;

import dane.Graphics2D;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
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

	int fps = 50;
	double ft = 0.0;

	Model model;
	int rotation = 0;

	int cameraPitch = 196;
	int cameraYaw;
	int cameraX;
	int cameraY = 1024;
	int cameraZ = 2048;

	boolean running;

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

		model = new Model();

		// assemble our grid model
		for (int x = 0; x < 32; x++) {
			for (int z = 0; z < 32; z++) {
				int x1 = x * 128;
				int z1 = z * 128;

				int x2 = x1 + 128;
				int z2 = z1 + 128;

				int a = model.getVertex(x1, 0, z1);
				int b = model.getVertex(x2, 0, z1);
				int c = model.getVertex(x1, 0, z2);
				int d = model.getVertex(x2, 0, z2);

				model.addTriangle(a, b, c);
				model.addTriangle(c, b, d);
			}
		}

		// we need bounds before we center
		model.calculateBoundaries();

		// center the model
		model.translate(-model.maxBoundX / 2, 0, -model.maxBoundZ / 2);

		// raise the points to give it a hilly look
		for (int i = 0; i < model.vertexCount; i++) {
			model.vertexY[i] += (int) (Math.random() * 32);
		}

		model.triangleColor = new int[model.triangleCount];

		for (int i = 0; i < model.triangleCount; i++) {
			model.triangleColor[i] = (17 << 10) | (3 << 7) | 48;
		}

		model.colorA = new int[model.triangleCount];
		model.colorB = new int[model.triangleCount];
		model.colorC = new int[model.triangleCount];

		model.triangleType = new int[model.triangleCount];
		model.calculateNormals();
		model.applyLighting(64, 768, -50, -50, -30, true);

		System.gc();

		running = true;
		new Thread(this).start();
	}

	@Override
	public void addNotify() {
		super.addNotify(); //To change body of generated methods, choose Tools | Templates.
	}

	public void draw() {
		Graphics3D.clear(0x303030);
		Graphics3D.clearZBuffer();

		int cameraPitchSine = Model.sin[cameraPitch];
		int cameraPitchCosine = Model.cos[cameraPitch];
		int cameraYawSine = Model.sin[cameraYaw];
		int cameraYawCosine = Model.cos[cameraYaw];

		DRAW_MODEL:
		{
			model.draw(0, rotation, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ, 1);
			rotation += 8;
			rotation &= 0x7FF;
		}

		// I just wanted a reason to show vector rotation so anybody can see how to translate a 3d point to a point on the screen.
		DRAW_ORIGIN_DOT:
		{
			int x = 0 - cameraX;
			int y = 0 - cameraY;
			int z = 0 - cameraZ;

			int w = z * cameraYawSine + x * cameraYawCosine >> 16;
			z = z * cameraYawCosine - x * cameraYawSine >> 16;
			x = w;

			w = y * cameraPitchCosine - z * cameraPitchSine >> 16;
			z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
			y = w;

			if (z == 0) {
				break DRAW_ORIGIN_DOT;
			}

			int sx = Graphics3D.halfWidth + (x << 9) / z;
			int sy = Graphics3D.halfHeight + (y << 9) / z;

			// no need to worry about clipping
			Graphics2D.fillRect(sx - 1, sy - 1, 3, 3, 0xFF0000);
		}

		DRAW_TEST:
		{
			int x = 32;
			int y = 128;

			Graphics2D.drawHorizontalLine(x, y, 36, 0xFFFFFF);
			y += 2;

			Graphics2D.drawHorizontalLine(x, y, 36, 0xFFFFFF, 127);
			y += 2;

			Graphics2D.drawVerticalLine(x, y, 32, 0xFFFFFF);
			x += 2;

			Graphics2D.drawVerticalLine(x, y, 32, 0xFFFFFF, 127);

			x += 2;

			Graphics2D.drawRect(x, y, 32, 32, 0xFFFFFF);
			Graphics2D.drawRect(x + 2, y + 2, 28, 28, 0xFFFFFF, 127);

			Graphics2D.fillCircle(x + 16, y + 16, 6, 0xFFFFFF);
			Graphics2D.fillCircle(x + 16, y + 16, 12, 0xFFFFFF, 127);

			x -= 2;
			y += 32;

			Graphics2D.fillOval(x, y, 32, 32, 0xFFFFFF, 3, 0);

			x += 32;
			Graphics2D.fillOval(x, y, 32, 32, 0xFFFFFF, 5, 0);

			x += 32;
			Graphics2D.fillOval(x, y, 32, 32, 0xFFFFFF, 3 + (rotation / 64), 0, 127);

		}

		DRAW_DEBUG:
		{
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

		long lastFPSUpdate = System.nanoTime();

		while (g == null) {
			g = getGraphics();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}

		int frame = 0;

		// not a proper game loop, just using it to draw *around* 50fps
		while (running) {
			long time = System.nanoTime();
			draw();
			g.drawImage(this.image, 0, 0, getWidth(), getHeight(), null);
			ft = (System.nanoTime() - time) / 1_000_000.0;

			frame++;

			if (time - lastFPSUpdate > 1_000_000_000) {
				fps = frame;
				frame = 0;
				lastFPSUpdate = time;
			}

			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
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
