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

import java.util.HashMap;

/**
 * This class allows a caller to normalize text in various ways.
 * It will load the ICU4J jar file if it is defined on the classpath.
 * 
 */
public class TextNormalize {
    private ICU4JImpl icu4j = null;
    private static final HashMap diacHash = new HashMap();
    private String encoding;

    /**
     * 
     * @param encoding The Encoding that the text will eventually be written as (or null)
     */
    public TextNormalize(String encoding) {
        findICU4J();
        populateDiacHash();
        this.encoding = encoding;
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
    /*
     * Adds non-decomposing diacritics to the hash with their related
     * combining character. These are values that the unicode spec claims
     * are equivalent but are not mapped in the form NFKC normalization method.
     * Determined by going through the Combining Diacritical Marks section of 
     * the Unicode spec and identifying which characters are not mapped to by 
     * the normalization. 
     */
    private void populateDiacHash(){
        diacHash.put(new Integer(0x0060), "\u0300");
        diacHash.put(new Integer(0x02CB), "\u0300");
        diacHash.put(new Integer(0x0027), "\u0301");
        diacHash.put(new Integer(0x02B9), "\u0301");
        diacHash.put(new Integer(0x02CA), "\u0301");
        diacHash.put(new Integer(0x005e), "\u0302");
        diacHash.put(new Integer(0x02C6), "\u0302");
        diacHash.put(new Integer(0x007E), "\u0303");
        diacHash.put(new Integer(0x02C9), "\u0304");
        diacHash.put(new Integer(0x00B0), "\u030A");
        diacHash.put(new Integer(0x02BA), "\u030B");
        diacHash.put(new Integer(0x02C7), "\u030C");
        diacHash.put(new Integer(0x02C8), "\u030D");
        diacHash.put(new Integer(0x0022), "\u030E");
        diacHash.put(new Integer(0x02BB), "\u0312");
        diacHash.put(new Integer(0x02BC), "\u0313");
        diacHash.put(new Integer(0x0486), "\u0313");
        diacHash.put(new Integer(0x055A), "\u0313");
        diacHash.put(new Integer(0x02BD), "\u0314");
        diacHash.put(new Integer(0x0485), "\u0314");
        diacHash.put(new Integer(0x0559), "\u0314");
        diacHash.put(new Integer(0x02D4), "\u031D");
        diacHash.put(new Integer(0x02D5), "\u031E");
        diacHash.put(new Integer(0x02D6), "\u031F");
        diacHash.put(new Integer(0x02D7), "\u0320");
        diacHash.put(new Integer(0x02B2), "\u0321");
        diacHash.put(new Integer(0x02CC), "\u0329");
        diacHash.put(new Integer(0x02B7), "\u032B");
        diacHash.put(new Integer(0x02CD), "\u0331");
        diacHash.put(new Integer(0x005F), "\u0332");
        diacHash.put(new Integer(0x204E), "\u0359");
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
    
    /**
     * Normalize the diacritic, for example, 
     * convert non-combining diacritic characters to their combining
     * counterparts. 
     * 
     * @param a_str String to normalize 
     * @return Normalized string (or original string if ICU4J library is not on classpath)
     */
    public String normalizeDiac(String a_str){
        /*
         * Unicode contains special combining forms of the diacritic characters
         * and we want to use these. 
         */
        if(encoding != null && encoding.toUpperCase().startsWith("UTF")){
            Integer c = new Integer(a_str.charAt(0));
            // convert the characters not defined in the Unicode spec
            if(diacHash.containsKey(c)){
                return (String)diacHash.get(c);
            }
            else if (icu4j != null) {
                return icu4j.normalizeDiac(a_str);
            }
            else {
                return a_str;
            }
        }
        else{
            return a_str;
        }
    }
}
