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
import java.io.FileFilter;

/**
 * Define a folder file filter.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class FolderFileFilter implements FileFilter {
	private boolean showHidden;

	public FolderFileFilter(boolean showHidden) {
		this.showHidden = showHidden;
	}
	
	@Override
	public boolean accept(File pathname) {
		boolean show = pathname.exists() && pathname.isDirectory();
		if (show) {
			if (pathname.isHidden()) {
				show = showHidden;
			}
		}
		return show;
	}

}
