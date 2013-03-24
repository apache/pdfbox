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

import java.awt.geom.AffineTransform;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.util.Matrix;


/**
 * This class represents an appearance for an annotation.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDAppearanceStream implements COSObjectable
{
    private COSStream stream = null;


    /**
     * Constructor.
     *
     * @param s The cos stream for this appearance.
     */
    public PDAppearanceStream( COSStream s )
    {
        stream = s;
    }

    /**
     * This will return the underlying stream.
     *
     * @return The wrapped stream.
     */
    public COSStream getStream()
    {
        return stream;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return stream;
    }

    /**
     * Get the bounding box for this appearance.  This may return null in which
     * case the Rectangle from the annotation should be used.
     *
     * @return The bounding box for this appearance.
     */
    public PDRectangle getBoundingBox()
    {
        PDRectangle box = null;
        COSArray bbox = (COSArray)stream.getDictionaryObject( COSName.BBOX );
        if( bbox != null )
        {
            box = new PDRectangle( bbox );
        }
        return box;
    }

    /**
     * This will set the bounding box for this appearance stream.
     *
     * @param rectangle The new bounding box.
     */
    public void setBoundingBox( PDRectangle rectangle )
    {
        COSArray array = null;
        if( rectangle != null )
        {
            array = rectangle.getCOSArray();
        }
        stream.setItem( COSName.BBOX, array );
    }

    /**
     * This will get the resources for this appearance stream.
     *
     * @return The appearance stream resources.
     */
    public PDResources getResources()
    {
        PDResources retval = null;
        COSDictionary dict = (COSDictionary)stream.getDictionaryObject( COSName.RESOURCES );
        if( dict != null )
        {
            retval = new PDResources( dict );
        }
        return retval;
    }

    /**
     * This will set the new resources.
     *
     * @param resources The new resources.
     */
    public void setResources( PDResources resources )
    {
        COSDictionary dict = null;
        if( resources != null )
        {
            dict = resources.getCOSDictionary();
        }
        stream.setItem( COSName.RESOURCES, dict );
    }

    /**
     * Gets the optional matrix for this appearance.  This may return null.
     *
     * @return The matrix of this appearance.
     */
    public Matrix getMatrix()
    {
        Matrix retval = null;
        COSArray array = (COSArray)stream.getDictionaryObject( COSName.MATRIX );
        if( array != null )
        {
            retval = new Matrix();
            retval.setValue(0, 0, ((COSNumber) array.get(0)).floatValue());
            retval.setValue(0, 1, ((COSNumber) array.get(1)).floatValue());
            retval.setValue(1, 0, ((COSNumber) array.get(2)).floatValue());
            retval.setValue(1, 1, ((COSNumber) array.get(3)).floatValue());
            retval.setValue(2, 0, ((COSNumber) array.get(4)).floatValue());
            retval.setValue(2, 1, ((COSNumber) array.get(5)).floatValue());
        }
        return retval;
    }

    /**
     * Sets the optional Matrix entry for this appearance.
     * @param transform the transformation matrix
     */
    public void setMatrix(AffineTransform transform)
    {
        if (transform != null)
        {
            COSArray matrix = new COSArray();
            double[] values = new double[6];
            transform.getMatrix(values);
            for (double v : values)
            {
                matrix.add(new COSFloat((float)v));
            }
            stream.setItem(COSName.MATRIX, matrix);
        }
        else
        {
            stream.removeItem(COSName.MATRIX);
        }
    }

}
