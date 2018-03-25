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
package org.apache.pdfbox.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;

/**
 * Helper class to contain predictor decoding used by Flate and LZW filter. 
 * To see the history, look at the FlateFilter class.
 */
public final class Predictor
{

    private Predictor()
    {
    }

    /**
     * Decodes a single line of data in-place.
     * @param predictor Predictor value for the current line
     * @param colors Number of color components, from decode parameters.
     * @param bitsPerComponent Number of bits per components, from decode parameters.
     * @param columns Number samples in a row, from decode parameters.
     * @param actline Current (active) line to decode. Data will be decoded in-place,
     *                i.e. - the contents of this buffer will be modified.
     * @param lastline The previous decoded line. When decoding the first line, this
     *                 parameter should be an empty byte array of the same length as
     *                 <code>actline</code>.
     */
    static void decodePredictorRow(int predictor, int colors, int bitsPerComponent, int columns, byte[] actline, byte[] lastline)
    {
        if (predictor == 1)
        {
            // no prediction
            return;
        }
        final int bitsPerPixel = colors * bitsPerComponent;
        final int bytesPerPixel = (bitsPerPixel + 7) / 8;
        final int rowlength = actline.length;
        switch (predictor)
        {
            case 2:
                // PRED TIFF SUB
                if (bitsPerComponent == 8)
                {
                    // for 8 bits per component it is the same algorithm as PRED SUB of PNG format
                    for (int p = bytesPerPixel; p < rowlength; p++)
                    {
                        int sub = actline[p] & 0xff;
                        int left = actline[p - bytesPerPixel] & 0xff;
                        actline[p] = (byte) (sub + left);
                    }
                    break;
                }
                if (bitsPerComponent == 16)
                {
                    for (int p = bytesPerPixel; p < rowlength; p += 2)
                    {
                        int sub = ((actline[p] & 0xff) << 8) + (actline[p + 1] & 0xff);
                        int left = (((actline[p - bytesPerPixel] & 0xff) << 8)
                                + (actline[p - bytesPerPixel + 1] & 0xff));
                        actline[p] = (byte) (((sub + left) >> 8) & 0xff);
                        actline[p + 1] = (byte) ((sub + left) & 0xff);
                    }
                    break;
                }
                if (bitsPerComponent == 1 && colors == 1)
                {
                    // bytesPerPixel cannot be used:
                    // "A row shall occupy a whole number of bytes, rounded up if necessary.
                    // Samples and their components shall be packed into bytes
                    // from high-order to low-order bits."
                    for (int p = 0; p < rowlength; p++)
                    {
                        for (int bit = 7; bit >= 0; --bit)
                        {
                            int sub = (actline[p] >> bit) & 1;
                            if (p == 0 && bit == 7)
                            {
                                continue;
                            }
                            int left;
                            if (bit == 7)
                            {
                                // use bit #0 from previous byte
                                left = actline[p - 1] & 1;
                            }
                            else
                            {
                                // use "previous" bit
                                left = (actline[p] >> (bit + 1)) & 1;
                            }
                            if (((sub + left) & 1) == 0)
                            {
                                // reset bit
                                actline[p] = (byte) (actline[p] & ~(1 << bit));
                            }
                            else
                            {
                                // set bit
                                actline[p] = (byte) (actline[p] | (1 << bit));
                            }
                        }
                    }
                    break;
                }
                // everything else, i.e. bpc 2 and 4, but has been tested for bpc 1 and 8 too
                int elements = columns * colors;
                for (int p = colors; p < elements; ++p)
                {
                    int bytePosSub = p * bitsPerComponent / 8;
                    int bitPosSub = 8 - p * bitsPerComponent % 8 - bitsPerComponent;
                    int bytePosLeft = (p - colors) * bitsPerComponent / 8;
                    int bitPosLeft = 8 - (p - colors) * bitsPerComponent % 8 - bitsPerComponent;

                    int sub = getBitSeq(actline[bytePosSub], bitPosSub, bitsPerComponent);
                    int left = getBitSeq(actline[bytePosLeft], bitPosLeft, bitsPerComponent);
                    actline[bytePosSub] = (byte) calcSetBitSeq(actline[bytePosSub], bitPosSub, bitsPerComponent, sub + left);
                }
                break;
            case 10:
                // PRED NONE
                // do nothing
                break;
            case 11:
                // PRED SUB
                for (int p = bytesPerPixel; p < rowlength; p++)
                {
                    int sub = actline[p];
                    int left = actline[p - bytesPerPixel];
                    actline[p] = (byte) (sub + left);
                }
                break;
            case 12:
                // PRED UP
                for (int p = 0; p < rowlength; p++)
                {
                    int up = actline[p] & 0xff;
                    int prior = lastline[p] & 0xff;
                    actline[p] = (byte) ((up + prior) & 0xff);
                }
                break;
            case 13:
                // PRED AVG
                for (int p = 0; p < rowlength; p++)
                {
                    int avg = actline[p] & 0xff;
                    int left = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff : 0;
                    int up = lastline[p] & 0xff;
                    actline[p] = (byte) ((avg + (left + up) / 2) & 0xff);
                }
                break;
            case 14:
                // PRED PAETH
                for (int p = 0; p < rowlength; p++)
                {
                    int paeth = actline[p] & 0xff;
                    int a = p - bytesPerPixel >= 0 ? actline[p - bytesPerPixel] & 0xff : 0;// left
                    int b = lastline[p] & 0xff;// upper
                    int c = p - bytesPerPixel >= 0 ? lastline[p - bytesPerPixel] & 0xff : 0;// upperleft
                    int value = a + b - c;
                    int absa = Math.abs(value - a);
                    int absb = Math.abs(value - b);
                    int absc = Math.abs(value - c);

                    if (absa <= absb && absa <= absc)
                    {
                        actline[p] = (byte) ((paeth + a) & 0xff);
                    }
                    else if (absb <= absc)
                    {
                        actline[p] = (byte) ((paeth + b) & 0xff);
                    }
                    else
                    {
                        actline[p] = (byte) ((paeth + c) & 0xff);
                    }
                }
                break;
            default:
                break;
        }
    }
    
