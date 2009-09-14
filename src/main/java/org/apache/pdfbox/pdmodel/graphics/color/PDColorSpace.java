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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSArray;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

import java.io.IOException;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;


/**
 * This class represents a color space in a pdf document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public abstract class PDColorSpace implements COSObjectable
{
    /**
     * array for the given parameters. 
     */
    protected COSArray array;

    /**
     * Cached Java AWT color space.
     *
     * @see #getJavaColorSpace()
     */
    private ColorSpace colorSpace = null;

    /**
     * This will return the name of the color space.
     *
     * @return The name of the color space.
     */
    public abstract String getName();

    /**
     * This will get the number of components that this color space is made up of.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public abstract int getNumberOfComponents() throws IOException;

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
     * Returns the Java AWT color space for this instance.
     *
     * @return Java AWT color space
     * @throws IOException if the color space can not be created
     */
    public ColorSpace getJavaColorSpace() throws IOException {
        if (colorSpace == null) {
            colorSpace = createColorSpace();
        }
        return colorSpace;
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    protected abstract ColorSpace createColorSpace() throws IOException;

    /**
     * Create a Java color model for this colorspace.
     *
     * @param bpc The number of bits per component.
     *
     * @return A color model that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color model.
     */
    public abstract ColorModel createColorModel( int bpc ) throws IOException;

    /*
    Don't just tell me its color type -- tell me its contents!
    */
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return  getName() + "{ " + (array==null? "" : array.toString() ) + " }";
    }
}
