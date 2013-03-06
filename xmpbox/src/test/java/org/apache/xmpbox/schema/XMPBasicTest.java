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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class XMPBasicTest extends AbstractXMPSchemaTest
{

    public XMPBasicTest(String prop, PropertyType type, Object val)
    {
        super(prop, type, val);
    }

    @Before
    public void initTempMetaData() throws Exception
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddXMPBasicSchema();
        schemaClass = XMPBasicSchema.class;
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        List<Object[]> data = new ArrayList<Object[]>();

        data.add(wrapProperty("Advisory", Types.XPath, Cardinality.Bag, new String[] { "xpath1", "xpath2" }));
        data.add(wrapProperty("BaseURL", Types.URL, "URL"));
        data.add(wrapProperty("CreateDate", Types.Date, Calendar.getInstance()));
        data.add(wrapProperty("CreatorTool", Types.AgentName, "CreatorTool"));
        data.add(wrapProperty("Identifier", Types.Text, Cardinality.Bag, new String[] { "id1", "id2" }));
        data.add(wrapProperty("Label", Types.Text, "label"));
        data.add(wrapProperty("MetadataDate", Types.Date, Calendar.getInstance()));
        data.add(wrapProperty("ModifyDate", Types.Date, Calendar.getInstance()));
        data.add(wrapProperty("Nickname", Types.Text, "nick name"));
        data.add(wrapProperty("Rating", Types.Integer, 7));

        data.add(wrapProperty("Thumbnails", Types.Thumbnail, Cardinality.Alt, null));

        return data;
    }

}
