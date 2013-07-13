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

import ro.ciubex.propeditor.PropEditorApplication;
import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.models.Constants;
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
	private PropEditorApplication application;
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
		application = (PropEditorApplication) responder.getApplication();
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
		boolean shouldMountSystem = false;
		boolean continueRestore = application.getUnixShell().hasRootAccess();
		if (continueRestore) {
			shouldMountSystem = application.getUnixShell()
					.checkPartitionMountFlags(Constants.SYSTEM_PARTITION,
							Constants.READ_WRITE) != true;
			if (shouldMountSystem) {
				continueRestore = application.getUnixShell().mountPartition(
						Constants.SYSTEM_PARTITION, Constants.READ_WRITE);
			}
			if (continueRestore) {
				restoreBackupFile();
				if (shouldMountSystem) {
					application.getUnixShell().mountPartition(
							Constants.SYSTEM_PARTITION, Constants.READ_ONLY);
				}
			} else {
				defaultResult.resultId = Constants.ERROR;
				defaultResult.resultMessage = responder.getApplication().getString(
						R.string.system_no_mount);
			}
		} else {
			defaultResult.resultId = Constants.ERROR;
			defaultResult.resultMessage = application
					.getString(R.string.no_root_privilages);
		}
	}

	/**
	 * Method used to restore the original file.
	 */
	private void restoreBackupFile() {
		String backupFileName = fileName + ".bak";
		if (Utilities.existFile(backupFileName)) {
			if (application.getUnixShell().runUnixCommand(
					"mv " + backupFileName + " " + fileName)) {
				defaultResult.resultMessage = responder.getApplication()
						.getString(R.string.file_restored, fileName);
			} else {
				defaultResult.resultId = Constants.ERROR;
				defaultResult.resultMessage = responder.getApplication()
						.getString(R.string.restore_file_failed);
			}
		} else {
			defaultResult.resultId = Constants.ERROR;
			defaultResult.resultMessage = responder.getApplication().getString(
					R.string.file_not_exist, backupFileName);
		}
	}
}
