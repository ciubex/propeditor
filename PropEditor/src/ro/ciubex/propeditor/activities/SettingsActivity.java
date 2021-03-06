/**
 * This file is part of PropEditor application.
 *
 * Copyright (C) 2016 Claudiu Ciobotariu
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
package ro.ciubex.propeditor.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import ro.ciubex.propeditor.PropEditorApplication;
import ro.ciubex.propeditor.R;

/**
 * This is settings activity class definition.
 *
 * @author Claudiu Ciobotariu
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private PropEditorApplication mApplication;
    private Preference mAppTheme;
    private EditTextPreference mSuPath;
    private AlertDialog mAlertDialog;

    /**
     * Method called when this preference activity is created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApplication = (PropEditorApplication) getApplication();
        applyApplicationTheme();
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        prepareUI();
    }

    /**
     * Prepare activity UI.
     */
    private void prepareUI() {
        mAppTheme = findPreference(PropEditorApplication.KEY_APP_THEME);
        mSuPath = (EditTextPreference)findPreference(PropEditorApplication.KEY_SU_PATH);
    }

    /**
     * Apply application theme.
     */
    private void applyApplicationTheme() {
        this.setTheme(mApplication.getApplicationTheme());
    }

    /**
     * Prepare all informations when the activity is resuming
     */
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        prepareSummaries();
    }

    /**
     * Unregister the preference changes when the activity is on pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        mApplication.destroyAlertDialog(mAlertDialog);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PropEditorApplication.KEY_APP_THEME.equals(key)) {
            showRestartActivityMessage();
            prepareSummaries();
        } else if (PropEditorApplication.KEY_SU_PATH.equals(key)) {
            showRestartActivityMessage();
            prepareSummaries();
        }
    }

    /**
     * Show to the user an alert message.
     */
    private void showRestartActivityMessage() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.must_restart_application)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        restartApplication();
                    }
                }).show();
    }

    /**
     * Mark the application to be restarted.
     */
    private void restartApplication() {
        mApplication.setMustRestart(true);
        finish();
    }

    /**
     * Prepare preferences summaries
     */
    private void prepareSummaries() {
        String label = mApplication.getApplicationContext().getString(R.string.app_theme_title_param,
                getSelectedThemeLabel());
        mAppTheme.setTitle(label);
        label = mApplication.getSuPath();
        if (label.length() > 0) {
            label = mApplication.getApplicationContext().getString(R.string.su_path_desc_param, label);
            mSuPath.setSummary(label);
        } else {
            mSuPath.setSummary(R.string.su_path_desc);
        }
    }

    /**
     * Get the application theme label.
     *
     * @return The application theme label.
     */
    private String getSelectedThemeLabel() {
        String[] labels = mApplication.getApplicationContext().getResources().
                getStringArray(R.array.app_theme_labels);
        int themeId = mApplication.getApplicationTheme();
        if (R.style.AppThemeDark == themeId) {
            return labels[0];
        }
        return labels[1];
    }
}
