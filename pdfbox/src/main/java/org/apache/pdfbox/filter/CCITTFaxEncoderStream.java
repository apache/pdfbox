/*
 * Copyright (c) 2013, Harald Kuhr
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * CCITT Modified Group 4 (T6) fax compression.
 *
 * @author <a href="mailto:mail@schmidor.de">Oliver Schmidtmer</a>
 *
 * Taken from commit 047884e3d9e1b30516c79b147ead763303dc9bcb of 21.4.2016 from
 * twelvemonkeys/imageio/plugins/tiff/CCITTFaxEncoderStream.java
 *
 * Initial changes for PDFBox:
 * - removed Validate
 * - G4 compression only
 * - removed options
 */
final class CCITTFaxEncoderStream extends OutputStream {
    
    private int currentBufferLength = 0;
    private final byte[] inputBuffer;
    private final int inputBufferLength;
    private final int columns;
    private final int rows;

    private int[] changesCurrentRow;
    private int[] changesReferenceRow;
    private int currentRow = 0;
    private int changesCurrentRowLength = 0;
    private int changesReferenceRowLength = 0;
    private byte outputBuffer = 0;
    private byte outputBufferBitLength = 0;
    private final int fillOrder;
    private final OutputStream stream;

    CCITTFaxEncoderStream(final OutputStream stream, final int columns, final int rows, final int fillOrder) {

        this.stream = stream;
        this.columns = columns;
        this.rows = rows;
        this.fillOrder = fillOrder;

        this.changesReferenceRow = new int[columns];
        this.changesCurrentRow = new int[columns];
        
        inputBufferLength = (columns + 7) / 8;
        inputBuffer = new byte[inputBufferLength];
    }

