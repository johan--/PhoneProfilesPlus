package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;

import static android.content.Context.DEVICE_POLICY_SERVICE;

public class ProfilePreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    protected int startupSource;

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private Context context;

    private static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    private static final String PREFS_NAME_DEFAULT_PROFILE = PPApplication.DEFAULT_PROFILE_PREFS_NAME;

    private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;

    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 1981;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    static final String PREF_DEVICE_ADMINISTRATOR_SETTINGS = "prf_pref_lockDevice_deviceAdminSettings";
    private static final int RESULT_DEVICE_ADMINISTRATOR_SETTINGS = 1982;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.d("------ ProfilePreferencesNestedFragment.onCreate", "this="+this);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        Bundle bundle = this.getArguments();
        if (bundle != null)
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);

        //Log.d("------ ProfilePreferencesNestedFragment.onCreate", "startupSource="+startupSource);

        context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
    }

    public static String getPreferenceName(int startupSource) {
        String PREFS_NAME;
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        /*else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;*/
        else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE)
            PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        return PREFS_NAME;
    }

    protected void setPreferencesManager() {
        String PREFS_NAME = getPreferenceName(startupSource);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("ProfilePreferencesNestedFragment.onActivityCreated","startupSource="+startupSource);

        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE) {
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            Bundle bundle = getArguments();
            if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
                toolbar.setSubtitle(getString(R.string.title_activity_default_profile_preferences));
            else
                toolbar.setSubtitle(null);
        }

        setPreferencesManager();
        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        //Log.d("------ ProfilePreferencesNestedFragment.onActivityCreated", "this="+this);
        //Log.d("------ ProfilePreferencesNestedFragment.onActivityCreated", "prefMng="+prefMng);
        //Log.d("------ ProfilePreferencesNestedFragment.onActivityCreated", "preferences="+preferences);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
            /*if (ringerModePreference.findIndexOfValue("5") < 0) {
                // add zen mode option to preference Ringer mode
                CharSequence[] entries = ringerModePreference.getEntries();
                CharSequence[] entryValues = ringerModePreference.getEntryValues();

                CharSequence[] newEntries = new CharSequence[entries.length + 1];
                CharSequence[] newEntryValues = new CharSequence[entries.length + 1];

                for (int i = 0; i < entries.length; i++) {
                    newEntries[i] = entries[i];
                    newEntryValues[i] = entryValues[i];
                }

                newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
                newEntryValues[entries.length] = "5";

                ringerModePreference.setEntries(newEntries);
                ringerModePreference.setEntryValues(newEntryValues);
                ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));
                setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                     (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);
            PPApplication.logE("ProfilePreferencesNestedFragment.onActivityCreated","canEnableZenMode="+canEnableZenMode);

            ListPreference zenModePreference = (ListPreference)prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    zenModePreference.setTitle(R.string.profile_preferences_volumeZenModeM);
                    zenModePreference.setDialogTitle(R.string.profile_preferences_volumeZenModeM);
                }
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
            }

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (notificationAccessPreference != null) {
                if (canEnableZenMode) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                    preferenceCategory.removePreference(notificationAccessPreference);
                } else {
                    if (ringerModePreference != null) {
                        CharSequence[] entries = ringerModePreference.getEntries();
                        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE)
                            entries[5] = "(S) " + getString(R.string.array_pref_ringerModeArray_ZenMode);
                        else
                            entries[6] = "(S) " + getString(R.string.array_pref_ringerModeArray_ZenMode);
                        ringerModePreference.setEntries(entries);
                    }

                    boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                    @SuppressLint("InlinedApi")
                    final boolean showDoNotDisturbPermission =
                            (android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                            GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext());
                    if (showDoNotDisturbPermission) {
                        notificationAccessPreference.setTitle(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions));
                        notificationAccessPreference.setSummary(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary));
                    }

                    //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            if (showDoNotDisturbPermission) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            }
                            else
                            if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
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

            if (ringerModePreference != null) {
                ringerModePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String sNewValue = (String) newValue;
                        int iNewValue;
                        if (sNewValue.isEmpty())
                            iNewValue = 0;
                        else
                            iNewValue = Integer.parseInt(sNewValue);

                    /*final boolean canEnableZenMode =
                            (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                            );*/
                        final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), true);

                        Preference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                        if (zenModePreference != null) {
                            zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);

                            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            @SuppressLint("InlinedApi")
                            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                            GlobalGUIRoutines.setPreferenceTitleStyle(zenModePreference, false, false, false, addS);
                        }

                        return true;
                    }
                });
            }
        }
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if ((android.os.Build.VERSION.SDK_INT < 23) || (android.os.Build.VERSION.SDK_INT > 23)) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT == 23) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_othersCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceWiFiAP));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP, value);
            }
        }
        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
        {
            ListPreference networkTypePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (networkTypePreference != null) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int phoneType = TelephonyManager.PHONE_TYPE_GSM;
                if (telephonyManager != null)
                    phoneType = telephonyManager.getPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));
                    }
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));
                    }
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }
            }
        }
        Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
        if (preference != null)
        {
            preference.setTitle("[M] " + context.getString(R.string.profile_preferences_duration));
            String value = preferences.getString(Profile.PREF_PROFILE_DURATION, "");
            setSummary(Profile.PREF_PROFILE_DURATION, value);
        }
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(context)) {
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_volumeCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        else {
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // start preferences activity for default profile
                        Intent intent = new Intent(getActivity().getBaseContext(), PhoneProfilesPreferencesActivity.class);
                        intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "categorySystem");
                        intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                        getActivity().startActivityForResult(intent, RESULT_UNLINK_VOLUMES_APP_PREFERENCES);
                        return false;
                    }
                });
            }
        }

        Preference deviceAdminSettings = prefMng.findPreference(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
        if (deviceAdminSettings != null) {
            DevicePolicyManager manager = (DevicePolicyManager)getActivity().getSystemService(DEVICE_POLICY_SERVICE);
            final ComponentName component = new ComponentName(getActivity(), PPDeviceAdminReceiver.class);
            if ((manager == null) || !manager.isAdminActive(component)) {
                deviceAdminSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
                        getActivity().startActivityForResult(intent, RESULT_DEVICE_ADMINISTRATOR_SETTINGS);
                        return false;
                    }
                });
            }
        }

        InfoDialogPreference infoDialogPreference = (InfoDialogPreference)prefMng.findPreference("prf_pref_prefernceTypesInfo");
        if (infoDialogPreference != null) {
            infoDialogPreference.setInfoText(
                    getString(R.string.important_info_profile_grant)+"\n"+
                    getString(R.string.profile_preferences_typesInfoGrant)+"\n\n"+
                    getString(R.string.important_info_profile_root)+"\n\n"+
                    getString(R.string.important_info_profile_settings)+"\n\n"+
                    getString(R.string.important_info_profile_interactive));
        }

        Preference showInActivatorPreference = prefMng.findPreference(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        if (showInActivatorPreference != null)
            showInActivatorPreference.setTitle("[A] " + getResources().getString(R.string.profile_preferences_showInActivator));

    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}
        super.onDestroy();
    }

    private String getTitleWhenPreferenceChanged(String key, boolean systemSettings) {
        Preference preference = prefMng.findPreference(key);
        String title = "";
        if ((preference != null) && (preference.isEnabled())) {
            if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                /*boolean defaultValue =
                        getResources().getBoolean(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "bool", context));*/
                boolean defaultValue = Profile.defaultValuesBoolean.get(preference.getKey());
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = preference.getTitle().toString();
            }
            else {
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(preference.getKey(), "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(preference.getKey());
                if (preference instanceof VolumeDialogPreference) {
                    if (VolumeDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else if (preference instanceof BrightnessDialogPreference) {
                    if (BrightnessDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else {
                    if (!preferences.getString(preference.getKey(), defaultValue).equals(defaultValue)) {
                        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) &&
                                (android.os.Build.VERSION.SDK_INT >= 23))
                            title = context.getString(R.string.profile_preferences_volumeZenModeM);
                        else
                            title = preference.getTitle().toString();
                    }
                }
            }
            if (systemSettings) {
                if (!title.isEmpty() && !title.contains("(S)"))
                    title = "(S) " + title;
            }
            return title;
        }
        else
            return title;
    }

    private void setCategorySummary(Preference preference, boolean bold) {
        String key = preference.getKey();
        boolean _bold = bold;
        Preference preferenceScreen = null;
        String summary = "";

        if (key.equals(Profile.PREF_PROFILE_DURATION) ||
            key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, false);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            if (bold) {
                // any of duration preferences are set
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, false);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
            @SuppressLint("InlinedApi")
            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, addS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, false);
                if (!title.isEmpty()) {
                    _bold = true;
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title;
                }
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(Profile.PREF_PROFILE_SOUND_ALARM)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);
            preferenceScreen = prefMng.findPreference("prf_pref_soundsCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED) ||
                key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            //key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            //key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER)
            key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING)) {
            String title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, false);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, false);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_applicationCategory");
        }

        if (preferenceScreen != null) {
            GlobalGUIRoutines.setPreferenceTitleStyle(preferenceScreen, _bold, false, false, false);
            if (_bold)
                preferenceScreen.setSummary(summary);
            else
                preferenceScreen.setSummary("");
        }
    }

    private void setSummaryForNotificationVolume0() {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigured);
            else
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigureForVolume0);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
                setCategorySummary(preference, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        listPreference.setEnabled(false);
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        @SuppressLint("InlinedApi")
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                        GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, addS);
                        setCategorySummary(listPreference, false);
                    }
                }
                else
                {
                    String sValue = value.toString();
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        int iValue = Integer.parseInt(sValue);
                        int index = listPreference.findIndexOfValue(sValue);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        if ((iValue != 0) && (iValue != 99)) {
                            if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                                String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                                summary = summary + " - " + summaryArray[iValue - 1];
                            }
                        }
                        listPreference.setSummary(summary);

                        final String sRingerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                        int iRingerMode;
                        if (sRingerMode.isEmpty())
                            iRingerMode = 0;
                        else
                            iRingerMode = Integer.parseInt(sRingerMode);

                        if (iRingerMode == 5) {
                            boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                            @SuppressLint("InlinedApi")
                            boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                    GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                            GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, addS);
                            setCategorySummary(listPreference, index > 0);
                        }
                        listPreference.setEnabled(iRingerMode == 5);
                    }
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0);
            }
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                ListPreference mobileDataPreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
                if (mobileDataPreference != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                        mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData_21);
                    }
                    else {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                        mobileDataPreference.setDialogTitle(R.string.profile_preferences_deviceMobileData);
                    }
                }
            }
            int canChange = Profile.isProfilePreferenceAllowed(key, context);
            if (canChange != PPApplication.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, false, false, false);
                    setCategorySummary(preference, false);
                }
            }
            else
            if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    String sValue = value.toString();
                    boolean bold = !sValue.equals(Profile.CONNECTTOSSID_JUSTANY);
                    GlobalGUIRoutines.setPreferenceTitleStyle(preference, bold, false, false, false);
                    setCategorySummary(preference, bold);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    //if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA))
                    //    Log.e("ProfilePreferencesFragment", "index="+index);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }

        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, false);
                    setCategorySummary(listPreference, false);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
            key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
            key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
            key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS))
        {
            int canChange = PPApplication.PREFERENCE_ALLOWED;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
                canChange = Profile.isProfilePreferenceAllowed(key, context);
            if (canChange != PPApplication.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, false);
                    setCategorySummary(listPreference, false);
                }
            }
            else {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed_23);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed_23);
                } else {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed);
                    listPreference.setDialogTitle(R.string.profile_preferences_notificationLed);
                }
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, false);
                    setCategorySummary(listPreference, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS))
        {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, false);
                    setCategorySummary(listPreference, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                int iValue = 0;
                if (!sValue.isEmpty())
                    iValue = Integer.valueOf(sValue);
                //preference.setSummary(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, iValue > 0, false, false, false);
                setCategorySummary(preference, iValue > 0);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(key, "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(key);
                GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, value != defaultValue, false, false, false);
                setCategorySummary(listPreference, /*index > 0*/false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyle(checkBoxPreference, show, false, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreference ringtonePreference = (RingtonePreference) prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyle(ringtonePreference, show, false, false, false);
                setCategorySummary(ringtonePreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyle(checkBoxPreference, show, false, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyle(checkBoxPreference, show, false, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, change, false, false, false);
                setCategorySummary(preference, change);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0();
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, change, false, false, false);
                setCategorySummary(preference, change);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING) ||
            key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int canChange = Profile.isProfilePreferenceAllowed(key, context);
                if (canChange != PPApplication.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == PPApplication.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, false, false, false);
                    setCategorySummary(listPreference, false);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, index > 0, false, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }

    }

    void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = Profile.getVolumeRingtoneChange(ringtoneValue);
        if (enabled) {
            int volume = Profile.getVolumeRingtoneValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes(context) &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context) &&
                    Profile.getVolumeRingtoneChange(notificationValue) && (Profile.getVolumeRingtoneValue(notificationValue) == 0);
    }

    private void disableDependedPref(String key, Object value)
    {
        String sValue = value.toString();

        final String NO_CHANGE = "0";
        final String DEFAULT_PROFILE = "99";
        final String ON = "1";

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            boolean enabled = getEnableVolumeNotificationByRingtone(sValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION)) {
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                                    getEnableVolumeNotificationByRingtone(ringtoneValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            enabled = getEnableVolumeNotificationVolume0(enabled, sValue);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            boolean enabled = !sValue.equals(ON);
            ListPreference preference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(NO_CHANGE);
                preference.setEnabled(enabled);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
                boolean enabled = false;
                if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, context) == PPApplication.PREFERENCE_ALLOWED) &&
                        ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
                ListPreference preference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(NO_CHANGE);
                    preference.setEnabled(enabled);
                }
            }
        }

        DevicePolicyManager manager = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);
        final ComponentName component = new ComponentName(getActivity(), PPDeviceAdminReceiver.class);
        /* THIS NOT WORKING - after enabling device admin, entries are not refreshed in dialog :-/
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            ListPreference lockDevicePreference = (ListPreference) prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (lockDevicePreference != null) {
                CharSequence[] entries = lockDevicePreference.getEntries();
                if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE) {
                    if (!manager.isAdminActive(component))
                        entries[1] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_enabled) + ")";
                    if (!PPApplication.isRooted())
                        entries[2] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_rooted) + ")";
                } else {
                    if (!manager.isAdminActive(component))
                        entries[2] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_enabled) + ")";
                    if (!PPApplication.isRooted())
                        entries[3] = getString(R.string.array_pref_lockDevice_deviceAdmin) + " (" +
                                getString(R.string.array_pref_lockDevice_not_rooted) + ")";
                }
                lockDevicePreference.setEntries(entries);
            }
        }*/
        if (key.equals(PREF_DEVICE_ADMINISTRATOR_SETTINGS)) {
            if ((manager != null) && manager.isAdminActive(component)) {
                Preference preference = prefMng.findPreference(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
                if (preference != null) {
                    preference.setSummary(R.string.profile_preferences_lockDevice_deviceAdminSettings_summary_activated);
                    preference.setEnabled(false);
                }
            }
        }
    }

    protected void disableDependedPref(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {

        String value;
        if (key.equals(Profile.PREF_PROFILE_NAME)) {
            value = sharedPreferences.getString(key, "");
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + value);
        }
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
            value = sharedPreferences.getString(key, "");
        setSummary(key, value);
        // disable depended preferences
        disableDependedPref(key, value);

        if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE) {
            // no save menu for shared profile
            ProfilePreferencesActivity activity = (ProfilePreferencesActivity)getActivity();
            ProfilePreferencesActivity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.d("------ ProfilePreferencesFragment.doOnActivityResult", "this="+this);
        //Log.d("------ ProfilePreferencesFragment.doOnActivityResult", "prefMng="+prefMng);
        //Log.d("------ ProfilePreferencesFragment.doOnActivityResult", "preferences="+preferences);

        if (requestCode == EditorProfilesActivity.REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == Activity.RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                //int newProfileMode = data.getIntExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id == Profile.DEFAULT_PROFILE_ID)
                {
                    Profile defaultProfile = Profile.getDefaultProfile(context);
                    Permissions.grantProfilePermissions(context, defaultProfile, false, true,
                            /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, /*true,*/ null, false);

                    Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                    //TODO Android O
                    //if (Build.VERSION.SDK_INT < 26)
                    context.startService(serviceIntent);
                    //else
                    //    context.startForegroundService(serviceIntent);
                }
            }
        }
        if (requestCode == ImageViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        //noinspection WrongConstant
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception ignored) {
                    }
                }

                if (ProfilePreferencesFragment.changedImageViewPreference != null) {
                    // set image identifier for get bitmap path
                    ProfilePreferencesFragment.changedImageViewPreference.setImageIdentifier(selectedImage.toString());
                    ProfilePreferencesFragment.changedImageViewPreference = null;
                }
            }
        }
        if (requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        //noinspection WrongConstant
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception ignored) {
                    }
                }

                if (ProfilePreferencesFragment.changedProfileIconPreference != null) {
                    // set image identifier ant type for get bitmap path
                    ProfilePreferencesFragment.changedProfileIconPreference.setImageIdentifierAndType(selectedImage.toString(), false, true);
                    ProfilePreferencesFragment.changedProfileIconPreference = null;
                }
            }
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if (requestCode == ApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR && resultCode == Activity.RESULT_OK && data != null)
        {
            if (ProfilePreferencesFragment.applicationsDialogPreference != null) {
                //Log.e("ProfilePreferencesNestedFragment.doOnActivityResult", "data="+PPApplication.intentToString(data));

                ProfilePreferencesFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        /*(Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),*/
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));

                ProfilePreferencesFragment.applicationsDialogPreference = null;
            }
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        }
        if (requestCode == RESULT_DEVICE_ADMINISTRATOR_SETTINGS) {
            disableDependedPref(PREF_DEVICE_ADMINISTRATOR_SETTINGS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("------ ProfilePreferencesFragment.onActivityResult", "this="+this);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        //Log.d("------ ProfilePreferencesFragment.addPreferencesFromResource", "startupSource="+startupSource);
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAULT_PROFILE)
            return "DefaultProfilePreferencesFragment_PreferenceScreenKey";
        else
            return "ProfilePreferencesFragment_PreferenceScreenKey";
    }

}
