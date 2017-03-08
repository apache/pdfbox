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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This is the class that represents a rubber stamp annotation. Introduced in PDF 1.3 specification
 *
 * @author Paul King
 */
public class PDAnnotationRubberStamp extends PDAnnotationMarkup
{

    /*
     * The various values of the rubber stamp as defined in the PDF 1.6 reference Table 8.28
     */

    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_APPROVED = "Approved";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_EXPERIMENTAL = "Experimental";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_NOT_APPROVED = "NotApproved";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_AS_IS = "AsIs";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_EXPIRED = "Expired";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_NOT_FOR_PUBLIC_RELEASE = "NotForPublicRelease";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FOR_PUBLIC_RELEASE = "ForPublicRelease";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_DRAFT = "Draft";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FOR_COMMENT = "ForComment";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_TOP_SECRET = "TopSecret";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_DEPARTMENTAL = "Departmental";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_CONFIDENTIAL = "Confidential";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_FINAL = "Final";
    /**
     * Constant for the name of a rubber stamp.
     */
    public static final String NAME_SOLD = "Sold";

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Stamp";

    /**
     * Constructor.
     */
    public PDAnnotationRubberStamp()
    {
        getCOSObject().setItem(COSName.SUBTYPE, COSName.getPDFName(SUB_TYPE));
    }

    /**
     * Creates a Rubber Stamp annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF objet to represent as a field.
     */
    public PDAnnotationRubberStamp(COSDictionary field)
    {
        super(field);
    }

    /**
     * This will set the name (and hence appearance, AP taking precedence) For this annotation. See the NAME_XXX
     * constants for valid values.
     *
     * @param name The name of the rubber stamp.
     */
    public void setName(String name)
    {
        getCOSObject().setName(COSName.NAME, name);
    }

    /**
     * This will retrieve the name (and hence appearance, AP taking precedence) For this annotation. The default is
     * DRAFT.
     *
     * @return The name of this rubber stamp, see the NAME_XXX constants.
     */
    public String getName()
    {
        return getCOSObject().getNameAsString(COSName.NAME, NAME_DRAFT);
    }
}
