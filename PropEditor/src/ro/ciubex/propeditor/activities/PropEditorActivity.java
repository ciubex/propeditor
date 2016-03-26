package ro.ciubex.propeditor.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

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
import ro.ciubex.propeditor.util.Utilities;

public class PropEditorActivity extends BaseActivity implements
        LoadPropertiesTask.Responder, SavePropertiesTask.Responder,
        RestorePropertiesTask.Responder {

    private final String BUILD_PROP = "/system/build.prop";
    private PropertiesListAdapter adapter;
    private EditText filterBox;
    private ListView propertiesList = null;

    private static final int CONFIRM_ID_DELETE = 0;
    private static final int CONFIRM_ID_RESTORE = 1;
    private static final int CONFIRM_ID_RELOAD = 2;
    private static final int CONFIRM_ID_DONATE = 3;
    private static final int CONFIRM_ID_REBOOT = 4;
    private static final int REQUEST_CODE_SETTINGS = 0;

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
     * @param charSequence The char sequence from the filter
     */
    private void applyFilter(CharSequence charSequence) {
        if (adapter != null) {
            mApplication.showProgressDialog(this, R.string.filtering);
            adapter.getFilter().filter(charSequence);
            mApplication.hideProgressDialog();
        }
    }

    /**
     * Prepare main list view with all controls
     */
    private void prepareMainListView() {
        propertiesList = (ListView) findViewById(R.id.properties_list);
        propertiesList.setEmptyView(findViewById(R.id.empty_list_view));
        propertiesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position > -1 && position < adapter.getCount()) {
                    showItemDialogMenu(position);
                }
            }
        });
        if (mApplication.getEntities().isEmpty()) {
            loadPropertiesList();
        } else {
            reloadAdapter();
        }
    }

    private void loadPropertiesList() {
        new LoadPropertiesTask(this, BUILD_PROP, mApplication.getEntities()).execute();
    }

    /**
     * This method is invoked when is selected a menu item from the option menu
     *
     * @param menuItemId The selected menu item
     */
    @Override
    protected boolean onMenuItemSelected(int menuItemId) {
        boolean processed = false;
        switch (menuItemId) {
            case R.id.menu_settings:
                processed = onMenuSettings();
                break;
            case R.id.item_exit:
                processed = true;
                onExit();
                break;
            case R.id.item_reboot:
                processed = true;
                onMenuItemReboot();
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
        mApplication.showProgressDialog(this, R.string.loading_properties);
    }

    /**
     * Method invoked when the load properties task is finished
     */
    @Override
    public void endLoadProperties(DefaultAsyncTaskResult result) {
        propertiesList.removeAllViewsInLayout();
        reloadAdapter();
        mApplication.getEntities().setModified(false);
        mApplication.hideProgressDialog();
        if (Constants.OK == result.resultId) {
            mApplication.showMessageInfo(this, result.resultMessage);
        } else {
            mApplication.showMessageError(this, result.resultMessage);
        }
    }

    /**
     * Reload adapter and properties list based on the provided properties.
     */
    public void reloadAdapter() {
        adapter = new PropertiesListAdapter(this, mApplication, mApplication.getEntities());
        propertiesList.setAdapter(adapter);
        propertiesList.setFastScrollEnabled(mApplication.getEntities().size() > 50);
    }

    /**
     * This method show the popup menu when the user do a long click on a list
     * item
     *
     * @param position The contact position where was made the long click
     */
    private void showItemDialogMenu(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Entity entity = adapter.getItem(position);
        builder.setTitle(getString(R.string.item_edit, entity.getKey()));
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
        new EditorDialog(this, mApplication.getEntities(), null, R.string.add_property)
                .show();
    }

    /**
     * Invoked when is chose the Edit from menus.
     *
     * @param position The position of selected element to be edited
     */
    private void onMenuItemEdit(int position) {
        Entity entity = adapter.getItem(position);
        new EditorDialog(this, mApplication.getEntities(), entity,
                R.string.edit_property).show();
    }

    /**
     * Invoked when is chose the Delete from menus.
     *
     * @param position The position of selected element to be deleted
     */
    private void onMenuItemDelete(int position) {
        final Entity entity = adapter.getItem(position);
        if (entity != null) {
            showConfirmationDialog(
                    R.string.remove_property,
                    mApplication.getString(R.string.remove_property_question,
                            entity.getKey()), CONFIRM_ID_DELETE, entity);
        }
    }

    /**
     * Show the settings activity (the preference activity)
     *
     * @return True, because this activity processed the menu item.
     */
    private boolean onMenuSettings() {
        Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        return true;
    }

    /**
     * This method is invoked by the each time when is accepted a confirmation
     * dialog.
     *
     * @param confirmationId The confirmation ID to identify the case.
     * @param anObject       An object send by the caller method.
     */
    @Override
    protected void onConfirmation(int confirmationId, Object anObject) {
        switch (confirmationId) {
            case CONFIRM_ID_DELETE:
                doDeleteEntity(anObject);
                break;
            case CONFIRM_ID_RESTORE:
                new RestorePropertiesTask(this, BUILD_PROP).execute();
                break;
            case CONFIRM_ID_DONATE:
                startBrowserWithPage(R.string.donate_url);
                break;
            case CONFIRM_ID_RELOAD:
                doListReload();
                break;
            case CONFIRM_ID_REBOOT:
                doReboot();
                break;
        }
    }

    /**
     * Method used to delete an entity from the list.
     *
     * @param anEntity Entity to be deleted.
     */
    private void doDeleteEntity(Object anEntity) {
        if (anEntity instanceof Entity) {
            Entity entity = (Entity) anEntity;
            mApplication.getEntities().remove(entity);
            reloadAdapter();
            mApplication.getEntities().setModified(true);
        }
    }

    /**
     * Launch the default browser with a specified URL page.
     *
     * @param urlResourceId The URL resource id.
     */
    private void startBrowserWithPage(int urlResourceId) {
        String url = mApplication.getString(urlResourceId);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException exception) {
        }
    }

    /**
     * Method invoked when is clicked the reboot menu item.
     */
    private void onMenuItemReboot() {
        if (mApplication.getUnixShell().hasRootAccess()) {
            showConfirmationDialog(R.string.reboot,
                    mApplication.getString(R.string.reboot_confirmation),
                    CONFIRM_ID_REBOOT, null);
        } else {
            mApplication.showMessageError(this, R.string.no_root_privilages);
        }
    }

    /**
     * Invoked when is chose the Reload menu item.
     */
    private void onMenuItemReload() {
        if (mApplication.getEntities().isModified()) {
            showConfirmationDialog(R.string.reload,
                    mApplication.getString(R.string.reload_confirmation),
                    CONFIRM_ID_RELOAD, null);
        } else {
            doListReload();
        }
    }

    /**
     * Method used to invoke the list reloading.
     */
    private void doListReload() {
        loadPropertiesList();
    }

    /**
     * Method used to reboot the device.
     */
    private void doReboot() {
        mApplication.showProgressDialog(this, R.string.rebooting);
        Utilities.reboot(mApplication);
    }

    /**
     * This is invoked when the user chose the restore menu item.
     */
    private void onMenuItemRestore() {
        showConfirmationDialog(R.string.restore,
                mApplication.getString(R.string.restore_confirmation),
                CONFIRM_ID_RESTORE, null);
    }

    /**
     * Invoked when the user chose the Donate menu item.
     */
    private void onMenuItemDonate() {
        showConfirmationDialog(R.string.donate_title,
                mApplication.getString(R.string.donate_message), CONFIRM_ID_DONATE, null);
    }

    /**
     * Invoked when is chose the Save menu item.
     */
    private void onMenuItemSave() {
        new SavePropertiesTask(this, BUILD_PROP, mApplication.getEntities()).execute();
    }

    /**
     * The saving process is started.
     */
    @Override
    public void startSaveProperties() {
        mApplication.showProgressDialog(this, R.string.saving_properties);
    }

    /**
     * The saving process is ended.
     */
    @Override
    public void endSaveProperties(DefaultAsyncTaskResult result) {
        mApplication.hideProgressDialog();
        if (Constants.OK == result.resultId) {
            mApplication.showMessageInfo(this, result.resultMessage);
        } else {
            mApplication.showMessageError(this, result.resultMessage);
        }
    }

    /**
     * Show the Save To dialog.
     */
    private void onMenuItemSaveTo() {
        new SaveToDialog(this, R.string.save_to, "build.prop",
                mApplication.getEntities()).show();
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
        mApplication.showProgressDialog(this, R.string.restoring);
    }

    /**
     * This method is invoked when the restore process is finished.
     */
    @Override
    public void endRestoreProperties(DefaultAsyncTaskResult result) {
        mApplication.hideProgressDialog();
        if (Constants.OK == result.resultId) {
            mApplication.showMessageInfo(this, result.resultMessage);
            loadPropertiesList();
        } else {
            mApplication.showMessageError(this, result.resultMessage);
        }
    }

    /**
     * This method is invoked when a child activity is finished and this
     * activity is showed again
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (mApplication.isMustRestart()) {
                mApplication.setMustRestart(false);
                restartActivity();
            }
        }
    }

    /**
     * Restart this activity.
     */
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
