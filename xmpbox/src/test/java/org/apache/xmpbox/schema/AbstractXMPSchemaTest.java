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

package org.apache.xmpbox.schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AgentNameType;
import org.apache.xmpbox.type.BooleanType;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.DateType;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.ThumbnailType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.type.URIType;
import org.apache.xmpbox.type.URLType;
import org.junit.Test;

public abstract class AbstractXMPSchemaTest
{

    protected XMPMetadata metadata;

    protected String property;

    protected PropertyType type;

    protected XMPSchema schema;

    protected Class<?> schemaClass;

    protected Object value;

    public AbstractXMPSchemaTest(String property, PropertyType type, Object value)
    {
        this.property = property;
        this.value = value;
        this.type = type;
    }

    public static Object[] wrapProperty(String name, Types type, Object value)
    {
        return wrapProperty(name, type, Cardinality.Simple, value);
    }

    public static Object[] wrapProperty(String name, Types type, Cardinality card, Object value)
    {
        // if (type==Types.Boolean) {
        // Assert.assertTrue(value instanceof Boolean);
        // } else if (type==Types.Text) {
        // Assert.assertTrue(value instanceof String);
        // } else if (type==Types.Integer) {
        // Assert.assertTrue(value instanceof Integer);
        // } else if (type==Types.Date) {
        // Assert.assertTrue(value instanceof Calendar);
        // } else if (type==Types.URL) {
        // Assert.assertTrue(value instanceof String);
        // }
        return new Object[] { name, TypeMapping.createPropertyType(type, card), value };
    }

    @Test
    public void testGetSetValue() throws Exception
    {
        if (type.type() == Types.Text && type.card() == Cardinality.Simple)
        {
            testGetSetTextValue();
        }
        else if (type.type() == Types.Boolean && type.card() == Cardinality.Simple)
        {
            testGetSetBooleanValue();
        }
        else if (type.type() == Types.Integer && type.card() == Cardinality.Simple)
        {
            testGetSetIntegerValue();
        }
        else if (type.type() == Types.Date && type.card() == Cardinality.Simple)
        {
            testGetSetDateValue();
        }
        else if (type.type() == Types.URI && type.card() == Cardinality.Simple)
        {
            testGetSetTextValue();
        }
        else if (type.type() == Types.URL && type.card() == Cardinality.Simple)
        {
            testGetSetTextValue();
        }
        else if (type.type() == Types.AgentName && type.card() == Cardinality.Simple)
        {
            testGetSetTextValue();
        }
        else if (type.type() == Types.LangAlt && type.card() == Cardinality.Simple)
        {
            // do nothing
        }
        else if (type.type() == Types.ResourceRef && type.card() == Cardinality.Simple)
        {
            // do nothing
        }
        else if (type.card() != Cardinality.Simple)
        {
            // do nothing
        }
        else
        {
            throw new Exception("Unknown type : " + type);
        }
    }

