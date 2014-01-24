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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.upnext.blekit.BeaconEvent;
import com.upnext.blekit.EventOccurenceUnit;

import java.util.Date;

/**
 * Local database used for storing BLEKit entities like events for counting their occurences.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class BeaconsDB extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "BeaconsDB";

    public BeaconsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_BEACON_EVENTS_TABLE = "CREATE TABLE " + TABLE_BEACON_EVENTS + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_EVENT + " TEXT, "+
                KEY_BEACON_ID + " TEXT, "+
                KEY_DATE + " INTEGER )";
        db.execSQL(CREATE_BEACON_EVENTS_TABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACON_EVENTS);
        this.onCreate(db);
    }

    private static final String TABLE_BEACON_EVENTS = "beacon_events";

    private static final String KEY_ID = "id";
    private static final String KEY_EVENT = "event";
    private static final String KEY_BEACON_ID = "beacon_id";
    private static final String KEY_DATE = "date";

    private static final String[] COLUMNS = {KEY_ID,KEY_EVENT,KEY_BEACON_ID,KEY_DATE};


    /**
     * Adds an event do the events database
     *
     * @param event beacon event
     * @param beaconId beacon identifier
     */
    public void addBeaconEvent( BeaconEvent event, String beaconId ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT, event.name());
        values.put(KEY_BEACON_ID, beaconId);
        values.put(KEY_DATE, new Date().getTime());
        db.insert(TABLE_BEACON_EVENTS, null, values);
        db.close();
    }

    /**
     * Returns the number of occurences of event for given beacon id in unit of time.
     *
     * @param event beacon event
     * @param beaconId beacon id
     * @param occurenceUnit occurence unit
     * @return number of occurences
     */
    public int getNumOccurencesForBeaconInTime( BeaconEvent event, String beaconId, EventOccurenceUnit occurenceUnit ) {
        if( occurenceUnit==null ) return 0;

        String query = "SELECT count(*) FROM " + TABLE_BEACON_EVENTS + " WHERE " +
                KEY_BEACON_ID + "='" + beaconId + "' AND " +
                KEY_EVENT + "='" + event.name() + "' AND " +
                KEY_DATE + " BETWEEN " + getStartDateForOccurence(occurenceUnit) + " AND " + new Date().getTime();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        return count;
    }

    public void listAll() {
        String query = "SELECT * FROM " + TABLE_BEACON_EVENTS;
        L.d(query);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if( cursor.moveToFirst() ) {
            do {
                L.d( Integer.parseInt(cursor.getString(0)) +
                        " : " + cursor.getString(1) +
                        " : " + cursor.getString(2) +
                        " : " + cursor.getLong(3)
                );
            } while (cursor.moveToNext());
        }

    }


    private static final long MILLIS_HOUR = 60*60*1000;
    private static final long MILLIS_DAY = MILLIS_HOUR*24;
    private static final long MILLIS_MONTH = MILLIS_DAY*30;
    private static final long MILLIS_YEAR = MILLIS_DAY*365;

    private long getStartDateForOccurence( EventOccurenceUnit occurenceUnit ) {
        long now = new Date().getTime();
        switch (occurenceUnit) {
            case HOUR:
                return now - MILLIS_HOUR;

            case DAY:
                return now - MILLIS_DAY;

            case MONTH:
                return now - MILLIS_MONTH;

            case YEAR:
                return now - MILLIS_YEAR;

            case TOTAL:
                return 0;
        }
        return 0;
    }

}
