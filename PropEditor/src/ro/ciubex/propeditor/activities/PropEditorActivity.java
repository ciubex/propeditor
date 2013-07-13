package ro.ciubex.propeditor.activities;

import ro.ciubex.propeditor.R;
import ro.ciubex.propeditor.dialogs.EditorDialog;
import ro.ciubex.propeditor.dialogs.SaveToDialog;
import ro.ciubex.propeditor.list.PropertiesListAdapter;
import ro.ciubex.propeditor.models.Constants;
import ro.ciubex.propeditor.properties.Entity;
import ro.ciubex.propeditor.tasks.DefaultAsyncTaskResult;
import ro.ciubex.propeditor.tasks.LoadPropertiesTask;
import ro.ciubex.propeditor.tasks.RestorePropertiesTask;
import ro.ciubex.propeditor.tasks.SavePropertiesTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class PropEditorActivity extends BaseActivity implements
		LoadPropertiesTask.Responder, SavePropertiesTask.Responder,
		RestorePropertiesTask.Responder {

	private final String BUILD_PROP = "/system/build.prop";
	private PropertiesListAdapter adapter;
	private EditText filterBox;
	private ListView propertiesList = null;

	private final int CONFIRM_ID_DELETE = 0;
	private final int CONFIRM_ID_RESTORE = 1;
	private final int CONFIRM_ID_DONATE = 2;

	/**
	 * The method invoked when the activity is creating
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prop_editor);
		setMenuId(R.menu.activity_prop_editor);
		prepareFilterBox();
		prepareMainListView();
	}

	/**
	 * Prepare filter editor box
	 */
	private void prepareFilterBox() {
		filterBox = (EditText) findViewById(R.id.filter_box);
		filterBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				applyFilter(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * This method is invoked when the filter is edited
	 * 
	 * @param charSequence
	 *            The char sequence from the filter
	 */
	private void applyFilter(CharSequence charSequence) {
		if (adapter != null) {
			app.showProgressDialog(this, R.string.filtering);
			adapter.getFilter().filter(charSequence);
			app.hideProgressDialog();
		}
	}

	/**
	 * Prepare main list view with all controls
	 */
	private void prepareMainListView() {
		propertiesList = (ListView) findViewById(R.id.properties_list);
		propertiesList.setEmptyView(findViewById(R.id.empty_list_view));
		propertiesList
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View v, int position, long id) {
						boolean isProcessed = false;
						if (position > -1 && position < adapter.getCount()) {
							isProcessed = true;
							showItemDialogMenu(position);
						}
						return isProcessed;
					}
				});
		if (app.getEntities().isEmpty()) {
			loadPropertiesList();
		} else {
			reloadAdapter();
		}
	}

	private void loadPropertiesList() {
		new LoadPropertiesTask(this, BUILD_PROP, app.getEntities()).execute();
	}

	/**
	 * This method is invoked when is selected a menu item from the option menu
	 * 
	 * @param menuItemId
	 *            The selected menu item
	 */
	@Override
	protected boolean onMenuItemSelected(int menuItemId) {
		boolean processed = false;
		switch (menuItemId) {
		case R.id.item_exit:
			processed = true;
			onExit();
			break;
		case R.id.item_add:
			processed = true;
			onMenuItemAdd();
			break;
		case R.id.item_reload:
			processed = true;
			onMenuItemReload();
			break;
		case R.id.item_restore:
			processed = true;
			onMenuItemRestore();
			break;
		case R.id.item_donate:
			processed = true;
			onMenuItemDonate();
			break;
		case R.id.item_save:
			processed = true;
			onMenuItemSave();
			break;
		case R.id.item_save_to:
			processed = true;
			onMenuItemSaveTo();
			break;
		case R.id.item_about:
			processed = true;
			onMenuItemAbout();
			break;
		}
		return processed;
	}

	/**
	 * Method invoked when the load properties task is started
	 */
	@Override
	public void startLoadProperties() {
		app.showProgressDialog(this, R.string.loading_properties);
	}

	/**
	 * Method invoked when the load properties task is finished
	 */
	@Override
	public void endLoadProperties(DefaultAsyncTaskResult result) {
		propertiesList.removeAllViewsInLayout();
		reloadAdapter();
		app.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			app.showMessageInfo(this, result.resultMessage);
		} else {
			app.showMessageError(this, result.resultMessage);
		}
	}

	/**
	 * Reload adapter and properties list based on the provided properties.
	 */
	public void reloadAdapter() {
		adapter = new PropertiesListAdapter(app, app, app.getEntities());
		propertiesList.setAdapter(adapter);
		propertiesList.setFastScrollEnabled(app.getEntities().size() > 50);
	}

	/**
	 * This method show the popup menu when the user do a long click on a list
	 * item
	 * 
	 * @param contactPosition
	 *            The contact position where was made the long click
	 */
	private void showItemDialogMenu(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.item_edit);
		builder.setItems(R.array.menu_list,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							onMenuItemEdit(position);
							break;
						case 1:
							onMenuItemDelete(position);
							break;
						case 2:
							onMenuItemAdd();
							break;
						}
					}
				});
		builder.create().show();
	}

	/**
	 * Invoked when is chose the Add from menus. Should open the Editor to add a
	 * new property.
	 */
	private void onMenuItemAdd() {
		new EditorDialog(this, app.getEntities(), null, R.string.add_property)
				.show();
	}

	/**
	 * Invoked when is chose the Edit from menus.
	 * 
	 * @param position
	 *            The position of selected element to be edited
	 */
	private void onMenuItemEdit(int position) {
		Entity entity = adapter.getItem(position);
		new EditorDialog(this, app.getEntities(), entity,
				R.string.edit_property).show();
	}

	/**
	 * Invoked when is chose the Delete from menus.
	 * 
	 * @param position
	 *            The position of selected element to be deleted
	 */
	private void onMenuItemDelete(int position) {
		final Entity entity = adapter.getItem(position);
		if (entity != null) {
			showConfirmationDialog(
					R.string.remove_property,
					app.getString(R.string.remove_property_question,
							entity.getKey()), CONFIRM_ID_DELETE, entity);
		}
	}

	/**
	 * This method is invoked by the each time when is accepted a confirmation
	 * dialog.
	 * 
	 * @param confirmationId
	 *            The confirmation ID to identify the case.
	 * @param anObject
	 *            An object send by the caller method.
	 */
	@Override
	protected void onConfirmation(int confirmationId, Object anObject) {
		if (CONFIRM_ID_DELETE == confirmationId && anObject != null) {
			Entity entity = (Entity) anObject;
			app.getEntities().remove(entity);
			reloadAdapter();
		} else if (CONFIRM_ID_RESTORE == confirmationId) {
			new RestorePropertiesTask(this, BUILD_PROP).execute();
		} else if (CONFIRM_ID_DONATE == confirmationId) {
			startBrowserWithPage(R.string.donate_url);
		}
	}

	/**
	 * Launch the default browser with a specified URL page.
	 * 
	 * @param urlResourceId
	 *            The URL resource id.
	 */
	private void startBrowserWithPage(int urlResourceId) {
		String url = app.getString(urlResourceId);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	/**
	 * Invoked when is chose the Reload menu item.
	 */
	private void onMenuItemReload() {
		loadPropertiesList();
	}

	private void onMenuItemRestore() {
		showConfirmationDialog(R.string.restore,
				app.getString(R.string.restore_confirmation),
				CONFIRM_ID_RESTORE, null);
	}

	/**
	 * Invoked when the user chose the Donate menu item.
	 */
	private void onMenuItemDonate() {
		showConfirmationDialog(R.string.donate_title,
				app.getString(R.string.donate_message), CONFIRM_ID_DONATE, null);
	}

	/**
	 * Invoked when is chose the Save menu item.
	 */
	private void onMenuItemSave() {
		new SavePropertiesTask(this, BUILD_PROP, app.getEntities()).execute();
	}

	/**
	 * The saving process is started.
	 */
	@Override
	public void startSaveProperties() {
		app.showProgressDialog(this, R.string.saving_properties);
	}

	/**
	 * The saving process is ended.
	 */
	@Override
	public void endSaveProperties(DefaultAsyncTaskResult result) {
		app.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			app.showMessageInfo(this, result.resultMessage);
		} else {
			app.showMessageError(this, result.resultMessage);
		}
	}

	/**
	 * Show the Save To dialog.
	 */
	private void onMenuItemSaveTo() {
		new SaveToDialog(this, R.string.save_to, "build.prop",
				app.getEntities()).show();
	}

	/**
	 * Show the about activity
	 */
	private void onMenuItemAbout() {
		Intent intent = new Intent(getBaseContext(), AboutActivity.class);
		startActivityForResult(intent, 1);
	}

	/**
	 * This method is invoked when is started the restore process.
	 */
	@Override
	public void startRestoreProperties() {
		app.showProgressDialog(this, R.string.restoring);
	}

	/**
	 * This method is invoked when the restore process is finished.
	 */
	@Override
	public void endRestoreProperties(DefaultAsyncTaskResult result) {
		app.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			app.showMessageInfo(this, result.resultMessage);
			loadPropertiesList();
		} else {
			app.showMessageError(this, result.resultMessage);
		}
	}
}
