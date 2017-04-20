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
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;

/**
 * This class represents a type 0 function in a PDF document.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 * 
 */
public class PDFunctionType0 extends PDFunction
{
 
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFunctionType0.class);

    /**
     * An array of 2 x m numbers specifying the linear mapping of input values 
     * into the domain of the function's sample table. Default value: [ 0 (Size0
     * - 1) 0 (Size1 - 1) ...].
     */
    private COSArray encode = null;
    /**
     * An array of 2 x n numbers specifying the linear mapping of sample values 
     * into the range appropriate for the function's output values. Default
     * value: same as the value of Range
     */
    private COSArray decode = null;
    /**
     * An array of m positive integers specifying the number of samples in each 
     * input dimension of the sample table.
     */
    private COSArray size = null;
    /**
     * The samples of the function.
     */
    private int[][] samples = null;
    
    /**
     * Constructor.
     *
     * @param function The function.
     */
    public PDFunctionType0(COSBase function)
    {
        super(function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFunctionType()
    {
        return 0;
    }

    /**
     * The "Size" entry, which is the number of samples in each input dimension
     * of the sample table.
     *
     * @return A List of java.lang.Integer objects.
     */
    public COSArray getSize()
    {
        if (size == null)
        {
            size = (COSArray) getCOSObject().getDictionaryObject(COSName.SIZE);
        }
        return size;
    }

    /**
     * Get all sample values of this function.
     * 
     * @return an array with all samples.
     */
    private int[][] getSamples()
    {
        if (samples == null)
        {
            int arraySize = 1;
            int numberOfInputValues = getNumberOfInputParameters();
            int numberOfOutputValues = getNumberOfOutputParameters();
            COSArray sizes = getSize();
            for (int i = 0; i < numberOfInputValues; i++)
            {
                arraySize *= sizes.getInt(i);
            }
            samples = new int[arraySize][numberOfOutputValues];
            int bitsPerSample = getBitsPerSample();
            int index = 0;
            try
            {
                // PDF spec 1.7 p.171:
                // Each sample value is represented as a sequence of BitsPerSample bits. 
                // Successive values are adjacent in the bit stream; there is no padding at byte boundaries.
                try (ImageInputStream mciis = new MemoryCacheImageInputStream(getPDStream().createInputStream()))
                {
                    for (int i = 0; i < arraySize; i++)
                    {
                        for (int k = 0; k < numberOfOutputValues; k++)
                        { 
                            // TODO will this cast work properly for 32 bitsPerSample or should we use long[]?
                            samples[index][k] = (int) mciis.readBits(bitsPerSample);
                        }
                        index++;
                    }
                }
            }
            catch (IOException exception)
            {
                LOG.error("IOException while reading the sample values of this function.", exception);
            }
        }
        return samples;
    }

    /**
     * Get the number of bits that the output value will take up.  
     * 
     * Valid values are 1,2,4,8,12,16,24,32.
     *
     * @return Number of bits for each output value.
     */
    public int getBitsPerSample()
    {
        return getCOSObject().getInt(COSName.BITS_PER_SAMPLE);
    }

    /**
     * Get the order of interpolation between samples. Valid values are 1 and 3,
     * specifying linear and cubic spline interpolation, respectively. Default
     * is 1. See p.170 in PDF spec 1.7.
     *
     * @return order of interpolation.
     */
    public int getOrder()
    {
        return getCOSObject().getInt(COSName.ORDER, 1);
    }

    /**
     * Set the number of bits that the output value will take up. Valid values
     * are 1,2,4,8,12,16,24,32.
     *
     * @param bps The number of bits for each output value.
     */
    public void setBitsPerSample(int bps)
    {
        getCOSObject().setInt(COSName.BITS_PER_SAMPLE, bps);
    }
    
    /**
     * Returns all encode values as COSArray.
     * 
     * @return the encode array. 
     */
    private COSArray getEncodeValues() 
    {
        if (encode == null)
        {
            encode = (COSArray) getCOSObject().getDictionaryObject(COSName.ENCODE);
            // the default value is [0 (size[0]-1) 0 (size[1]-1) ...]
            if (encode == null)
            {
                encode = new COSArray();
                COSArray sizeValues = getSize();
                int sizeValuesSize = sizeValues.size();
                for (int i = 0; i < sizeValuesSize; i++)
                {
                    encode.add(COSInteger.ZERO);
                    encode.add(COSInteger.get(sizeValues.getInt(i) - 1));
                }
            }
        }
        return encode;
    }

    /**
     * Returns all decode values as COSArray.
     * 
     * @return the decode array. 
     */
    private COSArray getDecodeValues() 
    {
        if (decode == null)
        {
            decode = (COSArray) getCOSObject().getDictionaryObject(COSName.DECODE);
            // if decode is null, the default values are the range values
            if (decode == null)
            {
                decode = getRangeValues();
            }
        }
        return decode;
    }

    /**
     * Get the encode for the input parameter.
     *
     * @param paramNum The function parameter number.
     *
     * @return The encode parameter range or null if none is set.
     */
    public PDRange getEncodeForParameter(int paramNum)
    {
        PDRange retval = null;
        COSArray encodeValues = getEncodeValues();
        if (encodeValues != null && encodeValues.size() >= paramNum * 2 + 1)
        {
            retval = new PDRange(encodeValues, paramNum);
        }
        return retval;
    }

    /**
     * This will set the encode values.
     *
     * @param encodeValues The new encode values.
     */
    public void setEncodeValues(COSArray encodeValues)
    {
        encode = encodeValues;
        getCOSObject().setItem(COSName.ENCODE, encodeValues);
    }

    /**
     * Get the decode for the input parameter.
     *
     * @param paramNum The function parameter number.
     *
     * @return The decode parameter range or null if none is set.
     */
    public PDRange getDecodeForParameter(int paramNum)
    {
        PDRange retval = null;
        COSArray decodeValues = getDecodeValues();
        if (decodeValues != null && decodeValues.size() >= paramNum * 2 + 1)
        {
            retval = new PDRange(decodeValues, paramNum);
        }
        return retval;
    }

    /**
     * This will set the decode values.
     *
     * @param decodeValues The new decode values.
     */
    public void setDecodeValues(COSArray decodeValues)
    {
        decode = decodeValues;
        getCOSObject().setItem(COSName.DECODE, decodeValues);
    }
    
    /**
     * calculate array index (structure described in p.171 PDF spec 1.7) in
     * multiple dimensions.
     *
     * @param vector with coordinates
     * @return index in flat array
     */
    private int calcSampleIndex(int[] vector)
    {
        // inspiration: http://stackoverflow.com/a/12113479/535646
        // but used in reverse
        float[] sizeValues = getSize().toFloatArray();
        int index = 0;
        int sizeProduct = 1;
        int dimension = vector.length;
        for (int i = dimension - 2; i >= 0; --i)
        {
            sizeProduct *= sizeValues[i];
        }
        for (int i = dimension - 1; i >= 0; --i)
        {
            index += sizeProduct * vector[i];
            if (i - 1 >= 0)
            {
                sizeProduct /= sizeValues[i - 1];
            }
        }
        return index;
    }

    /**
     * Inner class do to an interpolation in the Nth dimension by comparing the
     * content size of N-1 dimensional objects. This is done with the help of
     * recursive calls. To understand the algorithm without recursion, here is a
     * <a
     * href="http://harmoniccode.blogspot.de/2011/04/bilinear-color-interpolation.html">bilinear
     * interpolation</a> and here's a <a
     * href="https://en.wikipedia.org/wiki/Trilinear_interpolation">trilinear
     * interpolation</a> (external links).
     */
    private class Rinterpol
    {
        // coordinate that is to be interpolated
        private final float[] in;
        // coordinate of the "ceil" point
        private final int[] inPrev;
        // coordinate of the "floor" point
        private final int[] inNext;
        private final int numberOfInputValues;
        private final int numberOfOutputValues = getNumberOfOutputParameters();

        /**
         * Constructor.
         *
         * @param input the input coordinates
         * @param inputPrev coordinate of the "ceil" point
         * @param inputNext coordinate of the "floor" point
         *
         */
        Rinterpol(float[] input, int[] inputPrev, int[] inputNext)
        {
            in = input;
            inPrev = inputPrev;
            inNext = inputNext;
            numberOfInputValues = input.length;
        }

        /**
         * Calculate the interpolation.
         *
         * @return interpolated result sample
         */
        float[] rinterpolate()
        {
            return rinterpol(new int[numberOfInputValues], 0);
        }

        /**
         * Do a linear interpolation if the two coordinates can be known, or
         * call itself recursively twice.
         *
         * @param coord coord partially set coordinate (not set from step
         * upwards); gets fully filled in the last call ("leaf"), where it is
         * used to get the correct sample
         * @param step between 0 (first call) and dimension - 1
         * @return interpolated result sample
         */
        private float[] rinterpol(int[] coord, int step)
        {
            float[] resultSample = new float[numberOfOutputValues];
            if (step == in.length - 1)
            {
                // leaf
                if (inPrev[step] == inNext[step])
                {
                    coord[step] = inPrev[step];
                    int[] tmpSample = getSamples()[calcSampleIndex(coord)];
                    for (int i = 0; i < numberOfOutputValues; ++i)
                    {
                        resultSample[i] = tmpSample[i];
                    }
                    return resultSample;
                }
                coord[step] = inPrev[step];
                int[] sample1 = getSamples()[calcSampleIndex(coord)];
                coord[step] = inNext[step];
                int[] sample2 = getSamples()[calcSampleIndex(coord)];
                for (int i = 0; i < numberOfOutputValues; ++i)
                {
                    resultSample[i] = interpolate(in[step], inPrev[step], inNext[step], sample1[i], sample2[i]);
                }
                return resultSample;
            }
            else
            {
                // branch
                if (inPrev[step] == inNext[step])
                {
                    coord[step] = inPrev[step];
                    return rinterpol(coord, step + 1);
                }
                coord[step] = inPrev[step];
                float[] sample1 = rinterpol(coord, step + 1);
                coord[step] = inNext[step];
                float[] sample2 = rinterpol(coord, step + 1);
                for (int i = 0; i < numberOfOutputValues; ++i)
                {
                    resultSample[i] = interpolate(in[step], inPrev[step], inNext[step], sample1[i], sample2[i]);
                }
                return resultSample;
            }
        }
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public float[] eval(float[] input) throws IOException
    {
        //This involves linear interpolation based on a set of sample points.
        //Theoretically it's not that difficult ... see section 3.9.1 of the PDF Reference.

        float[] sizeValues = getSize().toFloatArray();
        int bitsPerSample = getBitsPerSample();
        float maxSample = (float) (Math.pow(2, bitsPerSample) - 1.0);
        int numberOfInputValues = input.length;
        int numberOfOutputValues = getNumberOfOutputParameters();

        int[] inputPrev = new int[numberOfInputValues];
        int[] inputNext = new int[numberOfInputValues];

        for (int i = 0; i < numberOfInputValues; i++)
        {
            PDRange domain = getDomainForInput(i);
            PDRange encodeValues = getEncodeForParameter(i);
            input[i] = clipToRange(input[i], domain.getMin(), domain.getMax());
            input[i] = interpolate(input[i], domain.getMin(), domain.getMax(), 
                    encodeValues.getMin(), encodeValues.getMax());
            input[i] = clipToRange(input[i], 0, sizeValues[i] - 1);
            inputPrev[i] = (int) Math.floor(input[i]);
            inputNext[i] = (int) Math.ceil(input[i]);
        }
        
        float[] outputValues = new Rinterpol(input, inputPrev, inputNext).rinterpolate();

        for (int i = 0; i < numberOfOutputValues; i++)
        {
            PDRange range = getRangeForOutput(i);
            PDRange decodeValues = getDecodeForParameter(i);
            outputValues[i] = interpolate(outputValues[i], 0, maxSample, decodeValues.getMin(), decodeValues.getMax());
            outputValues[i] = clipToRange(outputValues[i], range.getMin(), range.getMax());
        }

        return outputValues;
    }
}
