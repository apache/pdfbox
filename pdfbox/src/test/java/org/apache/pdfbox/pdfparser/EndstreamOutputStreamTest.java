/*
 * Copyright 2014 The Apache Software Foundation.
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

package org.apache.pdfbox.pdfparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 * @author Tilman Hausherr
 */
public class EndstreamOutputStreamTest extends TestCase
{
    public void testEndstreamOutputStream() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EndstreamOutputStream feos = new EndstreamOutputStream(baos);
        byte[] tab1 = {1, 2, 3, 4};
        byte[] tab2 = {5, 6, 7, '\r', '\n'};
        byte[] tab3 = {8, 9, '\r', '\n'};
        feos.write(tab1, 0, tab1.length);
        feos.write(tab2, 0, tab2.length);
        feos.write(tab3, 0, tab3.length);
        feos.flush();
        byte[] expectedResult1 = { 1, 2, 3, 4, 5, 6, 7, '\r', '\n', 8, 9};
        Assert.assertArrayEquals(expectedResult1, baos.toByteArray());

        baos = new ByteArrayOutputStream();
        feos = new EndstreamOutputStream(baos);
        byte[] tab4 = {1, 2, 3, 4};
        byte[] tab5 = {5, 6, 7, '\r' };
        byte[] tab6 = {8, 9, '\n'};
        feos.write(tab4, 0, tab4.length);
        feos.write(tab5, 0, tab5.length);
        feos.write(tab6, 0, tab6.length);
        feos.flush();
        byte[] expectedResult2 = { 1, 2, 3, 4, 5, 6, 7, '\r', 8, 9};
        Assert.assertArrayEquals(expectedResult2, baos.toByteArray());
        
        baos = new ByteArrayOutputStream();
        feos = new EndstreamOutputStream(baos);
        byte[] tab7 = {1, 2, 3, 4, '\r'};
        byte[] tab8 = {'\n', 5, 6, 7, '\n' };
        byte[] tab9 = {8, 9, '\r'}; // final CR is not to be discarded
        feos.write(tab7, 0, tab7.length);
        feos.write(tab8, 0, tab8.length);
        feos.write(tab9, 0, tab9.length);
        feos.flush();
        byte[] expectedResult3 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\n', 8, 9, '\r'};
        Assert.assertArrayEquals(expectedResult3, baos.toByteArray());
        
        baos = new ByteArrayOutputStream();
        feos = new EndstreamOutputStream(baos);
        byte[] tab10 = {1, 2, 3, 4, '\r'};
        byte[] tab11 = {'\n', 5, 6, 7, '\r' };
        byte[] tab12 = {8, 9, '\r'};
        byte[] tab13 = {'\n'}; // final CR LF across buffers
        feos.write(tab10, 0, tab10.length);
        feos.write(tab11, 0, tab11.length);
        feos.write(tab12, 0, tab12.length);
        feos.write(tab13, 0, tab13.length);
        feos.flush();
        byte[] expectedResult4 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\r', 8, 9};
        Assert.assertArrayEquals(expectedResult4, baos.toByteArray());

        baos = new ByteArrayOutputStream();
        feos = new EndstreamOutputStream(baos);
        byte[] tab14 = {1, 2, 3, 4, '\r'};
        byte[] tab15 = {'\n', 5, 6, 7, '\r' };
        byte[] tab16 = {8, 9, '\n'};
        byte[] tab17 = {'\r'}; // final CR is not to be discarded
        feos.write(tab14, 0, tab14.length);
        feos.write(tab15, 0, tab15.length);
        feos.write(tab16, 0, tab16.length);
        feos.write(tab17, 0, tab17.length);
        feos.flush();
        byte[] expectedResult5 = { 1, 2, 3, 4, '\r', '\n', 5, 6, 7, '\r', 8, 9, '\n', '\r'};
        Assert.assertArrayEquals(expectedResult5, baos.toByteArray());
    }
    
}
