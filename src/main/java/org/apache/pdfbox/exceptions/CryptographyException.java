/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.exceptions;

/**
 * An exception that indicates that something has gone wrong during a
 * cryptography operation.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class CryptographyException extends Exception
{
    private Exception embedded;

    /**
     * Constructor.
     *
     * @param msg A msg to go with this exception.
     */
    public CryptographyException( String msg )
    {
        super( msg );
    }

    /**
     * Constructor.
     *
     * @param e The root exception that caused this exception.
     */
    public CryptographyException( Exception e )
    {
        super( e.getMessage() );
        setEmbedded( e );
    }
    /**
     * This will get the exception that caused this exception.
     *
     * @return The embedded exception if one exists.
     */
    public Exception getEmbedded()
    {
        return embedded;
    }
    /**
     * This will set the exception that caused this exception.
     *
     * @param e The sub exception.
     */
    private void setEmbedded( Exception e )
    {
        embedded = e;
    }
}
