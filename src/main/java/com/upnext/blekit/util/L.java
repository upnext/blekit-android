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
package com.upnext.blekit.util;

import android.util.Log;

/**
 * Helper class for logging
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class L {

    /**
     * Set to true if you want to see debug messages associated with this library
     */
    public static final boolean DEBUG_ENABLED = true;

    private static final String APP_NAME = "BLEKIT";

    public static void d( String msg ) {
        debug(  msg );
    }

    public static void d( Object obj ) {
        if( obj != null )
            debug( obj.toString() );
    }

    public static void e( String msg ) {
        Log.e(APP_NAME, msg);
    }

    public static void e( String msg, Exception e ) {
        Log.e( APP_NAME, msg, e );
    }

    private static void debug( String msg ) {
        if( DEBUG_ENABLED ) {
            StackTraceElement[] els = Thread.currentThread().getStackTrace();
            String className = els[4].getClassName();
            className = className.substring( className.lastIndexOf( "." )+1 );
            Log.d( APP_NAME, "[" + className + "." + els[4].getMethodName() + "] " + msg );
        }
    }

}
