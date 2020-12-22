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

package org.apache.pdfbox.preflight.content;

import org.apache.pdfbox.preflight.exception.ValidationException;

public class ContentStreamException extends ValidationException
{
    private String errorCode = "";

    public ContentStreamException(final String arg0, final Throwable arg1)
    {
        super(arg0);
    }

    public ContentStreamException(final String arg0)
    {
        super(arg0);
    }

    public ContentStreamException(final Throwable arg0)
    {
        super(arg0.getMessage());
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(final String errorCode)
    {
        this.errorCode = errorCode;
    }

}
