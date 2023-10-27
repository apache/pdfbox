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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

/**
 * This factory tries to encode a PNG given as byte array into a PDImageXObject
 * by directly coping the image data into the PDF streams without
 * decoding/encoding and re-compressing the PNG data.
 * <p>
 * If this is for any reason not possible, the factory will return null. You
 * must then encode the image by loading it and using the LosslessFactory.
 * <p>
 * The W3C PNG spec was used to implement this class:
 * https://www.w3.org/TR/2003/REC-PNG-20031110
 *
 * @author Emmeran Seehuber
 */
final class PNGConverter
{
    private static final Logger LOG = LogManager.getLogger(PNGConverter.class);

    // Chunk Type definitions. The bytes in the comments are the bytes in the spec.
    private static final int CHUNK_IHDR = 0x49484452; // IHDR: 73 72 68 82
    private static final int CHUNK_IDAT = 0x49444154; // IDAT: 73 68 65 84
    private static final int CHUNK_PLTE = 0x504C5445; // PLTE: 80 76 84 69
    private static final int CHUNK_IEND = 0x49454E44; // IEND: 73 69 78 68
    private static final int CHUNK_TRNS = 0x74524E53; // tRNS: 116 82 78 83
    private static final int CHUNK_CHRM = 0x6348524D; // cHRM: 99 72 82 77
    private static final int CHUNK_GAMA = 0x67414D41; // gAMA: 103 65 77 65
    private static final int CHUNK_ICCP = 0x69434350; // iCCP: 105 67 67 80
    private static final int CHUNK_SBIT = 0x73424954; // sBIT: 115 66 73 84
    private static final int CHUNK_SRGB = 0x73524742; // sRGB: 115 82 71 66
    private static final int CHUNK_TEXT = 0x74455874; // tEXt: 116 69 88 116
    private static final int CHUNK_ZTXT = 0x7A545874; // zTXt: 122 84 88 116
    private static final int CHUNK_ITXT = 0x69545874; // iTXt: 105 84 88 116
    private static final int CHUNK_KBKG = 0x6B424B47; // kBKG: 107 66 75 71
    private static final int CHUNK_HIST = 0x68495354; // hIST: 104 73 83 84
    private static final int CHUNK_PHYS = 0x70485973; // pHYs: 112 72 89 115
    private static final int CHUNK_SPLT = 0x73504C54; // sPLT: 115 80 76 84
    private static final int CHUNK_TIME = 0x74494D45; // tIME: 116 73 77 69

    // CRC Reference Implementation, see
    // https://www.w3.org/TR/2003/REC-PNG-20031110/#D-CRCAppendix
    // for details

    /* Table of CRCs of all 8-bit messages. */
    private static final int[] CRC_TABLE = new int[256];

    static
    {
        makeCrcTable();
    }

    private PNGConverter()
    {
    }

    /**
     * Try to convert a PNG into a PDImageXObject. If for any reason the PNG can not
     * be converted, null is returned.
     * <p>
     * This usually means the PNG structure is damaged (CRC error, etc.) or it uses
     * some features which can not be mapped to PDF.
     *
     * @param doc       the document to put the image in
     * @param imageData the byte data of the PNG
     * @return null or the PDImageXObject built from the png
     */
    static PDImageXObject convertPNGImage(PDDocument doc, byte[] imageData) throws IOException
    {
        PNGConverterState state = parsePNGChunks(imageData);
        if (!checkConverterState(state))
        {
            // There is something wrong, we can't convert this PNG
            return null;
        }

        return convertPng(doc, state);
    }

