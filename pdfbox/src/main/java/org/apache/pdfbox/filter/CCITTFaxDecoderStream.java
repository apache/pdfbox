/*
 * Copyright (c) 2012, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "TwelveMonkeys" nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.apache.pdfbox.filter;


import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * CCITT Modified Huffman RLE, Group 3 (T4) and Group 4 (T6) fax compression.
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @author <a href="https://github.com/Schmidor">Oliver Schmidtmer</a>
 * @author last modified by $Author: haraldk$
 * @version $Id: CCITTFaxDecoderStream.java,v 1.0 23.05.12 15:55 haraldk Exp$
 * 
 * Taken from commit fa0341f30237effe523e9905e672d709ffe9c6bd of 7.5.2016 from twelvemonkeys/imageio/plugins/tiff/CCITTFaxDecoderStream.java
 * 
 * Initial changes for PDFBox, discussed in PDFBOX-3338:
 * - added optionByteAligned to constructor and to each decodeRowType() method
 * - removed Validate() usages
 * - catch VALUE_EOL in decode1D()
 */
final class CCITTFaxDecoderStream extends FilterInputStream {
    // See TIFF 6.0 Specification, Section 10: "Modified Huffman Compression", page 43.

    private final int columns;
    private final byte[] decodedRow;

    private int decodedLength;
    private int decodedPos;

    // Need to take fill order into account (?) (use flip table?)
    private final int fillOrder;
    private final int type;

    private int[] changesReferenceRow;
    private int[] changesCurrentRow;
    private int changesReferenceRowCount;
    private int changesCurrentRowCount;
    private int lastChangingElement = 0;

    private boolean optionG32D = false;

    @SuppressWarnings("unused") // Leading zeros for aligning EOL
    private boolean optionG3Fill = false;

    private boolean optionUncompressed = false;
    private boolean optionByteAligned = false;

    CCITTFaxDecoderStream(final InputStream stream, final int columns, final int type, final int fillOrder,
                                 final long options) {
        super(stream);

        this.columns = columns;
        // We know this is only used for b/w (1 bit)
        this.decodedRow = new byte[(columns + 7) / 8];
        this.type = type;
        
        this.fillOrder = fillOrder;

        this.changesReferenceRow = new int[columns + 2];
        this.changesCurrentRow = new int[columns + 2];

        switch (type) {
            case TIFFExtension.COMPRESSION_CCITT_MODIFIED_HUFFMAN_RLE:
                optionByteAligned = (options & TIFFExtension.GROUP3OPT_BYTEALIGNED) != 0;
                break;
            case TIFFExtension.COMPRESSION_CCITT_T4:
                optionG32D = (options & TIFFExtension.GROUP3OPT_2DENCODING) != 0;
                optionG3Fill = (options & TIFFExtension.GROUP3OPT_FILLBITS) != 0;
                optionUncompressed = (options & TIFFExtension.GROUP3OPT_UNCOMPRESSED) != 0;
                optionByteAligned = (options & TIFFExtension.GROUP3OPT_BYTEALIGNED) != 0;
                break;
            case TIFFExtension.COMPRESSION_CCITT_T6:
                optionUncompressed = (options & TIFFExtension.GROUP4OPT_UNCOMPRESSED) != 0;
                optionByteAligned = (options & TIFFExtension.GROUP4OPT_BYTEALIGNED) != 0;
                break;
        }

    }

    private void fetch() throws IOException {
        if (decodedPos >= decodedLength) {
            decodedLength = 0;

            try {
                decodeRow();
            }
            catch (EOFException e) {
                // TODO: Rewrite to avoid throw/catch for normal flow...
                if (decodedLength != 0) {
                    throw e;
                }

                // ..otherwise, just client code trying to read past the end of
                // stream
                decodedLength = -1;
            }

            decodedPos = 0;
        }
    }

    private void decode1D() throws IOException {
        int index = 0;
        boolean white = true;
        changesCurrentRowCount = 0;

        do {
            int completeRun;

            if (white) {
                completeRun = decodeRun(whiteRunTree);
            }
            else {
                completeRun = decodeRun(blackRunTree);
            }
            
            if (completeRun == VALUE_EOL)
            {
                continue;
            }

            index += completeRun;
            changesCurrentRow[changesCurrentRowCount++] = index;

            // Flip color for next run
            white = !white;
        } while (index < columns);
    }

