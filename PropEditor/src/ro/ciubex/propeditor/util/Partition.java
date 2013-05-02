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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class preserve all partition informations: device, mount point, type and
 * flags.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Partition {
	private String mDevice;
	private String mMountPoint;
	private String mType;
	private Set<String> mFlags;

	public Partition(String device, String mountPoint, String type, String flags) {
		this.mDevice = device;
		this.mMountPoint = mountPoint;
		this.mType = type;
		mFlags = new LinkedHashSet<String>(Arrays.asList(flags.split(",")));
	}

	public String getDevice() {
		return mDevice;
	}

	public String getMountPoint() {
		return mMountPoint;
	}

	public String getType() {
		return mType;
	}

	public Set<String> getFlags() {
		return mFlags;
	}
}
