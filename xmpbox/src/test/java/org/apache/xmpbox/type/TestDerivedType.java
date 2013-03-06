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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.xmpbox.XMPMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestDerivedType
{

    public static final String PREFIX = "myprefix";

    public static final String NAME = "myname";

    public static final String VALUE = "myvalue";

    protected XMPMetadata xmp;

    protected String type = null;

    protected Class<? extends TextType> clz = null;

    protected Constructor<? extends TextType> constructor = null;

    public TestDerivedType(Class<? extends TextType> clz, String type)
    {
        super();
        this.clz = clz;
        this.type = type;
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        Collection<Object[]> result = new ArrayList<Object[]>();

        result.add(new Object[] { AgentNameType.class, "AgentName" });
        result.add(new Object[] { ChoiceType.class, "Choice" });
        result.add(new Object[] { GUIDType.class, "GUID" });
        result.add(new Object[] { LocaleType.class, "Locale" });
        result.add(new Object[] { MIMEType.class, "MIME" });
        result.add(new Object[] { PartType.class, "Part" });
        result.add(new Object[] { ProperNameType.class, "ProperName" });
        result.add(new Object[] { RenditionClassType.class, "RenditionClass" });
        result.add(new Object[] { URIType.class, "URI" });
        result.add(new Object[] { URLType.class, "URL" });
        result.add(new Object[] { XPathType.class, "XPath" });

        return result;

    }

    @Before
    public void before() throws Exception
    {
        xmp = XMPMetadata.createXMPMetadata();
        constructor = clz.getConstructor(new Class[] { XMPMetadata.class, String.class, String.class, String.class,
                Object.class });
    }

    protected TextType instanciate(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName,
            Object value) throws Exception
    {
        Object[] initargs = new Object[] { metadata, namespaceURI, prefix, propertyName, value };
        return constructor.newInstance(initargs);
    }

    @Test
    public void test1() throws Exception
    {
        TextType element = instanciate(xmp, null, PREFIX, NAME, VALUE);
        Assert.assertNull(element.getNamespace());
        Assert.assertTrue(element.getValue() instanceof String);
        Assert.assertEquals(VALUE, element.getValue());

    }

}
