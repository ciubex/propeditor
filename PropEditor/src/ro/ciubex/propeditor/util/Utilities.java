/**
 * This file is part of PropEditor application.
 * 
 * Copyright (C) 2016 Claudiu Ciobotariu
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

import android.util.Log;

import java.io.Closeable;
import java.io.File;

import ro.ciubex.propeditor.PropEditorApplication;

/**
 * Utilities class
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Utilities {
	private static final String TAG = Utilities.class.getName();

	/**
	 * Check if the file exist.
	 * 
	 * @param fileName
	 *            The name of the file to check.
	 * @return True if the file exist.
	 */
	public static boolean existFile(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	/**
	 * Check if two strings are equals.
	 * 
	 * @param string1
	 *            First string to check.
	 * @param string2
	 *            Second string to check.
	 * @return True if the both strings are equals.
	 */
	public static boolean stringEquals(String string1, String string2) {
		boolean result = string1 != null && string2 != null;
		if (result) {
			result = string1.equals(string2);
		}
		return result;
	}

	/**
	 * Method which invoke a method to reboot the phone.
	 * 
	 * @param app
	 *            The application for the "old way" reboot.
	 */
	public static void reboot(PropEditorApplication app) {
		String[] cmds = { "reboot now", "reboot recovery",
				"toolbox reboot recovery", "busybox reboot recovery" };
		for (String cmd : cmds) {
			if (app.getUnixShell().runUnixCommand(cmd))
				break;
		}
	}



	/**
	 * Close a closeable object.
	 *
	 * @param closeable Object to be close.
	 */
	public static void doClose(Object closeable) {
		if (closeable instanceof Closeable) {
			try {
				((Closeable) closeable).close();
			} catch (RuntimeException rethrown) {
				throw rethrown;
			} catch (Exception e) {
				Log.e(TAG, "doClose Exception: " + e.getMessage(), e);
			}
		}
	}
}
