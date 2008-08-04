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

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDRange;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public abstract class PDStreamFunction extends PDFunction
{
    private PDStream function = null;

    /**
     * Constructor to create a new blank function, should only be called by 
     * subclasses.
     * 
     *  @param doc The document that this function is part of.
     *  @param functionType An integer describing the function type, only 0,2,3,4
     *  are defined by the PDF sepc.
     */
    protected PDStreamFunction( PDDocument doc, int functionType )
    {
        function = new PDStream( doc );
        function.getStream().setInt( "FunctionType", functionType );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDStreamFunction( PDStream functionDictionary )
    {
        function = functionDictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return function.getCOSObject();
    }

    /**
     * This will get the underlying array value.
     *
     * @return The cos object that this object wraps.
     */
    public COSStream getCOSStream()
    {
        return function.getStream();
    }
    
    private COSArray getRangeArray( String fieldName, int n )
    {
        COSArray rangeArray = (COSArray)function.getStream().getDictionaryObject( COSName.getPDFName( "Range" ) );
        if( rangeArray == null )
        {
            rangeArray = new COSArray();
            function.getStream().setItem( fieldName, rangeArray );
            while( rangeArray.size() < n*2 )
            {
                rangeArray.add( new COSFloat( 0 ) );
                rangeArray.add( new COSFloat( 0 ) );
            }
        }
        return rangeArray;
    }
    
    /**
     * This will get the number of output parameters that
     * have a range specified.  A range for output parameters
     * is optional so this may return zero for a function
     * that does have output parameters, this will simply return the
     * number that have the rnage specified.
     * 
     * @return The number of input parameters that have a range
     * specified.
     */
    public int getNumberOfOutputParameters()
    {
        COSArray array = getRangeArray( "Range", 0 );
        return array.size() / 2;
    }

    /**
     * This will get the range for a certain output parameters.  This is will never
     * return null.  If it is not present then the range 0 to 0 will
     * be returned.
     *
     * @param n The output parameter number to get the range for.
     *
     * @return The range for this component.
     */
    public PDRange getRangeForOutput( int n )
    {
        COSArray rangeArray = getRangeArray( "Range", n );
        return new PDRange( rangeArray, n );
    }

    /**
     * This will set the a range for output parameter.
     *
     * @param range The new range for the output parameter.
     * @param n The ouput parameter number to set the range for.
     */
    public void setRangeForOutput( PDRange range, int n )
    {
        COSArray rangeArray = getRangeArray("Range", n );
        rangeArray.set( n*2, new COSFloat( range.getMin() ) );
        rangeArray.set( n*2+1, new COSFloat( range.getMax() ) );
    }
    
    /**
     * This will get the number of input parameters that
     * have a domain specified.
     * 
     * @return The number of input parameters that have a domain
     * specified.
     */
    public int getNumberOfInputParameters()
    {
        COSArray array = getRangeArray( "Domain", 0 );
        return array.size() / 2;
    }
    
    /**
     * This will get the range for a certain input parameter.  This is will never
     * return null.  If it is not present then the range 0 to 0 will
     * be returned.
     *
     * @param n The parameter number to get the domain for.
     *
     * @return The domain range for this component.
     */
    public PDRange getDomainForInput( int n )
    {
        COSArray rangeArray = getRangeArray( "Domain", n );
        return new PDRange( rangeArray, n );
    }

    /**
     * This will set the domain for the input values.
     *
     * @param range The new range for the input.
     * @param n The number of the input parameter to set the domain for.
     */
    public void setDomainForInput( PDRange range, int n )
    {
        COSArray rangeArray = getRangeArray("Domain", n );
        rangeArray.set( n*2, new COSFloat( range.getMin() ) );
        rangeArray.set( n*2+1, new COSFloat( range.getMax() ) );
    }
}