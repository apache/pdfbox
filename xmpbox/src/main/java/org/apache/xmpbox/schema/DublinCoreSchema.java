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

package org.apache.xmpbox.schema;

import java.util.Calendar;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.MIMEType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;

/**
 * Representation of a DublinCore Schema
 * 
 * @author a183132
 * 
 */
@StructuredType(preferedPrefix = "dc", namespace = "http://purl.org/dc/elements/1.1/")
public class DublinCoreSchema extends XMPSchema
{

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String CONTRIBUTOR = "contributor";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String COVERAGE = "coverage";

    @PropertyType(type = Types.Text, card = Cardinality.Seq)
    public static final String CREATOR = "creator";

    @PropertyType(type = Types.Date, card = Cardinality.Seq)
    public static final String DATE = "date";

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String DESCRIPTION = "description";

    @PropertyType(type = Types.MIMEType, card = Cardinality.Simple)
    public static final String FORMAT = "format";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String IDENTIFIER = "identifier";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String LANGUAGE = "language";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String PUBLISHER = "publisher";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String RELATION = "relation";

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String RIGHTS = "rights";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String SOURCE = "source";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String SUBJECT = "subject";

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String TITLE = "title";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String TYPE = "type";

    /**
     * Constructor of a Dublin Core schema with preferred prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     */
    public DublinCoreSchema(final XMPMetadata metadata)
    {
        super(metadata);
    }

