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
package org.apache.pdfbox.pdmodel.common;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;

/**
 * A named text stream is a combination of a name and a PDTextStream object.  This
 * is used in name trees.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDNamedTextStream implements DualCOSObjectable
{
    private COSName streamName;
    private PDTextStream stream;

    /**
     * Constructor.
     */
    public PDNamedTextStream()
    {
        //default constructor
    }

    /**
     * Constructor.
     *
     * @param name The name of the stream.
     * @param str The stream.
     */
    public PDNamedTextStream( COSName name, COSBase str )
    {
        streamName = name;
        stream = PDTextStream.createTextStream( str );
    }

    /**
     * The name of the named text stream.
     *
     * @return The stream name.
     */
    public String getName()
    {
        String name = null;
        if( streamName != null )
        {
            name = streamName.getName();
        }
        return name;
    }

    /**
     * This will set the name of the named text stream.
     *
     * @param name The name of the named text stream.
     */
    public void setName( String name )
    {
        streamName = COSName.getPDFName( name );
    }

    /**
     * This will get the stream.
     *
     * @return The stream associated with this name.
     */
    public PDTextStream getStream()
    {
        return stream;
    }

    /**
     * This will set the stream.
     *
     * @param str The stream associated with this name.
     */
    public void setStream( PDTextStream str )
    {
        stream = str;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getFirstCOSObject()
    {
        return streamName;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getSecondCOSObject()
    {
        COSBase retval = null;
        if( stream != null )
        {
            retval = stream.getCOSObject();
        }
        return retval;
    }
}
