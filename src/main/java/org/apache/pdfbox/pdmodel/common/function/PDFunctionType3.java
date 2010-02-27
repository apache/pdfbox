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

/**
 * This class represents a type 3 function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType3 extends PDDictionaryFunction
{

    /**
     * Constructor to create a new blank type 3 function.
     */
    protected PDFunctionType3()
    {
        super( 3 );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDFunctionType3( COSDictionary functionDictionary )
    {
        super( functionDictionary );
    }

    /**
    * {@inheritDoc}
    */
    public COSArray Eval(COSArray input) throws IOException
    {
        //This function is known as a "stitching" function. Based on the input, it decides which child function to call.
        //See PDF Reference section 3.9.3.
        
        PDFunction F=null;
        float x = ((COSFloat)input.get(0)).floatValue();
        COSArray Domain = getDomainForInput(1).getCOSArray();
        
        if (getBounds().size() == 0)
        {
            F = PDFunction.create(getFunctions().get(0));
        }
        else
        {
            //check boundary conditions first ...
            if (x < ((COSFloat)Domain.get(0)).floatValue())
                F = PDFunction.create(getFunctions().get(0));
            else if (x > ((COSFloat)Domain.get(1)).floatValue())
                F = PDFunction.create(getFunctions().get(getFunctions().size()-1));
            else
            {
                float[] fBounds = getBounds().toFloatArray();
                for (int k = 0; k<getBounds().size(); k++){
                    if (x <= fBounds[k])
                    {
                        F = PDFunction.create(getFunctions().get(k));
                        break;
                    }
                }
                if(F==null) //must be in last partition
                {
                    F = PDFunction.create(getFunctions().get(getFunctions().size()-1));
                }
            }
        }
        return F.Eval(input);
    }
    
    protected COSArray getFunctions()
    {
        return (COSArray)(getCOSDictionary().getDictionaryObject( COSName.getPDFName( "Functions" )  ));
    }
    
    protected COSArray getBounds()
    {
        return (COSArray)(getCOSDictionary().getDictionaryObject( COSName.getPDFName( "Bounds" )  ));
    }
    
    protected COSArray getEncode()
    {
        return (COSArray)(getCOSDictionary().getDictionaryObject( COSName.getPDFName( "Encode" )  ));
    }
}
