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

package org.apache.xmpbox.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;
import org.apache.xmpbox.XMPMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


class TestDerivedType
{

    public static final String PREFIX = "myprefix";

    public static final String NAME = "myname";

    public static final String VALUE = "myvalue";

    protected XMPMetadata xmp;

    protected String type = null;

    protected Class<? extends TextType> clz = null;

    protected Constructor<? extends TextType> constructor = null;

    @BeforeEach
    public void before() throws Exception
    {
        xmp = XMPMetadata.createXMPMetadata();
    }

    private static Stream<Arguments> initializeParameters()
    {
        return Stream.of(
            // data for JobType
            Arguments.of(AgentNameType.class, "AgentName"),
            Arguments.of(ChoiceType.class, "Choice"),
            Arguments.of(GUIDType.class, "GUID"),
            Arguments.of(LocaleType.class, "Locale"),
            Arguments.of(MIMEType.class, "MIME"),
            Arguments.of(PartType.class, "Part"),
            Arguments.of(ProperNameType.class, "ProperName"),
            Arguments.of(RenditionClassType.class, "RenditionClass"),
            Arguments.of(URIType.class, "URI"),
            Arguments.of(URLType.class, "URL"),
            Arguments.of(XPathType.class, "XPath")
        );
    }

    protected TextType instanciate(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName,
            Object value) throws Exception
    {
        Object[] initargs = { metadata, namespaceURI, prefix, propertyName, value };
        return constructor.newInstance(initargs);
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void test1(Class<? extends TextType> clz, String type) throws Exception
    {
        constructor = clz.getDeclaredConstructor(XMPMetadata.class, String.class, String.class, String.class, Object.class);
        TextType element = instanciate(xmp, null, PREFIX, NAME, VALUE);
        assertNull(element.getNamespace());
        assertTrue(element.getValue() instanceof String);
        assertEquals(VALUE, element.getValue());
    }

}
