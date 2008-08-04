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
package org.pdfbox.pdmodel.graphics.color;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

import org.pdfbox.pdmodel.common.COSDictionaryMap;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents attributes for a DeviceN color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDDeviceNAttributes
{
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDDeviceNAttributes()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param attributes A dictionary that has all of the attributes.
     */
    public PDDeviceNAttributes( COSDictionary attributes )
    {
        dictionary = attributes;
    }

    /**
     * This will get the underlying cos dictionary.
     *
     * @return The dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * This will get a map of colorants.  See the PDF Reference for more details about
     * this attribute.  The map will contain a java.lang.String as the key, a colorant name,
     * and a PDColorSpace as the value.
     *
     * @return The colorant map.
     *
     * @throws IOException If there is an error getting the colorspaces.
     */
    public Map getColorants() throws IOException
    {
        Map actuals = new HashMap();
        COSDictionary colorants = (COSDictionary)dictionary.getDictionaryObject( COSName.getPDFName( "Colorants" ) );
        if( colorants == null )
        {
            colorants = new COSDictionary();
            dictionary.setItem( COSName.getPDFName( "Colorants" ), colorants );
        }
        Iterator iter = colorants.keyList().iterator();
        while( iter.hasNext() )
        {
            COSName name = (COSName)iter.next();
            COSBase value = colorants.getDictionaryObject( name );
            actuals.put( name.getName(), PDColorSpaceFactory.createColorSpace( value ) );
        }
        return new COSDictionaryMap( actuals, colorants );
    }

    /**
     * This will replace the existing colorant attribute.  The key should be strings
     * and the values should be PDColorSpaces.
     *
     * @param colorants The map of colorants.
     */
    public void setColorants( Map colorants )
    {
        COSDictionary colorantDict = null;
        if( colorants != null )
        {
            colorantDict = COSDictionaryMap.convert( colorants );
        }
        dictionary.setItem( COSName.getPDFName( "Colorants" ), colorantDict );
    }
}