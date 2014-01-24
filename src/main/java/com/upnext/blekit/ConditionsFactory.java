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
package com.upnext.blekit;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.upnext.blekit.conditions.BLECondition;
import com.upnext.blekit.conditions.CameFarCondition;
import com.upnext.blekit.conditions.CameImmediateCondition;
import com.upnext.blekit.conditions.CameNearCondition;
import com.upnext.blekit.conditions.EnterCondition;
import com.upnext.blekit.conditions.HttpOkCondition;
import com.upnext.blekit.conditions.LeaveCondition;
import com.upnext.blekit.conditions.StaysCondition;
import com.upnext.blekit.util.JsonParser;
import com.upnext.blekit.util.L;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory providing conditions ({@link com.upnext.blekit.conditions.BLECondition}) for BLEKit.
 *
 * When the factory is instantiated, there are basic conditions provided by deafult:
 * <ul>
 *  <li>{@link com.upnext.blekit.conditions.EnterCondition}
 *  <li>{@link com.upnext.blekit.conditions.LeaveCondition}
 *  <li>{@link com.upnext.blekit.conditions.CameFarCondition}
 *  <li>{@link com.upnext.blekit.conditions.CameNearCondition}
 *  <li>{@link com.upnext.blekit.conditions.CameImmediateCondition}
 *  <li>{@link com.upnext.blekit.conditions.HttpOkCondition}
 *  <li>{@link com.upnext.blekit.conditions.StaysCondition}
 * </ul>
 *
 * To add a custom condition to the factory, call {@link #addCondition(com.upnext.blekit.conditions.BLECondition)} passing your implementation as the parameter.
 *
 * In case you want to overwrite a default condition with the same type ({@link com.upnext.blekit.conditions.BLECondition#getType()}, you have to remove it from the
 * factory first - {@link #remove(String)}, eg. to remove HttpOkCondition:
 * <pre>
 * {@code
 * BLEKit
 *   .create(ctx)
 *   //other calls here
 *   .removeConditionByType( HttpOkCondition.TYPE )
 *   .start(ctx);
 * }
 * </pre>
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class ConditionsFactory {

    private Map<String, BLECondition> conditionsMap;
    private JsonParser jsonParser = new JsonParser();

    /**
     * Default constructor.
     * Instantiates the factory and provides basic conditions.
     */
    public ConditionsFactory() {
        conditionsMap = new HashMap<String, BLECondition>();

        provideBasicConditions();
    }

    /**
     * Add condition implementation to the factory.
     *
     * @param condition condition implementation
     * @throws IllegalArgumentException thrown if condition with given type already exists
     */
    public void addCondition( BLECondition condition ) {
        if( condition==null ) return;
        if( conditionsMap.containsKey( condition.getType() ) ) {
            throw new IllegalArgumentException( "Condition with given type already exists: " + condition.getType() );
        }
        conditionsMap.put( condition.getType(), condition );
    }

    /**
     * Returns condition intance of given type for given event.
     *
     * @param type type of condition (eg. enter, leave, etc.)
     * @param beaconEvent type of event that triggered this condition; condition should return true in its {@link com.upnext.blekit.conditions.BLECondition#isValidForEvent(BeaconEvent)} in order to be returned
     * @param parameters parameters specified in JSON configuration to be provided to condition
     * @param expression optional expression specified in JSON configuration to be provided to condition
     * @param context context
     *
     * @return condition instance if found or null if not found
     */
    public BLECondition get( String type, BeaconEvent beaconEvent, JsonNode parameters, String expression, Context context ) {
        BLECondition condition = conditionsMap.get(type);

        if( condition==null || !condition.isValidForEvent(beaconEvent) ) {
            return null;
        }

        BLECondition conditionInstance = condition.getInstance();

        conditionInstance.setBeaconEvent( beaconEvent );
        conditionInstance.setExpression(expression);
        conditionInstance.setContext( context );
        conditionInstance.setParameters(jsonParser.parse(parameters, condition.getParameterClass()));

        return conditionInstance;
    }

    /**
     * Returns condition intance of given type.
     *
     * @param type type of condition (eg. enter, leave, etc.)
     *
     * @return condition instance if found or null if not found
     */
    public BLECondition get( String type ) {
        return conditionsMap.get(type);
    }

    /**
     * Removes action with given type from factory
     *
     * @param type action type
     */
    public void remove( String type ) {
        conditionsMap.remove(type);
    }

    private void provideBasicConditions() {
        final EnterCondition enterCondition = new EnterCondition();
        conditionsMap.put(enterCondition.getType(), enterCondition);

        final LeaveCondition leaveCondition = new LeaveCondition();
        conditionsMap.put(leaveCondition.getType(), leaveCondition);

        final CameFarCondition cameFarCondition = new CameFarCondition();
        conditionsMap.put(cameFarCondition.getType(), cameFarCondition);

        final CameNearCondition cameNearCondition = new CameNearCondition();
        conditionsMap.put(cameNearCondition.getType(), cameNearCondition);

        final CameImmediateCondition cameImmediateCondition = new CameImmediateCondition();
        conditionsMap.put(cameImmediateCondition.getType(), cameImmediateCondition);

        final HttpOkCondition httpOkCondition = new HttpOkCondition();
        conditionsMap.put(httpOkCondition.getType(), httpOkCondition);

        final StaysCondition staysCondition = new StaysCondition();
        conditionsMap.put(staysCondition.getType(), staysCondition);
    }
}
