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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDAppearanceDictionary.class);

    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDAppearanceDictionary()
    {
        dictionary = new COSDictionary();
        //the N entry is required.
        dictionary.setItem( COSName.N, new COSDictionary() );
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
    public Map<String,PDAppearanceStream> getNormalAppearance()
    {
        COSBase ap = dictionary.getDictionaryObject( COSName.N );
        if ( ap == null )
        { 
            return null; 
        }
        else if( ap instanceof COSStream )
        {
            COSStream aux = (COSStream) ap;
            ap = new COSDictionary();
            ((COSDictionary)ap).setItem(COSName.DEFAULT, aux );
        }
        COSDictionary map = (COSDictionary)ap;
        Map<String, PDAppearanceStream> actuals = new HashMap<String, PDAppearanceStream>();
        Map<String, PDAppearanceStream> retval = new COSDictionaryMap<String, PDAppearanceStream>( actuals, map );
        for( COSName asName : map.keySet() )
        {
            COSBase stream = map.getDictionaryObject( asName );
            // PDFBOX-1599: this is just a workaround. The given PDF provides "null" as stream 
            // which leads to a COSName("null") value and finally to a ClassCastExcpetion
            if (stream instanceof COSStream)
            {
                COSStream as = (COSStream)stream;
                actuals.put( asName.getName(), new PDAppearanceStream( as ) );
            }
            else
            {
                LOG.debug("non-conformance workaround: ignore null value for appearance stream.");
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
    public void setNormalAppearance( Map<String,PDAppearanceStream> appearanceMap )
    {
        dictionary.setItem( COSName.N, COSDictionaryMap.convert( appearanceMap ) );
    }

    /**
     * This will set the normal appearance when there is only one appearance
     * to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setNormalAppearance( PDAppearanceStream ap )
    {
        dictionary.setItem( COSName.N, ap.getStream() );
    }

    /**
     * This will return a list of appearances.  In the case where there is
     * only one appearance the map will contain one entry whose key is the string
     * "default".  If there is no rollover appearance then the normal appearance
     * will be returned.  Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public Map<String,PDAppearanceStream> getRolloverAppearance()
    {
        Map<String,PDAppearanceStream> retval = null;
        COSBase ap = dictionary.getDictionaryObject( COSName.R );
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
                ((COSDictionary)ap).setItem(COSName.DEFAULT, aux );
            }
            COSDictionary map = (COSDictionary)ap;
            Map<String, PDAppearanceStream> actuals = new HashMap<String, PDAppearanceStream>();
            retval = new COSDictionaryMap<String, PDAppearanceStream>( actuals, map );
            for( COSName asName : map.keySet() )
            {
                COSBase stream = map.getDictionaryObject( asName );
                // PDFBOX-1599: this is just a workaround. The given PDF provides "null" as stream 
                // which leads to a COSName("null") value and finally to a ClassCastExcpetion
                if (stream instanceof COSStream)
                {
                    COSStream as = (COSStream)stream;
                    actuals.put( asName.getName(), new PDAppearanceStream( as ) );
                }
                else
                {
                    LOG.debug("non-conformance workaround: ignore null value for appearance stream.");
                }
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
    public void setRolloverAppearance( Map<String,PDAppearanceStream> appearanceMap )
    {
        dictionary.setItem( COSName.R, COSDictionaryMap.convert( appearanceMap ) );
    }

    /**
     * This will set the rollover appearance when there is rollover appearance
     * to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setRolloverAppearance( PDAppearanceStream ap )
    {
        dictionary.setItem( COSName.R, ap.getStream() );
    }

    /**
     * This will return a list of appearances.  In the case where there is
     * only one appearance the map will contain one entry whose key is the string
     * "default".  If there is no rollover appearance then the normal appearance
     * will be returned.  Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public Map<String,PDAppearanceStream> getDownAppearance()
    {
        Map<String,PDAppearanceStream> retval = null;
        COSBase ap = dictionary.getDictionaryObject( COSName.D );
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
                ((COSDictionary)ap).setItem(COSName.DEFAULT, aux );
            }
            COSDictionary map = (COSDictionary)ap;
            Map<String, PDAppearanceStream> actuals =
                new HashMap<String, PDAppearanceStream>();
            retval = new COSDictionaryMap<String, PDAppearanceStream>( actuals, map );
            for( COSName asName : map.keySet() )
            {
                COSBase stream = map.getDictionaryObject( asName );
                // PDFBOX-1599: this is just a workaround. The given PDF provides "null" as stream 
                // which leads to a COSName("null") value and finally to a ClassCastExcpetion
                if (stream instanceof COSStream)
                {
                    COSStream as = (COSStream)stream;
                    actuals.put( asName.getName(), new PDAppearanceStream( as ) );
                }
                else
                {
                    LOG.debug("non-conformance workaround: ignore null value for appearance stream.");
                }
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
    public void setDownAppearance( Map<String,PDAppearanceStream> appearanceMap )
    {
        dictionary.setItem( COSName.D, COSDictionaryMap.convert( appearanceMap ) );
    }
    
    /**
     * This will set the down appearance when there is down appearance
     * to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setDownAppearance( PDAppearanceStream ap )
    {
        dictionary.setItem( COSName.D, ap.getStream() );
    }

}
