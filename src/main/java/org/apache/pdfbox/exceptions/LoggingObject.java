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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of base object to help with error-handling.
 *
 * @author <a href="mailto:DanielWilson@users.sourceforge.net">Daniel Wilson</a>
 * @version $Revision: 1.1 $
 */
public abstract class LoggingObject
{
    private static Log logger; //dwilson 3/15/07

    /**
     * Returns the main logger instance.
     * @return the logger instance
     */
    protected Log logger() //dwilson 3/15/07
    {
        if (logger == null)
        {
            logger = LogFactory.getLog(getClass());
        }
        return logger;
    }

}
