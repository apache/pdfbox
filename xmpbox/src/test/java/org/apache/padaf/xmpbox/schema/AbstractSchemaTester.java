 /*****************************************************************************
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.TypeDescription;
import org.apache.padaf.xmpbox.type.TypeMapping;
import org.apache.padaf.xmpbox.type.TypeTestingHelper;
import org.junit.Test;

public abstract class AbstractSchemaTester {

	
	protected XMPMetadata xmp;
	
	protected String fieldName;
	
	protected String type;
	
	protected Cardinality cardinality;

	protected TypeMapping typeMapping = null;
	
	protected XMPDocumentBuilder builder;
	
	protected enum Cardinality {Simple, Bag, Seq, Alt}
	
	public void before () throws Exception {
		builder = new XMPDocumentBuilder();
		xmp = builder.createXMPMetadata();
		typeMapping = builder.getTypeMapping();
	}

	protected abstract XMPSchema getSchema ();
	
	protected Class<? extends XMPSchema> getSchemaClass () {
		return getSchema().getClass();
	}
	
	public AbstractSchemaTester (String fieldName, String type, Cardinality card) {
		this.fieldName = fieldName;
		this.type = type;
		this.cardinality = card;
	}

	@Test
	public void testInitializedToNull() throws Exception {
		// default method
		Assert.assertNull(getSchema().getProperty(fieldName));
		// accessor
		if (cardinality==Cardinality.Simple) {
			String getter = TypeTestingHelper.calculateSimpleGetter(fieldName);
	    	Method get = getSchemaClass().getMethod(getter, new Class[0]);
	    	Object result = get.invoke(getSchema(), new Object [0]);
	    	Assert.assertNull(result);
		} else {
			// arrays
			String getter = TypeTestingHelper.calculateArrayGetter(fieldName);
	    	Method get = getSchemaClass().getMethod(getter, new Class[0]);
	    	Object result = get.invoke(getSchema(), new Object [0]);
	    	Assert.assertNull(result);
		}
		
	}

	@Test
	public void testSettingValue() throws Exception {
		if (cardinality!=Cardinality.Simple) return;
		// only test simple properties
		TypeDescription td =typeMapping.getTypeDescription(type);
		Object value = TypeTestingHelper.getJavaValue(td);
		AbstractSimpleProperty property = getSchema().instanciateSimple(fieldName, value);
		getSchema().addProperty(property);
		String qn = getPropertyQualifiedName(fieldName);
		Assert.assertNotNull(getSchema().getProperty(qn));
		// check other properties not modified
		List<Field> fields = TypeTestingHelper.getXmpFields(getSchemaClass());
		for (Field field : fields) {
			// do not check the current name
			String fqn = getPropertyQualifiedName(field.get(null).toString());
			if (!fqn.equals(qn)) {
				Assert.assertNull(getSchema().getProperty(fqn));
			}
		}
	}

	@Test
	public void testSettingValueInArray() throws Exception {
		if (cardinality==Cardinality.Simple) return;
		// only test array properties
		TypeDescription td =typeMapping.getTypeDescription(type);
		Object value = TypeTestingHelper.getJavaValue(td);
		AbstractSimpleProperty property = getSchema().instanciateSimple(fieldName, value);
		switch (cardinality) {
		case Seq:
			getSchema().addUnqualifiedSequenceValue(property.getPropertyName(), property);
			break;
		case Bag:
			getSchema().addBagValue(property.getQualifiedName(), property);
			break;
		default :
			throw new Exception ("Unexpected case in test : "+cardinality.name());
		}
		String qn = getPropertyQualifiedName(fieldName);
		Assert.assertNotNull(getSchema().getProperty(qn));
		// check other properties not modified
		List<Field> fields = TypeTestingHelper.getXmpFields(getSchemaClass());
		for (Field field : fields) {
			// do not check the current name
			String fqn = getPropertyQualifiedName(field.get(null).toString());
			if (!fqn.equals(qn)) {
				Assert.assertNull(getSchema().getProperty(fqn));
			}
		}
	}

	
    @Test
    public void testPropertySetterSimple () throws Exception {
		if (cardinality!=Cardinality.Simple) return;
    	String setter = TypeTestingHelper.calculateSimpleSetter(fieldName)+"Property";
    	TypeDescription td = typeMapping.getTypeDescription(type);
    	Object value = TypeTestingHelper.getJavaValue(td);
    	AbstractSimpleProperty asp = typeMapping.instanciateSimpleProperty(
    			xmp, getSchema().getNamespaceValue(), 
    			getSchema().getLocalPrefix(), fieldName, value, type);
    	Method set = getSchemaClass().getMethod(setter, new Class<?>[] {td.getTypeClass()} );
    	set.invoke(getSchema(), new Object [] {asp});
    	// check property set
    	AbstractSimpleProperty stored = (AbstractSimpleProperty)getSchema().getProperty(getPropertyQualifiedName(fieldName));
    	Assert.assertEquals(value, stored.getObjectValue());
    	// check getter
    	String getter = TypeTestingHelper.calculateSimpleGetter(fieldName)+"Property";
    	Method get = getSchemaClass().getMethod(getter, new Class[0]);
    	Object result = get.invoke(getSchema(), new Object [0]);
    	Assert.assertTrue(td.getTypeClass().isAssignableFrom(result.getClass()));
    	Assert.assertEquals(asp, result);
    }

    @Test
    public void testPropertySetterInArray () throws Exception {
		if (cardinality==Cardinality.Simple) return;
		// add value
    	String setter = "add"+TypeTestingHelper.calculateFieldNameForMethod(fieldName);
    	TypeDescription td = typeMapping.getTypeDescription(type);
    	Object value1 = TypeTestingHelper.getJavaValue(td);
    	Method set = getSchemaClass().getMethod(setter, new Class<?>[] {TypeTestingHelper.getJavaType(td)} );
    	set.invoke(getSchema(), new Object [] {value1});
    	// retrieve complex property
    	String getter = TypeTestingHelper.calculateArrayGetter(fieldName)+"Property";
    	Method getcp = getSchemaClass().getMethod(getter, new Class[0]);
    	Object ocp = getcp.invoke(getSchema(), new Object[0]);
    	Assert.assertTrue(ocp instanceof ArrayProperty);
    	ArrayProperty cp = (ArrayProperty)ocp;
    	// check size is ok (1)
    	Assert.assertEquals(1,cp.getContainer().getAllProperties().size());
    	// add a new one
    	Object value2 = TypeTestingHelper.getJavaValue(td);
    	set.invoke(getSchema(), new Object [] {value2});
    	Assert.assertEquals(2,cp.getContainer().getAllProperties().size());
    	// remove the first
    	String remover = "remove"+TypeTestingHelper.calculateFieldNameForMethod(fieldName);
    	Method remove = getSchemaClass().getMethod(remover, new Class<?>[] {TypeTestingHelper.getJavaType(td)});
    	remove.invoke(getSchema(), value1);
    	Assert.assertEquals(1,cp.getContainer().getAllProperties().size());
    	
    	
//    	// check property set
//    	AbstractSimpleProperty stored = (AbstractSimpleProperty)getSchema().getProperty(getPropertyQualifiedName(fieldName));
//    	Assert.assertEquals(value, stored.getObjectValue());
//    	// check getter
//    	String getter = TypeTestingHelper.calculateSimpleGetter(fieldName)+"Property";
//    	Method get = getSchemaClass().getMethod(getter, new Class[0]);
//    	Object result = get.invoke(getSchema(), new Object [0]);
//    	Assert.assertTrue(td.getTypeClass().isAssignableFrom(result.getClass()));
//    	Assert.assertEquals(asp, result);
    }
	
	
	protected String getPropertyQualifiedName (String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSchema().getLocalPrefix()).append(":").append(name);
		return sb.toString();
	}
	
}
