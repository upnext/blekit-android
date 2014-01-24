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
package com.upnext.blekit.util.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upnext.blekit.util.L;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Http client class used for creating simple HTTP requests.
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class HttpClient {

    private final static String TAG = HttpClient.class.getSimpleName();
    private final static boolean LOG_RESPONSE = true;
    private String url;
    private final ObjectMapper objectMapper;

    public HttpClient(String url) {
        this.url = url;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private String urlWithParams(String url, Map<String, String> postParams) throws UnsupportedEncodingException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(url);
        boolean isFirst = true;
        if ( postParams != null ) {
            for ( String k: postParams.keySet() ) {
                urlBuilder.append(isFirst ? "?" : "&");
                isFirst = false;
                urlBuilder.append(URLEncoder.encode(k, "UTF-8"));
                urlBuilder.append("=");
                String v = postParams.get(k);
                urlBuilder.append(URLEncoder.encode(v, "UTF-8"));
            }
        }
        return urlBuilder.toString();
    }

    public <T> Response<T> get(Class<T> clazz,  Map<String, String> params) {
        return fetchResponse(clazz, null, params, "GET");
    }

    public <T> Response<T> post(Class<T> clazz,  Map<String, String> params) {
        return fetchResponse(clazz, null, params, "POST");
    }

    public <T> Response<T> put(Class<T> clazz,  Map<String, String> params) {
        return fetchResponse(clazz, null, params, "PUT");
    }

    public <T> Response<T> get(Class<T> clazz, String path, Map<String, String> params) {
        return fetchResponse(clazz, path, params, "GET");
    }

    public <T> Response<T> post(Class<T> clazz, String path, Map<String, String> params, String payload) {
        return fetchResponse(clazz, path, params, "POST", payload);
    }

    public <T> Response<T> post(Class<T> clazz, String path, Map<String, String> params, Object payload) {

        if(payload instanceof String) {
            return fetchResponse(clazz, path, params, "POST", (String)payload);
        }

        try {
            String payloadString = objectMapper.writeValueAsString(payload);
            return fetchResponse(clazz, path, params, "POST", payloadString, "application/json;charset=UTF-8");

        } catch (JsonProcessingException e) {
            return new Response<T>(Error.serlizerError(e));
        }
    }

    public <T> Response<T> put(Class<T> clazz, String path, Map<String, String> params) {
        return fetchResponse(clazz, path, params, "PUT");
    }

    public <T> Response<T> delete(Class<T> clazz, String path, Map<String, String> params) {
        return fetchResponse(clazz, path, params, "DELETE");
    }

    public <T> Response<T> fetchResponse(Class<T> clazz, String path, Map<String, String> params, String httpMethod) {
        return fetchResponse(clazz, path, params, httpMethod, null);
    }

    public <T> Response<T> fetchResponse(Class<T> clazz, String path, Map<String, String> params, String httpMethod, String payload) {
        return fetchResponse(clazz, path, params, httpMethod, payload, "application/x-www-form-urlencoded;charset=UTF-8");
    }

    public <T> Response<T> fetchResponse(Class<T> clazz, String path, Map<String, String> params, String httpMethod, String payload, String payloadContentType) {
        try {
            String fullUrl = urlWithParams(path != null ? url + path : url, params);
            L.d("[" + httpMethod + "] " + fullUrl);
            final URLConnection connection = new URL(fullUrl).openConnection();
            if ( connection instanceof HttpURLConnection) {
                final HttpURLConnection httpConnection = (HttpURLConnection)connection;
                httpConnection.setDoInput(true);
                if ( httpMethod != null ) {
                    httpConnection.setRequestMethod(httpMethod);
                    if(httpMethod.equals("POST")) {
                        connection.setDoOutput(true); // Triggers POST.
                        connection.setRequestProperty("Accept-Charset", "UTF-8");
                        connection.setRequestProperty("Content-Type", payloadContentType);
                    }
                } else {
                    httpConnection.setRequestMethod(params != null ? "POST" : "GET");
                }
                httpConnection.addRequestProperty("Accept", "application/json");
                httpConnection.connect();
                if(payload != null) {
                    OutputStream outputStream = httpConnection.getOutputStream();
                    try {
                        if(LOG_RESPONSE) {
                            L.d("[payload] " + payload);
                        }
                        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                        writer.write(payload);
                        writer.close();
                    } finally {
                        outputStream.close();
                    }
                }
                InputStream input = null;
                try {
                    input = connection.getInputStream();
                } catch ( IOException e ) {
                    // workaround for Android HttpURLConnection ( IOException is thrown for 40x error codes ).
                    final int statusCode = httpConnection.getResponseCode();
                    if ( statusCode == -1 ) throw e;
                    return new Response<T>(Error.httpError(httpConnection.getResponseCode()) );
                }
                final int statusCode = httpConnection.getResponseCode();
                L.d("statusCode " + statusCode);
                if ( statusCode == HttpURLConnection.HTTP_OK ||
                        statusCode == HttpURLConnection.HTTP_CREATED ) {
                    try {
                        T value = null;
                        if ( clazz != Void.class ) {
                            if(LOG_RESPONSE || clazz == String.class) {
                                StringBuilder sb=new StringBuilder();
                                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                                String read = br.readLine();
                                while(read != null) {
                                    sb.append(read);
                                    read = br.readLine();
                                }
                                String response = sb.toString();
                                if( LOG_RESPONSE ) {
                                    L.d("response " + response);
                                }
                                if( clazz == String.class ) {
                                    value = (T) response;
                                } else {
                                    value = (T) objectMapper.readValue(response, clazz);
                                }
                            }
                            else {
                                value = (T) objectMapper.readValue(input, clazz);
                            }
                        }
                        return new Response<T>(value);
                    } catch (JsonMappingException e ) {
                        return new Response<T>(Error.serlizerError(e));
                    } catch (JsonParseException e) {
                        return new Response<T>(Error.serlizerError(e));
                    }
                } else if(statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    try {
                        T def = clazz.newInstance();
                        if(LOG_RESPONSE) {
                            L.d("statusCode  == HttpURLConnection.HTTP_NO_CONTENT");
                        }
                        return new Response<T>(def);
                    } catch (InstantiationException e) {
                        return new Response<T>(Error.ioError(e));
                    } catch (IllegalAccessException e) {
                        return new Response<T>(Error.ioError(e));
                    }
                }  else {
                    if(LOG_RESPONSE) {
                        L.d("error, statusCode " + statusCode);
                    }
                    return new Response<T>(Error.httpError(statusCode));
                }
            }
            return new Response<T>(Error.ioError(new Exception("Url is not a http link")));
        } catch ( IOException e ) {
            if(LOG_RESPONSE) {
                L.d("error, ioError " + e);
            }
            return new Response<T>(Error.ioError(e));
        }
    }
}
