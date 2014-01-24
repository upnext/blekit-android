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
package com.upnext.blekit.util.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Map;

/**
 * Util class for HTTP communication
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class HttpUtils {

    /**
     * Checks whether there is a valid Internet connection available on the device.
     *
     * @param ctx Android Context, passed from the calling entity
     * @return <code>true</code> if device is online
     */
    public static boolean isOnline( Context ctx ) {
        if( ctx==null ) return false;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Checks whether a HTTP GET request for given url returns HTTP OK (200) response.
     *
     * @param url url
     * @param params optional HTTP params
     * @return <code>true</code> if HTTP OK received, <code>false</code> otherwise
     */
    public static boolean isHttpOk( String url, Map<String, String> params ) {
        HttpClient client = new HttpClient( url );
        Response<Void> response = client.get( Void.class, params );
        return response!=null && !response.hasError();
    }

}
