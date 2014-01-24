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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.upnext.blekit.R;

import java.util.List;

/**
 * Action showing a business in Yelp.
 * If Yelp application is installed, then it will be launched. Otherwise a browser will be launched.
 *
 * In background a notification is shown and Yelp is shown (new Intent) after click.
 * In foreground Yelp is shown (new Intent).
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class YelpAction extends BLEAction<YelpActionParams> {

    public static final int NOTIFICATION_ID = 2254234;

    public static final String TYPE = "yelp";

    public static final String YELP_BUSINESS_URI = "http://yelp.com/biz/";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Class<YelpActionParams> getParameterClass() {
        return YelpActionParams.class;
    }

    @Override
    public void perfromInBackground(Context context) {
        displayNotification(context, parameters.notification_title, parameters.notification_message, TYPE, R.drawable.ic_notification, NOTIFICATION_ID);
    }

    @Override
    public void performInForeground(Activity activity) {
        activity.startActivity(getYelpIntent(activity));
    }

    @Override
    public void processIntent(Intent intent, Activity activity) {
        performInForeground(activity);
    }

    private Intent getYelpIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse( YELP_BUSINESS_URI + parameters.business_id ));
        intent.setComponent(new ComponentName("com.yelp.android", "com.yelp.android.ui.activities.ActivityBusinessPageUrlCatcher"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if( !isYelpIntentAvailable(context, intent) ) {
            intent.setComponent(null);
        }

        return intent;
    }

    private boolean isYelpIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return resolveInfo.size() > 0;
    }
}
