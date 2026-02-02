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
 * Exception when Parsing cannot be made
 * 
 * @author a183132
 * 
 */
public class XmpSerializationException extends Exception
{

    private static final long serialVersionUID = -3495894314480173555L;

    /**
     * Create an instance of TransformException
     * 
     * @param message
     *            a description of the encountered problem
     */
    public XmpSerializationException(String message)
    {
        super(message);
    }

    /**
     * Create an instance of TransformException
     * 
     * @param message
     *            a description of the encountered problem
     * @param cause
     *            the cause of the exception
     */
    public XmpSerializationException(String message, Throwable cause)
    {
        super(message, cause);

    }

}
