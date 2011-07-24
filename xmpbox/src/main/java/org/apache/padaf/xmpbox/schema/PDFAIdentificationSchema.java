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
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.TextType;


/**
 * Representation of PDF/A Identification Schema
 * 
 * @author a183132
 * 
 */
public class PDFAIdentificationSchema extends XMPSchema {

	public static final String IDPREFIX = "pdfaid";
	public static final String IDPREFIXSEP = "pdfaid:";
	public static final String IDURI = "http://www.aiim.org/pdfa/ns/id/";

	@PropertyType(propertyType = "Integer")
	public static final String PART = "part";

	@PropertyType(propertyType = "Text")
	public static final String AMD = "amd";

	@PropertyType(propertyType = "Text")
	public static final String CONFORMANCE = "conformance";

	/*
	 * <rdf:Description rdf:about=""
	 * xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
	 * <pdfaid:conformance>B</pdfaid:conformance> <pdfaid:part>1</pdfaid:part>
	 * </rdf:Description>
	 */

	/**
	 * Constructor of a PDF/A Identification schema
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public PDFAIdentificationSchema(XMPMetadata metadata) {
		super(metadata, IDPREFIX, IDURI);
	}

	public PDFAIdentificationSchema(XMPMetadata metadata, String prefix) {
		super(metadata, prefix, IDURI);
	}

	/**
	 * Set the PDFA Version identifier (with string)
	 * 
	 * @param value
	 *            The version Id value to set
	 * 
	 */
	public void setPartValueWithString(String value) {
		IntegerType part = new IntegerType(metadata, IDPREFIX, PART, value);
		addProperty(part);
	}

	/**
	 * Set the PDFA Version identifier (with an int)
	 * 
	 * @param value
	 *            The version Id value to set
	 */
	public void setPartValueWithInt(int value) {
		IntegerType part = new IntegerType(metadata, IDPREFIX, PART, value);
		addProperty(part);
	}

	/**
	 * Set the PDF/A Version identifier (with an int)
	 * 
	 * @param value
	 *            The version Id property to set
	 */
	public void setPartValue(Integer value) {
		IntegerType part = new IntegerType(metadata, IDPREFIX, PART, value);
		addProperty(part);
	}

	/**
	 * Set the PDF/A Version identifier
	 * 
	 * @param part
	 *            set the PDF/A Version id property
	 */
	public void setPart(IntegerType part) {
		addProperty(part);
	}

	/**
	 * Set the PDF/A amendment identifier
	 * 
	 * @param value
	 *            The amendment identifier value to set
	 */
	public void setAmdValue(String value) {
		TextType amd = new TextType(metadata, IDPREFIX, AMD, value);
		addProperty(amd);
	}

	/**
	 * Set the PDF/A amendment identifier
	 * 
	 * @param amd
	 *            The amendment identifier property to set
	 */
	public void setAmd(TextType amd) {
		addProperty(amd);
	}

	/**
	 * Set the PDF/A conformance level
	 * 
	 * @param value
	 *            The conformance level value to set
	 * @throws BadFieldValueException
	 *             If Conformance Value not 'A' or 'B'
	 */
	public void setConformanceValue(String value) throws BadFieldValueException {
		if (value.equals("A") || value.equals("B")) {
			TextType conf = new TextType(metadata, IDPREFIX, CONFORMANCE, value);
			addProperty(conf);

		} else {
			throw new BadFieldValueException(
					"The property given not seems to be a PDF/A conformance level (must be A or B)");
		}
	}

	/**
	 * Set the PDF/A conformance level
	 * 
	 * @param conf
	 *            The conformance level property to set
	 * @throws BadFieldValueException
	 *             If Conformance Value not 'A' or 'B'
	 */
	public void setConformance(TextType conf) throws BadFieldValueException {
		String value = conf.getStringValue();
		if (value.equals("A") || value.equals("B")) {
			addProperty(conf);
		} else {
			throw new BadFieldValueException(
					"The property given not seems to be a PDF/A conformance level (must be A or B)");
		}
	}

	/**
	 * Give the PDFAVersionId (as an integer)
	 * 
	 * @return Part value (Integer)
	 */
	public Integer getPartValue() {
		AbstractField tmp = getPart();
		if (tmp != null) {
			if (tmp instanceof IntegerType) {
				return ((IntegerType) tmp).getValue();
			}
			return null;
		} else {
			for (Attribute attribute : getAllAttributes()) {
				if (attribute.getQualifiedName().equals(IDPREFIXSEP + PART)) {
					return new Integer(attribute.getValue());
				}
			}
			return null;
			
		}
	}

	/**
	 * Give the property corresponding to the PDFA Version id
	 * 
	 * @return Part property
	 */
	public IntegerType getPart() {
		AbstractField tmp = getProperty(IDPREFIXSEP + PART);
		if (tmp != null) {
			if (tmp instanceof IntegerType) {
				return (IntegerType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the PDFAAmendmentId (as an String)
	 * 
	 * @return Amendment value
	 */
	public String getAmendmentValue() {
		AbstractField tmp = getProperty(IDPREFIXSEP + AMD);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return ((TextType) tmp).getStringValue();
			}
		}
		return null;
	}

	/**
	 * Give the property corresponding to the PDFA Amendment id
	 * 
	 * @return Amendment property
	 */
	public TextType getAmd() {
		AbstractField tmp = getProperty(IDPREFIXSEP + AMD);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the PDFA Amendment Id (as an String)
	 * 
	 * @return Amendment Value
	 */
	public String getAmdValue() {
		TextType tmp = getAmd();
		if (tmp==null) {
			for (Attribute attribute : getAllAttributes()) {
				if (attribute.getQualifiedName().equals(IDPREFIXSEP + AMD)) {
					return attribute.getValue();
				}
			}
			return null;
		} else {
			return tmp.getStringValue();
		}
	}

	/**
	 * Give the property corresponding to the PDFA Conformance id
	 * 
	 * @return conformance property
	 */
	public TextType getConformance() {
		AbstractField tmp = getProperty(IDPREFIXSEP + CONFORMANCE);
		if (tmp != null) {
			if (tmp instanceof TextType) {
				return (TextType) tmp;
			}
		}
		return null;
	}

	/**
	 * Give the Conformance id
	 * 
	 * @return conformance id value
	 */
	public String getConformanceValue() {
		TextType tt = getConformance();
		if (tt==null) {
			for (Attribute attribute : getAllAttributes()) {
				if (attribute.getQualifiedName().equals(IDPREFIXSEP + CONFORMANCE)) {
					return attribute.getValue();
				}
			}
			return null;
		} else {
			return tt.getStringValue();
		}
	}

}
