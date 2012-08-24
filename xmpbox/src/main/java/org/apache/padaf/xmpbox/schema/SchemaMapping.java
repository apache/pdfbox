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

import java.util.HashMap;
import java.util.Map;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPSchemaFactory;
import org.apache.padaf.xmpbox.parser.XmpSchemaException;
import org.apache.padaf.xmpbox.type.PropMapping;
import org.apache.padaf.xmpbox.type.ReflectHelper;

public final class SchemaMapping {

	private static Map<String, XMPSchemaFactory> nsMaps;

	static {
		nsMaps = new HashMap<String, XMPSchemaFactory>();
		addNameSpace("http://ns.adobe.com/xap/1.0/", XMPBasicSchema.class);
		addNameSpace(DublinCoreSchema.DCURI, DublinCoreSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/extension/", PDFAExtensionSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/mm/", XMPMediaManagementSchema.class);
		addNameSpace("http://ns.adobe.com/pdf/1.3/", AdobePDFSchema.class);
		addNameSpace("http://www.aiim.org/pdfa/ns/id/", PDFAIdentificationSchema.class);
		addNameSpace("http://ns.adobe.com/xap/1.0/rights/",     XMPRightsManagementSchema.class);
		addNameSpace(PhotoshopSchema.PHOTOSHOPURI,      PhotoshopSchema.class);
		addNameSpace(XMPBasicJobTicketSchema.JOB_TICKET_URI,XMPBasicJobTicketSchema.class);

	}

	public SchemaMapping () {
	}
	
	
	/**
	 * Add a namespace declaration and Schema factory associated
	 * 
	 * @param ns
	 *            the Namespace URI
	 * @param classSchem
	 *            The class representation of the schema linked to the namespace
	 * @throws XmpSchemaException
	 *             When could not read property name in Schema Class given
	 */
	private static void addNameSpace(String ns, Class<? extends XMPSchema> classSchem) {
		nsMaps.put(ns, new XMPSchemaFactory(ns, classSchem,	ReflectHelper.initializePropMapping(ns, classSchem)));
	}

	public void addNewNameSpace(String ns) {
		PropMapping mapping = new PropMapping(ns);
		nsMaps.put(ns, new XMPSchemaFactory(ns, XMPSchema.class, mapping));
	}
	
	
	/**
	 * Return the specialized schema class representation if it's known (create
	 * and add it to metadata). In other cases, return null
	 * 
	 * @param metadata
	 *            Metadata to link the new schema
	 * @param namespace
	 *            The namespace URI
	 * @return Schema representation
	 * @throws XmpSchemaException
	 *             When Instancing specified Object Schema failed
	 */
	public XMPSchema getAssociatedSchemaObject(XMPMetadata metadata, String namespace, String prefix) throws XmpSchemaException {
		if (!nsMaps.containsKey(namespace)) {
			return null;
		}
		XMPSchemaFactory factory = nsMaps.get(namespace);
		return factory.createXMPSchema(metadata, prefix);
	}

	public XMPSchemaFactory getSchemaFactory(String namespace) {
		return nsMaps.get(namespace);
	}

	
	public boolean isContainedNamespace(String namespace) {
		return nsMaps.containsKey(namespace);
	}

	
	
}
