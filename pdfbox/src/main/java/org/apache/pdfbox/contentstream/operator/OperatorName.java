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
package org.apache.pdfbox.contentstream.operator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class OperatorName
{
    // non stroking color
    public static final String NON_STROKING_COLOR = "sc";
    public static final String NON_STROKING_COLOR_N = "scn";
    public static final String NON_STROKING_RGB = "rg";
    public static final String NON_STROKING_GRAY = "g";
    public static final String NON_STROKING_CMYK = "k";
    public static final String NON_STROKING_COLORSPACE = "cs";

    // stroking color
    public static final String STROKING_COLOR = "SC";
    public static final String STROKING_COLOR_N = "SCN";
    public static final String STROKING_COLOR_RGB = "RG";
    public static final String STROKING_COLOR_GRAY = "G";
    public static final String STROKING_COLOR_CMYK = "K";
    public static final String STROKING_COLORSPACE = "CS";

    // marked content
    public static final String BEGIN_MARKED_CONTENT_SEQ = "BDC";
    public static final String BEGIN_MARKED_CONTENT = "BMC";
    public static final String END_MARKED_CONTENT = "EMC";
    public static final String MARKED_CONTENT_POINT_WITH_PROPS = "DP";
    public static final String MARKED_CONTENT_POINT = "MP";
    public static final String DRAW_OBJECT = "Do";

    // state
    public static final String CONCAT = "cm";
    public static final String RESTORE = "Q";
    public static final String SAVE = "q";
    public static final String SET_FLATNESS = "i";
    public static final String SET_GRAPHICS_STATE_PARAMS = "gs";
    public static final String SET_LINE_CAPSTYLE = "J";
    public static final String SET_LINE_DASHPATTERN = "d";
    public static final String SET_LINE_JOINSTYLE = "j";
    public static final String SET_LINE_MITERLIMIT = "M";
    public static final String SET_LINE_WIDTH = "w";
    public static final String SET_MATRIX = "Tm";
    public static final String SET_RENDERINGINTENT = "ri";

    // graphics
    public static final String APPEND_RECT = "re";
    public static final String BEGIN_INLINE_IMAGE = "BI";
    public static final String BEGIN_INLINE_IMAGE_DATA = "ID";
    public static final String END_INLINE_IMAGE = "EI";
    public static final String CLIP_EVEN_ODD = "W*";
    public static final String CLIP_NON_ZERO = "W";
    public static final String CLOSE_AND_STROKE = "s";
    public static final String CLOSE_FILL_EVEN_ODD_AND_STROKE = "b*";
    public static final String CLOSE_FILL_NON_ZERO_AND_STROKE = "b";
    public static final String CLOSE_PATH = "h";
    public static final String CURVE_TO = "c";
    public static final String CURVE_TO_REPLICATE_FINAL_POINT = "y";
    public static final String CURVE_TO_REPLICATE_INITIAL_POINT = "v";
    public static final String ENDPATH = "n";
    public static final String FILL_EVEN_ODD_AND_STROKE = "B*";
    public static final String FILL_EVEN_ODD = "f*";
    public static final String FILL_NON_ZERO_AND_STROKE = "B";
    public static final String FILL_NON_ZERO = "f";
    public static final String LEGACY_FILL_NON_ZERO = "F";
    public static final String LINE_TO = "l";
    public static final String MOVE_TO = "m";
    public static final String SHADING_FILL = "sh";
    public static final String STROKE_PATH = "S";

    // text
    public static final String BEGIN_TEXT = "BT";
    public static final String END_TEXT = "ET";
    public static final String MOVE_TEXT = "Td";
    public static final String MOVE_TEXT_SET_LEADING = "TD";
    public static final String NEXT_LINE = "T*";
    public static final String SET_CHAR_SPACING = "Tc";
    public static final String SET_FONT_AND_SIZE = "Tf";
    public static final String SET_TEXT_HORIZONTAL_SCALING = "Tz";
    public static final String SET_TEXT_LEADING = "TL";
    public static final String SET_TEXT_RENDERINGMODE = "Tr";
    public static final String SET_TEXT_RISE = "Ts";
    public static final String SET_WORD_SPACING = "Tw";
    public static final String SHOW_TEXT = "Tj";
    public static final String SHOW_TEXT_ADJUSTED = "TJ";
    public static final String SHOW_TEXT_LINE = "'";
    public static final String SHOW_TEXT_LINE_AND_SPACE = "\"";

    // type3 font
    public static final String TYPE3_D0 = "d0";
    public static final String TYPE3_D1 = "d1";

    // compatibility section
    public static final String BEGIN_COMPATIBILITY_SECTION = "BX";
    public static final String END_COMPATIBILITY_SECTION = "EX";

    private static final Map<String, byte[]> nameAsBytes = new HashMap<>();

    static
    {
        // non stroking color
        nameAsBytes.put(NON_STROKING_COLOR, //
                NON_STROKING_COLOR.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NON_STROKING_COLOR_N, //
                NON_STROKING_COLOR_N.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NON_STROKING_RGB, //
                NON_STROKING_RGB.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NON_STROKING_GRAY, //
                NON_STROKING_GRAY.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NON_STROKING_CMYK, //
                NON_STROKING_CMYK.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NON_STROKING_COLORSPACE, //
                NON_STROKING_COLORSPACE.getBytes(StandardCharsets.US_ASCII));
        // stroking color
        nameAsBytes.put(STROKING_COLOR, //
                STROKING_COLOR.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKING_COLOR_N, //
                STROKING_COLOR_N.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKING_COLOR_RGB, //
                STROKING_COLOR_RGB.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKING_COLOR_GRAY, //
                STROKING_COLOR_GRAY.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKING_COLOR_CMYK, //
                STROKING_COLOR_CMYK.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKING_COLORSPACE, //
                STROKING_COLORSPACE.getBytes(StandardCharsets.US_ASCII));
        // marked content
        nameAsBytes.put(BEGIN_MARKED_CONTENT_SEQ, //
                BEGIN_MARKED_CONTENT_SEQ.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(BEGIN_MARKED_CONTENT, //
                BEGIN_MARKED_CONTENT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(END_MARKED_CONTENT, //
                END_MARKED_CONTENT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(MARKED_CONTENT_POINT_WITH_PROPS, //
                MARKED_CONTENT_POINT_WITH_PROPS.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(MARKED_CONTENT_POINT, //
                MARKED_CONTENT_POINT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(DRAW_OBJECT, //
                DRAW_OBJECT.getBytes(StandardCharsets.US_ASCII));
        // state
        nameAsBytes.put(CONCAT, //
                CONCAT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(RESTORE, //
                RESTORE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SAVE, //
                SAVE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_FLATNESS, //
                SET_FLATNESS.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_GRAPHICS_STATE_PARAMS, //
                SET_GRAPHICS_STATE_PARAMS.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_LINE_CAPSTYLE, //
                SET_LINE_CAPSTYLE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_LINE_DASHPATTERN, //
                SET_LINE_DASHPATTERN.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_LINE_JOINSTYLE, //
                SET_LINE_JOINSTYLE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_LINE_MITERLIMIT, //
                SET_LINE_MITERLIMIT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_LINE_WIDTH, //
                SET_LINE_WIDTH.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_MATRIX, //
                SET_MATRIX.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_RENDERINGINTENT, //
                SET_RENDERINGINTENT.getBytes(StandardCharsets.US_ASCII));
        // graphics
        nameAsBytes.put(APPEND_RECT, //
                APPEND_RECT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(BEGIN_INLINE_IMAGE, //
                BEGIN_INLINE_IMAGE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(BEGIN_INLINE_IMAGE_DATA, //
                BEGIN_INLINE_IMAGE_DATA.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(END_INLINE_IMAGE, //
                END_INLINE_IMAGE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLIP_EVEN_ODD, //
                CLIP_EVEN_ODD.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLIP_NON_ZERO, //
                CLIP_NON_ZERO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLOSE_AND_STROKE, //
                CLOSE_AND_STROKE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLOSE_FILL_EVEN_ODD_AND_STROKE, //
                CLOSE_FILL_EVEN_ODD_AND_STROKE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLOSE_FILL_NON_ZERO_AND_STROKE, //
                CLOSE_FILL_NON_ZERO_AND_STROKE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CLOSE_PATH, //
                CLOSE_PATH.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CURVE_TO, //
                CURVE_TO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CURVE_TO_REPLICATE_FINAL_POINT, //
                CURVE_TO_REPLICATE_FINAL_POINT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(CURVE_TO_REPLICATE_INITIAL_POINT, //
                CURVE_TO_REPLICATE_INITIAL_POINT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(ENDPATH, //
                ENDPATH.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(FILL_EVEN_ODD_AND_STROKE, //
                FILL_EVEN_ODD_AND_STROKE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(FILL_EVEN_ODD, //
                FILL_EVEN_ODD.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(FILL_NON_ZERO_AND_STROKE, //
                FILL_NON_ZERO_AND_STROKE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(FILL_NON_ZERO, //
                FILL_NON_ZERO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(LEGACY_FILL_NON_ZERO, //
                LEGACY_FILL_NON_ZERO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(LINE_TO, //
                LINE_TO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(MOVE_TO, //
                MOVE_TO.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SHADING_FILL, //
                SHADING_FILL.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(STROKE_PATH, //
                STROKE_PATH.getBytes(StandardCharsets.US_ASCII));
        // text
        nameAsBytes.put(BEGIN_TEXT, //
                BEGIN_TEXT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(END_TEXT, //
                END_TEXT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(MOVE_TEXT, //
                MOVE_TEXT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(MOVE_TEXT_SET_LEADING, //
                MOVE_TEXT_SET_LEADING.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(NEXT_LINE, //
                NEXT_LINE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_CHAR_SPACING, //
                SET_CHAR_SPACING.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_FONT_AND_SIZE, //
                SET_FONT_AND_SIZE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_TEXT_HORIZONTAL_SCALING, //
                SET_TEXT_HORIZONTAL_SCALING.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_TEXT_LEADING, //
                SET_TEXT_LEADING.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_TEXT_RENDERINGMODE, //
                SET_TEXT_RENDERINGMODE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_TEXT_RISE, //
                SET_TEXT_RISE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SET_WORD_SPACING, //
                SET_WORD_SPACING.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SHOW_TEXT, //
                SHOW_TEXT.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SHOW_TEXT_ADJUSTED, //
                SHOW_TEXT_ADJUSTED.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SHOW_TEXT_LINE, //
                SHOW_TEXT_LINE.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(SHOW_TEXT_LINE_AND_SPACE, //
                SHOW_TEXT_LINE_AND_SPACE.getBytes(StandardCharsets.US_ASCII));
        // type3 font
        nameAsBytes.put(TYPE3_D0, //
                TYPE3_D0.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(TYPE3_D1, //
                TYPE3_D1.getBytes(StandardCharsets.US_ASCII));
        // compatibility section
        nameAsBytes.put(BEGIN_COMPATIBILITY_SECTION, //
                BEGIN_COMPATIBILITY_SECTION.getBytes(StandardCharsets.US_ASCII));
        nameAsBytes.put(END_COMPATIBILITY_SECTION, //
                END_COMPATIBILITY_SECTION.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * private constructor
     */
    private OperatorName()
    {
    }

    /**
     * Returns the ASCII representation of the given operator name as byte array.
     * 
     * @param operatorName the name of the operator.
     * @return the ASCII representation of the operator name as byte array.
     */
    public static byte[] getNameAsBytes(String operatorName)
    {
        byte[] stringBytes = nameAsBytes.get(operatorName);
        if (stringBytes == null)
        {
            throw new IllegalArgumentException("unknown operator " + operatorName);
        }
        return stringBytes;
    }
}
