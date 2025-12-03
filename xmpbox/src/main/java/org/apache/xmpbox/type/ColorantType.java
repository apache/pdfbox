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

import org.apache.xmpbox.XMPMetadata;

/**
 */
@StructuredType(preferedPrefix = "xmpG",namespace = "http://ns.adobe.com/xap/1.0/g/")
public class ColorantType extends AbstractStructuredType
{

    @PropertyType(type = Types.Integer)
    public static final String A = "A";

    @PropertyType(type = Types.Integer)
    public static final String B = "B";

    @PropertyType(type = Types.Real)
    public static final String L = "L";

    @PropertyType(type = Types.Real)
    public static final String BLACK = "black";

    @PropertyType(type = Types.Real)
    public static final String CYAN = "cyan";

    @PropertyType(type = Types.Real)
    public static final String MAGENTA = "magenta";

    @PropertyType(type = Types.Real)
    public static final String YELLOW = "yellow";

    @PropertyType(type = Types.Integer)
    public static final String BLUE = "blue";

    @PropertyType(type = Types.Integer)
    public static final String GREEN = "green";

    @PropertyType(type = Types.Integer)
    public static final String RED = "red";

    @PropertyType(type = Types.Choice, card = Cardinality.Simple)
    public static final String MODE = "mode";

    @PropertyType(type = Types.Text)
    public static final String SWATCH_NAME = "swatchName";

    @PropertyType(type = Types.Choice, card = Cardinality.Simple)
    public static final String TYPE = "type";

    public ColorantType(XMPMetadata metadata)
    {
        super(metadata);
    }
}
