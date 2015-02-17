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
public class Grid extends Model {

	public int xSubdivisions;
	public int ySubdivisions;
	public int size;

	public Grid(int size, int xSubdivisions, int ySubdivisions) {
		int tileSizeX = size / xSubdivisions;
		int tileSizeY = size / ySubdivisions;

		this.size = size;
		this.xSubdivisions = xSubdivisions;
		this.ySubdivisions = ySubdivisions;

		this.triangleVertexA = new int[triangleCount];
		this.triangleVertexB = new int[triangleCount];
		this.triangleVertexB = new int[triangleCount];

		this.vertexX = new int[triangleCount];
		this.vertexY = new int[triangleCount];
		this.vertexZ = new int[triangleCount];

		for (int x = 0; x < xSubdivisions; x++) {
			for (int y = 0; y < ySubdivisions; y++) {
				int x1 = x * tileSizeX;
				int z1 = y * tileSizeY;

				int x2 = x1 + tileSizeX;
				int z2 = z1 + tileSizeY;

				int a = getVertex(x1, 0, z1);
				int b = getVertex(x2, 0, z1);
				int c = getVertex(x1, 0, z2);
				int d = getVertex(x2, 0, z2);

				addTriangle(a, b, c);
				addTriangle(c, b, d);
			}
		}
	}

}
