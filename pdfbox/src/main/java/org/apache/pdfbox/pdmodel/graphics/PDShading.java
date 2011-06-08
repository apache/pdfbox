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
import org.apache.pdfbox.cos.COSFloat;
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
    private COSArray domain = null;
    private COSArray extend = null;
    private PDFunction function = null;
    private PDColorSpace colorspace = null;
    
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
        return DictShading.getInt(COSName.SHADING_TYPE);
    }
    
    /**
    * This will return the Color Space.
    * Required in all Shading Dictionaries.
    *
    * @return The Color Space of the shading dictionary
    */
    public PDColorSpace getColorSpace() throws IOException
    {
        if (colorspace == null)
        {
            colorspace = PDColorSpaceFactory.createColorSpace(DictShading.getDictionaryObject(COSName.COLORSPACE));
        }
        return colorspace;
    }
    
    /**
    * This will return a boolean flag indicating whether to antialias the shading pattern.
    *
    * @return The antialias flag, defaulting to False
    */
    public boolean getAntiAlias()
    {
        return DictShading.getBoolean(COSName.ANTI_ALIAS,false);
    }
    
    /**
    * Returns the coordinate array used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The coordinate array.
    */
    public COSArray getCoords()
    {
        return (COSArray)(DictShading.getDictionaryObject(COSName.COORDS));
    }
    
    /**
    * Returns the function used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The gradient function.
    */
    public PDFunction getFunction() throws IOException
    {
        if (function == null)
        {
            function = PDFunction.create(DictShading.getDictionaryObject(COSName.FUNCTION));
        }
        return function;
    }
    
    /**
    * Returns the Domain array used by several of the gradient types. Interpretation depends on the ShadingType.
    *
    * @return The Domain array.
    */
    public COSArray getDomain()
    {
        if (domain == null) 
        {
            domain = (COSArray)(DictShading.getDictionaryObject(COSName.DOMAIN));
            // use default values
            if (domain == null) 
            {
                domain = new COSArray();
                domain.add(new COSFloat(0.0f));
                domain.add(new COSFloat(1.0f));
            }
        }
        return domain;
    }
    
    /**
    * Returns the Extend array used by several of the gradient types. Interpretation depends on the ShadingType.
    * Default is {false, false}.
    *
    * @return The Extend array.
    */
    public COSArray getExtend()
    {
        if (extend == null)
        {
            extend = (COSArray)(DictShading.getDictionaryObject(COSName.EXTEND));
            // use default values
            if (extend == null)
            {
                extend = new COSArray();
                extend.add(COSBoolean.FALSE);
                extend.add(COSBoolean.FALSE);
            }
        }
        return extend;
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
            + "\tCoords: " + (getCoords() != null ? getCoords().toString() : "") + "\n"
            + "\tDomain: " + getDomain().toString() + "\n"
            + "\tFunction: " + sFunction + "\n"
            + "\tExtend: " + getExtend().toString() + "\n"
            + "\tRaw Value:\n" +
         DictShading.toString();
        
        return s;
    }
    
}
