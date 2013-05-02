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

import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.activities.PropEditorActivity;
import ro.ciubex.propeditor.properties.Entities;
import ro.ciubex.propeditor.properties.Entity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**
 * Here is defined the property editor dialog.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class EditorDialog extends BaseDialog {

	private Entities properties;
	private Entity entity;
	private EditText keyEdit, valueEdit;

	public EditorDialog(final Context context, Entities properties,
			Entity entity, int titleId) {
		super(context);

		this.properties = properties;
		this.entity = entity;
		initDialog(R.layout.editor_layout, titleId);
		initEditTextFields();
	}

	/**
	 * This method is used to populate the edit text fields with the entity key
	 * and content.
	 */
	@Override
	protected void initEditTextFields() {
		keyEdit = (EditText) findViewById(R.id.property_key);
		valueEdit = (EditText) findViewById(R.id.property_value);
		listEditText.add(keyEdit);
		listEditText.add(valueEdit);
		if (entity != null) {
			keyEdit.setText(entity.getKey());
			valueEdit.setText(entity.getContent());
		}
		super.initEditTextFields();
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
			onSave();
		}
		super.onClick(view);
	}

	/**
	 * Save edited text to the entity.
	 */
	private void onSave() {
		String key = keyEdit.getText().toString();
		String content = valueEdit.getText().toString();
		if (key != null && key.length() > 0) {
			if (entity == null) {
				entity = new Entity(key, content);
				properties.add(entity);
				((PropEditorActivity) parentActivity).reloadAdapter();
			} else {
				entity.setKey(key);
				entity.setContent(content);
			}
		}
	}

}
