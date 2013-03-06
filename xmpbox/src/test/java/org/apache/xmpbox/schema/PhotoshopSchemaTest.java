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

package org.apache.xmpbox.schema;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.Types;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PhotoshopSchemaTest extends AbstractSchemaTester
{

    protected PhotoshopSchema schema = null;

    public PhotoshopSchema getSchema()
    {
        return schema;
    }

    @Before
    public void before() throws Exception
    {
        super.before();
        schema = xmp.createAndAddPhotoshopSchema();
    }

    public PhotoshopSchemaTest(String fieldName, Types type, Cardinality card)
    {
        super(fieldName, type, card);
    }

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        Collection<Object[]> result = new ArrayList<Object[]>();

        result.add(new Object[] { "AncestorID", Types.URI, Cardinality.Simple });
        result.add(new Object[] { "AuthorsPosition", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "CaptionWriter", Types.ProperName, Cardinality.Simple });
        result.add(new Object[] { "Category", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "City", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "ColorMode", Types.Integer, Cardinality.Simple });
        result.add(new Object[] { "Country", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "Credit", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "DateCreated", Types.Date, Cardinality.Simple });
        result.add(new Object[] { "Headline", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "History", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "ICCProfile", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "Instructions", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "Source", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "State", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "SupplementalCategories", Types.Text, Cardinality.Bag });
        result.add(new Object[] { "TransmissionReference", Types.Text, Cardinality.Simple });
        result.add(new Object[] { "Urgency", Types.Integer, Cardinality.Simple });

        return result;
    }

}
