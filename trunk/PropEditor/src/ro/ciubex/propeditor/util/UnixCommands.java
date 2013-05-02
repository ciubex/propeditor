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
package ro.ciubex.propeditor.util;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an utility class used to launch Unix commands from the application.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class UnixCommands {

	private static final UnixCommands instance = new UnixCommands();
	private List<Partition> partitions;

	private UnixCommands() {
		populatePartitions();
	}

	public static UnixCommands getInstance() {
		return instance;
	}

	/**
	 * Scan the device partition and save to the partitions list.
	 */
	private void populatePartitions() {
		if (partitions == null) {
			partitions = new ArrayList<Partition>();
		} else {
			partitions.clear();
		}
		try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(
					"/proc/mounts"));
			String line;
			String[] fields;
			while ((line = lnr.readLine()) != null) {
				fields = line.split(" ");
				if (fields.length > 3) {
					partitions.add(new Partition(fields[0], fields[1],
							fields[2], fields[3]));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try to run an Unix command with super user privileges.
	 * 
	 * @param command
	 *            The UNIX command to be run.
	 * @return True if the command was successfully.
	 */
	public boolean runUnixCommand(String command) {
		boolean result = false;
		System.out.println("command:" + command);
		Process process = null;
		DataOutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = new DataOutputStream(process.getOutputStream());
			out.writeBytes(command + "\n");
			out.writeBytes("exit\n");
			out.flush();
			process.waitFor();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			doClose(out);
			if (process != null) {
				doClose(process.getInputStream());
				doClose(process.getErrorStream());
			}
		}
		return result;
	}

	/**
	 * A generic closing method, to avoid many try catch blocks.
	 * 
	 * @param closeable
	 *            An object to be closed.
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
	 * Mount a partition.
	 * 
	 * @param partition
	 *            The partition to be mounted.
	 * @param mountType
	 *            The mount type.
	 * @return True, if the partition was mounted.
	 */
	public boolean mountPartition(String partition, String mountType) {
		Partition p = getPartition(partition);
		boolean result = true;
		if (p != null) {
			boolean isMountMode = p.getFlags().contains(mountType);
			if (!isMountMode) {
				String command = "mount -o " + mountType + ",remount "
						+ p.getDevice() + " " + p.getMountPoint();
				if (runUnixCommand(command)) {
					result = true;
				} else if (runUnixCommand("busybox " + command)) {
					result = true;
				} else if (runUnixCommand("toolbox " + command)) {
					result = true;
				} else if (runUnixCommand("/system/bin/toolbox " + command)) {
					result = true;
				} else {
					result = false;
				}
				if (result) {
					populatePartitions();
				}
			}
		}
		return result;
	}

	/**
	 * Obtain a partition model based on the mount point name.
	 * 
	 * @param partition
	 *            The mount point name.
	 * @return The partition or null if no partition was found.
	 */
	public Partition getPartition(String partition) {
		for (Partition p : partitions) {
			if (p.getMountPoint().equals(partition)) {
				return p;
			}
		}
		return null;
	}
}
