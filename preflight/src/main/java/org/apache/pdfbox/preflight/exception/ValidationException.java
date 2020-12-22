/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.exception;

import java.io.IOException;

public class ValidationException extends IOException
{

    /**
     * serial version number
     */
    private static final long serialVersionUID = -1616141241190424669L;
    
    protected Integer pageNumber = null;

    public ValidationException(final String message, final Throwable cause, final Integer pageNumber)
    {
        super(message);
        initCause(cause);
        this.pageNumber = pageNumber;
    }

    public ValidationException(final String message, final Throwable cause)
    {
        super(message);
        initCause(cause);
    }

    public ValidationException(final String message)
    {
        super(message);
    }

    public ValidationException(final Throwable cause)
    {
        super();
        initCause(cause);
    }

    /**
     * Returns the page number related to the exception, or null if not known.
     */
    public Integer getPageNumber()
    {
        return pageNumber;
    }
}