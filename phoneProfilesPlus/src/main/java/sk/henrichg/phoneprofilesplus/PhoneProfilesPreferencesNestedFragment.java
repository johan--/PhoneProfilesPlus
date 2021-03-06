package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class PhoneProfilesPreferencesNestedFragment extends PreferenceFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    protected PreferenceManager prefMng;
    protected SharedPreferences preferences;

    public static MobileCellsRegistrationDialogPreference.MobileCellsRegistrationBroadcastReceiver mobileCellsRegistrationBroadcastReceiver;

    private static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    private static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    private static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    private static final int RESULT_WIFI_BLUETOOTH_LOCATION_SETTINGS = 1992;
    private static final String PREF_POWER_SAVE_MODE_SETTINGS = "applicationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    private static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    private static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    private static final int RESULT_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = 1995;
    private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    private static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    private static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    private static final String PREF_AUTOSTART_PERMISSION_MIUI = "applicationAutoStartMIUI";
    private static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
        Bundle bundle = getArguments();
        if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
            toolbar.setSubtitle(getString(R.string.title_activity_phone_profiles_preferences));
        else
            toolbar.setSubtitle(null);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        PreferenceScreen systemCategory = (PreferenceScreen) findPreference("categorySystem");
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null)
                systemCategory.removePreference(preference);
        }
        else {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);
        }

        /*if (Build.VERSION.SDK_INT >= 24) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
            if (preference != null)
                preferenceCategory.removePreference(preference);
            preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCALE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                            startActivityForResult(intent, RESULT_LOCALE_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
        }
        else {*/
            PreferenceScreen _preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference _preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (_preference != null)
                _preferenceCategory.removePreference(_preference);
        //}
        if (Build.VERSION.SDK_INT >= 21) {
            //PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            //Preference preference = findPreference(PREF_POWER_SAVE_MODE_INTERNAL);
            //if (preference != null)
            //    preferenceCategory.removePreference(preference);

            Preference preference = prefMng.findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressLint("InlinedApi")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        boolean activityExists;
                        Intent intent;
                        if (Build.VERSION.SDK_INT == 21) {
                            intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                            activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                        } else {
                            activityExists = GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, getActivity().getApplicationContext());
                            intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                        }
                        if (activityExists) {
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            try {
                                startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                            } catch (Exception e) {
                                if (Build.VERSION.SDK_INT > 21) {
                                    intent = new Intent();
                                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                                    activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                                    if (activityExists) {
                                        try {
                                            startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }// else
                                //    e.printStackTrace();
                            }
                        }
                        if (!activityExists) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
        } else {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            Preference preference = findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            Preference preference = prefMng.findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:sk.henrichg.phoneprofilesplus"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
            preference = prefMng.findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }
            preference = prefMng.findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext())) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            return false;
                        }
                    });
                } else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }
            }
            preference = prefMng.findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //if (android.os.Build.VERSION.SDK_INT >= 25) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
                            }
                            return false;
                        }
                    });
                /*} else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }*/
            }

            //int locationMode = Settings.Secure.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            /*
            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean isScanAlwaysAvailable = WifiScanJob.wifi.isScanAlwaysAvailable();

            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "locationMode="+locationMode);
            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "isScanAlwaysAvailable="+isScanAlwaysAvailable);

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {*/
                preference = prefMng.findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
                if (preference != null) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_LOCATION_SETTINGS);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
                            }
                            return false;
                        }
                    });
                }
            /*}
            else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("wifiScanningCategory");
                preference = findPreference(PREF_WIFI_SCANNING_SYSTEM_SETTINGS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }*/

            preference = prefMng.findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_IP_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                preference = prefMng.findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                if (preference != null) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_LOCATION_SETTINGS);
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
                            }
                            return false;
                        }
                    });
                }
            /*}
            else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanninCategory");
                preference = findPreference(PREF_BLUETOOTH_SCANNING_SYSTEM_SETTINGS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }*/

            preference = prefMng.findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            }

        }
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);

            preferenceCategory = (PreferenceScreen) findPreference("wifiScanningCategory");
            Preference preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);

            preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanninCategory");
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);

            preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        if (!WifiBluetoothScanner.bluetoothLESupported(getActivity().getApplicationContext())) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanninCategory");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        Preference preference = prefMng.findPreference(PREF_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                    return false;
                }
            });
        }
        if (android.os.Build.VERSION.SDK_INT < 21) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryNotifications");
            preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        if ((PPApplication.sLook == null) || (!PPApplication.sLookCocktailPanelEnabled)) {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySamsungEdgePanel");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }
        preference = prefMng.findPreference(PREF_AUTOSTART_PERMISSION_MIUI);
        if (preference != null) {
            String manufacturer = "xiaomi";
            if (manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            //this will open auto start screen where user can enable permission for your app
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                        }catch (Exception e) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            dialogBuilder.show();
                        }
                        return false;
                    }
                });
            } else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryApplicationStart");
                preferenceCategory.removePreference(preference);
            }
        }
        long jobMinInterval = TimeUnit.MILLISECONDS.toMinutes(JobRequest.MIN_INTERVAL);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                Long.toString(jobMinInterval) + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = prefMng.findPreference("applicationEventLocationUpdateIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventWifiScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventBluetoothScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventOrientationScanIntervalInfo");
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
    }

    /*
    private void setTitleStyle(Preference preference, boolean bold, boolean underline)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (bold || underline)
        {
            if (bold)
                sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (underline)
                sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setTitle(sbt);
        }
        else
        {
            preference.setTitle(sbt);
        }
    }
    */

    void setSummary(String key)
    {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        PreferenceScreen preferenceCategoryNotifications = (PreferenceScreen) findPreference("categoryNotifications");
        boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
        boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        if (!(notificationStatusBar && notificationStatusBarPermanent)) {
            GlobalGUIRoutines.setPreferenceTitleStyle(preferenceCategoryNotifications, true, false, true, false);
            if (preferenceCategoryNotifications != null)
                preferenceCategoryNotifications.setSummary(getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                                                            getString(R.string.phone_profiles_pref_notificationStatusBarRequired));
        }
        else {
            GlobalGUIRoutines.setPreferenceTitleStyle(preferenceCategoryNotifications, false, false, false, false);
            if (preferenceCategoryNotifications != null)
                preferenceCategoryNotifications.setSummary(R.string.empty_string);
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, !notificationStatusBar, false, !notificationStatusBar, false);
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, !notificationStatusBarPermanent, false, !notificationStatusBarPermanent, false);
        }

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                if (_preference != null)
                    _preference.setEnabled(show);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof TwoStatePreference) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE))
        {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference)preference;
            profilePreference.setSummary(lProfileId);

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
        }
        else
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            // added support for "%" in list items
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null)
            {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            }
            else
                preference.setSummary(null);

            //if (key.equals(PPApplication.PREF_APPLICATION_LANGUAGE))
            //    setTitleStyle(preference, true, false);


        }
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RingtonePreference) {
            // keep summary from preference
        }
        else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            //preference.setSummary(preference.toString());
             preference.setSummary(stringValue);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext());
                //Log.d("PhoneProfilesPreferencesNestedFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }


    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}

        if (mobileCellsRegistrationBroadcastReceiver != null) {
            try {
                getActivity().unregisterReceiver(mobileCellsRegistrationBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
    {
        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
            (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
            (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
            (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context context = getActivity().getApplicationContext();

                boolean finishActivity = false;
                boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                    boolean canWrite = Settings.System.canWrite(context);
                    permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                    if (canWrite)
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                }
                if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                    permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                    if (notificationPolicyGranted)
                        Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                }
                if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                    boolean canDrawOverlays = Settings.canDrawOverlays(context);
                    permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                    if (canDrawOverlays)
                        Permissions.setShowRequestDrawOverlaysPermission(context, true);
                }
                if (requestCode == RESULT_APPLICATION_PERMISSIONS) {
                    boolean calendarPermission = Permissions.checkCalendar(context);
                    permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                    // finish Editor when permission is disabled
                    finishActivity = permissionsChanged && (!calendarPermission);
                    if (!permissionsChanged) {
                        boolean contactsPermission = Permissions.checkContacts(context);
                        permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!contactsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean locationPermission = Permissions.checkLocation(context);
                        permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!locationPermission);
                    }
                    if (!permissionsChanged) {
                        boolean smsPermission = Permissions.checkSMS(context);
                        permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!smsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean phonePermission = Permissions.checkPhone(context);
                        permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!phonePermission);
                    }
                    if (!permissionsChanged) {
                        boolean storagePermission = Permissions.checkStorage(context);
                        permissionsChanged = Permissions.getStoragePermission(context) != storagePermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!storagePermission);
                    }
                }

                Permissions.saveAllPermissions(context, permissionsChanged);

                if (permissionsChanged) {
                    DataWrapper dataWrapper = new DataWrapper(context, false, 0);

                    //Profile activatedProfile = dataWrapper.getActivatedProfile(true, true);
                    //dataWrapper.refreshProfileIcon(activatedProfile);
                    if (PhoneProfilesService.instance != null)
                        PhoneProfilesService.instance.showProfileNotification(dataWrapper);
                    ActivateProfileHelper.updateGUI(context, true);

                    if (finishActivity) {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finishAffinity();
                    } else {
                        getActivity().setResult(Activity.RESULT_OK);
                    }
                }
                else
                    getActivity().setResult(Activity.RESULT_CANCELED);
            }
        }

        if (requestCode == RESULT_LOCATION_SYSTEM_SETTINGS) {
            final boolean enabled = PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext());
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }

        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            //Log.d("EventPreferencesFragment.doOnActivityResult", "xxx");
            if (PhoneProfilesPreferencesFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    PhoneProfilesPreferencesFragment.changedLocationGeofencePreference.setGeofenceFromEditor(/*geofenceId*/);
                    PhoneProfilesPreferencesFragment.changedLocationGeofencePreference = null;
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "PhoneProfilesPreferencesFragment_PreferenceScreenKey";
    }

}
