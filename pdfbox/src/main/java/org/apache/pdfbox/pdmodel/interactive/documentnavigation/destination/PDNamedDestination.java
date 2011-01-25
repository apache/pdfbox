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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination;

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

/**
 * This represents a destination to a page by referencing it with a name.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDNamedDestination extends PDDestination
{
    private COSBase namedDestination;

    /**
     * Constructor.
     *
     * @param dest The named destination.
     */
    public PDNamedDestination( COSString dest )
    {
        namedDestination = dest;
    }

    /**
     * Constructor.
     *
     * @param dest The named destination.
     */
    public PDNamedDestination( COSName dest )
    {
        namedDestination = dest;
    }

    /**
     * Default constructor.
     */
    public PDNamedDestination()
    {
        //default, so do nothing
    }

    /**
     * Default constructor.
     *
     * @param dest The named destination.
     */
    public PDNamedDestination( String dest )
    {
        namedDestination = new COSString( dest );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return namedDestination;
    }

    /**
     * This will get the name of the destination.
     *
     * @return The name of the destination.
     */
    public String getNamedDestination()
    {
        String retval = null;
        if( namedDestination instanceof COSString )
        {
            retval = ((COSString)namedDestination).getString();
        }
        else if( namedDestination instanceof COSName )
        {
            retval = ((COSName)namedDestination).getName();
        }

        return retval;
    }

    /**
     * Set the named destination.
     *
     * @param dest The new named destination.
     *
     * @throws IOException If there is an error setting the named destination.
     */
    public void setNamedDestination( String dest ) throws IOException
    {
        if( namedDestination instanceof COSString )
        {
            COSString string = ((COSString)namedDestination);
            string.reset();
            string.append( dest.getBytes("ISO-8859-1") );
        }
        else if( dest == null )
        {
            namedDestination = null;
        }
        else
        {
            namedDestination = new COSString( dest );
        }
    }

}
