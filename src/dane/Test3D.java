package dane;

import dane.applet.AppletShell;
import dane.input.Keyboard;
import dane.input.Mouse;
import dane.input.MouseButton;
import dane.media2d.BitmapFont;
import dane.media2d.Graphics2D;
import dane.media2d.ImageProducer3D;
import dane.media2d.Sprite;
import dane.media3d.Graphics3D;
import dane.media3d.Model;
import dane.media3d.primitive.Grid;
import dane.media3d.reader.ModelReader;
import dane.util.Colors;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


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
public class Test3D extends AppletShell {

	private static final Logger logger = Logger.getLogger(Test3D.class.getName());

	@Override
	public void shutdown() {
		System.out.println("shutdown n junk");
	}

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

	ImageProducer3D viewport;
	Sprite sprite;
	List<TestEntity> entities = new ArrayList<>();
	Model grid;
	int rotation = 0;

	int cameraPitch = 128;
	int cameraYaw;
	int cameraX = -1600;
	int cameraY = 484;
	int cameraZ = 384;

	@Override
	public void initialize(int width, int height) {
		viewport = new ImageProducer3D(width, height);
		viewport.bind();

		Graphics3D.createPalette(1.0);
		Graphics3D.texturedShading = false;

		long time = System.nanoTime();
		{
			grid = new Grid(13312, 64, 64);

			for (int i = 0; i < grid.vertexCount; i++) {
				grid.vertexY[i] += (int) (Math.random() * 128);
			}

			// we need bounds before we center
			grid.calculateBoundaries();
			grid.translate(-grid.maxBoundX / 2, -grid.minBoundY / 2, -grid.maxBoundZ / 2);
			grid.setColor(Colors.rgbToHSL16(0x7CFC00));
			grid.calculateNormals();
			grid.calculateLighting(64, 768, -50, -50, -30);
		}
		time = System.nanoTime() - time;
		System.out.println("Grid took " + String.format("%sms", time / 1_000_000.0) + " to create.");

		time = System.nanoTime();
		{
			try {
				sprite = Sprite.load(new File("test.png"));
			} catch (IOException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
		time = System.nanoTime() - time;
		System.out.println("Sprite took " + String.format("%sms", time / 1_000_000.0) + " to load.");

		time = System.nanoTime();
		{
			int x = -1280;
			for (String s : new String[]{"cube.ply", "icosphere.ply", "cone.ply", "torus.ply", "torusknot.ply", "teapot.ply", "suzanne.ply"}) {
				try {
					Model m = ModelReader.get("ply").read(new File(s));
					m.setColor(Colors.rgbToHSL16(0xFFD700));
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
					logger.log(Level.WARNING, "Error reading " + s, e);
				}
			}
		}
		time = System.nanoTime() - time;
		System.out.println("Models took " + String.format("%sms", time / 1_000_000.0) + " to load.");

		time = System.nanoTime();
		{
			try {
				TestEntity e = new TestEntity();
				e.x = -1600;
				e.y = 200;

				Model m = ModelReader.get("obj").read(new File("untitled.obj"));
				m.calculateBoundaries();
				m.calculateNormals();
				m.calculateLighting(64, 768, -50, -50, -30);

				e.model = m;

				System.out.println(m.vertexCount + ", " + m.triangleCount);

				entities.add(e);
			} catch (Exception e) {
				logger.log(Level.WARNING, null, e);
			}
		}
		time = System.nanoTime() - time;
		System.out.println("Obj model took " + String.format("%sms", time / 1_000_000.0) + " to load.");

		super.initialize(width, height);
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

	public boolean keyIsDown(int key) {
		return false;
	}

	@Override
	public void update() {
		Mouse mouse = this.getMouse();
		Keyboard keyboard = this.getKeyboard();

		if (mouse.getActiveButton() == MouseButton.LEFT) {
			int dx = mouse.getDragDeltaX() * 2;
			int dy = mouse.getDragDeltaY() * 2;

			if (dx != 0 || dy != 0) {
				cameraYaw -= dx;
				cameraYaw &= 0x7FF;

				cameraPitch += dy;
				cameraPitch &= 0x7FF;
			}
		}

		int speed = 32;
		int backward = 0;
		int left = 0;

		if (keyboard.isKeyDown(KeyEvent.VK_W)) {
			backward = -speed;
		} else if (keyboard.isKeyDown(KeyEvent.VK_S)) {
			backward = speed;
		}

		if (keyboard.isKeyDown(KeyEvent.VK_A)) {
			left = speed;
		} else if (keyboard.isKeyDown(KeyEvent.VK_D)) {
			left = -speed;
		}

		if (backward != 0 || left != 0) {
			translateCamera(backward, left);
		}
	}

	@Override
	public void draw(Graphics g, int width, int height) {
		Model.frameTriangleCount = 0;

		Graphics2D.clear(Colors.SKYBLUE);
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

			Graphics2D.drawString("Triangles: " + Model.frameTriangleCount, x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Fps: " + this.getFPS(), x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Graphics2D.drawString("Ft: " + (Math.round(this.getFrameTime() * 10000.0) / 10000.0) + "ms", x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;

			Mouse mouse = this.getMouse();
			Graphics2D.drawString("Mouse: " + mouse.getActiveButton() + ", " + mouse.getX() + ", " + mouse.getY(), x, y, color, BitmapFont.SHADOW);
			y += Graphics2D.font.height;
			y += 32;

			int w = 128 + (32 * Model.sin[rotation] >> 16);
			int h = 128 + (32 * Model.cos[rotation] >> 16);
			sprite.draw(0, y, w, h);
		}

		viewport.draw(g, 0, 0);
	}

}
