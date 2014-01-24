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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.upnext.blekit.BLEKit;
import com.upnext.blekit.BackgroundMode;
import com.upnext.blekit.util.JsonParser;
import com.upnext.blekit.util.L;

import org.json.JSONObject;

/**
 * Base abstract class for custom BLEKit actions.
 *
 * As type <T> is provided class with action parameters.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public abstract class BLEAction<T> {

    protected T parameters;

    /**
     * Returns type for given action.
     * It is matched against type in JSON ("type":"_action_type_") and should be exactly the same (case sensitive).
     *
     * @return type for this action
     */
    public abstract String getType();

    /**
     * Sets parametrs for this action.
     *
     * @param parameters parameters instance
     */
    public void setParameters(T parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns parameter class.
     * If this action does not support parameters, then Void.class should be returned.
     *
     * Returned class is used for JSON deserialization, so the structure of the class should be the same as JSON object with parameters.
     * For more help see {@link com.fasterxml.jackson.databind.ObjectMapper#readValue(String, Class)}.
     *
     * @return parameter class
     */
    public abstract Class<T> getParameterClass();

    /**
     * If BLEKit service is in background (@see {@link com.upnext.blekit.BLEKit#setBackgroundMode(boolean, android.app.Activity)}),
     * then this action is performed if conditions are met.
     *
     * @param context Android Context, passed from the calling entity
     */
    public abstract void perfromInBackground(Context context);

    /**
     * If BLEKit service is in foreground (@see {@link com.upnext.blekit.BLEKit#setBackgroundMode(boolean, android.app.Activity)}),
     * then this action is performed if conditions are met.
     *
     * @param activity activity on which behalf this action performs in foreground
     */
    public abstract void performInForeground( Activity activity );

    /**
     * Called when user selects the notification created by this action.
     *
     * @param intent Intent
     * @param activity Android Activity
     */
    public abstract void processIntent( Intent intent, Activity activity );

    /**
     * Performs action (either in foreground or background, @see {@link com.upnext.blekit.BLEKit#setBackgroundMode(boolean, android.app.Activity)}).
     *
     * If an attempt to perform action in foreground fails (eg. IllegalStateException is thrown), then it is performed in background.
     *
     * @param context context
     * @param backgroundMode background mode
     */
    public final void performAction(Context context, BackgroundMode backgroundMode) {

        if( backgroundMode.inBackground || backgroundMode.activity==null || backgroundMode.activity.isDestroyed() || backgroundMode.activity.isFinishing() ) {

            perfromInBackground( context );

        } else {

            try {
                performInForeground( backgroundMode.activity );
            } catch (IllegalStateException exc) {
                L.d("No longer in foreground, performing action in background");
                // happens when a fragment cannot be displayed "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState"
                // so as a fallback we perform the action in background
                perfromInBackground(context);
            }

        }
    }

    /**
     * Helper method for deserializing JSON into desired Object
     *
     * @param parameters JSONode that is meant to be deserialized
     * @param parameterClass instance of this class will be created and filled with data from parameters
     * @return deserialized obect instance
     */
    public static <T>T getPrametersFromJson( JsonNode parameters, Class parameterClass ) {
        JsonParser jsonParser = new JsonParser();
        return (T) jsonParser.parse( parameters, parameterClass );
    }

    /**
     * Displays a notification
     *
     * @param context Android Context, passed from the calling entity
     * @param title title for the notification
     * @param msg message and ticker for the notification
     * @param type action type that will be put as an extra into the result Intent (<code>resultIntent.putExtra("type", type);</code>)
     * @param notificationIconResId notification icon resource id
     * @param notificationId an identifier for this notification as in {@link android.app.NotificationManager#notify(int, android.app.Notification)}
     */
    public static void displayNotification( Context context, String title, String msg, String type, int notificationIconResId, int notificationId ) {
        L.d(".");

        if( BLEKit.getTargetActivityForNotifications()==null ) {
            throw new IllegalArgumentException( "Target activity for notifications is not set. Call BLEKit.getTargetActivityForNotifications() first." );
        }

        Notification.Builder builder = new Notification.Builder(context)
                .setContentText(msg)
                .setTicker(msg)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSmallIcon(notificationIconResId);

        Intent resultIntent = new Intent(context, BLEKit.getTargetActivityForNotifications() );
        resultIntent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
        resultIntent.putExtra("type", type);

        PendingIntent resultPendingIntent = PendingIntent.getActivity( context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, builder.build());
    }

    /**
     * Displays a notification
     *
     * @param context Android Context, passed from the calling entity
     * @param title title for the notification
     * @param msg message and ticker for the notification
     * @param type action type that will be put as an extra into the result Intent (<code>resultIntent.putExtra("type", type);</code>)
     * @param notificationIconResId notification icon resource id
     */
    public static void displayNotification( Context context, String title, String msg, String type, int notificationIconResId ) {
        displayNotification( context, title, msg, type, notificationIconResId, 1 );
    }

}
