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

import java.util.Calendar;

import junit.framework.Assert;

import org.apache.xmpbox.XMPMetadata;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractStructuredType
{

    private static class MyStructuredType extends AbstractStructuredType
    {

        @PropertyType(type = Types.Text, card = Cardinality.Simple)
        public static final String MYTEXT = "my-text";

        @PropertyType(type = Types.Date, card = Cardinality.Simple)
        public static final String MYDATE = "my-date";

        public MyStructuredType(XMPMetadata metadata, String namespaceURI, String fieldPrefix)
        {
            super(metadata, namespaceURI, fieldPrefix, "structuredPN");
        }

    }

    protected MyStructuredType st;

    public static final String MY_NS = "http://www.apache.org/test#";

    public static final String MY_PREFIX = "test";

    @Before
    public void before() throws Exception
    {
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();
        st = new MyStructuredType(xmp, MY_NS, MY_PREFIX);
    }

    @Test
    public void validate() throws Exception
    {
        Assert.assertEquals(MY_NS, st.getNamespace());
        Assert.assertEquals(MY_PREFIX, st.getPrefix());
        Assert.assertEquals(MY_PREFIX, st.getPrefix());
    }

    @Test
    public void testNonExistingProperty() throws Exception
    {
        Assert.assertNull(st.getProperty("NOT_EXISTING"));
    }

    @Test
    public void testNotValuatedPropertyProperty() throws Exception
    {
        Assert.assertNull(st.getProperty(MyStructuredType.MYTEXT));
    }

    @Test
    public void testValuatedTextProperty() throws Exception
    {
        String s = "my value";
        st.addSimpleProperty(MyStructuredType.MYTEXT, s);
        Assert.assertEquals(s, st.getPropertyValueAsString(MyStructuredType.MYTEXT));
        Assert.assertNull(st.getPropertyValueAsString(MyStructuredType.MYDATE));
        Assert.assertNotNull(st.getProperty(MyStructuredType.MYTEXT));
    }

    @Test
    public void testValuatedDateProperty() throws Exception
    {
        Calendar c = Calendar.getInstance();
        st.addSimpleProperty(MyStructuredType.MYDATE, c);
        Assert.assertEquals(c, st.getDatePropertyAsCalendar(MyStructuredType.MYDATE));
        Assert.assertNull(st.getDatePropertyAsCalendar(MyStructuredType.MYTEXT));
        Assert.assertNotNull(st.getProperty(MyStructuredType.MYDATE));
    }

}
