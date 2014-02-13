/*
 * Copyright (c) 2014 UP-NEXT. All rights reserved.
 * http://www.up-next.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.upnext.blekit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.fasterxml.jackson.databind.JsonNode;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.conditions.BLECondition;
import com.upnext.blekit.conditions.OccurenceCondition;
import com.upnext.blekit.listeners.BLEKitStateListener;
import com.upnext.blekit.listeners.BeaconEventListener;
import com.upnext.blekit.listeners.ZoneUpdateListener;
import com.upnext.blekit.model.Beacon;
import com.upnext.blekit.model.Condition;
import com.upnext.blekit.model.CurrentBeaconProximity;
import com.upnext.blekit.model.Trigger;
import com.upnext.blekit.model.Zone;
import com.upnext.blekit.util.BeaconPreferences;
import com.upnext.blekit.util.BeaconsDB;
import com.upnext.blekit.util.JsonParser;
import com.upnext.blekit.util.L;
import com.upnext.blekit.util.http.HttpClient;
import com.upnext.blekit.util.http.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic class used for BLEKit lifecycle management and configuration.
 *
 * Basic usage (in Activity):
 * <pre>
 * {@code
 * BLEKit
 * .create(this)
 * .setJsonUrl("http://your.server.com/zone.json")
 * .start(this);
 * }
 * </pre>
 *
 * Advanced configuration (in Activity):
 * <pre>
 * {@code
 * BLEKit
 * .create(this)
 * .setJsonUrl("http://your.server.com/zone.json")
 * .addAction(new CustomAction())
 * .addCondition(new CustomCondition())
 * .removeActionByType(FacebookCheckinAction.TYPE)
 * .setStateListener(this)
 * .start(this);
 * }
 * </pre>
 *
 * It is the responsibility of the developer to stop BLEKit service with {@link #stop(android.content.Context)}.
 *
 * @author Roman Wozniak (roman@up-next.com)
 *
 * @see com.upnext.blekit.actions.BLEAction
 * @see com.upnext.blekit.conditions.BLECondition
 */
public class BLEKit {

    private static BLEKit _bleKit;
    private static ConditionsFactory mConditionsFactory;
    private static ActionsFactory mActionsFactory;

    private static String jsonUrl;
    private static String jsonContent;

    private static BackgroundMode mBackgroundMode = new BackgroundMode(null, true);
    private static BeaconEventListener mBeaconEventListener;

    private static ZoneUpdateListener mZoneUpdateListener;

    private static BLEKitStateListener mStateListener;
    private static Map<String, Proximity> mCurrentBeaconsStates = new HashMap<String, Proximity>();
    private static JsonParser jsonParser = new JsonParser();

    private static Intent mEventToProcess;
    private static Zone mCurrentZone = null;

    private BeaconsDB beaconsDB;
    private Class targetActivityForNotifications;
    private boolean mBound;
    private Context mContext;


    private BLEKit(Context context) {
        mContext = context;
        String zoneJson = BeaconPreferences.getLastZoneJson(context);
        if( zoneJson!=null ) {
            mCurrentZone = jsonParser.parse(zoneJson);
        }

        beaconsDB = new BeaconsDB(context);
        mConditionsFactory = new ConditionsFactory();
        mActionsFactory = new ActionsFactory();
    }

    /**
     * Creates BLEKit service with default configuration.
     * If the service was runnig, it will be stopped first.
     *
     * Also creates new factories for conditions and actions.
     *
     * @param context context (either from activity or service)
     * @return BLEKit instance
     */
    public static BLEKit create( Context context ) {
        L.d( "create" );

        _bleKit = new BLEKit(context);

        return _bleKit;
    }

    /**
     * Starts BLEKit service.
     *
     * Fetches JSON configuration from given URL if provided.
     *
     * @param context context (either from activity or service)
     * @throws IllegalArgumentException thrown if configuration JSON was not provided (jsonUrl or jsonContent) or create() was not called before.
     */
    public void start( Context context ) throws IllegalArgumentException {
        checkInitialized();
        L.d( "start" );

        if( jsonContent==null && jsonUrl==null ) {
            throw new IllegalArgumentException( "You have to provide either jsonUrl or jsonContent" );
        }

        if( jsonUrl!=null ) {
            fetchJson();
        } else {
            _bleKit.updateZone(jsonContent);
        }

        mBound = true;

        if( mStateListener!=null ) {
            mStateListener.onBLEKitStarted();
        }

        if( mEventToProcess!=null ) {
            processServiceEvent(mEventToProcess, context);
        }

    }

