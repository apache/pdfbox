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

import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.ArrayProperty;
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
    public XMPRightsManagementSchema(XMPMetadata metadata)
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
    public XMPRightsManagementSchema(XMPMetadata metadata, String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    /**
     * Add a legal owner for the described resource.
     * 
     * @param value
     *            value to add
     */
    public void addOwner(String value)
    {
        addQualifiedBagValue(OWNER, value);
    }

    public void removeOwner(String value)
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
    public void setMarked(Boolean marked)
    {
        BooleanType tt = (BooleanType) instanciateSimple(MARKED, marked ? BooleanType.TRUE : BooleanType.FALSE);
        setMarkedProperty(tt);
    }

    /**
     * Set Marked property
     * 
     * @param marked
     *            Marked property to set
     */
    public void setMarkedProperty(BooleanType marked)
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
        BooleanType bt = ((BooleanType) getProperty(MARKED));
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
    public void addUsageTerms(String lang, String value)
    {
        setUnqualifiedLanguagePropertyValue(USAGETERMS, lang, value);
    }

    /**
     * Set the default usage terms for this resource.
     * 
     * @param terms
     *            The resource usage terms.
     */
    public void setUsageTerms(String terms)
    {
        addUsageTerms(null, terms);
    }

    /**
     * Convenience method for jempbox signature compatibility
     * 
     * @see XMPRightsManagementSchema#addUsageTerms(String, String)
     */
    @Deprecated
    public void setDescription(String language, String terms)
    {
        addUsageTerms(language, terms);
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
     * Return a list of languages defined in description property
     * 
     * @return list of languages defined for usageterms
     */
    public List<String> getUsageTermsLanguages()
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(USAGETERMS);
    }

    /**
     * Return a language value for description property
     * 
     * @param lang
     *            concerned language
     * @return value of specified language
     */
    public String getUsageTerms(String lang)
    {
        return getUnqualifiedLanguagePropertyValue(USAGETERMS, lang);
    }

    /**
     * Get the default usage terms for the document.
     * 
     * @return The terms for this resource.
     */
    public String getUsageTerms()
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
        TextType tt = ((TextType) getProperty(WEBSTATEMENT));
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Set the WebStatement url
     * 
     * @param url
     *            WebStatemen url value to set
     */
    public void setWebStatement(String url)
    {
        URLType tt = (URLType) instanciateSimple(WEBSTATEMENT, url);
        setWebStatementProperty(tt);
    }

    /**
     * Set the WebStatement url
     * 
     * @param url
     *            WebStatemen url property to set
     */
    public void setWebStatementProperty(URLType url)
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
        TextType tt = ((TextType) getProperty(CERTIFICATE));
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Convenience method for jempbox signature compatibility
     * 
     * @see XMPRightsManagementSchema#getCertificate()
     */
    @Deprecated
    public String getCopyright()
    {
        return getCertificate();
    }

    /**
     * Convenience method for jempbox signature compatibility
     * 
     * @see XMPRightsManagementSchema#getCertificate()
     */
    @Deprecated
    public String getCertificateURL()
    {
        return getCertificate();
    }

    /**
     * Set the Certificate URL.
     * 
     * @param url
     *            certficate url value to set
     */
    public void setCertificate(String url)
    {
        URLType tt = (URLType) instanciateSimple(CERTIFICATE, url);
        setCertificateProperty(tt);
    }

    /**
     * Convenience method for jempbox signature compatibility
     * 
     * @see XMPRightsManagementSchema#setCertificate(String)
     */
    @Deprecated
    public void setCertificateURL(String certificate)
    {
        setCertificate(certificate);
    }

    /**
     * Convenience method for jempbox signature compatibility
     * 
     * @see XMPRightsManagementSchema#setCertificate(String)
     */
    @Deprecated
    public void setCopyright(String certificate)
    {
        setCertificate(certificate);
    }

    /**
     * Set the Certificate URL.
     * 
     * @param url
     *            certificate url property to set
     */
    public void setCertificateProperty(URLType url)
    {
        addProperty(url);
    }
}
