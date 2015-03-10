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

import com.sun.glass.events.KeyEvent;
import dane.applet.AppletShell;
import dane.image.Graphics2D;
import dane.image.ImageProducer;
import dane.input.Keyboard;
import dane.input.Mouse;
import dane.media.Model;
import java.awt.Graphics;

/**
 *
 * @author Dane
 */
public class Test2D extends AppletShell {

	public static void main(String[] args) {
		new Test2D().initialize(640, 480, 2, true);
	}

	ImageProducer viewport;
	StringBuilder sb = new StringBuilder();

	@Override
	public void startup() {
		this.viewport = new ImageProducer(this.getWidth(), this.getHeight());
	}

	@Override
	public boolean update() {
		Keyboard keyboard = this.getKeyboard();

		while (true) {
			char c = keyboard.poll();

			if (c == Character.MIN_VALUE) {
				break;
			}

			if (c == KeyEvent.VK_BACKSPACE) {
				if (sb.length() > 0) {
					sb.setLength(sb.length() - 1);
				}
				continue;
			}

			sb.append(c);
		}
		return true;
	}

	@Override
	public void draw(Graphics g, int width, int height) {
		this.viewport.bind();

		Mouse mouse = this.getMouse();
		Graphics2D.clear();
		Graphics2D.fillCircle(mouse.getX(), mouse.getY(), 32 + ((32 * Model.sin[(this.getCycle() * 16) & 0x7FF]) >> 16), 0xFFFFFF);
		Graphics2D.drawString("Something: " + sb.toString(), 8, 8, 0xFFFFFF);

		this.viewport.draw(g, 0, 0);
	}

	@Override
	public void shutdown() {
	}

}
