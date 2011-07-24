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

package org.apache.padaf.xmpbox.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.apache.padaf.xmpbox.BuildPDFAExtensionSchemaDescriptionException;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.FieldDescription;
import org.apache.padaf.xmpbox.type.ValueTypeDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Manage XML valuetype description file Allow user to create XML valuetype
 * description file or retrieve List of ValueTypeDescription from an XML File
 * 
 * If file data are specified in class schema representation, building of
 * Description schema (which must included in PDF/A Extension) will use these
 * information.
 * 
 * @author a183132
 * 
 */
public class XMLValueTypeDescriptionManager {

	protected List<ValueTypeDescription> vTypes;
	protected XStream xstream;

	/**
	 * Create a new XMLValueTypeDescriptionManager
	 */
	public XMLValueTypeDescriptionManager() {
		vTypes = new ArrayList<ValueTypeDescription>();
		xstream = new XStream(new DomDriver());
		xstream.alias("ValueTypesDescriptions", List.class);
		xstream.alias("ValueTypeDescription", ValueTypeDescription.class);
		xstream.alias("FieldDescription", FieldDescription.class);
	}

	/**
	 * Add a new Value Type description without any fields
	 * 
	 * @param type
	 *            Type of the Value Type
	 * @param nsURI
	 *            namespace URI of the Value Type
	 * @param prefix
	 *            prefix of the Value Type
	 * @param description
	 *            Description of the Value Type
	 */
	public void addValueTypeDescription(String type, String nsURI,
			String prefix, String description) {
		vTypes.add(new ValueTypeDescription(type, nsURI, prefix, description));
	}

	/**
	 * /** Add a new Value Type description with fields
	 * 
	 * @param type
	 *            Type of the Value Type
	 * @param nsURI
	 *            namespace URI of the Value Type
	 * @param prefix
	 *            prefix of the Value Type
	 * @param description
	 *            Description of the Value Type
	 * @param fields
	 *            List of FieldDescription
	 */
	public void addValueTypeDescription(String type, String nsURI,
			String prefix, String description, List<FieldDescription> fields) {
		vTypes.add(new ValueTypeDescription(type, nsURI, prefix, description,
				fields));
	}

	/**
	 * Save as XML data, all information defined before to the OutputStream
	 * 
	 * @param os
	 *            The stream where write data
	 */
	public void toXML(OutputStream os) {
		xstream.toXML(vTypes, os);
	}

	/**
	 * Load Value Types Descriptions from a well-formed XML File
	 * 
	 * @param classSchem
	 *            Description Schema where properties description are used
	 * @param path
	 *            Relative Path (file loading search in same class Schema
	 *            representation folder)
	 * @throws BuildPDFAExtensionSchemaDescriptionException
	 *             When problems to get or treat data in XML description file
	 */
	public void loadListFromXML(Class<? extends XMPSchema> classSchem,
			String path) throws BuildPDFAExtensionSchemaDescriptionException {
		InputStream fis = classSchem.getResourceAsStream(path);
		if (fis == null) {
			throw new BuildPDFAExtensionSchemaDescriptionException(
					"Failed to find specified XML valuetypes descriptions File");
		}
		loadListFromXML(fis);
	}

	/**
	 * Get all Value Types Descriptions defined
	 * 
	 * @return List of ValueTypeDescription
	 */
	public List<ValueTypeDescription> getValueTypesDescriptionList() {
		return vTypes;
	}

	/**
	 * Load Value Types Descriptions from XML Stream
	 * 
	 * @param is
	 *            Stream where read data
	 * @throws BuildPDFAExtensionSchemaDescriptionException
	 *             When problems to get or treat data in XML description file
	 */
	public void loadListFromXML(InputStream is)
			throws BuildPDFAExtensionSchemaDescriptionException {
		try {
			Object o = xstream.fromXML(is);
			if (o instanceof List<?>) {
				if (((List<?>) o).get(0) != null) {
					if (((List<?>) o).get(0) instanceof ValueTypeDescription) {
						vTypes = (List<ValueTypeDescription>) o;
					} else {
						throw new BuildPDFAExtensionSchemaDescriptionException(
								"Failed to get correct valuetypes descriptions from specified XML stream");
					}
				} else {
					throw new BuildPDFAExtensionSchemaDescriptionException(
							"Failed to find a valuetype description into the specified XML stream");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildPDFAExtensionSchemaDescriptionException(
					"Failed to get correct valuetypes descriptions from specified XML stream",
					e.getCause());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Sample of using to write/read information
	 * 
	 * @param args
	 *            not used
	 * @throws BuildPDFAExtensionSchemaDescriptionException
	 *             When errors during building/reading xml file
	 */
	public static void main(String[] args)
			throws BuildPDFAExtensionSchemaDescriptionException {
		XMLValueTypeDescriptionManager vtMaker = new XMLValueTypeDescriptionManager();

		// add Descriptions
		for (int i = 0; i < 3; i++) {
			vtMaker.addValueTypeDescription("testType" + i, "nsURI" + i,
					"prefix" + i, "description" + i);

		}
		List<FieldDescription> fieldSample = new ArrayList<FieldDescription>();
		for (int i = 0; i < 2; i++) {
			fieldSample.add(new FieldDescription("fieldName" + i, "valueType"
					+ i, "description" + i));
		}
		vtMaker.addValueTypeDescription("testTypeField",
				"http://test.withfield.com/vt/", "prefTest",
				" value type description", fieldSample);

		// Display XML conversion
		System.out.println("Display XML Result:");
		vtMaker.toXML(System.out);

		// Sample to show how to build object from XML file
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		vtMaker.toXML(bos);
		IOUtils.closeQuietly(bos);

		// emulate a new reading
		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		vtMaker = new XMLValueTypeDescriptionManager();
		vtMaker.loadListFromXML(is);
		List<ValueTypeDescription> result = vtMaker
				.getValueTypesDescriptionList();
		System.out.println();
		System.out.println();
		System.out.println("Result of XML Loading :");
		for (ValueTypeDescription propertyDescription : result) {
			System.out.println(propertyDescription.getType() + " :"
					+ propertyDescription.getDescription());
			if (propertyDescription.getFields() != null) {
				Iterator<FieldDescription> fit = propertyDescription
						.getFields().iterator();
				FieldDescription field;
				while (fit.hasNext()) {
					field = fit.next();
					System.out.println("Field " + field.getName() + " :"
							+ field.getValueType());
				}
			}
		}

	}

}
