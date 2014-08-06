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
package org.apache.pdfbox.pdmodel.graphics.state;

/**
 * Text Rendering Mode.
 *
 * @author John Hewson
 */
public enum RenderingMode
{
    /**
     * Fill text.
     */
    FILL(0),

    /**
     * Stroke text.
     */
    STROKE(1),

    /**
     * Fill, then stroke text.
     */
    FILL_STROKE(2),

    /**
     * Neither fill nor stroke text (invisible)
     */
    NEITHER(3),

    /**
     * Fill text and add to path for clipping.
     */
    FILL_CLIP(4),

    /**
     * Stroke text and add to path for clipping.
     */
    STROKE_CLIP(5),

    /**
     * Fill, then stroke text and add to path for clipping.
     */
    FILL_STROKE_CLIP(6),

    /**
     * Add text to path for clipping.
     */
    NEITHER_CLIP(7);

    private static final RenderingMode[] VALUES = RenderingMode.values();

    public static RenderingMode fromInt(int value)
    {
        return VALUES[value];
    }

    private final int value;

    RenderingMode(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value of this mode, as used in a PDF file.
     */
    public int intValue()
    {
        return value;
    }

    /**
     * Returns true is this mode fills text.
     */
    public boolean isFill()
    {
        return this == FILL ||
               this == FILL_STROKE ||
               this == FILL_CLIP ||
               this == FILL_STROKE_CLIP;
    }

    /**
     * Returns true is this mode strokes text.
     */
    public boolean isStroke()
    {
        return this == STROKE ||
               this == FILL_STROKE ||
               this == STROKE ||
               this == FILL_STROKE_CLIP;
    }

    /**
     * Returns true is this mode clips text.
     */
    public boolean isClip()
    {
        return this == FILL_CLIP ||
               this == STROKE_CLIP ||
               this == FILL_STROKE_CLIP ||
               this == NEITHER_CLIP;
    }
}
