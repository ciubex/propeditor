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

import java.io.File;


/**
 * Utilities class
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Utilities {
	
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
}
