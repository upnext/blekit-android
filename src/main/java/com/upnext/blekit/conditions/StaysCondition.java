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

import android.os.Handler;
import android.os.Message;

import com.upnext.blekit.ActionsFactory;
import com.upnext.blekit.BLEKit;
import com.upnext.blekit.BackgroundMode;
import com.upnext.blekit.BeaconEvent;
import com.upnext.blekit.actions.BLEAction;
import com.upnext.blekit.model.Beacon;
import com.upnext.blekit.util.L;

import java.util.HashMap;
import java.util.Map;

/**
 * Condition checking whether a given amount of time between {@link com.upnext.blekit.BeaconEvent#REGION_ENTER} and
 * {@link com.upnext.blekit.BeaconEvent#REGION_LEAVE} has passed without any {@link com.upnext.blekit.BeaconEvent#REGION_LEAVE} in the meantime
 * (so the user stayed in the proximity of the beacon for the given amount of time).
 *
 * Upon first evaluation a countdown timer is fired. When it reaches 0, the action is performed.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class StaysCondition extends BLECondition<StaysParams> {

    public static final String TYPE = "stays";

    private static Map<String, StaysCondition> mEnteredRegions = new HashMap<String, StaysCondition>();

    private static StaysHandler staysHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE;
    }

    class StaysHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Beacon beacn = (Beacon) msg.obj;
            L.d( mEnteredRegions.containsKey(beacn.id) + " " + beacn.id );
            if( mEnteredRegions.containsKey(beacn.id) ) {
                StaysCondition condition = mEnteredRegions.get(beacn.id);
                processEvent(condition);
                mEnteredRegions.remove(beacn.id);
            }
        }
    }

    public StaysCondition() {
        if( staysHandler==null )
            staysHandler = new StaysHandler();
    }

    private void processEvent(StaysCondition condition) {
        L.d( "starting to process after stayed for " + condition.parameters.interval );
        final ActionsFactory actionsFactory = BLEKit.getActionsFactory();
        BLEAction bleAction = actionsFactory.get( condition.trigger.action.type, condition.trigger.action.parameters );
        if( bleAction==null ) {
            L.d("Did not find action implementation for type '" + condition.trigger.action.type + "'");
        }
        bleAction.performAction( condition.context, new BackgroundMode(null, false) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean evaluate() {
        L.d(parameters);
        if( BeaconEvent.REGION_ENTER.equals(beaconEvent) ) {

            mEnteredRegions.put( beacon.id, this );
            Message msg = Message.obtain();
            msg.obj = beacon;
            staysHandler.sendMessageDelayed( msg, parameters.interval*1000 );
            L.d( "'stays' will be triggered in " + parameters.interval + " seconds" );

        } else if( BeaconEvent.REGION_LEAVE.equals(beaconEvent) ) {

            mEnteredRegions.remove( beacon.id );

        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<StaysParams> getParameterClass() {
        return StaysParams.class;
    }

    /**
     * Valid only for events {@link com.upnext.blekit.BeaconEvent#REGION_ENTER} and {@link com.upnext.blekit.BeaconEvent#REGION_LEAVE}.
     *
     * @param beaconEvent event
     * @return <code>true</code> if event is {@link com.upnext.blekit.BeaconEvent#REGION_ENTER} or {@link com.upnext.blekit.BeaconEvent#REGION_LEAVE}
     */
    @Override
    public boolean isValidForEvent(BeaconEvent beaconEvent) {
        return beaconEvent!=null && (beaconEvent==BeaconEvent.REGION_ENTER || beaconEvent==BeaconEvent.REGION_LEAVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BLECondition getInstance() {
        return new StaysCondition();
    }
}