    /**
     * Constructor of a Dublin Core schema with specified prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     * @param ownPrefix
     *            The prefix to assign
     */
    public DublinCoreSchema(final XMPMetadata metadata, final String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    /**
     * set contributor(s) to the resource (other than the authors)
     * 
     * @param properName
     *            Value to set
     */
    public void addContributor(final String properName)
    {
        addQualifiedBagValue(CONTRIBUTOR, properName);
    }

    public void removeContributor(final String properName)
    {
        removeUnqualifiedBagValue(CONTRIBUTOR, properName);
    }

    /**
     * set the extent or scope of the resource
     * 
     * @param text
     *            Value to set
     */
    public void setCoverage(final String text)
    {
        addProperty(createTextType(COVERAGE, text));
    }

    /**
     * set the extent or scope of the resource
     * 
     * @param text
     *            Property to set
     */
    public void setCoverageProperty(final TextType text)
    {
        addProperty(text);
    }

    /**
     * set the author(s) of the resource
     *
     * @param properName Value to add
     */
    public void addCreator(final String properName)
    {
        addUnqualifiedSequenceValue(CREATOR, properName);
    }

    public void removeCreator(final String name)
    {
        removeUnqualifiedSequenceValue(CREATOR, name);
    }

    /**
     * Set date(s) that something interesting happened to the resource
     * 
     * @param date
     *            Value to add
     */
    public void addDate(final Calendar date)
    {
        addUnqualifiedSequenceDateValue(DATE, date);
    }

    public void removeDate(final Calendar date)
    {
        removeUnqualifiedSequenceDateValue(DATE, date);
    }

    /**
     * add a textual description of the content of the resource (multiple values may be present for different languages)
     * 
     * @param lang
     *            language concerned
     * @param value
     *            Value to add
     */
    public void addDescription(final String lang, final String value)
    {
        setUnqualifiedLanguagePropertyValue(DESCRIPTION, lang, value);
    }

    /**
     * Set the default value for the description.
     * 
     * @param value
     *            The description of this resource.
     */
    public void setDescription(final String value)
    {
        addDescription(null, value);
    }

    /**
     * set the file format used when saving the resource.
     * 
     * @param mimeType
     *            Value to set
     */
    public void setFormat(final String mimeType)
    {
        addProperty(createTextType(FORMAT, mimeType));
    }

    /**
     * Set the unique identifier of the resource
     * 
     * @param text
     *            Value to set
     */
    public void setIdentifier(final String text)
    {
        addProperty(createTextType(IDENTIFIER, text));
    }

    /**
     * Set the unique identifier of the resource
     * 
     * @param text
     *            Property to set
     */
    public void setIdentifierProperty(final TextType text)
    {
        addProperty(text);
    }

    /**
     * Add language(s) used in this resource
     * 
     * @param locale
     *            Value to set
     */
    public void addLanguage(final String locale)
    {
        addQualifiedBagValue(LANGUAGE, locale);
    }

    public void removeLanguage(final String locale)
    {
        removeUnqualifiedBagValue(LANGUAGE, locale);
    }

    /**
     * add publisher(s)
     * 
     * @param properName
     *            Value to add
     */
    public void addPublisher(final String properName)
    {
        addQualifiedBagValue(PUBLISHER, properName);
    }

    public void removePublisher(final String name)
    {
        removeUnqualifiedBagValue(PUBLISHER, name);
    }

    /**
     * Add relationships to other documents
     * 
     * @param text
     *            Value to set
     */
    public void addRelation(final String text)
    {
        addQualifiedBagValue(RELATION, text);
    }

    public void removeRelation(final String text)
    {
        removeUnqualifiedBagValue(RELATION, text);
    }

    /**
     * add informal rights statement, by language.
     * 
     * @param lang
     *            Language concerned
     * @param value
     *            Value to set
     */
    public void addRights(final String lang, final String value)
    {
        setUnqualifiedLanguagePropertyValue(RIGHTS, lang, value);
    }

    /**
     * Set the unique identifier of the work from which this resource was derived
     * 
     * @param text
     *            Value to set
     */
    public void setSource(final String text)
    {
        addProperty(createTextType(SOURCE, text));
    }

    /**
     * Set the unique identifier of the work from which this resource was derived
     * 
     * @param text
     *            Property to set
     */
    public void setSourceProperty(final TextType text)
    {
        addProperty(text);
    }

    /**
     * Set the unique identifier of the work from which this resource was derived
     * 
     * @param text
     *            Property to set
     */
    public void setFormatProperty(final MIMEType text)
    {
        addProperty(text);
    }

    /**
     * add descriptive phrases or keywords that specify the topic of the content of the resource
     * 
     * @param text
     *            Value to add
     */
    public void addSubject(final String text)
    {
        addQualifiedBagValue(SUBJECT, text);
    }

    public void removeSubject(final String text)
    {
        removeUnqualifiedBagValue(SUBJECT, text);
    }

    /**
     * set the title of the document, or the name given to the resource (by language)
     * 
     * @param lang
     *            Language concerned
     * @param value
     *            Value to set
     */
    public void setTitle(final String lang, final String value)
    {
        setUnqualifiedLanguagePropertyValue(TITLE, lang, value);
    }

    /**
     * set default title
     * 
     * @param value Value to set
     */
    public void setTitle(final String value)
    {
        setTitle(null, value);
    }

    /**
     * set the title of the document, or the name given to the resource (by language)
     * 
     * @param lang Language concerned
     * @param value Value to set
     * @see DublinCoreSchema#setTitle(String)
     * 
     */
    public void addTitle(final String lang, final String value)
    {
        setTitle(lang, value);
    }

    /**
     * set the document type (novel, poem, ...)
     * 
     * @param type
     *            Value to set
     */
    public void addType(final String type)
    {
        addQualifiedBagValue(TYPE, type);
    }

    /**
     * Return the Bag of contributor(s)
     * 
     * @return Contributor property
     */
    public ArrayProperty getContributorsProperty()
    {
        return (ArrayProperty) getProperty(CONTRIBUTOR);
    }

    /**
     * Return a String list of contributor(s)
     * 
     * @return List of contributors values
     */
    public List<String> getContributors()
    {
        return getUnqualifiedBagValueList(CONTRIBUTOR);

    }

    /**
     * Return the Coverage TextType Property
     * 
     * @return Coverage property
     */
    public TextType getCoverageProperty()
    {
        return (TextType) getProperty(COVERAGE);
    }

    /**
     * Return the value of the coverage
     * 
     * @return Coverage value
     */
    public String getCoverage()
    {
        final TextType tt = (TextType) getProperty(COVERAGE);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Return the Sequence of contributor(s)
     * 
     * @return Creator property
     */
    public ArrayProperty getCreatorsProperty()
    {
        return (ArrayProperty) getProperty(CREATOR);
    }

    /**
     * Return the creator(s) string value
     * 
     * @return List of creators values
     */
    public List<String> getCreators()
    {
        return getUnqualifiedSequenceValueList(CREATOR);
    }

    /**
     * Return the sequence of date(s)
     * 
     * @return date property
     */
    public ArrayProperty getDatesProperty()
    {
        return (ArrayProperty) getProperty(DATE);
    }

    /**
     * Return a calendar list of date
     * 
     * @return List of dates values
     */
    public List<Calendar> getDates()
    {
        return getUnqualifiedSequenceDateValueList(DATE);
    }

    /**
     * Return the Lang alt Description
     * 
     * @return Description property
     */
    public ArrayProperty getDescriptionProperty()
    {
        return (ArrayProperty) getProperty(DESCRIPTION);
    }

    /**
     * Return a list of languages defined in the description property.
     *
     * @return The List of languages defined for the description property or null if it doesn't
     * exist.
     * @throws BadFieldValueException If the description property is not a multi-lingual property.
     */
    public List<String> getDescriptionLanguages() throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(DESCRIPTION);
    }

    /**
     * Return a language value for the description property.
     *
     * @param lang The language wanted.
     * @return Description value for specified language or null if it doesn't exist.
     * @throws BadFieldValueException BadFieldValueException If the description property is not a
     * multi-lingual property.
     */
    public String getDescription(final String lang) throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyValue(DESCRIPTION, lang);
    }

    /**
     * Get the default value for the description property.
     * 
     * @return The description of this resource.
     * @throws BadFieldValueException If the description property is not a multi-lingual property.
     */
    public String getDescription() throws BadFieldValueException
    {
        return getDescription(null);
    }

    /**
     * Return the file format property
     * 
     * @return the format property
     */
    public TextType getFormatProperty()
    {
        return (TextType) getProperty(FORMAT);
    }

