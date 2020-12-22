/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.OECFType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class TestExifXmp
{
    @Test
    void testNonStrict() throws Exception
    {
        final InputStream is = this.getClass().getResourceAsStream("/validxmp/exif.xmp");

        final DomXmpParser builder = new DomXmpParser();
        builder.setStrictParsing(false);
        final XMPMetadata rxmp = builder.parse(is);
        final ExifSchema schema = (ExifSchema)rxmp.getSchema(ExifSchema.class);
        final TextType ss = (TextType)schema.getProperty(ExifSchema.SPECTRAL_SENSITIVITY);
        assertNotNull(ss);
        assertEquals("spectral sens value",ss.getValue());
    }

    @Test
    void testGenerate() throws Exception
    {
        final XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        final TypeMapping tmapping = metadata.getTypeMapping();
        final ExifSchema exif = new ExifSchema(metadata);
        metadata.addSchema(exif);
        final OECFType oecf = new OECFType(metadata);
        oecf.addProperty(tmapping.createInteger(oecf.getNamespace(), oecf.getPrefix(), OECFType.COLUMNS, 14));
        oecf.setPropertyName(ExifSchema.OECF);
        exif.addProperty(oecf);

        final XmpSerializer serializer = new XmpSerializer();

        serializer.serialize(metadata, new ByteArrayOutputStream(), false);
    }
}
