package org.apache.fontbox.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class PDFBoxInternalCleanerTest
{

    private static class ToCleanObject
    {
        private final static PDFBoxInternalCleaner cleaner = PDFBoxInternalCleaner.create();
        private final static AtomicInteger cleanups = new AtomicInteger(0);
        private final static AtomicInteger created = new AtomicInteger(0);

        public ToCleanObject()
        {
            created.incrementAndGet();
            cleaner.register(this, cleanups::incrementAndGet);
        }
    }

    @Test
    public void testCleaner()
    {
        for (int i = 0; i < 100; i++)
        {
            ToCleanObject object = new ToCleanObject();
            assertNotNull(object);
        }
        System.gc();
        dumpStats();
        for (int i = 0; i < 100_000; i++)
        {
            ToCleanObject object = new ToCleanObject();
            assertNotNull(object);
        }
        for (int i = 0; i < 5; i++)
            System.gc();
        dumpStats();
        for (int i = 0; i < 5; i++)
            System.gc();
        dumpStats();
    }

    private void dumpStats()
    {
        System.out.println("Created " + ToCleanObject.created.get() + " Cleanups Run: "
                + ToCleanObject.cleanups.get());
    }

}
