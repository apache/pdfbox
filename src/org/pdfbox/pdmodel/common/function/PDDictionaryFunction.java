/**
 * Copyright (c) 2006, www.pdfbox.org
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
package org.pdfbox.pdmodel.common.function;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.common.PDRange;

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