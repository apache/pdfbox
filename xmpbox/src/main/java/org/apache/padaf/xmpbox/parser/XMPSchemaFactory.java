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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.XMPSchema;


/**
 * A factory for each kind of schemas
 * 
 * @author a183132
 * 
 */
public class XMPSchemaFactory {

	protected String namespace;
	protected Class<? extends XMPSchema> schemaClass;
	protected PropMapping propDef;
	protected String nsName;
	protected boolean isDeclarative;
	
	protected List<PropMapping> importedPropertyMapping = new ArrayList<PropMapping>();
	
	/**
	 * Factory Constructor for basic known schemas
	 * 
	 * @param namespace
	 *            namespace URI to treat
	 * @param schemaClass
	 *            Class representation associated to this URI
	 * @param propDef
	 *            Properties Types list associated
	 */
	public XMPSchemaFactory(String namespace,
			Class<? extends XMPSchema> schemaClass, PropMapping propDef) {
		this.isDeclarative = false;
		this.namespace = namespace;
		this.schemaClass = schemaClass;
		this.propDef = propDef;
	}

	/**
	 * Factory constructor for declarative XMP Schemas
	 * 
	 * @param nsName
	 *            namespace name to treat
	 * @param namespace
	 *            namespace URI to treat
	 * @param schemaClass
	 *            Class representation associated to this URI
	 * @param propDef
	 *            Properties Types list associated
	 */
	public XMPSchemaFactory(String nsName, String namespace,
			Class<? extends XMPSchema> schemaClass, PropMapping propDef) {
		this.isDeclarative = true;
		this.namespace = namespace;
		this.schemaClass = schemaClass;
		this.propDef = propDef;
		this.nsName = nsName;
	}

	public void importXMPSchemaFactory(XMPSchemaFactory externalFactory) 
	throws XmpSchemaException {
		if (!this.namespace.equals(externalFactory.namespace)) {
			throw new XmpSchemaException("Unable to import a XMPSchemaFactory if the namespace is different." +
					" - expected : " + this.namespace + " - import : " + externalFactory.namespace);
		}
		this.importedPropertyMapping.add(externalFactory.propDef);
	}
	
	/**
	 * Get namespace URI treated by this factory
	 * 
	 * @return The namespace URI
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Get type declared for the name property given
	 * 
	 * @param name
	 *            The property name
	 * @return null if propery name is unknown
	 */
	public String getPropertyType(String name) {
		String result = propDef.getPropertyType(name);
		if (result == null) {
			for (PropMapping mapping : importedPropertyMapping) {
				result = mapping.getPropertyType(name);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Get attributes declared for a property (NOT USED YET)
	 * 
	 * @param name
	 *            The property Name
	 * @return List of all attributes defined for this property
	 */
	public List<String> getPropertyAttributes(String name) {
		List<String> result = propDef.getPropertyAttributes(name);
		if (result == null) {
			for (PropMapping mapping : importedPropertyMapping) {
				result = mapping.getPropertyAttributes(name);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Create a schema that corresponding to this factory and add it to metadata
	 * 
	 * @param metadata
	 *            Metadata to attach the Schema created
	 * @param prefix
	 * 						The namespace prefix (optional)
	 * @return the schema created and added to metadata
	 * @throws XmpSchemaException
	 *             When Instancing specified Object Schema failed
	 */
	@SuppressWarnings("unchecked")
	public XMPSchema createXMPSchema(XMPMetadata metadata, String prefix)
	throws XmpSchemaException {
		XMPSchema schema = null;
		Class[] argsClass;
		Object[] schemaArgs;

		if (isDeclarative) {
			argsClass = new Class[] { XMPMetadata.class, String.class, String.class };
			schemaArgs = new Object[] { metadata, nsName, namespace };
		} else if (prefix != null && !"".equals(prefix)) {
			argsClass = new Class[] { XMPMetadata.class, String.class };
			schemaArgs = new Object[] { metadata, prefix };
		} else {
			argsClass = new Class[] { XMPMetadata.class };
			schemaArgs = new Object[] { metadata };
		}

		Constructor<? extends XMPSchema> schemaConstructor;
		try {
			schemaConstructor = schemaClass.getConstructor(argsClass);
			schema = schemaConstructor.newInstance(schemaArgs);
			if (schema != null) {
				metadata.addSchema(schema);
			}
			return schema;
		} catch (Exception e) {
			throw new XmpSchemaException(
					"Cannot Instanciate specified Object Schema", e);
		}
	}

}
