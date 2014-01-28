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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.upnext.blekit.R;

/**
 * Action of type 'alert'.
 * In the foreground a dialog is displayed with title and message from the parameters.
 * In the background a notification is displayed with title and message/ticker from the parameters.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class AlertAction extends BLEAction<AlertActionParams> {

    public static final int NOTIFICATION_ID = 2414234;

    public static final String TYPE = "alert";

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
    public Class<AlertActionParams> getParameterClass() {
        return AlertActionParams.class;
    }

    /**
     * Displays a notification.
     *
     * @param context Android Context, passed from the calling entity
     */
    @Override
    public void performInBackground(Context context) {
        displayNotification(context);
    }

    /**
     * Shows a dialog.
     *
     * @param activity activity on which behalf this action performs in foreground
     */
    @Override
    public void performInForeground( Activity activity ) {
        DialogFragment dialog = AlertDialogFragment.newInstance( parameters.title, parameters.message );
        dialog.show(activity.getFragmentManager(), "dialog");
    }

    /**
     * Shows a dialog.
     *
     * @param intent Intent
     * @param activity Android Activity
     */
    @Override
    public void processIntent(Intent intent, Activity activity) {
        if( intent==null ) return;
        String type = intent.getStringExtra("type");
        if( type!=null && type.equals(getType()) ) {
            performInForeground(activity);
        }
    }

    protected void displayNotification(Context context) {
        displayNotification( context, parameters.title, parameters.message, TYPE, R.drawable.ic_notification, NOTIFICATION_ID );
    }

    public static class AlertDialogFragment extends DialogFragment {

        public static AlertDialogFragment newInstance(String title, String message) {
            AlertDialogFragment frag = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString( "title", title );
            args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(true)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
    }

}
