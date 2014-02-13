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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.upnext.blekit.model.Beacon;
import com.upnext.blekit.model.CurrentBeaconProximity;
import com.upnext.blekit.util.BeaconPreferences;
import com.upnext.blekit.util.L;
import com.upnext.blekit.util.Rand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

    public static final String ACTION = "com.upnext.blekit.BLEKitService";

    public interface Extra {
        public static final String EXTRA_COMMAND = "com.upnext.blekit.extra_command";
        public static final String EXTRA_CLIENT_APP_PACKAGE = "com.upnext.blekit.client_app_package";
        public static final String EXTRA_BACKGROUND_MODE = "com.upnext.blekit.background_mode";
        public static final String EXTRA_BEACONS_LIST = "com.upnext.blekit.beacons_list";

        public static final int COMMAND_START_SCAN = 1;
        public static final int COMMAND_STOP_SCAN = 2;
        public static final int COMMAND_SET_BACKGROUND_MODE = 3;
        public static final int COMMAND_UPDATE_BEACONS = 4;
        public static final int COMMAND_HEALTHCHECK = 5;
    }


    private IBeaconManager iBeaconManager;
    private boolean mBeaconManagerConnected = false;

    private Map<String, AverageIBeacon> mMonitoredRegionsUniqueIds = new HashMap<String, AverageIBeacon>();
    private Map<String, Proximity> mMonitoredBeaconIds = new HashMap<String, Proximity>();

    private EnterLeaveDelayedHandler mEnterLeaveHandler = new EnterLeaveDelayedHandler();
    private Set<String> mRegionsToLeave = new HashSet<String>();

    private boolean mAnyClientInForeground = true;

    private Map<String, BLEKitClient> clients = new HashMap<String, BLEKitClient>();

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( intent==null ) {
            return START_STICKY;
        }

        int command = intent.getIntExtra(Extra.EXTRA_COMMAND, -1);
        L.d(". " + command );

        switch (command) {

            case Extra.COMMAND_START_SCAN:
                processStartCommand(
                        intent.getStringExtra(Extra.EXTRA_CLIENT_APP_PACKAGE),
                        intent.getBooleanExtra(Extra.EXTRA_BACKGROUND_MODE, true),
                        intent.getStringArrayListExtra(Extra.EXTRA_BEACONS_LIST)
                );
                break;

            case Extra.COMMAND_STOP_SCAN:
                processStopCommand(intent.getStringExtra(Extra.EXTRA_CLIENT_APP_PACKAGE));
                break;

            case Extra.COMMAND_SET_BACKGROUND_MODE:
                setBackgroundMode(
                        intent.getStringExtra(Extra.EXTRA_CLIENT_APP_PACKAGE),
                        intent.getBooleanExtra(Extra.EXTRA_BACKGROUND_MODE, true)
                );
                break;

            case Extra.COMMAND_UPDATE_BEACONS:
                updateBeacons(
                        intent.getStringExtra(Extra.EXTRA_CLIENT_APP_PACKAGE),
                        intent.getStringArrayListExtra(Extra.EXTRA_BEACONS_LIST)
                );
                break;

            case Extra.COMMAND_HEALTHCHECK:
                discardOldBeacons();
                break;

            default:
                super.onStartCommand(intent, flags, startId);
        }

        return START_STICKY;
    }

    private void updateBeacons(String packageName, ArrayList<String> beaconIds) {
        if( packageName==null ) return;

        if( clients.containsKey(packageName)) {
            updateClient(packageName, beaconIds);
        } else {
            addClient(packageName, false, beaconIds);
        }
    }

    private void processStartCommand( String packageName, boolean inBackground, List<String> beaconIDs ) {
        L.d(".");
        if( packageName==null || beaconIDs==null || beaconIDs.isEmpty() ) {
            return;
        }

        if( clients.containsKey(packageName) ) {
            updateClient( packageName, inBackground, beaconIDs );
        } else {
            addClient( packageName, inBackground, beaconIDs );
        }
    }

    private void updateClient(String packageName, boolean inBackground, List<String> beaconIDs) {
        L.d(".");
        final BLEKitClient client = clients.get(packageName);

        updateMonitoredBeacons( beaconIDs, client );

        client.setInBackground(inBackground);
        client.setMonitoredBeaconIDs(beaconIDs);

        updateBackgroundMode();
    }

    private void updateClient(String packageName, List<String> beaconIDs) {
        L.d(".");
        final BLEKitClient client = clients.get(packageName);
        updateMonitoredBeacons(beaconIDs, client);
        client.setMonitoredBeaconIDs(beaconIDs);

        sendCurrentStateForBeacons(packageName);
    }

    private void addClient(String packageName, boolean inBackground, List<String> beaconIDs) {
        L.d(".");
        Set<String> idsToAdd = new HashSet<String>(beaconIDs);
        idsToAdd.removeAll(mMonitoredBeaconIds.keySet());

        if( !idsToAdd.isEmpty() ) {
            for( String id : idsToAdd ) {
                mMonitoredBeaconIds.put(id.toLowerCase(), Proximity.UNKNOWN);
            }
            startScanningZoneForBeaconIds(idsToAdd);
        }

        BLEKitClient client = new BLEKitClient(packageName, inBackground, new HashSet<String>(beaconIDs));
        clients.put( packageName, client );

        updateBackgroundMode();

        sendToAllClients( BLEKit.Extra.EXTRA_CLIENT_ADD, client );
        sendCurrentStateForBeacons(packageName);
    }

    private void updateMonitoredBeacons(List<String> beaconIDs, BLEKitClient client) {
        Set<String> idsToRemove = new HashSet<String>(client.getMonitoredBeaconIDs());
        idsToRemove.removeAll(beaconIDs);

        if( !idsToRemove.isEmpty() ) {
            removeBeaconsFromScan(idsToRemove);
        }

        Set<String> idsToAdd = new HashSet<String>(beaconIDs);
        idsToAdd.removeAll(client.getMonitoredBeaconIDs());
        idsToAdd.removeAll(mMonitoredBeaconIds.keySet());

        if( !idsToAdd.isEmpty() ) {
            for( String id : idsToAdd ) {
                mMonitoredBeaconIds.put(id.toLowerCase(), Proximity.UNKNOWN);
            }
            startScanningZoneForBeaconIds(idsToAdd);
        }
    }

    private void removeBeaconsFromScan( Set<String> idsToRemove ) {
        L.d("." + idsToRemove);
        for( String idToRemove : idsToRemove ) {
            boolean idFound = false;
            for( String clientPkg : clients.keySet() ) {
                final Set<String> clientBeacons = clients.get(clientPkg).getMonitoredBeaconIDs();
                if( clientBeacons!=null && !clientBeacons.isEmpty() ) {
                    if( clientBeacons.contains(idToRemove) ) {
                        idFound = true;
                        break;
                    }
                }
            }

            if(!idFound) {
                mMonitoredBeaconIds.remove(idToRemove);
                stopScanningZoneForBeaconId(idToRemove);
            }
        }
    }


    private void processStopCommand( String packageName ) {
        L.d(".");
        final BLEKitClient client = clients.remove(packageName);
        if( client!=null ) {
            Set<String> idsToRemove = new HashSet<String>(client.getMonitoredBeaconIDs());
            removeBeaconsFromScan(idsToRemove);
            sendToAllClients( BLEKit.Extra.EXTRA_CLIENT_REMOVE, client );
        }
    }

    private void sendToAllClients(String command, BLEKitClient client) {
        for( String pkg : clients.keySet() ) {
            clients.get(pkg).call( this, command, client );
        }
    }

    private void sendCurrentStateForBeacons(String packageName) {
        final BLEKitClient client = clients.get(packageName);
        if( client!=null ) {
            for( String beaconId : client.getMonitoredBeaconIDs() ) {
                if( mMonitoredBeaconIds.containsKey(beaconId) ) {
                    client.call( this, BLEKit.Extra.EXTRA_CURRENT_BEACON_PROXIMITY, new CurrentBeaconProximity(beaconId, mMonitoredBeaconIds.get(beaconId)));
                }
            }
        }
    }

    private void setBackgroundMode( String packageName, boolean inBackground ) {
        final BLEKitClient client = clients.get(packageName);
        if( client!=null ) {
            client.setInBackground(inBackground);
        }

        updateBackgroundMode();
    }

    private void updateBackgroundMode() {
        boolean anyInForeground = false;
        for( String pkg : clients.keySet() ) {
            if( !clients.get(pkg).isInBackground() ) {
                anyInForeground = true;
                break;
            }
        }

        if( anyInForeground != mAnyClientInForeground ) {
            mAnyClientInForeground = anyInForeground;
            iBeaconManager.setBackgroundMode( this, !mAnyClientInForeground );
        }
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
                    mMonitoredRegionsUniqueIds.remove(region.getUniqueId());
                    processEvent(BeaconEvent.REGION_LEAVE, region);
                }

            } else {
                super.handleMessage(msg);
            }
        }
    }

    private void stopScanningZoneForBeaconId( String beaconId ) {
        List<Beacon> beacons = new ArrayList<Beacon>();
        Beacon b = new Beacon();
        b.id = beaconId;
        beacons.add( b );
        stopScanningZones(beacons);
        persistBeaconStates();
    }

    private void stopScanningZones(List<Beacon> beacons) {
        if( beacons==null || iBeaconManager==null ) return;

        for( Beacon beacon : beacons ) {
            Set<String> idsToRemove = new HashSet<String>();
            for( String monitoringUniqueId : mMonitoredRegionsUniqueIds.keySet() ) {
                if( monitoringUniqueId.toLowerCase().startsWith( beacon.id.toLowerCase() ) ) {
                    L.d( "stopScanningZone " + monitoringUniqueId );
                    try {
                        iBeaconManager.stopMonitoringBeaconsInRegion( new Region(monitoringUniqueId, null, null, null) );
                        iBeaconManager.stopRangingBeaconsInRegion(new Region(monitoringUniqueId, null, null, null));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    idsToRemove.add(monitoringUniqueId);
                }
            }
            for( String id : idsToRemove ) {
                mMonitoredRegionsUniqueIds.remove(id);
            }
        }
    }

    private void stopScanningZones() {
        if( iBeaconManager==null ) return;

        for( String monitoringUniqueId : mMonitoredRegionsUniqueIds.keySet() ) {
            L.d( "stopScanningZones " + monitoringUniqueId );
            try {
                iBeaconManager.stopMonitoringBeaconsInRegion( new Region(monitoringUniqueId, null, null, null) );
                iBeaconManager.stopRangingBeaconsInRegion(new Region(monitoringUniqueId, null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mMonitoredRegionsUniqueIds.clear();
        mMonitoredBeaconIds.clear();
    }

    private void startScanningZoneForBeaconIds( Set<String> beaconIds ) {
        L.d(".");
        List<Beacon> beacons = new ArrayList<Beacon>();
        for( String id : beaconIds ) {
            Beacon b = new Beacon();
            b.id = id;
            beacons.add( b );
        }
        startScanningZones(beacons);
        persistBeaconStates();
    }

    private void startScanningZones(List<Beacon> beacons) {
        L.d(". " + mBeaconManagerConnected);
        if( beacons==null || !mBeaconManagerConnected ) return;

        for( Beacon beacon : beacons ) {
            String monitoringId = beacon.id + Rand.nextLong();
            mMonitoredRegionsUniqueIds.put(monitoringId, null);
            Region region = new Region( monitoringId, beacon.getProximityUid(), beacon.getMajor(), beacon.getMinor() );
            L.d( "startScanningZone " + region.toString() );
            try {
                iBeaconManager.startMonitoringBeaconsInRegion( region );
                iBeaconManager.startRangingBeaconsInRegion( region );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void startScanningZones() {
        if( iBeaconManager==null || !mBeaconManagerConnected || mMonitoredBeaconIds.isEmpty()) return;

        startScanningZoneForBeaconIds(mMonitoredBeaconIds.keySet());
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
                sendDelayedEnter(region);
            }

            @Override
            public void didExitRegion(Region region) {
                sendDelayedLeave(region);
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                //not used
            }
        });

        iBeaconManager.setRangeNotifier( new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {

                //cancel any leave events that are in the leave queue
                if( iBeacons!=null && !iBeacons.isEmpty() ) {
                    mRegionsToLeave.remove(region.getUniqueId());
                }

                //precaution for cached beacon proximities
                updateLastSeenValues(iBeacons, region);

                //for compliance with iOS version of the library ranging events are disabled for background mode
                if( !mAnyClientInForeground ) {
                    return;
                }


                if( iBeacons==null || iBeacons.isEmpty() ) return;

                AverageIBeacon averageBeacon = mMonitoredRegionsUniqueIds.get( region.getUniqueId() );

                //include only closest
                IBeacon closestBeacon = getClosestBeacon( iBeacons );

                if( averageBeacon == null ) {

                    averageBeacon = new AverageIBeacon(closestBeacon);
                    mMonitoredRegionsUniqueIds.put(region.getUniqueId(), averageBeacon);

                } else {

                    averageBeacon.update(closestBeacon);

                    if( averageBeacon.isProximityReady() ) {
                        averageBeacon.approximate();

                        if( averageBeacon.proximityChanged() ) {
                            processProximity(averageBeacon.getProximity(), region);

                            averageBeacon.setPrevProximity(averageBeacon.getProximity());
                        }

                    }
                }

            }
        });

        iBeaconManager.setForegroundScanPeriod(Config.FOREGROUND_SCAN_DURATION_MILLIS);
        iBeaconManager.setForegroundBetweenScanPeriod(Config.FOREGROUND_PAUSE_DURATION_MILLIS);
        iBeaconManager.setBackgroundScanPeriod(Config.BACKGROUND_SCAN_DURATION_MILLIS);
        iBeaconManager.setBackgroundBetweenScanPeriod(Config.BACKGROUND_PAUSE_DURATION_MILLIS);

        startScanningZones();
    }

    private void updateLastSeenValues(Collection<IBeacon> iBeacons, Region region) {
        if( iBeacons==null || iBeacons.isEmpty() ) {
            return;
        }

        final AverageIBeacon averageBeacon = mMonitoredRegionsUniqueIds.get( region.getUniqueId() );
        if( averageBeacon!=null ) {
            averageBeacon.setLastSeen( System.currentTimeMillis() );
        }
    }

    private void discardOldBeacons() {
        for( String uniqueId : mMonitoredRegionsUniqueIds.keySet() ) {

            final AverageIBeacon averageBeacon = mMonitoredRegionsUniqueIds.get( uniqueId );
            if(averageBeacon!=null && averageBeacon.getLastSeen()<(System.currentTimeMillis()-Config.LEAVE_MSG_DELAY_MILLIS) && !mRegionsToLeave.contains(uniqueId)) {
                final Region region = new Region( uniqueId, averageBeacon.getProximityUuid(), averageBeacon.getMajor(), averageBeacon.getMinor() );
                sendDelayedLeave( region );
            }

            if(averageBeacon==null) {
                for( String beaconId : mMonitoredBeaconIds.keySet() ) {
                    if( uniqueId.toLowerCase().startsWith(beaconId.toLowerCase()) && mMonitoredBeaconIds.get(beaconId)!=Proximity.UNKNOWN && !mRegionsToLeave.contains(uniqueId) ) {
                        final Beacon beacon = new Beacon();
                        beacon.id = beaconId;
                        final Region region = new Region( uniqueId, beacon.getProximityUid(), beacon.getMajor(), beacon.getMinor() );
                        sendDelayedLeave( region );
                    }
                }
            }
        }
    }

    private void sendDelayedLeave( Region region ) {
        L.d(". " + region.getUniqueId());
        Message msg = Message.obtain();
        msg.what = BeaconEvent.REGION_LEAVE.ordinal();
        msg.obj = region;
        mEnterLeaveHandler.sendMessageDelayed( msg, Config.LEAVE_MSG_DELAY_MILLIS );
        mRegionsToLeave.add( region.getUniqueId() );
    }

    private void sendDelayedEnter( Region region ) {
        L.d(".");
        Message msg = Message.obtain();
        msg.what = BeaconEvent.REGION_ENTER.ordinal();
        msg.obj = region;
        mEnterLeaveHandler.sendMessage(msg);
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
    public void onCreate() {
        startHealthcheck();

        Set<BLEKitClient> runningClients = BeaconPreferences.getRunningClients(this);
        if( runningClients!=null ) {
            for( BLEKitClient client : runningClients ) {
                clients.put( client.getPackageName(), client );
            }
        }

        mMonitoredBeaconIds = BeaconPreferences.getMonitoredBeacons(this);

        L.d("added " + (runningClients != null ? runningClients.size() : 0) + " packages and " + mMonitoredBeaconIds.size() + " beacons");
        for( String id : mMonitoredBeaconIds.keySet() ) {
            L.d( id + " " + mMonitoredBeaconIds.get(id) );
        }

        iBeaconManager = IBeaconManager.getInstanceForApplication(this);
        iBeaconManager.bind(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        L.d("onDestroy");
        super.onDestroy();
        stopScanningZones();
        iBeaconManager.unBind(this);
        mBeaconManagerConnected = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processProximity(final int newProximity, final Region region) {
        BeaconEvent event = BeaconEvent.fromIBeaconProximity(newProximity);

        final String beaconId = regionToBeaconId(region);
        final Proximity newProx = Proximity.fromBeaconEvent(event);

        if( newProx == mMonitoredBeaconIds.get(beaconId) ) {
            L.d( "proximity is same as old value, not broadcasting" );
            return;
        }

        sendEventToClients( event, region, beaconId );
    }

    private void processEvent( final BeaconEvent beaconEvent, final Region region ) {
        final String beaconId = regionToBeaconId(region);

        final Proximity oldProximity = mMonitoredBeaconIds.get(beaconId);

        if( beaconEvent==BeaconEvent.REGION_ENTER && oldProximity!=null && oldProximity!=Proximity.UNKNOWN ) {
            L.d( "enter event, but we have a more accurate proximity value already" );
            return;
        }

        sendEventToClients( beaconEvent, region, beaconId );
    }


    private void sendEventToClients( final BeaconEvent event, final Region region, final String beaconId ) {
        L.d( event.name() + " " + region.getMajor()+"-"+region.getMinor() );

        final Proximity newProximity = Proximity.fromBeaconEvent(event);

        if( newProximity == mMonitoredBeaconIds.get(beaconId) ) {
            L.d( "proximity is same as old value, not broadcasting" );
            return;
        }

        mMonitoredBeaconIds.put( beaconId, newProximity );
        persistBeaconStates();

        for( String pkg : clients.keySet() ) {
            BLEKitClient client = clients.get(pkg);
            L.d("." + pkg);
            if(client!=null && isAnyBeaconInRegion(client.getMonitoredBeaconIDs(), region)) {
                client.call( this, event, beaconId );
            }
        }
    }

    private String regionToBeaconId( Region region ) {
        if( region==null ) return null;
        return region.getProximityUuid() + "+" + region.getMajor() + "+" + region.getMinor();
    }

    private boolean isAnyBeaconInRegion( Set<String> beaconIds, Region region ) {
        for( String beaconId : beaconIds ) {
            Beacon beacon = new Beacon();
            beacon.id = beaconId;
            if( beacon.matchesRegion(region) ) {
                return true;
            }
        }
        return false;
    }

    private void persistBeaconStates() {
        BeaconPreferences.setMonitoredBeacons(this, mMonitoredBeaconIds);
    }



    private void startHealthcheck() {
        Intent intent = new Intent(ACTION);
        intent.putExtra( Extra.EXTRA_COMMAND, Extra.COMMAND_HEALTHCHECK );
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime(), Config.HEALTHCHECK_REPEAT_SECONDS*1000, pintent);
    }
}
