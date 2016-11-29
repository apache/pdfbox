package org.apache.fontbox.ttf;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BufferedRandomAccessFileTest {

    @Test
    public void ensureReadFinishes() throws IOException {

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
