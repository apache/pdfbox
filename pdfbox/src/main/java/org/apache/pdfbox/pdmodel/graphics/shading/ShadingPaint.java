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

import org.apache.pdfbox.util.Matrix;

/**
 * This is base class for all PDShading-Paints to allow other low level libraries access to the
 * shading source data. One user of this interface is the PdfBoxGraphics2D-adapter.
 * 
 * @param <T> the actual PDShading class.
 */
public abstract class ShadingPaint<T extends PDShading> implements Paint
{
    protected final T shading;
    protected final Matrix matrix;

    ShadingPaint(T shading, Matrix matrix)
    {
        this.shading = shading;
        this.matrix = matrix;
    }

    /**
     * @return the PDShading of this paint
     */
    public T getShading()
    {
        return shading;
    }

    /**
     * @return the active Matrix of this paint
     */
    public Matrix getMatrix()
    {
        return matrix;
    }
}
