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
import java.nio.charset.StandardCharsets;

/**
 * Utility class providing common Charsets used in PDFBox.
 *
 * @author John Hewson
 */
public final class Charsets
{
    private Charsets() {}

    /**
     * ASCII charset
     *
     * @deprecated use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset US_ASCII = StandardCharsets.US_ASCII;

    /**
     * UTF-16BE charset
     *
     * @deprecated use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;

    /**
     * UTF-16LE charset
     *
     * @deprecated use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;

    /**
     * ISO-8859-1 charset
     *
     * @deprecated use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    /**
     * Windows-1252 charset
     */
    public static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    /**
     * UTF-8 charset
     *
     * @deprecated use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
}
