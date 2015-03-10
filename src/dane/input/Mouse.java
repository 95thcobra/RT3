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

import dane.applet.AppletShell;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * A class for receiving and handling mouse events.
 *
 * @author Dane
 */
public final class Mouse {

	private int x;
	private int y;
	private MouseButton activeButton = MouseButton.NONE;
	private MouseButton pressedButton;
	private int pressX;
	private int pressY;
	private int releaseX;
	private int releaseY;
	private int dragDeltaX;
	private int dragDeltaY;
	private int dragX;
	private int dragY;
	private int wheelRotation;
	private final AppletShell shell;

	public Mouse(AppletShell shell) {
		this.shell = shell;
	}

	/**
	 * Resets frame related variables to their default values.
	 */
	public void reset() {
		this.wheelRotation = 0;
		this.dragDeltaX = 0;
		this.dragDeltaY = 0;
		this.pressedButton = MouseButton.NONE;
	}

	/**
	 * Sets the active button to <code>MouseButton.NONE</code>.
	 *
	 * @see MouseButton#NONE
	 */
	public void release() {
		this.activeButton = MouseButton.NONE;
		this.setReleasePosition(this.x, this.y);
	}

	public void setActiveButton(MouseButton button) {
		this.activeButton = button;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setPressPosition(int x, int y) {
		this.pressX = x;
		this.pressY = y;
	}

	public void setReleasePosition(int x, int y) {
		this.releaseX = x;
		this.releaseY = y;
	}

	public void setDragPosition(int x, int y) {
		this.dragX = x;
		this.dragY = y;
	}

	public void consumeEvent(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent e = (MouseEvent) event;

			int eventX = e.getX();
			int eventY = e.getY();
			int shellScale = this.shell.getScale();

			if (shellScale != 1) {
				eventX /= shellScale;
				eventY /= shellScale;
			}

			switch (e.getID()) {
				case MouseEvent.MOUSE_MOVED: {
					this.setPosition(eventX, eventY);
					break;
				}

				case MouseEvent.MOUSE_PRESSED: {
					this.setPosition(eventX, eventY);
					this.setPressPosition(eventX, eventY);
					this.setDragPosition(eventX, eventY);
					this.activeButton = MouseButton.values()[e.getButton()];
					break;
				}

				case MouseEvent.MOUSE_RELEASED: {
					this.release();
					break;
				}

				case MouseEvent.MOUSE_DRAGGED: {
					this.setPosition(eventX, eventY);

					this.dragDeltaX = eventX - this.dragX;
					this.dragDeltaY = eventY - this.dragY;

					this.setDragPosition(eventX, eventY);
					break;
				}
			}
		} else if (event instanceof MouseWheelEvent) {
			MouseWheelEvent e = (MouseWheelEvent) event;
			this.wheelRotation = e.getWheelRotation();
		}
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public MouseButton getActiveButton() {
		return this.activeButton;
	}

	public MouseButton getPressedButton() {
		return this.pressedButton;
	}

	public int getPressX() {
		return this.pressX;
	}

	public int getPressY() {
		return this.pressY;
	}

	public int getReleaseX() {
		return this.releaseX;
	}

	public int getReleaseY() {
		return this.releaseY;
	}

	public int getDragDeltaX() {
		return this.dragDeltaX;
	}

	public int getDragDeltaY() {
		return this.dragDeltaY;
	}

	public int getDragX() {
		return this.dragX;
	}

	public int getDragY() {
		return this.dragY;
	}

	public int getWheelRotation() {
		return this.wheelRotation;
	}

}
