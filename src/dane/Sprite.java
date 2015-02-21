package dane;

import dane.Graphics2D;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;

public class Sprite extends Graphics2D {

	private static final Logger logger = Logger.getLogger(Sprite.class.toString());

	public static final Sprite load(File f) throws IOException {
		BufferedImage image = ImageIO.read(f);
		Sprite b = new Sprite(image.getWidth(), image.getHeight());
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

	public Sprite(int width, int height) {
		this.pixels = new int[width * height];
		this.width = this.clipWidth = width;
		this.height = this.clipHeight = height;
		this.clipX = this.clipY = 0;
	}

	public Sprite(byte[] src, Component c) {
		try {
			Image i = Toolkit.getDefaultToolkit().createImage(src);
			MediaTracker t = new MediaTracker(c);
			t.addImage(i, 0);
			t.waitForAll();
			this.width = i.getWidth(c);
			this.height = i.getHeight(c);
			this.clipWidth = this.width;
			this.clipHeight = this.height;
			this.clipX = 0;
			this.clipY = 0;
			this.pixels = new int[this.width * this.height];
			PixelGrabber g = new PixelGrabber(i, 0, 0, this.width, this.height, this.pixels, 0, this.width);
			g.grabPixels();
		} catch (Exception e) {
			System.out.println("Error converting jpg");
		}
	}

	public void bind() {
		Graphics2D.setTarget(this.pixels, this.width, this.height);
	}

	public void replaceRGB(int a, int b) {
		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == a) {
				this.pixels[i] = b;
			}
		}
	}

	// technically same as draw(int x, int y)
	public void drawOpaque(int x, int y) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels);
	}

	public void draw(int x, int y) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels);
	}

	public void draw(int x, int y, int alpha) {
		Graphics2D.drawPixels(x, y, this.width, this.height, this.pixels, alpha);
	}

	public void draw(int x, int y, int w, int h) {
		Graphics2D.drawSprite(this, x, y, w, h);
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
					Graphics2D.target[off++] = this.pixels[(srcX >> 16) + (srcY >> 16) * this.width];
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
