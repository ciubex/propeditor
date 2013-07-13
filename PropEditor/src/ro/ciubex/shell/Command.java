/**
 * This file is part of PropEditor application.
 * 
 * Copyright (C) 2013 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.shell;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is a regular shell command. Inspired from RootCommands - Library to
 * access root commands with Java API link:
 * https://github.com/dschuermann/root-commands
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Command {
	private String[] command;
	private int exitCode;
	private boolean finished;

	/**
	 * Build a command or a set of commands
	 * 
	 * @param command
	 */
	public Command(String... command) {
		this.command = command;
		exitCode = 0;
	}

	/**
	 * Method used to compute then shell command
	 * 
	 * @return Shell commands.
	 */
	public String getCommand() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < command.length; i++) {
			sb.append(command[i] + " 2>&1");
			sb.append('\n');
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * Write to the output current commands.
	 * 
	 * @param out
	 *            The shell output.
	 * @throws IOException
	 */
	public void writeCommand(OutputStream out) throws IOException {
		out.write(getCommand().getBytes());
	}

	/**
	 * Set an exit code to the command.
	 * 
	 * @param code
	 *            Exit code to be set.
	 */
	public void setExitCode(int code) {
		synchronized (this) {
			exitCode = code;
			finished = true;
			this.notifyAll();
		}
	}

	/**
	 * Wait to be finished the command.
	 * 
	 * @return True if the command was finished successfully.
	 */
	public boolean waitForFinish() {
		boolean result = true;
		synchronized (this) {
			while (!finished) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!finished) {
					finished = true;
					result = false;
				} else {
					result = (exitCode == 0);
				}
			}
		}
		return result;
	}

	/**
	 * Get the command exit code.
	 * 
	 * @return The command exit code.
	 */
	public int getExitCode() {
		return exitCode;
	}
}
