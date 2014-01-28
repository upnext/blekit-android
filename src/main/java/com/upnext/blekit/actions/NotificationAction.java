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
package com.upnext.blekit.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.upnext.blekit.util.L;

/**
 * Action that sends an ordered broadcast.
 * Action of the broadcast is {@link #NOTIFICATION_ACTION}.
 * As an EXTRA String {@link #EXTRA_NAME} a parameter ({@link com.upnext.blekit.actions.NotificationActionParams#name}) value is passed.
 *
 * To receive this event set up a BroadcastRecevier in your AndroidManifest.xml:
 * <pre>
 * {@code
 *  <receiver
 *      android:name="com.your.package.name.MyNotificationReceiver"
 *      android:enabled="true"
 *      android:exported="true" >
 *          <intent-filter android:priority="1" >
 *              <action android:name="com.upnext.blekit.NOTIFICATION_ACTION"/>
 *          </intent-filter>
 *  </receiver>
 * }
 * </pre>
 *
 * Basic implememntation of that receiver would look like this:
 * <pre>
 * <code>
 * public class MyNotificationReceiver extends BroadcastReceiver {
 *
 *     public MyNotificationReceiver() {
 *     }
 *
 *     {@literal @}Override
 *     public void onReceive(Context context, Intent intent) {
 *         if ( intent != null ) {
 *             final String nameParam = intent.getStringExtra(NotificationAction.EXTRA_NAME);
 *             //your code here
 *         }
 *     }
 * }
 * </code>
 * </pre>
 *
 * It is possible to cancel futher boradcast - {@link android.content.BroadcastReceiver#abortBroadcast()}
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class NotificationAction extends BLEAction<NotificationActionParams> {

    /**
     * Action name of the Intent
     */
    public static final String NOTIFICATION_ACTION = "com.upnext.blekit.NOTIFICATION_ACTION";

    /**
     * Name of the EXTRA where parameter 'name' is passed
     */
    public static final String EXTRA_NAME = "name";

    public static final String TYPE = "notification";

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
    public Class<NotificationActionParams> getParameterClass() {
        return NotificationActionParams.class;
    }

    /**
     * Sends an ordered boradcast.
     *
     * @param context Android Context, passed from the calling entity
     */
    @Override
    public void performInBackground(Context context) {
        sendBroadcast(context);
    }

    /**
     * Sends an ordered boradcast.
     *
     * @param activity activity on which behalf this action performs in foreground
     */
    @Override
    public void performInForeground(Activity activity) {
        sendBroadcast(activity);
    }

    private void sendBroadcast( Context context ) {
        L.d( "sending " + parameters.name );
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_ACTION);
        intent.putExtra( EXTRA_NAME, parameters.name );
        context.sendOrderedBroadcast(intent, null);
    }

    /**
     * Not used method.
     *
     * @param intent Intent
     * @param activity Android Activity
     */
    @Override
    public void processIntent(Intent intent, Activity activity) {
        //not used
    }
}
