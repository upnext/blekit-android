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

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum describing event occurence units
 *
 * @author Roman Wozniak (roman@up-next.com)
 * @see com.upnext.blekit.conditions.OccurenceCondition
 */
public enum EventOccurenceUnit {

    /**
     * hour time unit
     */
    HOUR,

    /**
     * day time unit
     */
    DAY,

    /**
     * month time unit
     */
    MONTH,

    /**
     * year time unit
     */
    YEAR,

    /**
     * total time unit - all events are counted
     */
    TOTAL;

    private static Map<String, EventOccurenceUnit> namesMap = new HashMap<String, EventOccurenceUnit>(5);

    static {
        namesMap.put("hour", HOUR);
        namesMap.put("day", DAY);
        namesMap.put("month", MONTH);
        namesMap.put("year", YEAR);
        namesMap.put("total", TOTAL);
    }

    @JsonCreator
    public static EventOccurenceUnit forValue(String value) {
        if( value==null ) return TOTAL;
        EventOccurenceUnit event = namesMap.get(value.toLowerCase());
        if( event==null ) return TOTAL;
        return event;
    }
}
