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
package org.apache.pdfbox.pdmodel.graphics;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;



import java.io.IOException;

/**
 * This class represents a Shading Pattern color space.
 *  See section 4.6.3 of the PDF 1.7 specification.
 *
 * @author <a href="mailto:Daniel.Wilson@BlackLocustSoftware.com">Daniel wilson</a>
 * @version $Revision: 1.0 $
 */
public class PDShading implements COSObjectable
{
    private COSDictionary DictShading;
    private COSName shadingname;

    /**
     * The name of this object.
     */
    public static final String NAME = "Shading";

    /**
     * Default constructor.
     */
    public PDShading()
    {
        DictShading = new COSDictionary();
        //DictShading.add( COSName.getPDFName( NAME ) );
    }

    /**
     * Constructor.
     *
     * @param shading The shading dictionary.
     */
    public PDShading(COSName name, COSDictionary shading)
    {
        DictShading = shading;
        shadingname = name;
    }

    /**
     * This will return the name of the object.
     *
     * @return The name of the object.
     */
    public String getName()
    {
        return NAME;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return COSName.SHADING;
    }
    
    /**
    * This will return the name of this particular shading dictionary
    *
    * @return The name of the shading dictionary
    */
    public COSName getShadingName()
    {
        return shadingname;
    }
    
    /**
    * This will return the ShadingType -- an integer between 1 and 7 that specifies the gradient type.
    * Required in all Shading Dictionaries.
    *
    * @return The Shading Type
    */
    public int getShadingType()
    {
        return DictShading.getInt("ShadingType");
    }
    
    /**
    * This will return the Color Space.
    * Required in all Shading Dictionaries.
    *
    * @return The Color Space of the shading dictionary
    */
    public PDColorSpace getColorSpace() throws IOException
    {
        return PDColorSpaceFactory.createColorSpace(DictShading.getDictionaryObject("ColorSpace"));
    }
    
    /**
    * This will return a boolean flag indicating whether to antialias the shading pattern.
    *
    * @return The antialias flag, defaulting to False
    */
    public boolean getAntiAlias()
    {
        return DictShading.getBoolean("AntiAlias",false);
    }
    
    /**
    * Returns the coordinate array used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The coordinate array.
    */
    public COSArray getCoords()
    {
        return (COSArray)(DictShading.getDictionaryObject("Coords"));
    }
    
    /**
    * Returns the function used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The gradient function.
    */
    public PDFunction getFunction() throws IOException
    {
        return PDFunction.create(DictShading.getDictionaryObject("Function"));
    }
    
    /**
    * Returns the Domain array used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The Domain array.
    */
    public COSArray getDomain()
    {
        return (COSArray)(DictShading.getDictionaryObject("Domain"));
    }
    
    /**
    * Returns the Extend array used by several of the gradient types. Interpretation depends on the ShadingType.
    * Default is {false, false}.
    *
    * @return The Extend array.
    */
    public COSArray getExtend()
    {
        COSArray arExtend=(COSArray)(DictShading.getDictionaryObject("Extend"));
        if (arExtend == null)
        {
            arExtend = new COSArray();
            arExtend.add(COSBoolean.FALSE);
            arExtend.add(COSBoolean.FALSE);
        }
        
        return arExtend;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() 
    {
        String sColorSpace;
        String sFunction;
        try
        {
            sColorSpace = getColorSpace().toString();
        }catch (IOException e)
        {
            sColorSpace = "Failure retrieving ColorSpace: " + e.toString();
        }
        try
        {
            sFunction = getFunction().toString();
        }catch(IOException e)
        {
            sFunction = "n/a";
        }
        
        
        String s = "Shading " + shadingname + "\n"
            + "\tShadingType: " + getShadingType() + "\n"
            + "\tColorSpace: " + sColorSpace + "\n"
            + "\tAntiAlias: " + getAntiAlias() + "\n"
            + "\tCoords: " + getCoords().toString() + "\n"
            + "\tDomain: " + getDomain().toString() + "\n"
            + "\tFunction: " + sFunction + "\n"
            + "\tExtend: " + getExtend().toString() + "\n"
            + "\tRaw Value:\n" +
         DictShading.toString();
        
        return s;
    }
    
}
