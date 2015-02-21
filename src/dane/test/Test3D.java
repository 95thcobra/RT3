package dane.test;

import dane.*;
import java.awt.event.*;
import java.io.*;
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
public class Test3D extends TestApplet {

	private static final Logger logger = Logger.getLogger(Test3D.class.getName());

	private static class TestEntity {

		public int x, y, z;
		public int pitch, yaw;
		public Model model;
		public int bitset;

		public void draw(int cameraPitchSine, int cameraPitchCosine, int cameraYawSine, int cameraYawCosine, int cameraX, int cameraY, int cameraZ) {
			if (model == null) {
				return;
			}

			model.draw(this.pitch, this.yaw, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX - x, cameraY - y, cameraZ - z, bitset);
		}

	}

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
	List<TestEntity> entities = new ArrayList<>();
	Model grid;
	int rotation = 0;

	int cameraPitch = 128;
	int cameraYaw;
	int cameraX;
	int cameraY = 512;
	int cameraZ = 1024;

	public Test3D() {
		Graphics3D.texturedShading = false;

		long time = System.nanoTime();
		grid = new Grid(13312 / 4, 104 / 4, 104 / 4);
		time = System.nanoTime() - time;
		System.out.println("Grid took " + String.format("%sms", time / 1_000_000.0) + " to create.");

		try {
			sprite = Sprite.load(new File("test.png"));
		} catch (IOException ex) {
			Logger.getLogger(Test3D.class.getName()).log(Level.SEVERE, null, ex);
		}

		int x = 0;

		for (String s : new String[]{"cube.ply", "icosphere.ply", "suzanne.ply"}) {
			try {
				Model m = ModelReader.get("ply").read(new File(s));
				m.setColor(64);
				m.calculateBoundaries();
				m.calculateNormals();
				m.calculateLighting(64, 768, -50, -50, -30);

				TestEntity e = new TestEntity();
				e.x = x;
				e.y = 200;
				e.model = m;
				entities.add(e);

				x += 512;
			} catch (Exception e) {
				logger.log(Level.WARNING, null, e);
			}
		}

		for (int i = 0; i < grid.vertexCount; i++) {
			grid.vertexY[i] += (int) (Math.random() * 128);
		}

		// we need bounds before we center
		grid.calculateBoundaries();
		grid.translate(-grid.maxBoundX / 2, -grid.minBoundY / 2, -grid.maxBoundZ / 2);
		grid.setColor((14 << 10) | (3 << 7) | 88);
		grid.calculateNormals();
		grid.calculateLighting(64, 768, -50, -50, -30);

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

		int speed = 32;
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
		Graphics2D.clear(0xBFEEFF);
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
			for (TestEntity e : entities) {
				e.draw(cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ);
				e.yaw += 8;
				e.yaw &= 0x7FF;
			}

			grid.draw(0, 0, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ, 1);
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
				int sx = Graphics3D.centerX + (x << 9) / z;
				int sy = Graphics3D.centerY + (y << 9) / z;

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

			Graphics2D.drawString("Triangles: " + grid.triangleCount, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Vertices: " + grid.vertexCount, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Fps: " + fps, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Ft: " + frameTime, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			y += 32;

			int w = 128 + (32 * Model.sin[rotation] >> 16);
			int h = 128 + (32 * Model.cos[rotation] >> 16);
			sprite.draw(0, y, w, h);
		}

	}

}
