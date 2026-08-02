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
package org.apache.pdfbox.pdfwriter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.junit.jupiter.api.Test;

class OperatorNameTest
{
    @Test
    void testNameAsByteMappingNonStrokingColor()
    {
        assertArrayEquals(OperatorName.NON_STROKING_COLOR.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_COLOR));
        assertArrayEquals(OperatorName.NON_STROKING_COLOR_N.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_COLOR_N));
        assertArrayEquals(OperatorName.NON_STROKING_RGB.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_RGB));
        assertArrayEquals(OperatorName.NON_STROKING_GRAY.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_GRAY));
        assertArrayEquals(OperatorName.NON_STROKING_CMYK.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_CMYK));
        assertArrayEquals(OperatorName.NON_STROKING_COLORSPACE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NON_STROKING_COLORSPACE));
    }

    @Test
    void testNameAsByteMappingStrokingColor()
    {
        assertArrayEquals(OperatorName.STROKING_COLOR.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLOR));
        assertArrayEquals(OperatorName.STROKING_COLOR_N.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLOR_N));
        assertArrayEquals(OperatorName.STROKING_COLOR_RGB.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLOR_RGB));
        assertArrayEquals(OperatorName.STROKING_COLOR_GRAY.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLOR_GRAY));
        assertArrayEquals(OperatorName.STROKING_COLOR_CMYK.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLOR_CMYK));
        assertArrayEquals(OperatorName.STROKING_COLORSPACE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKING_COLORSPACE));
    }

    @Test
    void testNameAsByteMappingMarkedContent()
    {
        assertArrayEquals(OperatorName.BEGIN_MARKED_CONTENT_SEQ.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_MARKED_CONTENT_SEQ));
        assertArrayEquals(OperatorName.BEGIN_MARKED_CONTENT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_MARKED_CONTENT));
        assertArrayEquals(OperatorName.END_MARKED_CONTENT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.END_MARKED_CONTENT));
        assertArrayEquals(
                OperatorName.MARKED_CONTENT_POINT_WITH_PROPS.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.MARKED_CONTENT_POINT_WITH_PROPS));
        assertArrayEquals(OperatorName.MARKED_CONTENT_POINT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.MARKED_CONTENT_POINT));
        assertArrayEquals(OperatorName.DRAW_OBJECT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.DRAW_OBJECT));
    }

    @Test
    void testNameAsByteMappingState()
    {
        assertArrayEquals(OperatorName.CONCAT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CONCAT));
        assertArrayEquals(OperatorName.RESTORE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.RESTORE));
        assertArrayEquals(OperatorName.SAVE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SAVE));
        assertArrayEquals(OperatorName.SET_FLATNESS.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_FLATNESS));
        assertArrayEquals(
                OperatorName.SET_GRAPHICS_STATE_PARAMS.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_GRAPHICS_STATE_PARAMS));
        assertArrayEquals(OperatorName.SET_LINE_CAPSTYLE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_LINE_CAPSTYLE));
        assertArrayEquals(OperatorName.SET_LINE_DASHPATTERN.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_LINE_DASHPATTERN));
        assertArrayEquals(OperatorName.SET_LINE_JOINSTYLE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_LINE_JOINSTYLE));
        assertArrayEquals(OperatorName.SET_LINE_MITERLIMIT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_LINE_MITERLIMIT));
        assertArrayEquals(OperatorName.SET_LINE_WIDTH.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_LINE_WIDTH));
        assertArrayEquals(OperatorName.SET_MATRIX.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_MATRIX));
        assertArrayEquals(OperatorName.SET_RENDERINGINTENT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_RENDERINGINTENT));

    }

    @Test
    void testNameAsByteGraphics()
    {
        assertArrayEquals(OperatorName.APPEND_RECT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.APPEND_RECT));
        assertArrayEquals(OperatorName.BEGIN_INLINE_IMAGE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_INLINE_IMAGE));
        assertArrayEquals(OperatorName.BEGIN_INLINE_IMAGE_DATA.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_INLINE_IMAGE_DATA));
        assertArrayEquals(OperatorName.END_INLINE_IMAGE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.END_INLINE_IMAGE));
        assertArrayEquals(OperatorName.CLIP_EVEN_ODD.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLIP_EVEN_ODD));
        assertArrayEquals(OperatorName.CLIP_NON_ZERO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLIP_NON_ZERO));
        assertArrayEquals(OperatorName.CLOSE_AND_STROKE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLOSE_AND_STROKE));
        assertArrayEquals(
                OperatorName.CLOSE_FILL_EVEN_ODD_AND_STROKE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLOSE_FILL_EVEN_ODD_AND_STROKE));
        assertArrayEquals(
                OperatorName.CLOSE_FILL_NON_ZERO_AND_STROKE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLOSE_FILL_NON_ZERO_AND_STROKE));
        assertArrayEquals(OperatorName.CLOSE_PATH.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CLOSE_PATH));
        assertArrayEquals(OperatorName.CURVE_TO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CURVE_TO));
        assertArrayEquals(
                OperatorName.CURVE_TO_REPLICATE_FINAL_POINT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CURVE_TO_REPLICATE_FINAL_POINT));
        assertArrayEquals(
                OperatorName.CURVE_TO_REPLICATE_INITIAL_POINT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.CURVE_TO_REPLICATE_INITIAL_POINT));
        assertArrayEquals(OperatorName.ENDPATH.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.ENDPATH));
        assertArrayEquals(OperatorName.FILL_EVEN_ODD_AND_STROKE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.FILL_EVEN_ODD_AND_STROKE));
        assertArrayEquals(OperatorName.FILL_EVEN_ODD.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.FILL_EVEN_ODD));
        assertArrayEquals(OperatorName.FILL_NON_ZERO_AND_STROKE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.FILL_NON_ZERO_AND_STROKE));
        assertArrayEquals(OperatorName.FILL_NON_ZERO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.FILL_NON_ZERO));
        assertArrayEquals(OperatorName.LEGACY_FILL_NON_ZERO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.LEGACY_FILL_NON_ZERO));
        assertArrayEquals(OperatorName.LINE_TO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.LINE_TO));
        assertArrayEquals(OperatorName.MOVE_TO.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.MOVE_TO));
        assertArrayEquals(OperatorName.SHADING_FILL.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SHADING_FILL));
        assertArrayEquals(OperatorName.STROKE_PATH.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.STROKE_PATH));
    }

    @Test
    void testNameAsByteText()
    {
        assertArrayEquals(OperatorName.BEGIN_TEXT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_TEXT));
        assertArrayEquals(OperatorName.END_TEXT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.END_TEXT));
        assertArrayEquals(OperatorName.MOVE_TEXT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.MOVE_TEXT));
        assertArrayEquals(OperatorName.MOVE_TEXT_SET_LEADING.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.MOVE_TEXT_SET_LEADING));
        assertArrayEquals(OperatorName.NEXT_LINE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.NEXT_LINE));
        assertArrayEquals(OperatorName.SET_CHAR_SPACING.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_CHAR_SPACING));
        assertArrayEquals(OperatorName.SET_FONT_AND_SIZE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_FONT_AND_SIZE));
        assertArrayEquals(
                OperatorName.SET_TEXT_HORIZONTAL_SCALING.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_TEXT_HORIZONTAL_SCALING));
        assertArrayEquals(OperatorName.SET_TEXT_LEADING.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_TEXT_LEADING));
        assertArrayEquals(OperatorName.SET_TEXT_RENDERINGMODE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_TEXT_RENDERINGMODE));
        assertArrayEquals(OperatorName.SET_TEXT_RISE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_TEXT_RISE));
        assertArrayEquals(OperatorName.SET_WORD_SPACING.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SET_WORD_SPACING));
        assertArrayEquals(OperatorName.SHOW_TEXT.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SHOW_TEXT));
        assertArrayEquals(OperatorName.SHOW_TEXT_ADJUSTED.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SHOW_TEXT_ADJUSTED));
        assertArrayEquals(OperatorName.SHOW_TEXT_LINE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SHOW_TEXT_LINE));
        assertArrayEquals(OperatorName.SHOW_TEXT_LINE_AND_SPACE.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.SHOW_TEXT_LINE_AND_SPACE));
    }

    @Test
    void testNameAsByteType3()
    {
        assertArrayEquals(OperatorName.TYPE3_D0.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.TYPE3_D0));
        assertArrayEquals(OperatorName.TYPE3_D1.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.TYPE3_D1));
    }

    @Test
    void testNameAsByteCompatibility()
    {
        assertArrayEquals(
                OperatorName.BEGIN_COMPATIBILITY_SECTION.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.BEGIN_COMPATIBILITY_SECTION));
        assertArrayEquals(
                OperatorName.END_COMPATIBILITY_SECTION.getBytes(StandardCharsets.US_ASCII),
                OperatorName.getNameAsBytes(OperatorName.END_COMPATIBILITY_SECTION));
    }

    @Test
    void testUnkownOperator()
    {
        assertThrows(IllegalArgumentException.class, () -> OperatorName.getNameAsBytes("UNKNOWN"));
    }
}