    @Test
    public void testGetSetProperty() throws Exception
    {
        if (type.type() == Types.Text && type.card() == Cardinality.Simple)
        {
            testGetSetTextProperty();
        }
        else if (type.type() == Types.URI && type.card() == Cardinality.Simple)
        {
            testGetSetURIProperty();
        }
        else if (type.type() == Types.URL && type.card() == Cardinality.Simple)
        {
            testGetSetURLProperty();
        }
        else if (type.type() == Types.AgentName && type.card() == Cardinality.Simple)
        {
            testGetSetAgentNameProperty();
        }
        else if (type.type() == Types.Boolean && type.card() == Cardinality.Simple)
        {
            testGetSetBooleanProperty();
        }
        else if (type.type() == Types.Integer && type.card() == Cardinality.Simple)
        {
            testGetSetIntegerProperty();
        }
        else if (type.type() == Types.Date && type.card() == Cardinality.Simple)
        {
            testGetSetDateProperty();
        }
        else if (type.type() == Types.Text && type.card() == Cardinality.Seq)
        {
            testGetSetTextListValue("seq");
        }
        else if (type.type() == Types.Version && type.card() == Cardinality.Seq)
        {
            testGetSetTextListValue("seq");
        }
        else if (type.type() == Types.Text && type.card() == Cardinality.Bag)
        {
            testGetSetTextListValue("bag");
        }
        else if (type.type() == Types.ProperName && type.card() == Cardinality.Bag)
        {
            testGetSetTextListValue("bag");
        }
        else if (type.type() == Types.XPath && type.card() == Cardinality.Bag)
        {
            testGetSetTextListValue("bag");
        }
        else if (type.type() == Types.Date && type.card() == Cardinality.Seq)
        {
            testGetSetDateListValue("seq");
        }
        else if (type.type() == Types.LangAlt && type.card() == Cardinality.Simple)
        {
            testGetSetLangAltValue();
        }
        else if (type.type() == Types.Thumbnail && type.card() == Cardinality.Alt)
        {
            testGetSetThumbnail();
        }
        else
        {
            throw new Exception("Unknown type : " + type);
        }
        Field[] fields = schemaClass.getFields();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(PropertyType.class))
            {
                if (!field.get(schema).equals(property))
                {
                    PropertyType pt = field.getAnnotation(PropertyType.class);
                    if (pt.type() == Types.LangAlt)
                    {
                        // do not check method existence
                    }
                    else if (pt.type() == Types.Thumbnail && pt.card() == Cardinality.Alt)
                    {
                        // do not check method existence
                    }
                    else if (pt.type() == Types.ResourceRef)
                    {
                        // do not check method existence
                    }
                    else if (pt.type() == Types.Version && pt.card() == Cardinality.Seq)
                    {
                        // do not check method existence
                    }
                    else
                    {
                        // type test
                        PropertyType spt = retrievePropertyType(field.get(schema).toString());
                        String getNameProperty = "get" + prepareName(field.get(schema).toString(), spt) + "Property";
                        Method getMethod = schemaClass.getMethod(getNameProperty);
                        Assert.assertNull(getNameProperty + " should return null when testing " + property,
                                getMethod.invoke(schema));
                        // value test
                        String getNameValue = "get" + prepareName(field.get(schema).toString(), spt);
                        getMethod = schemaClass.getMethod(getNameValue);
                        Assert.assertNotNull(getNameValue + " method should exist", getMethod);
                        Assert.assertNull(getNameValue + " should return null when testing " + property,
                                getMethod.invoke(schema));
                    }
                }
            }
        }
    }

    protected PropertyType retrievePropertyType(String prop) throws IllegalArgumentException, IllegalAccessException
    {
        Field[] fields = schemaClass.getFields();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(PropertyType.class))
            {
                PropertyType pt = field.getAnnotation(PropertyType.class);
                if (field.get(schema).equals(prop))
                {
                    return pt;
                }
            }
        }
        return type;
    }

    protected String firstUpper(String name)
    {
        StringBuilder sb = new StringBuilder(name.length());
        sb.append(name.substring(0, 1).toUpperCase());
        sb.append(name.substring(1));
        return sb.toString();
    }

    protected String prepareName(String prop, PropertyType type)
    {
        String fu = firstUpper(prop);
        StringBuilder sb = new StringBuilder(fu.length() + 1);
        sb.append(fu);
        if (fu.endsWith("s"))
        {
            // do nothing
        }
        else if (fu.endsWith("y"))
        {
            // do nothing
        }
        else if (type.card() != Cardinality.Simple)
        {
            sb.append("s");
        }
        return sb.toString();
    }

    protected String setMethod(String prop)
    {
        StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("set").append(prepareName(prop, type)).append("Property");
        return sb.toString();
    }

    protected String addMethod(String prop)
    {
        String fu = firstUpper(prop);
        StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("add").append(fu);
        return sb.toString();
    }

    protected String getMethod(String prop)
    {
        String fu = firstUpper(prop);
        StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("get").append(fu).append("Property");
        return sb.toString();
    }

    protected String setValueMethod(String prop)
    {
        String fu = firstUpper(prop);
        StringBuilder sb = new StringBuilder(8 + prop.length());
        sb.append("set").append(fu);
        return sb.toString();
    }

    protected String getValueMethod(String prop)
    {
        StringBuilder sb = new StringBuilder(8 + prop.length());
        sb.append("get").append(prepareName(prop, type));
        return sb.toString();
    }

    protected String addToValueMethod(String prop)
    {
        String fu = firstUpper(prop);
        StringBuilder sb = new StringBuilder(10 + prop.length());
        sb.append("add").append(fu);
        return sb.toString();

    }

    protected void testGetSetBooleanProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        BooleanType bt = new BooleanType(metadata, null, schema.getPrefix(), property, value);
        Method setMethod = schemaClass.getMethod(setName, BooleanType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, bt);
        Boolean found = ((BooleanType) getMethod.invoke(schema)).getValue();
        Assert.assertEquals(value, found);

    }

    protected void testGetSetDateProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        DateType dt = new DateType(metadata, null, schema.getPrefix(), property, value);
        Method setMethod = schemaClass.getMethod(setName, DateType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, dt);
        Calendar found = ((DateType) getMethod.invoke(schema)).getValue();
        Assert.assertEquals(value, found);
    }

    protected void testGetSetIntegerProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        IntegerType it = new IntegerType(metadata, null, schema.getPrefix(), property, value);
        Method setMethod = schemaClass.getMethod(setName, IntegerType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, it);
        Integer found = ((IntegerType) getMethod.invoke(schema)).getValue();
        Assert.assertEquals(value, found);
    }

    protected void testGetSetTextProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        TextType tt = metadata.getTypeMapping().createText(null, schema.getPrefix(), property, (String) value);
        Method setMethod = schemaClass.getMethod(setName, TextType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        Assert.assertEquals(value, found);

    }

    protected void testGetSetURIProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        URIType tt = metadata.getTypeMapping().createURI(null, schema.getPrefix(), property, (String) value);
        Method setMethod = schemaClass.getMethod(setName, URIType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        Assert.assertEquals(value, found);

    }

    protected void testGetSetURLProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        URLType tt = metadata.getTypeMapping().createURL(null, schema.getPrefix(), property, (String) value);
        Method setMethod = schemaClass.getMethod(setName, URLType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        Assert.assertEquals(value, found);

    }

    protected void testGetSetAgentNameProperty() throws Exception
    {
        String setName = setMethod(property);
        String getName = getMethod(property);

        AgentNameType tt = metadata.getTypeMapping()
                .createAgentName(null, schema.getPrefix(), property, (String) value);
        Method setMethod = schemaClass.getMethod(setName, AgentNameType.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        String found = ((AgentNameType) getMethod.invoke(schema)).getStringValue();
        Assert.assertEquals(value, found);

    }

    protected void testGetSetTextListValue(String tp) throws Exception
    {
        String setName = addToValueMethod(property);
        String getName = getValueMethod(property);
        String[] svalue = (String[]) value;
        Arrays.sort(svalue);
        // push all
        Method setMethod = schemaClass.getMethod(setName, String.class);
        for (String string : svalue)
        {
            setMethod.invoke(schema, string);
        }
        // retrieve
        Method getMethod = schemaClass.getMethod(getName);
        List<String> fields = (List<String>) getMethod.invoke(schema);
        for (String field : fields)
        {
            Assert.assertTrue(field + " should be found in list", Arrays.binarySearch(svalue, field) >= 0);
        }
    }

    protected void testGetSetDateListValue(String tp) throws Exception
    {
        String setName = addToValueMethod(property);
        String getName = getValueMethod(property);
        Calendar[] svalue = (Calendar[]) value;
        Arrays.sort(svalue);
        // push all
        Method setMethod = schemaClass.getMethod(setName, Calendar.class);
        for (Calendar inst : svalue)
        {
            setMethod.invoke(schema, inst);
        }
        // retrieve
        Method getMethod = schemaClass.getMethod(getName);
        List<Calendar> fields = (List<Calendar>) getMethod.invoke(schema);
        for (Calendar field : fields)
        {
            Assert.assertTrue(field + " should be found in list", Arrays.binarySearch(svalue, field) >= 0);
        }
    }

    protected void testGetSetThumbnail() throws Exception
    {
        String addName = addMethod(property);
        String getName = getMethod(property);
        Method setMethod = schemaClass.getMethod(addName, Integer.class, Integer.class, String.class, String.class);
        Method getMethod = schemaClass.getMethod(getName);
        Integer height = 162;
        Integer width = 400;
        String format = "JPEG";
        String img = "/9j/4AAQSkZJRgABAgEASABIAAD";
        setMethod.invoke(schema, height, width, format, img);
        List<ThumbnailType> found = ((List<ThumbnailType>) getMethod.invoke(schema));
        Assert.assertTrue(found.size() == 1);
        ThumbnailType t1 = found.get(0);
        Assert.assertEquals(height, t1.getHeight());
        Assert.assertEquals(width, t1.getWidth());
        Assert.assertEquals(format, t1.getFormat());
        Assert.assertEquals(img, t1.getImage());

    }

    protected void testGetSetLangAltValue() throws Exception
    {
        String setName = addToValueMethod(property);
        String getName = getValueMethod(property);
        Map<String, String> svalue = (Map<String, String>) value;
        // push all
        Method setMethod = schemaClass.getMethod(setName, String.class, String.class);

        for (Map.Entry<String, String> inst : svalue.entrySet())
        {
            setMethod.invoke(schema, inst.getKey(), inst.getValue());
        }
        // retrieve
        String getLanguagesName = "get" + firstUpper(property) + "Languages";
        Method getLanguages = schemaClass.getMethod(getLanguagesName);
        List<String> lgs = (List<String>) getLanguages.invoke(schema);
        for (String string : lgs)
        {
            Method getMethod = schemaClass.getMethod(getName, String.class);
            String res = (String) getMethod.invoke(schema, string);
            Assert.assertEquals(res, svalue.get(string));
        }
    }

    protected void testGetSetURLValue() throws Exception
    {
        String setName = addToValueMethod(property);
        String getName = getValueMethod(property);
        String svalue = (String) value;
        // push all
        Method setMethod = schemaClass.getMethod(setName, String.class, String.class);
        setMethod.invoke(schema, property, svalue);

        // retrieve
        String getLanguagesName = "get" + firstUpper(property) + "Languages";
        Method getLanguages = schemaClass.getMethod(getLanguagesName);
        List<String> lgs = (List<String>) getLanguages.invoke(schema);
        for (String string : lgs)
        {
            Method getMethod = schemaClass.getMethod(getName, String.class);
            String res = (String) getMethod.invoke(schema, string);
            Assert.assertEquals(res, svalue);
        }
    }

    protected void testGetSetTextValue() throws Exception
    {
        String setName = setValueMethod(property);
        String getName = getValueMethod(property);

        Method setMethod = schemaClass.getMethod(setName, String.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        String found = (String) getMethod.invoke(schema);

        Assert.assertEquals(value, found);
    }

    protected void testGetSetBooleanValue() throws Exception
    {
        String setName = setValueMethod(property);
        String getName = getValueMethod(property);

        Method setMethod = schemaClass.getMethod(setName, Boolean.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        Boolean found = (Boolean) getMethod.invoke(schema);

        Assert.assertEquals(value, found);
    }

    protected void testGetSetDateValue() throws Exception
    {
        String setName = setValueMethod(property);
        String getName = getValueMethod(property);

        Method setMethod = schemaClass.getMethod(setName, Calendar.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        Calendar found = (Calendar) getMethod.invoke(schema);

        Assert.assertEquals(value, found);
    }

    protected void testGetSetIntegerValue() throws Exception
    {
        String setName = setValueMethod(property);
        String getName = getValueMethod(property);

        Method setMethod = schemaClass.getMethod(setName, Integer.class);
        Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        Integer found = (Integer) getMethod.invoke(schema);

        Assert.assertEquals(value, found);
    }
}
