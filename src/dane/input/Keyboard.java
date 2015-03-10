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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * A class for receiving and handing key events.
 *
 * @author Dane
 */
public class Keyboard {

	private final boolean[] pressedKeys = new boolean[256];
	private final boolean[] heldKeys = new boolean[256];
	private final Queue<Character> typedDeque = new ArrayDeque<>();

	/**
	 * Clears the pressed keys for the previous frame.
	 */
	public void reset() {
		Arrays.fill(this.pressedKeys, false);
	}

	/**
	 * Deactivates all held key states.
	 */
	public void releaseAllKeys() {
		Arrays.fill(this.heldKeys, false);
	}

	/**
	 * Polls a character from the typed queue.
	 *
	 * @return a character.
	 */
	public char poll() {
		Character c = typedDeque.poll();

		if (c != null) {
			return c;
		}

		return Character.MIN_VALUE;
	}

	/**
	 * Returns true whether the provided key code was pressed within the last frame.
	 *
	 * @param key the key.
	 * @return true if the key was pressed in the last frame.
	 */
	public boolean wasKeyPressed(int key) {
		return this.pressedKeys[key];
	}

	/**
	 * Returns true whether the provided key code is currently being held down.
	 *
	 * @param key the key.
	 * @return true if the key is currently held down.
	 */
	public boolean isKeyDown(int key) {
		return this.heldKeys[key];
	}

	/**
	 * Used to provide the Keyboard class a {@code KeyEvent}.
	 *
	 * @param e the key event.
	 */
	public void consumeEvent(KeyEvent e) {
		int i = e.getKeyCode();
		char c = e.getKeyChar();

		if (i < 0 || i >= Byte.MAX_VALUE) {
			return;
		}

		switch (e.getID()) {
			case KeyEvent.KEY_PRESSED: {
				// only add as a newly pressed key before the 'held' state.
				if (!this.heldKeys[i]) {
					this.pressedKeys[i] = true;
				}

				this.heldKeys[i] = true;

				if (c >= ' ' && c <= '~'
					|| c == KeyEvent.VK_ENTER
					|| c == KeyEvent.VK_BACK_SPACE
					|| (c >= KeyEvent.VK_F1 && c <= KeyEvent.VK_F12)) {
					typedDeque.add(c);
				}
				break;
			}
			case KeyEvent.KEY_RELEASED: {
				this.heldKeys[i] = false;
				break;
			}
		}
	}

}
