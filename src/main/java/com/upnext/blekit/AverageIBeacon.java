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

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * Beacon extension class which provides better sampling of results.
 *
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class AverageIBeacon extends IBeacon {

    public static final int SAMPLES_REQUIRED = 3;
    public static final int RELATIVE_TX_POWER = -58;

    private int numSamples = 0;

    private int prevProximity = PROXIMITY_UNKNOWN;

    private long lastSeen;

    public AverageIBeacon(IBeacon otherIBeacon) {
        super(otherIBeacon);
        lastSeen = System.currentTimeMillis();
        numSamples = 0;
        if( accuracy<0 ) accuracy=0d;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public boolean isProximityReady() {
        return numSamples==SAMPLES_REQUIRED;
    }

    public void update( IBeacon otherIBeacon ) {
        accuracy += calculateAccuracy( RELATIVE_TX_POWER, otherIBeacon.getRssi() );
        numSamples++;
    }

    public void approximate() {
        accuracy = accuracy/numSamples;
        numSamples = 1;
    }

    @Override
    public double getAccuracy() {
        return accuracy;
    }

    @Override
    public int getProximity() {
        return calculateProximity(accuracy);
    }

    public int getPrevProximity() {
        return prevProximity;
    }

    public void setPrevProximity(int prevProximity) {
        this.prevProximity = prevProximity;
    }

    public boolean proximityChanged() {
        return getProximity() != prevProximity;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
}
