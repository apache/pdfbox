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

package org.apache.padaf.xmpbox.schema;

import java.util.Calendar;
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.MIMEType;
import org.apache.padaf.xmpbox.type.PropertyType;
import org.apache.padaf.xmpbox.type.TextType;


/**
 * Representation of a DublinCore Schema
 * 
 * @author a183132
 * 
 */
public class DublinCoreSchema extends XMPSchema {

	public static final String PREFERRED_DC_PREFIX = "dc";

	public static final String DCURI = "http://purl.org/dc/elements/1.1/";

	@PropertyType(propertyType = "bag Text")
	public static final String CONTRIBUTOR = "contributor";

	@PropertyType(propertyType = "Text")
	public static final String COVERAGE = "coverage";

	@PropertyType(propertyType = "seq Text")
	public static final String CREATOR = "creator";

	@PropertyType(propertyType = "seq Date")
	public static final String DATE = "date";

	@PropertyType(propertyType = "Lang Alt")
	public static final String DESCRIPTION = "description";

	@PropertyType(propertyType = "MIMEType")
	public static final String FORMAT = "format";

	@PropertyType(propertyType = "Text")
	public static final String IDENTIFIER = "identifier";

	@PropertyType(propertyType = "bag Text")
	public static final String LANGUAGE = "language";

	@PropertyType(propertyType = "bag Text")
	public static final String PUBLISHER = "publisher";

	@PropertyType(propertyType = "bag Text")
	public static final String RELATION = "relation";

	@PropertyType(propertyType = "Lang Alt")
	public static final String RIGHTS = "rights";

	@PropertyType(propertyType = "Text")
	public static final String SOURCE = "source";

	@PropertyType(propertyType = "bag Text")
	public static final String SUBJECT = "subject";

	@PropertyType(propertyType = "Lang Alt")
	public static final String TITLE = "title";

	@PropertyType(propertyType = "bag Text")
	public static final String TYPE = "type";

