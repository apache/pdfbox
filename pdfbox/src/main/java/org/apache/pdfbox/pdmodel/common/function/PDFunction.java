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

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public abstract class PDFunction implements COSObjectable
{

    private PDStream functionStream = null;
    private COSDictionary functionDictionary = null;
    private COSArray domain = null;
    private COSArray range = null;

    /**
     * Constructor.
     *
     * @param functionStream The function stream.
     */
    public PDFunction( COSBase function )
    {
        if (function instanceof COSStream)
        {
            functionStream = new PDStream( (COSStream)function );
            functionStream.getStream().setName( COSName.TYPE, "Function" );
        }
        else if (function instanceof COSDictionary)
        {
            functionDictionary = (COSDictionary)function;
        }
    }

    /**
     * Returns the function type.
     * 
     * Possible values are:
     * 
     * 0 - Sampled function
     * 2 - Exponential interpolation function
     * 3 - Stitching function
     * 4 - PostScript calculator function
     * 
     * @return the function type.
     */
    public abstract int getFunctionType();
    
    /**
     * Returns the COSObject.
     *
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        if (functionStream != null)
        {
            return functionStream.getCOSObject();
        }
        else 
        {
            return functionDictionary;
        }
    }

    /**
     * Returns the stream.
     * @return The stream for this object.
     */
    public COSDictionary getDictionary()
    {
        if (functionStream != null)
        {
            return functionStream.getStream();
        }
        else 
        {
            return functionDictionary;
        }
    }

    /**
     * Returns the underlying PDStream.
     * @return The stream.
     */
    protected PDStream getPDStream()
    {
        return functionStream;
    }
    /**
     * Create the correct PD Model function based on the COS base function.
     *
     * @param function The COS function dictionary.
     *
     * @return The PDModel Function object.
     *
     * @throws IOException If we are unable to create the PDFunction object.
     */
    public static PDFunction create( COSBase function ) throws IOException
    {
        PDFunction retval = null;
        if( function instanceof COSObject )
        {
            function = ((COSObject)function).getCOSObject();
        }
        COSDictionary functionDictionary = (COSDictionary)function;
        int functionType =  functionDictionary.getInt( COSName.FUNCTION_TYPE );
        if( functionType == 0 )
        {
            retval = new PDFunctionType0(functionDictionary);
        }
        else if( functionType == 2 )
        {
            retval = new PDFunctionType2(functionDictionary);
        }
        else if( functionType == 3 )
        {
            retval = new PDFunctionType3(functionDictionary);
        }
        else if( functionType == 4 )
        {
            retval = new PDFunctionType4(functionDictionary);
        }
        else
        {
            throw new IOException( "Error: Unknown function type " + functionType );
        }
        return retval;
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
        COSArray rangeValues = getRangeValues();
        return rangeValues.size() / 2;
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
    public PDRange getRangeForOutput(int n)
    {
        COSArray rangeValues = getRangeValues();
        return new PDRange( rangeValues, n );
    }

    /**
     * This will set the range values.
     *
     * @param range The new range values.
     */
    public void setRangeValues(COSArray rangeValues)
    {
        range = rangeValues;
        getDictionary().setItem(COSName.RANGE, rangeValues);
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
        COSArray array = getDomainValues();
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
    public PDRange getDomainForInput(int n) 
    {
        COSArray domainValues = getDomainValues();
        return new PDRange( domainValues, n );
    }

    /**
     * This will set the domain values.
     *
     * @param range The new domain values.
     */
    public void setDomainValues(COSArray domainValues)
    {
        domain = domainValues;
        getDictionary().setItem(COSName.DOMAIN, domainValues);
    }

    /**
     * Evaluates the function at the given input.
     * ReturnValue = f(input)
     *
     * @param input The array of input values for the function. In many cases will be an array of a single value, but not always.
     * @return The of outputs the function returns based on those inputs. In many cases will be an array of a single value, but not always.
     */
    public abstract COSArray eval(COSArray input) throws IOException;
    
    /**
     * Returns all ranges for the output values as COSArray .
     * Required for type 0 and type 4 functions
     * @return the ranges array. 
     */
    protected COSArray getRangeValues() 
    {
        if (range == null) 
        {
            range = (COSArray)getDictionary().getDictionaryObject( COSName.RANGE );
        }
        return range;
    }

    /**
     * Returns all domains for the input values as COSArray.
     * Required for all function types.
     * @return the domains array. 
     */
    private COSArray getDomainValues()
    {
        if (domain == null) 
        {
            domain = (COSArray)getDictionary().getDictionaryObject( COSName.DOMAIN );
        }
        return domain;
    }

    /**
     * Clip the given input values to the ranges.
     * 
     * @param inputArray the input values
     * @return the clipped values
     */
    protected COSArray clipToRange(COSArray inputArray) 
    {
        COSArray rangesArray = getRangeValues();
        COSArray result = null;
        if (rangesArray != null) 
        {
            float[] inputValues = inputArray.toFloatArray();
            float[] rangeValues = rangesArray.toFloatArray();
            result = new COSArray();
            int numberOfRanges = rangeValues.length/2;
            for (int i=0; i<numberOfRanges; i++)
                result.add(new COSFloat( clipToRange(inputValues[i], rangeValues[2*i], rangeValues[2*i+1])));
        }
        else
        {
            result = inputArray;
        }
        return result;
    }

    /**
     * Clip the given input value to the given range.
     * 
     * @param x the input value
     * @param rangeMin the min value of the range
     * @param rangeMax the max value of the range

     * @return the clipped value
     */
    protected float clipToRange(float x, float rangeMin, float rangeMax) 
    {
        return Math.min(Math.max(x, rangeMin), rangeMax);
    }

    /**
     * For a given value of x, interpolate calculates the y value 
     * on the line defined by the two points (xRangeMin , xRangeMax ) 
     * and (yRangeMin , yRangeMax ).
     * 
     * @param x
     * @param xRangeMin
     * @param xRangeMax
     * @param yRangeMin
     * @param yRangeMax
     * @return the interpolated y value
     */
    protected float interpolate(float x, float xRangeMin, float xRangeMax, float yRangeMin, float yRangeMax) 
    {
        return yRangeMin + ((x - xRangeMin) * (yRangeMax - yRangeMin)/(xRangeMax - xRangeMin));
    }

}
