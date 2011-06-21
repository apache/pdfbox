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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Define XMP properties that are common to all schemas.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class XMPSchemaBasic extends XMPSchema
{
    /**
     * The namespace of this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/xap/1.0/";
    
    /**
     * Construct a new blank PDF schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaBasic( XMPMetadata parent )
    {
        super( parent, "xmp", NAMESPACE );
        schema.setAttributeNS( 
            NS_NAMESPACE, 
            "xmlns:xapGImg", 
            "http://ns.adobe.com/xap/1.0/g/img/" );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaBasic( Element element, String prefix )
    {
        super( element, prefix );
        if( schema.getAttribute( "xmlns:xapGImg" ) == null )
        {
            schema.setAttributeNS( 
                NS_NAMESPACE, 
                "xmlns:xapGImg", 
                "http://ns.adobe.com/xap/1.0/g/img/" );
        }
    }
    
    /**
     * Remove an Advisory xpath expression.
     *
     * @param advisory An xpath expression specifying properties that
     * were edited outside of the authoring application.
     */
    public void removeAdvisory( String advisory )
    {
        removeBagValue( prefix + ":Advisory", advisory );
    }
    
    /**
     * Add an advisory to the list.
     *
     * @param advisory The new advisory xpath expression.
     */
    public void addAdvisory( String advisory )
    {
        addBagValue( prefix + ":Advisory", advisory );
    }
    
    /**
     * Get the complete list of advisories.
     *
     * @return The list of advisories.
     */
    public List<String> getAdvisories()
    {
        return getBagList( prefix + ":Advisory" );
    }
    
    /**
     * The base URL of the resource, for relative URLs in the document.
     *
     * @param url The base URL.
     */
    public void setBaseURL( String url )
    {
        setTextProperty( prefix + ":BaseURL", url );
    }
    
    /**
     * Get the base URL of the resource.
     *
     * @return The base URL.
     */
    public String getBaseURL()
    {
        return getTextProperty( prefix + ":BaseURL" );
    }
    
    /**
     * Set the creation date of the resource.
     *
     * @param date The creation date of the resource.
     */
    public void setCreateDate( Calendar date )
    {
        setDateProperty( prefix + ":CreateDate", date );
    }
    
    /**
     * Get the creation date of the resource.
     *
     * @return The creation date of the resource.
     * 
     * @throws IOException If there is an error while converting this property to
     * a date.
     */
    public Calendar getCreateDate() throws IOException
    {
        return getDateProperty( prefix + ":CreateDate" );
    }
    
    /**
     * The creator tool for the resource.  In the form of "vendor app version", ie
     * "Adobe Acrobat Distiller 5.0"
     *
     * @param creator The tool that was used to create the resource.
     */
    public void setCreatorTool( String creator )
    {
        setTextProperty( prefix + ":CreatorTool", creator );
    }
    
    /**
     * Get the tool that created this resource, in the form of "vendor app version", ie
     * "Adobe Acrobat Distiller 5.0".
     *
     * @return The creator tool.
     */
    public String getCreatorTool()
    {
        return getTextProperty( prefix + ":CreatorTool" );
    }
    
    /**
     * Remove an identifier to this resource.
     *
     * @param id An identifier to this resource.
     */
    public void removeIdentifier( String id )
    {
        removeBagValue( prefix + ":Identifier", id );
    }
    
    /**
     * Add a new identifier for this resource.
     *
     * @param id A new identifier for this resource.
     */
    public void addIdentifier( String id )
    {
        addBagValue( prefix + ":Identifier", id );
    }
    
    /**
     * Get the complete list of identifiers.
     *
     * @return The list of identifiers.
     */
    public List<String> getIdentifiers()
    {
        return getBagList( prefix + ":Identifier" );
    }
    
    /**
     * Set a short phrase that identifies this resource.
     *
     * @param label A short description of this resource.
     */
    public void setLabel( String label )
    {
        setTextProperty( prefix + ":Label", label );
    }
    
    /**
     * Get the short phrase that describes this resource.
     *
     * @return The label for this resource.
     */
    public String getLabel()
    {
        return getTextProperty( prefix + "p:Label" );
    }
    
    /**
     * Set a Title for this resource.
     *
     * @param title A title denoting this resource
     */
    public void setTitle( String title )
    {
        setTextProperty( prefix + ":Title", title);
    }
    
    /**
     * Get the title for this resource.
     *
     * @return The titled denoting this resource.
     */
    public String getTitle()
    {
        return getTextProperty( prefix + ":Title" );
    }
    
    /**
     * Set the date that any metadata was updated.
     *
     * @param date The metadata change date for this resource.
     */
    public void setMetadataDate( Calendar date )
    {
        setDateProperty( prefix + ":MetadataDate", date );
    }
    
    /**
     * Get the metadata change date for this resource.
     *
     * @return The metadata change date of the resource.
     * 
     * @throws IOException If there is an error while converting this property to
     * a date.
     */
    public Calendar getMetadataDate() throws IOException
    {
        return getDateProperty( prefix + ":MetadataDate" );
    }
    
    /**
     * Set the date that the resource was last modified.
     *
     * @param date The modify date for this resource.
     */
    public void setModifyDate( Calendar date )
    {
        setDateProperty( prefix + ":ModifyDate", date );
    }
    
    /**
     * Get the date the resource was last modified.
     *
     * @return The modify date of the resource.
     * 
     * @throws IOException If there is an error while converting this property to
     * a date.
     */
    public Calendar getModifyDate() throws IOException
    {
        return getDateProperty( prefix + ":ModifyDate" );
    }
    
    /**
     * Set a short informal name for the resource.
     *
     * @param nickname A short name of this resource.
     */
    public void setNickname( String nickname )
    {
        setTextProperty( prefix + ":Nickname", nickname );
    }
    
    /**
     * Get the short informal name for this resource.
     *
     * @return The short name for this resource.
     */
    public String getNickname()
    {
        return getTextProperty( prefix + ":Nickname" );
    }
    
    /**
     * Get a number that indicates the documents status.
     * 
     * @return The rating of the document.
     */
    public Integer getRating()
    {
        return getIntegerProperty( prefix + ":Rating" );
    }
    
    /**
     * Set the document status.
     * 
     * @param rating A number indicating status relative to other documents.
     */
    public void setRating( Integer rating )
    {
        setIntegerProperty( prefix + ":Rating", rating );
    }
    
    /**
     * Set the default value for the thumbnail.
     *
     * @param thumbnail The thumbnail of this resource.
     */
    public void setThumbnail( Thumbnail thumbnail )
    {
        setThumbnailProperty( prefix + ":Thumbnails", null, thumbnail );
    }
    
    /**
     * Get the default value for the thumbnail.
     *
     * @return The thumbnail of this resource.
     */
    public Thumbnail getThumbnail()
    {
        return getThumbnailProperty( prefix + ":Thumbnails", null );
    }
    
    /**
     * Set the thumbnail of this resource in a specific language.
     *
     * @param language The language code.
     * @param thumbnail The thumbnail in a specific language.
     */
    public void setThumbnail( String language, Thumbnail thumbnail )
    {
        setThumbnailProperty( prefix + ":Thumbnails", language, thumbnail );
    }
    
    /**
     * Get the thumbnail in a specific language.
     *
     * @param language The language code to get the description for.
     *
     * @return The thumbnail in the specified language or null if it does not exist.
     */
    public Thumbnail getThumbnail( String language )
    {
        return getThumbnailProperty( prefix + ":Thumbnails", language );
    }
    
    /**
     * Get a list of all languages that a thumbnail exists for.
     *
     * @return A non-null list of languages, potentially an empty list.
     */
    public List<String> getThumbnailLanguages()
    {
        return getLanguagePropertyLanguages( prefix + ":Thumbnails" );
    }
}