    private void decode2D() throws IOException {
        changesReferenceRowCount = changesCurrentRowCount;
        int[] tmp = changesCurrentRow;
        changesCurrentRow = changesReferenceRow;
        changesReferenceRow = tmp;

        boolean white = true;
        int index = 0;
        changesCurrentRowCount = 0;

        mode: while (index < columns) {
            // read mode
            Node n = codeTree.root;

            while (true) {
                n = n.walk(readBit());

                if (n == null) {
                    continue mode;
                }
                else if (n.isLeaf) {
                    switch (n.value) {
                        case VALUE_HMODE:
                            int runLength;
                            runLength = decodeRun(white ? whiteRunTree : blackRunTree);
                            index += runLength;
                            changesCurrentRow[changesCurrentRowCount++] = index;

                            runLength = decodeRun(white ? blackRunTree : whiteRunTree);
                            index += runLength;
                            changesCurrentRow[changesCurrentRowCount++] = index;
                            break;

                        case VALUE_PASSMODE:
                            int pChangingElement = getNextChangingElement(index, white) + 1;

                            if (pChangingElement >= changesReferenceRowCount) {
                                index = columns;
                            }
                            else {
                                index = changesReferenceRow[pChangingElement];
                            }

                            break;

                        default:
                            // Vertical mode (-3 to 3)
                            int vChangingElement = getNextChangingElement(index, white);

                            if (vChangingElement >= changesReferenceRowCount || vChangingElement == -1) {
                                index = columns + n.value;
                            }
                            else {
                                index = changesReferenceRow[vChangingElement] + n.value;
                            }

                            changesCurrentRow[changesCurrentRowCount] = index;
                            changesCurrentRowCount++;
                            white = !white;

                            break;
                    }

                    continue mode;
                }
            }
        }
    }

    private int getNextChangingElement(final int a0, final boolean white) {
        int start = (lastChangingElement & 0xFFFFFFFE) + (white ? 0 : 1);
        if (start > 2) {
            start -= 2;
        }

        if (a0 == 0) {
            return start;
        }

        for (int i = start; i < changesReferenceRowCount; i += 2) {
            if (a0 < changesReferenceRow[i]) {
                lastChangingElement = i;
                return i;
            }
        }

        return -1;
    }

    private void decodeRowType2() throws IOException {
        if (optionByteAligned)
        {
            resetBuffer();
        }
        decode1D();
    }

    private void decodeRowType4() throws IOException {
        if(optionByteAligned) {
            bufferPos = -1;  // Skip remaining bits and fetch the next byte at row start
        }
        eof: while (true) {
            // read till next EOL code
            Node n = eolOnlyTree.root;

            while (true) {
                n = n.walk(readBit());

                if (n == null) {
                    continue eof;
                }

                if (n.isLeaf) {
                    break eof;
                }
            }
        }

        if (!optionG32D || readBit()) {
            decode1D();
        }
        else {
            decode2D();
        }
    }

    private void decodeRowType6() throws IOException {
        if(optionByteAligned) {
            bufferPos = -1;  // Skip remaining bits and fetch the next byte at row start
        }
        decode2D();
    }

    private void decodeRow() throws IOException {
        switch (type) {
            case TIFFExtension.COMPRESSION_CCITT_MODIFIED_HUFFMAN_RLE:
                decodeRowType2();
                break;
            case TIFFExtension.COMPRESSION_CCITT_T4:
                decodeRowType4();
                break;
            case TIFFExtension.COMPRESSION_CCITT_T6:
                decodeRowType6();
                break;
        }

        int index = 0;
        boolean white = true;

            lastChangingElement = 0;
            for (int i = 0; i <= changesCurrentRowCount; i++) {
            int nextChange = columns;

            if (i != changesCurrentRowCount) {
                nextChange = changesCurrentRow[i];
            }

            if (nextChange > columns) {
                nextChange = columns;
            }

            int byteIndex = index / 8;

            while (index % 8 != 0 && (nextChange - index) > 0) {
                decodedRow[byteIndex] |= (white ? 0 : 1 << (7 - ((index) % 8)));
                index++;
            }

            if (index % 8 == 0) {
                byteIndex = index / 8;
                final byte value = (byte) (white ? 0x00 : 0xff);

                while ((nextChange - index) > 7) {
                    decodedRow[byteIndex] = value;
                    index += 8;
                    ++byteIndex;
                }
            }

            while ((nextChange - index) > 0) {
                if (index % 8 == 0) {
                    decodedRow[byteIndex] = 0;
                }

                decodedRow[byteIndex] |= (white ? 0 : 1 << (7 - ((index) % 8)));
                index++;
            }

            white = !white;
        }
        
        if (index != columns) {
            throw new IOException("Sum of run-lengths does not equal scan line width: " + index + " > " + columns);
        }

        decodedLength = (index + 7) / 8;
    }

