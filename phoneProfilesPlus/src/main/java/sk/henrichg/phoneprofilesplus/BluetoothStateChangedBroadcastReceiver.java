package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BluetoothStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothStateChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, BluetoothService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(BluetoothAdapter.EXTRA_STATE, intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);
    }
}
