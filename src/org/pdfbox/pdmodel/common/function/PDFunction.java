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

import java.io.IOException;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;
import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.PDRange;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This class represents a function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public abstract class PDFunction implements COSObjectable
{
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
        if( function instanceof COSDictionary )
        {
            COSDictionary funcDic = (COSDictionary)function;
            int functionType =  funcDic.getInt( "FunctionType" );
            if( function instanceof COSStream )
            {
                if( functionType == 0 )
                {
                    retval = new PDFunctionType0(new PDStream((COSStream)function));
                }
                else if( functionType == 4 )
                {
                    retval = new PDFunctionType4(new PDStream((COSStream)function));
                }
                else
                {
                    throw new IOException( "Error: Unknown stream function type " + functionType );
                }
            }
            else
            {
                if( functionType == 2 )
                {
                    retval = new PDFunctionType2(funcDic);
                }
                else if( functionType == 3 )
                {
                    retval = new PDFunctionType3(funcDic);
                }
                else
                {
                    throw new IOException( "Error: Unknown dictionary function type " + functionType );
                }       
            }
            
        }
        else
        {
            throw new IOException( "Error: Unknown COS type for function " + function );
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
    public abstract int getNumberOfOutputParameters();

    /**
     * This will get the range for a certain output parameters.  This is will never
     * return null.  If it is not present then the range 0 to 0 will
     * be returned.
     *
     * @param n The output parameter number to get the range for.
     *
     * @return The range for this component.
     */
    public abstract PDRange getRangeForOutput(int n);

    /**
     * This will set the a range for output parameter.
     *
     * @param range The new range for the output parameter.
     * @param n The ouput parameter number to set the range for.
     */
    public abstract void setRangeForOutput(PDRange range, int n);

    /**
     * This will get the number of input parameters that
     * have a domain specified.
     * 
     * @return The number of input parameters that have a domain
     * specified.
     */
    public abstract int getNumberOfInputParameters();

    /**
     * This will get the range for a certain input parameter.  This is will never
     * return null.  If it is not present then the range 0 to 0 will
     * be returned.
     *
     * @param n The parameter number to get the domain for.
     *
     * @return The domain range for this component.
     */
    public abstract PDRange getDomainForInput(int n);

    /**
     * This will set the domain for the input values.
     *
     * @param range The new range for the input.
     * @param n The number of the input parameter to set the domain for.
     */
    public abstract void setDomainForInput(PDRange range, int n);

}