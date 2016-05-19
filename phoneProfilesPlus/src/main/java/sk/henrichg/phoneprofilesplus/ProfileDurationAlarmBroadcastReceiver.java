package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ProfileDurationAlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### ProfileDurationAlarmBroadcastReceiver.onReceive", "xxx");

        if (GlobalData.getApplicationStarted(context))
        {
            GlobalData.loadPreferences(context);

            long profileId = intent.getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
            if (profileId != 0)
            {
                DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

                if (dataWrapper.getIsManualProfileActivation())
                {
                    Profile profile = dataWrapper.getProfileById(profileId, false);
                    Profile activatedProfile = dataWrapper.getActivatedProfile();

                    if ((profile != null) &&
                        (activatedProfile._id == profile._id) &&
                        (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
                    {
                        // alarm is from activated profile

                        long activateProfileId = 0;
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE)
                        {
                            activateProfileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
                            if (activateProfileId == GlobalData.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_BACKGROUNDPROFILE, null,
                                                                            dataWrapper.getProfileNameWithManualIndicator(profile, true, true),
                                                                            profile._icon, 0);
                        }
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE)
                        {
                            activateProfileId = GlobalData.getActivatedProfileForDuration(context);

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_UNDOPROFILE, null,
                                    dataWrapper.getProfileNameWithManualIndicator(profile, true, true),
                                    profile._icon, 0);
                        }
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_RESTARTEVENTS)
                        {
                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_AFTERDURATION_RESTARTEVENTS, null,
                                    dataWrapper.getProfileNameWithManualIndicator(profile, true, true),
                                    profile._icon, 0);

                            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                            GlobalData.logE("$$$ restartEvents", "from ProfileDurationAlarmBroadcastReceiver");
                            dataWrapper.restartEventsWithDelay(3, true);
                        }
                        else
                        {
                            dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);
                            dataWrapper.activateProfile(activateProfileId, GlobalData.STARTUP_SOURCE_SERVICE, null/*, ""*/);
                        }
                    }
                }

                dataWrapper.invalidateDataWrapper();

            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static public void setAlarm(Profile profile, Context context)
    {
        removeAlarm(context);

        if (profile == null)
            return;

        if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
            (profile._duration > 0))
        {
            // duration for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, profile._duration);
            long alarmTime = now.getTimeInMillis();// + 1000 * 60 * profile._duration;

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //String result = sdf.format(alarmTime);

            Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);
            intent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

            //this._isInDelay = true;
        }
        //else
        //	this._isInDelay = false;

        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);

        return;
    }

    static public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, ProfileDurationAlarmBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        //this._isInDelay = false;
        //dataWrapper.getDatabaseHandler().updateEventInDelay(this);
    }

}