	/**
	 * Constructor of a Dublin Core schema with preferred prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public DublinCoreSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_DC_PREFIX, DCURI);
	}

	/**
	 * Constructor of a Dublin Core schema with specified prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param ownPrefix
	 *            The prefix to assign
	 */
	public DublinCoreSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, DCURI);
	}

	/**
	 * set contributor(s) to the resource (other than the authors)
	 * 
	 * @param properName
	 *            Value to set
	 */
	public void addContributor(String properName) {
		addQualifiedBagValue(CONTRIBUTOR, properName);
	}

	public void removeContributor (String properName) {
		removeUnqualifiedBagValue(CONTRIBUTOR, properName);
	}
	
	
	/**
	 * set the extent or scope of the resource
	 * 
	 * @param text
	 *            Value to set
	 */
	public void setCoverage(String text) {
		addProperty(new TextType(getMetadata(), null, getLocalPrefix(), COVERAGE, text));
	}

	/**
	 * set the extent or scope of the resource
	 * 
	 * @param text
	 *            Property to set
	 */
	public void setCoverageProperty(TextType text) {
		addProperty(text);
	}

	/**
	 * set the autor(s) of the resource
	 * 
	 * @param properName
	 *            Value to add
	 * @throws InappropriateTypeException
	 */
	public void addCreator(String properName) {
		addUnqualifiedSequenceValue(CREATOR, properName);
	}

	public void removeCreator (String name) {
		removeUnqualifiedSequenceValue(CREATOR, name);
	}
	
	/**
	 * Set date(s) that something interesting happened to the resource
	 * 
	 * @param date
	 *            Value to add
	 */
	public void addDate(Calendar date) {
		addUnqualifiedSequenceDateValue(DATE, date);
	}

	public void removeDate (Calendar date) {
		removeUnqualifiedSequenceDateValue(DATE, date);
	}
	
	/**
	 * add a textual description of the content of the resource (multiple values
	 * may be present for different languages)
	 * 
	 * @param lang
	 *            language concerned
	 * @param value
	 *            Value to add
	 */
	public void addDescription(String lang, String value) {
		setUnqualifiedLanguagePropertyValue(DESCRIPTION, lang, value);
	}

    /**
     * Set the default value for the description.
     *
     * @param value The description of this resource.
     */
    public void setDescription( String value )
    {
		addDescription(null, value);
    }
    

    /**
     * Convenience method for signature compatibility with jempbox
     *
     * @see DublinCoreSchema#addDescription(String, String)
     */
    @Deprecated
    public void setDescription( String language, String description )
    {
        addDescription(language, description );
    }
    

	
	/**
	 * set the file format used when saving the resource.
	 * 
	 * @param mimeType
	 *            Value to set
	 */
	public void setFormat(String mimeType) {
		addProperty(new TextType(getMetadata(), null, getLocalPrefix(), FORMAT, mimeType));
	}

	/**
	 * Set the unique identifier of the resource
	 * 
	 * @param text
	 *            Value to set
	 */
	public void setIdentifier(String text) {
		addProperty(new TextType(getMetadata(), null, getLocalPrefix(), IDENTIFIER, text));
	}

	/**
	 * Set the unique identifier of the resource
	 * 
	 * @param text
	 *            Property to set
	 */
	public void setIdentifierProperty(TextType text) {
		addProperty(text);
	}

	/**
	 * Add language(s) used in this resource
	 * 
	 * @param locale
	 *            Value to set
	 */
	public void addLanguage(String locale) {
		addQualifiedBagValue(LANGUAGE, locale);
	}
	
	public void removeLanguage (String locale) {
		removeUnqualifiedBagValue(LANGUAGE, locale);
	}

	/**
	 * add publisher(s)
	 * 
	 * @param properName
	 *            Value to add
	 */
	public void addPublisher(String properName) {
		addQualifiedBagValue(PUBLISHER, properName);
	}

	public void removePublisher (String name) {
		removeUnqualifiedBagValue(PUBLISHER, name);
	}
	
	/**
	 * Add relationships to other documents
	 * 
	 * @param text
	 *            Value to set
	 */
	public void addRelation(String text) {
		addQualifiedBagValue(RELATION, text);
	}

	public void removeRelation (String text) {
		removeUnqualifiedBagValue (RELATION, text);
	}
	
	/**
	 * add informal rights statement, by language.
	 * 
	 * @param lang
	 *            Language concerned
	 * @param value
	 *            Value to set
	 */
	public void addRights(String lang, String value) {
		setUnqualifiedLanguagePropertyValue(RIGHTS, lang, value);
	}

    /**
     * Convenience method for signature compatibility with jempbox
     *
     * @see DublinCoreSchema#addRights(String, String)
     */
	@Deprecated
    public void setRights( String language, String rights )
    {
        addRights(language, rights );
    }

    /**
     * Convenience method for signature compatibility with jempbox.
     * Add default rights
     *
     * @see DublinCoreSchema#addRights(String, String)
     */
	@Deprecated
    public void setRights( String rights )
    {
        addRights(null, rights );
    }

	
	/**
	 * Set the unique identifer of the work from which this resource was derived
	 * 
	 * @param text
	 *            Value to set
	 */
	public void setSource(String text) {
		addProperty(new TextType(getMetadata(), null, getLocalPrefix(), SOURCE, text));
	}

	/**
	 * Set the unique identifer of the work from which this resource was derived
	 * 
	 * @param text
	 *            Property to set
	 */
	public void setSourceProperty(TextType text) {
		addProperty(text);
	}

	/**
	 * Set the unique identifer of the work from which this resource was derived
	 * 
	 * @param text
	 *            Property to set
	 */
	public void setFormatProperty(MIMEType text) {
		addProperty(text);
	}

	/**
	 * add descriptive phrases or keywords that specify the topic of the content
	 * of the resource
	 * 
	 * @param text
	 *            Value to add
	 */
	public void addSubject(String text) {
		addQualifiedBagValue(SUBJECT, text);
	}

	public void removeSubject (String text) {
		removeUnqualifiedBagValue(SUBJECT, text);
	}
	
	/**
	 * set the title of the document, or the name given to the resource (by
	 * language)
	 * 
	 * @param lang
	 *            Language concerned
	 * @param value
	 *            Value to set
	 */
	public void setTitle(String lang, String value) {
		setUnqualifiedLanguagePropertyValue(TITLE, lang, value);
	}

	/**
	 * set default title
	 * @param lang
	 * @param value
	 */
	public void setTitle(String value) {
		setTitle(null, value);
	}

	/**
	 * set the title of the document, or the name given to the resource (by
	 * language)
	 * 
	 * @see DublinCoreSchema#setTitle(String)
	 * 
	 */
	public void addTitle(String lang, String value) {
		setTitle(lang,value);
	}

	/**
	 * set the document type (novel, poem, ...)
	 * 
	 * @param type
	 *            Value to set
	 */
	public void addType(String type) {
		addQualifiedBagValue(TYPE, type);
	}

	/**
	 * Return the Bag of contributor(s)
	 * 
	 * @return Contributor property
	 */
	public ArrayProperty getContributorsProperty() {
		return (ArrayProperty) getUnqualifiedProperty(CONTRIBUTOR);
	}

	/**
	 * Return a String list of contributor(s)
	 * 
	 * @return List of contributors values
	 */
	public List<String> getContributors() {
		return getUnqualifiedBagValueList(CONTRIBUTOR);

	}

	/**
	 * Return the Coverage TextType Property
	 * 
	 * @return Coverage property
	 */
	public TextType getCoverageProperty() {
		return (TextType) getUnqualifiedProperty(COVERAGE);
	}

	/**
	 * Return the value of the coverage
	 * 
	 * @return Coverage value
	 */
	public String getCoverage() {
		TextType tt = (TextType) getUnqualifiedProperty(COVERAGE);
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Return the Sequence of contributor(s)
	 * 
	 * @return Creator property
	 */
	public ArrayProperty getCreatorsProperty() {
		return (ArrayProperty) getUnqualifiedProperty(CREATOR);
	}

	/**
	 * Return the creator(s) string value
	 * 
	 * @return List of creators values
	 */
	public List<String> getCreators() {
		return getUnqualifiedSequenceValueList(CREATOR);
	}

	/**
	 * Return the sequence of date(s)
	 * 
	 * @return date property
	 */
	public ArrayProperty getDatesProperty() {
		return (ArrayProperty) getUnqualifiedProperty(DATE);
	}

	/**
	 * Return a calendar list of date
	 * 
	 * @return List of dates values
	 */
	public List<Calendar> getDates() {
		return getUnqualifiedSequenceDateValueList(DATE);
	}

	/**
	 * Return the Lang alt Description
	 * 
	 * @return Description property
	 */
	public ArrayProperty getDescriptionProperty() {
		return (ArrayProperty) getUnqualifiedProperty(DESCRIPTION);
	}

	/**
	 * Return a list of languages defined in description property
	 * 
	 * @return get List of languages defined for description property
	 */
	public List<String> getDescriptionLanguages() {
		return getUnqualifiedLanguagePropertyLanguagesValue(DESCRIPTION);
	}

	/**
	 * Return a language value for description property
	 * 
	 * @param lang
	 *            The language wanted
	 * @return Desription value for specified language
	 */
	public String getDescription(String lang) {
		return getUnqualifiedLanguagePropertyValue(DESCRIPTION, lang);
	}
	
    /**
     * Get the default value for the description.
     *
     * @return The description of this resource.
     */
    public String getDescription()
    {
        return getDescription( null );
    }
    


	/**
	 * Return the file format property
	 * 
	 * @return the format property
	 */
	public TextType getFormatProperty() {
		return (TextType) getUnqualifiedProperty(FORMAT);
	}

	/**
	 * return the file format value
	 * 
	 * @return the format value
	 */
	public String getFormat() {
		TextType tt = (TextType) getUnqualifiedProperty(FORMAT);
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Return the unique identifier property of this resource
	 * 
	 * @return the identifier property
	 */
	public TextType getIdentifierProperty() {
		return (TextType) getUnqualifiedProperty(IDENTIFIER);
	}

	/**
	 * return the unique identifier value of this resource
	 * 
	 * @return the unique identifier value
	 */
	public String getIdentifier() {
		TextType tt = (TextType) getUnqualifiedProperty(IDENTIFIER);
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Return the bag DC language
	 * 
	 * @return language property
	 */
	public ArrayProperty getLanguagesProperty() {
		return (ArrayProperty) getUnqualifiedProperty(LANGUAGE);
	}

	/**
	 * Return the list of values defined in the DC language
	 * 
	 * @return list of languages defined for language property
	 */
	public List<String> getLanguages() {
		return getUnqualifiedBagValueList(LANGUAGE);
	}

	/**
	 * Return the bag DC publisher
	 * 
	 * @return publisher property
	 */
	public ArrayProperty getPublishersProperty() {
		return (ArrayProperty) getUnqualifiedProperty(PUBLISHER);
	}

	/**
	 * Return the list of values defined in the DC publisher
	 * 
	 * @return list of values for publisher property
	 */
	public List<String> getPublishers() {
		return getUnqualifiedBagValueList(PUBLISHER);
	}

	/**
	 * Return the bag DC relation
	 * 
	 * @return relation property
	 */
	public ArrayProperty getRelationsProperty() {
		return (ArrayProperty) getUnqualifiedProperty(RELATION);
	}

	/**
	 * Return the list of values defined in the DC relation
	 * 
	 * @return list of values for relation property
	 */
	public List<String> getRelations() {
		return getUnqualifiedBagValueList(RELATION);
	}

	/**
	 * Convenience method for signature compatibility with jempbox
	 * 
	 * @see DublinCoreSchema#getRelations()
	 */
	@Deprecated
	public List<String> getRelationships() {
		return getRelations();
	}

	/**
	 * Return the Lang alt Rights
	 * 
	 * @return rights property
	 */
	public ArrayProperty getRightsProperty() {
		return (ArrayProperty) getUnqualifiedProperty(RIGHTS);
	}

	/**
	 * Return a list of languages defined in Right property
	 * 
	 * @return list of rights languages values defined
	 */
	public List<String> getRightsLanguages() {
		return getUnqualifiedLanguagePropertyLanguagesValue(RIGHTS);
	}

	/**
	 * Return a language value for Right property
	 * 
	 * @param lang
	 *            language concerned
	 * @return the rights value for specified language
	 */
	public String getRights(String lang) {
		return getUnqualifiedLanguagePropertyValue(RIGHTS, lang);
	}

	/**
	 * Return the default value for Right property
	 * 
	 * @see DublinCoreSchema#getRights(String)
	 */
	public String getRights() {
		return getRights(null);
	}

	
	/**
	 * Return the source property of this resource
	 * 
	 * @return source property
	 */
	public TextType getSourceProperty() {
		return (TextType) getUnqualifiedProperty(SOURCE);
	}

	/**
	 * return the source value of this resource
	 * 
	 * @return value of source property
	 */
	public String getSource() {
		TextType tt = (TextType) getUnqualifiedProperty(SOURCE);
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Return the bag DC Subject
	 * 
     * @return the subject property
	 */
	public ArrayProperty getSubjectsProperty() {
		return (ArrayProperty) getUnqualifiedProperty(SUBJECT);
	}

	/**
	 * Return the list of values defined in the DC Subject
	 * 
	 * @return the list of subject values
	 */
	public List<String> getSubjects() {
		return getUnqualifiedBagValueList(SUBJECT);
	}

	/**
	 * Return the Lang alt Title
	 * 
	 * @return the title property
	 */
	public ArrayProperty getTitleProperty() {
		return (ArrayProperty) getUnqualifiedProperty(TITLE);
	}

	/**
	 * Return a list of languages defined in Title property
	 * 
	 * @return list of languages defined for title property
	 */
	public List<String> getTitleLanguages() {
		return getUnqualifiedLanguagePropertyLanguagesValue(TITLE);
	}

	/**
	 * Return a language value for Title property
	 * 
	 * @param lang
	 *            the language concerned
	 * @return the title value for specified language
	 */
	public String getTitle(String lang) {
		return getUnqualifiedLanguagePropertyValue(TITLE, lang);
	}

	/**
	 * Get the default value for the title.
	 *
	 * @return The default title of this resource.
	 */
	public String getTitle()
	{
		return getTitle( null );
	}


	/**
	 * Return the bag DC Type
	 * 
	 * @return the type property
	 */
	public ArrayProperty getTypesProperty() {
		return (ArrayProperty) getUnqualifiedProperty(TYPE);
	}

	/**
	 * Return the list of values defined in the DC Type
	 * 
	 * @return the value of type property
	 */
	public List<String> getTypes() {
		return getUnqualifiedBagValueList(TYPE);
	}

	public void removeType (String type) {
		removeUnqualifiedBagValue(TYPE, type);
	}

}
