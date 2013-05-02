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

import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.models.Constants;
import ro.ciubex.propeditor.util.UnixCommands;
import ro.ciubex.propeditor.util.Utilities;
import android.app.Application;
import android.os.AsyncTask;

/**
 * An asynchronous task to restore the original properties.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class RestorePropertiesTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {

	/**
	 * Responder used on loading process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startRestoreProperties();

		public void endRestoreProperties(DefaultAsyncTaskResult result);
	}

	private Responder responder;
	private DefaultAsyncTaskResult defaultResult;
	private String fileName;

	/**
	 * The constructor.
	 * 
	 * @param responder
	 *            The process responder provided to get some application info
	 * @param fileName
	 *            The full path for file name of properties
	 */
	public RestorePropertiesTask(Responder responder, String fileName) {
		this.responder = responder;
		this.fileName = fileName;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		defaultResult = new DefaultAsyncTaskResult();
		defaultResult.resultId = Constants.OK;
		restoreTheProperties();
		return defaultResult;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.startRestoreProperties();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		responder.endRestoreProperties(result);
	}

	/**
	 * Initiating the restore method.
	 */
	private void restoreTheProperties() {
		if (UnixCommands.getInstance().mountPartition("/system", "rw")) {
			restoreBackupFile();
			UnixCommands.getInstance().mountPartition("/system", "ro");
		} else {
			defaultResult.resultId = Constants.ERROR;
			defaultResult.resultMessage = responder.getApplication().getString(
					R.string.system_no_mount);
		}
	}

	/**
	 * Method used to restore the original file.
	 */
	private void restoreBackupFile() {
		String backupFileName = fileName + ".bak";
		if (Utilities.existFile(backupFileName)) {
			UnixCommands.getInstance().runUnixCommand(
					"mv -f " + backupFileName + " " + fileName);
			defaultResult.resultMessage = responder.getApplication().getString(
					R.string.file_restored, fileName);
		} else {
			defaultResult.resultId = Constants.ERROR;
			defaultResult.resultMessage = responder.getApplication().getString(
					R.string.file_not_exist, backupFileName);
		}
	}
}
