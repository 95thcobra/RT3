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
package dane.input;

import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 *
 * @author Dane
 */
public class Keyboard {

	private final boolean[] pressedKeys = new boolean[256];
	private final boolean[] heldKeys = new boolean[256];

	/**
	 * Clears the pressed keys for the previous frame.
	 */
	public void reset() {
		Arrays.fill(this.pressedKeys, false);
	}

	public void releaseAllKeys() {
		Arrays.fill(this.heldKeys, false);
	}

	public boolean wasKeyPressed(int key) {
		return pressedKeys[key];
	}

	public boolean isKeyDown(int key) {
		return heldKeys[key];
	}

	/**
	 * Used to provide the Keyboard class a {@code KeyEvent}.
	 *
	 * @param e the key event.
	 */
	public void consumeEvent(KeyEvent e) {
		int code = e.getKeyCode();

		if (code < 0 || code >= Byte.MAX_VALUE) {
			return;
		}

		switch (e.getID()) {
			case KeyEvent.KEY_PRESSED: {
				// only add as a newly pressed key before the 'held' state.
				if (!heldKeys[code]) {
					pressedKeys[code] = true;
				}

				heldKeys[code] = true;
				break;
			}
			case KeyEvent.KEY_RELEASED: {
				heldKeys[code] = false;
				break;
			}
		}
	}

}
