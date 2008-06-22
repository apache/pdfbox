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
 * This class represents a multiple part reference to a resource.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class ResourceRef implements Elementable
{   
    /**
     * The DOM representation of this object.
     */
    protected Element parent = null;
    
    /**
     * Create a resource reference based on a existing parent property set.
     *
     * @param parentElement The parent element that will store the resource properties.
     */
    public ResourceRef( Element parentElement )
    {
        parent = parentElement;
        if( !parent.hasAttribute( "xmlns:stRef" ) )
        {
            parent.setAttributeNS( 
                "http://ns.adobe.com/xap/1.0/sType/ResourceRef#", 
                "xmlns:stRef", 
                "http://ns.adobe.com/xap/1.0/sType/ResourceRef#" );
        }
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
     * Get the referenced resource's id.
     * 
     * @return The id of the reference.
     */
    public String getInstanceID()
    {
        return XMLUtil.getStringValue( parent, "stRef:instanceID" );
    }
    
    /**
     * Set the referenced resource's id.
     * 
     * @param id The id of the reference.
     */
    public void setInstanceID( String id )
    {
        XMLUtil.setStringValue( parent, "stRef:instanceID", id );
    }
    
    /**
     * Get the referenced resource's document id.
     * 
     * @return The id of the reference document.
     */
    public String getDocumentID()
    {
        return XMLUtil.getStringValue( parent, "stRef:documentID" );
    }
    
    /**
     * Set the referenced resource's document id.
     * 
     * @param id The id of the reference document.
     */
    public void setDocumentID( String id )
    {
        XMLUtil.setStringValue( parent, "stRef:documentID", id );
    }
    
    /**
     * Get the referenced resource's document version id.
     * 
     * @return The id of the reference document version.
     */
    public String getVersionID()
    {
        return XMLUtil.getStringValue( parent, "stRef:versionID" );
    }
    
    /**
     * Set the referenced resource's version id.
     * 
     * @param id The id of the reference document version.
     */
    public void setVersionID( String id )
    {
        XMLUtil.setStringValue( parent, "stRef:veresionID", id );
    }
    
    /**
     * Get the rendition class.
     * 
     * @return The value of the rendition class property.
     * 
     * @see ResourceRef#setRenditionClass( String )
     */
    public String getRenditionClass()
    {
        return XMLUtil.getStringValue( parent, "stRef:renditionClass" );
    }
    
    /**
     * Set the rendition class.  The rendition class is derived from a defined
     * set of names.  The value is series of colon separated tokens and parameters.<br/>
     * Defined values are:<br/>
     * <table>
     *     <tr><td>Token Name</td><td>Description</td></tr>
     *     <tr><td>default</td><td>Specifies master document, no additional tokens allowed</td></tr>
     *     <tr><td>thumbnail</td>
     *         <td>A simplied preview.  Recommended order is: thumbnail<i>format:size:colorspace</i></td></tr>
     *     <tr><td>screen</td><td>Screen resolution</td></tr>
     *     <tr><td>proof</td><td>A review proof</td></tr>
     *     <tr><td>draft</td><td>A review rendition</td></tr>
     *     <tr><td>low-res</td><td>A low resolution, full size stand-in</td><tr>
     * </table>
     * 
     * 
     * @param renditionClass The rendition class.
     */
    public void setRenditionClass( String renditionClass )
    {
        XMLUtil.setStringValue( parent, "stRef:renditionClass", renditionClass );
    }
    
    /**
     * Get the extra rendition params.
     * 
     * @return Additional rendition parameters.
     */
    public String getRenditionParams()
    {
        return XMLUtil.getStringValue( parent, "stRef:renditionParams" );
    }
    
    /**
     * Set addition rendition params.
     * 
     * @param params Additional rendition parameters that are too complex for the rendition class.
     */
    public void setRenditionParams( String params )
    {
        XMLUtil.setStringValue( parent, "stRef:renditionParams", params );
    }
    
    /**
     * Get name of the asset management system that manages this resource.
     * 
     * @return The name of a asset management system.
     */
    public String getManager()
    {
        return XMLUtil.getStringValue( parent, "stRef:manager" );
    }
    
    /**
     * Set the name of the system that manages this resource.
     * 
     * @param manager The name of the management system.
     */
    public void setMangager( String manager )
    {
        XMLUtil.setStringValue( parent, "stRef:manager", manager );
    }
    
    /**
     * Get name of the variant of asset management system that manages this resource.
     * 
     * @return The name of a asset management system.
     */
    public String getManagerVariant()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerVariant" );
    }
    
    /**
     * Set the name of the variant of the system that manages this resource.
     * 
     * @param managerVariant The name of the management system.
     */
    public void setMangagerVariant( String managerVariant )
    {
        XMLUtil.setStringValue( parent, "stRef:managerVariant", managerVariant );
    }
    
    /**
     * URI identifying the managed resource.
     * 
     * @return The URI to resource.
     */
    public String getManagerTo()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerTo" );
    }
    
    /**
     * Set the URI to the managed resource.
     * 
     * @param managerTo The URI to the managed resource.
     */
    public void setMangagerTo( String managerTo )
    {
        XMLUtil.setStringValue( parent, "stRef:managerTo", managerTo );
    }
    
    /**
     * URI to info about the managed resource.
     * 
     * @return The URI to the resource info.
     */
    public String getManagerUI()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerUI" );
    }
    
    /**
     * Set the URI to the info about the managed resource.
     * 
     * @param managerUI The URI to the managed resource information.
     */
    public void setMangagerUI( String managerUI )
    {
        XMLUtil.setStringValue( parent, "stRef:managerUI", managerUI );
    }
    
}