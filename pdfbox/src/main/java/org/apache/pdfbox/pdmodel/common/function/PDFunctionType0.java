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
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType0 extends PDFunction
{
 
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDFunctionType0.class);

    /**
     * An array of 2 × m numbers specifying the linear mapping of input values 
     * into the domain of the function’s sample table. 
     * Default value: [ 0 (Size0 − 1) 0 (Size1 − 1) … ].
     */
    private COSArray encode = null;
    /**
     * An array of 2 × n numbers specifying the linear mapping of sample values 
     * into the range appropriate for the function’s output values. 
     * Default value: same as the value of Range
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
     * @param functionStream The function .
     */
    public PDFunctionType0(COSBase function)
    {
        super( function );
    }

    /**
     * {@inheritDoc}
     */
    public int getFunctionType()
    {
        return 0;
    }
    /**
     * The "Size" entry, which is the number of samples in
     * each input dimension of the sample table.
     *
     * @return A List of java.lang.Integer objects.
     */
    public COSArray getSize()
    {
        if (size == null)
        {
            size = (COSArray)getDictionary().getDictionaryObject( COSName.SIZE );
        }
        return size;
    }

    /**
     * Get all sample values of this function.
     * 
     * @return an array with all samples.
     */
    public int[][] getSamples()
    {
        if (samples == null)
        {
            int arraySize = 1;
            int numberOfInputValues = getNumberOfInputParameters();
            int numberOfOutputValues = getNumberOfOutputParameters();
            COSArray sizes = getSize();
            for (int i=0;i<numberOfInputValues;i++)
            {
                arraySize *= sizes.getInt(i);
            }
            samples = new int[arraySize][getNumberOfOutputParameters()];
            int bitsPerSample = getBitsPerSample();
            int index = 0;
            int arrayIndex = 0;
            try {
                byte[] samplesArray = getPDStream().getByteArray();
                for (int i=0;i<numberOfInputValues;i++) 
                {
                    int sizeInputValues = sizes.getInt(i);
                    for (int j=0;j<sizeInputValues;j++)
                    {
                        int bitsLeft = 0;
                        int bitsToRead = bitsPerSample;
                        int currentValue = 0;
                        for (int k=0;k<numberOfOutputValues;k++)
                        {
                            if (bitsLeft == 0)
                            {
                                currentValue = (samplesArray[arrayIndex++]+256)%256;
                                bitsLeft = 8;
                            }
                            int value = 0;
                            while (bitsToRead > 0)
                            {
                                int bits = Math.min(bitsToRead, bitsLeft);
                                value = value << bits;
                                int valueToAdd = currentValue >> (8 - bits); 
                                value |= valueToAdd;
                                bitsToRead -= bits;
                                bitsLeft -= bits;
                                if (bitsLeft == 0 && bitsToRead > 0)
                                {
                                    currentValue = (samplesArray[arrayIndex++]+256)%256;
                                    bitsLeft = 8;
                                }
                            }
                            samples[index][k] = value;
                            bitsToRead = bitsPerSample;
                        }
                        index++;
                    }
                }
            }
            catch (IOException exception)
            {
                log.error("IOException while reading the sample values of this function.");
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
        return getDictionary().getInt( COSName.BITS_PER_SAMPLE );
    }

    /**
     * Set the number of bits that the output value will take up.  Valid values
     * are 1,2,4,8,12,16,24,32.
     *
     * @param bps The number of bits for each output value.
     */
    public void setBitsPerSample( int bps )
    {
        getDictionary().setInt( COSName.BITS_PER_SAMPLE, bps );
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
            encode = (COSArray)getDictionary().getDictionaryObject( COSName.ENCODE );
            // the default value is [0 (size[0]-1) 0 (size[1]-1) ...]
            if (encode == null)
            {
                encode = new COSArray();
                COSArray sizeValues = getSize();
                int sizeValuesSize = sizeValues.size();
                for (int i=0; i <sizeValuesSize; i++)
                {
                    encode.add( COSInteger.ZERO );
                    encode.add( COSInteger.get( sizeValues.getInt(i) - 1) );
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
            decode = (COSArray)getDictionary().getDictionaryObject( COSName.DECODE );
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
    public PDRange getEncodeForParameter( int paramNum )
    {
        PDRange retval = null;
        COSArray encodeValues = getEncodeValues();
        if( encodeValues != null && encodeValues.size() >= paramNum*2+1 )
        {
            retval = new PDRange(encodeValues, paramNum );
        }
        return retval;
    }

    /**
     * This will set the encode values.
     *
     * @param range The new encode values.
     */
    public void setEncodeValues(COSArray encodeValues)
    {
        encode = encodeValues;
        getDictionary().setItem(COSName.ENCODE, encodeValues);
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
        COSArray decodeValues = getDecodeValues();
        if( decodeValues != null && decodeValues.size() >= paramNum*2+1 )
        {
            retval = new PDRange(decodeValues, paramNum );
        }
        return retval;
    }

    /**
     * This will set the decode values.
     *
     * @param range The new decode values.
     */
    public void setDecodeValues(COSArray decodeValues)
    {
        decode = decodeValues;
        getDictionary().setItem(COSName.DECODE, decodeValues);
    }
    
    /**
    * {@inheritDoc}
    */
    public COSArray eval(COSArray input) throws IOException
    {
        //This involves linear interpolation based on a set of sample points.
        //Theoretically it's not that difficult ... see section 3.9.1 of the PDF Reference.
        float[] inputValues = input.toFloatArray();
        float[] sizeValues = getSize().toFloatArray();
        int bitsPerSample = getBitsPerSample();
        int numberOfInputValues = inputValues.length;
        int numberOfOutputValues = getNumberOfOutputParameters();
        int[] intInputValuesPrevious = new int[numberOfInputValues];
        int[] intInputValuesNext = new int[numberOfInputValues];
        for (int i=0; i<numberOfInputValues; i++) {
            PDRange domain = getDomainForInput(i);
            PDRange encode = getEncodeForParameter(i);
            inputValues[i] = clipToRange(inputValues[i], domain.getMin(), domain.getMax());
            inputValues[i] = interpolate(inputValues[i], domain.getMin(), domain.getMax(), encode.getMin(), encode.getMax());
            inputValues[i] = clipToRange(inputValues[i], 0, sizeValues[i]-1);
            intInputValuesPrevious[i] = (int)Math.floor(inputValues[i]);
            intInputValuesNext[i] = (int)Math.ceil(inputValues[i]);
        }
        float[] outputValuesPrevious = null;
        float[] outputValuesNext = null;
        outputValuesPrevious = getSample(intInputValuesPrevious);
        outputValuesNext = getSample(intInputValuesNext);
        float[] outputValues = new float[numberOfOutputValues];
        for (int i=0;i<numberOfOutputValues;i++) 
        {
            PDRange range = getRangeForOutput(i);
            PDRange decode = getDecodeForParameter(i);
            // TODO using only a linear interpolation. 
            // See "Order" entry in table 3.36 of the PDF reference
            outputValues[i] = (outputValuesPrevious[i] + outputValuesNext[i]) / 2;
            outputValues[i] = interpolate(outputValues[i], 0, (float)Math.pow(2, bitsPerSample), decode.getMin(), decode.getMax());
            outputValues[i] = clipToRange(outputValues[i], range.getMin(), range.getMax());
        }

        COSArray result = new COSArray();
        result.setFloatArray(outputValues);
        return result;
    }
    
    /**
     * Get the samples for the given input values.
     * 
     * @param inputValues an array containing the input values
     * @return an array with the corresponding samples
     */
    private float[] getSample(int[] inputValues)
    {
        int[][] sampleValues = getSamples();
        COSArray sizes = getSize();
        int numberOfInputValues = getNumberOfInputParameters();
        int index = 0;
        int previousSize = 1;
        for (int i=0;i<numberOfInputValues;i++)
        {
            index += inputValues[i];
            previousSize *= sizes.getInt(i);
        }
        int numberOfOutputValues = getNumberOfOutputParameters();
        float[] result = new float[numberOfOutputValues];
        for (int i=0;i<numberOfOutputValues;i++)
        {
            result[i] = sampleValues[index][i];
        }
        return result;
    }
}
