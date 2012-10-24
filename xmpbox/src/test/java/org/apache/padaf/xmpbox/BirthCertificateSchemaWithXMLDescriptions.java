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

import javax.xml.XMLConstants;

import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.PropertyType;
import org.apache.padaf.xmpbox.type.StructuredType;


@StructuredType(preferedPrefix="adn",namespace="http://test.apache.com/xap/adn/")
public class BirthCertificateSchemaWithXMLDescriptions extends XMPSchema {

	@PropertyType(propertyType = "Text")
	public static final String FIRST_NAME = "firstname";

	@PropertyType(propertyType = "seq Text")
	public static final String LAST_NAME = "lastname";

	@PropertyType(propertyType = "Text")
	public static final String BIRTH_PLACE = "birth-place";

	@PropertyType(propertyType = "Date")
	public static final String BIRTH_DATE = "birth-date";

	@PropertyType(propertyType = "Text")
	public static final String BIRTH_COUNTRY = "birth-country";

	public BirthCertificateSchemaWithXMLDescriptions(XMPMetadata metadata) {
		super(metadata);
		this.setAttribute(new Attribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "madn",
				"http://test.withfield.com/vt/"));
		this.setAboutAsSimple("");
	}

	public void setFirstname(String fn) {
		this.setTextPropertyValueAsSimple(FIRST_NAME, fn);
	}

	public void addLastname(String ln) {
		this.addUnqualifiedSequenceValue(LAST_NAME, ln);
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
		return this.getUnqualifiedTextProperty(FIRST_NAME)
				.getStringValue();
	}

	public String getLastname() {
		return this.getUnqualifiedTextProperty(LAST_NAME)
				.getStringValue();
	}


}
