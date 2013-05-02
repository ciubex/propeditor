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
package ro.ciubex.propeditor.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.models.Constants;
import ro.ciubex.propeditor.properties.Entities;
import android.app.Application;
import android.os.AsyncTask;

/**
 * An asynchronous task to load the properties.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class LoadPropertiesTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {

	/**
	 * Responder used on loading process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startLoadProperties();

		public void endLoadProperties(DefaultAsyncTaskResult result);
	}

	private Responder responder;
	private DefaultAsyncTaskResult defaultResult;
	private String fileName;
	private Entities properties;

	/**
	 * Constructor of this async task
	 * 
	 * @param responder
	 *            The process responder provided to get some application info
	 * @param fileName
	 *            The full path for file name of properties
	 * @param properties
	 *            The propertied to be loaded
	 */
	public LoadPropertiesTask(Responder responder, String fileName,
			Entities properties) {
		this.responder = responder;
		this.fileName = fileName;
		this.properties = properties;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		defaultResult = new DefaultAsyncTaskResult();
		defaultResult.resultId = Constants.OK;
		loadTheProperties();
		return defaultResult;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.startLoadProperties();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		responder.endLoadProperties(result);
	}

	private void loadTheProperties() {
		File f = new File(fileName);
		if (f.exists()) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(f);
				properties.load(inputStream);
				defaultResult.resultMessage = responder.getApplication()
						.getString(R.string.properties_loaded,
								properties.size());
			} catch (FileNotFoundException e) {
				defaultResult.resultId = Constants.ERROR;
				defaultResult.resultMessage = responder.getApplication()
						.getString(R.string.loading_exception, fileName,
								"FileNotFoundException", e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				defaultResult.resultId = Constants.ERROR;
				defaultResult.resultMessage = responder.getApplication()
						.getString(R.string.loading_exception, fileName,
								"IOException", e.getMessage());
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						/* ignored */
					}
				}
			}
		} else {
			defaultResult.resultId = Constants.ERROR;
			defaultResult.resultMessage = responder.getApplication().getString(
					R.string.file_not_exist, fileName);
		}
	}

}
