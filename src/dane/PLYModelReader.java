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

import java.io.*;

/**
 *
 * @author Dane
 */
public class PLYModelReader extends ModelReader {

	PLYModelReader() {

	}

	@Override
	public Model read(InputStream in) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));

		if (!"ply".equals(r.readLine())) {
			throw new IOException("invalid file format");
		}

		boolean validHeader = false;
		String s;

		int vertexCount = 0;
		int triangleCount = 0;

		READ_HEADER:
		{
			while ((s = r.readLine()) != null) {
				String[] tokens = s.split("[ \r\n]+");

				switch (tokens[0]) {
					case "end_header": {
						validHeader = true;
						break READ_HEADER;
					}

					case "format": {
						if (!"ascii".equalsIgnoreCase(tokens[1])) {
							break READ_HEADER;
						}
						break;
					}

					case "element": {
						if ("vertex".equalsIgnoreCase(tokens[1])) {
							vertexCount = Integer.parseInt(tokens[2]);
						} else if ("face".equalsIgnoreCase(tokens[1])) {
							triangleCount = Integer.parseInt(tokens[2]);
						} else {
							break READ_HEADER;
						}
						break;
					}

					case "property": // TODO: use property properly ;)
					case "comment": {
						break;
					}
				}
			}
		}

		if (!validHeader) {
			throw new IOException("invalid header");
		}

		if (vertexCount == 0 || triangleCount == 0) {
			throw new IOException("blank model");
		}

		Model m = new Model();
		m.setVertexCount(vertexCount);
		m.setTriangleCount(triangleCount);

		for (int v = 0; v < vertexCount; v++) {
			String[] tokens = r.readLine().split("[ \r\n]+");

			m.setVertex(v,
				(int) (Float.parseFloat(tokens[0])),
				(int) (Float.parseFloat(tokens[1])),
				(int) (Float.parseFloat(tokens[2]))
			);
		}

		for (int t = 0; t < triangleCount; t++) {
			String[] tokens = r.readLine().split("[ \r\n]+");

			m.setTriangle(t,
				Integer.parseInt(tokens[1]),
				Integer.parseInt(tokens[2]),
				Integer.parseInt(tokens[3])
			);
		}

		return m;
	}

}
