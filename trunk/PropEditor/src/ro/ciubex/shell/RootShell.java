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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the Root shell class. Inspired from RootCommands - Library to access
 * root commands with Java API link:
 * https://github.com/dschuermann/root-commands
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class RootShell implements Closeable {
	boolean rootAccess;
	boolean closing;
	private String suPath;
	private Process rootProcess;
	private BufferedReader consoleOut;
	private DataOutputStream consoleIn;
	private List<Command> commands;
	private static final String token = "F*D^W@#FGF";

	/**
	 * The class constructor used to initialize the root shell.
	 */
	public RootShell() {
		commands = new ArrayList<Command>();
		scanForSU();
		initializeRootProcess();
	}

	/**
	 * Check if the shell have root access;
	 * 
	 * @return True if is enabled root access;
	 */
	public boolean hasRootAccess() {
		return rootAccess;
	}

	/**
	 * Places where could be placed the SU binnary.
	 */
	static final String[] binPlaces = { "/data/bin/", "/system/bin/",
			"/system/xbin/", "/sbin/", "/data/local/xbin/", "/data/local/bin/",
			"/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/" };

	/**
	 * Method used at initialization to find the path for SU
	 */
	private void scanForSU() {
		File su;
		for (String path : binPlaces) {
			su = new File(path + "su");
			if (su.exists()) {
				rootAccess = true;
				suPath = su.getAbsolutePath();
				break;
			}
		}
	}

	/**
	 * Initialize the root shell with all necessary to run root commands.
	 */
	private void initializeRootProcess() {
		if (rootAccess) {
			try {
				startRootProcess();
			} catch (IOException e) {
				rootAccess = false;
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method used to start the root process.
	 * 
	 * @throws IOException
	 */
	private void startRootProcess() throws IOException {
		String line;
		boolean isOk = false;
		try {
			rootProcess = Runtime.getRuntime().exec(suPath);
		} catch (IOException e) {
			rootAccess = false;
			e.printStackTrace();
		}
		if (rootAccess) {
			consoleOut = new BufferedReader(new InputStreamReader(
					rootProcess.getInputStream()));
			consoleIn = new DataOutputStream(rootProcess.getOutputStream());

			consoleIn.write("echo test_root\n".getBytes());
			consoleIn.flush();

			while (true) {
				line = consoleOut.readLine();
				if (line == null) {
					rootAccess = false;
					break;
				}
				if ("".equals(line)) {
					continue;
				}
				if ("test_root".equals(line)) {
					isOk = true;
					break;
				}
				destroyRootProcess();
			}
		}
		if (isOk) {
			new Thread(inputRunnable, "Shell Input").start();
			new Thread(outputRunnable, "Shell Output").start();
		}
	}

	private Runnable inputRunnable = new Runnable() {
		public void run() {
			try {
				writeCommands();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	private Runnable outputRunnable = new Runnable() {
		public void run() {
			try {
				readOutput();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Method used to stop the root process.
	 */
	private void destroyRootProcess() {
		doClose(consoleIn);
		doClose(consoleOut);
		try {
			rootProcess.exitValue();
		} catch (IllegalThreadStateException e) {
			rootProcess.destroy();
		}
	}

	/**
	 * Close a closeable object.
	 * 
	 * @param closeable
	 *            Object to be close.
	 */
	private void doClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method used to write a command to the root process console.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void writeCommands() throws IOException, InterruptedException {
		int commandIndex = 0;
		Command command;
		String line;
		while (true) {
			synchronized (commands) {
				while (!closing && commandIndex >= commands.size()) {
					commands.wait();
				}
			}
			if (commandIndex < commands.size()) {
				command = commands.get(commandIndex);
				line = "\necho " + token + " " + commandIndex + " $?\n";
				command.writeCommand(consoleIn);
				consoleIn.write(line.getBytes());
				consoleIn.flush();
				commandIndex++;
			} else if (closing) {
				consoleIn.write("\nexit 0\n".getBytes());
				consoleIn.flush();
				consoleIn.close();
				break;
			}
		}
	}

	/**
	 * Method used to read the command response from the root process console.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void readOutput() throws IOException, InterruptedException {
		int commandIndex = 0;
		Command command = null;
		String line, fields[];
		int pos, id;
		while (true) {
			line = consoleOut.readLine();
			if (line == null) {
				break;
			}
			if (command == null) {
				if (commandIndex >= commands.size()) {
					if (closing)
						break;
					continue;
				}
				command = commands.get(commandIndex);
			}
			System.out.println(line);
			pos = line.indexOf(token);
			if (pos >= 0) {
				line = line.substring(pos);
				fields = line.split(" ");
				id = Integer.parseInt(fields[1]);
				if (id == commandIndex) {
					command.setExitCode(Integer.parseInt(fields[2]));
					commandIndex++;
					command = null;
					continue;
				}
			}
		}
		rootProcess.waitFor();
		destroyRootProcess();
	}

	/**
	 * Add a command to the command list.
	 * 
	 * @param command
	 *            Command string to be added.
	 * @return The command object for provided string command.
	 */
	public Command addCommand(String command) {
		Command cmd = new Command(command);
		return addCommand(cmd);
	}

	/**
	 * Add a command to the command list.
	 * 
	 * @param command
	 *            Command to be added.
	 * @return The command object for provided command.
	 */
	public Command addCommand(Command command) {
		if (rootAccess && !closing) {
			synchronized (commands) {
				commands.add(command);
				commands.notifyAll();
			}
		}
		return command;
	}

	/**
	 * Close root shell
	 */
	@Override
	public void close() throws IOException {
		synchronized (commands) {
			closing = true;
			commands.notifyAll();
		}
	}

}
