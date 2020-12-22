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

import org.junit.jupiter.api.Test;

class AttributeTest
{

    @Test
    void testAtt()
    {
        final String nsUri = "nsUri";
        final String localName = "localName";
        final String value = "value";

        final Attribute att = new Attribute(nsUri, localName, value);

        assertEquals(nsUri, att.getNamespace());
        assertEquals(localName, att.getName());
        assertEquals(value, att.getValue());

        final String nsUri2 = "nsUri2";
        final String localName2 = "localName2";
        final String value2 = "value2";

        att.setNsURI(nsUri2);
        att.setName(localName2);
        att.setValue(value2);

        assertEquals(nsUri2, att.getNamespace());
        assertEquals(localName2, att.getName());
        assertEquals(value2, att.getValue());

    }

    @Test
    void testAttWithoutPrefix()
    {
        final String nsUri = "nsUri";
        final String localName = "localName";
        final String value = "value";

        Attribute att = new Attribute(nsUri, localName, value);

        assertEquals(nsUri, att.getNamespace());
        assertEquals(localName, att.getName());

        att = new Attribute(nsUri, localName, value);
        assertEquals(nsUri, att.getNamespace());
        assertEquals(localName, att.getName());
    }
}
