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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.upnext.blekit.util.L;
import com.upnext.blekit.util.http.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper Activity for Foursquare interaction.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FoursquareCheckinActivity extends Activity {

    private static final int REQUEST_CODE_FSQ_CONNECT = 1;
    private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 2;

    private FoursquareCheckinActionParams params;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( getIntent()==null ) {
            finish();
            return;
        }

        params = getIntent().getParcelableExtra( "params" );
        if( params==null ) {
            finish();
            return;
        }

        Intent intent = FoursquareOAuth.getConnectIntent(this, params.client_id);
        boolean isPlay = FoursquareOAuth.isPlayStoreIntent(intent);
        L.d( "isPlay " + isPlay );
        startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_FSQ_CONNECT:
                AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
                Intent intent = FoursquareOAuth.getTokenExchangeIntent(
                        this,
                        params.client_id,
                        params.secret_code,
                        codeResponse.getCode());
                startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
                break;

            case REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
                AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
                L.d( "tokenResponse " + tokenResponse.getAccessToken() );
                if( tokenResponse.getException()==null && tokenResponse.getAccessToken()!=null ) {
                    checkin(tokenResponse.getAccessToken());
                }

                break;
        }
    }

    private void checkin(String accessToken) {
        this.accessToken = accessToken;
        CheckinAsyncTask task = new CheckinAsyncTask();
        task.execute();
    }

    private class CheckinAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... p) {
            HttpClient client = new HttpClient( "https://api.foursquare.com/v2/checkins/add" );
            Map<String, String> reqParams = new HashMap<String, String>();
            reqParams.put( "oauth_token", accessToken );
            reqParams.put( "venueId", params.venue_id );
            reqParams.put( "broadcast", params.broadcast );
            reqParams.put( "shout", params.message );
            client.post( Void.class, reqParams );

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }
}
