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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.upnext.blekit.util.L;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class containing data about a client connected to the BLEKit service.
 *
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class BLEKitClient implements Parcelable {

    private String packageName;
    private boolean inBackground;
    private Set<String> monitoredBeaconIDs;

    /**
     * Constructor.
     *
     * @param packageName package name of the client (package of application using BLEKit library)
     * @param inBackground whether client application is in background
     * @param monitoredBeaconIDs set of beacon identifiers (proximityUUID+major+minor)
     */
    public BLEKitClient(String packageName, boolean inBackground, Set<String> monitoredBeaconIDs) {
        this.packageName = packageName;
        this.inBackground = inBackground;
        this.monitoredBeaconIDs = monitoredBeaconIDs;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isInBackground() {
        return inBackground;
    }

    public void setInBackground(boolean inBackground) {
        this.inBackground = inBackground;
    }

    public Set<String> getMonitoredBeaconIDs() {
        return monitoredBeaconIDs;
    }

    public void setMonitoredBeaconIDs(Set<String> monitoredBeaconIDs) {
        this.monitoredBeaconIDs = monitoredBeaconIDs;
    }

    public void setMonitoredBeaconIDs(List<String> monitoredBeaconIDs) {
        this.monitoredBeaconIDs = new HashSet<String>();
        for( String id : monitoredBeaconIDs ) {
            this.monitoredBeaconIDs.add(id);
        }
    }

    /**
     * Sends an intent with beacon event to the application.
     *
     * @param context context
     * @param event beacon event
     * @param beaconId beacon that triggered the event
     */
    public void call(Context context, BeaconEvent event, String beaconId) {
        L.d(". " + event + " " + beaconId);
        final Intent intent = prepareIntent();
        intent.putExtra(BLEKit.Extra.EXTRA_BEACON_EVENT, event.name());
        intent.putExtra(BLEKit.Extra.EXTRA_BEACON_ID, beaconId);
        context.startService(intent);
    }

    /**
     * Sends an intent with event to the application.
     *
     * @param context context
     * @param dataName name of extra passed in intent
     * @param data data passed in intent extra
     */
    public void call(Context context, String dataName, Parcelable data) {
        final Intent intent = prepareIntent();
        intent.putExtra(dataName, data);
        context.startService(intent);
    }

    /**
     * Sends an intent with event to the application.
     *
     * @param context context
     * @param dataName name of extra passed in intent
     * @param data data passed in intent extra
     */
    public void call(Context context, String dataName, String data) {
        L.d(". " + dataName + " " + data);
        final Intent intent = prepareIntent();
        intent.putExtra(dataName, data);
        context.startService(intent);
    }

    private Intent prepareIntent() {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, BLEKitIntentProcessor.class.getCanonicalName() /*"com.upnext.blekit.BLEKitIntentProcessor"*/));
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeInt(inBackground ? 1 : 0);
        dest.writeStringList(new ArrayList<String>(monitoredBeaconIDs));
    }

    /**
     * {@inheritDoc}
     */
    public BLEKitClient(Parcel in) {
        packageName = in.readString();
        inBackground = in.readInt()==1;
        List<String> list = new ArrayList<String>();
        in.readStringList( list );
        monitoredBeaconIDs = new HashSet<String>(list);
    }

    /**
     * {@inheritDoc}
     */
    public static final Parcelable.Creator<BLEKitClient> CREATOR
            = new Parcelable.Creator<BLEKitClient>() {
        @Override
        public BLEKitClient createFromParcel(Parcel source) {
            return new BLEKitClient(source);
        }

        @Override
        public BLEKitClient[] newArray(int size) {
            return new BLEKitClient[size];
        }
    };
}
