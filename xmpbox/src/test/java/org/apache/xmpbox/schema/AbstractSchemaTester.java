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

package org.apache.xmpbox.schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AbstractSimpleProperty;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.type.AbstractTypeTester;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractSchemaTester extends AbstractTypeTester
{

    protected XMPMetadata xmp;

    protected String fieldName;

    protected Types type;

    protected Cardinality cardinality;

    protected TypeMapping typeMapping = null;

    protected DomXmpParser builder;

    public void before() throws Exception
    {
        builder = new DomXmpParser();
        xmp = XMPMetadata.createXMPMetadata();
        typeMapping = xmp.getTypeMapping();
    }

    protected abstract XMPSchema getSchema();

    protected Class<? extends XMPSchema> getSchemaClass()
    {
        return getSchema().getClass();
    }

    public AbstractSchemaTester(String fieldName, Types type, Cardinality card)
    {
        this.fieldName = fieldName;
        this.type = type;
        this.cardinality = card;
    }

    @Test
    public void testInitializedToNull() throws Exception
    {
        XMPSchema schema = getSchema();
        // default method
        Assert.assertNull(schema.getProperty(fieldName));
        // accessor
        if (cardinality == Cardinality.Simple)
        {
            String getter = calculateSimpleGetter(fieldName);
            Method get = getSchemaClass().getMethod(getter);
            Object result = get.invoke(schema);
            Assert.assertNull(result);
        }
        else
        {
            // arrays
            String getter = calculateArrayGetter(fieldName);
            Method get = getSchemaClass().getMethod(getter);
            Object result = get.invoke(schema);
            Assert.assertNull(result);
        }

    }

    @Test
    public void testSettingValue() throws Exception
    {
        internalTestSettingValue();
    }

    @Test
    public void testRandomSettingValue() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestSettingValue();
        }
    }

    private void internalTestSettingValue() throws Exception
    {
        if (cardinality != Cardinality.Simple)
        {
            return;
        }
        XMPSchema schema = getSchema();
        // only test simple properties
        Object value = getJavaValue(type);
        AbstractSimpleProperty property = schema.instanciateSimple(fieldName, value);
        schema.addProperty(property);
        String qn = getPropertyQualifiedName(fieldName);
        Assert.assertNotNull(schema.getProperty(fieldName));
        // check other properties not modified
        List<Field> fields = getXmpFields(getSchemaClass());
        for (Field field : fields)
        {
            // do not check the current name
            String fqn = getPropertyQualifiedName(field.get(null).toString());
            if (!fqn.equals(qn))
            {
                Assert.assertNull(schema.getProperty(fqn));
            }
        }
    }

    @Test
    public void testSettingValueInArray() throws Exception
    {
        internalTestSettingValueInArray();
    }

    @Test
    public void testRandomSettingValueInArray() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestSettingValueInArray();
        }
    }

    private void internalTestSettingValueInArray() throws Exception
    {
        if (cardinality == Cardinality.Simple)
        {
            return;
        }
        XMPSchema schema = getSchema();
        // only test array properties
        Object value = getJavaValue(type);
        AbstractSimpleProperty property = schema.instanciateSimple(fieldName, value);
        switch (cardinality)
        {
            case Seq:
                schema.addUnqualifiedSequenceValue(property.getPropertyName(), property);
                break;
            case Bag:
                schema.addBagValue(property.getPropertyName(), property);
                break;
            default:
                throw new Exception("Unexpected case in test : " + cardinality.name());
        }
        String qn = getPropertyQualifiedName(fieldName);
        Assert.assertNotNull(schema.getProperty(fieldName));
        // check other properties not modified
        List<Field> fields = getXmpFields(getSchemaClass());
        for (Field field : fields)
        {
            // do not check the current name
            String fqn = getPropertyQualifiedName(field.get(null).toString());
            if (!fqn.equals(qn))
            {
                Assert.assertNull(schema.getProperty(fqn));
            }
        }
    }

    @Test
    public void testPropertySetterSimple() throws Exception
    {
        internalTestPropertySetterSimple();
    }

    @Test
    public void testRandomPropertySetterSimple() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestPropertySetterSimple();
        }
    }

    private void internalTestPropertySetterSimple() throws Exception
    {
        if (cardinality != Cardinality.Simple)
        {
            return;
        }
        XMPSchema schema = getSchema();
        String setter = calculateSimpleSetter(fieldName) + "Property";
        Object value = getJavaValue(type);
        AbstractSimpleProperty asp = typeMapping.instanciateSimpleProperty(schema.getNamespace(), schema
                .getPrefix(), fieldName, value, type);
        Method set = getSchemaClass().getMethod(setter, type.getImplementingClass());
        set.invoke(schema, asp);
        // check property set
        AbstractSimpleProperty stored = (AbstractSimpleProperty) schema.getProperty(fieldName);
        Assert.assertEquals(value, stored.getValue());
        // check getter
        String getter = calculateSimpleGetter(fieldName) + "Property";
        Method get = getSchemaClass().getMethod(getter);
        Object result = get.invoke(schema);
        Assert.assertTrue(type.getImplementingClass().isAssignableFrom(result.getClass()));
        Assert.assertEquals(asp, result);
    }

    @Test
    public void testPropertySetterInArray() throws Exception
    {
        internalTestPropertySetterInArray();
    }

    @Test
    public void testRandomPropertySetterInArray() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestPropertySetterInArray();
        }
    }

    private void internalTestPropertySetterInArray() throws Exception
    {
        if (cardinality == Cardinality.Simple)
        {
            return;
        }
        XMPSchema schema = getSchema();
        // add value
        String setter = "add" + calculateFieldNameForMethod(fieldName);
        // TypeDescription<AbstractSimpleProperty> td =
        // typeMapping.getSimpleDescription(type);
        Object value1 = getJavaValue(type);
        Method set = getSchemaClass().getMethod(setter, getJavaType(type));
        set.invoke(schema, value1);
        // retrieve complex property
        String getter = calculateArrayGetter(fieldName) + "Property";
        Method getcp = getSchemaClass().getMethod(getter);
        Object ocp = getcp.invoke(schema);
        Assert.assertTrue(ocp instanceof ArrayProperty);
        ArrayProperty cp = (ArrayProperty) ocp;
        // check size is ok (1)
        Assert.assertEquals(1, cp.getContainer().getAllProperties().size());
        // add a new one
        Object value2 = getJavaValue(type);
        set.invoke(schema, value2);
        Assert.assertEquals(2, cp.getContainer().getAllProperties().size());
        // remove the first
        String remover = "remove" + calculateFieldNameForMethod(fieldName);
        Method remove = getSchemaClass().getMethod(remover, getJavaType(type));
        remove.invoke(schema, value1);
        Assert.assertEquals(1, cp.getContainer().getAllProperties().size());

    }

    protected String getPropertyQualifiedName(String name)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getSchema().getPrefix()).append(":").append(name);
        return sb.toString();
    }

}
