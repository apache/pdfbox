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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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

class XMPSchemaTester
{
    private final XMPMetadata metadata;
    private final XMPSchema schema;
    private final Class<?> schemaClass;
    private final String property;
    private final PropertyType type;
    private final Object value;

    public XMPSchemaTester(final XMPMetadata metadata, final XMPSchema schema, final Class<?> schemaClass, final String property, final PropertyType type, final Object value)
    {
        this.metadata = metadata;
        this.schema = schema;
        this.schemaClass = schemaClass;
        this.property = property;
        this.type = type;
        this.value = value;
    }

    public static PropertyType createPropertyType(final Types type)
    {
        return TypeMapping.createPropertyType(type, Cardinality.Simple);
    }

    public static PropertyType createPropertyType(final Types type, final Cardinality card)
    {
        return TypeMapping.createPropertyType(type, card);
    }

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
        final Field[] fields = schemaClass.getFields();
        for (final Field field : fields)
        {
            if (field.isAnnotationPresent(PropertyType.class))
            {
                if (!field.get(schema).equals(property))
                {
                    final PropertyType pt = field.getAnnotation(PropertyType.class);
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
                        final PropertyType spt = retrievePropertyType(field.get(schema).toString());
                        final String getNameProperty = "get" + prepareName(field.get(schema).toString(), spt) + "Property";
                        Method getMethod = schemaClass.getMethod(getNameProperty);
                        assertNull(getMethod.invoke(schema), getNameProperty + " should return null when testing " + property);
                        // value test
                        final String getNameValue = "get" + prepareName(field.get(schema).toString(), spt);
                        getMethod = schemaClass.getMethod(getNameValue);
                        assertNotNull(getMethod, getNameValue + " method should exist");
                        assertNull(getMethod.invoke(schema), getNameValue + " should return null when testing " + property);
                    }
                }
            }
        }
    }

    protected PropertyType retrievePropertyType(final String prop) throws IllegalArgumentException, IllegalAccessException
    {
        final Field[] fields = schemaClass.getFields();
        for (final Field field : fields)
        {
            if (field.isAnnotationPresent(PropertyType.class))
            {
                final PropertyType pt = field.getAnnotation(PropertyType.class);
                if (field.get(schema).equals(prop))
                {
                    return pt;
                }
            }
        }
        return type;
    }

    protected String firstUpper(final String name)
    {
        final StringBuilder sb = new StringBuilder(name.length());
        sb.append(name.substring(0, 1).toUpperCase());
        sb.append(name.substring(1));
        return sb.toString();
    }

    protected String prepareName(final String prop, final PropertyType type)
    {
        final String fu = firstUpper(prop);
        final StringBuilder sb = new StringBuilder(fu.length() + 1);
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

    protected String setMethod(final String prop)
    {
        final StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("set").append(prepareName(prop, type)).append("Property");
        return sb.toString();
    }

    protected String addMethod(final String prop)
    {
        final String fu = firstUpper(prop);
        final StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("add").append(fu);
        return sb.toString();
    }

    protected String getMethod(final String prop)
    {
        final String fu = firstUpper(prop);
        final StringBuilder sb = new StringBuilder(3 + prop.length());
        sb.append("get").append(fu).append("Property");
        return sb.toString();
    }

    protected String setValueMethod(final String prop)
    {
        final String fu = firstUpper(prop);
        final StringBuilder sb = new StringBuilder(8 + prop.length());
        sb.append("set").append(fu);
        return sb.toString();
    }

    protected String getValueMethod(final String prop)
    {
        final StringBuilder sb = new StringBuilder(8 + prop.length());
        sb.append("get").append(prepareName(prop, type));
        return sb.toString();
    }

    protected String addToValueMethod(final String prop)
    {
        final String fu = firstUpper(prop);
        final StringBuilder sb = new StringBuilder(10 + prop.length());
        sb.append("add").append(fu);
        return sb.toString();

    }

    protected void testGetSetBooleanProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final BooleanType bt = new BooleanType(metadata, null, schema.getPrefix(), property, value);
        final Method setMethod = schemaClass.getMethod(setName, BooleanType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, bt);
        final Boolean found = ((BooleanType) getMethod.invoke(schema)).getValue();
        assertEquals(value, found);

    }

    protected void testGetSetDateProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final DateType dt = new DateType(metadata, null, schema.getPrefix(), property, value);
        final Method setMethod = schemaClass.getMethod(setName, DateType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, dt);
        final Calendar found = ((DateType) getMethod.invoke(schema)).getValue();
        assertEquals(value, found);
    }

    protected void testGetSetIntegerProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final IntegerType it = new IntegerType(metadata, null, schema.getPrefix(), property, value);
        final Method setMethod = schemaClass.getMethod(setName, IntegerType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, it);
        final Integer found = ((IntegerType) getMethod.invoke(schema)).getValue();
        assertEquals(value, found);
    }

    protected void testGetSetTextProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final TextType tt = metadata.getTypeMapping().createText(null, schema.getPrefix(), property, (String) value);
        final Method setMethod = schemaClass.getMethod(setName, TextType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        final String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        assertEquals(value, found);

    }

    protected void testGetSetURIProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final URIType tt = metadata.getTypeMapping().createURI(null, schema.getPrefix(), property, (String) value);
        final Method setMethod = schemaClass.getMethod(setName, URIType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        final String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        assertEquals(value, found);

    }

    protected void testGetSetURLProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final URLType tt = metadata.getTypeMapping().createURL(null, schema.getPrefix(), property, (String) value);
        final Method setMethod = schemaClass.getMethod(setName, URLType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        final String found = ((TextType) getMethod.invoke(schema)).getStringValue();
        assertEquals(value, found);

    }

    protected void testGetSetAgentNameProperty() throws Exception
    {
        final String setName = setMethod(property);
        final String getName = getMethod(property);

        final AgentNameType tt = metadata.getTypeMapping()
                .createAgentName(null, schema.getPrefix(), property, (String) value);
        final Method setMethod = schemaClass.getMethod(setName, AgentNameType.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, tt);
        final String found = ((AgentNameType) getMethod.invoke(schema)).getStringValue();
        assertEquals(value, found);

    }

    protected void testGetSetTextListValue(final String tp) throws Exception
    {
        final String setName = addToValueMethod(property);
        final String getName = getValueMethod(property);
        final String[] svalue = (String[]) value;
        Arrays.sort(svalue);
        // push all
        final Method setMethod = schemaClass.getMethod(setName, String.class);
        for (final String string : svalue)
        {
            setMethod.invoke(schema, string);
        }
        // retrieve
        final Method getMethod = schemaClass.getMethod(getName);
        final List<String> fields = (List<String>) getMethod.invoke(schema);
        for (final String field : fields)
        {
            assertTrue(Arrays.binarySearch(svalue, field) >= 0, field + " should be found in list");
        }
    }

    protected void testGetSetDateListValue(final String tp) throws Exception
    {
        final String setName = addToValueMethod(property);
        final String getName = getValueMethod(property);
        final Calendar[] svalue = (Calendar[]) value;
        Arrays.sort(svalue);
        // push all
        final Method setMethod = schemaClass.getMethod(setName, Calendar.class);
        for (final Calendar inst : svalue)
        {
            setMethod.invoke(schema, inst);
        }
        // retrieve
        final Method getMethod = schemaClass.getMethod(getName);
        final List<Calendar> fields = (List<Calendar>) getMethod.invoke(schema);
        for (final Calendar field : fields)
        {
            assertTrue(Arrays.binarySearch(svalue, field) >= 0, field + " should be found in list");
        }
    }

    protected void testGetSetThumbnail() throws Exception
    {
        final String addName = addMethod(property);
        final String getName = getMethod(property);
        final Method setMethod = schemaClass.getMethod(addName, Integer.class, Integer.class, String.class, String.class);
        final Method getMethod = schemaClass.getMethod(getName);
        final Integer height = 162;
        final Integer width = 400;
        final String format = "JPEG";
        final String img = "/9j/4AAQSkZJRgABAgEASABIAAD";
        setMethod.invoke(schema, height, width, format, img);
        final List<ThumbnailType> found = ((List<ThumbnailType>) getMethod.invoke(schema));
        assertEquals(1, found.size());
        final ThumbnailType t1 = found.get(0);
        assertEquals(height, t1.getHeight());
        assertEquals(width, t1.getWidth());
        assertEquals(format, t1.getFormat());
        assertEquals(img, t1.getImage());
    }

    protected void testGetSetLangAltValue() throws Exception
    {
        final String setName = addToValueMethod(property);
        final String getName = getValueMethod(property);
        final Map<String, String> svalue = (Map<String, String>) value;
        // push all
        final Method setMethod = schemaClass.getMethod(setName, String.class, String.class);

        for (final Map.Entry<String, String> inst : svalue.entrySet())
        {
            setMethod.invoke(schema, inst.getKey(), inst.getValue());
        }
        // retrieve
        final String getLanguagesName = "get" + firstUpper(property) + "Languages";
        final Method getLanguages = schemaClass.getMethod(getLanguagesName);
        final List<String> lgs = (List<String>) getLanguages.invoke(schema);
        for (final String string : lgs)
        {
            final Method getMethod = schemaClass.getMethod(getName, String.class);
            final String res = (String) getMethod.invoke(schema, string);
            assertEquals(res, svalue.get(string));
        }
    }

    protected void testGetSetURLValue() throws Exception
    {
        final String setName = addToValueMethod(property);
        final String getName = getValueMethod(property);
        final String svalue = (String) value;
        // push all
        final Method setMethod = schemaClass.getMethod(setName, String.class, String.class);
        setMethod.invoke(schema, property, svalue);

        // retrieve
        final String getLanguagesName = "get" + firstUpper(property) + "Languages";
        final Method getLanguages = schemaClass.getMethod(getLanguagesName);
        final List<String> lgs = (List<String>) getLanguages.invoke(schema);
        for (final String string : lgs)
        {
            final Method getMethod = schemaClass.getMethod(getName, String.class);
            final String res = (String) getMethod.invoke(schema, string);
            assertEquals(res, svalue);
        }
    }

    protected void testGetSetTextValue() throws Exception
    {
        final String setName = setValueMethod(property);
        final String getName = getValueMethod(property);

        final Method setMethod = schemaClass.getMethod(setName, String.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        final String found = (String) getMethod.invoke(schema);

        assertEquals(value, found);
    }

    protected void testGetSetBooleanValue() throws Exception
    {
        final String setName = setValueMethod(property);
        final String getName = getValueMethod(property);

        final Method setMethod = schemaClass.getMethod(setName, Boolean.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        final Boolean found = (Boolean) getMethod.invoke(schema);

        assertEquals(value, found);
    }

    protected void testGetSetDateValue() throws Exception
    {
        final String setName = setValueMethod(property);
        final String getName = getValueMethod(property);

        final Method setMethod = schemaClass.getMethod(setName, Calendar.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        final Calendar found = (Calendar) getMethod.invoke(schema);

        assertEquals(value, found);
    }

    protected void testGetSetIntegerValue() throws Exception
    {
        final String setName = setValueMethod(property);
        final String getName = getValueMethod(property);

        final Method setMethod = schemaClass.getMethod(setName, Integer.class);
        final Method getMethod = schemaClass.getMethod(getName);

        setMethod.invoke(schema, value);
        final Integer found = (Integer) getMethod.invoke(schema);

        assertEquals(value, found);
    }
}
