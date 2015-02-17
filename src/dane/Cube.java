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

	public Cube(int size) {
		int a = addVertex(0, 0, 0);
		int b = addVertex(size, 0, 0);
		int c = addVertex(0, 0, size);
		int d = addVertex(size, 0, size);

		int e = addVertex(0, size, 0);
		int f = addVertex(size, size, 0);
		int g = addVertex(0, size, size);
		int h = addVertex(size, size, size);

		addTriangle(a, b, c);
		addTriangle(d, b, c);

		addTriangle(e, f, g);
		addTriangle(h, f, g);

		addTriangle(a, e, b);
		addTriangle(f, e, b);

		addTriangle(b, f, d);
		addTriangle(h, f, d);

		addTriangle(d, h, c);
		addTriangle(g, h, c);

		addTriangle(g, c, e);
		addTriangle(a, c, e);
	}

}
