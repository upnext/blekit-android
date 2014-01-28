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

import com.upnext.blekit.BeaconEvent;

/**
 * Condition checking whether an {@link com.upnext.blekit.BeaconEvent#CAME_FAR} has just been received.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class CameFarCondition extends BLECondition<Void> {

    public static final String TYPE = "cameFar";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean evaluate() {
        return beaconEvent!=null && beaconEvent==BeaconEvent.CAME_FAR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    /**
     * Returns <code>true</code> only if beacon event equals {@link com.upnext.blekit.BeaconEvent#CAME_FAR}
     *
     * @param beaconEvent event
     * @return <code>true</code> only if beacon event equals {@link com.upnext.blekit.BeaconEvent#CAME_FAR}
     */
    @Override
    public boolean isValidForEvent(BeaconEvent beaconEvent) {
        return beaconEvent!=null && beaconEvent==BeaconEvent.CAME_FAR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BLECondition getInstance() {
        return new CameFarCondition();
    }
}
