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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;

/**
 * This represents a destination in a PDF document.
 *
 * @author Ben Litchfield
 */
public abstract class PDDestination implements PDDestinationOrAction
{

    /**
     * This will create a new destination depending on the type of COSBase
     * that is passed in.
     *
     * @param base The base level object.
     *
     * @return A new destination.
     *
     * @throws IOException If the base cannot be converted to a Destination.
     */
    public static PDDestination create( COSBase base ) throws IOException
    {
        PDDestination retval = null;
        if( base == null )
        {
            //this is ok, just return null.
        }
        else if (base instanceof COSArray 
                && ((COSArray) base).size() > 1 
                && ((COSArray) base).getObject(1) instanceof COSName)
        {
            COSArray array = (COSArray) base;
            COSName type = (COSName) array.getObject(1);
            String typeString = type.getName();
            switch (typeString)
            {
                case PDPageFitDestination.TYPE:
                case PDPageFitDestination.TYPE_BOUNDED:
                    retval = new PDPageFitDestination(array);
                    break;
                case PDPageFitHeightDestination.TYPE:
                case PDPageFitHeightDestination.TYPE_BOUNDED:
                    retval = new PDPageFitHeightDestination(array);
                    break;
                case PDPageFitRectangleDestination.TYPE:
                    retval = new PDPageFitRectangleDestination(array);
                    break;
                case PDPageFitWidthDestination.TYPE:
                case PDPageFitWidthDestination.TYPE_BOUNDED:
                    retval = new PDPageFitWidthDestination(array);
                    break;
                case PDPageXYZDestination.TYPE:
                    retval = new PDPageXYZDestination(array);
                    break;
                default:
                    throw new IOException("Unknown destination type: " + type.getName());
            }
        }
        else if( base instanceof COSString )
        {
            retval = new PDNamedDestination( (COSString)base );
        }
        else if( base instanceof COSName )
        {
            retval = new PDNamedDestination( (COSName)base );
        }
        else
        {
            throw new IOException( "Error: can't convert to Destination " + base );
        }
        return retval;
    }

}
