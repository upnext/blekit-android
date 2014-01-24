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

import com.upnext.blekit.model.Zone;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

/**
 * Class used for evaluating expressions in conditions {@link com.upnext.blekit.conditions.BLECondition#expression}
 *
 * @see com.upnext.blekit.conditions.BLECondition
 * @author Roman Wozniak (roman@up-next.com)
 */
public class ExpressionEvaluator {

    private Scriptable scope;
    private Context ctx;

    private Zone zone;
    private String expression;

    public ExpressionEvaluator() {
        ctx = Context.enter();
        ctx.setOptimizationLevel(-1);
        scope = ctx.initStandardObjects();
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean eval() {
        boolean result = false;
        try {
            ScriptableObject.putProperty( scope, "zone", Context.javaToJS(zone, scope) );

            Object r = ctx.evaluateString( scope, expression, "expression", 1, null );
            L.d(r);
            if( r instanceof Boolean ) {
                L.d("boolean");
                result = (Boolean) r;
            } else {
                result = false;
            }
        } finally {
            Context.exit();
        }
        return result;
    }

    public void putProperties( Map<String, Object> properties ) {
        if( properties==null ) return;
        for( String key : properties.keySet() ) {
            ScriptableObject.putProperty( scope, key, Context.javaToJS(properties.get(key), scope) );
        }
    }

    public void putProperty( String key, Object value ) {
        ScriptableObject.putProperty( scope, key, Context.javaToJS(value, scope) );
    }
}
