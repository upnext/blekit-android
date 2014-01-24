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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.fasterxml.jackson.databind.JsonNode;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.conditions.BLECondition;
import com.upnext.blekit.listeners.BLEKitStateListener;
import com.upnext.blekit.listeners.BeaconEventListener;
import com.upnext.blekit.listeners.ZoneUpdateListener;
import com.upnext.blekit.util.L;
import com.upnext.blekit.util.http.HttpClient;
import com.upnext.blekit.util.http.Response;

import java.net.MalformedURLException;
import java.net.URL;

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
 * .setScanPeriods(new BLEKitScanPeriods(1100, 1000, 1100, 15000))
 * .addAction(new CustomAction())
 * .addCondition(new CustomCondition())
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
 * @see com.upnext.blekit.BLEKitScanPeriods
 */
public class BLEKit {

    private static BLEKit _bleKit;
    private static ConditionsFactory mConditionsFactory;
    private static ActionsFactory mActionsFactory;

    private String jsonUrl;
    private String jsonContent;
    private BLEKitScanPeriods scanPeriods = new BLEKitScanPeriods(30000, 100, 30000, 100);

    private Messenger mBLEKitService = null;
    private boolean mBound;
    private BackgroundMode mBackgroundMode = new BackgroundMode(null, false);

    private Class targetActivityForNotifications;

    private BeaconEventListener mBeaconEventListener;
    private ZoneUpdateListener mZoneUpdateListener;
    private BLEKitStateListener mStateListener;

    private boolean mActionProcessingDisabled = false;

    private BLEKit() {
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

        //stop service if it is running
        if( _bleKit !=null && _bleKit.mBound ) {
            stop(context);
        }

        _bleKit = new BLEKit();

        mConditionsFactory = new ConditionsFactory();
        mActionsFactory = new ActionsFactory();

        return _bleKit;
    }

