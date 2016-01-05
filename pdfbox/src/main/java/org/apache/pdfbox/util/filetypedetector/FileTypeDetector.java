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
package org.apache.pdfbox.util.filetypedetector;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.pdfbox.util.Charsets;

/**
 * @author Drew Noakes
 *
 * code taken from https://github.com/drewnoakes/metadata-extractor
 *
 * 2016-01-04
 *
 * latest commit number 73f1a48
 *
 * Examines the a file's first bytes and estimates the file's type.
 */
public final class FileTypeDetector
{
    private static final ByteTrie<FileType> root;

    static
    {
        root = new ByteTrie<FileType>();
        root.setDefaultValue(FileType.UNKNOWN);

        // https://en.wikipedia.org/wiki/List_of_file_signatures

        root.addPath(FileType.JPEG, new byte[]{(byte)0xff, (byte)0xd8});
        root.addPath(FileType.TIFF, "II".getBytes(Charsets.ISO_8859_1), new byte[]{0x2a, 0x00});
        root.addPath(FileType.TIFF, "MM".getBytes(Charsets.ISO_8859_1), new byte[]{0x00, 0x2a});
        root.addPath(FileType.PSD, "8BPS".getBytes(Charsets.ISO_8859_1));
        root.addPath(FileType.PNG, new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52});
        // TODO technically there are other very rare magic numbers for OS/2 BMP files...
        root.addPath(FileType.BMP, "BM".getBytes(Charsets.ISO_8859_1)); 
        root.addPath(FileType.GIF, "GIF87a".getBytes(Charsets.ISO_8859_1));
        root.addPath(FileType.GIF, "GIF89a".getBytes(Charsets.ISO_8859_1));
        root.addPath(FileType.ICO, new byte[]{0x00, 0x00, 0x01, 0x00});
        // multiple PCX versions, explicitly listed
        root.addPath(FileType.PCX, new byte[]{0x0A, 0x00, 0x01}); 
        root.addPath(FileType.PCX, new byte[]{0x0A, 0x02, 0x01});
        root.addPath(FileType.PCX, new byte[]{0x0A, 0x03, 0x01});
        root.addPath(FileType.PCX, new byte[]{0x0A, 0x05, 0x01});
        root.addPath(FileType.RIFF, "RIFF".getBytes(Charsets.ISO_8859_1));

        root.addPath(FileType.ARW, "II".getBytes(Charsets.ISO_8859_1), new byte[]{0x2a, 0x00, 0x08, 0x00});
        root.addPath(FileType.CRW, "II".getBytes(Charsets.ISO_8859_1), new byte[]{0x1a, 0x00, 0x00, 0x00}, "HEAPCCDR".getBytes(Charsets.ISO_8859_1));
        root.addPath(FileType.CR2, "II".getBytes(Charsets.ISO_8859_1), new byte[]{0x2a, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52});
        root.addPath(FileType.NEF, "MM".getBytes(Charsets.ISO_8859_1), new byte[]{0x00, 0x2a, 0x00, 0x00, 0x00, (byte)0x80, 0x00});
        root.addPath(FileType.ORF, "IIRO".getBytes(Charsets.ISO_8859_1), new byte[]{(byte)0x08, 0x00});
        root.addPath(FileType.ORF, "IIRS".getBytes(Charsets.ISO_8859_1), new byte[]{(byte)0x08, 0x00});
        root.addPath(FileType.RAF, "FUJIFILMCCD-RAW".getBytes(Charsets.ISO_8859_1));
        root.addPath(FileType.RW2, "II".getBytes(Charsets.ISO_8859_1), new byte[]{0x55, 0x00});
    }

    private FileTypeDetector() throws Exception
    {
    }

    /**
     * Examines the a file's first bytes and estimates the file's type.
     * <p>
     * Requires a {@link BufferedInputStream} in order to mark and reset the stream to the position
     * at which it was provided to this method once completed.
     * <p>
     * Requires the stream to contain at least eight bytes.
     *
     * @param inputStream a buffered input stream of the file to examine.
     * @return the file type.
     * @throws IOException if an IO error occurred or the input stream ended unexpectedly.
     */
    public static FileType detectFileType(final BufferedInputStream inputStream) throws IOException
    {
        if (!inputStream.markSupported())
        {
            throw new IOException("Stream must support mark/reset");
        }

        int maxByteCount = root.getMaxDepth();

        inputStream.mark(maxByteCount);

        byte[] bytes = new byte[maxByteCount];
        int bytesRead = inputStream.read(bytes);

        if (bytesRead == -1)
        {
            throw new IOException("Stream ended before file's magic number could be determined.");
        }

        inputStream.reset();

        //noinspection ConstantConditions
        return root.find(bytes);
    }
}
