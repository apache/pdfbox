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
package org.apache.pdfbox.util;

/**
 * This class allows a caller to normalize text in various ways.
 * It will load the ICU4J jar file if it is defined on the classpath.
 * 
 */
public class TextNormalize {
    private ICU4JImpl icu4j = null;

    public TextNormalize() {
        findICU4J();
    }


    private void findICU4J() {
        // see if we can load the icu4j classes from the classpath
        try {
            this.getClass().getClassLoader().loadClass("com.ibm.icu.text.Bidi");
            this.getClass().getClassLoader().loadClass("com.ibm.icu.text.Normalizer");
            icu4j = new ICU4JImpl();
        } catch (ClassNotFoundException e) {
            icu4j = null;
        }
    }


    /**
     * Takes a line of text in presentation order and converts it to logical order.
     * For most text other than Arabic and Hebrew, the presentation and logical
     * orders are the same. However, for Arabic and Hebrew, they are different and
     * if the text involves both RTL and LTR text then the Unicode BIDI algorithm
     * must be used to determine how to map between them.  
     * 
     * @param a_str Presentation form of line to convert (i.e. left most char is first char)
     * @param a_isRtlDominant true if the PAGE has a dominant right to left ordering
     * @return Logical form of string (or original string if ICU4J library is not on classpath)
     */
    public String makeLineLogicalOrder(String a_str, boolean a_isRtlDominant) {
        if (icu4j != null) {
            return icu4j.makeLineLogicalOrder(a_str, a_isRtlDominant);
        }
        else {
            return a_str;
        }
    }

    /**
     * Normalize the presentation forms of characters in the string.
     * For example, convert the single "fi" ligature to "f" and "i". 
     * 
     * @param a_str String to normalize
     * @return Normalized string (or original string if ICU4J library is not on classpath)
     */
    public String normalizePres(String a_str) {
        if (icu4j != null) {
            return icu4j.normalizePres(a_str);
        }
        else {
            return a_str;
        }
    }
}
