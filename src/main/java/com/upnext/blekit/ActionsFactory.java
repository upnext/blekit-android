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

import com.fasterxml.jackson.databind.JsonNode;
import com.upnext.blekit.actions.AlertAction;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.actions.ContentAction;
import com.upnext.blekit.actions.NotificationAction;
import com.upnext.blekit.actions.YelpAction;
import com.upnext.blekit.actions.facebook.FacebookCheckinAction;
import com.upnext.blekit.actions.foursquare.FoursquareCheckinAction;
import com.upnext.blekit.util.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory providing actions ({@link com.upnext.blekit.actions.BLEAction}) for BLEKit.
 *
 * When the factory is instantiated, there are basic actions provided by deafult:
 * <ul>
 *  <li>{@link com.upnext.blekit.actions.AlertAction}
 *  <li>{@link com.upnext.blekit.actions.ContentAction}
 *  <li>{@link com.upnext.blekit.actions.facebook.FacebookCheckinAction}
 *  <li>{@link com.upnext.blekit.actions.foursquare.FoursquareCheckinAction}
 *  <li>{@link com.upnext.blekit.actions.NotificationAction}
 *  <li>{@link com.upnext.blekit.actions.YelpAction}
 * </ul>
 *
 * To add a custom action to the factory, call {@link #addAction(com.upnext.blekit.actions.BLEAction)} passing your implementation as the parameter.
 *
 * In case you want to overwrite a default action with the same type ({@link com.upnext.blekit.actions.BLEAction#getType()}, you have to remove it from the
 * factory first - {@link #remove(String)}, eg. to remove Facebook check-in action call:
 * <pre>
 * {@code
 * BLEKit
 *   .create(ctx)
 *   //other calls here
 *   .removeActionByType( FacebookCheckinAction.TYPE )
 *   .start(ctx);
 * }
 * </pre>
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class ActionsFactory {

    private Map<String, BLEAction> actionsMap;
    private JsonParser jsonParser = new JsonParser();

    /**
     * Default constructor.
     * Instantiates the factory and provides basic actions.
     */
    public ActionsFactory() {
        actionsMap = new HashMap<String, BLEAction>();

        provideBasicActions();
    }

    /**
     * Add action implementation to the factory.
     *
     * @param action action implementation
     * @throws IllegalArgumentException thrown if action with given type already exists
     */
    public void addAction(BLEAction action) throws IllegalArgumentException {
        if( action==null ) return;
        if( actionsMap.containsKey( action.getType() ) ) {
            throw new IllegalArgumentException( "Action with given type already exists: " + action.getType() );
        }
        actionsMap.put(action.getType(), action);
    }

    /**
     * Returns action implementation of given type.
     *
     * @param type type of action (eg. alert, facebook-chekin, yelp, etc.)
     * @param parameters parameters specified in JSON configuration to be provided to condition
     *
     * @return action instance if found or null if not found
     */
    public BLEAction get( String type, JsonNode parameters ) {
        BLEAction action = actionsMap.get(type);

        if( action==null ) {
            return null;
        }

        action.setParameters(jsonParser.parse(parameters, action.getParameterClass()));

        return action;
    }

    /**
     * Returns action implementation of given type.
     *
     * @param type type of action (eg. alert, facebook-chekin, yelp, etc.)
     *
     * @return action instance if found or null if not found
     */
    public BLEAction get( String type ) {
        return actionsMap.get(type);
    }

    /**
     * Removes action with given type from factory
     *
     * @param type action type
     */
    public void remove( String type ) {
        actionsMap.remove(type);
    }

    private void provideBasicActions() {
        AlertAction alertAction = new AlertAction();
        actionsMap.put(alertAction.getType(), alertAction);

        ContentAction contentAction = new ContentAction();
        actionsMap.put(contentAction.getType(), contentAction);

        FacebookCheckinAction facebookCheckinAction = new FacebookCheckinAction();
        actionsMap.put(facebookCheckinAction.getType(), facebookCheckinAction);

        FoursquareCheckinAction foursquareCheckinAction = new FoursquareCheckinAction();
        actionsMap.put(foursquareCheckinAction.getType(), foursquareCheckinAction);

        NotificationAction notificationAction = new NotificationAction();
        actionsMap.put(notificationAction.getType(), notificationAction);

        YelpAction yelpAction = new YelpAction();
        actionsMap.put(yelpAction.getType(), yelpAction);
    }
}
