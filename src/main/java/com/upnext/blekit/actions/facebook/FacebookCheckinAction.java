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
package com.upnext.blekit.actions.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.upnext.blekit.R;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.util.L;

import java.util.List;

/**
 * Action responsible for check-in to Facebook.
 *
 * It is mandatory that an application_id for a Facebook application is defined in AndroidManifest.xml for check-in to work properly:
 * <pre>
 * {@code
 * <application>
 *
 *     <meta-data android:name="com.facebook.sdk.ApplicationId" android:value="facebook_application_id" />
 *
 * </application>
 * }
 * </pre>
 *
 *
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FacebookCheckinAction extends BLEAction<FacebookCheckinActionParams> {

    public static final String TYPE = "facebook-checkin";
    public static final int NOTIFICATION_ID = 2454234;

    protected static String applicationId;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Tries to check-in user in background.
     * Should succeed when:
     * <ul>
     *  <li> Facebook session is valid and user is logged in
     *  <li> application has permissions to post on users wall
     * </ul>
     *
     * If check-in is successful, a notification will be shown, where:
     * <ul>
     *  <li> notification title will be set to {@link com.upnext.blekit.actions.facebook.FacebookCheckinActionParams#notification_title}
     *  <li> notification message will be set to {@link com.upnext.blekit.actions.facebook.FacebookCheckinActionParams#notification_message}
     *  <li> notification ticker will be set to {@link com.upnext.blekit.actions.facebook.FacebookCheckinActionParams#notification_message}
     * </ul>
     *
     * Unsuccessful check-in is consumed silently (user not logged-in or same check-in message).
     *
     * @param context Android Context, passed from the calling entity
     * @see com.upnext.blekit.actions.BLEAction#performInBackground(android.content.Context)
     */
    @Override
    public void performInBackground(Context context) {
        L.d(".");
        if( applicationId==null ) {
            throw new IllegalStateException( "No application_id is set. Use FacebookCheckinAction.setApplicationId(\"APP_ID\") first." );
        }

        tryChekinInBackgroud(context);
    }

    /**
     * Tries to check-in user in foreground.
     * Should succeed when:
     * <ul>
     *  <li> Facebook session is valid and user is logged in
     *  <li> application has permissions to post on users wall
     * </ul>
     *
     * If check-in is successful, a toast message will be shown with message content set to {@link com.upnext.blekit.actions.facebook.FacebookCheckinActionParams#notification_message}
     *
     * Unsuccessful check-in is consumed silently (user not logged-in or same check-in message).
     *
     * @param activity Android Activity, passed from the calling entity
     * @see com.upnext.blekit.actions.BLEAction#performInForeground(android.app.Activity)
     */
    @Override
    public void performInForeground(Activity activity) {
        L.d(".");
        tryChekinInBackgroud(activity);
    }

    /**
     * Called when user selects the notification created by this action (after successful background check-in).
     *
     * Shows a toast message with message content set to {@link com.upnext.blekit.actions.facebook.FacebookCheckinActionParams#notification_message}
     *
     * @param intent Intent
     * @param activity Anroid Activity
     * @see com.upnext.blekit.actions.BLEAction#processIntent(android.content.Intent, android.app.Activity)
     */
    @Override
    public void processIntent(Intent intent, Activity activity) {
        if( intent==null ) return;
        String type = intent.getStringExtra("type");
        if( type!=null && type.equals(getType()) ) {
            showSuccessfulToast(activity);
        }
    }

    /**
     * Sets Facebook application_id associated with this action.
     * It is mandatory to call this method before using this action.
     *
     *
     * @param appId Facebook application id
     */
    public static void setApplicationId( String appId ) {
        applicationId = appId;
    }

    protected void displayNotification(Context context) {
        displayNotification( context, parameters.notification_title, parameters.notification_message, TYPE, R.drawable.ic_notification, NOTIFICATION_ID );
    }

    protected boolean tryChekinInBackgroud(Context context) {
        Session session = Session.getActiveSession();
        if ( session == null ) {
            L.d("session is null");
            return false;
        }

        L.d( "session " + session );

        if( !session.isOpened() ) {
            L.d( "Session is not open" );
            return false;
        }

        if( hasPostPermission(session.getPermissions()) ) {
            checkin(context, session);
            return true;
        }

        L.d( "no post permission" );
        return false;
    }

    protected void checkin(final Context context, Session session) {
        L.d( "BG checking in with " + parameters.place_id );
        Bundle params = new Bundle();
        params.putString("message", parameters.message);
        if( parameters.privacy!=null ) {
            params.putString("privacy", "{\"value\":\"" + parameters.privacy + "\"}");
        } else {
            params.putString("privacy", "{\"value\":\"EVERYONE\"}");
        }

        params.putString("place", parameters.place_id+"");

        new Request(
                session,
                "/me/feed",
                params,
                HttpMethod.POST,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        L.d("finished " + response);
                        if( response.getError()!=null ) {
                            L.d("error " + response.getError().getErrorMessage());
                        } else {
                            if( context instanceof Activity ) {
                                ((Activity)context).runOnUiThread( new Runnable() {
                                    @Override
                                    public void run() {
                                        showSuccessfulToast(context);
                                    }
                                });
                            } else {
                                displayNotification(  context);
                            }
                        }
                    }
                }
        ).executeAndWait();
    }

    protected void showSuccessfulToast(final Context context) {
        Toast.makeText( context, parameters.notification_message, Toast.LENGTH_LONG ).show();
    }

    protected boolean hasPostPermission(List<String> permissions) {
        for( String permission : permissions ) {
            if( "publish_actions".equals(permission) )
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getParameterClass() {
        return FacebookCheckinActionParams.class;
    }


}
