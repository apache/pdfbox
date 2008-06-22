/**
 * Copyright (c) 2006, www.jempbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.jempbox.org
 *
 */
package org.jempbox.xmp;

import org.jempbox.impl.XMLUtil;
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
        this( metadata.xmpDocument.createElement( "rfd:li" ) );
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
        return XMLUtil.getStringValue( parent, "xapGImg:format" );
    }
}