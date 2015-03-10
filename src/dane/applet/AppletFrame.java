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
package dane.applet;

import javax.swing.JFrame;

/**
 *
 * @author Dane
 */
public class AppletFrame extends JFrame {

	private final AppletShell shell;

	public AppletFrame(AppletShell shell) {
		this.shell = shell;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);

		this.add(shell);
		this.pack();
		this.setLocationRelativeTo(null);

		this.setVisible(true);
	}

	@Override
	public void dispose() {
		if (this.shell.canShutdown()) {
			super.dispose();
			this.shell.destroy();
		}
	}

	public AppletShell getShell() {
		return this.shell;
	}

}
