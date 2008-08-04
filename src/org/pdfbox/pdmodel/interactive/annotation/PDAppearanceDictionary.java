/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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
package org.pdfbox.pdmodel.interactive.annotation;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.COSDictionaryMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a PDF /AP entry the appearance dictionary.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDAppearanceDictionary implements COSObjectable
{
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDAppearanceDictionary()
    {
        dictionary = new COSDictionary();
        //the N entry is required.
        dictionary.setItem( COSName.getPDFName( "N" ), new COSDictionary() );
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAppearanceDictionary( COSDictionary dict )
    {
        dictionary = dict;
    }

    /**
     * returns the dictionary.
     * @return the dictionary
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * returns the dictionary.
     * @return the dictionary
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will return a list of appearances.  In the case where there is
     * only one appearance the map will contain one entry whose key is the string
     * "default".
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public Map getNormalAppearance()
    {
        COSBase ap = dictionary.getDictionaryObject( COSName.getPDFName( "N" ) );
        if( ap instanceof COSStream )
        {
            COSStream aux = (COSStream) ap;
            ap = new COSDictionary();
            ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), aux );
        }
        COSDictionary map = (COSDictionary)ap;
        Map actuals = new HashMap();
        Map retval = new COSDictionaryMap( actuals, map );
        Iterator asNames = map.keyList().iterator();
        while( asNames.hasNext() )
        {
            COSName asName = (COSName)asNames.next();
            COSStream as = (COSStream)map.getDictionaryObject( asName );
            actuals.put( asName.getName(), new PDAppearanceStream( as ) );
        }

        return retval;
    }

    /**
     * This will set a list of appearances.  If you would like to set the single
     * appearance then you should use the key "default", and when the PDF is written
     * back to the filesystem then there will only be one stream.
     *
     * @param appearanceMap The updated map with the appearance.
     */
    public void setNormalAppearance( Map appearanceMap )
    {
        dictionary.setItem( COSName.getPDFName( "N" ), COSDictionaryMap.convert( appearanceMap ) );
    }

    /**
     * This will set the normal appearance when there is only one appearance
     * to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setNormalAppearance( PDAppearanceStream ap )
    {
        dictionary.setItem( COSName.getPDFName( "N" ), ap.getStream() );
    }

    /**
     * This will return a list of appearances.  In the case where there is
     * only one appearance the map will contain one entry whose key is the string
     * "default".  If there is no rollover appearance then the normal appearance
     * will be returned.  Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public Map getRolloverAppearance()
    {
        Map retval = null;
        COSBase ap = dictionary.getDictionaryObject( COSName.getPDFName( "R" ) );
        if( ap == null )
        {
            retval = getNormalAppearance();
        }
        else
        {
            if( ap instanceof COSStream )
            {
                ap = new COSDictionary();
                ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), ap );
            }
            COSDictionary map = (COSDictionary)ap;
            Map actuals = new HashMap();
            retval = new COSDictionaryMap( actuals, map );
            Iterator asNames = map.keyList().iterator();
            while( asNames.hasNext() )
            {
                COSName asName = (COSName)asNames.next();
                COSStream as = (COSStream)map.getDictionaryObject( asName );
                actuals.put( asName.getName(), new PDAppearanceStream( as ) );
            }
        }

        return retval;
    }

    /**
     * This will set a list of appearances.  If you would like to set the single
     * appearance then you should use the key "default", and when the PDF is written
     * back to the filesystem then there will only be one stream.
     *
     * @param appearanceMap The updated map with the appearance.
     */
    public void setRolloverAppearance( Map appearanceMap )
    {
        dictionary.setItem( COSName.getPDFName( "R" ), COSDictionaryMap.convert( appearanceMap ) );
    }

    /**
     * This will return a list of appearances.  In the case where there is
     * only one appearance the map will contain one entry whose key is the string
     * "default".  If there is no rollover appearance then the normal appearance
     * will be returned.  Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public Map getDownAppearance()
    {
        Map retval = null;
        COSBase ap = dictionary.getDictionaryObject( COSName.getPDFName( "D" ) );
        if( ap == null )
        {
            retval = getNormalAppearance();
        }
        else
        {
            if( ap instanceof COSStream )
            {
                ap = new COSDictionary();
                ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), ap );
            }
            COSDictionary map = (COSDictionary)ap;
            Map actuals = new HashMap();
            retval = new COSDictionaryMap( actuals, map );
            Iterator asNames = map.keyList().iterator();
            while( asNames.hasNext() )
            {
                COSName asName = (COSName)asNames.next();
                COSStream as = (COSStream)map.getDictionaryObject( asName );
                actuals.put( asName.getName(), new PDAppearanceStream( as ) );
            }
        }

        return retval;
    }

    /**
     * This will set a list of appearances.  If you would like to set the single
     * appearance then you should use the key "default", and when the PDF is written
     * back to the filesystem then there will only be one stream.
     *
     * @param appearanceMap The updated map with the appearance.
     */
    public void setDownAppearance( Map appearanceMap )
    {
        dictionary.setItem( COSName.getPDFName( "D" ), COSDictionaryMap.convert( appearanceMap ) );
    }
}