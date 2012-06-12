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
package org.apache.pdfbox.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents an Operator in the content stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public class PDFOperator
{
    private String theOperator;
    private byte[] imageData;
    private ImageParameters imageParameters;

    /** map for singleton operator objects; use {@link ConcurrentHashMap} for better scalability with multiple threads */
    private final static ConcurrentHashMap<String,PDFOperator> operators = new ConcurrentHashMap<String, PDFOperator>();

    /**
     * Constructor.
     *
     * @param aOperator The operator that this object will represent.
     */
    private PDFOperator( String aOperator )
    {
        theOperator = aOperator;
        if( aOperator.startsWith( "/" ) )
        {
            throw new RuntimeException( "Operators are not allowed to start with / '" + aOperator + "'" );
        }
    }

    /**
     * This is used to create/cache operators in the system.
     *
     * @param operator The operator for the system.
     *
     * @return The operator that matches the operator keyword.
     */
    public static PDFOperator getOperator( String operator )
    {
        PDFOperator operation = null;
        if( operator.equals( "ID" ) || operator.equals( "BI" ) )
        {
            //we can't cache the ID operators.
            operation = new PDFOperator( operator );
        }
        else
        {
            operation = operators.get( operator );
            if( operation == null )
            {
                // another thread may has already added an operator of this kind
                // make sure that we get the same operator
                operation = operators.putIfAbsent( operator, new PDFOperator( operator ) );
                if ( operation == null )
                {
                    operation = operators.get( operator );
                }
            }
        }

        return operation;
    }

    /**
     * This will get the operation that this operator represents.
     *
     * @return The string representation of the operation.
     */
    public String getOperation()
    {
        return theOperator;
    }

    /**
     * This will print a string rep of this class.
     *
     * @return A string rep of this class.
     */
    public String toString()
    {
        return "PDFOperator{" + theOperator + "}";
    }

    /**
     * This is the special case for the ID operator where there are just random
     * bytes inlined the stream.
     *
     * @return Value of property imageData.
     */
    public byte[] getImageData()
    {
        return this.imageData;
    }

    /**
     * This will set the image data, this is only used for the ID operator.
     *
     * @param imageDataArray New value of property imageData.
     */
    public void setImageData(byte[] imageDataArray)
    {
        imageData = imageDataArray;
    }

    /**
     * This will get the image parameters, this is only valid for BI operators.
     *
     * @return The image parameters.
     */
    public ImageParameters getImageParameters()
    {
        return imageParameters;
    }

    /**
     * This will set the image parameters, this is only valid for BI operators.
     *
     * @param params The image parameters.
     */
    public void setImageParameters( ImageParameters params)
    {
        imageParameters = params;
    }
}
