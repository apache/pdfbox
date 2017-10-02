/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class PDActionURITest
{
    /**
     * PDFBOX-3913: Check that URIs encoded in UTF-8 are also supported.
     */
    @Test
    public void testUTF8URI()
    {
        PDActionURI actionURI = new PDActionURI();
        assertNull(actionURI.getURI());
        actionURI.setURI("http://çµ„åŒ¶æ›¿ç¶Ž.com/");
        assertEquals("http://経営承継.com/", actionURI.getURI());
    }
}
