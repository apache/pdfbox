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

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.TextType;

/**
 * Representation of Adobe PDF Schema
 * 
 * @author a183132
 * 
 */
public class AdobePDFSchema extends XMPSchema {

	public static final String PREFERRED_PDF_PREFIX = "pdf";

	public static final String PDFURI = "http://ns.adobe.com/pdf/1.3/";

	@PropertyType(propertyType = "Text")
	public static final String KEYWORDS = "Keywords";

	@PropertyType(propertyType = "Text")
	public static final String PDF_VERSION = "PDFVersion";

	@PropertyType(propertyType = "Text")
	public static final String PRODUCER = "Producer";

	/**
	 * Constructor of an Adobe PDF schema with preferred prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public AdobePDFSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_PDF_PREFIX, PDFURI);
	}

	/**
	 * Constructor of an Adobe PDF schema with specified prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param ownPrefix
	 *            The prefix to assign
	 */
	public AdobePDFSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, PDFURI);
	}

	/**
	 * Set the PDF keywords
	 * 
	 * @param value
	 *            Value to set
	 */
	public void setKeywordsValue(String value) {
		TextType keywords;
		keywords = new TextType(metadata, localPrefix, KEYWORDS, value);
		addProperty(keywords);
	}

	/**
	 * Set the PDF keywords
	 * 
	 * @param keywords
	 *            Property to set
	 */
	public void setKeywords(TextType keywords) {
		addProperty(keywords);
	}

	/**
	 * Set the PDFVersion
	 * 
	 * @param value
	 *            Value to set
	 */
	public void setPDFVersionValue(String value) {
		TextType version;
		version = new TextType(metadata, localPrefix, PDF_VERSION, value);
		addProperty(version);

	}

	/**
	 * Set the PDFVersion
	 * 
	 * @param version
	 *            Property to set
	 */
	public void setPDFVersion(TextType version) {
		addProperty(version);
	}

	/**
	 * Set the PDFProducer
	 * 
	 * @param value
	 *            Value to set
	 */
	public void setProducerValue(String value) {
		TextType producer;
		producer = new TextType(metadata, localPrefix, PRODUCER, value);
		addProperty(producer);
	}

	/**
	 * Set the PDFProducer
	 * 
	 * @param producer
	 *            Property to set
	 */
	public void setProducer(TextType producer) {
		addProperty(producer);
	}

	/**
	 * Give the PDF Keywords property
	 * 
	 * @return The property object
	 */
	public TextType getKeywords() {
		AbstractField tmp = getProperty(localPrefixSep + KEYWORDS);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the PDF Keywords property value (string)
	 * 
	 * @return The property value
	 */
	public String getKeywordsValue() {
		AbstractField tmp = getProperty(localPrefixSep + KEYWORDS);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return ((TextType) tmp).getStringValue();
			}
		}
		return null;
	}

	/**
	 * Give the PDFVersion property
	 * 
	 * @return The property object
	 */
	public TextType getPDFVersion() {
		AbstractField tmp = getProperty(localPrefixSep + PDF_VERSION);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the PDFVersion property value (string)
	 * 
	 * @return The property value
	 */
	public String getPDFVersionValue() {
		AbstractField tmp = getProperty(localPrefixSep + PDF_VERSION);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return ((TextType) tmp).getStringValue();
			}
		}
		return null;
	}

	/**
	 * Give the producer property
	 * 
	 * @return The property object
	 */
	public TextType getProducer() {
		AbstractField tmp = getProperty(localPrefixSep + PRODUCER);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the producer property value (string)
	 * 
	 * @return The property value
	 */
	public String getProducerValue() {
		AbstractField tmp = getProperty(localPrefixSep + PRODUCER);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return ((TextType) tmp).getStringValue();
			}
		}
		return null;
	}

}
