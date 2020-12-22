/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class SchemaTester extends AbstractTypeTester {

    private final XMPMetadata metadata;
    private final Class<?> schemaClass;
    private final String fieldName;
    private final Types type;
    private final Cardinality cardinality;

    private final TypeMapping typeMapping;

    XMPSchema getSchema()
    {
        switch(schemaClass.getSimpleName())
        {
            case "DublinCoreSchema":
                return metadata.createAndAddDublinCoreSchema();
            case "PhotoshopSchema":
                return metadata.createAndAddPhotoshopSchema();
            default:
                return metadata.createAndAddXMPBasicSchema();
        }    
    }

    public SchemaTester(final XMPMetadata metadata, final Class<?> schemaClass, final String fieldName, final Types type, final Cardinality card)
    {
        this.metadata = metadata;
        this.schemaClass = schemaClass;
        this.typeMapping = metadata.getTypeMapping();
        this.fieldName = fieldName;
        this.type = type;
        this.cardinality = card;
    }

    public void testInitializedToNull() throws Exception
    {
        final XMPSchema schema = getSchema();
        // default method
        assertNull(schema.getProperty(fieldName));
        // accessor
        if (cardinality == Cardinality.Simple)
        {
            final String getter = calculateSimpleGetter(fieldName);
            final Method get = schemaClass.getMethod(getter);
            final Object result = get.invoke(schema);
            assertNull(result);
        }
        else
        {
            // arrays
            final String getter = calculateArrayGetter(fieldName);
            final Method get = schemaClass.getMethod(getter);
            final Object result = get.invoke(schema);
            assertNull(result);
        }

    }

    public void testSettingValue() throws Exception
    {
        internalTestSettingValue();
    }

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
        final XMPSchema schema = getSchema();
        // only test simple properties
        final Object value = getJavaValue(type);
        final AbstractSimpleProperty property = schema.instanciateSimple(fieldName, value);
        schema.addProperty(property);
        final String qn = getPropertyQualifiedName(fieldName);
        assertNotNull(schema.getProperty(fieldName));
        // check other properties not modified
        final List<Field> fields = getXmpFields(schemaClass);
        for (final Field field : fields)
        {
            // do not check the current name
            final String fqn = getPropertyQualifiedName(field.get(null).toString());
            if (!fqn.equals(qn))
            {
                assertNull(schema.getProperty(fqn));
            }
        }
    }

    public void testSettingValueInArray() throws Exception
    {
        internalTestSettingValueInArray();
    }

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

        final XMPSchema schema = getSchema();

        // only test array properties
        final Object value = getJavaValue(type);
        final AbstractSimpleProperty property = schema.instanciateSimple(fieldName, value);
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
        final String qn = getPropertyQualifiedName(fieldName);
        assertNotNull(schema.getProperty(fieldName));
        // check other properties not modified
        final List<Field> fields = getXmpFields(schemaClass);
        for (final Field field : fields)
        {
            // do not check the current name
            final String fqn = getPropertyQualifiedName(field.get(null).toString());
            if (!fqn.equals(qn))
            {
                assertNull(schema.getProperty(fqn));
            }
        }
    }

    public void testPropertySetterSimple() throws Exception
    {
        internalTestPropertySetterSimple();
    }

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

        final XMPSchema schema = getSchema();

        final String setter = calculateSimpleSetter(fieldName) + "Property";
        final Object value = getJavaValue(type);
        final AbstractSimpleProperty asp = typeMapping.instanciateSimpleProperty(schema.getNamespace(), schema
                .getPrefix(), fieldName, value, type);
        final Method set = schemaClass.getMethod(setter, type.getImplementingClass());
        set.invoke(schema, asp);
        // check property set
        final AbstractSimpleProperty stored = (AbstractSimpleProperty) schema.getProperty(fieldName);
        assertEquals(value, stored.getValue());
        // check getter
        final String getter = calculateSimpleGetter(fieldName) + "Property";
        final Method get = schemaClass.getMethod(getter);
        final Object result = get.invoke(schema);
        assertTrue(type.getImplementingClass().isAssignableFrom(result.getClass()));
        assertEquals(asp, result);
    }

    public void testPropertySetterInArray() throws Exception
    {
        internalTestPropertySetterInArray();
    }

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

        final XMPSchema schema = getSchema();

        // add value
        final String setter = "add" + calculateFieldNameForMethod(fieldName);
        // TypeDescription<AbstractSimpleProperty> td =
        // typeMapping.getSimpleDescription(type);
        final Object value1 = getJavaValue(type);
        final Method set = schemaClass.getMethod(setter, getJavaType(type));
        set.invoke(schema, value1);
        // retrieve complex property
        final String getter = calculateArrayGetter(fieldName) + "Property";
        final Method getcp = schemaClass.getMethod(getter);
        final Object ocp = getcp.invoke(schema);
        assertTrue(ocp instanceof ArrayProperty);
        final ArrayProperty cp = (ArrayProperty) ocp;
        // check size is ok (1)
        assertEquals(1, cp.getContainer().getAllProperties().size());
        // add a new one
        final Object value2 = getJavaValue(type);
        set.invoke(schema, value2);
        assertEquals(2, cp.getContainer().getAllProperties().size());
        // remove the first
        final String remover = "remove" + calculateFieldNameForMethod(fieldName);
        final Method remove = schemaClass.getMethod(remover, getJavaType(type));
        remove.invoke(schema, value1);
        assertEquals(1, cp.getContainer().getAllProperties().size());
    }

    protected String getPropertyQualifiedName(final String name)
    {
        final XMPSchema schema = getSchema();
        final StringBuilder sb = new StringBuilder();
        sb.append(schema.getPrefix()).append(":").append(name);
        return sb.toString();
    }
}
