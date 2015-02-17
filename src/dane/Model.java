package dane;

import java.util.*;

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
public class Model {

	public static boolean allowInput;
	public static int mouseX, mouseY;

	public static final int NEAR_Z = 50;
	public static final int FAR_Z = 0x7FFFF;

	public static final int MAX_COMPONENT_COUNT = 1024 * 12;

	public static boolean[] testTriangleX = new boolean[MAX_COMPONENT_COUNT];
	public static boolean[] projectTriangle = new boolean[MAX_COMPONENT_COUNT];

	public static int[] vertexScreenX = new int[MAX_COMPONENT_COUNT];
	public static int[] vertexScreenY = new int[MAX_COMPONENT_COUNT];
	public static int[] vertexDepth = new int[MAX_COMPONENT_COUNT];

	public static int[] projectSceneX = new int[MAX_COMPONENT_COUNT];
	public static int[] projectSceneY = new int[MAX_COMPONENT_COUNT];
	public static int[] projectSceneZ = new int[MAX_COMPONENT_COUNT];

	public static int[] tmpX = new int[10];
	public static int[] tmpY = new int[10];
	public static int[] tmpZ = new int[10];
	public static int[] tmpColor = new int[10];

	public static int[] sin = Graphics3D.sin;
	public static int[] cos = Graphics3D.cos;
	public static int[] palette = Graphics3D.palette;
	public static int[] oneOverFixed1616 = Graphics3D.oneOverFixed1616;

	public static final boolean withinTriangle(int x, int y, int y0, int y1, int y2, int x0, int x1, int x2) {
		if (y < y0 && y < y1 && y < y2) {
			return false;
		}
		if (y > y0 && y > y1 && y > y2) {
			return false;
		}
		if (x < x0 && x < x1 && x < x2) {
			return false;
		}
		return !(x > x0 && x > x1 && x > x2);
	}

	public static final int adjustHSLLightness(int hsl, int lightness, int type) {
		if ((type & 0x2) == 2) {
			if (lightness < 0) {
				lightness = 0;
			} else if (lightness > 127) {
				lightness = 127;
			}
			lightness = 127 - lightness;
			return lightness;
		}

		lightness = lightness * (hsl & 0x7f) >> 7;

		if (lightness < 2) {
			lightness = 2;
		} else if (lightness > 126) {
			lightness = 126;
		}

		return (hsl & 0xff80) + lightness;
	}

	public int vertexCount;
	public int[] vertexX = new int[0];
	public int[] vertexY = new int[0];
	public int[] vertexZ = new int[0];
	public Map<Long, Integer> vertexLookup = new HashMap<>();

	public int triangleCount;
	public int[] triangleVertexA = new int[0];
	public int[] triangleVertexB = new int[0];
	public int[] triangleVertexC = new int[0];

	public int[] triangleColor = new int[0];
	public int[] colorA = new int[0];
	public int[] colorB = new int[0];
	public int[] colorC = new int[0];

	public int[] triangleType;
	public int[] trianglePriorities;
	public int[] triangleAlpha;

	public int minBoundX, maxBoundX;
	public int minBoundZ, maxBoundZ;
	public int minBoundY, maxBoundY;
	public int boundLengthXZ;

	public int maxDepth, minDepth;

	public Normal[] normals, unmodifiedNormals;

	/**
	 * If there is an existing vertex in the same location, then it will be
	 * returned, else a new vertex is created and returned.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param z the z.
	 * @return the vertex index.
	 */
	public int getVertex(int x, int y, int z) {
		long l = (long) x | ((long) y << 20L) | ((long) z << 40L);

		if (vertexLookup.containsKey(l)) {
			return vertexLookup.get(l);
		}

		return addVertex(x, y, z);
	}

	public final int addVertex(int x, int y, int z) {
		int[] old = vertexX;
		int count = vertexCount + 1;

		if (vertexX.length < count) {
			vertexX = new int[count];
			System.arraycopy(old, 0, vertexX, 0, old.length);
			old = vertexY;

			vertexY = new int[count];
			System.arraycopy(old, 0, vertexY, 0, old.length);
			old = vertexZ;

			vertexZ = new int[count];
			System.arraycopy(old, 0, vertexZ, 0, old.length);
		}

		vertexX[vertexCount] = x;
		vertexY[vertexCount] = y;
		vertexZ[vertexCount] = z;
		vertexLookup.put((long) x | ((long) y << 20L) | ((long) z << 40L), vertexCount);
		return vertexCount++;
	}