    @Override
    public void write(int b) throws IOException {
        inputBuffer[currentBufferLength] = (byte) b;
        currentBufferLength++;

        if (currentBufferLength == inputBufferLength) {
            encodeRow();
            currentBufferLength = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    private void encodeRow() throws IOException {
        currentRow++;
        int[] tmp = changesReferenceRow;
        changesReferenceRow = changesCurrentRow;
        changesCurrentRow = tmp;
        changesReferenceRowLength = changesCurrentRowLength;
        changesCurrentRowLength = 0;

        int index = 0;
        boolean white = true;
        while (index < columns) {
            int byteIndex = index / 8;
            int bit = index % 8;
            if ((((inputBuffer[byteIndex] >> (7 - bit)) & 1) == 1) == (white)) {
                changesCurrentRow[changesCurrentRowLength] = index;
                changesCurrentRowLength++;
                white = !white;
            }
            index++;
        }

        encodeRowType6();

        if (currentRow == rows) {
            writeEOL();
            writeEOL();
            fill();
        }
    }


    private void encodeRowType6() throws IOException {
        encode2D();
    }

    private int[] getNextChanges(int pos, boolean white) {
        int[] result = new int[] {columns, columns};
        for (int i = 0; i < changesCurrentRowLength; i++) {
            if (pos < changesCurrentRow[i] || (pos == 0 && white)) {
                result[0] = changesCurrentRow[i];
                if ((i + 1) < changesCurrentRowLength) {
                    result[1] = changesCurrentRow[i + 1];
                }
                break;
            }
        }

        return result;
    }

    private void writeRun(int runLength, boolean white) throws IOException {
        int nonterm = runLength / 64;
        Code[] codes = white ? WHITE_NONTERMINATING_CODES : BLACK_NONTERMINATING_CODES;
        while (nonterm > 0) {
            if (nonterm >= codes.length) {
                write(codes[codes.length - 1].code, codes[codes.length - 1].length);
                nonterm -= codes.length;
            }
            else {
                write(codes[nonterm - 1].code, codes[nonterm - 1].length);
                nonterm = 0;
            }
        }

        Code c = white ? WHITE_TERMINATING_CODES[runLength % 64] : BLACK_TERMINATING_CODES[runLength % 64];
        write(c.code, c.length);
    }

    private void encode2D() throws IOException {
        boolean white = true;
        int index = 0; // a0
        while (index < columns) {
            int[] nextChanges = getNextChanges(index, white); // a1, a2

            int[] nextRefs = getNextRefChanges(index, white); // b1, b2

            int difference = nextChanges[0] - nextRefs[0];
            if (nextChanges[0] > nextRefs[1]) {
                // PMODE
                write(1, 4);
                index = nextRefs[1];
            }
            else if (difference > 3 || difference < -3) {
                // HMODE
                write(1, 3);
                writeRun(nextChanges[0] - index, white);
                writeRun(nextChanges[1] - nextChanges[0], !white);
                index = nextChanges[1];

            }
            else {
                // VMODE
                switch (difference) {
                    case 0:
                        write(1, 1);
                        break;
                    case 1:
                        write(3, 3);
                        break;
                    case 2:
                        write(3, 6);
                        break;
                    case 3:
                        write(3, 7);
                        break;
                    case -1:
                        write(2, 3);
                        break;
                    case -2:
                        write(2, 6);
                        break;
                    case -3:
                        write(2, 7);
                        break;
                }
                white = !white;
                index = nextRefs[0] + difference;
            }
        }
    }

    private int[] getNextRefChanges(int a0, boolean white) {
        int[] result = new int[] {columns, columns};
        for (int i = (white ? 0 : 1); i < changesReferenceRowLength; i += 2) {
            if (changesReferenceRow[i] > a0 || (a0 == 0 && i == 0)) {
                result[0] = changesReferenceRow[i];
                if ((i + 1) < changesReferenceRowLength) {
                    result[1] = changesReferenceRow[i + 1];
                }
                break;
            }
        }
        return result;
    }

    private void write(int code, int codeLength) throws IOException {

        for (int i = 0; i < codeLength; i++) {
            boolean codeBit = ((code >> (codeLength - i - 1)) & 1) == 1;
            if (fillOrder == TIFFExtension.FILL_LEFT_TO_RIGHT) {
                outputBuffer |= (codeBit ? 1 << (7 - ((outputBufferBitLength) % 8)) : 0);
            }
            else {
                outputBuffer |= (codeBit ? 1 << (((outputBufferBitLength) % 8)) : 0);
            }
            outputBufferBitLength++;

            if (outputBufferBitLength == 8) {
                stream.write(outputBuffer);
                clearOutputBuffer();
            }
        }
    }

    private void writeEOL() throws IOException {
        write(1, 12);
    }

    private void fill() throws IOException {
        if (outputBufferBitLength != 0) {
            stream.write(outputBuffer);
        }
        clearOutputBuffer();
    }

    private void clearOutputBuffer() {
        outputBuffer = 0;
        outputBufferBitLength = 0;
    }

    private static class Code {
        private Code(int code, int length) {
            this.code = code;
            this.length = length;
        }

        final int code;
        final int length;
    }

    private static final Code[] WHITE_TERMINATING_CODES;

    private static final Code[] WHITE_NONTERMINATING_CODES;

    private static final Code[] BLACK_TERMINATING_CODES;

    private static final Code[] BLACK_NONTERMINATING_CODES;

    static {
        // Setup HUFFMAN Codes
        WHITE_TERMINATING_CODES = new Code[64];
        WHITE_NONTERMINATING_CODES = new Code[40];
        for (int i = 0; i < CCITTFaxDecoderStream.WHITE_CODES.length; i++) {
            int bitLength = i + 4;
            for (int j = 0; j < CCITTFaxDecoderStream.WHITE_CODES[i].length; j++) {
                int value = CCITTFaxDecoderStream.WHITE_RUN_LENGTHS[i][j];
                int code = CCITTFaxDecoderStream.WHITE_CODES[i][j];

                if (value < 64) {
                    WHITE_TERMINATING_CODES[value] = new Code(code, bitLength);
                }
                else {
                    WHITE_NONTERMINATING_CODES[(value / 64) - 1] = new Code(code, bitLength);
                }
            }
        }

        BLACK_TERMINATING_CODES = new Code[64];
        BLACK_NONTERMINATING_CODES = new Code[40];
        for (int i = 0; i < CCITTFaxDecoderStream.BLACK_CODES.length; i++) {
            int bitLength = i + 2;
            for (int j = 0; j < CCITTFaxDecoderStream.BLACK_CODES[i].length; j++) {
                int value = CCITTFaxDecoderStream.BLACK_RUN_LENGTHS[i][j];
                int code = CCITTFaxDecoderStream.BLACK_CODES[i][j];

                if (value < 64) {
                    BLACK_TERMINATING_CODES[value] = new Code(code, bitLength);
                }
                else {
                    BLACK_NONTERMINATING_CODES[(value / 64) - 1] = new Code(code, bitLength);
                }
            }
        }
    }
}
