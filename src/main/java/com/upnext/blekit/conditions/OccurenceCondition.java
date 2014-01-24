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
package com.upnext.blekit.conditions;

import com.upnext.blekit.EventOccurenceUnit;
import com.upnext.blekit.util.BeaconsDB;
import com.upnext.blekit.util.L;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract condition that counts occurences of condition.
 * Occurence is increased only if {@link com.upnext.blekit.conditions.BLECondition#isValidForEvent(com.upnext.blekit.BeaconEvent)} returned <code>true</code>.
 *
 * Occurence can be checked either by using parameters in configuration or through expression (only 'occurence' parameter here, no 'occurence_unit').
 *
 * Occurence is unique per beacon id ({@link com.upnext.blekit.model.Beacon#id}
 *
 * @see com.upnext.blekit.conditions.OccurenceParams
 * @see com.upnext.blekit.BeaconEvent
 * @author Roman Wozniak (roman@up-next.com)
 */
public abstract class OccurenceCondition extends BLECondition<OccurenceParams> {

    protected String beaconId;
    protected BeaconsDB beaconsDB;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean evaluate() {
        if( parameters!=null ) {
            if( parameters.occurence_unit==null ) {
                parameters.occurence_unit = EventOccurenceUnit.TOTAL;
            }
            int count = beaconsDB.getNumOccurencesForBeaconInTime(beaconEvent, beaconId, parameters.occurence_unit);
            L.d( "so far: " + count );
            return count == parameters.occurence;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<OccurenceParams> getParameterClass() {
        return OccurenceParams.class;
    }

    /**
     * Sets beacons DB for persisting occurences.
     *
     * @param beaconsDB beacons database
     */
    public void setBeaconsDB(BeaconsDB beaconsDB) {
        this.beaconsDB = beaconsDB;
    }

    /**
     * Sets id of the beacon for which occurence should be increased.
     *
     * @param beaconId beacon identifier
     */
    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    /**
     * {@inheritDoc}
     *
     * Provides only 'occurence' parameter which is the TOTAL count of events.
     */
    @Override
    protected Map<String, Object> getExpressionParameters() {
        Map<String, Object> myParamsMap = new HashMap<String, Object>();

        int count = beaconsDB.getNumOccurencesForBeaconInTime(beaconEvent, beaconId, EventOccurenceUnit.TOTAL);
        myParamsMap.put( "occurence", count );

        return myParamsMap;
    }
}