    /**
     * Provide url with JSON configuration.
     * BLEKit should be created previously (#see BLEKit.create)
     *
     * If BLEKit is already started, then it fetches the JSON from given url and reloads the configuration.
     *
     * @param url url pointing to JSON configuration
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     * @throws java.net.MalformedURLException thrown if given url is malformed.
     */
    public static BLEKit setJsonUrl( String url ) throws IllegalStateException, MalformedURLException {
        checkInitialized();

        //validate URL
        new URL(url);

        jsonUrl = url;

        if( _bleKit.mBound ) {
            _bleKit.fetchJson();
        }

        return _bleKit;
    }

    /**
     * Provide content with JSON configuration.
     * BLEKit should be created previously (#see BLEKit.create)
     *
     * If BLEKit is already started, then it fetches the JSON from given url and reloads the configuration.
     *
     * @param content String containing JSON configuration
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized
     */
    public static BLEKit setJsonContent( String content ) throws IllegalStateException {
        checkInitialized();

        jsonContent = content;

        if( _bleKit.mBound ) {
            _bleKit.updateZone(jsonContent);
        }

        return _bleKit;
    }

    /**
     * If a JSON url was provided previously (and was valid), then it is fetched again and all actions are triggered.
     *
     * @param ctx context
     * @throws IllegalStateException thrown if BLEKit was not initialized
     */
    public static void reloadJson( Context ctx ) throws IllegalStateException {
        if( _bleKit==null ) {
            restartBlekit(ctx);
            return;
        }


        if(jsonUrl==null) return;

        try {
            new URL(jsonUrl);
        } catch (Exception e) {
            //if url is malformed, cosume error silently
            return;
        }

        if( _bleKit.mBound ) {
            _bleKit.fetchJson();
        } else {
            fetchJsonLocal();
        }
    }


    /**
     * Stops BLEKit service if it is running.
     *
     * @param context context (either from activity or service)
     */
    public static void stop( Context context ) {
        L.d("stop");
        mCurrentBeaconsStates = new HashMap<String, Proximity>();

        if( _bleKit!=null && _bleKit.mBound ) {

            _bleKit.sendStop();

            _bleKit.mBound = false;

            if( mStateListener!=null ) {
                mStateListener.onBLEKitStopped();
            }
        }
    }

    /**
     * Add a custom condition implementation.
     * Should only be used before starting (in the config phase) the BLEKit.
     *
     * @param condition instance of new condition
     * @return BLEKit instance
     * @throws java.lang.IllegalStateException thrown when BLEKit is already started
     */
    public BLEKit addCondition( BLECondition condition ) throws IllegalStateException {
        checkStarted();
        mConditionsFactory.addCondition(condition);
        return _bleKit;
    }

    /**
     * Add a custom action implementation.
     * Should only be used before starting (in the config phase) the BLEKit.
     *
     * @param action instance of new action
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public BLEKit addAction( BLEAction action ) throws IllegalStateException {
        checkStarted();
        mActionsFactory.addAction(action);
        return _bleKit;
    }


    /**
     * Set background mode.
     * If action supports foreground mode then for example in case of AlertAction and <code>background==false</code> an alert dialog will be shown.
     * Otherwise action will be processed in background (eg. a notification will be shown).
     *
     * @param background true for background, false for foreground
     * @param activity for a foreground state an Activity has to be provided; otherwise provide <code>null</code>
     * @throws java.lang.IllegalStateException thrown when <code>background==false</code> and <code>activity is null</code>
     */
    public static void setBackgroundMode( boolean background, Activity activity ) throws IllegalStateException {
        L.d("setBackgroundMode " + background);

        if( !background && activity==null ) {
            throw new IllegalArgumentException( "Activity is null for foreground" );
        }

        mBackgroundMode = new BackgroundMode( activity, background );
        if( _bleKit!=null && _bleKit.mBound ) {
            _bleKit.sendSetBackgroundMode(mBackgroundMode);
        }
    }

