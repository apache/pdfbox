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

import java.io.IOException;
import java.util.logging.*;

/**
 * Implementation of base object to help with error-handling.
 *
 * @author <a href="mailto:DanielWilson@users.sourceforge.net">Daniel Wilson</a>
 * @version $Revision: 1.1 $
 */
public abstract class LoggingObject
{
    private static Logger logger_;//dwilson 3/15/07
    
    static 
    {
        try 
        {
            FileHandler fh = new FileHandler("PDFBox.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger_ = Logger.getLogger("TestLog");
            logger_.addHandler(fh);

            /*Set the log level here.
            The lower your logging level, the more stuff will be logged.
            Options are:
                * OFF -- log nothing
                * SEVERE (highest value)
                * WARNING
                * INFO
                * CONFIG
                * FINE
                * FINER
                * FINEST (lowest value)
            http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/Level.html

            I recommend INFO for debug builds and either SEVERE or OFF for production builds.
            */
            logger_.setLevel(Level.WARNING);
//            logger_.setLevel(Level.INFO);
        }
        catch (IOException exception)
        {
            System.err.println("Error while opening the logfile:");
            exception.printStackTrace();
        }
    }

    protected Logger logger() throws IOException //dwilson 3/15/07
    {
        return logger_;
    }

    protected static String FullStackTrace(Throwable e){
		int i;
		StackTraceElement [] L;

		StringBuffer sRet = new StringBuffer();
		L = e.getStackTrace();
		for (i=0; i<L.length; i++)
		{
			sRet.append((L[i].toString())).append("\n");
		}
		if (e.getCause() != null)
		{
			sRet.append("Caused By \n\t").append(e.getCause().getMessage());
			sRet.append(FullStackTrace(e.getCause()));
		}

		return sRet.toString();
    }
}
