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

import com.upnext.blekit.actions.BaseNotificationParams;

/**
 * Parameters for Facebook check-in action.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class FacebookCheckinActionParams extends BaseNotificationParams {

    /**
     * Facebook place_id - place to which the user will be checked into
     */
    public String place_id;

    /**
     * Message that will be posted with check-in on Facebook.
     */
    public String message;

    /**
     * Privacy setting for check-in.
     * One of the valid values provided by Facebook (for the moment of writing this the valid values are: enum{'EVERYONE', 'ALL_FRIENDS', 'FRIENDS_OF_FRIENDS', 'CUSTOM', 'SELF'})
     */
    public String privacy;

    @Override
    public String toString() {
        return "FacebookCheckinActionParams{" +
                "place_id='" + place_id + '\'' +
                ", notification_title='" + notification_title + '\'' +
                ", notification_message='" + notification_message + '\'' +
                ", message='" + message + '\'' +
                ", privacy='" + privacy + '\'' +
                '}';
    }
}
