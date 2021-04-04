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
package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.util.Matrix;

/**
 * Resources for a shading type 6 (Coons Patch Mesh).
 */
public class PDShadingType6 extends PDMeshBasedShadingType
{
    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary the dictionary for this shading
     */
    public PDShadingType6(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    @Override
    public int getShadingType()
    {
        return PDShading.SHADING_TYPE6;
    }

    @Override
    public Paint toPaint(Matrix matrix)
    {
        return new Type6ShadingPaint(this, matrix);
    }
    
    @Override
    protected Patch generatePatch(Point2D[] points, float[][] color)
    {
        return new CoonsPatch(points, color);
    }

    @Override
    public Rectangle2D getBounds(AffineTransform xform, Matrix matrix) throws IOException
    {
        return getBounds(xform, matrix, 12);
    }
}
