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

import android.os.Parcel;
import android.os.Parcelable;

import com.upnext.blekit.actions.BaseNotificationParams;

/**
 * Parameters for foursquare check-in action.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FoursquareCheckinActionParams extends BaseNotificationParams implements Parcelable {

    /**
     * The venue where the user is checking in. Find venue IDs by searching or from historical APIs.
     */
    public String venue_id;

    /**
     * A message about your check-in. The maximum length of this field is 140 characters.
     */
    public String message;

    /**
     * Who to broadcast this check-in to. One of private,public,facebook,twitter,followers. If no valid value is found, the default is followers.
     */
    public String broadcast;

    /**
     * Foursquare registered application Client ID
     */
    public String client_id;

    /**
     * Foursquare registered application Client Secret
     */
    public String secret_code;

    @Override
    public int describeContents() {
        return 0;
    }

    public FoursquareCheckinActionParams() {}

    public FoursquareCheckinActionParams(Parcel in) {
        notification_title = in.readString();
        notification_message = in.readString();
        venue_id = in.readString();
        message = in.readString();
        broadcast = in.readString();
        client_id = in.readString();
        secret_code = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( notification_title );
        dest.writeString( notification_message );
        dest.writeString( venue_id );
        dest.writeString( message );
        dest.writeString( broadcast );
        dest.writeString( client_id );
        dest.writeString( secret_code );
    }

    public static final Parcelable.Creator<FoursquareCheckinActionParams> CREATOR
            = new Parcelable.Creator<FoursquareCheckinActionParams>() {
        @Override
        public FoursquareCheckinActionParams createFromParcel(Parcel source) {
            return new FoursquareCheckinActionParams(source);
        }

        @Override
        public FoursquareCheckinActionParams[] newArray(int size) {
            return new FoursquareCheckinActionParams[size];
        }
    };
}