	public final int addTriangle(int a, int b, int c) {
		int[] old = triangleVertexA;
		int count = old.length + 1;

		if (triangleVertexA.length < count) {
			triangleVertexA = new int[count];
			System.arraycopy(old, 0, triangleVertexA, 0, old.length);

			old = triangleVertexB;
			triangleVertexB = new int[count];
			System.arraycopy(old, 0, triangleVertexB, 0, old.length);

			old = triangleVertexC;
			triangleVertexC = new int[count];
			System.arraycopy(old, 0, triangleVertexC, 0, old.length);
		}

		triangleVertexA[triangleCount] = a;
		triangleVertexB[triangleCount] = b;
		triangleVertexC[triangleCount] = c;
		return triangleCount++;
	}

	public void invert() {
		for (int v = 0; v < vertexCount; v++) {
			vertexZ[v] = -vertexZ[v];
		}

		for (int t = 0; t < triangleCount; t++) {
			int a = triangleVertexA[t];
			triangleVertexA[t] = triangleVertexC[t];
			triangleVertexC[t] = a;
		}
	}

	public final void calculateNormals() {
		if (normals == null) {
			normals = new Normal[vertexCount];

			for (int n = 0; n < vertexCount; n++) {
				normals[n] = new Normal();
			}
		}

		for (int t = 0; t < triangleCount; t++) {
			int a = triangleVertexA[t];
			int b = triangleVertexB[t];
			int c = triangleVertexC[t];

			int dxAB = vertexX[b] - vertexX[a];
			int dyAB = vertexY[b] - vertexY[a];
			int dzAB = vertexZ[b] - vertexZ[a];

			int dxCA = vertexX[c] - vertexX[a];
			int dyCA = vertexY[c] - vertexY[a];
			int dzCA = vertexZ[c] - vertexZ[a];

			int lX = dyAB * dzCA - dyCA * dzAB;
			int lY = dzAB * dxCA - dzCA * dxAB;
			int lZ = dxAB * dyCA - dxCA * dyAB;

			// while it's too large, shrink it by half
			for (; (lX > 8192 || lY > 8192 || lZ > 8192 || lX < -8192 || lY < -8192 || lZ < -8192);) {
				lX >>= 1;
				lY >>= 1;
				lZ >>= 1;
			}

			int length = (int) Math.sqrt((double) (lX * lX + lY * lY + lZ * lZ));

			if (length <= 0) {
				length = 1;
			}

			// normalizing
			lX = (lX * 256) / length;
			lY = (lY * 256) / length;
			lZ = (lZ * 256) / length;

			if (triangleType == null || (triangleType[t] & 0x1) == 0) {
				Normal n = normals[a];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;

				n = normals[b];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;

				n = normals[c];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;
			}
		}
	}

