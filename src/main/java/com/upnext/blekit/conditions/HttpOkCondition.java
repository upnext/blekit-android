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
package com.upnext.blekit.conditions;

import com.upnext.blekit.BeaconEvent;
import com.upnext.blekit.util.http.HttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Performs an HTTP GET call to given url with optional username parameter.
 * Condition is met only if HTTP response code 200 was returned.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class HttpOkCondition extends BLECondition<HttpOkParams> {

    public static final String TYPE = "httpOk";

    private static final String PARAM_USERNAME = "username";

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
    protected boolean evaluate() {
        if( HttpUtils.isOnline(context) ) {
            Map<String, String> params = new HashMap<String, String>();
            if( parameters.username!=null ) {
                params.put( PARAM_USERNAME, parameters.username );
            }
            return HttpUtils.isHttpOk( parameters.url, params );
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<HttpOkParams> getParameterClass() {
        return HttpOkParams.class;
    }

    /**
     * Always valid.
     *
     * @param beaconEvent event
     * @return returns <code>true</code>
     */
    @Override
    public boolean isValidForEvent(BeaconEvent beaconEvent) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BLECondition getInstance() {
        return new HttpOkCondition();
    }

}
