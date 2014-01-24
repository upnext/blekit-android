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

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.conditions.BLECondition;
import com.upnext.blekit.conditions.OccurenceCondition;
import com.upnext.blekit.listeners.BeaconEventListener;
import com.upnext.blekit.listeners.ZoneUpdateListener;
import com.upnext.blekit.model.Beacon;
import com.upnext.blekit.model.Condition;
import com.upnext.blekit.model.Trigger;
import com.upnext.blekit.model.Zone;
import com.upnext.blekit.util.BeaconsDB;
import com.upnext.blekit.util.JsonParser;
import com.upnext.blekit.util.L;
import com.upnext.blekit.util.Rand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for beacon scanning.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class BLEKitService extends Service implements IBeaconConsumer {

    protected static final int MSG_UPDATE_SCAN_PERIODS = 1;
    protected static final int MSG_UPDATE_ZONE = 2;
    protected static final int MSG_SET_BACKGROUND_MODE = 3;
    protected static final int MSG_PROCESS_EVENTS_FOR_ACTION_TYPE = 4;
    protected static final int MSG_SET_BEACON_EVENT_LISTENER = 5;
    protected static final int MSG_SET_ZONE_LISTENER = 6;
    protected static final int MSG_DISABLE_ACTION_PROCESSING = 8;

    /**
     * After first LEAVE envent if no ENTER is seen in this amount of time, a proper LEAVE will be sent.
     */
    public static final long LEAVE_MSG_DELAY_MILLIS = 15000;

    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private boolean mBeaconManagerConnected = false;
    private JsonParser jsonParser = new JsonParser();

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private Zone mCurrentZone = null;
    private Map<String, AverageIBeacon> mMonitoredBeacons = new HashMap<String, AverageIBeacon>();
    private int mBindCount = 0;
    private BeaconsDB beaconsDB;
    private BackgroundMode backgroundMode = new BackgroundMode(null, false);

    private EnterLeaveDelayedHandler mEnterLeaveHandler = new EnterLeaveDelayedHandler();
    private Set<String> mRegionsToLeave = new HashSet<String>();

    private BeaconEventListener mBeaconEventListener;
    private ZoneUpdateListener mZoneUpdateListener;

    private boolean mActionProcessingDisabled = false;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SCAN_PERIODS:
                    BLEKitScanPeriods scanPeriods = (BLEKitScanPeriods) msg.obj;
                    updateScanPeriods(scanPeriods);
                    break;

                case MSG_UPDATE_ZONE:
                    String zoneJson = (String)msg.obj;
                    updateZone( zoneJson );
                    break;

                case MSG_SET_BACKGROUND_MODE:
                    BackgroundMode inBackground = (BackgroundMode) msg.obj;
                    setBackgroundMode( inBackground );
                    break;

                case MSG_PROCESS_EVENTS_FOR_ACTION_TYPE:
                    String actionType = (String) msg.obj;
                    ProcessActionsForEventTypeAsyncTask task = new ProcessActionsForEventTypeAsyncTask();
                    task.execute(actionType);
                    break;

                case MSG_SET_BEACON_EVENT_LISTENER:
                    mBeaconEventListener = (BeaconEventListener) msg.obj;
                    break;

                case MSG_SET_ZONE_LISTENER:
                    mZoneUpdateListener = (ZoneUpdateListener) msg.obj;
                    break;

                case MSG_DISABLE_ACTION_PROCESSING:
                    mActionProcessingDisabled = (Boolean)msg.obj;
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void setBackgroundMode(BackgroundMode mode) {
        L.d("setBackgroundMode " + mode.inBackground);
        backgroundMode = mode;
        iBeaconManager.setBackgroundMode( this, mode.inBackground );
    }

    class EnterLeaveDelayedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Region region = (Region) msg.obj;

            if( msg.what == BeaconEvent.REGION_ENTER.ordinal() ) {

                if( !mRegionsToLeave.remove(region.getUniqueId()) ) {
                    processEvent(BeaconEvent.REGION_ENTER, region);
                }

            } else if( msg.what == BeaconEvent.REGION_LEAVE.ordinal() ) {

                if( mRegionsToLeave.remove(region.getUniqueId()) ) {
                    mMonitoredBeacons.remove(region.getUniqueId());
                    processEvent(BeaconEvent.REGION_LEAVE, region);
                }

            } else {
                super.handleMessage(msg);
            }
        }
    }

    private void updateScanPeriods(BLEKitScanPeriods scanPeriods) {
        L.d( "updateScanPeriods: " + scanPeriods );
        iBeaconManager.setForegroundScanPeriod(scanPeriods.foregroundScanPeriod);
        iBeaconManager.setForegroundBetweenScanPeriod(scanPeriods.foregroundIdlePeriod);
        iBeaconManager.setBackgroundScanPeriod(scanPeriods.backgroundScanPeriod);
        iBeaconManager.setBackgroundBetweenScanPeriod(scanPeriods.backgroundIdlePeriod);
        try {
            iBeaconManager.setScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateZone(String zoneJson) {
        final Zone newZone = jsonParser.parse(zoneJson);
        L.d( "updateZone: " + newZone );

        // we have previous zone, need to check if any beacons have changed (added/removed)
        if( mCurrentZone!=null ) {

            List<Beacon> removedBeacons = diff( mCurrentZone, newZone );
            stopScanningZones( removedBeacons );

            List<Beacon> newBeacons = diff( newZone, mCurrentZone );
            startScanningZones( newBeacons );

            mCurrentZone = newZone;

        } else {

            mCurrentZone = newZone;
            startScanningZones();

        }

        if( mZoneUpdateListener!=null ) {
            mZoneUpdateListener.onZoneUpdated(newZone);
        }
    }

    private List<Beacon> diff(Zone zone1, Zone zone2) {
        if( zone1==null || zone1.beacons==null ) {
            return null;
        }
        if( zone2==null || zone2.beacons==null ) {
            return zone1.beacons;
        }

        final List<Beacon> diffBeacons = new ArrayList<Beacon>(zone1.beacons);
        diffBeacons.removeAll(zone2.beacons);

        return diffBeacons;
    }

    private void stopScanningZones(List<Beacon> beacons) {
        if( beacons==null || iBeaconManager==null ) return;

        for( Beacon beacon : beacons ) {
            for( String monitoringUniqueId : mMonitoredBeacons.keySet() ) {
                if( monitoringUniqueId.toLowerCase().startsWith( beacon.id.toLowerCase() ) ) {
                    L.d( "stopScanningZone " + monitoringUniqueId );
                    try {
                        iBeaconManager.stopMonitoringBeaconsInRegion( new Region(monitoringUniqueId, null, null, null) );
                        iBeaconManager.stopRangingBeaconsInRegion(new Region(monitoringUniqueId, null, null, null));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void stopScanningZones() {
        if( iBeaconManager==null ) return;

        for( String monitoringUniqueId : mMonitoredBeacons.keySet() ) {
            L.d( "stopScanningZones " + monitoringUniqueId );
            try {
                iBeaconManager.stopMonitoringBeaconsInRegion( new Region(monitoringUniqueId, null, null, null) );
                iBeaconManager.stopRangingBeaconsInRegion(new Region(monitoringUniqueId, null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mMonitoredBeacons.clear();
    }

    private void startScanningZones(List<Beacon> beacons) {
        if( beacons==null ) return;

        for( Beacon beacon : beacons ) {
            String monitoringId = beacon.id + Rand.nextLong();
            Region region = new Region( monitoringId, beacon.getProximityUid(), beacon.getMajor(), beacon.getMinor() );
            L.d( "startScanningZone " + region.toString() );
            mMonitoredBeacons.put( monitoringId, null );
            try {
                iBeaconManager.startMonitoringBeaconsInRegion( region );
                iBeaconManager.startRangingBeaconsInRegion( region );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void startScanningZones() {
        if( mCurrentZone==null || iBeaconManager==null || !mBeaconManagerConnected || !mMonitoredBeacons.isEmpty()) return;

        startScanningZones( mCurrentZone.beacons );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        beaconsDB = new BeaconsDB(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onIBeaconServiceConnect() {
        L.d( "onIBeaconServiceConnect" );
        mBeaconManagerConnected = true;
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                L.d(".");
                Message msg = Message.obtain();
                msg.what = BeaconEvent.REGION_ENTER.ordinal();
                msg.obj = region;
                mEnterLeaveHandler.sendMessage(msg);
            }

            @Override
            public void didExitRegion(Region region) {
                L.d(".");
                Message msg = Message.obtain();
                msg.what = BeaconEvent.REGION_LEAVE.ordinal();
                msg.obj = region;
                mEnterLeaveHandler.sendMessageDelayed( msg, LEAVE_MSG_DELAY_MILLIS );
                mRegionsToLeave.add( region.getUniqueId() );
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                //not used
            }
        });

        iBeaconManager.setRangeNotifier( new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {

                //for compliance with iOS version of the library ranging events are disabled for background mode
                if( backgroundMode.inBackground ) {
                    return;
                }


                if( iBeacons==null || iBeacons.isEmpty() ) return;

                AverageIBeacon currBeacon = mMonitoredBeacons.get( region.getUniqueId() );

                //include only closest
                IBeacon closestBeacon = getClosestBeacon( iBeacons );

                if( currBeacon == null ) {

                    currBeacon = new AverageIBeacon(closestBeacon);
                    mMonitoredBeacons.put( region.getUniqueId(), currBeacon );

                } else {

                    currBeacon.update( closestBeacon );

                    if( currBeacon.isProximityReady() ) {
                        currBeacon.approximate();

                        if( currBeacon.proximityChanged() ) {
                            processProximity(currBeacon.getProximity(), region);

                            currBeacon.setPrevProximity(currBeacon.getProximity());
                        }

                    }
                }

            }
        });

        startScanningZones();
    }

    private void processEventsForActionType( String actionType ) {
        if( actionType==null ) return;
        if( mCurrentZone!=null ) {

            final ConditionsFactory conditionsFactory = BLEKit.getConditionsFactory();
            final ActionsFactory actionsFactory = BLEKit.getActionsFactory();

            for( Beacon beacon : mCurrentZone.beacons ) {
                for( Trigger trigger : beacon.triggers ) {
                    if( actionType.equals(trigger.action.type) ) {

                        for( String regionId : mMonitoredBeacons.keySet() ) {
                            if( regionId.toLowerCase().startsWith(beacon.id.toLowerCase()) ) {

                                IBeacon closestBeacon = mMonitoredBeacons.get(regionId);

                                if( allConditionsMet(trigger, conditionsFactory, BeaconEvent.REGION_ENTER, beacon) ) {
                                    performAction( actionsFactory, trigger);
                                }

                                if( closestBeacon!=null ) {
                                    BeaconEvent beaconEvent = BeaconEvent.fromIBeaconProximity( closestBeacon );
                                    if( beaconEvent!=null && allConditionsMet(trigger, conditionsFactory, beaconEvent, beacon) ) {
                                        performAction( actionsFactory, trigger);
                                    }
                                }

                            }
                        }

                    }
                }
            }
        }
    }

    private IBeacon getClosestBeacon( Collection<IBeacon> iBeacons ) {
        if( iBeacons==null || iBeacons.isEmpty() ) return null;
        final List<IBeacon> list = (List<IBeacon>) iBeacons;
        IBeacon closest = list.get(0);
        for( IBeacon beacon : list ) {
            if( beacon.getAccuracy()>=0 && beacon.getAccuracy()<closest.getAccuracy() ) {
                closest = beacon;
            }
        }
        return closest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        L.d("onBind " + ++mBindCount);
        iBeaconManager.bind(this);
        return mMessenger.getBinder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onUnbind(Intent intent) {
        L.d("onUnbind " + --mBindCount);
        stopScanningZones();
        iBeaconManager.unBind(this);
        mBeaconManagerConnected = false;
        return super.onUnbind(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        L.d("onDestroy");
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    private void processProximity(final int newProximity, final Region region) {
        //L.d( oldProximity + " " + newProximity + " " + region.getMajor()+"-"+region.getMinor() );

        final Thread processor = new Thread( new Runnable() {
            @Override
            public void run() {
                if( mCurrentZone==null || !mCurrentZone.containsMatchingBeacon(region) ) {
                    //L.d("zone does not contain region " + region.toString() );
                    return;
                }

                //L.d("zone contains beacons");

                for( Beacon beacon : mCurrentZone.getMatchingBeacons(region) ) {
                    switch (newProximity) {
                        case IBeacon.PROXIMITY_FAR:
                            processTriggersForBeacon( beacon, BeaconEvent.CAME_FAR );
                            break;

                        case IBeacon.PROXIMITY_NEAR:
                            processTriggersForBeacon( beacon, BeaconEvent.CAME_NEAR );
                            break;

                        case IBeacon.PROXIMITY_IMMEDIATE:
                            processTriggersForBeacon( beacon, BeaconEvent.CAME_IMMEDIATE );
                            break;
                    }

                }
            }
        });
        processor.start();
    }

    private void processEvent( final BeaconEvent beaconEvent, final Region region ) {
        L.d( beaconEvent.name() + " " + region.getMajor()+"-"+region.getMinor() );

        final Thread processor = new Thread( new Runnable() {
            @Override
            public void run() {
                if( mCurrentZone==null || !mCurrentZone.containsMatchingBeacon(region) ) {
                    L.d("zone does not contain region " + region.toString() );
                    return;
                }

                L.d("zone contains beacons");

                for( Beacon beacon : mCurrentZone.getMatchingBeacons(region) ) {
                    processTriggersForBeacon( beacon, beaconEvent );
                }
            }
        });
        processor.start();
    }

    private void processTriggersForBeacon(Beacon beacon, BeaconEvent beaconEvent) {
        L.d( "Processing for beacon '" + beacon.name + "' " + beaconEvent );

        //TODO maybe spawn another thread?
        if( mBeaconEventListener!=null ) {
            mBeaconEventListener.onEvent( beaconEvent, beacon );
        }

        final ConditionsFactory conditionsFactory = BLEKit.getConditionsFactory();
        final ActionsFactory actionsFactory = BLEKit.getActionsFactory();

        for(Trigger trigger : beacon.triggers) {
            L.d( "Processing trigger '" + trigger.name + "'" );
            boolean conditionsMet = allConditionsMet( trigger, conditionsFactory, beaconEvent, beacon );

            if( !conditionsMet ) continue;

            //all conditions met

            performAction(actionsFactory, trigger);
        }
    }

    private void performAction(final ActionsFactory actionsFactory, Trigger trigger) {
        BLEAction bleAction = actionsFactory.get( trigger.action.type, trigger.action.parameters );
        if( bleAction==null ) {
            L.d("Did not find action implementation for type '" + trigger.action.type + "'");
            return;
        }

        if( !mActionProcessingDisabled ) {
            bleAction.performAction(this, backgroundMode);
        }
    }

    private boolean allConditionsMet(Trigger trigger, final ConditionsFactory conditionsFactory, BeaconEvent beaconEvent, Beacon beacon) {
        boolean conditionsMet = true;
        for (Condition condition : trigger.conditions) {

            BLECondition bleCondition = conditionsFactory.get(condition.type, beaconEvent, condition.parameters, condition.expression, this);
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

    private class ProcessActionsForEventTypeAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            processEventsForActionType( params[0] );
            return null;
        }
    }

}