    /**
     * Sets listener for beacon events {@link com.upnext.blekit.BeaconEvent}
     * Events will arrive despite the current configuration (so you might have only a 'leave' condition with action defined in configuration and at the same time receive notifications of proximity events through this listener).
     *
     * @param listener beacon events listener
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setEventListener( BeaconEventListener listener ) throws IllegalStateException {
        mBeaconEventListener = listener;
        return _bleKit;
    }

    /**
     * Sets listener for configuration (zone) changes - eg. when a new configuration is fetched (after BLEKit start or after manual call to {@link #reloadJson(android.content.Context)})
     *
     * @param listener zone update listener
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setZoneUpdateListener( ZoneUpdateListener listener ) throws IllegalStateException {
        mZoneUpdateListener = listener;
        return _bleKit;
    }


    /**
     * Sets listener for BLEKit states - when in starts, stops and gets current beacon proximities after start.
     *
     * @param listener state listener
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setStateListener( BLEKitStateListener listener ) throws IllegalStateException {
        mStateListener = listener;
        return _bleKit;
    }

    /**
     * Returns conditions factory.
     * Useful when you want to remove any conditions provided by default or get an implementation by type.
     *
     * @return conditions factory
     */
    public static ConditionsFactory getConditionsFactory() {
        return mConditionsFactory;
    }

    /**
     * Returns actions factory.
     * Useful when you want to remove any actions provided by default or get an implementation by type.
     *
     * @return actions factory
     */
    public static ActionsFactory getActionsFactory() {
        return mActionsFactory;
    }

