package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class AirplaneModeStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### AirplaneModeStateChangedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "AirplaneModeStateChangedBroadcastReceiver.onReceive", "AirplaneModeStateChangedBroadcastReceiver_onReceive");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning(context)) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                final boolean state = intent.getBooleanExtra("state", false);
                EventsHandlerJob.startForRadioSwitchSensor(EventPreferencesRadioSwitch.RADIO_TYPE_AIRPLANE_MODE, state);
            }
        }
    }
}
