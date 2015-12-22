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
package org.apache.fontbox.util;

import java.nio.charset.Charset;

/**
 * This class provides an instance of all common charsets used to transform byte arrays into strings. 
 *  
 */
public final class Charsets
{
    private Charsets() {}
    
    /**
     * ISO-8859-1 Charset
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    /**
     * UTF-16 Charset
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");
    /**
     * UTF-16BE Charset
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    /**
     * US-ASCII Charset
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    /**
     * ISO-10646 Charset
     */
    public static final Charset ISO_10646 = Charset.forName("ISO-10646-UCS-2");
    
}