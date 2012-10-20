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
package org.apache.pdfbox.pdmodel.fdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.common.PDNamedTextStream;

/**
 * This represents an FDF JavaScript dictionary that is part of the FDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class FDFJavaScript implements COSObjectable
{
    private COSDictionary js;

    /**
     * Default constructor.
     */
    public FDFJavaScript()
    {
        js = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param javaScript The FDF java script.
     */
    public FDFJavaScript( COSDictionary javaScript )
    {
        js = javaScript;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return js;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return js;
    }

    /**
     * This will get the javascript that is executed before the import.
     *
     * @return Some javascript code.
     */
    public PDTextStream getBefore()
    {
        return PDTextStream.createTextStream( js.getDictionaryObject( "Before" ) );
    }

    /**
     * This will set the javascript code the will get execute before the import.
     *
     * @param before A reference to some javascript code.
     */
    public void setBefore( PDTextStream before )
    {
        js.setItem( "Before", before );
    }

    /**
     * This will get the javascript that is executed after the import.
     *
     * @return Some javascript code.
     */
    public PDTextStream getAfter()
    {
        return PDTextStream.createTextStream( js.getDictionaryObject( "After" ) );
    }

    /**
     * This will set the javascript code the will get execute after the import.
     *
     * @param after A reference to some javascript code.
     */
    public void setAfter( PDTextStream after )
    {
        js.setItem( "After", after );
    }

    /**
     * This will return a list of PDNamedTextStream objects.  This is the "Doc"
     * entry of the pdf document.  These will be added to the PDF documents
     * javascript name tree.  This will not return null.
     *
     * @return A list of all named javascript entries.
     */
    public List getNamedJavaScripts()
    {
        COSArray array = (COSArray)js.getDictionaryObject( "Doc" );
        List namedStreams = new ArrayList();
        if( array == null )
        {
            array = new COSArray();
            js.setItem( "Doc", array );
        }
        for( int i=0; i<array.size(); i++ )
        {
            COSName name = (COSName)array.get( i );
            i++;
            COSBase stream = array.get( i );
            PDNamedTextStream namedStream = new PDNamedTextStream( name, stream );
            namedStreams.add( namedStream );
        }
        return new COSArrayList( namedStreams, array );
    }

    /**
     * This should be a list of PDNamedTextStream objects.
     *
     * @param namedStreams The named streams.
     */
    public void setNamedJavaScripts( List namedStreams )
    {
        COSArray array = COSArrayList.converterToCOSArray( namedStreams );
        js.setItem( "Doc", array );
    }
}
