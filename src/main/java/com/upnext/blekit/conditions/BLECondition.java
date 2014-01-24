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

import android.content.Context;

import com.upnext.blekit.BeaconEvent;
import com.upnext.blekit.model.Beacon;
import com.upnext.blekit.model.Trigger;
import com.upnext.blekit.model.Zone;
import com.upnext.blekit.util.ExpressionEvaluator;

import java.util.Map;

/**
 * Base class for conditions, used to evaluate {@link com.upnext.blekit.BeaconEvent}.
 *
 *
 *
 * @see com.upnext.blekit.ConditionsFactory
 * @see com.upnext.blekit.BeaconEvent
 * @author Roman Wozniak (roman@up-next.com)
 */
public abstract class BLECondition<T> {

    protected BeaconEvent beaconEvent;
    protected String expression;
    protected Zone zone;
    protected Trigger trigger;
    protected Beacon beacon;
    protected Context context;
    protected T parameters;

    /**
     * Returns type for given condition.
     * It is matched against type in JSON ("type":"_action_type_") and should be exactly the same (case sensitive).
     *
     * @return type for this action
     */
    public abstract String getType();

    /**
     * Sets parametrs for this condition.
     *
     * @param parameters parameters instance
     */
    public void setParameters(T parameters) {
        this.parameters = parameters;
    }

    /**
     * Evaluates event.
     * If {@link com.upnext.blekit.conditions.BLECondition#expression} is not <code>null</code>, then it will be evaluated.
     * //TODO link to page explaining expressions
     *
     * If {@link com.upnext.blekit.conditions.BLECondition#expression} is <code>null</code>, then regular check will take place - {@link #evaluate()} will be called.
     *
     * @return <code>true</code> if condition is met, <code>false</code> otherwise
     */
    public boolean conditionMet() {
        if( expression==null || expression.trim().equals("") ) {
            return evaluate();
        } else {
            ExpressionEvaluator evaluator = new ExpressionEvaluator();
            evaluator.setZone(zone);
            evaluator.setExpression(expression);
            evaluator.putProperty("trigger", new Helper(getType()));
            evaluator.putProperties( getExpressionParameters() );

            return evaluator.eval();
        }
    }

    /**
     * Performs actual evaluation of condition.
     *
     * @return <code>true</code> if condition is met, <code>false</code> otherwise
     */
    protected abstract boolean evaluate();

    /**
     * Sets beacon event that triggered this condition.
     *
     * @param event beacon event
     */
    public void setBeaconEvent( BeaconEvent event ) {
        beaconEvent = event;
    }

    /**
     * Returns beacon event that triggered this condition.
     *
     * @return beacon event
     */
    public BeaconEvent getBeaconEvent() {
        return beaconEvent;
    }

    /**
     * Returns parameter class.
     * If this condition does not support parameters, then Void.class should be returned.
     *
     * Returned class is used for JSON deserialization, so the structure of the class should be the same as JSON object with parameters.
     * For more help see {@link com.fasterxml.jackson.databind.ObjectMapper#readValue(String, Class)}.
     *
     * @return parameter class
     */
    public abstract Class<T> getParameterClass();

    /**
     * Method for providing additional expression parameters.
     * Map structure:
     *  - key is the name of the parameter that will be used in the expression
     *  - value is an object with fields (or a primitive)
     *
     * Example:
     * <pre>
     * {@code
     * // Parameter obejct
     * public class MyExpressionParam {
     *     public int param1;
     *     public String param2;
     * }
     *
     * //getExpressionParameters implementation
     * protected Map<String, Object> getExpressionParameters() {
     *     Map<String, Object> params = new HashMap<String, Object>();
     *     MyExpressionParam p = new MyExpressionParam();
     *     p.param1 = 5;
     *     p.param2 = "blekit";
     *     params.put( "custom_param",  p);
     *     return params;
     * }
     * }
     *
     * </pre>
     *
     * Then in the expression it can be used as <code>custom_param.param1>2 && custom_param.param2=='blekit'</code>
     *
     *
     * @return additional expression parameters
     */
    protected Map<String, Object> getExpressionParameters() {
        return null;
    }

    /**
     * Sets an expression for this condition.
     *
     * @param expression expression
     */
    public void setExpression( String expression ) {
        this.expression = expression;
    }

    /**
     * Sets zone for this condition.
     *
     * @param zone zone
     * @see com.upnext.blekit.model.Zone
     */
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    /**
     * Sets trigger for this condition.
     *
     * @param trigger trigger
     * @see com.upnext.blekit.model.Trigger
     */
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    /**
     * Sets beacon for for this condition.
     *
     * @param beacon beacon
     * @see com.upnext.blekit.model.Beacon
     */
    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    /**
     * Sets Android Context
     *
     * @param context Android Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Checks whether given {@link com.upnext.blekit.BeaconEvent} is valid for this condition.
     * This method speeds up conditions checking for events that should only occur for one type of event (eg. facebook check-in should in most cases happen only on 'enter' condition).
     *
     * @param beaconEvent event
     * @return <code>true</code> if event is valid for this condition, <code>false</code> otherwise
     */
    public boolean isValidForEvent(BeaconEvent beaconEvent) {
        return false;
    }

    /**
     * Returns a new instance of this condition.
     *
     * @return condition instance
     */
    public abstract BLECondition getInstance();

    /**
     * Copies values from given condition.
     *
     * @param bleCondition pre-filled condition
     */
    public void copy( BLECondition bleCondition ) {
        this.setContext( bleCondition.context );
        this.setParameters((T) bleCondition.parameters);
        this.setZone(bleCondition.zone);
        this.setTrigger(bleCondition.trigger);
        this.setBeacon( bleCondition.beacon );
        this.setExpression( bleCondition.expression );
        this.setBeaconEvent( bleCondition.beaconEvent );
    }

    /**
     * Helper class for expression evaluator
     */
    public static class Helper {
        public String type;

        public Helper(String type) {
            this.type = type;
        }
    }
}
