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
package dane.scene.primitive;

import dane.scene.Model;

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
		this.setTriangleCount(6 * 2);// 6 sides each with 2 triangles
		this.setVertexCount(8); // 8 corners

		int a = setVertex(0, size, -size, -size);
		int b = setVertex(1, size, size, -size);
		int c = setVertex(2, size, size, size);
		int d = setVertex(3, -size, -size, -size);

		int e = setVertex(4, -size, size, -size);
		int f = setVertex(5, -size, -size, size);
		int g = setVertex(6, -size, size, size);
		int h = setVertex(7, size, -size, size);

		setTriangle(0, a, b, c);
		setTriangle(1, d, e, b);

		setTriangle(2, f, g, e);
		setTriangle(3, h, c, g);

		setTriangle(4, b, e, g);
		setTriangle(5, d, a, h);

		setTriangle(6, h, a, c);
		setTriangle(7, a, d, b);

		setTriangle(8, d, f, e);
		setTriangle(9, f, h, g);

		setTriangle(10, c, b, g);
		setTriangle(11, f, d, h);
	}

}
