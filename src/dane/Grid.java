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

	public int columns;
	public int rows;
	public int size;

	public Grid(int size, int columns, int rows) {
		int tileWidth = size / columns;
		int tileHeight = size / rows;

		this.size = size;
		this.columns = columns;
		this.rows = rows;

		this.triangleCount = (columns * rows) * 2;
		this.vertexCount = (columns + 1) * (rows + 1);

		this.triangleVertexA = new int[triangleCount];
		this.triangleVertexB = new int[triangleCount];
		this.triangleVertexC = new int[triangleCount];

		this.vertexX = new int[vertexCount];
		this.vertexY = new int[vertexCount];
		this.vertexZ = new int[vertexCount];

		int i = 0;
		for (int z = 0; z < rows + 1; z++) {
			for (int x = 0; x < columns + 1; x++) {
				setVertex(
					i,
					x * tileWidth,
					0,
					z * tileHeight
				);
				i++;
			}
		}

		int vertexColumns = columns + 1;
		int vertex = 0;

		for (i = 0; i < this.triangleCount; i += 2) {
			if (vertex % vertexColumns == columns) {
				vertex++;
			}

			int a = vertex;
			int b = vertex + 1;
			int c = vertex + columns + 1;
			int d = vertex + columns + 2;

			setTriangle(i, a, b, c);
			setTriangle(i + 1, c, b, d);
			vertex++;
		}
	}

}
