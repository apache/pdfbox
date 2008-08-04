/**
 * Copyright (c) 2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.common;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;

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