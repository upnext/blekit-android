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

import com.radiusnetworks.ibeacon.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * BLEKit zone model deserialized from JSON configuration.
 * Example:
 * <pre>
 * {@code
 * {
 *    "id":"Upnext Headquarters",
 *    "name":"Upnext Headquarters",
 *    "ttl":86400,
 *    "radius":0.5,
 *    "beacons":[]
 *    "location":{
 *       "latitude":52.530024,
 *       "longitude":13.383453
 *    }
 * }
 * }
 * </pre>
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class Zone {

    /**
     * Zone identifier
     */
    public String id;

    /**
     * Zone name
     */
    public String name;

    /**
     * Zone TTL
     */
    public long ttl;

    /**
     * Zone radius
     */
    public double radius;

    /**
     * Zone location
     */
    public Location location;

    /**
     * Zone beacons
     */
    public List<Beacon> beacons;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Zone{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ttl=" + ttl +
                ", radius=" + radius +
                ", location=" + location +
                ", beacons=" + beacons +
                '}';
    }

    /**
     * Returns <code>true</code> if this zone contains a beacon that matches given Region.
     *
     * @param region region
     * @return <code>true</code> if this zone contains a beacon that matches given Region, <code>false</code> otherwise
     */
    public boolean containsMatchingBeacon( Region region ) {
        for( Beacon beacon : beacons ) {
            if( beacon.matchesRegion(region) )
                return true;
        }
        return false;
    }

    /**
     * Returns a collection of beacons that match given region.
     * If no beacons match, an empty collection is returned.
     *
     * @param region region
     * @return collection of beacons
     */
    public List<Beacon> getMatchingBeacons( Region region ) {
        List<Beacon> matchingBeacons = new ArrayList<Beacon>();
        for( Beacon beacon : beacons ) {
            if( beacon.matchesRegion(region) )
                matchingBeacons.add(beacon);
        }
        return matchingBeacons;
    }
}
