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

package org.apache.xmpbox.xml;

/**
 * Exception thrown when Parsing failed
 * 
 * @author a183132
 * 
 */
public class XmpParsingException extends Exception
{

    public enum ErrorType
    {
        Undefined, Configuration, XpacketBadStart, XpacketBadEnd, NoRootElement, NoSchema, // undefined
                                                                                           // schema
        InvalidPdfaSchema, NoType, // undefined type
        InvalidType, Format, // weird things in serialized document
        NoValueType, RequiredProperty, InvalidPrefix, // unexpected namespace
                                                      // prefix used
    }

    private final ErrorType errorType;

    /**
     * serial version uid
     */
    private static final long serialVersionUID = -8843096358184702908L;

    /**
     * Create an instance of XmpParsingException
     * 
     * @param message
     *            a description of the encountered problem
     * @param cause
     *            the cause of the exception
     */
    public XmpParsingException(ErrorType error, String message, Throwable cause)
    {
        super(message, cause);
        this.errorType = error;
    }

    /**
     * Create an instance of XmpParsingException
     * 
     * @param message
     *            a description of the encountered problem
     */
    public XmpParsingException(ErrorType error, String message)
    {
        super(message);
        this.errorType = error;
    }

    public ErrorType getErrorType()
    {
        return errorType;
    }

}
