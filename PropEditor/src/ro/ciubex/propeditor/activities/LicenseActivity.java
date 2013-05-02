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
package ro.ciubex.propeditor.activities;

import java.io.IOException;
import java.io.InputStream;

import ro.ciubex.propeditor.R;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This activity is used to show the license content text
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class LicenseActivity extends BaseActivity {

	private String licenseText;
	private TextView licenseTextView;

	/**
	 * The method invoked when the activity is creating
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license_layout);

		licenseTextView = (TextView) findViewById(R.id.licenseTextView);
		// licenseTextView.setMovementMethod(new ScrollingMovementMethod());
	}

	/**
	 * This method is invoked when the activity is started
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (licenseText == null) {
			licenseText = getStreamText("LICENSE.TXT");
			licenseTextView.setText(licenseText);
		}
	}

	/**
	 * In this method is loaded the license text
	 * 
	 * @param fileName
	 *            File name with the license text
	 * @return The license text
	 */
	private String getStreamText(String fileName) {
		AssetManager assetManager = getAssets();
		StringBuilder sb = new StringBuilder();
		InputStream in = null;
		try {
			in = assetManager.open(fileName);
			if (in != null && in.available() > 0) {
				char c;
				while (in.available() > 0) {
					c = (char) in.read();
					sb.append(c);
				}
			}
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}