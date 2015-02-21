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
package dane;

/**
 *
 * @author Dane
 */
public class ColorUtil {

	/**
	 * Converts an INT24_RGB value to INT24_HSL.
	 *
	 * @param rgb the rgb.
	 * @return the HSL.
	 */
	public static final int rgbToHSL(int rgb) {
		double r = (double) ((rgb >> 16) & 0xFF) / 256.0;
		double g = (double) ((rgb >> 8) & 0xFF) / 256.0;
		double b = (double) (rgb & 0xFF) / 256.0;
		return rgbToHSL(r, g, b);
	}

	/**
	 * Converts an INT24_RGB value to INT24_HSL.
	 *
	 * @param r the red channel.
	 * @param g the green channel.
	 * @param b the blue channel.
	 * @return the HSL.
	 */
	public static final int rgbToHSL(double r, double g, double b) {
		double min = Math.min(Math.min(r, g), b);
		double max = Math.max(Math.max(r, g), b);

		double hue = 0.0;
		double saturation = 0.0;
		double lightness = (min + max) / 2.0;

		if (min != max) {
			if (lightness < 0.5) {
				saturation = (max - min) / (max + min);
			}
			if (lightness >= 0.5) {
				saturation = (max - min) / (2.0 - max - min);
			}

			if (r == max) {
				hue = (g - b) / (max - min);
			} else if (g == max) {
				hue = 2.0 + (b - r) / (max - min);
			} else if (b == max) {
				hue = 4.0 + (r - g) / (max - min);
			}
		}

		hue /= 6.0;

		hue *= 256.0;
		saturation *= 256.0;
		lightness *= 256.0;

		return ((int) (hue) << 16) | ((int) (saturation) << 8) | ((int) lightness);
	}

	/**
	 * Converts INT24_HSL to INT16_HSL. A format usually used with the palette generated in RuneTek 3 engines. (Lossy)
	 *
	 * @param hsl
	 * @return
	 */
	public static final int hsl24To16(int hsl) {
		int hue = (hsl >> 16) & 0xFF;
		int saturation = (hsl >> 8) & 0xFF;
		int lightness = hsl & 0xFF;

		if (lightness > 179) {
			saturation /= 2;
		}

		if (lightness > 192) {
			saturation /= 2;
		}

		if (lightness > 217) {
			saturation /= 2;
		}

		if (lightness > 243) {
			saturation /= 2;
		}
		return ((hue / 4) << 10) | ((saturation / 32) << 7) | lightness;
	}
}
