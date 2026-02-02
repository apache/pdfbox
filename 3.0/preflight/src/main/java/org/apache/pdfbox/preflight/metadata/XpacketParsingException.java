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

package org.apache.pdfbox.preflight.metadata;

import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * This exception is raised when the parsing of the xpacket fails
 * 
 */
public class XpacketParsingException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected ValidationError error;

    /**
     * Constructor
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public XpacketParsingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param message
     *            the message
     */
    public XpacketParsingException(String message)
    {
        super(message);
    }

    public XpacketParsingException(String message, ValidationError error)
    {
        super(message);
        this.error = error;
    }

    public ValidationError getError()
    {
        return error;
    }

}
