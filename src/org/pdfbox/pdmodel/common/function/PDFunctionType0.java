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
package org.pdfbox.pdmodel.common.function;

import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSNull;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.PDRange;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a type 0 function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType0 extends PDStreamFunction
{

    /**
     * Constructor to create a new blank type 0 function.
     * 
     * @param doc The document that the function will be part of.
     */
    protected PDFunctionType0( PDDocument doc )
    {
        super( doc, 0 );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDFunctionType0( PDStream functionDictionary )
    {
        super( functionDictionary );
    }
    
    /**
     * The "Size" entry, which is the number of samples in 
     * each input dimension of the sample table.
     * 
     * @return A List of java.lang.Integer objects.
     */
    public List getNumberOfSamples()
    {
        List retval = null;
        COSArray size = (COSArray)getCOSStream().getDictionaryObject( "Size" );
        if( size != null )
        {
            retval = COSArrayList.convertIntegerCOSArrayToList( size );
        }
        return retval;
    }
    
    /**
     * Set the samples data, the "Size" entry in the type 0 function.
     * 
     * @param samples The samples data.
     */
    public void setNumberOfSamples( List samples )
    {
        getCOSStream().setItem( "Size", COSArrayList.converterToCOSArray( samples ));
    }
    
    /**
     * Get the number of bits that the output value will take up.  Valid values
     * are 1,2,4,8,12,16,24,32.
     * 
     * @return Number of bits for each output value.
     */
    public int getBitsPerSample()
    {
        return getCOSStream().getInt( "BitsPerSample" );
    }
    
    /**
     * Set the number of bits that the output value will take up.  Valid values
     * are 1,2,4,8,12,16,24,32.
     * 
     * @param bps The number of bits for each output value.
     */
    public void setBitsPerSample( int bps )
    {
        getCOSStream().setInt( "BitsPerSample", bps );
    }
    
    /**
     * Get the encode for the input parameter.
     * 
     * @param paramNum The function parameter number.
     * 
     * @return The encode parameter range or null if none is set.
     */
    public PDRange getEncodeForParameter( int paramNum )
    {
        PDRange retval = null;
        COSArray encode = (COSArray)getCOSStream().getDictionaryObject( "Encode" );
        if( encode != null && encode.size() >= paramNum*2+1 )
        {
            retval = new PDRange(encode, paramNum );
        }
        return retval;
    }
    
    /**
     * Set the encode range for the param number.
     * 
     * @param paramNum The parameter number to set then encode values.
     * 
     * @param range The range value to set.
     */
    public void setEncodeForParameter( int paramNum, PDRange range )
    {
        COSArray encode = (COSArray)getCOSStream().getDictionaryObject( "Encode" );
        if( encode == null )
        {
            encode = new COSArray();
        }
        for( int i=encode.size(); i<paramNum*2+1; i++)
        {
            encode.add( COSNull.NULL );
        }
        encode.set( paramNum*2, new COSFloat( range.getMin() ) );
        encode.set( paramNum*2+1, new COSFloat( range.getMax() ) );
    }
    
    /**
     * Get the decode for the input parameter.
     * 
     * @param paramNum The function parameter number.
     * 
     * @return The decode parameter range or null if none is set.
     */
    public PDRange getDecodeForParameter( int paramNum )
    {
        PDRange retval = null;
        COSArray encode = (COSArray)getCOSStream().getDictionaryObject( "Decode" );
        if( encode != null && encode.size() >= paramNum*2+1 )
        {
            retval = new PDRange(encode, paramNum );
        }
        return retval;
    }
    
    /**
     * Set the decode range for the param number.
     * 
     * @param paramNum The parameter number to set then decode values.
     * 
     * @param range The range value to set.
     */
    public void setDecodeForParameter( int paramNum, PDRange range )
    {
        COSArray encode = (COSArray)getCOSStream().getDictionaryObject( "Decode" );
        if( encode == null )
        {
            encode = new COSArray();
        }
        for( int i=encode.size(); i<paramNum*2+1; i++)
        {
            encode.add( COSNull.NULL );
        }
        encode.set( paramNum*2, new COSFloat( range.getMin() ) );
        encode.set( paramNum*2+1, new COSFloat( range.getMax() ) );
    }
}