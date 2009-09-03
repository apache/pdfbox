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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import org.apache.pdfbox.exceptions.LoggingObject;

import org.apache.pdfbox.cos.COSArray;

/**
 * This class represents a color space and the color value for that colorspace.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class PDColorSpaceInstance extends LoggingObject implements Cloneable
{
    private PDColorSpace colorSpace = new PDDeviceGray();
    private COSArray colorSpaceValue = new COSArray();

    /**
     * Default constructor.
     *
     */
    public PDColorSpaceInstance()
    {
        setColorSpaceValue( new float[] {0});
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDColorSpaceInstance retval = new PDColorSpaceInstance();
        retval.colorSpace = this.colorSpace;
        retval.colorSpaceValue.clear();
        retval.colorSpaceValue.addAll( this.colorSpaceValue );
        return retval;
    }

    /**
     * Create the current color from the colorspace and values.
     * @return The current awt color.
     * @throws IOException If there is an error creating the color.
     */
    public Color createColor() throws IOException
    {
        Color retval = null;
        float[] components = colorSpaceValue.toFloatArray();
        try
        {
            if( colorSpace.getName().equals(PDDeviceRGB.NAME) && components.length == 3 )
            {
                //for some reason, when using RGB and the RGB colorspace
                //the new Color doesn't maintain exactly the same values
                //I think some color conversion needs to take place first
                //for now we will just make rgb a special case.
                retval = new Color( components[0], components[1], components[2] );
            }
            else
            {
                ColorSpace cs = colorSpace.createColorSpace();
                if (colorSpace.getName().equals(PDSeparation.NAME) && components.length == 1)
                {
                    //Use that component as a single-integer RGB value
                    retval = new Color((int)components[0]);
                }
                else
                {
                    retval = new Color( cs, components, 1f );
                }
            }
            return retval;
        }
        catch (java.lang.IllegalArgumentException exception)
        {
            String values = "Color Values: ";
            for(int i=0; i< components.length; i++)
            {
                values = values + components[i] + "\t";
            }
            logger().error(exception + "\n" + values, exception);
            throw exception;
        }
        catch (IOException ioexception)
        {
            logger().error(ioexception, ioexception);
            throw ioexception;
        }
        catch (Exception e)
        {
            logger().error(e, e);
            throw new IOException("Failed to Create Color");
         }
    }

    /**
     * Constructor with an existing color set.  Default colorspace is PDDeviceGray.
     *
     * @param csValues The color space values.
     */
    public PDColorSpaceInstance( COSArray csValues )
    {
        colorSpaceValue = csValues;
    }


    /**
     * This will get the current colorspace.
     *
     * @return The current colorspace.
     */
    public PDColorSpace getColorSpace()
    {
        return colorSpace;
    }

    /**
     * This will set the current colorspace.
     *
     * @param value The new colorspace.
     */
    public void setColorSpace(PDColorSpace value)
    {
        colorSpace = value;
    }

    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public float[] getColorSpaceValue()
    {
        return colorSpaceValue.toFloatArray();
    }

    /**
     * This will get the color space values.  Either 1 for gray or 3 for RGB.
     *
     * @return The colorspace values.
     */
    public COSArray getCOSColorSpaceValue()
    {
        return colorSpaceValue;
    }

    /**
     * This will update the colorspace values.
     *
     * @param value The new colorspace values.
     */
    public void setColorSpaceValue(float[] value)
    {
        colorSpaceValue.setFloatArray( value );
    }
}
