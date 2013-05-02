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
package ro.ciubex.propeditor.forms;

import ro.ciubex.propeditor.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class CustomEditText extends EditText {
	private Drawable imgDeleteIcon = getResources().getDrawable(
			R.drawable.delete_icon);

	public CustomEditText(Context context) {
		super(context);
		init();
	}

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * This method is used to define all customized behaviors for the current
	 * edit text control
	 */
	private void init() {

		// Set bounds of the Clear button so it will look ok
		imgDeleteIcon.setBounds(0, 0, imgDeleteIcon.getIntrinsicWidth(),
				imgDeleteIcon.getIntrinsicHeight());

		// There may be initial text in the field, so we may need to display the
		// button
		handleClearButton();

		// if the Close image is displayed and the user remove his finger from
		// the button, clear it. Otherwise do nothing
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				CustomEditText et = CustomEditText.this;

				if (et.getCompoundDrawables()[2] == null)
					return false;

				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				if (event.getX() > et.getWidth() - et.getPaddingRight()
						- imgDeleteIcon.getIntrinsicWidth()) {
					et.setText("");
					CustomEditText.this.handleClearButton();
				}
				return false;
			}
		});

		// if text changes, take care of the button
		this.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				CustomEditText.this.handleClearButton();
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});
	}

	/**
	 * This method handling the clear icon displayed at the end of editor text
	 * control
	 */
	private void handleClearButton() {
		if (this.getText().length() < 1) {
			// add the clear button
			this.setCompoundDrawables(this.getCompoundDrawables()[0],
					this.getCompoundDrawables()[1], null,
					this.getCompoundDrawables()[3]);
		} else {
			// remove clear button
			this.setCompoundDrawables(this.getCompoundDrawables()[0],
					this.getCompoundDrawables()[1], imgDeleteIcon,
					this.getCompoundDrawables()[3]);
		}
	}
}
