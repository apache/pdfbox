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

import java.util.Calendar;
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;

public abstract class AbstractStructuredType extends AbstractField {

	
	
	/** The prefix of the fields of the structure */
	private String fieldPrefix = null;
	
	private ComplexPropertyContainer container = null;

	protected static final String STRUCTURE_ARRAY_PREFIX = "rdf";

	protected static final  String STRUCTURE_ARRAY_NAME = "li"; 

	public AbstractStructuredType(XMPMetadata metadata, String namespaceURI,
			String fieldPrefix) {
		super(metadata, namespaceURI, STRUCTURE_ARRAY_PREFIX, STRUCTURE_ARRAY_NAME);
		this.container = new ComplexPropertyContainer(metadata, namespaceURI, STRUCTURE_ARRAY_PREFIX, "Description");
		this.fieldPrefix = fieldPrefix;
		getElement().appendChild(container.getElement());
	}

	public abstract String getFieldsNamespace();
	
	public String getFieldPrefix () {
		return this.fieldPrefix;
	}


	public final void addProperty(AbstractField obj) {
		container.addProperty(obj);
	}
	
	protected final AbstractField getFirstEquivalentProperty(String localName,
			Class<? extends AbstractField> type) {
		return container.getFirstEquivalentProperty(localName, type);
	}

	public final List<AbstractField> getAllProperties() {
		return container.getAllProperties();
	}

	
	protected void addSimpleProperty (String propertyName, Object value) {
		TypeMapping tm = getMetadata().getBuilder().getTypeMapping();
		AbstractSimpleProperty asp = tm.instanciateSimpleField(getClass(), getMetadata(),null,fieldPrefix,propertyName, value);
		addProperty(asp);
	}


	protected AbstractSimpleProperty getProperty (String fieldName) {
		List<AbstractField> list = container.getPropertiesByLocalName(fieldName);
		// return null if no property
		if (list==null) {
			return null;
		}
		// return the first element of the list
		return (AbstractSimpleProperty)list.get(0);
	}

	
	protected String getPropertyValueAsString (String fieldName) {
		AbstractSimpleProperty absProp = getProperty(fieldName);
		if (absProp == null) {
			return null;
		} else {
			return absProp.getStringValue();
		}
	}

	protected Calendar getDatePropertyAsCalendar(String fieldName) {
		DateType absProp = (DateType)container.getFirstEquivalentProperty(fieldName,DateType.class);
		if (absProp != null) {
			return absProp.getValue();
		} else {
			return null;
		}
	}

	public ComplexPropertyContainer getContainer() {
		return container;
	}

}
