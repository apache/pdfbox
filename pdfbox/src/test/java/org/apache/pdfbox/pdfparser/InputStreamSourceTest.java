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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unittest for org.apache.pdfbox.pdfparser.InputStreamSource
 */
public class InputStreamSourceTest
{
    @Test
    public void testPositionReadFully() throws IOException
    {
        byte[] inputValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        InputStreamSource inputStreamSource = new InputStreamSource(bais);

        Assert.assertEquals(0, inputStreamSource.getPosition());
        inputStreamSource.readFully(5);
        Assert.assertEquals(5, inputStreamSource.getPosition());

        inputStreamSource.close();
    }

    @Test
    public void testPositionRead() throws IOException
    {
        byte[] inputValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        InputStreamSource inputStreamSource = new InputStreamSource(bais);

        Assert.assertEquals(0, inputStreamSource.getPosition());
        inputStreamSource.read();
        inputStreamSource.read();
        inputStreamSource.read();
        Assert.assertEquals(3, inputStreamSource.getPosition());

        inputStreamSource.close();
    }

    @Test
    public void testPositionReadBytes() throws IOException
    {
        byte[] inputValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        InputStreamSource inputStreamSource = new InputStreamSource(bais);

        Assert.assertEquals(0, inputStreamSource.getPosition());
        byte[] buffer = new byte[4];
        inputStreamSource.read(buffer);
        Assert.assertEquals(4, inputStreamSource.getPosition());

        inputStreamSource.read(buffer, 1, 2);
        Assert.assertEquals(6, inputStreamSource.getPosition());

        inputStreamSource.close();
    }

    @Test
    public void testPositionPeek() throws IOException
    {
        byte[] inputValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        InputStreamSource inputStreamSource = new InputStreamSource(bais);

        Assert.assertEquals(0, inputStreamSource.getPosition());
        inputStreamSource.readFully(6);
        Assert.assertEquals(6, inputStreamSource.getPosition());

        inputStreamSource.peek();
        Assert.assertEquals(6, inputStreamSource.getPosition());

        inputStreamSource.close();
    }

    @Test
    public void testPositionUnreadBytes() throws IOException
    {
        byte[] inputValues = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteArrayInputStream bais = new ByteArrayInputStream(inputValues);

        InputStreamSource inputStreamSource = new InputStreamSource(bais);

        Assert.assertEquals(0, inputStreamSource.getPosition());
        inputStreamSource.read();
        inputStreamSource.read();
        byte[] readBytes = inputStreamSource.readFully(6);
        Assert.assertEquals(8, inputStreamSource.getPosition());
        inputStreamSource.unread(readBytes);
        Assert.assertEquals(2, inputStreamSource.getPosition());
        inputStreamSource.read();
        Assert.assertEquals(3, inputStreamSource.getPosition());
        inputStreamSource.read(readBytes, 2, 4);
        Assert.assertEquals(7, inputStreamSource.getPosition());
        inputStreamSource.unread(readBytes, 2, 4);
        Assert.assertEquals(3, inputStreamSource.getPosition());

        inputStreamSource.close();
    }
}
