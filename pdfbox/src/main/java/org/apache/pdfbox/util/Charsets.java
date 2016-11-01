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

import java.nio.charset.Charset;

/**
 * Utility class providing common Charsets used in PDFBox.
 *
 * @author John Hewson
 */
public final class Charsets
{
    private Charsets() {}

    /*** ASCII charset */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /*** UTF-16BE charset */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    /*** UTF-16LE charset */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
    
    /*** ISO-8859-1 charset */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /*** Windows-1252 charset */
    public static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    /*** UTF-8 charset */
    public static final Charset UTF_8 = Charset.forName("UTF-8");
}
