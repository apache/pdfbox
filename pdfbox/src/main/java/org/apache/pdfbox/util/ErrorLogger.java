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
package org.apache.pdfbox.util;

/**
 * This class deals with some logging that is not handled by the log4j replacement.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class ErrorLogger
{
    /**
     * Utility class, should not be instantiated.
     *
     */
    private ErrorLogger()
    {
    }

    /**
     * Log an error message.  This is only used for log4j replacement and
     * should never be used when writing code.
     *
     * @param errorMessage The error message.
     */
    public static void log( String errorMessage )
    {
        System.err.println( errorMessage );
    }

    /**
     * Log an error message.  This is only used for log4j replacement and
     * should never be used when writing code.
     *
     * @param errorMessage The error message.
     * @param t The exception.
     */
    public static void log( String errorMessage, Throwable t )
    {
        System.err.println( errorMessage );
        t.printStackTrace();
    }
}
