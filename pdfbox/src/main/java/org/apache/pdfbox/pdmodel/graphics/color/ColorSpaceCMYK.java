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

/**
 * This class represents a CMYK color space.
 *
 * @author <a href="mailto:andreas@lehmi.de">Andreas Lehmkühler</a>
 * @version $Revision: 1.0 $
 */
public class ColorSpaceCMYK extends ColorSpace
{
    /**
     * IDfor serialization.
     */
    private static final long serialVersionUID = -6362864473145799405L;
    
    /**
     * Constructor.
     */
    public ColorSpaceCMYK()
    {
        super(ColorSpace.TYPE_CMYK,4);
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
            // We have to convert from XYV to RGB to CMYK
            return fromRGB(fromCIEXYZtoRGB(colorvalue));
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
            // First of all we have to convert from RGB to CMY
            float c = 1 - rgbvalue[0];
            float m = 1 - rgbvalue[1];
            float y = 1 - rgbvalue[2];
            // Now we have to convert from CMY to CMYK
            float varK = 1;
            float[] cmyk = new float[4];
            if ( c < varK )
            {
                varK = c;
            }
            if ( m < varK )
            {
                varK = m;
            }
            if ( y < varK ) 
            {
                varK = y;
            }
            if ( varK == 1 ) 
            {
                cmyk[0] = cmyk[1] = cmyk[2] = 0;
            }
            else 
            {
                cmyk[0] = ( c - varK ) / ( 1 - varK );
                cmyk[1] = ( m - varK ) / ( 1 - varK );
                cmyk[2] = ( y - varK ) / ( 1 - varK );
            }
            cmyk[3] = varK;
            return cmyk;
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
            // We have to convert from CMYK to RGB to XYV
            return fromRGBtoCIEXYZ(toRGB(colorvalue));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float[] toRGB(float[] colorvalue) 
    {
        if (colorvalue != null && colorvalue.length == 4) 
        {
            // First of all we have to convert from CMYK to CMY
            float k = colorvalue[3];
            float c = ( colorvalue[0] * ( 1 - k ) + k );
            float m = ( colorvalue[1] * ( 1 - k ) + k );
            float y = ( colorvalue[2] * ( 1 - k ) + k );
            // Now we have to convert from CMY to RGB
            return new float[] { 1 - c, 1 - m, 1 - y };
        }
        return null;
    }

}
