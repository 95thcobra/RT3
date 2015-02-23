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
package dane.timer;

/**
 *
 * @author Dane
 */
public final class NanoTimer extends Timer {

	private static void sleep0(long ms) {
		try {
			if ((ms % 10) == 1) {
				sleep1(ms - 1);
				sleep1(1);
			} else {
				sleep1(ms);
			}
		} catch (Exception e) {
			//ignore
		}
	}

	private static void sleep1(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			//ignore
		}
	}

	private long lastTime;

	public NanoTimer() {
		this.lastTime = System.nanoTime();
	}

	@Override
	public int getCycleCount(int deltime, int mindel) {
		int cycles;
		long delay = (long) mindel * 1_000_000;
		long delta = this.lastTime - System.nanoTime();

		if (delay > delta) {
			delta = delay;
		}

		cycles = 0;
		sleep0(delta / 1_000_000);

		long time;
		for (time = System.nanoTime(); cycles < 10 && (~cycles > -2 || time > lastTime); lastTime += (long) deltime * 0xf4240L) {
			cycles++;
		}

		if (time > this.lastTime) {
			this.lastTime = time;
		}

		return cycles;
	}

}
