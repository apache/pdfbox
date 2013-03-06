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

import junit.framework.Assert;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.Test;

public abstract class AbstractStructuredTypeTester
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
        // default method
        Assert.assertNull(getStructured().getProperty(fieldName));
        // accessor
        Method get = clz.getMethod(TypeTestingHelper.calculateSimpleGetter(fieldName), new Class[0]);
        Object result = get.invoke(getStructured(), new Object[0]);
        Assert.assertNull(result);

    }

    @Test
    public void testSettingValue() throws Exception
    {
        Object value = TypeTestingHelper.getJavaValue(type);
        getStructured().addSimpleProperty(fieldName, value);
        Assert.assertNotNull(getStructured().getProperty(fieldName));
        // check other properties not modified
        List<Field> fields = TypeTestingHelper.getXmpFields(clz);
        for (Field field : fields)
        {
            // do not check the current name
            String name = field.get(null).toString();
            if (!name.equals(fieldName))
            {
                Assert.assertNull(getStructured().getProperty(name));
            }
        }
    }

    @Test
    public void testPropertyType() throws Exception
    {
        Object value = TypeTestingHelper.getJavaValue(type);
        getStructured().addSimpleProperty(fieldName, value);
        Assert.assertNotNull(getStructured().getProperty(fieldName));
        // check property type
        AbstractSimpleProperty asp = (AbstractSimpleProperty) getStructured().getProperty(fieldName);
        Assert.assertEquals(type.getImplementingClass(), asp.getClass());
    }

    @Test
    public void testSetter() throws Exception
    {
        String setter = TypeTestingHelper.calculateSimpleSetter(fieldName);
        Object value = TypeTestingHelper.getJavaValue(type);
        Method set = clz.getMethod(setter, new Class<?>[] { TypeTestingHelper.getJavaType(type) });
        set.invoke(getStructured(), new Object[] { value });
        // check property set
        Assert.assertEquals(value, ((AbstractSimpleProperty) getStructured().getProperty(fieldName)).getValue());
        // check getter
        Method get = clz.getMethod(TypeTestingHelper.calculateSimpleGetter(fieldName), new Class[0]);
        Object result = get.invoke(getStructured(), new Object[0]);
        // Assert.assertEquals(getJavaType(td),result.getClass());
        Assert.assertTrue(TypeTestingHelper.getJavaType(type).isAssignableFrom(result.getClass()));
        Assert.assertEquals(value, result);

    }

}
