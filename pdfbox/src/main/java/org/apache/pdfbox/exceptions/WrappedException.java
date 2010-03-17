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

import java.io.PrintStream;

/**
 * An exception that that holds a sub exception.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class WrappedException extends Exception
{
    private Exception wrapped = null;

    /**
     * constructor comment.
     *
     * @param e The root exception that caused this exception.
     */
    public WrappedException( Exception e )
    {
        wrapped = e;
    }

    /**
     * Gets the wrapped exception message.
     *
     * @return A message indicating the exception.
     */
    public String getMessage()
    {
        return wrapped.getMessage();
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param s <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream s)
    {
        super.printStackTrace( s );
        wrapped.printStackTrace( s );
    }
}