    /**
     * Removes action of given type from the actions factory.
     * This is required in order to provide custom implementation of actions for default actions.
     *
     *
     * @param actionType type of action you want to be removed (eg. alert)
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit removeActionByType( String actionType ) throws IllegalStateException {
        checkInitialized();
        mActionsFactory.remove(actionType);
        return _bleKit;
    }

    /**
     * Removes condition of given type from the conditions factory.
     * This is required in order to provide custom implementation of conditions for default actions.
     *
     *
     * @param conditionType type of condition you want to be removed (eg. enter)
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit removeConditionByType( String conditionType ) throws IllegalStateException {
        checkInitialized();
        mConditionsFactory.remove(conditionType);
        return _bleKit;
    }

    /**
     * Checks whether Bluetooth LE is available on the device and Bluetooth turned on.
     *
     * If BLE is not supported, then a dialog with appropriate message is shown.
     * If BLE is supported, but Bluetooth is not turned on then a dialog with message is shown and an option to go directly to settings and turn Bluetooth on.
     *
     * A good place to invoke this method would be onResume() in your Activity.
     *
     * @param activity Activity
     * @return false if BLE is not supported or Bluetooth not turned on; otherwise true
     */
    public static boolean checkAvailability(Activity activity) {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showErrorDialog( activity.getString(R.string.blekit_ble_unsupported), activity, true);
        }
        else {
            if (((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
                return true;
            }
            showErrorDialog( activity.getString(R.string.blekit_bt_not_on), activity, false );
        }
        return false;
    }

    /**
     * Set target Activity to launch after clicking on notification.
     * For the notification to be handled successfully add the following line to the <code>onNewIntent</code> method of this activity:
     * <pre>
     * {@code
     * protected void onNewIntent(Intent intent) {
     *     super.onNewIntent(intent);
     *     BLEKit.processIntent(intent, this); //add this line
     * }
     * }
     * </pre>
     *
     * @param cls target Activity class
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized
     * @see #processIntent(android.content.Intent, android.app.Activity)
     */
    public static BLEKit setTargetActivityForNotifications( Class cls ) throws IllegalStateException {
        checkInitialized();
        _bleKit.targetActivityForNotifications = cls;
        BeaconPreferences.setTargetActivityForNotifications(_bleKit.mContext, cls.getName());
        return _bleKit;
    }

    /**
     * Return the class for activity that handles notifications.
     *
     * @return Activity class handling notifications
     */
    public static Class getTargetActivityForNotifications() {
        return _bleKit.targetActivityForNotifications;
    }

    /**
     * Should be called from Activity that handles notifications.
     * For the notification to be handled successfully add the following line to the <code>onNewIntent</code> method of this activity:
     * <pre>
     * {@code
     * protected void onNewIntent(Intent intent) {
     *     super.onNewIntent(intent);
     *     BLEKit.processIntent(intent, this); //add this line
     * }
     * }
     * </pre>
     *
     * @param intent Android Intent
     * @param activity Android Activity
     */
    public static void processIntent( Intent intent, Activity activity ) {
        if( intent==null ) return;
        String type = intent.getStringExtra("type");
        if( type==null ) return;

        BLEAction action = mActionsFactory.get(type);
        if( action!=null ) {
            action.processIntent( intent, activity );
        }
    }

    /**
     * Checks the BLEKit states whether it is already started.
     *
     * @return <code>true</code> if BLEKit is started, <code>false</code> otherwise
     */
    public static boolean isStarted() {
        return _bleKit!=null && _bleKit.mBound;
    }

    /**
     * Returns a map of monitored beacons with their current proximity value.
     *
     * @return map of beacons
     */
    public static Map<String, Proximity> getCurrentBeaconStates() {
        return mCurrentBeaconsStates;
    }

    /**
     * Returns current zone configuration.
     *
     * @return current zone
     */
    public static Zone getZone() {
        return mCurrentZone;
    }




    /**
     * Starts an async task to fetch JSON.
     */
    private void fetchJson() {
        L.d(".");
        FetchJsonAsyncTask task = new FetchJsonAsyncTask();
        task.execute( jsonUrl );
    }

    private static void fetchJsonLocal() {
        L.d(".");
        AsyncTask<String, Void, JsonNode> task = new AsyncTask<String, Void, JsonNode>() {

            @Override
            protected JsonNode doInBackground(String... params) {
                L.d( "fetching from " + params[0] );
                HttpClient client = new HttpClient( params[0] );
                Response<JsonNode> response = client.get( JsonNode.class, null );
                return response.getBody();
            }

            @Override
            protected void onPostExecute(JsonNode s) {
                L.d( "fetched " + s );
                if( s!=null ) {
                    mCurrentZone = jsonParser.parse(s+"");
                }
            }
        };
        task.execute( jsonUrl );
    }


    private static void showErrorDialog(String msg, Activity activity, boolean bleUnavailable) {
        ErrorDialogFragment dlg = new ErrorDialogFragment(msg, bleUnavailable);
        dlg.show( activity.getFragmentManager(), null );
    }

    private static class ErrorDialogFragment extends DialogFragment {
        private String msg;
        private boolean bleUnavailable;

        public ErrorDialogFragment(String msg, boolean bleUnavailable) {
            this.msg = msg;
            this.bleUnavailable = bleUnavailable;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setMessage(msg)
                    .setNegativeButton(R.string.blekit_exit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().finish();
                        }
                    });
            if( !bleUnavailable ) {
                builder.setPositiveButton(R.string.blekit_turn_bt_on, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intentOpenBluetoothSettings);
                    }
                });
            }

            setCancelable(false);

            return builder.create();
        }
    }


    private void checkStarted() {
        if( mBound ) {
            throw new IllegalStateException( "BLEKit is already started, stop it first" );
        }
    }

    private static void checkInitialized() {
        if( _bleKit==null ) {
            throw new IllegalStateException( "BLEKit is not initialized, call create() first." );
        }
    }

    private void updateZone(String zoneJson) {
        if( zoneJson==null ) return;

        final Zone newZone = jsonParser.parse(zoneJson);
        BeaconPreferences.setLastZoneJson(mContext, zoneJson);
        L.d( "updateZone: " + newZone );

        if( mCurrentZone!=null ) {

            if( newZone!=null ) {
                sendUpdateBeacons( newZone.beacons );
            }

            mCurrentZone = newZone;

        } else {

            mCurrentZone = newZone;
            sendStart( mCurrentZone.beacons );

        }

        if( mZoneUpdateListener!=null ) {
            mZoneUpdateListener.onZoneUpdated(newZone);
        }
    }

    private void sendUpdateBeacons( List<Beacon> beacons ) {
        Intent intent = getServiceIntent();
        intent.putExtra(BLEKitService.Extra.EXTRA_COMMAND, BLEKitService.Extra.COMMAND_UPDATE_BEACONS);
        intent.putStringArrayListExtra(BLEKitService.Extra.EXTRA_BEACONS_LIST, beaconsToIds(beacons));
        sendCommandToService(intent);
    }

    private void sendStart( List<Beacon> beacons ) {
        Intent intent = getServiceIntent();
        intent.putExtra(BLEKitService.Extra.EXTRA_COMMAND, BLEKitService.Extra.COMMAND_START_SCAN);
        intent.putExtra(BLEKitService.Extra.EXTRA_BACKGROUND_MODE, mBackgroundMode.inBackground);
        intent.putStringArrayListExtra(BLEKitService.Extra.EXTRA_BEACONS_LIST, beaconsToIds(beacons));
        sendCommandToService(intent);
    }

    private void sendSetBackgroundMode( BackgroundMode backgroundMode ) {
        Intent intent = getServiceIntent();
        intent.putExtra(BLEKitService.Extra.EXTRA_COMMAND, BLEKitService.Extra.COMMAND_SET_BACKGROUND_MODE);
        intent.putExtra(BLEKitService.Extra.EXTRA_BACKGROUND_MODE, backgroundMode.inBackground);
        sendCommandToService(intent);
    }

    private void sendStop() {
        Intent intent = getServiceIntent();
        intent.putExtra(BLEKitService.Extra.EXTRA_COMMAND, BLEKitService.Extra.COMMAND_STOP_SCAN);
        sendCommandToService(intent);
    }

    private Intent getServiceIntent() {
        return new Intent( BLEKitService.ACTION );
    }

    private void sendCommandToService(Intent intent) {
        intent.putExtra(BLEKitService.Extra.EXTRA_CLIENT_APP_PACKAGE, mContext.getPackageName());
        mContext.startService(intent);
    }


    private ArrayList<String> beaconsToIds( List<Beacon> beacons ) {
        ArrayList<String> ids = new ArrayList<String>();
        for( Beacon beacon : beacons ) {
            ids.add(beacon.id.toLowerCase());
        }
        return ids;
    }


    private class FetchJsonAsyncTask extends AsyncTask<String, Void, JsonNode> {

        @Override
        protected JsonNode doInBackground(String... params) {
            L.d( "fetching from " + params[0] );
            HttpClient client = new HttpClient( params[0] );
            Response<JsonNode> response = client.get( JsonNode.class, null );
            return response.getBody();
        }

        @Override
        protected void onPostExecute(JsonNode s) {
            L.d( "fetched " + s );
            if( s!=null ) {
                updateZone(s+"");
            }
        }
    }



    protected interface Extra {
        public static final String EXTRA_BEACON_EVENT = "com.upnext.blekit.extra.BEACON_EVENT";
        public static final String EXTRA_BEACON_ID = "com.upnext.blekit.extra.BEACON_ID";

        public static final String EXTRA_CLIENT_ADD = "com.upnext.blekit.extra.CLIENT_ADD";
        public static final String EXTRA_CLIENT_REMOVE = "com.upnext.blekit.extra.CLIENT_REMOVE";

        public static final String EXTRA_CURRENT_BEACON_PROXIMITY = "com.upnext.blekit.extra.CURRENT_BEACON_PROXIMITY";
    }

    /**
     * Processes intent sent by BLEKitService.
     *
     * @param intent intent containing data
     * @param ctx context
     */
    protected static void processServiceEvent(Intent intent, Context ctx) {
        L.d(".");

        BLEKitClient clientAdd = intent.getParcelableExtra(Extra.EXTRA_CLIENT_ADD);
        if( clientAdd!=null ) {
            L.d(". add " + clientAdd.getPackageName() );
            BeaconPreferences.addClient( ctx, clientAdd );
            return;
        }

        BLEKitClient clientRemove = intent.getParcelableExtra(Extra.EXTRA_CLIENT_REMOVE);
        if( clientRemove!=null ) {
            L.d(". remove " + clientRemove.getPackageName() );
            BeaconPreferences.removeClient(ctx, clientRemove.getPackageName());
            return;
        }

        CurrentBeaconProximity currentBeaconProximity = intent.getParcelableExtra(Extra.EXTRA_CURRENT_BEACON_PROXIMITY);
        if( currentBeaconProximity!=null ) {
            L.d(". update proximity");
            mCurrentBeaconsStates.put( currentBeaconProximity.getBeaconId(), currentBeaconProximity.getProximity() );
            if( mStateListener!=null ) {
                mStateListener.onCurrentBeaconProximityReceived( currentBeaconProximity.getBeaconId(), currentBeaconProximity.getProximity() );
            }
            return;
        }

        //fresh start, we do not have instance - app brought from the dead
        if( _bleKit==null ) {
            L.d(". restart");
            restartBlekit(ctx);
            mEventToProcess = intent;
            return;
        }

        String event = intent.getStringExtra(Extra.EXTRA_BEACON_EVENT);
        if( event!=null ) {
            L.d(". event " + event );
            BeaconEvent beaconEvent = BeaconEvent.valueOf( event );
            String beaconId = intent.getStringExtra(Extra.EXTRA_BEACON_ID);

            mCurrentBeaconsStates.put( beaconId, Proximity.fromBeaconEvent(beaconEvent) );

            for( Beacon beacon : getBeaconsFromZone(beaconId) ) {
                _bleKit.processTriggersForBeacon( beacon, beaconEvent, ctx );
            }
        }

        mEventToProcess = null;
    }

    private static void restartBlekit(Context context) {
        Intent intnt = new Intent("com.upnext.blekit.service.RESTARTER");
        intnt.setPackage(context.getPackageName());
        if( context.getPackageManager().resolveService( intnt, 0 ) !=null ) {
            context.startService(intnt);
        }
    }

    private static List<Beacon> getBeaconsFromZone( String beaconId ) {
        List<Beacon> beacons = new ArrayList<Beacon>();
        if( mCurrentZone==null || mCurrentZone.beacons==null ) {
            return beacons;
        }
        for( Beacon beacon : mCurrentZone.beacons ) {
            if( beacon.id.equalsIgnoreCase(beaconId) ) {
                beacons.add(beacon);
            }
        }
        return beacons;
    }

    private void processTriggersForBeacon(Beacon beacon, BeaconEvent beaconEvent, Context ctx) {
        L.d( "Processing for beacon '" + beacon.name + "' " + beaconEvent );

        if( mBeaconEventListener!=null ) {
            mBeaconEventListener.onEvent(beaconEvent, beacon);
        }

        final ConditionsFactory conditionsFactory = BLEKit.getConditionsFactory();
        final ActionsFactory actionsFactory = BLEKit.getActionsFactory();

        for(Trigger trigger : beacon.triggers) {
            L.d( "Processing trigger '" + trigger.name + "'" );
            boolean conditionsMet = allConditionsMet( trigger, conditionsFactory, beaconEvent, beacon, ctx );

            if( !conditionsMet ) continue;

            //all conditions met
            performAction(actionsFactory, trigger, ctx);
        }
    }

    private void performAction(final ActionsFactory actionsFactory, Trigger trigger, Context ctx) {
        BLEAction bleAction = actionsFactory.get( trigger.action.type, trigger.action.parameters );
        if( bleAction==null ) {
            L.d("Did not find action implementation for type '" + trigger.action.type + "'");
            return;
        }

        L.d("." + mBackgroundMode.inBackground);
        bleAction.performAction(ctx, mBackgroundMode);
    }

    private boolean allConditionsMet(Trigger trigger, final ConditionsFactory conditionsFactory, BeaconEvent beaconEvent, Beacon beacon, Context ctx) {
        boolean conditionsMet = true;
        for (Condition condition : trigger.conditions) {

            BLECondition bleCondition = conditionsFactory.get(condition.type, beaconEvent, condition.parameters, condition.expression, ctx);
            if( bleCondition==null ) {
                L.d( "Did not find condition implementation for type '" + condition.type + "' and event '" + beaconEvent + "'" );
                conditionsMet = false;
                break;
            } else {

                bleCondition.setZone(mCurrentZone);
                bleCondition.setBeacon(beacon);
                bleCondition.setTrigger(trigger);

                increaseOccurence(bleCondition, beacon.id);
            }

            if( !bleCondition.conditionMet() ) {
                L.d( "Condition not met: '" + condition.type + "'" );
                conditionsMet = false;
                break;
            }
        }
        return conditionsMet;
    }

    private void increaseOccurence(BLECondition bleCondition, String beaconId) {
        if( !(bleCondition instanceof OccurenceCondition) ) return;

        OccurenceCondition condition = (OccurenceCondition) bleCondition;
        beaconsDB.addBeaconEvent( condition.getBeaconEvent(), beaconId );

        condition.setBeaconsDB(beaconsDB);
        condition.setBeaconId(beaconId);
    }

}
