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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import ro.ciubex.shell.Command;
import ro.ciubex.shell.RootShell;

/**
 * This is an utility class used to launch Unix commands from the application.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class UnixCommands {
	private List<Partition> partitions;
	private RootShell rootShell;

	public UnixCommands() {
		rootShell = new RootShell();
		populatePartitions();
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
		return rootShell.addCommand(command).waitForFinish();
	}

	/**
	 * Check partition mount flags if contain specified mount type
	 * 
	 * @param partition
	 *            The partition to check.
	 * @param mountType
	 *            The mount type for the partition to be checked.
	 * @return True, if the mounting flags contain specified mount type.
	 */
	public boolean checkPartitionMountFlags(String partition, String mountType) {
		Partition p = getPartition(partition);
		boolean result = p != null ? p.getFlags().contains(mountType) : false;
		return result;
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
		boolean result = false;
		if (p != null) {
			boolean isMountMode = p.getFlags().contains(mountType);
			if (!isMountMode) {
				String command = "mount -o " + mountType + ",remount "
						+ p.getDevice() + " " + p.getMountPoint();
				Command cmd = new Command(command,
						"busybox "+command,
						"toolbox "+command,
						"/system/bin/toolbox "+command);
				if (rootShell.addCommand(cmd).waitForFinish()) {
					result = true;
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
	
	/**
	 * Close the root shell.
	 */
	public void closeShell() {
		try {
			rootShell.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}