    /**
     * Starts BLEKit service.
     *
     * Fetches JSON configuration from given URL if provided.
     *
     * @param context context (either from activity or service)
     * @throws IllegalArgumentException thrown if configuration JSON was not provided (jsonUrl or jsonContent).
     */
    public void start( Context context ) throws IllegalArgumentException {
        L.d( "start" );

        if( jsonContent==null && jsonUrl==null ) {
            throw new IllegalArgumentException( "You have to provide either jsonUrl or jsonContent" );
        }

        if( jsonUrl!=null ) {
            fetchJson();
        }

        context.bindService(new Intent(context, BLEKitService.class), mBLEKitServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Provide url with JSON configuration.
     * BLEKit should be created previously (#see BLEKit.create)
     *
     * If BLEKit is already started, then it fetches the JSON from given url and reloads the configuration.
     *
     * @param jsonUrl url pointing to JSON configuration
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     * @throws java.net.MalformedURLException thrown if given url is malformed.
     */
    public static BLEKit setJsonUrl( String jsonUrl ) throws IllegalStateException, MalformedURLException {
        checkInitialized();

        //validate URL
        new URL(jsonUrl);

        _bleKit.jsonUrl = jsonUrl;

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
     * @param jsonContent String containing JSON configuration
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized
     */
    public static BLEKit setJsonContent( String jsonContent ) throws IllegalStateException {
        checkInitialized();
        _bleKit.jsonContent = jsonContent;

        if( _bleKit.mBound ) {
            _bleKit.updateZone(jsonContent);
        }

        return _bleKit;
    }

    /**
     * If a JSON url was provided previously (and was valid), then it is fetched again and all actions are triggered.
     *
     * @throws IllegalStateException thrown if BLEKit was not initialized
     */
    public static void reloadJson() throws IllegalStateException {
        checkInitialized();

        if(_bleKit.jsonUrl==null) return;

        try {
            new URL(_bleKit.jsonUrl);
        } catch (Exception e) {
            //if url is malformed, cosume error silently
            return;
        }

        if( _bleKit.mBound ) {
            _bleKit.fetchJson();
        }
    }

    /**
     * Can be used to force process all triggers fro given action type.
     *
     * @param actionType type of action ({@link com.upnext.blekit.actions.BLEAction#getType()}
     * @throws IllegalStateException thrown if BLEKit was not initialized
     */
    public static void processEventsForActionType( String actionType ) throws IllegalStateException {
        checkInitialized();
        _bleKit.sendProcessEventsForActionType(actionType);
    }

    /**
     * Stops BLEKit service if it is running.
     *
     * @param context context (either from activity or service)
     */
    public static void stop( Context context ) {
        L.d("stop");
        if( _bleKit!=null && _bleKit.mBound ) {
            context.unbindService(_bleKit.mBLEKitServiceConnection);
            _bleKit.mBound = false;

            if( _bleKit.mStateListener!=null ) {
                _bleKit.mStateListener.onBLEKitStopped();
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
     * Set scan periods for foreground and background states.
     * BLEKit should be initialized first.
     *
     * @param scanPeriods scan periods
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setScanPeriods( BLEKitScanPeriods scanPeriods ) throws IllegalStateException {
        checkInitialized();

        _bleKit.scanPeriods = scanPeriods;
        if( _bleKit.mBound ) {
            _bleKit.updateScanPeriods();
        }
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
        checkInitialized();

        if( !background && activity==null ) {
            throw new IllegalArgumentException( "Activity is null for foreground" );
        }

        _bleKit.mBackgroundMode = new BackgroundMode( activity, background );
        if( _bleKit.mBound ) {
            _bleKit.updateBackgroundMode();
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
        checkInitialized();

        _bleKit.mBeaconEventListener = listener;
        if( _bleKit.mBound ) {
            _bleKit.updateEventListener(listener);
        }
        return _bleKit;
    }

    /**
     * Sets listener for configuration (zone) changes - eg. when a new configuration is fetched (after BLEKit start or after manual call to {@link #reloadJson()})
     *
     * @param listener zone update listener
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setZoneUpdateListener( ZoneUpdateListener listener ) throws IllegalStateException {
        checkInitialized();

        _bleKit.mZoneUpdateListener = listener;
        if( _bleKit.mBound ) {
            _bleKit.updateZoneListener(listener);
        }
        return _bleKit;
    }

    /**
     * Provides the functionality to temporarily disable action processing.
     * By default action processing is enabled.
     *
     * @param disableActionProcessing if <code>true</code>, action processing will be disabled; if <code>false</code>, action processing will be enabled
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit disableActionProcessing( boolean disableActionProcessing ) throws IllegalStateException {
        checkInitialized();

        _bleKit.mActionProcessingDisabled = disableActionProcessing;
        if( _bleKit.mBound ) {
            _bleKit.updateActionProcessing(disableActionProcessing);
        }
        return _bleKit;
    }

    /**
     * Sets listener for BLEKit states - when in starts and stops.
     *
     * @param listener state listener
     * @return BLEKit instance
     * @throws IllegalStateException thrown if BLEKit was not initialized.
     */
    public static BLEKit setStateListener( BLEKitStateListener listener ) throws IllegalStateException {
        checkInitialized();
        _bleKit.mStateListener = listener;
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
     * Starts an async task to fetch JSON.
     */
    private void fetchJson() {
        FetchJsonAsyncTask task = new FetchJsonAsyncTask();
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


    private void updateBackgroundMode() {
        if( mBackgroundMode==null ) return;
        checkRunning();
        send( BLEKitService.MSG_SET_BACKGROUND_MODE, mBackgroundMode );
    }

    private void updateScanPeriods() {
        if( scanPeriods==null ) return;
        checkRunning();
        send( BLEKitService.MSG_UPDATE_SCAN_PERIODS, scanPeriods );
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

    private void checkRunning() {
        if( mBLEKitService==null ) {
            throw new IllegalStateException( "BLEKit service is not running. Start it first." );
        }
    }

    private void updateZone( String zoneJSON ) {
        checkRunning();
        send( BLEKitService.MSG_UPDATE_ZONE, zoneJSON );
    }

    private void sendProcessEventsForActionType(String actionType) {
        checkRunning();
        send( BLEKitService.MSG_PROCESS_EVENTS_FOR_ACTION_TYPE, actionType );
    }

    private void updateEventListener(BeaconEventListener listener) {
        checkRunning();
        send( BLEKitService.MSG_SET_BEACON_EVENT_LISTENER, listener );
    }

    private void updateZoneListener(ZoneUpdateListener listener) {
        checkRunning();
        send( BLEKitService.MSG_SET_ZONE_LISTENER, listener );
    }

    private void updateActionProcessing(boolean disabled) {
        checkRunning();
        send( BLEKitService.MSG_DISABLE_ACTION_PROCESSING, Boolean.valueOf(disabled) );
    }

    private ServiceConnection mBLEKitServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            L.d( "onServiceConnected" );
            mBLEKitService = new Messenger(service);
            mBound = true;

            updateScanPeriods();
            updateBackgroundMode();
            updateEventListener(mBeaconEventListener);
            updateZoneListener(mZoneUpdateListener);
            updateActionProcessing(mActionProcessingDisabled);

            if( jsonContent!=null )
                updateZone( jsonContent );

            if( mStateListener!=null ) {
                mStateListener.onBLEKitStarted();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            L.d( "onServiceDisconnected" );
            mBLEKitService = null;
            mBound = false;

            if( mStateListener!=null ) {
                mStateListener.onBLEKitStopped();
            }
        }
    };

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

    private void send( int msgID, Object object ) {
        Message msg = Message.obtain(null, msgID, object);
        try {
            mBLEKitService.send( msg );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
