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

import org.apache.padaf.xmpbox.schema.XMPSchema;


/**
 * 
 * This class help you to build a PDF/A extension schema description thanks to
 * annotation present in annotations included in the schema class representation
 * 
 * @author a183132
 * 
 */
public class BuildPDFExtensionSchemaHelper {

	// TODO re impl?menter
	
	/**
	 * According to check-style, Utility classes should not have a public or
	 * default constructor.
	 */
	protected BuildPDFExtensionSchemaHelper() {
	};

	/**
	 * Build automatically Schema description with annotations and data present
	 * in schema class and include it in the PDF/A Extension Schema NOTE : If
	 * metadata given not contains a PDF/A Extension Schema, this function add
	 * one automatically.
	 * 
	 * @param metadata
	 *            The concerned metadata in which include Schema Description
	 *            inside the PDF/A Extension Schema
	 * @param schema
	 *            The specific schema concerned by the description building
	 * @throws BuildPDFAExtensionSchemaDescriptionException
	 *             When failed to load or treat data from XML Descriptions files
	 *             or in Schema Class
	 */
	public static void includePDFAExtensionDefinition(XMPMetadata metadata,
			XMPSchema schema)
			throws BuildPDFAExtensionSchemaDescriptionException {
	}


	/**
	 * An helper to build a formated error message for schema Description errors
	 * 
	 * @param classSchem
	 *            Name of schema where errors are occured
	 * @param details
	 *            Error details
	 * @return Exception with formated error message
	 */
	protected static BuildPDFAExtensionSchemaDescriptionException schemaDescriptionError(
			String classSchem, String details) {
		StringBuilder sb = new StringBuilder(80);
		sb
				.append(
						"Error while building PDF/A Extension Schema description for '")
				.append(classSchem).append("' schema : ").append(details);
		return new BuildPDFAExtensionSchemaDescriptionException(sb.toString());
	}

	/**
	 * An helper to build a formated error message for properties Description
	 * errors
	 * 
	 * @param classSchem
	 *            Name of schema which include the bad property description
	 * @param propName
	 *            Name of property where a problem has occured
	 * @param details
	 *            Error details
	 * @param e
	 *            Original thrown Exception
	 * @return Exception with formated error message
	 */
	protected static BuildPDFAExtensionSchemaDescriptionException propertyDescriptionError(
			String classSchem, String propName, String details, Throwable e) {
		StringBuilder sb = new StringBuilder(80);
		sb
				.append(
						"Error while building PDF/A Extension Schema description for '")
				.append(classSchem).append("' schema, Failed to treat '")
				.append(propName).append("' property : ").append(details);
		return new BuildPDFAExtensionSchemaDescriptionException(sb.toString(),
				e);
	}

}
