package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class LocationGeofencePreference extends DialogPreference {

    private final Context context;

    final int onlyEdit;

    private MaterialDialog mDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    //private TextView geofenceName;
    private LocationGeofencesPreferenceAdapter listAdapter;

    public final DataWrapper dataWrapper;

    static final String EXTRA_GEOFENCE_ID = "geofence_id";
    static final int RESULT_GEOFENCE_EDITOR = 2100;

    public LocationGeofencePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray locationGeofenceType = context.obtainStyledAttributes(attrs,
                R.styleable.LocationGeofencePreference, 0, 0);

        onlyEdit = locationGeofenceType.getInt(R.styleable.LocationGeofencePreference_onlyEdit, 0);

        locationGeofenceType.recycle();

        this.context = context;

        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0);
    }

    @Override
    protected void showDialog(Bundle state) {

        if (onlyEdit == 0) {
            String value = "";
            value = getPersistedString(value);
            DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
        }

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                //.disableDefaultFonts()
                .autoDismiss(false)
                .content(getDialogMessage())
                .customView(R.layout.activity_location_pref_dialog, false)
                .dividerColor(0);

        if (onlyEdit == 0) {
            mBuilder.positiveText(getPositiveButtonText())
                    .negativeText(getNegativeButtonText());
            mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    persistGeofence(false);
                    mDialog.dismiss();
                }
            });
            mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    mDialog.dismiss();
                }
            });

        }
        else {
            mBuilder.positiveText(getPositiveButtonText());
            mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    mDialog.dismiss();
                }
            });
        }

        mDialog = mBuilder.build();
        View layout = mDialog.getCustomView();

        //progressLinearLayout = layout.findViewById(R.id.location_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.location_pref_dlg_rella_data);

        //geofenceName = layout.findViewById(R.id.location_pref_dlg_geofence_name);
        //updateGUIWithGeofence(dataWrapper.getDatabaseHandler().getCheckedGeofences());

        //noinspection ConstantConditions
        AppCompatImageButton addButton = layout.findViewById(R.id.location_pref_dlg_add);

        //noinspection ConstantConditions
        ListView geofencesListView = layout.findViewById(R.id.location_pref_dlg_listview);

        listAdapter = new LocationGeofencesPreferenceAdapter(context, DatabaseHandler.getInstance(context.getApplicationContext()).getGeofencesCursor(), this);
        geofencesListView.setAdapter(listAdapter);

        refreshListView();

        geofencesListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                LocationGeofencesPreferenceAdapter.ViewHolder viewHolder =
                        (LocationGeofencesPreferenceAdapter.ViewHolder) v.getTag();

                /*
                if (listAdapter.selectedRB != null){
                    long gid = (long)listAdapter.selectedRB.getTag();
                    Log.d("LocationGeofencePreference.onItemClick", "checked id="+gid);
                    listAdapter.selectedRB.setChecked(false);
                }
                */
                //listAdapter.selectedRB = viewHolder.radioButton;

                long gid = viewHolder.geofenceId;
                if (onlyEdit == 0) {
                    DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(String.valueOf(gid), 2);
                    //viewHolder.radioButton.setChecked(true);
                    //updateGUIWithGeofence(gid);
                    refreshListView();
                }
                else {
                    startEditor(gid);
                }

            }

        });

        /*
        final TextView helpText = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpText);
        String helpString = context.getString(R.string.pref_dlg_info_about_wildcards_1) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_2) + " " +
                            context.getString(R.string.wifi_ssid_pref_dlg_info_about_wildcards) + " " +
                            context.getString(R.string.pref_dlg_info_about_wildcards_3);
        helpText.setText(helpString);

        ImageView helpIcon = layout.findViewById(R.id.wifi_ssid_pref_dlg_helpIcon);
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE)
                    visibility = View.GONE;
                else
                    visibility = View.VISIBLE;
                helpText.setVisibility(visibility);
            }
        });
        */

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditor(0);
            }
        });

        final Button unselectAllButton = layout.findViewById(R.id.location_pref_dlg_uselectAll);
        unselectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence("", 0);
                refreshListView();
            }
        });

        GlobalGUIRoutines.registerOnActivityDestroyListener(this, this);

        if (state != null)
            mDialog.onRestoreInstanceState(state);

        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence("", 0);
        GlobalGUIRoutines.unregisterOnActivityDestroyListener(this, this);
        Cursor cursor = listAdapter.getCursor();
        if (cursor != null)
            cursor.close();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return ta.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (onlyEdit == 0) {
            if (restoreValue) {
                String value = "";
                value = getPersistedString(value);
                DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
            } else {
                String value = (String) defaultValue;
                persistString(value);
                DatabaseHandler.getInstance(context.getApplicationContext()).checkGeofence(value, 1);
            }
        }
    }    

    private void persistGeofence(boolean reset) {
        if (onlyEdit == 0) {
            if (shouldPersist()) {
                String value = DatabaseHandler.getInstance(context.getApplicationContext()).getCheckedGeofences();
                if (callChangeListener(value)) {
                    if (reset)
                        persistString("");
                    persistString(value);
                }
            }
        }
    }

    /*
    public void updateGUIWithGeofence(long geofenceId)
    {
        String name = "";
        if (onlyEdit == 0) {
            name = dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId);
            if (name.isEmpty())
                name = "[" + context.getString(R.string.event_preferences_locations_location_not_selected) + "]";
        }

        this.geofenceName.setText(name);
    }
    */
    
    public void refreshListView()
    {
        listAdapter.reload(dataWrapper);
    }

    private void startEditor(long geofenceId) {
        Intent intent = new Intent(context, LocationGeofenceEditorActivity.class);
        intent.putExtra(EXTRA_GEOFENCE_ID, geofenceId);

        // is not possible to get activity from preference, used is static method
        if (onlyEdit == 0) {
            EventPreferencesFragment.setChangedLocationGeofencePreference(this);
            ((Activity)context).startActivityForResult(intent, RESULT_GEOFENCE_EDITOR);
        }
        else {
            PhoneProfilesPreferencesFragment.setChangedLocationGeofencePreference(this);
            ((Activity)context).startActivityForResult(intent, RESULT_GEOFENCE_EDITOR);
        }
    }

    void setGeofenceFromEditor(/*long geofenceId*/) {
        //Log.d("LocationGeofencePreference.setGeofenceFromEditor", "geofenceId=" + geofenceId);
        persistGeofence(true);
        refreshListView();
        //updateGUIWithGeofence(geofenceId);
    }

    public void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context context = view.getContext();
        PopupMenu popup;
        if (android.os.Build.VERSION.SDK_INT >= 19)
            popup = new PopupMenu(context, view, Gravity.END);
        else
            popup = new PopupMenu(context, view);
        new MenuInflater(context).inflate(R.menu.location_geofence_pref_item_edit, popup.getMenu());

        final long geofenceId = (long)view.getTag();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(android.view.MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location_geofence_pref_item_menu_edit:
                        startEditor(geofenceId);
                        return true;
                    case R.id.location_geofence_pref_item_menu_delete:
                        if (geofenceId > 0) {
                            if (!DatabaseHandler.getInstance(context.getApplicationContext()).isGeofenceUsed(geofenceId)) {
                                DatabaseHandler.getInstance(context.getApplicationContext()).deleteGeofence(geofenceId);
                                refreshListView();
                                //updateGUIWithGeofence(0);
                                /*if (dataWrapper.getDatabaseHandler().getGeofenceCount() == 0) {
                                    // stop location updates
                                    if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted())
                                        PhoneProfilesService.getGeofencesScanner().disconnect();
                                }*/
                            }
                            else {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                dialogBuilder.setTitle(R.string.event_preferences_locations_cant_delete_location_title);
                                dialogBuilder.setMessage(R.string.event_preferences_locations_cant_delete_location_text);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                dialogBuilder.show();
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });


        popup.show();
    }

}