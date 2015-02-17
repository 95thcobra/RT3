package dane.test;

import dane.*;
import java.awt.event.*;
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

		test.initialize(800, 600);
		f.pack();

		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	Model model;
	int rotation = 0;

	int cameraPitch = 128;
	int cameraYaw;
	int cameraX;
	int cameraY = 2048;
	int cameraZ = 8192;

	public Test3D() {

		model = new Grid(13312, 32, 32);

		// we need bounds before we center
		model.calculateBoundaries();
		model.translate(-model.maxBoundX / 2, -model.minBoundY / 2, -model.maxBoundZ / 2);

		for (int i = 0; i < model.vertexCount; i++) {
			model.vertexY[i] += (int) (Math.random() * 128);
		}

		model.triangleColor = new int[model.triangleCount];

		for (int i = 0; i < model.triangleCount; i++) {
			model.triangleColor[i] = (17 << 10) | (3 << 7) | 48;
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

	public int dragX = -1;
	public int dragY = -1;

	@Override
	public void update() {
		if (dragButton != 0) {
			if (dragX == -1) {
				dragX = mouseX;
				dragY = mouseY;
			} else {
				int dx = mouseX - dragX;
				int dy = mouseY - dragY;

				cameraYaw -= dx;
				cameraYaw &= 0x7FF;

				cameraPitch += dy;
				cameraPitch &= 0x7FF;

				dragX = mouseX;
				dragY = mouseY;
			}
		}

		int cameraPitchSine = Model.sin[cameraPitch];
		int cameraPitchCosine = Model.cos[cameraPitch];
		int cameraYawSine = Model.sin[cameraYaw];
		int cameraYawCosine = Model.cos[cameraYaw];

		int speed = 128;
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
		Graphics3D.clear(0x303030);
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
			model.draw(0, 0, cameraPitchSine, cameraPitchCosine, cameraYawSine, cameraYawCosine, cameraX, cameraY, cameraZ, 1);
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

			graphics.drawString("Mem: " + (r.totalMemory() - r.freeMemory()) / 1024 + "k", 8, 16);
			graphics.drawString("Drawing model: " + Model.drawingModel, 8, 32);
			graphics.drawString("Triangles: " + model.triangleCount, 8, 48);
			graphics.drawString("Vertices: " + model.vertexCount, 8, 64);
			graphics.drawString("Fps: " + fps, 8, 80);
			graphics.drawString("Ft: " + frameTime, 8, 96);
		}
	}

}
