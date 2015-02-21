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

import dane.media2d.Graphics2D;
import dane.media2d.Sprite;
import dane.media3d.Graphics3D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author Dane
 */
public class GenerateGrid {

	public static void main(String[] args) {
		Sprite b = new Sprite(512 + 64, 512 + 64);

		Graphics2D.setTarget(b.data, b.width, b.height);
		Graphics3D.setOffsets();
		Graphics3D.setZBuffer();

		int startX = 32;
		int startY = 32;
		int endX = b.width - 32;
		int endY = b.height - 32;
		int width = endX - startX;
		int height = endY - startY;
		int columns = 8;
		int rows = 8;
		int tileWidth = width / columns;
		int tileHeight = height / rows;

		int vertexCount = (columns + 1) * (rows + 1);
		int[] vertexX = new int[vertexCount];
		int[] vertexY = new int[vertexCount];

		int i = 0;
		for (int y = 0; y < rows + 1; y++) {
			for (int x = 0; x < columns + 1; x++) {
				vertexX[i] = startX + (x * tileWidth);
				vertexY[i++] = startY + (y * tileHeight);
			}
		}

		Graphics3D.alpha = 127;

		int c = columns + 1;
		int d = c + 1;

		int a = 0;
		for (i = 0; i < (columns * rows); i++) {
			if (a % c == columns) {
				a++;
			}

			int aX = vertexX[a];
			int aY = vertexY[a];

			int bX = vertexX[a + 1];
			int bY = vertexY[a + 1];

			int cX = vertexX[a + c];
			int cY = vertexY[a + c];

			int dX = vertexX[a + d];
			int dY = vertexY[a + d];

			a++;

			Graphics3D.fillTriangleDepth(aX, aY, 0, bX, bY, 0, cX, cY, 0, (int) (0x7F + (Math.random() * 0x7F)) << 8);
			Graphics3D.fillTriangleDepth(cX, cY, 0, bX, bY, 0, dX, dY, 0, (int) (0x7F + (Math.random() * 0x7F)) << 8);
		}

		try {
			BufferedImage image = new BufferedImage(b.width, b.height, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, b.width, b.height, b.data, 0, b.width);
			ImageIO.write(image, "png", new File("grid.png"));
		} catch (Exception e) {
			// ignore
		}
	}
}
