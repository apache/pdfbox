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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class TestValidatePermitedMetadata
{

    static Collection<Object[]> initializeParameters() throws Exception
    {
        final List<Object[]> params = new ArrayList<>();
        final InputStream is =  TestValidatePermitedMetadata.class.getResourceAsStream("/permited_metadata.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
        String line = reader.readLine();
        while (line!=null)
        {
            if (line.startsWith("http://"))
            {
                // this is a line to handle
                final int pos = line.lastIndexOf(':');
                final int spos = line.lastIndexOf('/',pos);
                final String namespace = line.substring(0,spos+1);
                final String preferred = line.substring(spos+1,pos);
                final String fieldname = line.substring(pos+1);
                params.add(new String [] {namespace, preferred, fieldname});
            } // else skip line
            // next line
            line = reader.readLine();
        }
        return params;
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void checkExistence(final String namespace, final String preferred, final String fieldname) throws Exception
    {
        // ensure schema exists
        final XMPMetadata xmpmd = new XMPMetadata();
        final TypeMapping mapping = new TypeMapping(xmpmd);
        final XMPSchemaFactory factory = mapping.getSchemaFactory(namespace);
        assertNotNull(factory, "Schema not existing: " + namespace);
        // ensure preferred is as expected
        final XMPSchema schema = factory.createXMPSchema(xmpmd,"aa");
        assertEquals(preferred,schema.getPreferedPrefix());
        // ensure field is defined
        boolean found = false;
        final Class<?> clz  = schema.getClass();
        for (final Field dfield : clz.getDeclaredFields())
        {
            final PropertyType ptype = dfield.getAnnotation(PropertyType.class);
            if (ptype!=null)
            {
                // is a field definition
                if (String.class.equals(dfield.getType()))
                {
                    final String value = (String) dfield.get(clz);
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
        final String msg = String.format("Did not find field definition for '%s' in %s (%s)",
                fieldname,clz.getSimpleName(),namespace);
        assertTrue(found, msg);
    }

}
