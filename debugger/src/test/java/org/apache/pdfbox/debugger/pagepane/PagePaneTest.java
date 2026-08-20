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
package org.apache.pdfbox.debugger.pagepane;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

/**
 * Test the scheme allowlist used before document supplied link URIs are handed to
 * Desktop.browse().
 */
class PagePaneTest
{
    @Test
    void testBrowsableSchemes() throws Exception
    {
        assertTrue(PagePane.isBrowsableScheme(new URI("http://example.com")));
        assertTrue(PagePane.isBrowsableScheme(new URI("https://example.com/page?q=1")));
        assertTrue(PagePane.isBrowsableScheme(new URI("mailto:security@example.com")));
        assertTrue(PagePane.isBrowsableScheme(new URI("MAILTO:security@example.com")));

        // UNC / SMB style links used for NTLM hash leaks
        assertFalse(PagePane.isBrowsableScheme(new URI("file:////attacker.example/share/x")));
        assertFalse(PagePane.isBrowsableScheme(new URI("file:///etc/passwd")));
        assertFalse(PagePane.isBrowsableScheme(new URI("smb://attacker.example/x")));
        // arbitrary registered protocol handlers
        assertFalse(PagePane.isBrowsableScheme(new URI("search-ms:query")));
        assertFalse(PagePane.isBrowsableScheme(new URI("ms-msdt:/id%20PCWDiagnostic")));
        assertFalse(PagePane.isBrowsableScheme(new URI("jar:file:/tmp/a.jar!/x")));
        // no scheme at all
        assertFalse(PagePane.isBrowsableScheme(new URI("relative/path")));
        assertFalse(PagePane.isBrowsableScheme(null));
    }
}
