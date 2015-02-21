package dane.test;

import dane.*;
import java.awt.event.*;
import java.io.*;
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
public class Test3D extends TestApplet {

	private static final Logger logger = Logger.getLogger(Test3D.class.getName());

	public static void main(String[] args) throws Throwable {
		JFrame f = new JFrame();

		Test3D test = new Test3D();
		f.add(test);
		f.setResizable(false);

		test.initialize(512 * 2, 334 * 2);
		f.pack();

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	Sprite sprite;
	Model cube;
	Model model;
	int rotation = 0;

	int cameraPitch = 128;
	int cameraYaw;
	int cameraX;
	int cameraY = 512;
	int cameraZ = 1024;

	public Test3D() {
		Graphics3D.texturedShading = false;

		long time = System.nanoTime();
		model = new Grid(13312 / 4, 104 / 4, 104 / 4);
		time = System.nanoTime() - time;
		System.out.println("Grid took " + String.format("%sms", time / 1_000_000.0) + " to create.");

		try {
			sprite = Sprite.load(new File("test.png"));
		} catch (IOException ex) {
			Logger.getLogger(Test3D.class.getName()).log(Level.SEVERE, null, ex);
		}

		cube = new Cube(64);

		cube.calculateBoundaries();
		cube.translate(-cube.maxBoundX / 2, -cube.minBoundY / 2, -cube.maxBoundZ / 2);

		cube.triangleColor = new int[cube.triangleCount];

		for (int i = 0; i < cube.triangleCount; i++) {
			cube.triangleColor[i] = ((14 + (int) (Math.random() * 64)) << 10) | (3 << 7) | 64;
		}

		cube.colorA = new int[cube.triangleCount];
		cube.colorB = new int[cube.triangleCount];
		cube.colorC = new int[cube.triangleCount];

		cube.triangleType = new int[cube.triangleCount];
		//Arrays.fill(cube.triangleType, 1);
		cube.calculateNormals();
		cube.applyLighting(64, 768, -50, -50, -30, true);

		for (int i = 0; i < model.vertexCount; i++) {
			model.vertexY[i] += (int) (Math.random() * 128);
		}

		// we need bounds before we center
		model.calculateBoundaries();
		model.translate(-model.maxBoundX / 2, -model.minBoundY / 2, -model.maxBoundZ / 2);

		model.triangleColor = new int[model.triangleCount];

		for (int i = 0; i < model.triangleCount; i++) {
			model.triangleColor[i] = ((14 + (int) (Math.random() * 2)) << 10) | (3 << 7) | 64;
		}

		model.colorA = new int[model.triangleCount];
		model.colorB = new int[model.triangleCount];
		model.colorC = new int[model.triangleCount];

		model.triangleType = new int[model.triangleCount];
		//Arrays.fill(model.triangleType, 1);
		model.calculateNormals();
		model.applyLighting(64, 768, -50, -50, -30, true);

		System.gc();
	}

	public void translateCamera(int backward, int left) {
		int pitch = 2048 - cameraPitch & 0x7ff;
		int yaw = 2048 - cameraYaw & 0x7ff;

		int offsetX = left;
		int offsetY = 0;
		int offsetZ = backward;

		if (pitch != 0) {
			int pitchSin = Model.sin[pitch];
			int pitchCos = Model.cos[pitch];
			int w = offsetY * pitchCos - offsetZ * pitchSin >> 16;
			offsetZ = offsetY * pitchSin + offsetZ * pitchCos >> 16;
			offsetY = w;
		}

		if (yaw != 0) {
			int yawSin = Model.sin[yaw];
			int yawCos = Model.cos[yaw];
			int w = offsetZ * yawSin + offsetX * yawCos >> 16;
			offsetZ = offsetZ * yawCos - offsetX * yawSin >> 16;
			offsetX = w;
		}

		cameraX += offsetX;
		cameraY += offsetY;
		cameraZ += offsetZ;
	}

	public boolean dragging = false;
	public int dragX = -1;
	public int dragY = -1;

	@Override
	public void update() {
		boolean wasDragging = dragging;
		dragging = dragButton != 0;

		if (!wasDragging && dragging) {
			dragX = mouseX;
			dragY = mouseY;
		}

		if (dragging) {
			int dx = mouseX - dragX;
			int dy = mouseY - dragY;

			cameraYaw -= dx;
			cameraYaw &= 0x7FF;

			cameraPitch += dy;
			cameraPitch &= 0x7FF;

			dragX = mouseX;
			dragY = mouseY;
		}

		int speed = 64;
		int backward = 0;
		int left = 0;

		if (keyIsDown(KeyEvent.VK_W)) {
			backward = -speed;
		} else if (keyIsDown(KeyEvent.VK_S)) {
			backward = speed;
		}

		if (keyIsDown(KeyEvent.VK_A)) {
			left = speed;
		} else if (keyIsDown(KeyEvent.VK_D)) {
			left = -speed;
		}

		if (backward != 0 || left != 0) {
			translateCamera(backward, left);
		}
	}

	public void draw() {
		Graphics3D.clear(0xBFEEFF);
		Graphics3D.clearZBuffer();

		int cameraPitchSine = Model.sin[cameraPitch];
		int cameraPitchCosine = Model.cos[cameraPitch];
		int cameraYawSine = Model.sin[cameraYaw];
		int cameraYawCosine = Model.cos[cameraYaw];

		final int DRAW_MODEL = 1;
		final int DRAW_ORIGIN_DOT = (1 << 1);
		final int DRAW_DEBUG = (1 << 2);
		int flags = DRAW_MODEL | DRAW_DEBUG | DRAW_ORIGIN_DOT;

		if ((flags & DRAW_MODEL) != 0) {
			cube.draw(0, 0, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY - 300, cameraZ, 1);
			model.draw(0, 0, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ, 1);
			rotation += 16;
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

				if (sx >= 0 && sx <= width && sy >= 0 && sy <= height) {
					Graphics2D.fillRect(sx - 1, sy - 1, 3, 3, 0xFF0000);
				}
			}
		}

		if ((flags & DRAW_DEBUG) != 0) {
			Runtime r = Runtime.getRuntime();

			int x = 8;
			int y = 16;
			int color = 0xFFFFFF;

			Graphics2D.drawString("Mem: " + (r.totalMemory() - r.freeMemory()) / 1024 + "k", x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Drawing model: " + Model.drawingModel, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Triangles: " + model.triangleCount, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Vertices: " + model.vertexCount, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Fps: " + fps, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Ft: " + frameTime, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;
		}

		int w = 128 + (96 * Model.sin[rotation] >> 16);
		int h = 128 + (96 * Model.cos[rotation] >> 16);
		sprite.draw((width - w) / 2, (height - h) / 2, w, h);

	}

}
