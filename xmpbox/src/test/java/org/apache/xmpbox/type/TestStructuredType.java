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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.xmpbox.XMPMetadata;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestStructuredType extends AbstractTypeTester
{

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testInitializedToNull(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        assertNull(structured.getProperty(fieldName));
        // accessor
        Method get = clz.getMethod(calculateSimpleGetter(fieldName));
        Object result = get.invoke(structured);
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testSettingValue(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        internalTestSettingValue(structured, clz, fieldName, type);
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomSettingValue(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestSettingValue(structured, clz, fieldName, type);        }
    }

    private void internalTestSettingValue(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        Object value = getJavaValue(type);
        structured.addSimpleProperty(fieldName, value);
        assertNotNull(structured.getProperty(fieldName));
        // check other properties not modified
        List<Field> fields = getXmpFields(clz);
        for (Field field : fields)
        {
            // do not check the current name
            String name = field.get(null).toString();
            if (!name.equals(fieldName))
            {
                assertNull(structured.getProperty(name));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testPropertyType(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        internalTestPropertyType(structured, clz, fieldName, type);
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomPropertyType(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestPropertyType(structured, clz, fieldName, type);
        }
    }


    private void internalTestPropertyType(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        Object value = getJavaValue(type);
        structured.addSimpleProperty(fieldName, value);
        assertNotNull(structured.getProperty(fieldName));
        // check property type
        AbstractSimpleProperty asp = (AbstractSimpleProperty) structured.getProperty(fieldName);
        assertEquals(type.getImplementingClass(), asp.getClass());
    }


    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testSetter(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        internalTestSetter(structured, clz, fieldName, type);
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void testRandomSetter(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        initializeSeed(new Random());
        for (int i=0; i < RAND_LOOP_COUNT;i++)
        {
            internalTestSetter(structured, clz, fieldName, type);
        }
    }

    private void internalTestSetter(AbstractStructuredType structured, Class<? extends AbstractStructuredType> clz, String fieldName, Types type) throws Exception
    {
        String setter = calculateSimpleSetter(fieldName);
        Object value = getJavaValue(type);
        Method set = clz.getMethod(setter, getJavaType(type));
        set.invoke(structured, value);
        // check property set
        assertEquals(value, ((AbstractSimpleProperty) structured.getProperty(fieldName)).getValue());
        // check getter
        Method get = clz.getMethod(calculateSimpleGetter(fieldName));
        Object result = get.invoke(structured);
        assertTrue(getJavaType(type).isAssignableFrom(result.getClass()));
        assertEquals(value, result);
    }

    private static Stream<Arguments> initializeParameters()
    {
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();

        return Stream.of(
            // data for JobType
            Arguments.of(new JobType(xmp, "job"), JobType.class, "id", Types.Text),
            Arguments.of(new JobType(xmp, "job"), JobType.class, "name", Types.Text),
            Arguments.of(new JobType(xmp, "job"), JobType.class, "url", Types.URL),
            // data for LayerType
            Arguments.of(new LayerType(xmp), LayerType.class, "LayerName", Types.Text),
            Arguments.of(new LayerType(xmp), LayerType.class, "LayerText", Types.Text),
            // data for ResourceEventType
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "action", Types.Choice),
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "changed", Types.Text),
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "instanceID", Types.GUID),
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "parameters", Types.Text),
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "softwareAgent", Types.AgentName),
            Arguments.of(new ResourceEventType(xmp), ResourceEventType.class, "when", Types.Date),
            // data for ResourceEventType
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "documentID", Types.URI),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "filePath", Types.URI),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "fromPart", Types.Part),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "instanceID", Types.URI),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "lastModifyDate", Types.Date),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "manager", Types.AgentName),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "managerVariant", Types.Text),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "manageTo", Types.URI),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "manageUI", Types.URI),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "maskMarkers", Types.Choice),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "partMapping", Types.Text),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "renditionClass", Types.RenditionClass),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "renditionParams", Types.Text),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "toPart", Types.Part),
            Arguments.of(new ResourceRefType(xmp), ResourceRefType.class, "versionID", Types.Text),
            // data for ThumbnailType
            Arguments.of(new ThumbnailType(xmp), ThumbnailType.class, "format", Types.Choice),
            Arguments.of(new ThumbnailType(xmp), ThumbnailType.class, "height", Types.Integer),
            Arguments.of(new ThumbnailType(xmp), ThumbnailType.class, "width", Types.Integer),
            Arguments.of(new ThumbnailType(xmp), ThumbnailType.class, "image", Types.Text),
            // data for VersionType
            Arguments.of(new VersionType(xmp), VersionType.class, "modifier", Types.ProperName) 
        );
    }
}
