package org.apache.pdfbox.io;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class RandomAccessInputStreamTest
{
    @Test
    public void shouldCleanupMemoryOnClose() throws IOException
    {
        MemoryCleanableRandomAccessRead mock = Mockito.mock(MemoryCleanableRandomAccessRead.class);
        new RandomAccessInputStream(mock).close();
        Mockito.verify(mock).cleanupMemory();
    }

    public interface MemoryCleanableRandomAccessRead extends RandomAccessRead, MemoryCleanable
    {

    }
}
