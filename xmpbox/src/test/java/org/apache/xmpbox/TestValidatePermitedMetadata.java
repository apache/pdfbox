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
package org.apache.xmpbox;

import org.apache.xmpbox.schema.XMPSchema;

import org.apache.xmpbox.schema.XMPSchemaFactory;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.TypeMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestValidatePermitedMetadata
{

    @Parameters(name="{0} {1} {2}")
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        List<Object[]> params = new ArrayList<>();
        InputStream is =  TestValidatePermitedMetadata.class.getResourceAsStream("/permited_metadata.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
        String line = reader.readLine();
        while (line!=null)
        {
            if (line.startsWith("http://"))
            {
                // this is a line to handle
                int pos = line.lastIndexOf(':');
                int spos = line.lastIndexOf('/',pos);
                String namespace = line.substring(0,spos+1);
                String preferred = line.substring(spos+1,pos);
                String fieldname = line.substring(pos+1);
                params.add(new String [] {namespace, preferred, fieldname});
            } // else skip line
            // next line
            line = reader.readLine();
        }
        return params;
    }

    private final String namespace;

    private final String fieldname;

    private final String preferred;

    public TestValidatePermitedMetadata(String ns, String prf, String fn)
    {
        this.namespace = ns;
        this.preferred = prf;
        this.fieldname = fn;
    }
    @Test
    public void checkExistence() throws Exception
    {
        // ensure schema exists
        XMPMetadata xmpmd = new XMPMetadata();
        TypeMapping mapping = new TypeMapping(xmpmd);
        XMPSchemaFactory factory = mapping.getSchemaFactory(namespace);
        assertNotNull("Schema not existing: " + namespace, factory);
        // ensure preferred is as expected
        XMPSchema schema = factory.createXMPSchema(xmpmd,"aa");
        assertEquals(preferred,schema.getPreferedPrefix());
        // ensure field is defined
        boolean found = false;
        Class<?> clz  = schema.getClass();
        for (Field dfield : clz.getDeclaredFields())
        {
            PropertyType ptype = dfield.getAnnotation(PropertyType.class);
            if (ptype!=null)
            {
                // is a field definition
                if (String.class.equals(dfield.getType()))
                {
                    String value = (String) dfield.get(clz);
                    if (fieldname.equals(value))
                    {
                        // found the field defining
                        found = true;
                        break;
                    }
                }
                else
                {
                    // All field declaration are string
                    throw new IllegalArgumentException("Should be a string : "+dfield.getName());
                }


            }
        }
        String msg = String.format("Did not find field definition for '%s' in %s (%s)",
                fieldname,clz.getSimpleName(),namespace);
        assertTrue(msg,found);
    }

}
