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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upnext.blekit.model.Zone;

import java.io.IOException;

/**
 * JSON parser class, used to deserialize JSON into Objects.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class JsonParser {

    private ObjectMapper objectMapper;

    /**
     * Constructor, initializes the obejct mapper, will not fail on unknown properties.
     */
    public JsonParser() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Deserializes given JSON into {@link com.upnext.blekit.model.Zone}
     *
     * @param zoneJSON zone as plain JSON
     * @return zone as object
     */
    public Zone parse( String zoneJSON ) {
        try {
            return objectMapper.readValue(zoneJSON, Zone.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deserializes given jsonObject into an object of given class.
     *
     * @param jsonObject json object
     * @param valueType class type for deserialization
     * @param <T> class type
     * @return object instance
     */
    public <T>T parse( JsonNode jsonObject, Class<T> valueType ) {
        if( jsonObject==null ) return null;
        try {
            return objectMapper.readValue( ""+jsonObject, valueType );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
