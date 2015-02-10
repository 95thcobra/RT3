package dane;

import dane.Graphics2D;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;

public class Bitmap extends Graphics2D {

	private static final Logger logger = Logger.getLogger(Bitmap.class.toString());

	public static final Bitmap load(File f) throws IOException {
		BufferedImage image = ImageIO.read(f);
		Bitmap b = new Bitmap(image.getWidth(), image.getHeight());
		image.getRGB(0, 0, b.width, b.height, b.pixels, 0, b.width);
		for (int i = 0; i < b.pixels.length; i++) {
			b.pixels[i] &= ~(0xFF000000);
		}
		return b;
	}

	public int[] pixels;
	public int width;
	public int height;
	public int clipX;
	public int clipY;
	public int clipWidth;
	public int clipHeight;

	public Bitmap(int w, int h) {
		pixels = new int[w * h];
		width = clipWidth = w;
		height = clipHeight = h;
		clipX = clipY = 0;
	}

	public Bitmap(byte[] src, Component c) {
		try {
			Image i = Toolkit.getDefaultToolkit().createImage(src);
			MediaTracker mt = new MediaTracker(c);
			mt.addImage(i, 0);
			mt.waitForAll();
			width = i.getWidth(c);
			height = i.getHeight(c);
			clipWidth = width;
			clipHeight = height;
			clipX = 0;
			clipY = 0;
			pixels = new int[width * height];
			PixelGrabber pg = new PixelGrabber(i, 0, 0, width, height, pixels, 0, width);
			pg.grabPixels();
		} catch (Exception e) {
			System.out.println("Error converting jpg");
		}
	}

	public void bind() {
		Graphics2D.setTarget(pixels, width, height);
	}