    /**
     * Convert the image using the state.
     *
     * @param doc   the document to put the image in
     * @param state the parser state containing the PNG chunks.
     * @return null or the converted image
     */
    private static PDImageXObject convertPng(PDDocument doc, PNGConverterState state)
            throws IOException
    {
        Chunk ihdr = state.IHDR;
        int ihdrStart = ihdr.start;
        int width = readInt(ihdr.bytes, ihdrStart);
        int height = readInt(ihdr.bytes, ihdrStart + 4);
        int bitDepth = ihdr.bytes[ihdrStart + 8] & 0xFF;
        int colorType = ihdr.bytes[ihdrStart + 9] & 0xFF;
        int compressionMethod = ihdr.bytes[ihdrStart + 10] & 0xFF;
        int filterMethod = ihdr.bytes[ihdrStart + 11] & 0xFF;
        int interlaceMethod = ihdr.bytes[ihdrStart + 12] & 0xFF;

        if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16)
        {
            LOG.error(String.format("Invalid bit depth %d.", bitDepth));
            return null;
        }
        if (width <= 0 || height <= 0)
        {
            LOG.error(String.format("Invalid image size %d x %d", width, height));
            return null;
        }
        if (compressionMethod != 0)
        {
            LOG.error(String.format("Unknown PNG compression method %d.", compressionMethod));
            return null;
        }
        if (filterMethod != 0)
        {
            LOG.error(String.format("Unknown PNG filtering method %d.", compressionMethod));
            return null;
        }
        if (interlaceMethod != 0)
        {
            LOG.debug(String.format("Can't handle interlace method %d.", interlaceMethod));
            return null;
        }

        state.width = width;
        state.height = height;
        state.bitsPerComponent = bitDepth;

