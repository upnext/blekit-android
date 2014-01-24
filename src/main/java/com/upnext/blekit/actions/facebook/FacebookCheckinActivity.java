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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.upnext.blekit.util.L;

import java.util.Arrays;

/**
 * Helper Activity for logging into Facebook.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FacebookCheckinActivity extends Activity {

    private String placeId;
    private String postMessage;
    private String privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( getIntent()!=null ) {
            placeId = getIntent().getStringExtra("place_id");
            postMessage = getIntent().getStringExtra("post_message");
            privacy = getIntent().getStringExtra("privacy");
        }

        L.d( "place_id= " + placeId );
        L.d( "post_message= " + postMessage );
        L.d( "privacy= " + privacy );

        if ( Session.getActiveSession() != null ) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }

        final Session session = new Session.Builder(this).build();
        Session.setActiveSession(session);

        final Session.OpenRequest openRequest = new Session.OpenRequest(this)
                .setDefaultAudience(SessionDefaultAudience.ONLY_ME)
                .setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK)
                .setPermissions( Arrays.asList("basic_info") );

        L.d( "opening for callback" );

        openRequest.setCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                L.d( "call " + state + " " + exception );
                if (state == SessionState.OPENED) {
                    final Session currentSession = Session.getActiveSession();
                    if (currentSession != null) {

                        requestPublish(currentSession);

                    }
                } else if(state == SessionState.OPENED_TOKEN_UPDATED) {
                    final Session currentSession = Session.getActiveSession();
                    if (currentSession != null) {

                        checkin(currentSession);

                    }
                } else if (state == SessionState.CLOSED_LOGIN_FAILED) {
                    session.closeAndClearTokenInformation();
                    // Possibly finish the activity
                } else if (state == SessionState.CLOSED) {
                    session.close();
                    // Possibly finish the activity
                }
            }
        });
        session.openForPublish(openRequest);

    }

    private void requestPublish(Session session) {
        L.d("requestPublish");

        session.requestNewPublishPermissions(
                new Session.NewPermissionsRequest(FacebookCheckinActivity.this, Arrays.asList("publish_actions"))
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.d("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        final Session activeSession = Session.getActiveSession();
        if ( activeSession != null ) activeSession.onActivityResult(this, requestCode, resultCode, data);
    }

    private void checkin(Session session) {
        L.d( "checking in with " + placeId );
        Bundle params = new Bundle();
        params.putString("message", postMessage);
        if( privacy!=null ) {
            params.putString("privacy", "{\"value\":\"" + privacy + "\"}");
        } else {
            params.putString("privacy", "{\"value\":\"SELF\"}");
        }

        params.putString("place", placeId+"");
        new Request(
                session,
                "/me/feed",
                params,
                HttpMethod.POST,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        L.d("finished " + response);
                        if( response.getError()!=null ) {
                            Toast.makeText( FacebookCheckinActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_LONG ).show();
                        }
                        FacebookCheckinActivity.this.finish();
                    }
                }
        ).executeAsync();
    }

}
