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

/**
 * Describes scan periods for Bluetooth LE.
 *
 * @author Roman Wozniak (roman@up-next.com)
 * @see com.upnext.blekit.BLEKit#setScanPeriods(BLEKitScanPeriods)
 */
public class BLEKitScanPeriods {

    /**
     * Time of BLE scan when the application is in foreground
     */
    public long foregroundScanPeriod;

    /**
     * Pause between scans when the application is in foreground
     */
    public long foregroundIdlePeriod;

    /**
     * Time of BLE scan when the application is in background
     */
    public long backgroundScanPeriod;

    /**
     * Pause between scans when the application is in background
     */
    public long backgroundIdlePeriod;

    /**
     * Constructor.
     *
     * @param foregroundScanPeriod Time of BLE scan when the application is in foreground
     * @param foregroundIdlePeriod Pause between scans when the application is in foreground
     * @param backgroundScanPeriod Time of BLE scan when the application is in background
     * @param backgroundIdlePeriod Pause between scans when the application is in background
     */
    public BLEKitScanPeriods(long foregroundScanPeriod, long foregroundIdlePeriod, long backgroundScanPeriod, long backgroundIdlePeriod) {
        this.foregroundScanPeriod = foregroundScanPeriod;
        this.foregroundIdlePeriod = foregroundIdlePeriod;
        this.backgroundScanPeriod = backgroundScanPeriod;
        this.backgroundIdlePeriod = backgroundIdlePeriod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BLEKitScanPeriods{" +
                "foregroundScanPeriod=" + foregroundScanPeriod +
                ", foregroundIdlePeriod=" + foregroundIdlePeriod +
                ", backgroundScanPeriod=" + backgroundScanPeriod +
                ", backgroundIdlePeriod=" + backgroundIdlePeriod +
                '}';
    }
}
