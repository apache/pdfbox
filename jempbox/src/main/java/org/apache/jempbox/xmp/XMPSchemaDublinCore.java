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
 * Define XMP properties used with the Dublin Core specification.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class XMPSchemaDublinCore extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://purl.org/dc/elements/1.1/";
    /**
     * Construct a new blank Dublin Core schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaDublinCore( XMPMetadata parent )
    {
        super( parent, "dc", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaDublinCore( Element element, String prefix )
    {
        super( element, prefix );
    }
    
    /**
     * Remove a contributor from the list of contributors.
     *
     * @param contributor The contributor to remove.
     */
    public void removeContributor( String contributor )
    {
        removeBagValue( prefix + ":contributor", contributor );
    }
    
    /**
     * Add a contributor to the list of contributors.  A contributor is someone other than an author.
     *
     * @param contributor The name of the contributor.
     */
    public void addContributor( String contributor )
    {
        addBagValue( prefix + ":contributor", contributor );
    }
    
    /**
     * Get the complete list of contributors.
     *
     * @return The list of contributors.
     */
    public List<String> getContributors()
    {
        return getBagList( prefix + ":contributor" );
    }
    
    /**
     * Set the coverage property.
     *
     * @param coverage The extend or scope of the resource.
     */
    public void setCoverage( String coverage )
    {
        setTextProperty( prefix + ":coverage", coverage );
    }
    
    /**
     * Get the coverage property.
     *
     * @return The extent or scope of the resource.
     */
    public String getCoverage()
    {
        return getTextProperty( prefix + ":coverage" );
    }
    
    /**
     * Remove a creator from the list of creators.
     *
     * @param creator The author of the resource.
     */
    public void removeCreator( String creator )
    {
        removeSequenceValue( prefix + ":creator", creator );
    }
    
    /**
     * Add a creator.
     *
     * @param creator The author of the resource.
     */
    public void addCreator( String creator )
    {
        addSequenceValue( prefix + ":creator", creator );
    }
    
    /**
     * Get a complete list of creators.
     *
     * @return A list of java.lang.String objects.
     */
    public List<String> getCreators()
    {
        return getSequenceList( prefix + ":creator" );
    }
    
    /**
     * Remove a date from the list of 'interesting' dates.
     *
     * @param date The date to remove.
     */
    public void removeDate( Calendar date )
    {
        removeSequenceDateValue( prefix + ":date", date );
    }
    
    /**
     * Add a date of interest to this schema.
     *
     * @param date The date to add to the schema.
     */
    public void addDate( Calendar date )
    {
        addSequenceDateValue( prefix + ":date", date );
    }
    
    /**
     * Get a list of all dates of interest to this resource.
     *
     * @return A list of java.util.Calendar objects.
     *
     * @throws IOException If there is an error creating the date object.
     */
    public List<Calendar> getDates() throws IOException
    {
        return getSequenceDateList( prefix + ":date" );
    }
    
    /**
     * Set the default value for the description.
     *
     * @param description The description of this resource.
     */
    public void setDescription( String description )
    {
        setLanguageProperty( prefix + ":description", null, description );
    }
    
    /**
     * Get the default value for the description.
     *
     * @return The description of this resource.
     */
    public String getDescription()
    {
        return getLanguageProperty( prefix + ":description", null );
    }
    
    /**
     * Set the description of this resource in a specific language.
     *
     * @param language The language code.
     * @param description The description in a specific language.
     */
    public void setDescription( String language, String description )
    {
        setLanguageProperty( prefix + ":description", language, description );
    }
    
    /**
     * Get the description in a specific language.
     *
     * @param language The language code to get the description for.
     *
     * @return The description in the specified language or null if it does not exist.
     */
    public String getDescription( String language )
    {
        return getLanguageProperty( prefix + ":description", language );
    }
    
    /**
     * Get a list of all languages that a description exists for.
     *
     * @return A non-null list of languages, potentially an empty list.
     */
    public List<String> getDescriptionLanguages()
    {
        return getLanguagePropertyLanguages( prefix + ":description" );
    }
    
    /**
     * Set the format property.
     *
     * @param format The mime-type of the saved resource.
     */
    public void setFormat( String format )
    {
        setTextProperty( prefix + ":format", format );
    }
    
    /**
     * Get the format property.
     *
     * @return The mime-type of the resource.
     */
    public String getFormat()
    {
        return getTextProperty( prefix + ":format" );
    }
    
    /**
     * Set the resource identifier.
     *
     * @param id An id to the resource.
     */
    public void setIdentifier( String id )
    {
        setTextProperty( prefix + ":identifier", id );
    }
    
    /**
     * Get the resource id.
     *
     * @return A key that identifies this resource.
     */
    public String getIdentifier()
    {
        return getTextProperty( prefix + ":identifier" );
    }
    
    /**
     * Remove a language from the list of languages.
     *
     * @param language The language to remove.
     */
    public void removeLanguage( String language )
    {
        removeBagValue( prefix + ":language", language );
    }
    
    /**
     * Add a language to the list of languages.  
     *
     * @param language The name of the language.
     */
    public void addLanguage( String language )
    {
        addBagValue( prefix + ":language", language );
    }
    
    /**
     * Get the complete list of languages.
     *
     * @return The list of languages.
     */
    public List<String> getLanguages()
    {
        return getBagList( prefix + ":language" );
    }
    
    /**
     * Remove a publisher from the list of publishers.
     *
     * @param publisher The publisher to remove.
     */
    public void removePublisher( String publisher )
    {
        removeBagValue( prefix + ":publisher", publisher );
    }
    
    /**
     * Add a publisher to the list of publishers.  
     *
     * @param publisher The name of the publisher.
     */
    public void addPublisher( String publisher )
    {
        addBagValue( prefix + ":publisher", publisher );
    }
    
    /**
     * Get the complete list of publishers.
     *
     * @return The list of publishers.
     */
    public List<String> getPublishers()
    {
        return getBagList( prefix + ":publisher" );
    }
    
    /**
     * Remove a relation from the list of relationships.  
     * A relationship to another resource.
     *
     * @param relation The publisher to remove.
     */
    public void removeRelation( String relation )
    {
        removeBagValue( prefix + ":relation", relation );
    }
    
    /**
     * Add a relation to the list of relationships.
     * A relationship to another resource.  
     *
     * @param relation The relation to the other resource.
     */
    public void addRelation( String relation )
    {
        addBagValue( prefix + ":relation", relation );
    }
    
    /**
     * Get the complete list of relationships.
     *
     * @return The list of relationships.
     */
    public List<String> getRelationships()
    {
        return getBagList( prefix + ":relation" );
    }
    
    /**
     * Set the default value for the rights of this document.  This property
     * specifies informal rights of the document.
     *
     * @param rights The rights for this resource.
     */
    public void setRights( String rights )
    {
        setLanguageProperty( prefix + ":rights", null, rights );
    }
    
    /**
     * Get the default value for the rights of this document.
     *
     * @return The informal rights for this resource.
     */
    public String getRights()
    {
        return getLanguageProperty( prefix + ":rights", null );
    }
    
    /**
     * Set the rights for this resource in a specific language.
     *
     * @param language The language code.
     * @param rights The rights in a specific language.
     */
    public void setRights( String language, String rights )
    {
        setLanguageProperty( prefix + ":rights", language, rights );
    }
    
    /**
     * Get the rights in a specific language.
     *
     * @param language The language code to get the description for.
     *
     * @return The rights in the specified language or null if it does not exist.
     */
    public String getRights( String language )
    {
        return getLanguageProperty( prefix + ":rights", language );
    }
    
    /**
     * Get a list of all languages that a rights description exists for.
     *
     * @return A non-null list of languages, potentially an empty list.
     */
    public List<String> getRightsLanguages()
    {
        return getLanguagePropertyLanguages( prefix + ":rights" );
    }
    
    /**
     * Set the resource source identifier.
     *
     * @param id An id to the resource source.
     */
    public void setSource( String id )
    {
        setTextProperty( prefix + ":source", id );
    }
    
    /**
     * Get the resource source id.
     *
     * @return A key that identifies this source of this resource.
     */
    public String getSource()
    {
        return getTextProperty( prefix + ":source" );
    }
    
    /**
     * Remove a subject from the list of subjects.
     *
     * @param subject The subject to remove.
     */
    public void removeSubject( String subject )
    {
        removeBagValue( prefix + ":subject", subject );
    }
    
    /**
     * Add a subject to the list of subjects.
     *
     * @param subject The subject of this resource.
     */
    public void addSubject( String subject )
    {
        addBagValue( prefix + ":subject", subject );
    }
    
    /**
     * Get the complete list of subjects.
     *
     * @return The list of subjects.
     */
    public List<String> getSubjects()
    {
        return getBagList( prefix + ":subject" );
    }
    
    /**
     * Set the default value for the title.
     *
     * @param title The title of this resource.
     */
    public void setTitle( String title )
    {
        setLanguageProperty( prefix + ":title", null, title );
    }
    
    /**
     * Get the default value for the title.
     *
     * @return The title of this resource.
     */
    public String getTitle()
    {
        return getLanguageProperty( prefix + ":title", null );
    }
    
    /**
     * Set the title of this resource in a specific language.
     *
     * @param language The language code.
     * @param title The title in a specific language.
     */
    public void setTitle( String language, String title )
    {
        setLanguageProperty( prefix + ":title", language, title );
    }
    
    /**
     * Get the title in a specific language.
     *
     * @param language The language code to get the description for.
     *
     * @return The title in the specified language or null if it does not exist.
     */
    public String getTitle( String language )
    {
        return getLanguageProperty( prefix + ":title", language );
    }
    
    /**
     * Get a list of all languages that a title exists for.
     *
     * @return A non-null list of languages, potentially an empty list.
     */
    public List<String> getTitleLanguages()
    {
        return getLanguagePropertyLanguages( prefix + ":title" );
    }
    
    /**
     * Add a type to the bag of types of this resource.
     *
     * @param type The type of resource to add (poem, novel).
     */
    public void addType( String type )
    {
        addBagValue(prefix + ":type", type );
    }
    
    /**
     * Get the list of types for this resource.
     *
     * @return A list of types for this resource.
     */
    public List<String> getTypes()
    {
        return getBagList(prefix + ":type" );
    }
}