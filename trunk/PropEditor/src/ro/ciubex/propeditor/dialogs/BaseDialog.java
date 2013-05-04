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

import java.util.ArrayList;
import java.util.List;

import ro.ciubex.propeditor.R;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * This class define default dialog behaviors.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public abstract class BaseDialog extends Dialog implements View.OnClickListener {
	protected Activity parentActivity;
	protected Application application;
	protected List<EditText> listEditText;
	protected Button btnOk, btnCancel;

	/**
	 * Default dialog constructor.
	 * 
	 * @param context
	 *            The parent context, in this case is an activity.
	 */
	public BaseDialog(Context context) {
		super(context);
		this.parentActivity = (Activity) context;
		application = parentActivity.getApplication();
		listEditText = new ArrayList<EditText>();
	}

	/**
	 * Initialize the dialog using specified layout and title.
	 * 
	 * @param layoutResID
	 *            Layout ID.
	 * @param titleId
	 *            Title string ID.
	 */
	protected void initDialog(int layoutResID, int titleId) {
		setContentView(layoutResID);
		setTitle(titleId);
		btnOk = (Button) findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(this);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(this);
	}

	/**
	 * Initialize all edit text fields to be able to hide the keyboard when the
	 * dialog is closed.
	 */
	protected void initEditTextFields() {
		for (EditText editText : listEditText) {
			editText.setRawInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_NORMAL);
		}
	}

	/**
	 * Called when a view has been clicked.
	 * 
	 * @param view
	 *            The view that was clicked.
	 */
	@Override
	public void onClick(View view) {
		hideKeyboard();
		dismiss();
	}

	/**
	 * This method is a workaround to hide the keyboard.
	 */
	protected void hideKeyboard() {
		if (!listEditText.isEmpty()) {
			InputMethodManager imm = (InputMethodManager) parentActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			for (EditText editText : listEditText) {
				imm.hideSoftInputFromWindow(editText.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

}
