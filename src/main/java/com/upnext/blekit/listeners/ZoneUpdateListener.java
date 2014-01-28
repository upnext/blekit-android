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
package com.upnext.blekit.listeners;

import com.upnext.blekit.model.Zone;

/**
 * Listener for zone (configuration) updates.
 *
 * @see com.upnext.blekit.model.Zone
 * @see com.upnext.blekit.BLEKit#setZoneUpdateListener(ZoneUpdateListener)
 * @author Roman Wozniak (roman@up-next.com)
 */
public interface ZoneUpdateListener {

    /**
     * Called immediately after a new configuration has been loaded (after BLEKit start or after manual call to {@link com.upnext.blekit.BLEKit#reloadJson()})
     *
     * @param zone the zone configuration that has just been loaded
     */
    void onZoneUpdated( Zone zone );

}
