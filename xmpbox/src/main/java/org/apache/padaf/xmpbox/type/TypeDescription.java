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

import org.apache.padaf.xmpbox.parser.PropMapping;


public class TypeDescription {

	public enum BasicType {	Text, Date, Integer, Boolean, Real}

	private String type;
	
	private BasicType basic;
	
	private Class<? extends AbstractField> clz;
	
	// TODO PropMapping should be in package Type
	private PropMapping properties = null;

	public TypeDescription(String type, BasicType basic,Class<? extends AbstractField> clz) {
		super();
		this.type = type;
		this.basic = basic;
		this.clz = clz;
	}
	

	public TypeDescription(String type, BasicType basic) {
		this(type, basic,TextType.class);
	}

	public TypeDescription(String type) {
		this(type,BasicType.Text,TextType.class);
	}

	
	public String getType() {
		return type;
	}

	public Class<? extends AbstractField> getTypeClass() {
		return clz;
	}

	public BasicType getBasic() {
		return basic;
	}

	public PropMapping getProperties() {
		return properties;
	}

	protected void setProperties(PropMapping properties) {
		this.properties = properties;
	}
	
}
