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
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;

/**
 * This is the abstract class that represents a text markup annotation introduced in the PDF 1.3
 * specification, except Squiggly lines in 1.4.
 *
 * @author Paul King
 */
public class PDAnnotationTextMarkup extends PDAnnotationMarkup
{
    /**
     * Creates a TextMarkup annotation of the specified sub type.
     *
     * @param subType the subtype the annotation represents
     */
    protected PDAnnotationTextMarkup(String subType)
    {
        setSubtype(subType);

        // Quad points are required, set an empty array
        setQuadPoints(new float[0]);
    }

    /**
     * Creates a TextMarkup annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    protected PDAnnotationTextMarkup(COSDictionary field)
    {
        super(field);
    }

    /**
     * This will set the set of quadpoints which encompass the areas of this annotation.
     *
     * @param quadPoints an array representing the set of area covered
     */
    public final void setQuadPoints(float[] quadPoints)
    {
        COSArray newQuadPoints = new COSArray();
        newQuadPoints.setFloatArray(quadPoints);
        getCOSObject().setItem(COSName.QUADPOINTS, newQuadPoints);
    }

    /**
     * This will retrieve the set of quadpoints which encompass the areas of this annotation.
     *
     * @return An array of floats representing the quad points.
     */
    public float[] getQuadPoints()
    {
        COSArray array = getCOSObject().getCOSArray(COSName.QUADPOINTS);
        return array != null ? array.toFloatArray() : null;
    }
}
