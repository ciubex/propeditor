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

import ro.ciubex.propeditor.PropEditorApplication;
import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.models.Constants;
import ro.ciubex.propeditor.properties.Entities;
import ro.ciubex.propeditor.tasks.DefaultAsyncTaskResult;
import ro.ciubex.propeditor.tasks.SavePropertiesTask;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Show to the user a dialog to chose the folder where should be saved the
 * properties.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class SaveToDialog extends BaseDialog implements SavePropertiesTask.Responder {
	private PropEditorApplication app;
	private EditText folderPath;
	private Button btnBrowse;
	private String fileName;
	private Entities properties;

	/**
	 * Dialog constructor.
	 * 
	 * @param context
	 *            The parent context.
	 * @param titleId
	 *            The dialog title string id.
	 * @param fileName
	 *            Name of the saved properties file.
	 * @param properties
	 *            The properties to be saved.
	 */
	public SaveToDialog(Context context, int titleId, String fileName,
			Entities properties) {
		super(context);
		app = (PropEditorApplication) application;
		this.fileName = fileName;
		this.properties = properties;
		initDialog(R.layout.save_to_layout, titleId);
		initEditTextFields();
	}

	/**
	 * Initialize the dialog.
	 * 
	 * @param layoutResID
	 *            The dialog layout ID.
	 * @param titleId
	 *            The dialog title string id.
	 */
	@Override
	protected void initDialog(int layoutResID, int titleId) {
		super.initDialog(layoutResID, titleId);
		btnBrowse = (Button) findViewById(R.id.btn_browse);
		btnBrowse.setOnClickListener(this);
	}

	/**
	 * Initialize the text fields.
	 */
	@Override
	protected void initEditTextFields() {
		folderPath = (EditText) findViewById(R.id.folder_path);
		listEditText.add(folderPath);
		folderPath.setText(getExternalStoragePath());
		super.initEditTextFields();
	}

	/**
	 * Obtain the external store path.
	 * 
	 * @return The external store path.
	 */
	@SuppressLint("SdCardPath")
	private String getExternalStoragePath() {
		String path = "/sdcard";
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		return path;
	}

	/**
	 * Called when a view has been clicked.
	 * 
	 * @param view
	 *            The view that was clicked.
	 */
	@Override
	public void onClick(View view) {
		if (btnBrowse == view) {
			showBrowseForlders();
		} else if (btnOk == view) {
			saveTo();
			super.onClick(view);
		} else {
			super.onClick(view);
		}
	}

	/**
	 * Browse for a folder to save.
	 */
	private void showBrowseForlders() {

	}

	/**
	 * Save current properties to the chosen folder.
	 */
	private void saveTo() {
		String content = folderPath.getText().toString();
		if (content != null && content.length() > 0) {
			if (!content.endsWith("/")) {
				content += "/";
			}
			new SavePropertiesTask(this, content + fileName, properties).execute();
		} else {
			app.showMessageError(parentActivity, R.string.no_folder_path);
		}
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public void startSaveProperties() {
		app.showProgressDialog(parentActivity, R.string.saving_properties);
	}

	@Override
	public void endSaveProperties(DefaultAsyncTaskResult result) {
		app.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			app.showMessageInfo(parentActivity, result.resultMessage);
		} else {
			app.showMessageError(parentActivity, result.resultMessage);
		}
	}

}
