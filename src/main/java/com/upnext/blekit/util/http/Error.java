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

/**
 * HTTP error helper class
 *
 * @author Roman Wozniak (roman@up-next.com)
 */
public class Error {

    public enum ErrorType { IO, SERIALIZER, HTTP };
    public final ErrorType type;
    public final Exception optionalException;
    public final int httpErrorCode;
    Error(ErrorType type, Exception optionalException, int httpErrorCode) {
        this.type = type;
        this.optionalException = optionalException;
        this.httpErrorCode = httpErrorCode;
    }
    public static Error ioError(Exception e) {
        return new Error(ErrorType.IO,  e, -1);
    }
    public static Error serlizerError(Exception e) {
        return new Error(ErrorType.SERIALIZER,  e, -1);
    }
    public static Error httpError(int errorCode) {
        return new Error(ErrorType.HTTP,  null, errorCode);
    }
    @Override
    public String toString() {
        return "Error [type=" + type + ", optionalErrorResponse="
                + optionalException + ", httpErrorCode=" + httpErrorCode + "]";
    }
}