	public void replace(int rgbA, int rgbB) {
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == rgbA) {
				pixels[i] = rgbB;
			}
		}
	}

	public void drawOpaque(int x, int y) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Graphics2D.targetWidth;
		int srcOff = 0;
		int h = height;
		int w = width;
		int dstStep = Graphics2D.targetWidth - w;
		int srcStep = 0;

		if (y < Graphics2D.top) {
			int cutoff = Graphics2D.top - y;
			h -= cutoff;
			y = Graphics2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Graphics2D.targetWidth;
		}

		if (y + h > Graphics2D.bottom) {
			h -= y + h - Graphics2D.bottom;
		}

		if (x < Graphics2D.left) {
			int cutoff = Graphics2D.left - x;
			w -= cutoff;
			x = Graphics2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}
		if (x + w > Graphics2D.right) {
			int i_22_ = x + w - Graphics2D.right;
			w -= i_22_;
			srcStep += i_22_;
			dstStep += i_22_;
		}

		if (w > 0 && h > 0) {
			copyImage(w, h, pixels, srcOff, srcStep, Graphics2D.target, dstOff, dstStep);
		}
	}

	private void copyImage(int w, int h, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep) {
		int hw = -(w >> 2);
		w = -(w & 0x3);

		for (int y = -h; y < 0; y++) {
			for (int x = hw; x < 0; x++) {
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
			}

			for (int x = w; x < 0; x++) {
				dst[dstOff++] = src[srcOff++];
			}

			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Graphics2D.targetWidth;
		int srcOff = 0;
		int w = width;
		int h = height;
		int dstStep = Graphics2D.targetWidth - w;
		int srcStep = 0;

		if (y < Graphics2D.top) {
			int cutoff = Graphics2D.top - y;
			h -= cutoff;
			y = Graphics2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Graphics2D.targetWidth;
		}

		if (y + h > Graphics2D.bottom) {
			h -= y + h - Graphics2D.bottom;
		}

		if (x < Graphics2D.left) {
			int cutoff = Graphics2D.left - x;
			w -= cutoff;
			x = Graphics2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (x + w > Graphics2D.right) {
			int cutoff = x + w - Graphics2D.right;
			w -= cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (w > 0 && h > 0) {
			copyImage(h, w, pixels, srcOff, srcStep, Graphics2D.target, dstOff, dstStep, 0);
		}
	}

	public void copyImage(int h, int w, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep, int rgb) {
		int hw = -(w >> 2);
		w = -(w & 0x3);
		for (int x = -h; x < 0; x++) {
			for (int y = hw; y < 0; y++) {
				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			for (int y = w; y < 0; y++) {
				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y, int alpha) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Graphics2D.targetWidth;
		int srcOff = 0;
		int w = width;
		int h = height;
		int dstStep = Graphics2D.targetWidth - w;
		int srcStep = 0;

		if (y < Graphics2D.top) {
			int cutoff = Graphics2D.top - y;
			h -= cutoff;
			y = Graphics2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Graphics2D.targetWidth;
		}

		if (y + h > Graphics2D.bottom) {
			h -= y + h - Graphics2D.bottom;
		}

		if (x < Graphics2D.left) {
			int cutoff = Graphics2D.left - x;
			w -= cutoff;
			x = Graphics2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (x + w > Graphics2D.right) {
			int cutoff = x + w - Graphics2D.right;
			w -= cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (w > 0 && h > 0) {
			copyImage(w, h, pixels, srcOff, srcStep, Graphics2D.target, dstOff, dstStep, alpha, 0);
		}
	}

	private void copyImage(int w, int h, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep, int alpha, int rgb) {
		int opacity = 256 - alpha;
		for (int y = -h; y < 0; y++) {
			for (int x = -w; x < 0; x++) {
				rgb = src[srcOff++];
				if (rgb != 0) {
					int dstRGB = dst[dstOff];
					dst[dstOff++] = ((((rgb & 0xff00ff) * alpha + (dstRGB & 0xff00ff) * opacity) & ~0xff00ff) + (((rgb & 0xff00) * alpha + (dstRGB & 0xff00) * opacity) & 0xff0000)) >> 8;
				} else {
					dstOff++;
				}
			}
			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y, int w, int h, int pivotX, int pivotY, int theta, int[] lineStart, int[] lineWidth) {
		try {
			int cx = -w / 2;
			int cy = -h / 2;

			int sin = (int) (Math.sin((double) theta / 326.11) * 65536.0);
			int cos = (int) (Math.cos((double) theta / 326.11) * 65536.0);

			int offX = (pivotX << 16) + (cy * sin + cx * cos);
			int offY = (pivotY << 16) + (cy * cos - cx * sin);
			int baseOffset = x + (y * Graphics2D.targetWidth);

			for (y = 0; y < h; y++) {
				int start = lineStart[y];
				int off = baseOffset + start;
				int srcX = offX + cos * start;
				int srcY = offY - sin * start;
				for (x = 0; x < lineWidth[y]; x++) {
					Graphics2D.target[off++] = pixels[(srcX >> 16) + (srcY >> 16) * width];
					srcX += cos;
					srcY -= sin;
				}
				offX += sin;
				offY += cos;
				baseOffset += Graphics2D.targetWidth;
			}
		} catch (Exception e) {
		}
	}

	public void draw(int x, int y, int w, int h, int pivotX, int pivotY, int theta) {
		try {
			int cx = -w / 2;
			int cy = -h / 2;

			int sin = (int) (Math.sin((double) theta / 326.11) * 65536.0);
			int cos = (int) (Math.cos((double) theta / 326.11) * 65536.0);

			int offX = (pivotX << 16) + (cy * sin + cx * cos);
			int offY = (pivotY << 16) + (cy * cos - cx * sin);
			int baseOffset = x + (y * Graphics2D.targetWidth);

			for (y = 0; y < h; y++) {
				int off = baseOffset;
				int dstX = offX + cos;
				int dstY = offY - sin;

				for (x = 0; x < w; x++) {
					int rgb = pixels[(dstX >> 16) + (dstY >> 16) * width];

					if (rgb != 0) {
						Graphics2D.target[off++] = rgb;
					} else {
						off++;
					}
					dstX += cos;
					dstY -= sin;
				}

				offX += sin;
				offY += cos;
				baseOffset += Graphics2D.targetWidth;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error drawing rotated bitmap", e);
		}
	}

	public void drawRotatedTest(int x, int y, int w, int h, int pivotX, int pivotY, int theta) {
		try {
			int cx = -w / 2;
			int cy = -h / 2;

			int sin = (int) (Math.sin((double) theta / 326.11) * 65536.0);
			int cos = (int) (Math.cos((double) theta / 326.11) * 65536.0);

			int offX = (pivotX << 16) + (cy * sin + cx * cos);
			int offY = (pivotY << 16) + (cy * cos - cx * sin);
			int baseOffset = x + (y * Graphics2D.targetWidth);

			for (y = 0; y < h; y++) {
				int off = baseOffset;
				int dstX = offX + cos;
				int dstY = offY - sin;

				for (x = 0; x < w; x++) {
					int left = x;
					int top = y;

					int right = (left + 1) % width;
					int bottom = (top + 1) % height;

					int rgbTopLeft = pixels[left + (top * width)];
					int rgbBottomLeft = pixels[left + (bottom * width)];

					int rgbTopRight = pixels[right + (top * width)];
					int rgbBottomRight = pixels[right + (bottom * width)];

					int u1 = (dstX >> 8) - (left << 8);
					int v1 = (dstY >> 8) - (top << 8);
					int u2 = (right << 8) - (dstX >> 8);
					int v2 = (bottom << 8) - (dstY >> 8);

					int a1 = u2 * v2;
					int a2 = u1 * v2;
					int a3 = u2 * v1;
					int a4 = u1 * v1;

					int r = (rgbTopLeft >> 16 & 0xff) * a1 + (rgbTopRight >> 16 & 0xff) * a2 + (rgbBottomLeft >> 16 & 0xff) * a3 + (rgbBottomRight >> 16 & 0xff) * a4 & 0xff0000;
					int g = (rgbTopLeft >> 8 & 0xff) * a1 + (rgbTopRight >> 8 & 0xff) * a2 + (rgbBottomLeft >> 8 & 0xff) * a3 + (rgbBottomRight >> 8 & 0xff) * a4 >> 8 & 0xff00;
					int b = (rgbTopLeft & 0xff) * a1 + (rgbTopRight & 0xff) * a2 + (rgbBottomLeft & 0xff) * a3 + (rgbBottomRight & 0xff) * a4 >> 16;

					Graphics2D.target[off++] = r | g | b;
					dstX += cos;
					dstY -= sin;
				}

				offX += sin;
				offY += cos;
				baseOffset += Graphics2D.targetWidth;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error drawing rotated bitmap", e);
		}
	}

	public void flipHorizontally() {
		int[] flipped = new int[width * height];
		int off = 0;
		for (int y = 0; y < height; y++) {
			for (int x = width - 1; x >= 0; x--) {
				flipped[off++] = pixels[x + (y * width)];
			}
		}
		pixels = flipped;
		clipX = clipWidth - width - clipX;
	}

	public void flipVertically() {
		int[] flipped = new int[width * height];
		int off = 0;
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				flipped[off++] = pixels[x + (y * width)];
			}
		}
		pixels = flipped;
		clipY = clipHeight - height - clipY;
	}

}