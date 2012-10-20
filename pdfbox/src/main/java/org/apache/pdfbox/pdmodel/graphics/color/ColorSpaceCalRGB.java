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
package org.apache.pdfbox.pdmodel.graphics.color;

import java.awt.color.ColorSpace;

import org.apache.pdfbox.pdmodel.common.PDMatrix;

/**
 * This class represents a CalRGB color space.
 *
 * In the first place this implementation is needed to support CalRGB.
 * To keep it simple, the CalRGB colorspace is treated similar to a DeviceRGB colorspace.
 * There is no conversion including the gamma, whitepoint, blackpoint or matrix values yet.
 * This should be suitable for displaying and simple printings.
 *  
 * @author <a href="mailto:andreas@lehmi.de">Andreas Lehmkühler</a>
 * @version $Revision: 1.0 $
 */
public class ColorSpaceCalRGB extends ColorSpace
{
    private PDGamma gamma = null;
    private PDTristimulus whitepoint = null;
    private PDTristimulus blackpoint = null;
    private PDMatrix matrix = null;
    
    /**
     * ID for serialization.
     */
    private static final long serialVersionUID = -6362864473145799405L;

    /**
     *  Constructor.
     */
    public ColorSpaceCalRGB()
    {
        super(ColorSpace.TYPE_3CLR,3);
    }

    /**
     * Constructor.
     * @param gammaValue Gamma
     * @param whitept Whitepoint
     * @param blackpt Blackpoint
     * @param linearMatrix Matrix value
     */
    public ColorSpaceCalRGB(PDGamma gammaValue, PDTristimulus whitept, PDTristimulus blackpt, PDMatrix linearMatrix)
    {
        this();
        this.gamma = gammaValue;
        this.whitepoint = whitept;
        this.blackpoint = blackpt;
        this.matrix = linearMatrix;
    }

    /**
     *  Converts colorvalues from RGB-colorspace to CIEXYZ-colorspace.
     *  @param rgbvalue RGB colorvalues to be converted.
     *  @return Returns converted colorvalues.
     */
    private float[] fromRGBtoCIEXYZ(float[] rgbvalue) 
    {
        ColorSpace colorspaceRGB = ColorSpace.getInstance(CS_sRGB);
        return colorspaceRGB.toCIEXYZ(rgbvalue);
    }
    
    /**
     *  Converts colorvalues from CIEXYZ-colorspace to RGB-colorspace.
     *  @param rgbvalue CIEXYZ colorvalues to be converted.
     *  @return Returns converted colorvalues.
     */
    private float[] fromCIEXYZtoRGB(float[] xyzvalue) 
    {
        ColorSpace colorspaceXYZ = ColorSpace.getInstance(CS_CIEXYZ);
        return colorspaceXYZ.toRGB(xyzvalue);
    }

    /**
     * {@inheritDoc}
     */
    public float[] fromCIEXYZ(float[] colorvalue)
    {
        if (colorvalue != null && colorvalue.length == 3)
        {
            // We have to convert from XYV to RGB
            return fromCIEXYZtoRGB(colorvalue);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float[] fromRGB(float[] rgbvalue) 
    {
        if (rgbvalue != null && rgbvalue.length == 3) 
        {
            return rgbvalue;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float[] toCIEXYZ(float[] colorvalue) 
    {
        if (colorvalue != null && colorvalue.length == 4)
        {
            // We have to convert from RGB to XYV
            return fromRGBtoCIEXYZ(toRGB(colorvalue));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float[] toRGB(float[] colorvalue) 
    {
        if (colorvalue != null && colorvalue.length == 3) 
        {
            return colorvalue;
        }
        return null;
    }

}
