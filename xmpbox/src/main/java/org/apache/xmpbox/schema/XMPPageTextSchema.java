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
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

/**
 * Created with IntelliJ IDEA.
 * User: yugui
 * Date: 07/12/13
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
@StructuredType(preferedPrefix = "xmpTPg",namespace = "http://ns.adobe.com/xap/1.0/t/pg/")
public class XMPPageTextSchema extends XMPSchema
{

    // The size of the largest page in the document
    @PropertyType(type = Types.Dimensions)
    public static final String MAX_PAGE_SIZE = "MaxPageSize";

    // The number of pages in the document
    @PropertyType(type = Types.Integer)
    public static final String N_PAGES = "NPages";

    // An ordered array of plate names that are needed to print the document
    @PropertyType(type = Types.Text, card = Cardinality.Seq)
    public static final String PLATENAMES = "PlateNames"; // Ordered array of Text

    // An ordered array of colorants (swatches) that are used in the document
    @PropertyType(type = Types.Colorant, card = Cardinality.Seq)
    public static final String COLORANTS = "Colorants"; // Ordered array of Colorants

    // An unordered array of fonts that are used in the document
    @PropertyType(type = Types.Font, card = Cardinality.Bag)
    public static final String FONTS = "Fonts"; // Unordered array of Fonts

    public XMPPageTextSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public XMPPageTextSchema(XMPMetadata metadata, String prefix)
    {
        super(metadata, prefix);
    }
}
