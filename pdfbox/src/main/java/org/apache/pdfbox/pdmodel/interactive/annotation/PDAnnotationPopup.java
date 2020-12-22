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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This is the class that represents a popup annotation. Introduced in PDF 1.3 specification
 *
 * @author Paul King
 */
public class PDAnnotationPopup extends PDAnnotation
{
    private static final Log LOG = LogFactory.getLog(PDAnnotationPopup.class);

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Popup";

    /**
     * Constructor.
     */
    public PDAnnotationPopup()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Creates a popup annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    public PDAnnotationPopup(final COSDictionary field)
    {
        super(field);
    }

    /**
     * This will set the initial state of the annotation, open or closed.
     *
     * @param open Boolean value, true = open false = closed.
     */
    public void setOpen(final boolean open)
    {
        getCOSObject().setBoolean("Open", open);
    }

    /**
     * This will retrieve the initial state of the annotation, open Or closed (default closed).
     *
     * @return The initial state, true = open false = closed.
     */
    public boolean getOpen()
    {
        return getCOSObject().getBoolean("Open", false);
    }

    /**
     * This will set the markup annotation which this popup relates to.
     *
     * @param annot the markup annotation.
     */
    public void setParent(final PDAnnotationMarkup annot)
    {
        getCOSObject().setItem(COSName.PARENT, annot.getCOSObject());
    }

    /**
     * This will retrieve the markup annotation which this popup relates to.
     *
     * @return The parent markup annotation.
     */
    public PDAnnotationMarkup getParent()
    {
        PDAnnotationMarkup am = null;
        try
        {
            am = (PDAnnotationMarkup) PDAnnotation.createAnnotation(getCOSObject()
                    .getDictionaryObject(COSName.PARENT, COSName.P));
        }
        catch (final IOException ioe)
        {
            LOG.debug("An exception while trying to get the parent markup - ignoring", ioe);
            // Couldn't construct the annotation, so return null i.e. do nothing
        }
        return am;
    }

}