	public final void applyLighting(int baseLightness, int intensity, int x, int y, int z, boolean calculateLighting) {
		int lightMagnitude = (int) Math.sqrt((double) (x * x + y * y + z * z));
		int lightIntensity = intensity * lightMagnitude >> 8;

		if (colorA == null) {
			colorA = new int[triangleCount];
			colorB = new int[triangleCount];
			colorC = new int[triangleCount];
		}

		if (normals == null) {
			normals = new Normal[vertexCount];

			for (int n = 0; n < vertexCount; n++) {
				normals[n] = new Normal();
			}
		}

		for (int t = 0; t < triangleCount; t++) {
			int a = triangleVertexA[t];
			int b = triangleVertexB[t];
			int c = triangleVertexC[t];

			int dxAB = vertexX[b] - vertexX[a];
			int dyAB = vertexY[b] - vertexY[a];
			int dzAB = vertexZ[b] - vertexZ[a];

			int dxCA = vertexX[c] - vertexX[a];
			int dyCA = vertexY[c] - vertexY[a];
			int dzCA = vertexZ[c] - vertexZ[a];

			int lX = dyAB * dzCA - dyCA * dzAB;
			int lY = dzAB * dxCA - dzCA * dxAB;
			int lZ = dxAB * dyCA - dxCA * dyAB;

			// while it's too large, shrink it by half
			for (; (lX > 8192 || lY > 8192 || lZ > 8192 || lX < -8192 || lY < -8192 || lZ < -8192);) {
				lX >>= 1;
				lY >>= 1;
				lZ >>= 1;
			}

			int length = (int) Math.sqrt((double) (lX * lX + lY * lY + lZ * lZ));

			if (length <= 0) {
				length = 1;
			}

			// normalizing
			lX = (lX * 256) / length;
			lY = (lY * 256) / length;
			lZ = (lZ * 256) / length;

			if (triangleType == null || (triangleType[t] & 0x1) == 0) {
				Normal n = normals[a];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;

				n = normals[b];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;

				n = normals[c];
				n.x += lX;
				n.y += lY;
				n.z += lZ;
				n.magnitude++;
			} else {
				int lightness = baseLightness + (x * lX + y * lY + z * lZ) / (lightIntensity + lightIntensity / 2);
				colorA[t] = adjustHSLLightness(triangleColor[t], lightness, triangleType[t]);
			}
		}

		if (calculateLighting) {
			calculateLighting(baseLightness, lightIntensity, x, y, z);
		} else {
			unmodifiedNormals = new Normal[vertexCount];

			for (int v = 0; v < vertexCount; v++) {
				Normal current = normals[v];
				Normal copy = unmodifiedNormals[v] = new Normal();
				copy.x = current.x;
				copy.y = current.y;
				copy.z = current.z;
				copy.magnitude = current.magnitude;
			}
		}

		if (calculateLighting) {
			calculateYBoundaries();
		} else {
			calculateBoundaries();
		}
	}

