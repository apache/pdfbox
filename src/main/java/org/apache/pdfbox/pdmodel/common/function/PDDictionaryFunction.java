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
package org.apache.pdfbox.pdmodel.common.function;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;

/**
 * This class represents a function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public abstract class PDDictionaryFunction extends PDFunction
{
    private COSDictionary function = null;

    /**
     * Constructor to create a new blank function, should only be called by
     * subclasses.
     *
     *  @param functionType An integer describing the function type, only 0,2,3,4
     *  are defined by the PDF sepc.
     */
    protected PDDictionaryFunction( int functionType )
    {
        function = new COSDictionary();
        function.setInt( "FunctionType", functionType );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDDictionaryFunction( COSDictionary functionDictionary )
    {
        function = functionDictionary;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return function;
    }

    /**
     * Get the underlying cos dictionary.
     *
     * @return The underlying cos dictionary.
     */
    public COSDictionary getCOSDictionary()
    {
        return function;
    }

    private COSArray getRangeArray( String fieldName, int n )
    {
        COSArray rangeArray = (COSArray)function.getDictionaryObject( COSName.getPDFName( "Range" ) );
        if( rangeArray == null )
        {
            rangeArray = new COSArray();
            function.setItem( fieldName, rangeArray );
            while( rangeArray.size() < n*2 )
            {
                rangeArray.add( new COSFloat( 0 ) );
                rangeArray.add( new COSFloat( 0 ) );
            }
        }
        return rangeArray;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfOutputParameters()
    {
        COSArray array = getRangeArray( "Range", 0 );
        return array.size() / 2;
    }

    /**
     * {@inheritDoc}
     */
    public PDRange getRangeForOutput( int n )
    {
        COSArray rangeArray = getRangeArray( "Range", n );
        return new PDRange( rangeArray, n );
    }

    /**
     * {@inheritDoc}
     */
    public void setRangeForOutput( PDRange range, int n )
    {
        COSArray rangeArray = getRangeArray("Range", n );
        rangeArray.set( n*2, new COSFloat( range.getMin() ) );
        rangeArray.set( n*2+1, new COSFloat( range.getMax() ) );
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfInputParameters()
    {
        COSArray array = getRangeArray( "Domain", 0 );
        return array.size() / 2;
    }

    /**
     * {@inheritDoc}
     */
    public PDRange getDomainForInput( int n )
    {
        COSArray rangeArray = getRangeArray( "Domain", n );
        return new PDRange( rangeArray, n );
    }

    /**
     * {@inheritDoc}
     */
    public void setDomainForInput( PDRange range, int n )
    {
        COSArray rangeArray = getRangeArray("Domain", n );
        rangeArray.set( n*2, new COSFloat( range.getMin() ) );
        rangeArray.set( n*2+1, new COSFloat( range.getMax() ) );
    }
}