        switch (colorType)
        {
        case 0:
            // Grayscale
            LOG.debug("Can't handle grayscale yet.");
            return null;
        case 2:
            // Truecolor
            if (state.tRNS != null)
            {
                LOG.debug("Can't handle images with transparent colors.");
                return null;
            }
            return buildImageObject(doc, state);
        case 3:
            // Indexed image
            return buildIndexImage(doc, state);
        case 4:
            // Grayscale with alpha.
            LOG.debug(
                    "Can't handle grayscale with alpha, would need to separate alpha from image data");
            return null;
        case 6:
            // Truecolor with alpha.
            LOG.debug(
                    "Can't handle truecolor with alpha, would need to separate alpha from image data");
            return null;
        default:
            LOG.error("Unknown PNG color type {}", colorType);
            return null;
        }
    }

    /**
     * Build a indexed image
     */
    private static PDImageXObject buildIndexImage(PDDocument doc, PNGConverterState state)
            throws IOException
    {
        Chunk plte = state.PLTE;
        if (plte == null)
        {
            LOG.error("Indexed image without PLTE chunk.");
            return null;
        }
        if (plte.length % 3 != 0)
        {
            LOG.error("PLTE table corrupted, last (r,g,b) tuple is not complete.");
            return null;
        }
        if (state.bitsPerComponent > 8)
        {
            LOG.debug(String.format("Can only convert indexed images with bit depth <= 8, not %d.",
                    state.bitsPerComponent));
            return null;
        }

        PDImageXObject image = buildImageObject(doc, state);
        if (image == null)
        {
            return null;
        }

        int highVal = (plte.length / 3) - 1;
        if (highVal > 255)
        {
            LOG.error(String.format("Too much colors in PLTE, only 256 allowed, found %d colors.",
                    highVal + 1));
            return null;
        }

        setupIndexedColorSpace(doc, plte, image, highVal);

        if (state.tRNS != null)
        {
            image.getCOSObject().setItem(COSName.SMASK,
                    buildTransparencyMaskFromIndexedData(doc, image, state));
        }

        return image;
    }

    private static PDImageXObject buildTransparencyMaskFromIndexedData(PDDocument doc,
            PDImageXObject image, PNGConverterState state) throws IOException
    {
        Filter flateDecode = FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        COSDictionary decodeParams = buildDecodeParams(state, PDDeviceGray.INSTANCE);
        COSDictionary imageDict = new COSDictionary();
        imageDict.setItem(COSName.FILTER, COSName.FLATE_DECODE);
        imageDict.setItem(COSName.DECODE_PARMS, decodeParams);
        flateDecode.decode(getIDATInputStream(state), outputStream, imageDict, 0);
        int length = image.getWidth() * image.getHeight();
        byte[] bytes = new byte[length];
        byte[] transparencyTable = state.tRNS.getData();
        byte[] decodedIDAT = outputStream.toByteArray();
        try (ImageInputStream iis = new MemoryCacheImageInputStream(
                new ByteArrayInputStream(decodedIDAT)))
        {
            int bitsPerComponent = state.bitsPerComponent;
            int w = 0;
            int neededBits = bitsPerComponent * state.width;
            int bitPadding = neededBits % 8;
            for (int i = 0; i < bytes.length; i++)
            {
                int idx = (int) iis.readBits(bitsPerComponent);
                if (idx < transparencyTable.length)
                {
                    // Inside the table, use the transparency value
                    bytes[i] = transparencyTable[idx];
                }
                else
                {
                    // Outside the table -> transparent value is 0xFF here.
                    bytes[i] = (byte) 0xFF;
                }
                w++;
                if (w == state.width)
                {
                    w = 0;
                    iis.readBits(bitPadding);
                }
            }
        }
        return LosslessFactory
                .prepareImageXObject(doc, bytes, image.getWidth(), image.getHeight(), 8,
                        PDDeviceGray.INSTANCE);
    }

    private static void setupIndexedColorSpace(PDDocument doc, Chunk lookupTable,
            PDImageXObject image, int highVal) throws IOException
    {
        COSArray indexedArray = new COSArray();
        indexedArray.add(COSName.INDEXED);
        indexedArray.add(image.getColorSpace());
        ((COSDictionary) image.getCOSObject().getItem(COSName.DECODE_PARMS))
                .setItem(COSName.COLORS, COSInteger.ONE);

        indexedArray.add(COSInteger.get(highVal));

        PDStream colorTable = new PDStream(doc);
        try (OutputStream colorTableStream = colorTable.createOutputStream(COSName.FLATE_DECODE))
        {
            colorTableStream.write(lookupTable.bytes, lookupTable.start, lookupTable.length);
        }
        indexedArray.add(colorTable);

        PDIndexed indexed = new PDIndexed(indexedArray);
        image.setColorSpace(indexed);
    }

    /**
     * Build the base image object from the IDATs and profile information
     */
    private static PDImageXObject buildImageObject(PDDocument document, PNGConverterState state)
            throws IOException
    {
        InputStream encodedByteStream = getIDATInputStream(state);

        PDColorSpace colorSpace = PDDeviceRGB.INSTANCE;

        PDImageXObject imageXObject = new PDImageXObject(document, encodedByteStream,
                COSName.FLATE_DECODE, state.width, state.height, state.bitsPerComponent,
                colorSpace);

        COSDictionary decodeParams = buildDecodeParams(state, colorSpace);
        imageXObject.getCOSObject().setItem(COSName.DECODE_PARMS, decodeParams);

        // We ignore gAMA and cHRM chunks if we have a ICC profile, as the ICC profile
        // takes preference
        boolean hasICCColorProfile = state.sRGB != null || state.iCCP != null;

        if (state.gAMA != null && !hasICCColorProfile)
        {
            if (state.gAMA.length != 4)
            {
                LOG.error("Invalid gAMA chunk length {}", state.gAMA.length);
                return null;
            }
            float gamma = readPNGFloat(state.gAMA.bytes, state.gAMA.start);
            // If the gamma is 2.2 for sRGB everything is fine. Otherwise bail out.
            // The gamma is stored as 1 / gamma.
            if (Math.abs(gamma - (1 / 2.2f)) > 0.00001)
            {
                LOG.debug(String.format("We can't handle gamma of %f yet.", gamma));
                return null;
            }
        }

        if (state.sRGB != null)
        {
            if (state.sRGB.length != 1)
            {
                LOG.error(
                        String.format("sRGB chunk has an invalid length of %d", state.sRGB.length));
                return null;
            }

            // Store the specified rendering intent
            int renderIntent = state.sRGB.bytes[state.sRGB.start];
            COSName value = mapPNGRenderIntent(renderIntent);
            imageXObject.getCOSObject().setItem(COSName.INTENT, value);
        }

        if (state.cHRM != null && !hasICCColorProfile)
        {
            if (state.cHRM.length != 32)
            {
                LOG.error("Invalid cHRM chunk length {}", state.cHRM.length);
                return null;
            }
            LOG.debug("We can not handle cHRM chunks yet.");
            return null;
        }

        // If possible we prefer a ICCBased color profile, just because its way faster
        // to decode ...
        if (state.iCCP != null || state.sRGB != null)
        {
            // We have got a color profile, which we must attach
            COSStream cosStream = createCOSStreamwithIccProfile(document, colorSpace, state);
            if (cosStream == null)
            {
                return null;
            }
            COSArray array = new COSArray();
            array.add(COSName.ICCBASED);
            array.add(cosStream);
            PDICCBased profile = PDICCBased.create(array, null);
            imageXObject.setColorSpace(profile);
        }
        return imageXObject;
    }

    private static COSStream createCOSStreamwithIccProfile
        (PDDocument document, PDColorSpace colorSpace, PNGConverterState state) throws IOException
    {
        int numberOfComponents = colorSpace.getNumberOfComponents();
        COSStream cosStream = document.getDocument().createCOSStream();
        cosStream.setInt(COSName.N, numberOfComponents);
        cosStream.setItem(COSName.ALTERNATE, 
                numberOfComponents == 1 ? COSName.DEVICEGRAY : COSName.DEVICERGB);
        cosStream.setItem(COSName.FILTER, COSName.FLATE_DECODE);
        if (state.iCCP != null)
        {
            // We need to skip over the name
            int iccProfileDataStart = 0;
            while (iccProfileDataStart < 80 && iccProfileDataStart < state.iCCP.length)
            {
                if (state.iCCP.bytes[state.iCCP.start + iccProfileDataStart] == 0)
                {
                    break;
                }
                iccProfileDataStart++;
            }
            iccProfileDataStart++;
            if (iccProfileDataStart >= state.iCCP.length)
            {
                LOG.error("Invalid iCCP chunk, to few bytes");
                return null;
            }
            byte compressionMethod = state.iCCP.bytes[state.iCCP.start + iccProfileDataStart];
            if (compressionMethod != 0)
            {
                LOG.error(String.format("iCCP chunk: invalid compression method %d",
                        compressionMethod));
                return null;
            }
            // Skip over the compression method
            iccProfileDataStart++;
            try (OutputStream rawOutputStream = cosStream.createRawOutputStream())
            {
                rawOutputStream.write(state.iCCP.bytes, state.iCCP.start + iccProfileDataStart,
                        state.iCCP.length - iccProfileDataStart);
            }
        }
        else
        {
            // We tag the image with the sRGB profile
            ICC_Profile rgbProfile = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
            try (OutputStream outputStream = cosStream.createOutputStream())
            {
                outputStream.write(rgbProfile.getData());
            }
        }
        return cosStream;
    }

    private static COSDictionary buildDecodeParams(PNGConverterState state, PDColorSpace colorSpace)
    {
        COSDictionary decodeParms = new COSDictionary();
        decodeParms.setItem(COSName.BITS_PER_COMPONENT, COSInteger.get(state.bitsPerComponent));
        decodeParms.setItem(COSName.PREDICTOR, COSInteger.get(15));
        decodeParms.setItem(COSName.COLUMNS, COSInteger.get(state.width));
        decodeParms.setItem(COSName.COLORS, COSInteger.get(colorSpace.getNumberOfComponents()));
        return decodeParms;
    }

    /**
     * Build an input stream for the IDAT data. May need to concat multiple IDAT
     * chunks.
     *
     * @param state the converter state.
     * @return a input stream with the IDAT data.
     */
    private static InputStream getIDATInputStream(PNGConverterState state)
    {
        MultipleInputStream inputStream = new MultipleInputStream();
        for (Chunk idat : state.IDATs)
        {
            inputStream.inputStreams
                    .add(new ByteArrayInputStream(idat.bytes, idat.start, idat.length));
        }
        return inputStream;
    }

    private static class MultipleInputStream extends InputStream
    {

        final List<InputStream> inputStreams = new ArrayList<>();
        int currentStreamIdx;
        InputStream currentStream;

        private boolean ensureStream()
        {
            if (currentStream == null)
            {
                if (currentStreamIdx >= inputStreams.size())
                {
                    return false;
                }
                currentStream = inputStreams.get(currentStreamIdx++);
            }
            return true;
        }

        @Override
        public int read() throws IOException
        {
            if (!ensureStream())
            {
                return -1;
            }
            int ret = currentStream.read();
            if (ret == -1)
            {
                currentStream = null;
                return read();
            }
            return ret;
        }

        @Override
        public int available() throws IOException
        {
            if (!ensureStream())
            {
                return 0;
            }
            return 1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (!ensureStream())
            {
                return -1;
            }
            int ret = currentStream.read(b, off, len);
            if (ret == -1)
            {
                currentStream = null;
                return read(b, off, len);
            }
            return ret;
        }
    }

    /**
     * Map the renderIntent int to a PDF render intent. See also
     * https://www.w3.org/TR/2003/REC-PNG-20031110/#11sRGB
     *
     * @param renderIntent the PNG render intent
     * @return the matching PDF Render Intent or null
     */
    static COSName mapPNGRenderIntent(int renderIntent)
    {
        COSName value;
        switch (renderIntent)
        {
        case 0:
            value = COSName.PERCEPTUAL;
            break;
        case 1:
            value = COSName.RELATIVE_COLORIMETRIC;
            break;
        case 2:
            value = COSName.SATURATION;
            break;
        case 3:
            value = COSName.ABSOLUTE_COLORIMETRIC;
            break;
        default:
            value = null;
            break;
        }
        return value;
    }

    /**
     * Check if the converter state is sane.
     *
     * @param state the parsed converter state
     * @return true if the state seems plausible
     */
    static boolean checkConverterState(PNGConverterState state)
    {
        if (state == null)
        {
            return false;
        }
        if (state.IHDR == null || !checkChunkSane(state.IHDR))
        {
            LOG.error("Invalid IHDR chunk.");
            return false;
        }
        if (!checkChunkSane(state.PLTE))
        {
            LOG.error("Invalid PLTE chunk.");
            return false;
        }
        if (!checkChunkSane(state.iCCP))
        {
            LOG.error("Invalid iCCP chunk.");
            return false;
        }
        if (!checkChunkSane(state.tRNS))
        {
            LOG.error("Invalid tRNS chunk.");
            return false;
        }
        if (!checkChunkSane(state.sRGB))
        {
            LOG.error("Invalid sRGB chunk.");
            return false;
        }
        if (!checkChunkSane(state.cHRM))
        {
            LOG.error("Invalid cHRM chunk.");
            return false;
        }
        if (!checkChunkSane(state.gAMA))
        {
            LOG.error("Invalid gAMA chunk.");
            return false;
        }

        // Check the IDATs
        if (state.IDATs.isEmpty())
        {
            LOG.error("No IDAT chunks.");
            return false;
        }
        for (Chunk idat : state.IDATs)
        {
            if (!checkChunkSane(idat))
            {
                LOG.error("Invalid IDAT chunk.");
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the chunk is sane, i.e. CRC matches and offsets and lengths in the
     * byte array
     */
    static boolean checkChunkSane(Chunk chunk)
    {
        if (chunk == null)
        {
            // If the chunk does not exist, it can not be wrong...
            return true;
        }

        if (chunk.start + chunk.length > chunk.bytes.length)
        {
            return false;
        }

        if (chunk.start < 4)
        {
            return false;
        }

        // We must include the chunk type in the CRC calculation
        int ourCRC = crc(chunk.bytes, chunk.start - 4, chunk.length + 4);
        if (ourCRC != chunk.crc)
        {
            LOG.error(String.format("Invalid CRC %08X on chunk %08X, expected %08X.", ourCRC,
                    chunk.chunkType, chunk.crc));
            return false;
        }
        return true;
    }

    /**
     * Holds the information about a chunks
     */
    static final class Chunk
    {
        /**
         * This field holds the whole byte array; In that it's redundant, as all chunks
         * will have the same byte array. But have this byte array per chunk makes it
         * easier to validate and pass around. And we won't have that many chunks, so
         * those 8 bytes for the pointer (on 64-bit systems) don't matter.
         */
        byte[] bytes;
        /**
         * The chunk type, see the CHUNK_??? constants.
         */
        int chunkType;
        /**
         * The crc of the chunk data, as stored in the PNG stream.
         */
        int crc;
        /**
         * The start index of the chunk data within bytes.
         */
        int start;
        /**
         * The length of the data within the byte array.
         */
        int length;

        /**
         * Get the data of this chunk as a byte array
         *
         * @return a byte-array with only the data of the chunk
         */
        byte[] getData()
        {
            return Arrays.copyOfRange(bytes, start, start + length);
        }
    }

    /**
     * Holds all relevant chunks of the PNG
     */
    static final class PNGConverterState
    {
        List<Chunk> IDATs = new ArrayList<>();
        @SuppressWarnings("SpellCheckingInspection") Chunk IHDR;
        @SuppressWarnings("SpellCheckingInspection") Chunk PLTE;
        Chunk iCCP;
        Chunk tRNS;
        Chunk sRGB;
        Chunk gAMA;
        Chunk cHRM;

        // Parsed header fields
        int width;
        int height;
        int bitsPerComponent;
    }

    private static int readInt(byte[] data, int offset)
    {
        int b1 = (data[offset] & 0xFF) << 24;
        int b2 = (data[offset + 1] & 0xFF) << 16;
        int b3 = (data[offset + 2] & 0xFF) << 8;
        int b4 = (data[offset + 3] & 0xFF);
        return b1 | b2 | b3 | b4;
    }

    private static float readPNGFloat(byte[] bytes, int offset)
    {
        int v = readInt(bytes, offset);
        return v / 100000f;
    }

    /**
     * Parse the PNG structure into the PNGConverterState. If we can't handle
     * something, this method will return null.
     *
     * @param imageData the byte array with the PNG data
     * @return null or the converter state with all relevant chunks
     */
    private static PNGConverterState parsePNGChunks(byte[] imageData)
    {
        if (imageData.length < 20)
        {
            LOG.error("ByteArray way to small: {}", imageData.length);
            return null;
        }

        PNGConverterState state = new PNGConverterState();
        int ptr = 8;
        int firstChunkType = readInt(imageData, ptr + 4);

        if (firstChunkType != CHUNK_IHDR)
        {
            LOG.error(String.format("First Chunktype was %08X, not IHDR", firstChunkType));
            return null;
        }

        while (ptr + 12 <= imageData.length)
        {
            int chunkLength = readInt(imageData, ptr);
            int chunkType = readInt(imageData, ptr + 4);
            ptr += 8;

            if (ptr + chunkLength + 4 > imageData.length)
            {
                LOG.error(
                        "Not enough bytes. At offset {} are {} bytes expected. Overall length is {}",
                        ptr, chunkLength, imageData.length);
                return null;
            }

            Chunk chunk = new Chunk();
            chunk.chunkType = chunkType;
            chunk.bytes = imageData;
            chunk.start = ptr;
            chunk.length = chunkLength;

            switch (chunkType)
            {
            case CHUNK_IHDR:
                if (state.IHDR != null)
                {
                    LOG.error("Two IHDR chunks? There is something wrong.");
                    return null;
                }
                state.IHDR = chunk;
                break;
            case CHUNK_IDAT:
                // The image data itself
                state.IDATs.add(chunk);
                break;
            case CHUNK_PLTE:
                // For indexed images the palette table
                if (state.PLTE != null)
                {
                    LOG.error("Two PLTE chunks? There is something wrong.");
                    return null;
                }
                state.PLTE = chunk;
                break;
            case CHUNK_IEND:
                // We are done, return the state
                return state;
            case CHUNK_TRNS:
                // For indexed images the alpha transparency table
                if (state.tRNS != null)
                {
                    LOG.error("Two tRNS chunks? There is something wrong.");
                    return null;
                }
                state.tRNS = chunk;
                break;
            case CHUNK_GAMA:
                // Gama
                state.gAMA = chunk;
                break;
            case CHUNK_CHRM:
                // Chroma
                state.cHRM = chunk;
                break;
            case CHUNK_ICCP:
                // ICC Profile
                state.iCCP = chunk;
                break;
            case CHUNK_SBIT:
                LOG.debug("Can't convert PNGs with sBIT chunk.");
                break;
            case CHUNK_SRGB:
                // We use the rendering intent from the chunk
                state.sRGB = chunk;
                break;
            case CHUNK_TEXT:
            case CHUNK_ZTXT:
            case CHUNK_ITXT:
                // We don't care about this text infos / metadata
                break;
            case CHUNK_KBKG:
                // As we can handle transparency we don't need the background color information.
                break;
            case CHUNK_HIST:
                // We don't need the color histogram
                break;
            case CHUNK_PHYS:
                // The PDImageXObject will be placed by the user however he wants,
                // so we can not enforce the physical dpi information stored here.
                // We just ignore it.
                break;
            case CHUNK_SPLT:
                // This palette stuff seems editor related, we don't need it.
                break;
            case CHUNK_TIME:
                // We don't need the last image change time either
                break;
            default:
                LOG.debug(String.format("Unknown chunk type %08X, skipping.", chunkType));
                break;
            }
            ptr += chunkLength;

            // Read the CRC
            chunk.crc = readInt(imageData, ptr);
            ptr += 4;
        }
        LOG.error("No IEND chunk found.");
        return null;
    }

    /* Make the table for a fast CRC. */
    private static void makeCrcTable()
    {
        int c;

        for (int n = 0; n < 256; n++)
        {
            c = n;
            for (int k = 0; k < 8; k++)
            {
                if ((c & 1) != 0)
                {
                    c = 0xEDB88320 ^ (c >>> 1);
                }
                else
                {
                    c = c >>> 1;
                }
            }
            CRC_TABLE[n] = c;
        }
    }

    /*
     * Update a running CRC with the bytes buf[0..len-1]--the CRC should be
     * initialized to all 1's, and the transmitted value is the 1's complement of
     * the final running CRC (see the crc() routine below).
     */
    private static int updateCrc(byte[] buf, int offset, int len)
    {
        int c = -1;
        int end = offset + len;
        for (int n = offset; n < end; n++)
        {
            c = CRC_TABLE[(c ^ buf[n]) & 0xff] ^ (c >>> 8);
        }
        return c;
    }

    /* Return the CRC of the bytes buf[offset..(offset+len-1)]. */
    static int crc(byte[] buf, int offset, int len)
    {
        return ~updateCrc(buf, offset, len);
    }
}
