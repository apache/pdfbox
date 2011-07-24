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
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.apache.padaf.xmpbox.BuildPDFAExtensionSchemaDescriptionException;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.PropertyDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Manage XML property description file Allow user to create XML property
 * description description file or retrieve List of PropertyDescription from an
 * XML File If file data are specified in class schema representation, building
 * of Description schema (which must included in PDF/A Extension) will use these
 * information.
 * 
 * @author a183132
 * 
 */
public class XMLPropertiesDescriptionManager {

	protected List<PropertyDescription> propDescs;
	protected XStream xstream;

	/**
	 * Create new XMLPropertiesDescriptionManager
	 */
	public XMLPropertiesDescriptionManager() {
		propDescs = new ArrayList<PropertyDescription>();
		xstream = new XStream(new DomDriver());
		xstream.alias("PropertiesDescriptions", List.class);
		xstream.alias("PropertyDescription", PropertyDescription.class);
	}

	/**
	 * Add a description for the named property
	 * 
	 * @param name
	 *            Name of property
	 * @param description
	 *            Description which will be used
	 */
	public void addPropertyDescription(String name, String description) {
		propDescs.add(new PropertyDescription(name, description));
	}

	/**
	 * Save as XML data, all information defined before to the OutputStream
	 * 
	 * @param os
	 *            The stream where write data
	 */
	public void toXML(OutputStream os) {
		xstream.toXML(propDescs, os);
	}

	/**
	 * Load Properties Description from a well-formed XML File
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
			// resource not found
			throw new BuildPDFAExtensionSchemaDescriptionException(
					"Failed to find specified XML properties descriptions resource");
		}
		loadListFromXML(fis);

	}

	/**
	 * Load Properties Description from XML Stream
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
					if (((List<?>) o).get(0) instanceof PropertyDescription) {
						propDescs = (List<PropertyDescription>) o;
					} else {
						throw new BuildPDFAExtensionSchemaDescriptionException(
								"Failed to get correct properties descriptions from specified XML stream");
					}
				} else {
					throw new BuildPDFAExtensionSchemaDescriptionException(
							"Failed to find a properties description into the specified XML stream");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildPDFAExtensionSchemaDescriptionException(
					"Failed to get correct properties descriptions from specified XML stream",
					e.getCause());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Get all Properties Descriptions defined
	 * 
	 * @return List of PropertyDescription
	 */
	public List<PropertyDescription> getPropertiesDescriptionList() {
		return propDescs;
	}

	/**
	 * Sample of using to write/read information
	 * 
	 * @param args
	 *            Not used
	 * @throws BuildPDFAExtensionSchemaDescriptionException
	 *             When errors during building/reading xml file
	 */
	public static void main(String[] args)
			throws BuildPDFAExtensionSchemaDescriptionException {
		XMLPropertiesDescriptionManager ptMaker = new XMLPropertiesDescriptionManager();

		// add Descriptions
		for (int i = 0; i < 3; i++) {
			ptMaker.addPropertyDescription("name" + i, "description" + i);

		}

		// Display XML conversion
		System.out.println("Display XML Result:");
		ptMaker.toXML(System.out);

		// Sample to show how to build object from XML file
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ptMaker.toXML(bos);
		IOUtils.closeQuietly(bos);

		// emulate a new reading
		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		ptMaker = new XMLPropertiesDescriptionManager();
		ptMaker.loadListFromXML(is);
		List<PropertyDescription> result = ptMaker
				.getPropertiesDescriptionList();
		System.out.println();
		System.out.println();
		System.out.println("Result of XML Loading :");
		for (PropertyDescription propertyDescription : result) {
			System.out.println(propertyDescription.getPropertyName() + " :"
					+ propertyDescription.getDescription());
		}
	}

}
