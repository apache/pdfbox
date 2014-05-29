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
package org.apache.pdfbox.encoding;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link CharsetProvider} implementation for publishing PDFBox's encodings.
 * @version $Revision$
 */
public class PDFBoxCharsetProvider extends CharsetProvider
{

    private final Set<Charset> available = new java.util.HashSet<Charset>();
    private final Map<String, Charset> map = new java.util.HashMap<String, Charset>();

    /**
     * Constructor.
     */
    public PDFBoxCharsetProvider()
    {
        available.add(PDFDocEncodingCharset.INSTANCE);
        for (Charset cs : available)
        {
            map.put(cs.name(), cs);
            for (String alias : cs.aliases())
            {
                map.put(alias, cs);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Charset> charsets()
    {
        return Collections.unmodifiableSet(available).iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Charset charsetForName(String charsetName)
    {
        return map.get(charsetName);
    }

}
