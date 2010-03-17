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

import java.util.List;

import org.w3c.dom.Element;

/**
 * Define XMP properties used with Adobe Photoshop documents.
 * 
 * @author $Author: benlitchfield $
 * @version $Revision: 1.2 $
 */
public class XMPSchemaPhotoshop extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/photoshop/1.0/";

    /**
     * Construct a new blank Photoshop schema.
     * 
     * @param parent
     *            The parent metadata schema that this will be part of.
     */
    public XMPSchemaPhotoshop(XMPMetadata parent)
    {
        super(parent, "photoshop", NAMESPACE);
    }

    /**
     * Constructor for existing XML element.
     * 
     * @param element The XML element.
     * @param aPrefix The XML prefix; photoshop.
     */
    public XMPSchemaPhotoshop(Element element, String aPrefix)
    {
        super(element, aPrefix);
    }

    /**
     * By-line title.
     * 
     * @param s The authors position.
     */
    public void setAuthorsPosition( String s )
    {
        setTextProperty(prefix + ":AuthorsPosition", s);
    }

    /**
     * By-line title.
     * 
     * @return The authors position.
     */
    public String getAuthorsPosition()
    {
        return getTextProperty(prefix + ":AuthorsPosition");
    }

    /**
     * Writer/editor.
     * 
     * @param s The caption writer.
     */
    public void setCaptionWriter( String s )
    {
        setTextProperty(prefix + ":CaptionWriter", s);
    }

    /**
     * Writer/editor.
     * 
     * @return The caption writer.
     */
    public String getCaptionWriter()
    {
        return getTextProperty(prefix + ":CaptionWriter");
    }

    /**
     * Category; limited to 3 7-bit characters.
     * @param s The category.
     */
    public void setCategory( String s )
    {
        if( s != null && s.length() > 3 )
        {
            throw new RuntimeException( "Error: photoshop:Category is limited to three characters value='" + s + "'" );
        }
        setTextProperty(prefix + ":Category", s);
    }

    /**
     * The category.
     * 
     * @return The category.
     */
    public String getCategory()
    {
        return getTextProperty(prefix + ":Category");
    }

    /**
     * The city.
     * 
     * @param s The city.
     */
    public void setCity( String s )
    {
        setTextProperty(prefix + ":City", s);
    }

    /**
     * The city.
     * 
     * @return The city.
     */
    public String getCity()
    {
        return getTextProperty(prefix + ":City");
    }

    /**
     * The country.
     * 
     * @param s The country.
     */
    public void setCountry( String s )
    {
        setTextProperty(prefix + ":Country", s);
    }

    /**
     * The country.
     * 
     * @return The country.
     */
    public String getCountry()
    {
        return getTextProperty(prefix + ":Country");
    }

    /**
     * Credit.
     * 
     * @param s The credit property.
     */
    public void setCredit( String s )
    {
        setTextProperty(prefix + ":Credit", s);
    }

    /**
     * The credit property.
     * 
     * @return The credit property.
     */
    public String getCredit()
    {
        return getTextProperty(prefix + ":Credit");
    }

    /**
     * Date created; creation date of the source document which may be
     * earlier than the digital representation.
     * 
     * @param s The date created.
     */
    public void setDateCreated( String s )
    {
        setTextProperty(prefix + ":DateCreated", s);
    }

    /**
     * Creation date.
     * 
     * @return The creation date.
     */
    public String getDateCreated()
    {
        return getTextProperty(prefix + ":DateCreated");
    }

    /**
     * The headline.
     * 
     * @param s The headline.
     */
    public void setHeadline( String s )
    {
        setTextProperty(prefix + ":Headline", s);
    }

    /**
     * Headline.
     * 
     * @return The headline.
     */
    public String getHeadline()
    {
        return getTextProperty(prefix + ":Headline");
    }

    /**
     * Instructions.
     * 
     * @param s The instructions.
     */
    public void setInstructions( String s )
    {
        setTextProperty(prefix + ":Instructions", s);
    }

    /**
     * The instructions.
     * 
     * @return The instructions.
     */
    public String getInstructions()
    {
        return getTextProperty(prefix + ":Instructions");
    }

    /**
     * The source.
     * 
     * @param s The source.
     */
    public void setSource( String s )
    {
        setTextProperty(prefix + ":Source", s);
    }

    /**
     * The source.
     * 
     * @return The source.
     */
    public String getSource()
    {
        return getTextProperty(prefix + ":Source");
    }

    /**
     * The state.
     * 
     * @param s The state.
     */
    public void setState( String s )
    {
        setTextProperty(prefix + ":State", s);
    }

    /**
     * The state.
     * 
     * @return The state.
     */
    public String getState()
    {
        return getTextProperty(prefix + ":State");
    }

    /**
     * Add a new supplemental category.
     * 
     * @param s The supplemental category.
     */
    public void addSupplementalCategory( String s )
    {
        addBagValue(prefix + ":SupplementalCategories", s);
    }

    /**
     * Get a list of all supplemental categories.
     * 
     * @return The supplemental categories.
     */
    public List<String> getSupplementalCategories()
    {
        return getBagList(prefix + ":SupplementalCategories");
    }

    /**
     * Remove a supplemental category.
     * 
     * @param s The supplemental category.
     */
    public void removeSupplementalCategory( String s )
    {
        removeBagValue(prefix + ":SupplementalCategories", s);
    }

    /**
     * The transmission reference.
     *  
     * @param s The transmission reference.
     */
    public void setTransmissionReference( String s )
    {
        setTextProperty(prefix + ":TransmissionReference", s);
    }

    /**
     * The transmission reference.
     * 
     * @return The transmission reference.
     */
    public String getTransmissionReference()
    {
        return getTextProperty(prefix + ":TransmissionReference");
    }

    /**
     * The urgency.
     * 
     * @param s The urgency.
     */
    public void setUrgency( Integer s )
    {
        if( s != null )
        {
            if( s.intValue() < 1 || s.intValue() > 8 )
            {
                throw new RuntimeException( "Error: photoshop:Urgency must be between 1 and 8.  value=" + s );
            }
        }
        setIntegerProperty(prefix + ":Urgency", s);
    }

    /**
     * The urgency.
     * 
     * @return The urgency.
     */
    public Integer getUrgency()
    {
        return getIntegerProperty(prefix + ":Urgency");
    }
}