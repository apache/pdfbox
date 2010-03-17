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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import java.io.IOException;
import java.lang.Math;

/**
 * This class represents a type 2 function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType2 extends PDDictionaryFunction
{
    
    private COSArray C0, C1;

    /**
     * Constructor to create a new blank type 2 function.
     */
    protected PDFunctionType2()
    {
        super( 2 );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDFunctionType2( COSDictionary functionDictionary )
    {
        super( functionDictionary );
    }

    /**
    * {@inheritDoc}
    */
    public COSArray Eval(COSArray input) throws IOException
    {
        //This function performs exponential interpolation.
        //It uses only a single value as its input, but may produce a multi-valued output.
        //See PDF Reference section 3.9.2.
                
        double x = input.toFloatArray()[0];
        COSArray y = new COSArray();
        for (int j=0;j<getC0().size();j++)
        {
            //y[j] = C0[j] + x^N*(C1[j] - C0[j])
            float FofX =(float)( ((COSFloat)C0.get(j)).floatValue() + java.lang.Math.pow(x,(double)getN())*(((COSFloat)C1.get(j)).floatValue() - ((COSFloat)C0.get(j)).floatValue()) );
            y.add( new COSFloat( FofX));
        }
        
        return y;
    }
    
    protected COSArray getC0()
    {
        if(C0 == null)
        {
            C0 = getRangeArray("C0",1);
        }        
        return C0;
    }
    
    protected COSArray getC1()
    {
        if(C1 == null)
        {
            //can't use getRangeArray here as the default is 1.0, not 0.0.
            C1 = (COSArray)getCOSDictionary().getDictionaryObject( COSName.getPDFName( "C1" ) );
            if( C1 == null )
            {
                C1 = new COSArray();
                getCOSDictionary().setItem( "C1", C1 );
                C1.add( new COSFloat( 1 ) );
            }      
        }            
        return C1;
    }
    
    protected float getN(){
        return getCOSDictionary().getFloat("N");
    }
}
