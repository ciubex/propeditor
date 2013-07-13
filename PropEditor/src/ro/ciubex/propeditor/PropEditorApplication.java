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
package ro.ciubex.propeditor;

import java.util.Locale;

import ro.ciubex.propeditor.properties.Entities;
import ro.ciubex.propeditor.util.UnixCommands;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * This is main application class. Here are defined the progress dialog and
 * information popup.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class PropEditorApplication extends Application {
	private ProgressDialog progressDialog;
	private Entities properties;
	private String waitString;
	private Locale defaultLocale;
	private UnixCommands unixShell;

	/**
	 * This method is invoked when the application is created.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		properties = new Entities();
		waitString = getString(R.string.please_wait);
		defaultLocale = Locale.getDefault();
	}

	/**
	 * Obtain the current unix shell.
	 * 
	 * @return Unix shell.
	 */
	public UnixCommands getUnixShell() {
		if (unixShell == null) {
			unixShell = new UnixCommands();
		}
		return unixShell;
	}

	/**
	 * Method used when the application should be closed.
	 */
	public void onClose() {
		hideProgressDialog();
		if (unixShell != null) {
			unixShell.closeShell();
		}
	}

	/**
	 * This will show a progress dialog using a context and a message ID from
	 * application string resources.
	 * 
	 * @param context
	 *            The context where should be displayed the progress dialog.
	 * @param messageId
	 *            The string resource id.
	 */
	public void showProgressDialog(Context context, int messageId) {
		showProgressDialog(context, getString(messageId));
	}

	/**
	 * This will show a progress dialog using a context and the message to be
	 * showed on the progress dialog.
	 * 
	 * @param context
	 *            The context where should be displayed the progress dialog.
	 * @param message
	 *            The message displayed inside of progress dialog.
	 */
	public void showProgressDialog(Context context, String message) {
		hideProgressDialog();
		progressDialog = ProgressDialog.show(context, waitString, message);
	}

	/**
	 * Method used to hide the progress dialog.
	 */
	public void hideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		progressDialog = null;
	}

	/**
	 * Method used to show the informations.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 * @param arguments
	 *            The message arguments.
	 */
	public void showMessageInfo(Context context, int resourceMessageId,
			Object... arguments) {
		String message = getString(resourceMessageId, arguments);
		showMessageInfo(context, message);
	}

	/**
	 * Method used to show the informations.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 */
	public void showMessageInfo(Context context, int resourceMessageId) {
		String message = getString(resourceMessageId);
		showMessageInfo(context, message);
	}

	/**
	 * This method is used to show on front of a context a toast message.
	 * 
	 * @param context
	 *            The context where should be showed the message.
	 * @param message
	 *            The message used to be displayed on the information box.
	 */
	public void showMessageInfo(Context context, String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method used to show the errors.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 */
	public void showMessageError(Context context, int resourceMessageId) {
		String message = getString(resourceMessageId);
		showMessageError(context, message);
	}

	/**
	 * Method used to show the errors.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 * @param arguments
	 *            The message arguments.
	 */
	public void showMessageError(Context context, int resourceMessageId,
			Object... arguments) {
		String message = getString(resourceMessageId, arguments);
		showMessageError(context, message);
	}

	/**
	 * This method is used to show on front of a context a toast message
	 * containing applications errors.
	 * 
	 * @param context
	 *            The context where should be showed the message.
	 * @param message
	 *            The error message used to be displayed on the information box.
	 */
	public void showMessageError(Context context, String message) {
		if (message != null && message.length() > 0) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			alertDialog.setTitle(getString(R.string.error_occurred));
			alertDialog.setMessage(message);
			
			AlertDialog alert = alertDialog.create();
			alert.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			
			alert.show();
		}
	}

	/**
	 * Retrieve default application locale
	 * 
	 * @return Default locale used on application
	 */
	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Retrieve available properties.
	 * 
	 * @return Loaded properties.
	 */
	public Entities getEntities() {
		return properties;
	}

}
