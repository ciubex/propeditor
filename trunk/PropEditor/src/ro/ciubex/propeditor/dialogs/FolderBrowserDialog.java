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
package ro.ciubex.propeditor.dialogs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ro.ciubex.propeditor.PropEditorApplication;
import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.tasks.BrowseFolderTask;
import ro.ciubex.propeditor.tasks.DefaultAsyncTaskResult;
import ro.ciubex.propeditor.util.AlphabeticallySort;
import ro.ciubex.propeditor.util.FolderFileFilter;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Show to the user a dialog to browse for folders.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class FolderBrowserDialog extends BaseDialog implements
		BrowseFolderTask.Responder {
	
	public interface FolderChosen {
		public void setFolder(String folder);
	}
	
	private FolderChosen folderChosen;
	private File folderPath;
	private Comparator<String> comparator;
	private FileFilter fileFilter;
	private List<String> folders;
	private TextView currentFolder;
	private ArrayAdapter<String> adapter;
	private ListView listView;

	public FolderBrowserDialog(final Context context, FolderChosen folderChosen, int titleId,
			String folderPath) {
		super(context);
		this.folderChosen = folderChosen;
		initDialog(R.layout.browse_folder_layout, titleId);
		this.folderPath = new File(folderPath);
		folders = new ArrayList<String>();
		comparator = new AlphabeticallySort();
		fileFilter = new FolderFileFilter(false);
		adapter = new ArrayAdapter<String>(parentActivity,
				R.layout.browser_item_layout, R.id.folderPathName, new ArrayList<String>());
		initInterface();
		updateCurrentFolder();
		startBrowseFolder();
	}

	/**
	 * Initialize the interface controls.
	 */
	private void initInterface() {
		currentFolder = (TextView) findViewById(R.id.current_folder);
		listView = (ListView) findViewById(R.id.folder_list);
		listView.setEmptyView(findViewById(R.id.empty_folder_list));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				setSelectedFolder(position);
			}
		});
		listView.setAdapter(adapter);
	}
	
	/**
	 * Called when a view has been clicked.
	 * 
	 * @param view
	 *            The view that was clicked.
	 */
	@Override
	public void onClick(View view) {
		if (view == btnOk) {
			folderChosen.setFolder(folderPath.getAbsolutePath());
		}
		super.onClick(view);
	}

	/**
	 * Set selected folder from the list.
	 * 
	 * @param position
	 *            Position on the list of selected folder.
	 */
	private void setSelectedFolder(int position) {
		if (folders.size() > position) {
			if (position == 0 && folderPath.getParentFile() != null && folderPath.getParentFile().exists()) {
				folderPath = folderPath.getParentFile();
				startBrowseFolder();
			} else if (position > 0) {
				folderPath = new File(folderPath + "/" + folders.get(position) + "/");
				startBrowseFolder();
			}
		}
	}

	/**
	 * Update current folder path.
	 */
	private void updateCurrentFolder() {
		currentFolder.setText(folderPath.getAbsolutePath());
	}

	/**
	 * Start an asynchronous task to scan all folders from a provided path.
	 */
	private void startBrowseFolder() {
		new BrowseFolderTask(this, fileFilter, folderPath.getAbsolutePath(), folders, comparator)
				.execute();
	}

	@Override
	public Application getApplication() {
		return application;
	}

	@Override
	public void startBrowseFolders() {
		((PropEditorApplication) application).showProgressDialog(
				parentActivity, R.string.please_wait);
	}

	@Override
	public void endBrowseFolders(DefaultAsyncTaskResult result) {
		updateCurrentFolder();
		adapter.clear();
		for (String folder : folders) {
			adapter.add(folder);
		}
		listView.setAdapter(adapter);
		((PropEditorApplication) application).hideProgressDialog();
	}
}
