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

package org.apache.xmpbox.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractStructuredTypeTester extends AbstractTypeTester
{

    protected XMPMetadata xmp;

    protected String fieldName;

    protected Types type;

    protected Class<? extends AbstractStructuredType> clz;

    protected TypeMapping typeMapping = null;

    protected DomXmpParser builder;

    public void before() throws Exception
    {
        builder = new DomXmpParser();
        xmp = XMPMetadata.createXMPMetadata();
        typeMapping = xmp.getTypeMapping();
    }

    public AbstractStructuredTypeTester(Class<? extends AbstractStructuredType> clz, String fieldName, Types type)
    {
        this.clz = clz;
        this.fieldName = fieldName;
        this.type = type;
    }

    protected abstract AbstractStructuredType getStructured();

    @Test
    public void testInitializedToNull() throws Exception
    {
        AbstractStructuredType structured = getStructured();
        // default method
        Assert.assertNull(structured.getProperty(fieldName));
        // accessor
        Method get = clz.getMethod(calculateSimpleGetter(fieldName));
        Object result = get.invoke(structured);
        Assert.assertNull(result);

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
        AbstractStructuredType structured = getStructured();
        Object value = getJavaValue(type);
        structured.addSimpleProperty(fieldName, value);
        Assert.assertNotNull(structured.getProperty(fieldName));
        // check other properties not modified
        List<Field> fields = getXmpFields(clz);
        for (Field field : fields)
        {
            // do not check the current name
            String name = field.get(null).toString();
            if (!name.equals(fieldName))
            {
                Assert.assertNull(structured.getProperty(name));
            }
        }
    }


    @Test
    public void testPropertyType() throws Exception
    {
        internalTestPropertyType();
    }

    @Test
    public void testRandomPropertyType() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestPropertyType();
        }
    }


    private void internalTestPropertyType() throws Exception
    {
        AbstractStructuredType structured = getStructured();
        Object value = getJavaValue(type);
        structured.addSimpleProperty(fieldName, value);
        Assert.assertNotNull(structured.getProperty(fieldName));
        // check property type
        AbstractSimpleProperty asp = (AbstractSimpleProperty) structured.getProperty(fieldName);
        Assert.assertEquals(type.getImplementingClass(), asp.getClass());
    }


    @Test
    public void testSetter() throws Exception
    {
        internalTestSetter();
    }

    @Test
    public void testRandomSetter() throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestSetter();
        }
    }

    private void internalTestSetter() throws Exception
    {
        AbstractStructuredType structured = getStructured();
        String setter = calculateSimpleSetter(fieldName);
        Object value = getJavaValue(type);
        Method set = clz.getMethod(setter, getJavaType(type));
        set.invoke(structured, value);
        // check property set
        Assert.assertEquals(value, ((AbstractSimpleProperty) structured.getProperty(fieldName)).getValue());
        // check getter
        Method get = clz.getMethod(calculateSimpleGetter(fieldName));
        Object result = get.invoke(structured);
        Assert.assertTrue(getJavaType(type).isAssignableFrom(result.getClass()));
        Assert.assertEquals(value, result);
    }
}
