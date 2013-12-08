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

public enum Types
{

    Structured(false, null, null), DefinedType(false, null, null),

    // basic
    Text(true, null, TextType.class), Date(true, null, DateType.class), Boolean(true, null, BooleanType.class), Integer(
            true, null, IntegerType.class), Real(true, null, RealType.class),GPSCoordinate(true,Text,TextType.class),

    ProperName(true, Text, ProperNameType.class), Locale(true, Text, LocaleType.class), AgentName(true, Text,
            AgentNameType.class), GUID(true, Text, GUIDType.class), XPath(true, Text, XPathType.class), Part(true,
            Text, PartType.class), URL(true, Text, URLType.class), URI(true, Text, URIType.class), Choice(true, Text,
            ChoiceType.class), MIMEType(true, Text, MIMEType.class), LangAlt(true, Text, TextType.class), RenditionClass(
            true, Text, RenditionClassType.class), Rational(true,Text,RationalType.class),

    Layer(false, Structured, LayerType.class), Thumbnail(false, Structured, ThumbnailType.class), ResourceEvent(false,
            Structured, ResourceEventType.class), ResourceRef(false, Structured, ResourceRefType.class), Version(false,
            Structured, VersionType.class), PDFASchema(false, Structured, PDFASchemaType.class), PDFAField(false,
            Structured, PDFAFieldType.class), PDFAProperty(false, Structured, PDFAPropertyType.class), PDFAType(false,
            Structured, PDFATypeType.class), Job(false, Structured, JobType.class),OECF(false,Structured,
            OECFType.class), CFAPattern(false,Structured, CFAPatternType.class),DeviceSettings(false,Structured,
            DeviceSettingsType.class),Flash(false,Structured,FlashType.class),Dimensions(false,Structured,
            DimensionsType.class);

    // For defined types

    private boolean simple;

    private Types basic;

    private Class<? extends AbstractField> clz;

    private Types(boolean s, Types b, Class<? extends AbstractField> c)
    {
        this.simple = s;
        this.basic = b;
        this.clz = c;
    }

    public boolean isSimple()
    {
        return simple;
    }

    public boolean isBasic()
    {
        return basic == null;
    }

    public boolean isStructured()
    {
        return basic == Structured;
    }

    public boolean isDefined()
    {
        return this == DefinedType;
    }

    public Types getBasic()
    {
        return basic;
    }

    public Class<? extends AbstractField> getImplementingClass()
    {
        return clz;
    }

}
