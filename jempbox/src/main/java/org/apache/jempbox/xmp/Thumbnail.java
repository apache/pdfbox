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
package org.apache.jempbox.xmp;

import org.apache.jempbox.impl.XMLUtil;
import org.w3c.dom.Element;

/**
 * This class represents a thumbnail datatype.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class Thumbnail
{
    /**
     * A supported thumnail format.
     */
    public static final String FORMAT_JPEG = "JPEG";
    
    
    /**
     * The DOM representation of this object.
     */
    protected Element parent = null;
    
    /**
     * Create a new thumbnail element.
     * 
     * @param metadata The metadata document that his thumbnail will be part of.
     */
    public Thumbnail( XMPMetadata metadata )
    {
        this( metadata.xmpDocument.createElement( "rdf:li" ) );
    }
    
    /**
     * Create a thumnail based on a parent property set.
     *
     * @param parentElement The parent element that will store the thumbnail properties.
     */
    public Thumbnail( Element parentElement )
    {
        parent = parentElement;
        parent.setAttributeNS( 
            XMPSchema.NS_NAMESPACE, 
            "xmlns:xapGImg", 
            "http://ns.adobe.com/xap/1.0/g/img/" );
    }
    
    /**
     * Get the underlying XML element.
     * 
     * @return The XML element that this object represents.
     */
    public Element getElement()
    {
        return parent;
    }
    
    /**
     * Get the height of the image in pixels.
     * 
     * @return The height of the image in pixels.
     */
    public Integer getHeight()
    {
        return XMLUtil.getIntValue( parent, "xapGImg:height" );
    }
    
    /**
     * Set the height of the element.
     * 
     * @param height The updated height of the element.
     */
    public void setHeight( Integer height )
    {
        XMLUtil.setIntValue( parent, "xapGImg:height", height );
    }
    
    /**
     * Get the width of the image in pixels.
     * 
     * @return The width of the image in pixels.
     */
    public Integer getWidth()
    {
        return XMLUtil.getIntValue( parent, "xapGImg:width" );
    }
    
    /**
     * Set the width of the element.
     * 
     * @param width The updated width of the element.
     */
    public void setWidth( Integer width )
    {
        XMLUtil.setIntValue( parent, "xapGImg:width", width );
    }
    
    /**
     * Set the format of the thumbnail, currently only JPEG is supported.  See FORMAT_XXX constants.
     * 
     * @param format The image format.
     */
    public void setFormat( String format )
    {
        XMLUtil.setStringValue( parent, "xapGImg:format", format );
    }
    
    /**
     * Get the format of the thumbnail.  See FORMAT_XXX constants.
     * 
     * @return The image format.
     */
    public String getFormat()
    {
        return XMLUtil.getStringValue( parent, "xapGImg:format" );
    }
    
    /**
     * Set the image data in base 64 encoding.
     * 
     * @param image The image.
     */
    public void setImage( String image )
    {
        XMLUtil.setStringValue( parent, "xapGImg:image", image );
    }
    
    /**
     * Get the image data in base 64 encoding.
     * 
     * @return The image data.
     */
    public String getImage()
    {
        return XMLUtil.getStringValue( parent, "xapGImg:image" );
    }
}