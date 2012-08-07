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

import java.util.HashMap;
import java.util.Map;

import org.apache.padaf.xmpbox.XMPMetadata;

public class DefinedStructuredType extends AbstractStructuredType {

	private Map<String, String> definedProperties = null;
	
	public DefinedStructuredType(XMPMetadata metadata, String namespaceURI,
			String fieldPrefix) {
		super(metadata, namespaceURI, fieldPrefix);
		this.definedProperties = new HashMap<String, String>();
	}

	@Override
	public String getFieldsNamespace() {
		return getNamespace();
	}

	public void addProperty (String name, String type) {
		definedProperties.put(name, type);
	}

	protected Map<String, String> getDefinedProperties() {
		return definedProperties;
	}


	
}
