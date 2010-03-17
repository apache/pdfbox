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
 * Define XMP properties that are related to rights management.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class XMPSchemaRightsManagement extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/xap/1.0/rights/";

    /**
     * Construct a new blank PDF schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaRightsManagement(XMPMetadata parent)
    {
        super(parent, "xmpRights", NAMESPACE);
    }

    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaRightsManagement(Element element, String prefix)
    {
        super(element, prefix);
    }

    /**
     * The online rights management certificate.
     *
     * @param certificate The URL to the rights cert.
     */
    public void setCertificateURL( String certificate )
    {
        setTextProperty("xmpRights:Certificate", certificate);
    }

    /**
     * Get the URL of the rights managment certificate.
     *
     * @return The rights management certificate URL.
     */
    public String getCertificateURL()
    {
        return getTextProperty(prefix + ":Certificate");
    }

    /**
     * Flag indicating if this is a rights managed resource.
     *
     * @param marked The marked value.
     */
    public void setMarked( Boolean marked )
    {
        setBooleanProperty(prefix + ":Marked", marked);
    }

    /**
     * Get the flag that indicates if this is a marked resource..
     *
     * @return The value of the marked flag.
     */
    public Boolean getMarked()
    {
        Boolean b = getBooleanProperty(prefix + ":Marked");
        return b != null ? b : Boolean.FALSE;
    }

    /**
     * Remove an owner from the list.
     *
     * @param owner The owner to remove.
     */
    public void removeOwner( String owner )
    {
        removeBagValue(prefix + ":Owner", owner);
    }

    /**
     * Add an owner to the list.
     *
     * @param owner A new legal owner to this resource.
     */
    public void addOwner( String owner )
    {
        addBagValue(prefix + ":Owner", owner);
    }

    /**
     * Get the complete list of legal owners.
     *
     * @return The list of owners.
     */
    public List<String> getOwners()
    {
        return getBagList(prefix + ":Owner");
    }

    /**
     * Set the default usage terms for this resource.
     *
     * @param terms The resource usage terms. 
     */
    public void setUsageTerms( String terms )
    {
        setLanguageProperty(prefix + ":UsageTerms", null, terms);
    }

    /**
     * Get the default usage terms for the document.
     *
     * @return The terms for this resource.
     */
    public String getUsageTerms()
    {
        return getLanguageProperty(prefix + ":UsageTerms", null);
    }

    /**
     * Set the usage terms of this resource in a specific language.
     *
     * @param language The language code.
     * @param terms The terms of this resource.
     */
    public void setDescription( String language, String terms )
    {
        setLanguageProperty(prefix + ":UsageTerms", language, terms);
    }

    /**
     * Get the usage terms in a specific language.
     *
     * @param language The language code to get the description for.
     *
     * @return The usage terms in the specified language or null if it does not exist.
     */
    public String getUsageTerms( String language )
    {
        return getLanguageProperty(prefix + ":UsageTerms", language);
    }

    /**
     * Get a list of all languages that a usage term exists for.
     *
     * @return A non-null list of languages, potentially an empty list.
     */
    public List<String> getUsageTermsLanguages()
    {
        return getLanguagePropertyLanguages(prefix + ":UsageTerms");
    }

    /**
     * Set the external link that describes the owners/rights of this resource.
     *
     * @param webStatement The URL to a terms site.
     */
    public void setWebStatement( String webStatement )
    {
        setTextProperty(prefix + ":WebStatement", webStatement);
    }

    /**
     * Get the URL that describes the terms of this resource.
     *
     * @return The usage rights URL.
     */
    public String getWebStatement()
    {
        return getTextProperty(prefix + ":WebStatement");
    }

    /**
     * Set the copyright information.
     *
     * @param copyright The copyright information.
     */
    public void setCopyright( String copyright )
    {
        setTextProperty(prefix + ":Copyright", copyright);
    }

    /**
     * Get the copyright information.
     *
     * @return The copyright information.
     */
    public String getCopyright()
    {
        return getTextProperty(prefix + ":Copyright");
    }
}