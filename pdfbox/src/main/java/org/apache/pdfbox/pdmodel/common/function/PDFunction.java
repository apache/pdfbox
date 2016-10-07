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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a function in a PDF document.
 *
 * @author Ben Litchfield
 * 
 */
public abstract class PDFunction implements COSObjectable
{

    private PDStream functionStream = null;
    private COSDictionary functionDictionary = null;
    private COSArray domain = null;
    private COSArray range = null;
    private int numberOfInputValues = -1;
    private int numberOfOutputValues = -1;

    /**
     * Constructor.
     *
     * @param function The function stream.
     * 
     */
    public PDFunction( COSBase function )
    {
        if (function instanceof COSStream)
        {
            functionStream = new PDStream( (COSStream)function );
            functionStream.getCOSObject().setItem( COSName.TYPE, COSName.FUNCTION );
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
     * Returns the stream.
     * @return The stream for this object.
     */
    @Override
    public COSDictionary getCOSObject()
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
        if (function == COSName.IDENTITY)
        {
            return new PDFunctionTypeIdentity(null);
        }

        COSDictionary functionDictionary;
        if (function instanceof COSObject)
        {
            functionDictionary = (COSDictionary) ((COSObject) function).getObject();
        }
        else
        {
            functionDictionary = (COSDictionary) function;
        }
        int functionType = functionDictionary.getInt(COSName.FUNCTION_TYPE);
        switch (functionType)
        {
            case 0:
                return new PDFunctionType0(functionDictionary);
            case 2:
                return new PDFunctionType2(functionDictionary);
            case 3:
                return new PDFunctionType3(functionDictionary);
            case 4:
                return new PDFunctionType4(functionDictionary);
            default:
                throw new IOException("Error: Unknown function type " + functionType);
        }
    }

    /**
     * This will get the number of output parameters that
     * have a range specified.  A range for output parameters
     * is optional so this may return zero for a function
     * that does have output parameters, this will simply return the
     * number that have the range specified.
     *
     * @return The number of output parameters that have a range
     * specified.
     */
    public int getNumberOfOutputParameters()
    {
        if (numberOfOutputValues == -1)
        {
            COSArray rangeValues = getRangeValues();
            numberOfOutputValues = rangeValues.size() / 2;
        }
        return numberOfOutputValues;
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
     * @param rangeValues The new range values.
     */
    public void setRangeValues(COSArray rangeValues)
    {
        range = rangeValues;
        getCOSObject().setItem(COSName.RANGE, rangeValues);
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
        if (numberOfInputValues == -1)
        {
            COSArray array = getDomainValues();
            numberOfInputValues = array.size() / 2;
        }
        return numberOfInputValues;
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
     * @param domainValues The new domain values.
     */
    public void setDomainValues(COSArray domainValues)
    {
        domain = domainValues;
        getCOSObject().setItem(COSName.DOMAIN, domainValues);
    }

    /**
     * @deprecated Replaced by {@link #eval(float[] input)}
     */
    @Deprecated
    public COSArray eval(COSArray input) throws IOException
    {
        float[] outputValues = eval(input.toFloatArray());
        COSArray array = new COSArray();
        array.setFloatArray(outputValues);
        return array;
    }

    /**
     * Evaluates the function at the given input.
     * ReturnValue = f(input)
     *
     * @param input The array of input values for the function. 
     * In many cases will be an array of a single value, but not always.
     * 
     * @return The of outputs the function returns based on those inputs. 
     * In many cases will be an array of a single value, but not always.
     * 
     * @throws IOException an IOExcpetion is thrown if something went wrong processing the function.  
     */
    public abstract float[] eval(float[] input) throws IOException;
    
    /**
     * Returns all ranges for the output values as COSArray .
     * Required for type 0 and type 4 functions
     * @return the ranges array. 
     */
    protected COSArray getRangeValues() 
    {
        if (range == null) 
        {
            range = (COSArray) getCOSObject().getDictionaryObject(COSName.RANGE);
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
            domain = (COSArray) getCOSObject().getDictionaryObject(COSName.DOMAIN);
        }
        return domain;
    }

    /**
     * Clip the given input values to the ranges.
     * 
     * @param inputValues the input values
     * @return the clipped values
     */
    protected float[] clipToRange(float[] inputValues) 
    {
        COSArray rangesArray = getRangeValues();
        float[] result;
        if (rangesArray != null) 
        {
            float[] rangeValues = rangesArray.toFloatArray();
            int numberOfRanges = rangeValues.length/2;
            result = new float[numberOfRanges];
            for (int i=0; i<numberOfRanges; i++)
            {
                int index = i << 1;
                result[i] = clipToRange(inputValues[i], rangeValues[index], rangeValues[index + 1]);
            }
        }
        else
        {
            result = inputValues;
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
        if (x < rangeMin)
        {
            return rangeMin;
        }
        else if (x > rangeMax)
        {
            return rangeMax;
        }
        return x;
    }

    /**
     * For a given value of x, interpolate calculates the y value 
     * on the line defined by the two points (xRangeMin , xRangeMax ) 
     * and (yRangeMin , yRangeMax ).
     * 
     * @param x the to be interpolated value.
     * @param xRangeMin the min value of the x range
     * @param xRangeMax the max value of the x range
     * @param yRangeMin the min value of the y range
     * @param yRangeMax the max value of the y range
     * @return the interpolated y value
     */
    protected float interpolate(float x, float xRangeMin, float xRangeMax, float yRangeMin, float yRangeMax) 
    {
        return yRangeMin + ((x - xRangeMin) * (yRangeMax - yRangeMin)/(xRangeMax - xRangeMin));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "FunctionType" + getFunctionType();
    }
}