    /**
     * return the file format value
     * 
     * @return the format value
     */
    public String getFormat()
    {
        final TextType tt = (TextType) getProperty(FORMAT);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Return the unique identifier property of this resource
     * 
     * @return the identifier property
     */
    public TextType getIdentifierProperty()
    {
        return (TextType) getProperty(IDENTIFIER);
    }

    /**
     * return the unique identifier value of this resource
     * 
     * @return the unique identifier value
     */
    public String getIdentifier()
    {
        final TextType tt = (TextType) getProperty(IDENTIFIER);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Return the bag DC language
     * 
     * @return language property
     */
    public ArrayProperty getLanguagesProperty()
    {
        return (ArrayProperty) getProperty(LANGUAGE);
    }

    /**
     * Return the list of values defined in the DC language
     * 
     * @return list of languages defined for language property
     */
    public List<String> getLanguages()
    {
        return getUnqualifiedBagValueList(LANGUAGE);
    }

    /**
     * Return the bag DC publisher
     * 
     * @return publisher property
     */
    public ArrayProperty getPublishersProperty()
    {
        return (ArrayProperty) getProperty(PUBLISHER);
    }

    /**
     * Return the list of values defined in the DC publisher
     * 
     * @return list of values for publisher property
     */
    public List<String> getPublishers()
    {
        return getUnqualifiedBagValueList(PUBLISHER);
    }

    /**
     * Return the bag DC relation
     * 
     * @return relation property
     */
    public ArrayProperty getRelationsProperty()
    {
        return (ArrayProperty) getProperty(RELATION);
    }

    /**
     * Return the list of values defined in the DC relation
     * 
     * @return list of values for relation property
     */
    public List<String> getRelations()
    {
        return getUnqualifiedBagValueList(RELATION);
    }

    /**
     * Return the Lang alt Rights
     * 
     * @return rights property
     */
    public ArrayProperty getRightsProperty()
    {
        return (ArrayProperty) getProperty(RIGHTS);
    }

    /**
     * Return a list of languages defined in Right property.
     * 
     * @return list of rights languages values defined
     * @throws BadFieldValueException If the Rights property is not a multi-lingual property. 
     */
    public List<String> getRightsLanguages() throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(RIGHTS);
    }

    /**
     * Return a language value for the Rights property.
     *
     * @param lang language concerned.
     * @return The rights value for specified language or null if it doesn't exist.
     * @throws BadFieldValueException If the Rights property is not a multi-lingual property.
     */
    public String getRights(final String lang) throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyValue(RIGHTS, lang);
    }

    /**
     * Return the default value for the Rights property.
     *
     * @throws BadFieldValueException If the Rights property is not a multi-lingual property. 
     * @see DublinCoreSchema#getRights(String)
     * @return the default rights value
     */
    public String getRights() throws BadFieldValueException
    {
        return getRights(null);
    }

    /**
     * Return the source property of this resource
     * 
     * @return source property
     */
    public TextType getSourceProperty()
    {
        return (TextType) getProperty(SOURCE);
    }

    /**
     * return the source value of this resource
     * 
     * @return value of source property
     */
    public String getSource()
    {
        final TextType tt = (TextType) getProperty(SOURCE);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Return the bag DC Subject
     * 
     * @return the subject property
     */
    public ArrayProperty getSubjectsProperty()
    {
        return (ArrayProperty) getProperty(SUBJECT);
    }

    /**
     * Return the list of values defined in the DC Subject
     * 
     * @return the list of subject values
     */
    public List<String> getSubjects()
    {
        return getUnqualifiedBagValueList(SUBJECT);
    }

    /**
     * Return the Lang alt Title
     * 
     * @return the title property
     */
    public ArrayProperty getTitleProperty()
    {
        return (ArrayProperty) getProperty(TITLE);
    }

    /**
     * Return a list of languages defined in the Title property
     *
     * @return list of languages defined for title property
     * @throws BadFieldValueException If the Title property is not a multi-lingual property.
     */
    public List<String> getTitleLanguages() throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(TITLE);
    }

    /**
     * Return a language value for the Title property
     *
     * @param lang the language concerned
     * @return the title value for specified language
     * @throws BadFieldValueException If the Title property is not a multi-lingual property.
     */
    public String getTitle(final String lang) throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyValue(TITLE, lang);
    }

    /**
     * Get the default value for the title.
     *
     * @return The default title of this resource.
     * @throws BadFieldValueException If the Title property is not a multi-lingual property.
     */
    public String getTitle() throws BadFieldValueException
    {
        return getTitle(null);
    }

    /**
     * Return the bag DC Type
     * 
     * @return the type property
     */
    public ArrayProperty getTypesProperty()
    {
        return (ArrayProperty) getProperty(TYPE);
    }

    /**
     * Return the list of values defined in the DC Type
     * 
     * @return the value of type property
     */
    public List<String> getTypes()
    {
        return getUnqualifiedBagValueList(TYPE);
    }

    public void removeType(final String type)
    {
        removeUnqualifiedBagValue(TYPE, type);
    }

}
