/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox.schema;

import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.BooleanType;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.type.URLType;

/**
 * Representation of XMP Rights Management Schema
 * 
 * @author a183132
 * 
 */
@StructuredType(preferedPrefix = "xmpRights", namespace = "http://ns.adobe.com/xap/1.0/rights/")
public class XMPRightsManagementSchema extends XMPSchema
{

    @PropertyType(type = Types.URL, card = Cardinality.Simple)
    public static final String CERTIFICATE = "Certificate";

    @PropertyType(type = Types.Boolean, card = Cardinality.Simple)
    public static final String MARKED = "Marked";

    @PropertyType(type = Types.ProperName, card = Cardinality.Bag)
    public static final String OWNER = "Owner";

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String USAGETERMS = "UsageTerms";

    @PropertyType(type = Types.URL, card = Cardinality.Simple)
    public static final String WEBSTATEMENT = "WebStatement";

    /**
     * Constructor of XMPRightsManagement Schema with preferred prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     */
    public XMPRightsManagementSchema(final XMPMetadata metadata)
    {
        super(metadata);
    }

    /**
     * Constructor of XMPRightsManagement schema with specified prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     * @param ownPrefix
     *            The prefix to assign
     */
    public XMPRightsManagementSchema(final XMPMetadata metadata, final String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    /**
     * Add a legal owner for the described resource.
     * 
     * @param value
     *            value to add
     */
    public void addOwner(final String value)
    {
        addQualifiedBagValue(OWNER, value);
    }

    public void removeOwner(final String value)
    {
        removeUnqualifiedBagValue(OWNER, value);
    }

    /**
     * Return the Bag of owner(s)
     * 
     * @return owners property
     */
    public ArrayProperty getOwnersProperty()
    {
        return (ArrayProperty) getProperty(OWNER);
    }

    /**
     * Return a String list of owner(s)
     * 
     * @return list of defined owners
     */
    public List<String> getOwners()
    {
        return getUnqualifiedBagValueList(OWNER);
    }

    /**
     * Set Marked value
     * 
     * @param marked
     *            value to add
     */
    public void setMarked(final Boolean marked)
    {
        final BooleanType tt = (BooleanType) instanciateSimple(MARKED, marked ? BooleanType.TRUE : BooleanType.FALSE);
        setMarkedProperty(tt);
    }

    /**
     * Set Marked property
     * 
     * @param marked
     *            Marked property to set
     */
    public void setMarkedProperty(final BooleanType marked)
    {
        addProperty(marked);
    }

    /**
     * Get Marked property
     * 
     * @return Marked property
     */
    public BooleanType getMarkedProperty()
    {
        return (BooleanType) getProperty(MARKED);
    }

    /**
     * Get Marked value
     * 
     * @return marked value
     */
    public Boolean getMarked()
    {
        final BooleanType bt = ((BooleanType) getProperty(MARKED));
        return bt == null ? null : bt.getValue();
    }

    /**
     * Add an usageTerms value
     * 
     * @param lang
     *            concerned language
     * @param value
     *            value to set
     */
    public void addUsageTerms(final String lang, final String value)
    {
        setUnqualifiedLanguagePropertyValue(USAGETERMS, lang, value);
    }

    /**
     * Set the default usage terms for this resource.
     * 
     * @param terms
     *            The resource usage terms.
     */
    public void setUsageTerms(final String terms)
    {
        addUsageTerms(null, terms);
    }

    /**
     * Return the Lang alt UsageTerms
     * 
     * @return usageterms property
     */
    public ArrayProperty getUsageTermsProperty()
    {
        return (ArrayProperty) getProperty(USAGETERMS);
    }

    /**
     * Return a list of languages defined in the UsageTerms property
     * 
     * @return list of languages defined for the UsageTerms property or null if it doesn't exist.
     * @throws BadFieldValueException If the UsageTerms property is not a multi-lingual property.
     */
    public List<String> getUsageTermsLanguages() throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(USAGETERMS);
    }

    /**
     * Return a language value for the UsageTerms property
     * 
     * @param lang
     *            concerned language
     * @return value of specified language or null if it doesn't exist.
     * @throws BadFieldValueException If the UsageTerms property is not a multi-lingual property.
     */
    public String getUsageTerms(final String lang) throws BadFieldValueException
    {
        return getUnqualifiedLanguagePropertyValue(USAGETERMS, lang);
    }

    /**
     * Get the default usage terms for the document.
     * 
     * @return The terms for this resource or null if it doesn't exist.
     * @throws BadFieldValueException If the UsageTerms property is not a multi-lingual property.
     */
    public String getUsageTerms() throws BadFieldValueException
    {
        return getUsageTerms(null);
    }

    /**
     * Return the WebStatement URL as TextType.
     * 
     * @return Webstatement URL property
     */
    public TextType getWebStatementProperty()
    {
        return ((TextType) getProperty(WEBSTATEMENT));
    }

    /**
     * Return the WebStatement URL as String.
     * 
     * @return webStatement URL value
     */
    public String getWebStatement()
    {
        final TextType tt = ((TextType) getProperty(WEBSTATEMENT));
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Set the WebStatement url
     * 
     * @param url
     *            WebStatemen url value to set
     */
    public void setWebStatement(final String url)
    {
        final URLType tt = (URLType) instanciateSimple(WEBSTATEMENT, url);
        setWebStatementProperty(tt);
    }

    /**
     * Set the WebStatement url
     * 
     * @param url
     *            WebStatemen url property to set
     */
    public void setWebStatementProperty(final URLType url)
    {
        addProperty(url);
    }

    /**
     * Return the Certificate URL as TextType.
     * 
     * @return certificate url property
     */
    public TextType getCertificateProperty()
    {
        return ((TextType) getProperty(CERTIFICATE));
    }

    /**
     * Return the Certificate URL as String.
     * 
     * @return certificate URL value
     */
    public String getCertificate()
    {
        final TextType tt = ((TextType) getProperty(CERTIFICATE));
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Set the Certificate URL.
     * 
     * @param url
     *            certificate url value to set
     */
    public void setCertificate(final String url)
    {
        final URLType tt = (URLType) instanciateSimple(CERTIFICATE, url);
        setCertificateProperty(tt);
    }

    /**
     * Set the Certificate URL.
     * 
     * @param url
     *            certificate url property to set
     */
    public void setCertificateProperty(final URLType url)
    {
        addProperty(url);
    }
}
