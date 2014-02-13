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
 * Contains configuration constants used across library.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public final class Config {

    /**
     * BLE scans configuration.
     */
    protected static final long FOREGROUND_SCAN_DURATION_MILLIS = 30000;
    protected static final long FOREGROUND_PAUSE_DURATION_MILLIS = 100;
    protected static final long BACKGROUND_SCAN_DURATION_MILLIS = 30000;
    protected static final long BACKGROUND_PAUSE_DURATION_MILLIS = 100;

    /**
     * Watchdog checks every this amount of seconds if service is running
     */
    protected static final int HEALTHCHECK_REPEAT_SECONDS = 10;

    /**
     * After first LEAVE envent if no ENTER is seen in this amount of time, a proper LEAVE will be sent.
     */
    protected static final long LEAVE_MSG_DELAY_MILLIS = 15000;

}
