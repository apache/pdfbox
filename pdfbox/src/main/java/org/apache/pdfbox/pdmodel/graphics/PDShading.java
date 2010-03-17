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
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

//import java.awt.color.ColorSpace;
//import java.awt.image.ColorModel;

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
        return COSName.getPDFName( getName() );
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

    public int getShadingType()
    {
        return DictShading.getInt("ShadingType");
    }
    
    public PDColorSpace getColorSpace() throws IOException
    {
        return PDColorSpaceFactory.createColorSpace(DictShading.getDictionaryObject("ColorSpace"));
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() 
    {
        String sColorSpace;
        try
        {
            sColorSpace = getColorSpace().toString();
        }catch (IOException e)
        {
            sColorSpace = "Failure retrieving ColorSpace: " + e.toString();
        }
        String s = "Shading " + shadingname + "\n"
            + "\tShadingType: " + getShadingType() + "\n"
            + "\tColorSpace: " + sColorSpace + "\n"
            + "\tRaw Value:\n" +
         DictShading.toString();
        
        return s;
    }
    
}