    static void decodePredictor(int predictor, int colors, int bitsPerComponent, int columns, InputStream in, OutputStream out)
            throws IOException
    {
        if (predictor == 1)
        {
            // no prediction
            IOUtils.copy(in, out);
        }
        else
        {
            // calculate sizes
            final int rowlength = calculateRowLength(colors, bitsPerComponent, columns);
            byte[] actline = new byte[rowlength];
            byte[] lastline = new byte[rowlength];

            int linepredictor = predictor;

            while (in.available() > 0)
            {
                // test for PNG predictor; each value >= 10 (not only 15) indicates usage of PNG predictor
                if (predictor >= 10)
                {
                    // PNG predictor; each row starts with predictor type (0, 1, 2, 3, 4)
                    // read per line predictor
                    linepredictor = in.read();
                    if (linepredictor == -1)
                    {
                        return;
                    }
                    // add 10 to tread value 0 as 10, 1 as 11, ...
                    linepredictor += 10;
                }

                // read line
                int i, offset = 0;
                while (offset < rowlength && ((i = in.read(actline, offset, rowlength - offset)) != -1))
                {
                    offset += i;
                }

                decodePredictorRow(linepredictor, colors, bitsPerComponent, columns, actline, lastline);
                System.arraycopy(actline, 0, lastline, 0, rowlength);
                out.write(actline);
            }
        }
    }

    static int calculateRowLength(int colors, int bitsPerComponent, int columns)
    {
        final int bitsPerPixel = colors * bitsPerComponent;
        return  (columns * bitsPerPixel + 7) / 8;
    }
    
    // get value from bit interval from a byte
    static int getBitSeq(int by, int startBit, int bitSize)
    {
        int mask = ((1 << bitSize) - 1);
        return (by >>> startBit) & mask;
    }

    // set value in a bit interval and return that value
    static int calcSetBitSeq(int by, int startBit, int bitSize, int val)
    {
        int mask = ((1 << bitSize) - 1);
        int truncatedVal = val & mask;
        mask = ~(mask << startBit);
        return (by & mask) | (truncatedVal << startBit);
    }

