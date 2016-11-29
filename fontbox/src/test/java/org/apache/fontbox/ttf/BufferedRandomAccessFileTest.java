package org.apache.fontbox.ttf;

/*
 * Copyright 2016 The Apache Software Foundation.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Cameron Rollhieser
 */
public class BufferedRandomAccessFileTest
{

    /**
     * Before solving PDFBOX-3605, this test never ended.
     * 
     * @throws IOException
     */
    @Test
    public void ensureReadFinishes() throws IOException
    {
        final File file = File.createTempFile("apache-pdfbox", ".dat");

        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        final String content = "1234567890";
        outputStream.write(content.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        final byte[] readBuffer = new byte[2];
        final BufferedRandomAccessFile buffer = new BufferedRandomAccessFile(file, "r", 4);

        int amountRead;
        int totalAmountRead = 0;
        while ((amountRead = buffer.read(readBuffer, 0, 2)) != -1)
        {
            totalAmountRead += amountRead;
        }
        Assert.assertEquals(10, totalAmountRead);
    }
}
