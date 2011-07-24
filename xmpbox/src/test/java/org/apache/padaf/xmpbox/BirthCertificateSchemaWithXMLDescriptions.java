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

package org.apache.padaf.xmpbox;

import java.util.Calendar;
import java.util.Iterator;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PropertyExtensionDefinition;
import org.apache.padaf.xmpbox.schema.PropertyType;
import org.apache.padaf.xmpbox.schema.SchemaExtensionDefinition;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.TextType;


@SchemaExtensionDefinition(schema = "Birth-Certificate Schema", valueType_description = "/org/apache/padaf/xmpbox/valueTypeDescription.xml", property_descriptions = "propertiesDescription.xml")
public class BirthCertificateSchemaWithXMLDescriptions extends XMPSchema {

	public static final String PREFERED_PREFIX = "adn";

	public static final String NAMESPACE = "http://test.apache.com/xap/adn/";

	@PropertyType(propertyType = "Text")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String FIRST_NAME = "firstname";

	@PropertyType(propertyType = "seq Text")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String LAST_NAME = "lastname";

	@PropertyType(propertyType = "Text")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String BIRTH_PLACE = "birth-place";

	@PropertyType(propertyType = "Date")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String BIRTH_DATE = "birth-date";

	@PropertyType(propertyType = "Text")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String BIRTH_COUNTRY = "birth-country";

	@PropertyType(propertyType = "mailaddress")
	@PropertyExtensionDefinition(propertyCategory = "external")
	public static final String MAIL_ADR = "mail";

	public BirthCertificateSchemaWithXMLDescriptions(XMPMetadata metadata) {
		super(metadata, PREFERED_PREFIX, NAMESPACE);
		this.setAttribute(new Attribute(null, "xmlns", "madn",
				"http://test.withfield.com/vt/"));
		this.setAboutAsSimple("");
	}

	public void setFirstname(String fn) {
		this.setTextPropertyValueAsSimple(FIRST_NAME, fn);
	}

	public void addLastname(String ln) {
		this.addSequenceValueAsSimple(LAST_NAME, ln);
	}

	public void setBirthPlace(String city) {
		this.setTextPropertyValueAsSimple(BIRTH_PLACE, city);
	}

	public void setBirthCountry(String country) {
		this.setTextPropertyValueAsSimple(BIRTH_COUNTRY, country);
	}

	public void setBirthDate(Calendar date) {
		this.setDatePropertyValueAsSimple(BIRTH_DATE, date);
	}

	public String getFirstname() {
		return this.getTextProperty(localPrefixSep + FIRST_NAME)
				.getStringValue();
	}

	public String getLastname() {
		return this.getTextProperty(localPrefixSep + LAST_NAME)
				.getStringValue();
	}

	public void setMailaddr(String name, String domain) {
		ComplexPropertyContainer field = new ComplexPropertyContainer(metadata,
				localPrefix, MAIL_ADR);
		field.setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
		TextType namePart = new TextType(metadata, "madn", "name", name);
		TextType domainPart = new TextType(metadata, "madn", "domain", domain);
		field.addProperty(namePart);
		field.addProperty(domainPart);
		addProperty(field);

	}

	private ComplexPropertyContainer getMailField() {
		AbstractField afield = this.getPropertyAsSimple(MAIL_ADR);
		if (afield == null) {
			return null;
		}
		if (afield instanceof ComplexPropertyContainer) {
			return (ComplexPropertyContainer) afield;
		} else {
			throw new IllegalArgumentException(MAIL_ADR
					+ " property found but not seems to be a field");
		}
	}

	private TextType getTextType(String nameProp) {
		ComplexPropertyContainer field = getMailField();
		Iterator<AbstractField> it = field.getAllProperties().iterator();
		AbstractField aProp;
		while (it.hasNext()) {
			aProp = it.next();
			if (aProp.getPropertyName().equals(nameProp)) {
				if (aProp instanceof TextType) {
					return (TextType) aProp;
				} else {
					throw new IllegalArgumentException(
							nameProp
									+ " property found but not seems to be in expected type");
				}
			}
		}
		return null;
	}

	public String getMailName() {
		TextType name = getTextType("name");
		if (name != null) {
			return name.getStringValue();
		}
		return null;
	}

	public String getMailDomain() {
		TextType name = getTextType("domain");
		if (name != null) {
			return name.getStringValue();
		}
		return null;
	}
}
