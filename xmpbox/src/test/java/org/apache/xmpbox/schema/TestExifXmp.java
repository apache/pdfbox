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

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.JobType;
import org.apache.xmpbox.type.OECFType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

public class TestExifXmp
{



    @Test
    public void testNonStrict() throws Exception
    {
        InputStream is = this.getClass().getResourceAsStream("/validxmp/exif.xmp");

        DomXmpParser builder = new DomXmpParser();
        builder.setStrictParsing(false);
        XMPMetadata rxmp = builder.parse(is);
        ExifSchema schema = (ExifSchema)rxmp.getSchema(ExifSchema.class);
        TextType ss = (TextType)schema.getProperty(ExifSchema.SPECTRAL_SENSITIVITY);
        Assert.assertNotNull(ss);
        Assert.assertEquals("spectral sens value",ss.getValue());
    }

    @Test
    public void testGenerate () throws Exception {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        TypeMapping tmapping = metadata.getTypeMapping();
        ExifSchema exif = new ExifSchema(metadata);
        metadata.addSchema(exif);
        OECFType oecf = new OECFType(metadata);
        oecf.addProperty(tmapping.createInteger(oecf.getNamespace(), oecf.getPrefix(), OECFType.COLUMNS, 14));
        oecf.setPropertyName(ExifSchema.OECF);
        exif.addProperty(oecf);

        XmpSerializer serializer = new XmpSerializer();
        serializer.serialize(metadata,System.out,false);



    }
}
