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

/**
 * Simple representation of an attribute
 * 
 * @author a183132
 * 
 */
public class Attribute {

	private String nsURI, prefix, localName, value;

	/**
	 * Constructor of a new Attribute
	 * 
	 * @param nsURI
	 *            namespaceURI of this attribute (could be null)
	 * @param prefix
	 *            prefix of this attribute
	 * @param localName
	 *            localName of this attribute
	 * @param value
	 *            value given to this attribute
	 */
	public Attribute(String nsURI, String prefix, String localName, String value) {
		this.nsURI = nsURI;
		this.prefix = prefix;
		this.localName = localName;
		this.value = value;
	}

	/**
	 * Get prefix defined for this attribute
	 * 
	 * @return prefix defined (could be null)
	 */
	public String getPrefix() {
		if (prefix != null) {
			if (prefix.equals("")) {
				return null;
			}
			return prefix;
		}
		return null;
	}

	/**
	 * Set prefix for this attribute
	 * 
	 * @param prefix
	 *            the prefix defined for this attribute
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Get the localName of this attribute
	 * 
	 * @return local name of this attribute
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Set the localName of this attribute
	 * 
	 * @param lname
	 *            the local name to set
	 */
	public void setLocalName(String lname) {
		localName = lname;
	}

	/**
	 * Get the namespace URI of this attribute
	 * 
	 * @return the namespace URI associated to this attribute (could be null)
	 */
	public String getNamespace() {
		return nsURI;
	}

	/**
	 * Set the namespace URI of this attribute
	 * 
	 * @param nsURI
	 *            the namespace URI to set
	 */
	public void setNsURI(String nsURI) {
		this.nsURI = nsURI;
	}

	/**
	 * Get the attribute qualified Name (prefix+localName)
	 * 
	 * @return the full qualified name of this attribute
	 */
	public String getQualifiedName() {
		if (prefix == null) {
			return localName;
		}
		if (prefix.equals("")) {
			return localName;
		}
		return prefix + ":" + localName;
	}

	/**
	 * Get value of this attribute
	 * 
	 * @return value of this attribute
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set value of this attribute
	 * 
	 * @param value
	 *            the value to set for this attribute
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
