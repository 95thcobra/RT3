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
public class Cube extends Model {

	/**
	 * Creates a 6 sided cube model with 8 vertices and 12 triangles.
	 *
	 * @param size the size of the cube.
	 */
	public Cube(int size) {
		this.triangleCount = 6 * 2; // 6 sides each with 2 triangles
		this.vertexCount = 8; // 8 corners

		this.triangleVertexA = new int[triangleCount];
		this.triangleVertexB = new int[triangleCount];
		this.triangleVertexC = new int[triangleCount];

		this.vertexX = new int[vertexCount];
		this.vertexY = new int[vertexCount];
		this.vertexZ = new int[vertexCount];

		// a b
		// |\|
		// c d
		int a = setVertex(0, 0, 0, 0);
		int b = setVertex(1, size, 0, 0);
		int c = setVertex(2, 0, 0, size);
		int d = setVertex(3, size, 0, size);

		int e = setVertex(4, 0, size, 0);
		int f = setVertex(5, size, size, 0);
		int g = setVertex(6, 0, size, size);
		int h = setVertex(7, size, size, size);

		setTriangle(0, c, a, d);
		setTriangle(1, a, b, d);

		setTriangle(2, g, e, f);
		setTriangle(3, h, f, g);

		setTriangle(4, a, e, b);
		setTriangle(5, f, e, b);

		setTriangle(6, b, f, d);
		setTriangle(7, h, f, d);

		setTriangle(8, d, h, c);
		setTriangle(9, g, h, c);

		setTriangle(10, g, c, e);
		setTriangle(11, a, c, e);
	}

}
