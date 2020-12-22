/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.font.util;

public class GlyphException extends Exception
{
    private final String errorCode;
    private final int invalidCid;

    public GlyphException(final String errorCode, final int cid)
    {
        super();
        this.errorCode = errorCode;
        this.invalidCid = cid;
    }

    public GlyphException(final String errorCode, final int cid, final String message)
    {
        super(message);
        this.errorCode = errorCode;
        this.invalidCid = cid;
    }

    public GlyphException(final String errorCode, final int cid, final String message, final Throwable cause)
    {
        super(message, cause);
        this.errorCode = errorCode;
        this.invalidCid = cid;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public int getInvalidCid()
    {
        return invalidCid;
    }

}