	public final void calculateLighting(int minIntensity, int intensity, int x, int y, int z) {
		for (int t = 0; t < triangleCount; t++) {
			int a = triangleVertexA[t];
			int b = triangleVertexB[t];
			int c = triangleVertexC[t];

			if (triangleType == null) {
				int color = triangleColor[t];

				Normal n = normals[a];
				int lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));

				colorA[t] = adjustHSLLightness(color, lightness, 0);

				n = normals[b];
				lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
				colorB[t] = adjustHSLLightness(color, lightness, 0);

				n = normals[c];
				lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
				colorC[t] = adjustHSLLightness(color, lightness, 0);
			} else if ((triangleType[t] & 0x1) == 0) {
				int color = triangleColor[t];
				int info = triangleType[t];
				int lightness;

				Normal n = normals[a];
				lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
				colorA[t] = adjustHSLLightness(color, lightness, info);

				n = normals[b];
				lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
				colorB[t] = adjustHSLLightness(color, lightness, info);

				n = normals[c];
				lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
				colorC[t] = adjustHSLLightness(color, lightness, info);
			}
		}
	}

	/**
	 * Used when normals are calculated and shading is applied.
	 */
	public final void calculateYBoundaries() {
		maxBoundY = 0;
		boundLengthXZ = 0;
		minBoundY = 0;

		for (int v = 0; v < vertexCount; v++) {
			int x = vertexX[v];
			int y = vertexY[v];
			int z = vertexZ[v];

			if (-y > maxBoundY) {
				maxBoundY = -y;
			}

			if (y > minBoundY) {
				minBoundY = y;
			}

			// couldn't think of a better name. This is squared.
			int length2 = x * x + z * z;

			if (length2 > boundLengthXZ) {
				boundLengthXZ = length2;
			}
		}

		boundLengthXZ = (int) Math.sqrt((double) boundLengthXZ);
		maxDepth = (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + maxBoundY * maxBoundY));
		minDepth = maxDepth + (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + minBoundY * minBoundY));
	}

	/**
	 * Used when normals are calculated, but no shading is applied.
	 */
	public void calculateBoundaries() {
		boundLengthXZ = 0;

		minBoundX = 999999;
		maxBoundX = -999999;

		maxBoundY = 0;
		minBoundY = 0;

		maxBoundZ = -99999;
		minBoundZ = 99999;

		for (int v = 0; v < vertexCount; v++) {
			int x = vertexX[v];
			int y = vertexY[v];
			int z = vertexZ[v];

			if (x < minBoundX) {
				minBoundX = x;
			}

			if (x > maxBoundX) {
				maxBoundX = x;
			}

			if (z < minBoundZ) {
				minBoundZ = z;
			}

			if (z > maxBoundZ) {
				maxBoundZ = z;
			}

			if (-y > maxBoundY) {
				maxBoundY = -y;
			}

			if (y > minBoundY) {
				minBoundY = y;
			}

			// couldn't think of a better name. This is squared.
			int length2 = x * x + z * z;

			if (length2 > boundLengthXZ) {
				boundLengthXZ = length2;
			}
		}

		boundLengthXZ = (int) Math.sqrt((double) boundLengthXZ);
		maxDepth = (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + maxBoundY * maxBoundY));
		minDepth = maxDepth + (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + minBoundY * minBoundY));
	}

	/**
	 * Translates all the vertices by the direction provided.
	 *
	 * @param x the x.
	 * @param y the y.
	 * @param z the z.
	 */
	public void translate(int x, int y, int z) {
		for (int v = 0; v < this.vertexCount; v++) {
			this.vertexX[v] += x;
			this.vertexY[v] += y;
			this.vertexZ[v] += z;
		}
	}

	public void draw(int pitch, int yaw, int roll, int cameraX, int cameraY, int cameraZ, int cameraPitch) {
		final int centerX = Graphics3D.center3dX;
		final int centerY = Graphics3D.center3dY;

		int pitchSine = sin[pitch];
		int pitchCosine = cos[pitch];

		int yawSine = sin[yaw];
		int yawCosine = cos[yaw];

		int rollSine = sin[roll];
		int rollCosine = cos[roll];

		int cameraPitchSine = sin[cameraPitch];
		int cameraPitchCosine = cos[cameraPitch];

		int depth = cameraY * cameraPitchSine + cameraZ * cameraPitchCosine >> 16;

		for (int v = 0; v < vertexCount; v++) {
			int x = vertexX[v];
			int y = vertexY[v];
			int z = vertexZ[v];

			if (roll != 0) {
				int z0 = y * rollSine + x * rollCosine >> 16;
				y = y * rollCosine - x * rollSine >> 16;
				x = z0;
			}

			if (pitch != 0) {
				int x0 = y * pitchCosine - z * pitchSine >> 16;
				z = y * pitchSine + z * pitchCosine >> 16;
				y = x0;
			}

			if (yaw != 0) {
				int y0 = z * yawSine + x * yawCosine >> 16;
				z = z * yawCosine - x * yawSine >> 16;
				x = y0;
			}

			x += cameraX;
			y += cameraY;
			z += cameraZ;

			int x0 = y * cameraPitchCosine - z * cameraPitchSine >> 16;
			z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
			y = x0;

			vertexDepth[v] = z - depth;
			vertexScreenX[v] = centerX + (x << 9) / z;
			vertexScreenY[v] = centerY + (y << 9) / z;
		}
		draw(0, false, false);
	}

	public static int sx1, sx2, sy1, sy2;
	public static boolean drawingModel = false;

	public void draw(int pitch, int yaw, int cameraPitchSine, int cameraPitchCosine, int cameraYawSine, int cameraYawCosine, int sceneX, int sceneY, int sceneZ, int bitset) {
		int a = sceneZ * cameraYawCosine - sceneX * cameraYawSine >> 16;
		int farZ = sceneY * cameraPitchSine + a * cameraPitchCosine >> 16;
		int c = boundLengthXZ * cameraPitchCosine >> 16;

		drawingModel = false;

		int nearZ = farZ + c;

		if (nearZ <= NEAR_Z || farZ >= FAR_Z) {
			return;
		}

		int e = sceneZ * cameraYawSine + sceneX * cameraYawCosine >> 16;

		int minX = e - boundLengthXZ << 9;

		if (minX / nearZ >= Graphics2D.halfWidth) {
			return;
		}

		sx1 = (minX / nearZ);

		int maxX = e + boundLengthXZ << 9;

		if (maxX / nearZ <= -Graphics2D.halfWidth) {
			return;
		}

		sx2 = (maxX / nearZ);

		int h = sceneY * cameraPitchCosine - a * cameraPitchSine >> 16;
		int i = boundLengthXZ * cameraPitchSine >> 16;

		int maxY = h + i << 9;

		if (maxY / nearZ <= -Graphics2D.halfHeight) {
			return;
		}

		sy2 = (maxY / nearZ);

		int k = i + (maxBoundY * cameraPitchCosine >> 16);
		int minY = h - k << 9;

		if (minY / nearZ >= Graphics2D.halfHeight) {
			return;
		}

		drawingModel = true;
		sy1 = (minY / nearZ);

		int m = c + (maxBoundY * cameraPitchSine >> 16);
		boolean project = false;

		if (farZ - m <= NEAR_Z) {
			project = true;
		}

		boolean hasInput = false;

		if (bitset > 0 && allowInput) {
			int maxZ = farZ - c;

			if (maxZ <= NEAR_Z) {
				maxZ = NEAR_Z;
			}

			if (e > 0) {
				minX /= nearZ;
				maxX /= maxZ;
			} else {
				maxX /= nearZ;
				minX /= maxZ;
			}

			if (h > 0) {
				minY /= nearZ;
				maxY /= maxZ;
			} else {
				maxY /= nearZ;
				minY /= maxZ;
			}

			int x = mouseX - Graphics3D.center3dX;
			int y = mouseY - Graphics3D.center3dY;

			if (x > minX && x < maxX && y > minY && y < maxY) {
				hasInput = true;
			}
		}

		int centerX = Graphics3D.center3dX;
		int centerY = Graphics3D.center3dY;

		int pitchSine = 0;
		int pitchCosine = 0;

		int yawSine = 0;
		int yawCosine = 0;

		if (pitch != 0) {
			pitchSine = sin[pitch];
			pitchCosine = cos[pitch];
		}

		if (yaw != 0) {
			yawSine = sin[yaw];
			yawCosine = cos[yaw];
		}

		for (int v = 0; v < vertexCount; v++) {
			int x = vertexX[v];
			int y = vertexY[v];
			int z = vertexZ[v];

			if (pitch != 0) {
				int w = (y * pitchCosine - z * pitchSine) >> 16;
				z = (y * pitchSine + z * pitchCosine) >> 16;
				y = w;
			}

			if (yaw != 0) {
				int w = (z * yawSine + x * yawCosine) >> 16;
				z = (z * yawCosine - x * yawSine) >> 16;
				x = w;
			}

			x += sceneX;
			y += sceneY;
			z += sceneZ;

			int w = z * cameraYawSine + x * cameraYawCosine >> 16;
			z = z * cameraYawCosine - x * cameraYawSine >> 16;
			x = w;

			w = y * cameraPitchCosine - z * cameraPitchSine >> 16;
			z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
			y = w;

			vertexDepth[v] = z - farZ;

			if (z >= NEAR_Z) {
				vertexScreenX[v] = centerX + (x << 9) / z;
				vertexScreenY[v] = centerY + (y << 9) / z;
			} else {
				vertexScreenX[v] = -5000;
				project = true;
			}

			if (project) {
				projectSceneX[v] = x;
				projectSceneY[v] = y;
				projectSceneZ[v] = z;
			}
		}

		try {
			draw(bitset, project, hasInput);
		} catch (Exception ex) {

		}

	}

	private void draw(int bitset, boolean projected, boolean hasInput) {
		for (int t = 0; t < triangleCount; t++) {
			if (triangleType == null || triangleType[t] != -1) {
				int a = triangleVertexA[t];
				int b = triangleVertexB[t];
				int c = triangleVertexC[t];
				int xA = vertexScreenX[a];
				int xB = vertexScreenX[b];
				int xC = vertexScreenX[c];

				if (projected && (xA == -5000 || xB == -5000 || xC == -5000)) {
					projectTriangle[t] = true;
					drawTriangle(t);
				} else {
					if (hasInput && withinTriangle(mouseX, mouseY, vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], xA, xB, xC)) {
						//hoveredBitsets[hoverCount++] = bitset;
						hasInput = false;
					}

					// ((xA - xB) * (yC - yB)) - ((yA - yB) * (xC - xB))
					int area = ((xA - xB) * (vertexScreenY[c] - vertexScreenY[b])) - ((vertexScreenY[a] - vertexScreenY[b]) * (xC - xB));

					// change to > 0 to only allow front faces, < 0 for back faces, and != 0 for both faces.
					if (area != 0) {
						projectTriangle[t] = false;
						testTriangleX[t] = xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX;
						drawTriangle(t);
					}
				}
			}
		}
	}

	private void drawTriangle(int index) {
		if (projectTriangle[index]) {
			drawProjectedTriangle(index);
		} else {
			int a = triangleVertexA[index];
			int b = triangleVertexB[index];
			int c = triangleVertexC[index];

			Graphics3D.testX = testTriangleX[index];

			if (triangleAlpha == null) {
				Graphics3D.alpha = 0;
			} else {
				Graphics3D.alpha = triangleAlpha[index];
			}

			int type;

			if (triangleType == null) {
				type = 0;
			} else {
				type = triangleType[index] & 0x3;
			}

			if (type == 0) {
				Graphics3D.fillShadedTriangleDepth(vertexScreenX[a], vertexScreenY[a], vertexDepth[a], vertexScreenX[b], vertexScreenY[b], vertexDepth[b], vertexScreenX[c], vertexScreenY[c], vertexDepth[c], colorA[index], colorB[index], colorC[index]);
			} else if (type == 1) {
				Graphics3D.fillTriangle(vertexScreenX[a], vertexScreenY[a], vertexDepth[a], vertexScreenX[b], vertexScreenY[b], vertexDepth[b], vertexScreenX[c], vertexScreenY[c], vertexDepth[c], palette[colorA[index]]);
			}
		}
	}

	private void drawProjectedTriangle(int index) {
		int cx = Graphics3D.center3dX;
		int cy = Graphics3D.center3dY;
		int n = 0;

		int vA = triangleVertexA[index];
		int vB = triangleVertexB[index];
		int vC = triangleVertexC[index];

		int zA = projectSceneZ[vA];
		int zB = projectSceneZ[vB];
		int zC = projectSceneZ[vC];

		if (zA >= NEAR_Z) {
			tmpX[n] = vertexScreenX[vA];
			tmpY[n] = vertexScreenY[vA];
			tmpZ[n] = zA;
			tmpColor[n++] = colorA[index];
		} else {
			int x = projectSceneX[vA];
			int y = projectSceneY[vA];
			int color = colorA[index];

			if (zC >= NEAR_Z) {
				int interpolant = (NEAR_Z - zA) * oneOverFixed1616[zC - zA];
				tmpX[n] = cx + ((x + (((projectSceneX[vC] - x) * interpolant) >> 16)) << 9) / NEAR_Z;
				tmpY[n] = cy + ((y + (((projectSceneY[vC] - y) * interpolant) >> 16)) << 9) / NEAR_Z;
				tmpZ[n] = zA;
				tmpColor[n++] = color + ((colorC[index] - color) * interpolant >> 16);
			}

			if (zB >= NEAR_Z) {
				int interpolant = (NEAR_Z - zA) * oneOverFixed1616[zB - zA];
				tmpX[n] = (cx + (x + ((projectSceneX[vB] - x) * interpolant >> 16) << 9) / NEAR_Z);
				tmpY[n] = (cy + (y + ((projectSceneY[vB] - y) * interpolant >> 16) << 9) / NEAR_Z);
				tmpZ[n] = zA;
				tmpColor[n++] = color + ((colorB[index] - color) * interpolant >> 16);
			}
		}

		if (zB >= NEAR_Z) {
			tmpX[n] = vertexScreenX[vB];
			tmpY[n] = vertexScreenY[vB];
			tmpZ[n] = zB;
			tmpColor[n++] = colorB[index];
		} else {
			int x = projectSceneX[vB];
			int y = projectSceneY[vB];
			int color = colorB[index];

			if (zA >= NEAR_Z) {
				int mul = (NEAR_Z - zB) * oneOverFixed1616[zA - zB];
				tmpX[n] = (cx + (x + ((projectSceneX[vA] - x) * mul >> 16) << 9) / NEAR_Z);
				tmpY[n] = (cy + (y + ((projectSceneY[vA] - y) * mul >> 16) << 9) / NEAR_Z);
				tmpZ[n] = zB;
				tmpColor[n++] = color + ((colorA[index] - color) * mul >> 16);
			}

			if (zC >= NEAR_Z) {
				int mul = (NEAR_Z - zB) * oneOverFixed1616[zC - zB];
				tmpX[n] = (cx + (x + ((projectSceneX[vC] - x) * mul >> 16) << 9) / NEAR_Z);
				tmpY[n] = (cy + (y + ((projectSceneY[vC] - y) * mul >> 16) << 9) / NEAR_Z);
				tmpZ[n] = zB;
				tmpColor[n++] = color + ((colorC[index] - color) * mul >> 16);
			}
		}

		if (zC >= NEAR_Z) {
			tmpX[n] = vertexScreenX[vC];
			tmpY[n] = vertexScreenY[vC];
			tmpZ[n] = zC;
			tmpColor[n++] = colorC[index];
		} else {
			int x = projectSceneX[vC];
			int y = projectSceneY[vC];
			int color = colorC[index];

			if (zB >= NEAR_Z) {
				int mul = (NEAR_Z - zC) * (oneOverFixed1616[zB - zC]);

				tmpX[n] = (cx + (x + (((projectSceneX[vB] - x) * mul) >> 16) << 9) / NEAR_Z);
				tmpY[n] = (cy + (y + (((projectSceneY[vB] - y) * mul) >> 16) << 9) / NEAR_Z);
				tmpZ[n] = zC;
				tmpColor[n++] = color + ((colorB[index] - color) * mul >> 16);
			}

			if (zA >= NEAR_Z) {
				int mul = (NEAR_Z - zC) * oneOverFixed1616[zA - zC];
				tmpX[n] = (cx + (x + (((projectSceneX[vA] - x) * mul) >> 16) << 9) / NEAR_Z);
				tmpY[n] = (cy + (y + (((projectSceneY[vA] - y) * mul) >> 16) << 9) / NEAR_Z);
				tmpZ[n] = zC;
				tmpColor[n++] = color + ((colorA[index] - color) * mul >> 16);
			}
		}

		int xA = tmpX[0];
		int xB = tmpX[1];
		int xC = tmpX[2];

		int yA = tmpY[0];
		int yB = tmpY[1];
		int yC = tmpY[2];

		if (((xA - xB) * (yC - yB) - (yA - yB) * (xC - xB)) > 0) {
			Graphics3D.testX = false;

			if (n == 3) {
				if (xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX) {
					Graphics3D.testX = true;
				}

				int type;

				if (triangleType == null) {
					type = 0;
				} else {
					type = triangleType[index] & 0x3;
				}

				if (type == 0) {
					Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, tmpColor[0], tmpColor[1], tmpColor[2]);
				} else if (type == 1) {
					Graphics3D.fillTriangle(xA, yA, zA, xB, yB, zB, xC, yC, zC, palette[colorA[index]]);
				}
			}

			if (n == 4) {
				if (xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX || tmpX[3] < 0 || tmpX[3] > Graphics2D.rightX) {
					Graphics3D.testX = true;
				}

				int type;

				if (triangleType == null) {
					type = 0;
				} else {
					type = triangleType[index] & 0x3;
				}

				if (type == 0) {
					Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, tmpColor[0], tmpColor[1], tmpColor[2]);
					Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xC, yC, zC, tmpX[3], tmpY[3], tmpZ[3], tmpColor[0], tmpColor[2], tmpColor[3]);
				} else if (type == 1) {
					int rgb = palette[colorA[index]];
					Graphics3D.fillTriangle(xA, yA, zA, xB, yB, zB, xC, yC, zC, rgb);
					Graphics3D.fillTriangle(xA, yA, zA, xC, yC, zC, tmpX[3], tmpY[3], tmpZ[3], rgb);
				}
			}
		}
	}

}
