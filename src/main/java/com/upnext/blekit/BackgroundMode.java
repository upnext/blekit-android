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

import android.app.Activity;

/**
 * Background mode class, describes current handling mode for BLEActions.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class BackgroundMode {

    /**
     * Activity in foreground
     */
    public Activity activity;

    /**
     * true - in background, false - in foreground
     */
    public boolean inBackground;

    /**
     * Creates new mode.
     * For foreground mode and Activity is required, for background pass null.
     *
     * @param activity Android Activity
     * @param inBackground <code>true</code> if in background, <code>false</code> otherwise
     */
    public BackgroundMode(Activity activity, boolean inBackground) {
        this.activity = activity;
        this.inBackground = inBackground;
    }
}
