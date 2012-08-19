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

package org.apache.padaf.xmpbox.type;

import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;

public abstract class AbstractComplexProperty extends AbstractField {

	private ComplexPropertyContainer container;
	
	public AbstractComplexProperty(XMPMetadata metadata, String namespaceURI,
			String prefix, String propertyName) {
		super(metadata, namespaceURI, prefix, propertyName);
		container = new ComplexPropertyContainer();
	}

	/**
	 * Add a property to the current structure
	 * 
	 * @param obj the property to add
	 */
	public final void addProperty(AbstractField obj) {
		container.addProperty(obj);
	}

	/**
	 * Remove a property
	 * 
	 * @param property
	 *            The property to remove
	 */
	public final void removeProperty(AbstractField property) {
		container.removeProperty(property);
	}

//	/**
//	 * Return the container of this Array
//	 * 
//	 * @return The complex property container that represents content of this
//	 *         property
//	 */
	public final ComplexPropertyContainer getContainer() {
		return container;
	}
	
	public final List<AbstractField> getAllProperties() {
		return container.getAllProperties();
	}

	public final AbstractSimpleProperty getProperty (String fieldName) {
		List<AbstractField> list = container.getPropertiesByLocalName(fieldName);
		// return null if no property
		if (list==null) {
			return null;
		}
		// return the first element of the list
		return (AbstractSimpleProperty)list.get(0);
	}

	public final ArrayProperty getArrayProperty (String fieldName) {
		List<AbstractField> list = container.getPropertiesByLocalName(fieldName);
		// return null if no property
		if (list==null) {
			return null;
		}
		// return the first element of the list
		return (ArrayProperty)list.get(0);
	}

	protected final AbstractField getFirstEquivalentProperty(String localName,
			Class<? extends AbstractField> type) {
		return container.getFirstEquivalentProperty(localName, type);
	}

}
