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
package com.upnext.blekit.actions.foursquare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.upnext.blekit.R;
import com.upnext.blekit.actions.BLEAction;

/**
 * Action responsible for check-in to Foursquare.
 *
 * In foreground launches a new transparent activity ({@link com.upnext.blekit.actions.foursquare.FoursquareCheckinActivity} which tires to check-in user.
 *
 * In background displays a notification + behaves like in foreground after click.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FoursquareCheckinAction extends BLEAction<FoursquareCheckinActionParams> {

    public static final String TYPE = "foursquare-checkin";
    public static final int NOTIFICATION_ID = 2451134;

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
    public Class<FoursquareCheckinActionParams> getParameterClass() {
        return FoursquareCheckinActionParams.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performInBackground(Context context) {
        displayNotification( context, parameters.notification_title, parameters.notification_message, TYPE, R.drawable.ic_notification, NOTIFICATION_ID );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performInForeground(Activity activity) {
        activity.startActivity( new Intent(activity, FoursquareCheckinActivity.class) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processIntent(Intent intent, Activity activity) {
        performInForeground(activity);
    }
}
