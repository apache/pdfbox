/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox.type;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;

/**
 * Object representation of an Thumbnail XMP type
 * 
 * @author eric
 */
@StructuredType(preferedPrefix = "xmpGImg", namespace = "http://ns.adobe.com/xap/1.0/g/img/")
public class ThumbnailType extends AbstractStructuredType
{

    @PropertyType(type = Types.Choice, card = Cardinality.Simple)
    public static final String FORMAT = "format";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String HEIGHT = "height";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String WIDTH = "width";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String IMAGE = "image";

    /**
     * 
     * @param metadata
     *            The metadata to attach to this property
     */
    public ThumbnailType(final XMPMetadata metadata)
    {
        super(metadata);
        setAttribute(new Attribute(XmpConstants.RDF_NAMESPACE, "parseType", "Resource"));
    }

    /**
     * Get Height
     * 
     * @return the height
     */
    public Integer getHeight()
    {
        final AbstractField absProp = getFirstEquivalentProperty(HEIGHT, IntegerType.class);
        if (absProp != null)
        {
            return ((IntegerType) absProp).getValue();
        }
        return null;
    }

    /**
     * Set Height
     * 
     * @param height
     *            the value of Height property to set
     */
    public void setHeight(final Integer height)
    {
        addSimpleProperty(HEIGHT, height);
    }

    /**
     * Get Width
     * 
     * @return the width
     */
    public Integer getWidth()
    {
        final AbstractField absProp = getFirstEquivalentProperty(WIDTH, IntegerType.class);
        if (absProp != null)
        {

            return ((IntegerType) absProp).getValue();
        }
        return null;
    }

    /**
     * Set Width
     * 
     * @param width
     *            the value of width property to set
     */
    public void setWidth(final Integer width)
    {
        addSimpleProperty(WIDTH, width);
    }

    /**
     * Get The img data
     * 
     * @return the img
     */
    public String getImage()
    {
        final AbstractField absProp = getFirstEquivalentProperty(IMAGE, TextType.class);
        if (absProp != null)
        {
            return ((TextType) absProp).getStringValue();
        }
        return null;
    }

    /**
     * Set Image data
     * 
     * @param image
     *            the value of image property to set
     */
    public void setImage(final String image)
    {
        addSimpleProperty(IMAGE, image);
    }

    /**
     * Get Format
     * 
     * @return the format
     */
    public String getFormat()
    {
        final AbstractField absProp = getFirstEquivalentProperty(FORMAT, ChoiceType.class);
        if (absProp != null)
        {
            return ((TextType) absProp).getStringValue();
        }
        return null;
    }

    /**
     * Set Format
     * 
     * @param format
     *            the value of format property to set
     */
    public void setFormat(final String format)
    {
        addSimpleProperty(FORMAT, format);
    }

}
