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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;

import java.util.HashMap;
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
        if ( ap == null )
        { 
            return null; 
        }
        else if( ap instanceof COSStream )
        {
            COSStream aux = (COSStream) ap;
            ap = new COSDictionary();
            ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), aux );
        }
        COSDictionary map = (COSDictionary)ap;
        Map<String, PDAppearanceStream> actuals = new HashMap<String, PDAppearanceStream>();
        Map retval = new COSDictionaryMap( actuals, map );
        for( COSName asName : map.keySet() )
        {
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
                COSStream aux = (COSStream) ap;
                ap = new COSDictionary();
                ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), aux );
            }
            COSDictionary map = (COSDictionary)ap;
            Map<String, PDAppearanceStream> actuals = new HashMap<String, PDAppearanceStream>();
            retval = new COSDictionaryMap( actuals, map );
            for( COSName asName : map.keySet() )
            {
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
                COSStream aux = (COSStream) ap;
                ap = new COSDictionary();
                ((COSDictionary)ap).setItem(COSName.getPDFName( "default" ), aux );
            }
            COSDictionary map = (COSDictionary)ap;
            Map<String, PDAppearanceStream> actuals =
                new HashMap<String, PDAppearanceStream>();
            retval = new COSDictionaryMap( actuals, map );
            for( COSName asName : map.keySet() )
            {
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
