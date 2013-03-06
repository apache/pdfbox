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

import junit.framework.Assert;

import org.junit.Test;

public class AttributeTest
{

    @Test
    public void testAtt()
    {
        String nsUri = "nsUri";
        String prefix = "prefix";
        String localName = "localName";
        String value = "value";

        Attribute att = new Attribute(nsUri, localName, value);

        Assert.assertEquals(nsUri, att.getNamespace());
        Assert.assertEquals(localName, att.getName());
        Assert.assertEquals(value, att.getValue());

        String nsUri2 = "nsUri2";
        String prefix2 = "prefix2";
        String localName2 = "localName2";
        String value2 = "value2";

        att.setNsURI(nsUri2);
        att.setName(localName2);
        att.setValue(value2);

        Assert.assertEquals(nsUri2, att.getNamespace());
        Assert.assertEquals(localName2, att.getName());
        Assert.assertEquals(value2, att.getValue());

    }

    @Test
    public void testAttWithoutPrefix()
    {
        String nsUri = "nsUri";
        String localName = "localName";
        String value = "value";

        Attribute att = new Attribute(nsUri, localName, value);

        Assert.assertEquals(nsUri, att.getNamespace());
        Assert.assertEquals(localName, att.getName());

        att = new Attribute(nsUri, localName, value);
        Assert.assertEquals(nsUri, att.getNamespace());
        Assert.assertEquals(localName, att.getName());
    }
}
