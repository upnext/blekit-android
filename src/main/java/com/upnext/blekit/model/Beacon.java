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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BLEKit beacon model deserialized from JSON configuration.
 * Example:
 * <pre>
 * {@code
 * {
 *    "beacons":[
 *       {
 *          "id":"D57092AC-DFAA-446C-8EF3-C81AA22815B5+5+5000",
 *          "description":"This is first test beacon",
 *          "name":"First beacon",
 *          "location":{
 *             "latitude":52.2353,
 *             "longitude":21.0114
 *          },
 *          "triggers":[]
 *       }
 *    ]
 * }
 * }
 * </pre>
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class Beacon {

    public static final String UID_REGEXP = "(.*)\\+(\\d+)\\+(\\d+)";
    private static final Pattern UID_PATTERN = Pattern.compile( UID_REGEXP );

    /**
     * Beacon identifier, which must match the regular epression {@link #UID_REGEXP}
     */
    public String id;

    /**
     * Beacon description
     */
    public String description;

    /**
     * Beacon name
     */
    public String name;

    /**
     * Beacon location
     */
    public Location location;

    /**
     * Beacon triggers
     */
    public List<Trigger> triggers;

    /**
     * Returns proximity UUID extracted from beacon id.
     *
     * @return proximity UUID extracted from beacon id.
     */
    public String getProximityUid() {
        Matcher matcher = UID_PATTERN.matcher(id);
        if( id==null || !matcher.matches() ) return id;
        return matcher.group(1);
    }

    /**
     * Return beacon Major value extracted from beacon id.
     * If major is not found, returns null.
     *
     * @return beacon major extracted from beacon id or null if not found
     */
    public Integer getMajor() {
        Matcher matcher = UID_PATTERN.matcher(id);
        if( id==null || !matcher.matches() ) return null;
        String majorString = matcher.group(2);
        try {
            return Integer.parseInt( majorString );
        } catch (NumberFormatException e) {}
        return null;
    }

    /**
     * Return beacon Minor value extracted from beacon id.
     * If minor is not found, returns null.
     *
     * @return beacon minor extracted from beacon id or null if not found
     */
    public Integer getMinor() {
        Matcher matcher = UID_PATTERN.matcher(id);
        if( id==null || !matcher.matches() ) return null;
        String minorString = matcher.group(3);
        try {
            return Integer.parseInt( minorString );
        } catch (NumberFormatException e) {}
        return null;
    }

    /**
     * Helper method checking whether this beacon maches to given {@link com.radiusnetworks.ibeacon.Region}
     *
     * @param region region
     * @return <code>true</code> if matches region, <code>false</code> otherwise
     */
    public boolean matchesRegion( Region region ) {
        if( region.getProximityUuid().equalsIgnoreCase( getProximityUid() ) ) {
            if( region.getMajor()==null && region.getMinor()==null ) {
                return true;
            } else {
                if( region.getMajor()!=null && region.getMajor().equals(getMajor()) &&
                        region.getMinor()!=null && region.getMinor().equals(getMinor()) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beacon beacon = (Beacon) o;

        if (id != null ? !id.equalsIgnoreCase(beacon.id) : beacon.id != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Beacon{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", triggers=" + triggers +
                '}';
    }
}
