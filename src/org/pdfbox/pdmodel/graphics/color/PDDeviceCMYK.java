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
package org.pdfbox.pdmodel.graphics.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;

import java.io.InputStream;
import java.io.IOException;

import org.pdfbox.util.ResourceLoader;

/**
 * This class represents a CMYK color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDDeviceCMYK extends PDColorSpace
{
    /**
     * The single instance of this class.
     */
    public static final PDDeviceCMYK INSTANCE = new PDDeviceCMYK();
    
    /**
     * The name of this color space.
     */
    public static final String NAME = "DeviceCMYK";

    /**
     * The abbreviated name of this color space.
     */
    public static final String ABBREVIATED_NAME = "CMYK";
    
    private ColorSpace cSpace = null;
    
    private PDDeviceCMYK()
    {
        
    }

    /**
     * This will return the name of the color space.
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * This will get the number of components that this color space is made up of.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return 4;
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    public ColorSpace createColorSpace() throws IOException
    {
        if( cSpace == null )
        {
            InputStream profile = null;
            try
            {
                profile = ResourceLoader.loadResource( "Resources/colorspace-profiles/CMYK.pf" );
                ICC_Profile iccProfile = ICC_Profile.getInstance( profile );
                cSpace = new ICC_ColorSpace( iccProfile );
            }
            finally
            {
                if( profile != null )
                {
                    profile.close();
                }
            }
        }
        return cSpace;
    }
    
    /**
     * Create a Java color model for this colorspace.
     *
     * @param bpc The number of bits per component.
     * 
     * @return A color model that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel( int bpc ) throws IOException
    {
        logger().warning ("This looks like a test case for sector9's patch!!!  We need one of these!!!");
        throw new IOException( "Not implemented" );
    }
}