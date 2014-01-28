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
import android.net.Uri;

import com.upnext.blekit.R;

/**
 * Action of type 'content'.
 *
 * In foreground launches a web browser with url fromparameters.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class ContentAction extends BLEAction<ContentActionParams> {

    public static final int NOTIFICATION_ID = 1454234;

    public static final String TYPE = "content";

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
    public Class<ContentActionParams> getParameterClass() {
        return ContentActionParams.class;
    }

    /**
     * Displays a notification, after click opens a web browser with url as {@link com.upnext.blekit.actions.ContentActionParams#url}
     *
     * @param context Android Context, passed from the calling entity
     */
    @Override
    public void performInBackground(Context context) {
        displayNotification( context, parameters.notification_title, parameters.notification_message, TYPE, R.drawable.ic_notification, NOTIFICATION_ID );
    }

    /**
     * Opens a web browser with url as {@link com.upnext.blekit.actions.ContentActionParams#url}
     *
     * @param activity activity on which behalf this action performs in foreground
     */
    @Override
    public void performInForeground(Activity activity) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(parameters.url));
        activity.startActivity(i);
    }

    /**
     * Opens a web browser with url as {@link com.upnext.blekit.actions.ContentActionParams#url}
     *
     * @param intent Intent
     * @param activity Android Activity
     * @see com.upnext.blekit.actions.BLEAction#processIntent(android.content.Intent, android.app.Activity)
     */
    @Override
    public void processIntent(Intent intent, Activity activity) {
        if( intent==null ) return;
        String type = intent.getStringExtra("type");
        if( type!=null && type.equals(getType()) ) {
            performInForeground(activity);
        }
    }
}
