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
package com.upnext.blekit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.upnext.blekit.Proximity;

/**
 * Helper class for passing beacon with its proximity in bundles.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class CurrentBeaconProximity implements Parcelable {

    private String beaconId;
    private Proximity proximity;

    public CurrentBeaconProximity(String beaconId, Proximity proximity) {
        this.beaconId = beaconId;
        this.proximity = proximity;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public Proximity getProximity() {
        return proximity;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(beaconId);
        dest.writeString(proximity.name());
    }

    public CurrentBeaconProximity(Parcel in) {
        beaconId = in.readString();
        proximity = Proximity.valueOf(in.readString());
    }

    /**
     * {@inheritDoc}
     */
    public static final Parcelable.Creator<CurrentBeaconProximity> CREATOR
            = new Parcelable.Creator<CurrentBeaconProximity>() {
        @Override
        public CurrentBeaconProximity createFromParcel(Parcel source) {
            return new CurrentBeaconProximity(source);
        }

        @Override
        public CurrentBeaconProximity[] newArray(int size) {
            return new CurrentBeaconProximity[size];
        }
    };
}
