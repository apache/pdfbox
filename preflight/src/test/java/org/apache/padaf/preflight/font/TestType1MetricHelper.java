/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.preflight.font;

import static org.apache.padaf.preflight.ValidationConstants.FONT_DICTIONARY_VALUE_ENCODING_WIN;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;


import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.font.Type1MetricHelper;
import org.junit.Test;

public class TestType1MetricHelper {

  @Test
  /**
   * This test validates the Glyph Width extraction of a Valid Type 1 Font Program
   */
  public void testNominal() throws Exception {
    int length1 = 926;
    int length2 = 12270;

    int first = 32;
    int last = 160;

    int[] widths = { 278, 0, 0, 0, 0, 0, 0, 191, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        556, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 611, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 556, 0, 556, 556, 278, 556, 556, 222, 0, 0, 222, 833, 556,
        556, 556, 0, 0, 500, 278, 0, 0, 722, 500, 500, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 278 };
    InputStream font = this.getClass().getResourceAsStream(
        "subset_type1_valid.font");

    Type1MetricHelper helper = new Type1MetricHelper(font, length1, length2,
        FONT_DICTIONARY_VALUE_ENCODING_WIN);
    helper.parse();
    for (int i = 0; i < (last - first + 1); ++i) {
      if (widths[i] != 0) {
        assertTrue(widths[i] == helper.getWidth(first + i));
      }
    }

  }

  @Test(expected = ValidationException.class)
  /**
   * A invalid Type1 Font program is parsed by the MetricHelper.
   * A ValidationException is expected.
   * 
   * (The Font Program is a TrueType Font.)
   */
  public void testInvalidFont() throws Exception {
    int length1 = 926;
    int length2 = 12270;

    InputStream font = this.getClass().getResourceAsStream("true_type.ttf");
    Type1MetricHelper helper = new Type1MetricHelper(font, length1, length2,
        FONT_DICTIONARY_VALUE_ENCODING_WIN);

    helper.parse();
    fail();
  }

  @Test
  /**
   * A Missing character code shouldn't cause Exception but return a Width of 0.
   */
  public void testMissingGlyph() throws Exception {
    int length1 = 926;
    int length2 = 12270;

    int first = 32;
    int last = 160;

    int[] widths = { 278, 0, 0, 0, 0, 0, 0, 191, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        556, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 611, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 556, 0, 556, 556, 278, 556, 556, 222, 0, 0, 222, 833, 556,
        556, 556, 0, 0, 500, 278, 0, 0, 722, 500, 500, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 278 };
    InputStream font = this.getClass().getResourceAsStream(
        "subset_type1_valid.font");

    Type1MetricHelper helper = new Type1MetricHelper(font, length1, length2,
        FONT_DICTIONARY_VALUE_ENCODING_WIN);
    helper.parse();

    for (int i = 0; i < (last - first + 1); ++i) {
      if (widths[i] != 0) {
        assertTrue(widths[i] == helper.getWidth(first + i));
      }
    }

    // ---- Missing Glyph returns 0
    assertTrue(helper.getWidth(200) == 0);
  }
}
