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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

/**
 * An appearance stream is a form XObject, a self-contained content stream that shall be rendered
 * inside the annotation rectangle.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDAppearanceStream extends PDFormXObject
{
    private final PDAnnotation parent;

    /**
     * Creates a Form XObject for reading.
     * @param stream The XObject stream
     */
    public PDAppearanceStream(COSStream stream, PDAnnotation parent)
    {
        super(new PDStream(stream));
        this.parent = parent;
    }

    /**
     * Creates a Form Image XObject for writing, in the given document.
     * @param document The current document
     */
    public PDAppearanceStream(PDDocument document, PDAnnotation parent)
    {
        super(document);
        this.parent = parent;
    }

    /**
     * Returns the matrix "A", which transforms the appearance box to align with the edges of the
     * annotation?s rectangle.
     */
    @Override
    public Matrix getMatrix()
    {
        PDRectangle bbox = getBBox();
        PDRectangle rect = parent.getRectangle();
        Matrix matrix = getActualMatrix();

        // transformed appearance box
        PDRectangle transformedBox = bbox.transform(matrix);

        // compute a matrix which scales and translates the transformed appearance box to align
        // with the edges of the annotation's rectangle
        Matrix a = Matrix.getTranslatingInstance(rect.getLowerLeftX(), rect.getLowerLeftY());
        a.concatenate(Matrix.getScaleInstance(rect.getWidth() / transformedBox.getWidth(),
                                              rect.getHeight() / transformedBox.getHeight()));
        a.concatenate(Matrix.getTranslatingInstance(-transformedBox.getLowerLeftX(),
                                                    -transformedBox.getLowerLeftY()));
        return a;
    }

    /**
     * Returns the actual /Matrix entry, unlike other forms this needs to be transformed using
     * the parent annotation's /Rect before it can be used to render the content stream.
     */
    private Matrix getActualMatrix()
    {
        COSArray array = (COSArray)getContentStream().getDictionaryObject(COSName.MATRIX);
        if( array != null )
        {
            Matrix matrix = new Matrix();
            matrix.setValue(0, 0, ((COSNumber) array.get(0)).floatValue());
            matrix.setValue(0, 1, ((COSNumber) array.get(1)).floatValue());
            matrix.setValue(1, 0, ((COSNumber) array.get(2)).floatValue());
            matrix.setValue(1, 1, ((COSNumber) array.get(3)).floatValue());
            matrix.setValue(2, 0, ((COSNumber) array.get(4)).floatValue());
            matrix.setValue(2, 1, ((COSNumber) array.get(5)).floatValue());
            return matrix;
        }
        else
        {
            // the default value is the identity matrix
            return new Matrix();
        }
    }
}
