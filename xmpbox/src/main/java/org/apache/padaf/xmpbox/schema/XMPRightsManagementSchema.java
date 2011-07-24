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

import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.TextType;


/**
 * Representation of XMP Rights Management Schema
 * 
 * @author a183132
 * 
 */
public class XMPRightsManagementSchema extends XMPSchema {
	public static final String PREFERRED_XMPRIGHTS_PREFIX = "xmpRights";

	public static final String XMPRIGHTSURI = "http://ns.adobe.com/xap/1.0/rights/";

	@PropertyType(propertyType = "URL")
	public static final String CERTIFICATE = "Certificate";

	@PropertyType(propertyType = "Boolean")
	public static final String MARKED = "Marked";

	@PropertyType(propertyType = "bag ProperName")
	public static final String OWNER = "Owner";

	@PropertyType(propertyType = "Lang Alt")
	public static final String USAGETERMS = "UsageTerms";

	@PropertyType(propertyType = "URL")
	public static final String WEBSTATEMENT = "WebStatement";

	/**
	 * Constructor of XMPRightsManagement Schema with preferred prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public XMPRightsManagementSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_XMPRIGHTS_PREFIX, XMPRIGHTSURI);
	}

	/**
	 * Constructor of XMPRightsManagement schema with specified prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param ownPrefix
	 *            The prefix to assign
	 */
	public XMPRightsManagementSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, XMPRIGHTSURI);
	}

	/**
	 * Add a legal owner for the described resource.
	 * 
	 * @param value
	 *            value to add
	 */
	public void addToOwnerValue(String value) {
		addBagValue(localPrefixSep + OWNER, value);
	}

	/**
	 * Return the Bag of owner(s)
	 * 
	 * @return owners property
	 */
	public ComplexProperty getOwner() {
		return (ComplexProperty) getProperty(localPrefixSep + OWNER);
	}

	/**
	 * Return a String list of owner(s)
	 * 
	 * @return list of defined owners
	 */
	public List<String> getOwnerValue() {
		return getBagValueList(localPrefixSep + OWNER);
	}

	/**
	 * Set Marked value
	 * 
	 * @param marked
	 *            value to add
	 */
	public void setMarkedValue(Boolean marked) {
		addProperty(new BooleanType(metadata, localPrefix, MARKED, marked));
	}

	/**
	 * Set Marked property
	 * 
	 * @param marked
	 *            Marked property to set
	 */
	public void setMarked(BooleanType marked) {
		addProperty(marked);
	}

	/**
	 * Get Marked property
	 * 
	 * @return Marked property
	 */
	public BooleanType getMarked() {
		return (BooleanType) getProperty(localPrefixSep + MARKED);
	}

	/**
	 * Get Marked value
	 * 
	 * @return marked value
	 */
	public Boolean getMarkedValue() {
		BooleanType bt = ((BooleanType) getProperty(localPrefixSep + MARKED));
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
	public void addToUsageTermsValue(String lang, String value) {
		setLanguagePropertyValue(localPrefixSep + USAGETERMS, lang, value);
	}

	/**
	 * Return the Lang alt UsageTerms
	 * 
	 * @return usageterms property
	 */
	public ComplexProperty getUsageTerms() {
		return (ComplexProperty) getProperty(localPrefixSep + USAGETERMS);
	}

	/**
	 * Return a list of languages defined in description property
	 * 
	 * @return list of languages defined for usageterms
	 */
	public List<String> getUsageTermsLanguages() {
		return getLanguagePropertyLanguagesValue(localPrefixSep + USAGETERMS);
	}

	/**
	 * Return a language value for description property
	 * 
	 * @param lang
	 *            concerned language
	 * @return value of specified language
	 */
	public String getUsageTermsValue(String lang) {
		return getLanguagePropertyValue(localPrefixSep + USAGETERMS, lang);
	}

	/**
	 * Return the WebStatement URL as TextType.
	 * 
	 * @return Webstatement URL property
	 */
	public TextType getWebStatement() {
		return ((TextType) getProperty(localPrefixSep + WEBSTATEMENT));
	}

	/**
	 * Return the WebStatement URL as String.
	 * 
	 * @return webStatement URL value
	 */
	public String getWebStatementValue() {
		TextType tt = ((TextType) getProperty(localPrefixSep + WEBSTATEMENT));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Set the WebStatement url
	 * 
	 * @param url
	 *            WebStatemen url value to set
	 */
	public void setWebStatementValue(String url) {
		addProperty(new TextType(metadata, localPrefix, WEBSTATEMENT, url));
	}

	/**
	 * Set the WebStatement url
	 * 
	 * @param url
	 *            WebStatemen url property to set
	 */
	public void setWebStatement(TextType url) {
		addProperty(url);
	}

	/**
	 * Return the Certificate URL as TextType.
	 * 
	 * @return certificate url property
	 */
	public TextType getCertificate() {
		return ((TextType) getProperty(localPrefixSep + CERTIFICATE));
	}

	/**
	 * Return the Certificate URL as String.
	 * 
	 * @return certificate URL value
	 */
	public String getCertificateValue() {
		TextType tt = ((TextType) getProperty(localPrefixSep + CERTIFICATE));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Set the Certificate URL.
	 * 
	 * @param url
	 *            certficate url value to set
	 */
	public void setCertificateValue(String url) {
		addProperty(new TextType(metadata, localPrefix, CERTIFICATE, url));
	}

	/**
	 * Set the Certificate URL.
	 * 
	 * @param url
	 *            certificate url property to set
	 */
	public void setCertificate(TextType url) {
		addProperty(url);
	}
}