    /**
     * Wraps and <code>OutputStream</code> in a predictor decoding stream as necessary.
     * If no predictor is specified by the parameters, the original stream is returned as is.
     *
     * @param out The stream to which decoded data should be written
     * @param decodeParams Decode parameters for the stream
     * @return An <code>OutputStream</code> is returned, which will write decoded data
     * into the given stream. If no predictor is specified, the original stream is returned.
     */
    static OutputStream wrapPredictor(OutputStream out, COSDictionary decodeParams)
    {
        int predictor = decodeParams.getInt(COSName.PREDICTOR);
        if (predictor > 1)
        {
            int colors = Math.min(decodeParams.getInt(COSName.COLORS, 1), 32);
            int bitsPerPixel = decodeParams.getInt(COSName.BITS_PER_COMPONENT, 8);
            int columns = decodeParams.getInt(COSName.COLUMNS, 1);

            return new PredictorOutputStream(out, predictor, colors, bitsPerPixel, columns);
        }
        else
        {
            return out;
        }
    }

    /**
     * Output stream that implements predictor decoding. Data is buffered until a complete
     * row is available, which is then decoded and written to the underlying stream.
     * The previous row is retained for decoding the next row.
     */
    private static final class PredictorOutputStream extends FilterOutputStream
    {
        // current predictor type
        private int predictor;
        // image decode parameters
        private final int colors;
        private final int bitsPerComponent;
        private final int columns;
        private final int rowLength;
        // PNG predictor (predictor>=10) means every row has a (potentially different)
        // predictor value
        private final boolean predictorPerRow;

        // data buffers
        private byte[] currentRow, lastRow;
        // amount of data in the current row
        private int currentRowData = 0;
        // was the per-row predictor value read for the current row being processed
        private boolean predictorRead = false;

        PredictorOutputStream(OutputStream out, int predictor, int colors, int bitsPerComponent, int columns)
        {
            super(out);
            this.predictor = predictor;
            this.colors = colors;
            this.bitsPerComponent = bitsPerComponent;
            this.columns = columns;
            this.rowLength = calculateRowLength(colors, bitsPerComponent, columns);
            this.predictorPerRow = predictor >= 10;
            currentRow = new byte[rowLength];
            lastRow = new byte[rowLength];
        }

        @Override
        public void write(byte[] bytes) throws IOException
        {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException
        {
            int currentOffset = off;
            int maxOffset = currentOffset + len;
            while (currentOffset < maxOffset)
            {
                if (predictorPerRow && currentRowData == 0 && !predictorRead)
                {
                    // PNG predictor; each row starts with predictor type (0, 1, 2, 3, 4)
                    // read per line predictor, add 10 to tread value 0 as 10, 1 as 11, ...
                    predictor = bytes[currentOffset] + 10;
                    currentOffset++;
                    predictorRead = true;
                }
                else
                {
                    int toRead = Math.min(rowLength - currentRowData, maxOffset - currentOffset);
                    System.arraycopy(bytes, currentOffset, currentRow, currentRowData, toRead);
                    currentRowData += toRead;
                    currentOffset += toRead;

                    // current row is filled, decode it, write it to underlying stream,
                    // and reset the state.
                    if (currentRowData == currentRow.length)
                    {
                        decodeAndWriteRow();
                    }
                }
            }
        }

        private void decodeAndWriteRow() throws IOException
        {
            decodePredictorRow(predictor, colors, bitsPerComponent, columns, currentRow, lastRow);
            out.write(currentRow);
            flipRows();
        }

        /**
         * Flips the row buffers (to avoid copying), and resets the current-row index
         * and predictorRead flag
         */
        private void flipRows()
        {
            byte[] temp = lastRow;
            lastRow = currentRow;
            currentRow = temp;
            currentRowData = 0;
            predictorRead = false;
        }

        @Override
        public void flush() throws IOException
        {
            // The last row is allowed to be incomplete, and should be completed with zeros.
            if (currentRowData > 0)
            {
                Arrays.fill(currentRow, currentRowData, rowLength, (byte)0);
                decodeAndWriteRow();
            }
            super.flush();
        }

        @Override
        public void write(int i) throws IOException
        {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
