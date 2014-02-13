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
 * Enum describing beacon events.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public enum BeaconEvent {

    /**
     * A beacon has appeared in range
     */
    REGION_ENTER,

    /**
     * A beacon has disappeared from range.
     * This event is in reality delayed to prevent imemdiate enter-exit-enter events.
     * Because of that LEAVE is delayed by {@link com.upnext.blekit.BLEKitService#LEAVE_MSG_DELAY_MILLIS} ms.
     */
    REGION_LEAVE,

    /**
     * A beacon has just changed proximity to immediate
     */
    CAME_IMMEDIATE,

    /**
     * A beacon has just changed proximity to near
     */
    CAME_NEAR,

    /**
     * A beacon has just changed proximity to far
     */
    CAME_FAR;


    /**
     * Matches IBeacon proximity to BeaconEvent proximity change
     *
     * @param iBeacon ibeacon
     * @return beacon event
     */
    public static BeaconEvent fromIBeaconProximity(IBeacon iBeacon) {
        return fromIBeaconProximity(iBeacon.getProximity());
    }

    /**
     * Matches IBeacon proximity to BeaconEvent proximity change
     *
     * @param proximity proximity
     * @return beacon event
     */
    public static BeaconEvent fromIBeaconProximity(int proximity) {
        switch (proximity) {
            case IBeacon.PROXIMITY_IMMEDIATE:
                return CAME_IMMEDIATE;

            case IBeacon.PROXIMITY_NEAR:
                return CAME_NEAR;

            case IBeacon.PROXIMITY_FAR:
                return CAME_FAR;

            default:
                return null;
        }
    }
}
