package dane;

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
import java.util.*;

/**
 *
 * @author Dane
 */
public class Graphics2D {

	/**
	 * The array being modified by the operations in this class.
	 */
	public static int[] target;

	/**
	 * The dimensions of the destintaion.
	 */
	public static int targetWidth, targetHeight;

	/**
	 * The clipped drawing boundaries.
	 */
	public static int left, top, right, bottom;

	/**
	 * The center of the destination.
	 */
	public static int halfWidth, halfHeight;

	/**
	 * The rightmost horizontal position in our destination.
	 */
	public static int rightX;

	/**
	 * Used for drawing ovals.
	 */
	private static final int[] ovalPointX = new int[1024], ovalPointY = new int[1024];

	/**
	 * Fills our destination with 0's.
	 *
	 * @param rgb the clear color. (INT24_RGB)
	 */
	public static void clear(int rgb) {
		Arrays.fill(target, rgb);
	}

	/**
	 * Sets our destination and resets the boundaries to accomodate.
	 *
	 * @param pixels the pixels.
	 * @param width the width.
	 * @param height the height.
	 */
	public static void setTarget(int[] pixels, int width, int height) {
		Graphics2D.target = pixels;
		Graphics2D.targetWidth = width;
		Graphics2D.targetHeight = height;
		setBounds(0, 0, width, height);
	}

	/**
	 * Resets the boundaries to fit the destintaion.
	 */
	public static void resetBounds() {
		left = 0;
		top = 0;
		right = 0;
		bottom = 0;
		rightX = right - 1;
		halfWidth = right / 2;
		halfHeight = bottom / 2;
	}

	/**
	 * Sets the area which we allow ourselves to draw into.
	 *
	 * @param left the leftmost horizontal pixel.
	 * @param top the topmost vertical pixel.
	 * @param right the rightmost horizontal pixel.
	 * @param bottom the bottommost vertical pixel.
	 */
	public static void setBounds(int left, int top, int right, int bottom) {
		if (left < 0) {
			left = 0;
		}

		if (right > targetWidth) {
			right = targetWidth;
		}

		if (top < 0) {
			top = 0;
		}

		if (bottom > targetHeight) {
			bottom = targetHeight;
		}

		Graphics2D.left = left;
		Graphics2D.top = top;
		Graphics2D.right = right;
		Graphics2D.bottom = bottom;
		Graphics2D.rightX = right - 1;
		Graphics2D.halfWidth = right / 2;
		Graphics2D.halfHeight = bottom / 2;
	}

	/**
	 * Fills an opaque oval.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param rgb the color. (INT24_RGB)
	 * @param points the segment count.
	 * @param angle the starting angle.
	 */
	public static void fillOval(int x, int y, int w, int h, int rgb, int points, int angle) {
		if (points < 3) {
			return;
		}

		int hw = w / 2;
		int hh = h / 2;

		x += hw;
		y += hh;

		for (int i = 0; i < points; i++) {
			int a = angle + ((i << 11) / points);

			a %= 2047; // keep it within the 0-2047 range

			ovalPointX[i] = x + ((hw * Model.cos[a]) >> 16);
			ovalPointY[i] = y + ((hh * Model.sin[a]) >> 16);
		}

		int cx = x;
		int cy = y;

		for (int i = 1; i < points; i++) {
			x = ovalPointX[i - 1];
			y = ovalPointY[i - 1];

			Graphics3D.fillTriangle(cy, y, ovalPointY[i], cx, x, ovalPointX[i], rgb);
		}

		// fill from last point to first point (last triangle)
		Graphics3D.fillTriangle(cy, ovalPointY[0], ovalPointY[points - 1], cx, ovalPointX[0], ovalPointX[points - 1], rgb);
	}

	/*
	 * Fills an opaque oval.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param rgb the color. (INT24_RGB)
	 * @param segments the segment count.
	 * @param alpha the alpha. (0-FF)
	 * @param angle the starting angle.
	 */
	public static void fillOval(int x, int y, int w, int h, int rgb, int segments, int angle, int alpha) {
		Graphics3D.alpha = alpha;
		fillOval(x, y, w, h, rgb, segments, angle);
	}

	/**
	 * Fills an opaque circle.
	 *
	 * @param x the center x of the circle.
	 * @param y the center y of the circle.
	 * @param radius the radius of the circle.
	 * @param color the color. (INT24_RGB)
	 */
	public static void fillCircle(int x, int y, int radius, int color) {
		int radius2 = radius * radius; // used to avoid Math.sqrt

		for (int xA = x - radius; xA < x + radius; xA++) {
			if (xA < left || xA > right) {
				continue;
			}

			for (int yA = y - radius; yA < y + radius; yA++) {
				if (yA < top || yA > bottom) {
					continue;
				}

				int xD = xA - x;
				int yD = yA - y;
				int distance2 = xD * xD + yD * yD;

				if (distance2 < radius2) { // hey look! no sqrt
					target[xA + (yA * targetWidth)] = color;
				}
			}
		}
	}

