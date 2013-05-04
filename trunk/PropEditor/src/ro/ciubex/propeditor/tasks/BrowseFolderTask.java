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
import java.io.FileFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ro.ciubex.propeditor.models.Constants;
import android.app.Application;
import android.os.AsyncTask;

/**
 * An asynchronous task used to browse for folders.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BrowseFolderTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {

	/**
	 * Responder used on browse folder process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startBrowseFolders();

		public void endBrowseFolders(DefaultAsyncTaskResult result);
	}

	private DefaultAsyncTaskResult defaultResult;
	private Responder responder;
	private String folderPath;
	private List<String> folders;
	private Comparator<String> comparator;
	private FileFilter fileFilter;

	/**
	 * Constructor of this asynchronous task.
	 * 
	 * @param responder
	 *            The task responder.
	 * @param fileFilter
	 *            The file filter used on scan for folders.
	 * @param folderPath
	 *            The folder to be browse.
	 * @param folders
	 *            A string list with all folders from path folderPath.
	 * @param comparator
	 *            A string comparator used to sort folder list.
	 */
	public BrowseFolderTask(Responder responder, FileFilter fileFilter,
			String folderPath, List<String> folders,
			Comparator<String> comparator) {
		this.responder = responder;
		this.fileFilter = fileFilter;
		this.folderPath = folderPath;
		this.folders = folders;
		this.comparator = comparator;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		defaultResult = new DefaultAsyncTaskResult();
		defaultResult.resultId = Constants.OK;
		defaultResult.resultMessage = folderPath;
		scanForFolders();
		return defaultResult;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.startBrowseFolders();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		responder.endBrowseFolders(result);
	}

	/**
	 * Scan for folders.
	 */
	private void scanForFolders() {
		File file = new File(folderPath);
		if (!folders.isEmpty()) {
			folders.clear();
		}
		folders.add("..");
		if (file.exists()) {
			File[] files = file.listFiles(fileFilter);
			if (files != null && files.length > 0) {
				for (File f : files) {
					folders.add(f.getName());
				}
			}
		}
		// apply sort comparator
		if (folders.size() > 2) {
			Collections.sort(folders, comparator);
		}
	}
}