    private int decodeRun(final Tree tree) throws IOException {     
        int total = 0;

        Node n = tree.root;

        while (true) {
            boolean bit = readBit();
            n = n.walk(bit);

            if (n == null) {
                throw new IOException("Unknown code in Huffman RLE stream");
            }

            if (n.isLeaf) {
                total += n.value;
                if (n.value < 64) {
                    return total;
                }
                else {
                    n = tree.root;
                }
            }
        }
    }

    private void resetBuffer() throws IOException {
        for (int i = 0; i < decodedRow.length; i++) {
            decodedRow[i] = 0;
        }

        while (true) {
            if (bufferPos == -1) {
                return;
            }

            readBit();
        }
    }

    int buffer = -1;
    int bufferPos = -1;

    private boolean readBit() throws IOException {
        if (bufferPos < 0 || bufferPos > 7) {
            buffer = in.read();

            if (buffer == -1) {
                throw new EOFException("Unexpected end of Huffman RLE stream");
            }

            bufferPos = 0;
        }

        boolean isSet;

        if (fillOrder == TIFFExtension.FILL_LEFT_TO_RIGHT) {
            isSet = ((buffer >> (7 - bufferPos)) & 1) == 1;
        }
        else {
            isSet = ((buffer >> (bufferPos)) & 1) == 1;
        }

        bufferPos++;

        if (bufferPos > 7) {
            bufferPos = -1;
        }

        return isSet;
    }

