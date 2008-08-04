/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pdfbox.pdmodel.interactive.annotation;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

/**
 * This is the class that represents a widget.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDAnnotationWidget extends PDAnnotation
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Widget";
    
    
    /**
     * Constructor.
     */
    public PDAnnotationWidget()
    {
        super();
        getDictionary().setName( COSName.SUBTYPE, SUB_TYPE);
    }


    /**
     * Creates a PDWidget from a COSDictionary, expected to be
     * a correct object definition for a field in PDF.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationWidget(COSDictionary field)
    {
        super( field );
    }
}