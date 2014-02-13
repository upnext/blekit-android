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
package com.upnext.blekit.util;

import android.content.Context;

import com.sentaca.dbpreferences.DatabaseBasedSharedPreferences;
import com.upnext.blekit.BLEKitClient;
import com.upnext.blekit.Proximity;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Persistence layer for beacons data.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class BeaconPreferences {

    private static DatabaseBasedSharedPreferences mPrefs;

    private static final String LAST_ZONE_JSON = "last_zone_json";
    private static final String TARGET_ACTIVITY_FOR_NOTIFICATIONS = "target_activity_for_notifications";

    private static final String RUNNING_CLIENTS = "running_clients";
    private static final String SEPARATOR_CLIENT_VALUES = "#";
    private static final String SEPARATOR_BEACON_VALUES = ",";

    private static final String MONITORED_BEACONS = "monitored_beacons";

    private static DatabaseBasedSharedPreferences getPrefs(Context context) {
        if(mPrefs==null) {
            mPrefs = new DatabaseBasedSharedPreferences(context);
        }
        return mPrefs;
    }

    public static Set<BLEKitClient> getRunningClients( Context context ) {
        Set<String> clients = getPrefs(context).getStringSet(RUNNING_CLIENTS);
        Set<BLEKitClient> result = new HashSet<BLEKitClient>();
        if( clients!=null ) {
            for( String clientString : clients ) {
                String[] vals = clientString.split(SEPARATOR_CLIENT_VALUES);

                String pkg = vals[0];
                String inBg = vals[1];
                String beacons = vals[2];

                BLEKitClient c = new BLEKitClient(pkg, Boolean.valueOf(inBg), beaconsFromString(beacons));
                result.add(c);
            }
        }
        return result;
    }

    private static Set<String> beaconsFromString(String beacons) {
        Set<String> result = new HashSet<String>();
        if(beacons!=null) {
            Collections.addAll(result, beacons.split(SEPARATOR_BEACON_VALUES));
        }
        return result;
    }

    private static String beaconsToString(Set<String> beacons) {
        StringBuffer buf = new StringBuffer();
        for( String beacon : beacons ) {
            buf.append(beacon).append(SEPARATOR_BEACON_VALUES);
        }
        if( buf.length()>0 ) {
            buf.deleteCharAt(buf.length()-1);
        }
        return buf.toString();
    }

    public static void setRunningClients( Context context, Set<BLEKitClient> clients ) {
        Set<String> clientsSet = new HashSet<String>();
        for( BLEKitClient client : clients ) {
            clientsSet.add(
                    client.getPackageName() + SEPARATOR_CLIENT_VALUES +
                            client.isInBackground() + SEPARATOR_CLIENT_VALUES +
                            beaconsToString(client.getMonitoredBeaconIDs())
            );
        }

        getPrefs(context).putStringSet(RUNNING_CLIENTS, clientsSet );
    }

    public static void addClient( Context context, BLEKitClient clientToAdd ) {
        Set<BLEKitClient> current = getRunningClients(context);
        if( current==null ) {
            current = new HashSet<BLEKitClient>();
        }

        boolean pkgFound = false;
        for( BLEKitClient client : current ) {
            if( client.getPackageName().equals(clientToAdd.getPackageName()) ) {
                pkgFound = true;
            }
        }

        if( !pkgFound ) {
            current.add(clientToAdd);
            setRunningClients(context, current);
        }
    }

    public static void removeClient( Context context, String clientPackage ) {
        Set<BLEKitClient> current = getRunningClients(context);
        if( current==null ) {
            return;
        }

        for( BLEKitClient client : current ) {
            if( client.getPackageName().equals(clientPackage) ) {
                current.remove(client);
                setRunningClients(context, current);
                return;
            }
        }
    }

    public static String getLastZoneJson( Context context ) {
        return getPrefs(context).getString(LAST_ZONE_JSON, null);
    }

    public static void setLastZoneJson( Context context, String json ) {
        getPrefs(context).putString(LAST_ZONE_JSON, json);
    }

    public static String getTargetActivityForNotifications( Context context ) {
        return getPrefs(context).getString(TARGET_ACTIVITY_FOR_NOTIFICATIONS, null);
    }

    public static void setTargetActivityForNotifications( Context context, String activity ) {
        getPrefs(context).putString(TARGET_ACTIVITY_FOR_NOTIFICATIONS, activity);
    }

    public static Map<String, Proximity> getMonitoredBeacons(Context context) {
        final Set<String> values = getPrefs(context).getStringSet(MONITORED_BEACONS);
        final Map<String, Proximity> result = new HashMap<String, Proximity>();
        if( values!=null ) {
            for( String value : values ) {
                final String[] split = value.split(SEPARATOR_BEACON_VALUES);
                final String beaconId = split[0];
                final Proximity proximity = Proximity.valueOf(split[1]);
                result.put(beaconId, proximity);
            }
        }
        return result;
    }

    public static void setMonitoredBeacons(Context context, Map<String, Proximity> monitoredBeacons) {
        if( monitoredBeacons==null || monitoredBeacons.isEmpty() ) return;
        final Set<String> values = new HashSet<String>();
        for( String beacondId : monitoredBeacons.keySet() ) {
            values.add( beacondId + SEPARATOR_BEACON_VALUES + monitoredBeacons.get(beacondId).name() );
        }
        getPrefs(context).putStringSet(MONITORED_BEACONS, values);
    }
}