    @Override
    public int read() throws IOException {
        if (decodedLength < 0) {
            return 0x0;
        }

        if (decodedPos >= decodedLength) {
            fetch();

            if (decodedLength < 0) {
                return 0x0;
            }
        }

        return decodedRow[decodedPos++] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (decodedLength < 0) {
            //TODO better? Math.min(off + len, b.length)
            Arrays.fill(b, off, off + len, (byte) 0x0);
            return len;
        }

        if (decodedPos >= decodedLength) {
            fetch();

            if (decodedLength < 0) {
                Arrays.fill(b, off, off + len, (byte) 0x0);
                return len;
            }
        }

        int read = Math.min(decodedLength - decodedPos, len);
        System.arraycopy(decodedRow, decodedPos, b, off, read);
        decodedPos += read;

        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        if (decodedLength < 0) {
            return -1;
        }

        if (decodedPos >= decodedLength) {
            fetch();

            if (decodedLength < 0) {
                return -1;
            }
        }

        int skipped = (int) Math.min(decodedLength - decodedPos, n);
        decodedPos += skipped;

        return skipped;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    private static final class Node {
        Node left;
        Node right;

        int value; // > 63 non term.

        boolean canBeFill = false;
        boolean isLeaf = false;

        void set(final boolean next, final Node node) {
            if (!next) {
                left = node;
            }
            else {
                right = node;
            }
        }

        Node walk(final boolean next) {
            return next ? right : left;
        }

        @Override
        public String toString() {
            return "[leaf=" + isLeaf + ", value=" + value + ", canBeFill=" + canBeFill + "]";
        }
    }

    private static final class Tree {
        final Node root = new Node();

        void fill(final int depth, final int path, final int value) throws IOException {
            Node current = root;

            for (int i = 0; i < depth; i++) {
                int bitPos = depth - 1 - i;
                boolean isSet = ((path >> bitPos) & 1) == 1;
                Node next = current.walk(isSet);

                if (next == null) {
                    next = new Node();

                    if (i == depth - 1) {
                        next.value = value;
                        next.isLeaf = true;
                    }

                    if (path == 0) {
                        next.canBeFill = true;
                    }

                    current.set(isSet, next);
                }
                else {
                    if (next.isLeaf) {
                        throw new IOException("node is leaf, no other following");
                    }
                }

                current = next;
            }
        }

        void fill(final int depth, final int path, final Node node) throws IOException {
            Node current = root;

            for (int i = 0; i < depth; i++) {
                int bitPos = depth - 1 - i;
                boolean isSet = ((path >> bitPos) & 1) == 1;
                Node next = current.walk(isSet);

                if (next == null) {
                    if (i == depth - 1) {
                        next = node;
                    }
                    else {
                        next = new Node();
                    }

                    if (path == 0) {
                        next.canBeFill = true;
                    }

                    current.set(isSet, next);
                }
                else {
                    if (next.isLeaf) {
                        throw new IOException("node is leaf, no other following");
                    }
                }

                current = next;
            }
        }
    }

    static final short[][] BLACK_CODES = {
            { // 2 bits
              0x2, 0x3,
              },
            { // 3 bits
              0x2, 0x3,
              },
            { // 4 bits
              0x2, 0x3,
              },
            { // 5 bits
              0x3,
              },
            { // 6 bits
              0x4, 0x5,
              },
            { // 7 bits
              0x4, 0x5, 0x7,
              },
            { // 8 bits
              0x4, 0x7,
              },
            { // 9 bits
              0x18,
              },
            { // 10 bits
              0x17, 0x18, 0x37, 0x8, 0xf,
              },
            { // 11 bits
              0x17, 0x18, 0x28, 0x37, 0x67, 0x68, 0x6c, 0x8, 0xc, 0xd,
              },
            { // 12 bits
              0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x1c, 0x1d, 0x1e, 0x1f, 0x24, 0x27, 0x28, 0x2b, 0x2c, 0x33,
              0x34, 0x35, 0x37, 0x38, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x64, 0x65,
              0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0xc8, 0xc9, 0xca, 0xcb, 0xcc, 0xcd, 0xd2, 0xd3,
              0xd4, 0xd5, 0xd6, 0xd7, 0xda, 0xdb,
              },
            { // 13 bits
              0x4a, 0x4b, 0x4c, 0x4d, 0x52, 0x53, 0x54, 0x55, 0x5a, 0x5b, 0x64, 0x65, 0x6c, 0x6d, 0x72, 0x73,
              0x74, 0x75, 0x76, 0x77,
              }
    };
    static final short[][] BLACK_RUN_LENGTHS = {
            { // 2 bits
              3, 2,
              },
            { // 3 bits
              1, 4,
              },
            { // 4 bits
              6, 5,
              },
            { // 5 bits
              7,
              },
            { // 6 bits
              9, 8,
              },
            { // 7 bits
              10, 11, 12,
              },
            { // 8 bits
              13, 14,
              },
            { // 9 bits
              15,
              },
            { // 10 bits
              16, 17, 0, 18, 64,
              },
            { // 11 bits
              24, 25, 23, 22, 19, 20, 21, 1792, 1856, 1920,
              },
            { // 12 bits
              1984, 2048, 2112, 2176, 2240, 2304, 2368, 2432, 2496, 2560, 52, 55, 56, 59, 60, 320, 384, 448, 53,
              54, 50, 51, 44, 45, 46, 47, 57, 58, 61, 256, 48, 49, 62, 63, 30, 31, 32, 33, 40, 41, 128, 192, 26,
              27, 28, 29, 34, 35, 36, 37, 38, 39, 42, 43,
              },
            { // 13 bits
              640, 704, 768, 832, 1280, 1344, 1408, 1472, 1536, 1600, 1664, 1728, 512, 576, 896, 960, 1024, 1088,
              1152, 1216,
              }
    };

    public static final short[][] WHITE_CODES = {
            { // 4 bits
              0x7, 0x8, 0xb, 0xc, 0xe, 0xf,
              },
            { // 5 bits
              0x12, 0x13, 0x14, 0x1b, 0x7, 0x8,
              },
            { // 6 bits
              0x17, 0x18, 0x2a, 0x2b, 0x3, 0x34, 0x35, 0x7, 0x8,
              },
            { // 7 bits
              0x13, 0x17, 0x18, 0x24, 0x27, 0x28, 0x2b, 0x3, 0x37, 0x4, 0x8, 0xc,
              },
            { // 8 bits
              0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x1a, 0x1b, 0x2, 0x24, 0x25, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d,
              0x3, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x4, 0x4a, 0x4b, 0x5, 0x52, 0x53, 0x54, 0x55, 0x58, 0x59,
              0x5a, 0x5b, 0x64, 0x65, 0x67, 0x68, 0xa, 0xb,
              },
            { // 9 bits
              0x98, 0x99, 0x9a, 0x9b, 0xcc, 0xcd, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xdb,
              },
            { // 10 bits
            },
            { // 11 bits
              0x8, 0xc, 0xd,
              },
            { // 12 bits
              0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x1c, 0x1d, 0x1e, 0x1f,
              }
    };

    public static final short[][] WHITE_RUN_LENGTHS = {
            { // 4 bits
              2, 3, 4, 5, 6, 7,
              },
            { // 5 bits
              128, 8, 9, 64, 10, 11,
              },
            { // 6 bits
              192, 1664, 16, 17, 13, 14, 15, 1, 12,
              },
            { // 7 bits
              26, 21, 28, 27, 18, 24, 25, 22, 256, 23, 20, 19,
              },
            { // 8 bits
              33, 34, 35, 36, 37, 38, 31, 32, 29, 53, 54, 39, 40, 41, 42, 43, 44, 30, 61, 62, 63, 0, 320, 384, 45,
              59, 60, 46, 49, 50, 51, 52, 55, 56, 57, 58, 448, 512, 640, 576, 47, 48,
              },
            { // 9 bits
              1472, 1536, 1600, 1728, 704, 768, 832, 896, 960, 1024, 1088, 1152, 1216, 1280, 1344, 1408,
              },
            { // 10 bits
            },
            { // 11 bits
              1792, 1856, 1920,
              },
            { // 12 bits
              1984, 2048, 2112, 2176, 2240, 2304, 2368, 2432, 2496, 2560,
              }
    };

    final static Node EOL;
    final static Node FILL;
    final static Tree blackRunTree;
    final static Tree whiteRunTree;
    final static Tree eolOnlyTree;
    final static Tree codeTree;

    final static int VALUE_EOL = -2000;
    final static int VALUE_FILL = -1000;
    final static int VALUE_PASSMODE = -3000;
    final static int VALUE_HMODE = -4000;

    static {
        EOL = new Node();
        EOL.isLeaf = true;
        EOL.value = VALUE_EOL;
        FILL = new Node();
        FILL.value = VALUE_FILL;
        FILL.left = FILL;
        FILL.right = EOL;

        eolOnlyTree = new Tree();
        try {
            eolOnlyTree.fill(12, 0, FILL);
            eolOnlyTree.fill(12, 1, EOL);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        blackRunTree = new Tree();
        try {
            for (int i = 0; i < BLACK_CODES.length; i++) {
                for (int j = 0; j < BLACK_CODES[i].length; j++) {
                    blackRunTree.fill(i + 2, BLACK_CODES[i][j], BLACK_RUN_LENGTHS[i][j]);
                }
            }
            blackRunTree.fill(12, 0, FILL);
            blackRunTree.fill(12, 1, EOL);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        whiteRunTree = new Tree();
        try {
            for (int i = 0; i < WHITE_CODES.length; i++) {
                for (int j = 0; j < WHITE_CODES[i].length; j++) {
                    whiteRunTree.fill(i + 4, WHITE_CODES[i][j], WHITE_RUN_LENGTHS[i][j]);
                }
            }

            whiteRunTree.fill(12, 0, FILL);
            whiteRunTree.fill(12, 1, EOL);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        codeTree = new Tree();
        try {
            codeTree.fill(4, 1, VALUE_PASSMODE); // pass mode
            codeTree.fill(3, 1, VALUE_HMODE); // H mode
            codeTree.fill(1, 1, 0); // V(0)
            codeTree.fill(3, 3, 1); // V_R(1)
            codeTree.fill(6, 3, 2); // V_R(2)
            codeTree.fill(7, 3, 3); // V_R(3)
            codeTree.fill(3, 2, -1); // V_L(1)
            codeTree.fill(6, 2, -2); // V_L(2)
            codeTree.fill(7, 2, -3); // V_L(3)
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}