	/**
	 * Fills an opaque circle.
	 *
	 * @param x the center x of the circle.
	 * @param y the center y of the circle.
	 * @param radius the radius of the circle.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void fillCircle(int x, int y, int radius, int color, int alpha) {
		int radius2 = radius * radius;

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);

		int alphaB = 256 - alpha;

		for (int xA = x - radius; xA < x + radius; xA++) {
			if (xA < left || xA > right) {
				continue;
			}

			for (int yA = y - radius; yA < y + radius; yA++) {
				if (yA < top || yA > bottom) {
					continue;
				}

				int xD = (xA - x);
				int yD = (yA - y);
				int distance2 = (xD * xD + yD * yD);

				if (distance2 < radius2) {
					int pos = xA + (yA * targetWidth);
					int old = target[pos];
					old = ((old & 0xFF00FF) * alphaB >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaB >> 8 & 0xFF00);
					target[pos] = color + old;
				}
			}
		}
	}

	/**
	 * Draws an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawRect(int x, int y, int w, int h, int color) {
		drawHorizontalLine(x, y, w, color);
		drawHorizontalLine(x, y + h - 1, w, color);
		drawVerticalLine(x, y, h, color);
		drawVerticalLine(x + w - 1, y, h, color);
	}

	/**
	 * Draws an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void drawRect(int x, int y, int w, int h, int color, int alpha) {
		drawHorizontalLine(x, y, w, color, alpha);
		drawHorizontalLine(x, y + h - 1, w, color, alpha);
		if (h > 2) {
			drawVerticalLine(x, y + 1, h - 2, color, alpha);
			drawVerticalLine(x + w - 1, y + 1, h - 2, color, alpha);
		}
	}

	/**
	 * Fills an opaque rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void fillRect(int x, int y, int w, int h, int color) {
		if (x < left) {
			w -= left - x;
			x = left;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (x + w > right) {
			w = right - x;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		int step = targetWidth - w;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				target[pos++] = color;
			}
			pos += step;
		}
	}

	/**
	 * Fills a translucent rectangle.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the width.
	 * @param h the height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void fillRect(int x, int y, int w, int h, int color, int alpha) {
		if (x < left) {
			w -= left - x;
			x = left;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (x + w > right) {
			w = right - x;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
		int alphaInverted = 256 - alpha;
		int step = targetWidth - w;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int old = target[pos];
				target[pos++] = color + ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
			}
			pos += step;
		}
	}

	/**
	 * Draws a horizontal line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the line width.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawHorizontalLine(int x, int y, int w, int color) {
		if (y >= top && y < bottom) {
			if (x < left) {
				w -= left - x;
				x = left;
			}

			if (x + w > right) {
				w = right - x;
			}

			int pos = x + y * targetWidth;

			for (int i = 0; i < w; i++) {
				target[pos++] = color;
			}
		}
	}

	/**
	 * Draws a vertical line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param h the line height.
	 * @param color the color. (INT24_RGB)
	 */
	public static void drawVerticalLine(int x, int y, int h, int color) {
		if (x < left || x >= right) {
			return;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			target[pos] = color;
			pos += targetWidth;
		}
	}

	/**
	 * Draws a horizontal line with an alpha channel.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param w the line width.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-0xFF)
	 */
	public static void drawHorizontalLine(int x, int y, int w, int color, int alpha) {
		if (y >= top && y < bottom) {
			if (x < left) {
				w -= left - x;
				x = left;
			}

			if (x + w > right) {
				w = right - x;
			}

			color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
			int alphaInverted = 256 - alpha;
			int pos = x + y * targetWidth;

			for (int i = 0; i < w; i++) {
				int old = target[pos];
				old = ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
				target[pos++] = color + old;
			}
		}
	}

	/**
	 * Draws a vertical line.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param h the line height.
	 * @param color the color. (INT24_RGB)
	 * @param alpha the alpha. (0-FF)
	 */
	public static void drawVerticalLine(int x, int y, int h, int color, int alpha) {
		if (x < left || x >= right) {
			return;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		color = ((color & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((color & 0xFF00) * alpha >> 8 & 0xFF00);
		int alphaInverted = 256 - alpha;
		int pos = x + y * targetWidth;

		for (int i = 0; i < h; i++) {
			int old = target[pos];
			old = ((old & 0xFF00FF) * alphaInverted >> 8 & 0xFF00FF) + ((old & 0xFF00) * alphaInverted >> 8 & 0xFF00);
			target[pos] = color + old;
			pos += targetWidth;
		}
	}

}
