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

/* $Id$ */

package org.apache.pdfbox.io.ccitt;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a CCITT Group 3 1D decoder (ITU T.4).
 * @version $Revision$
 */
public class CCITTFaxG31DDecodeInputStream extends InputStream implements CCITTFaxConstants
{

    private static final int CODE_WORD = 0;
    private static final int SIGNAL_EOD = -1;
    private static final int SIGNAL_EOL = -2;

    private InputStream source;
    private int columns;
    private int rows;

    //for reading compressed bits
    private int bits;
    private int bitPos = 8;

    //a single decoded line (one line decoded at a time, then read byte by byte)
    private PackedBitArray decodedLine;
    private int decodedWritePos; //write position in bits (used by the decoder algorithm)
    private int decodedReadPos; //read position in bytes (used by the actual InputStream reading)

    //state
    private int y = -1; //Current row/line
    private int accumulatedRunLength; //Used for make-up codes

    private static final NonLeafLookupTreeNode WHITE_LOOKUP_TREE_ROOT;
    private static final NonLeafLookupTreeNode BLACK_LOOKUP_TREE_ROOT;

    static {
        WHITE_LOOKUP_TREE_ROOT = new NonLeafLookupTreeNode();
        BLACK_LOOKUP_TREE_ROOT = new NonLeafLookupTreeNode();
        buildLookupTree();
    }

    /**
     * Creates a new decoder.
     * @param source the input stream containing the compressed data.
     * @param columns the number of columns
     * @param rows the number of rows (0 if undefined)
     */
    public CCITTFaxG31DDecodeInputStream(InputStream source, int columns, int rows)
    {
        this.source = source;
        this.columns = columns;
        this.rows = rows;
        this.decodedLine = new PackedBitArray(columns);
        this.decodedReadPos = this.decodedLine.getByteCount();
    }

    /**
     * Creates a new decoder.
     * @param source the input stream containing the compressed data.
     * @param columns the number of columns
     */
    public CCITTFaxG31DDecodeInputStream(InputStream source, int columns)
    {
        this(source, columns, 0);
    }

    /** {@inheritDoc} */
    public boolean markSupported()
    {
        return false;
    }

    /** {@inheritDoc} */
    public int read() throws IOException
    {
        if (this.decodedReadPos >= this.decodedLine.getByteCount())
        {
            boolean hasLine = decodeLine();
            if (!hasLine)
            {
                return -1;
            }
        }
        byte data = this.decodedLine.getData()[this.decodedReadPos++];

        //System.out.println("Returning " + PackedBitArray.visualizeByte(data));
        return data & 0xFF;
    }

    //TODO Implement the other two read methods

    private boolean decodeLine() throws IOException
    {
        if (this.bits < 0)
        {
            return false; //Shortcut after EOD
        }
        this.y++;
        //System.out.println("decodeLine " + this.y);
        int x = 0;
        if (this.rows > 0 && this.y >= this.rows)
        {
            return false; //All rows decoded, ignore further bits
        }
        this.decodedLine.clear();
        this.decodedWritePos = 0;
        int expectRTC = 6;
        boolean white = true;
        while (x < this.columns || this.accumulatedRunLength > 0)
        {
            CodeWord code;
            LookupTreeNode root = white ? WHITE_LOOKUP_TREE_ROOT : BLACK_LOOKUP_TREE_ROOT;
            code = root.getNextCodeWord(this);
            if (code == null)
            {
                //no more code words (EOD)
                if (x > 0)
                {
                    //Have last line
                    this.decodedReadPos = 0;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (code.getType() == SIGNAL_EOL)
            {
                expectRTC--;
                if (expectRTC == 0)
                {
                    //System.out.println("Return to Control");
                    return false; //Return to Control = End Of Data
                }
                if (x == 0)
                {
                    //System.out.println("Ignoring leading EOL");
                    continue; //Ignore leading EOL
                }
            }
            else
            {
                expectRTC = -1;
                x += code.execute(this);
                if (this.accumulatedRunLength == 0)
                {
                    //Only switch if not using make-up codes
                    white = !white;
                }
            }
        }
        this.decodedReadPos = 0;
        return true;
    }

    private void writeRun(int bit, int length)
    {
        this.accumulatedRunLength += length;

        //System.out.println(" Run " + bit + " for " + this.accumulatedRunLength + " at " + decodedWritePos);
        if (bit != 0)
        {
            this.decodedLine.setBits(this.decodedWritePos, this.accumulatedRunLength);
        }
        this.decodedWritePos += this.accumulatedRunLength;
        this.accumulatedRunLength = 0;
    }

    private void writeNonTerminating(int length)
    {
        //System.out.println(" Make up code for " + length + " bits");
        this.accumulatedRunLength += length;
    }

    private static final int[] BIT_POS_MASKS
        = new int[] {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};

    private int readBit() throws IOException
    {
        if (this.bitPos >= 8)
        {
            readByte();
            if (this.bits < 0)
            {
                return SIGNAL_EOD;
            }
        }
        final int bit = (this.bits & BIT_POS_MASKS[this.bitPos++]) == 0 ? 0 : 1;
        //System.out.print(bit);
        return bit;
    }

    private void readByte() throws IOException
    {
        this.bits = this.source.read();
        this.bitPos = 0;
    }

    private static final short EOL_STARTER = 0x0B00;

    private static void buildLookupTree()
    {
        buildUpTerminating(WHITE_TERMINATING, WHITE_LOOKUP_TREE_ROOT, true);
        buildUpTerminating(BLACK_TERMINATING, BLACK_LOOKUP_TREE_ROOT, false);
        buildUpMakeUp(WHITE_MAKE_UP, WHITE_LOOKUP_TREE_ROOT);
        buildUpMakeUp(BLACK_MAKE_UP, BLACK_LOOKUP_TREE_ROOT);
        buildUpMakeUpLong(LONG_MAKE_UP, WHITE_LOOKUP_TREE_ROOT);
        buildUpMakeUpLong(LONG_MAKE_UP, BLACK_LOOKUP_TREE_ROOT);
        LookupTreeNode eolNode = new EndOfLineTreeNode();
        addLookupTreeNode(EOL_STARTER, WHITE_LOOKUP_TREE_ROOT, eolNode);
        addLookupTreeNode(EOL_STARTER, BLACK_LOOKUP_TREE_ROOT, eolNode);
    }

    private static void buildUpTerminating(short[] codes, NonLeafLookupTreeNode root, boolean white)
    {
        for (int len = 0, c = codes.length; len < c; len++)
        {
            LookupTreeNode leaf = new RunLengthTreeNode(white ? 0 : 1, len);
            addLookupTreeNode(codes[len], root, leaf);
        }
    }

    private static void buildUpMakeUp(short[] codes, NonLeafLookupTreeNode root)
    {
        for (int len = 0, c = codes.length; len < c; len++)
        {
            LookupTreeNode leaf = new MakeUpTreeNode((len + 1) * 64);
            addLookupTreeNode(codes[len], root, leaf);
        }
    }

    private static void buildUpMakeUpLong(short[] codes, NonLeafLookupTreeNode root)
    {
        for (int len = 0, c = codes.length; len < c; len++)
        {
            LookupTreeNode leaf = new MakeUpTreeNode((len + 28) * 64);
            addLookupTreeNode(codes[len], root, leaf);
        }
    }

    private static void addLookupTreeNode(short code, NonLeafLookupTreeNode root,
            LookupTreeNode leaf)
    {
        int codeLength = code >> 8;
        int pattern = code & 0xFF;
        NonLeafLookupTreeNode node = root;
        for (int p = codeLength - 1; p > 0; p--)
        {
            int bit = (pattern >> p) & 0x01;
            LookupTreeNode child = node.get(bit);
            if (child == null)
            {
                child = new NonLeafLookupTreeNode();
                node.set(bit, child);
            }
            if (child instanceof NonLeafLookupTreeNode)
            {
                node = (NonLeafLookupTreeNode)child;
            }
            else
            {
                throw new IllegalStateException("NonLeafLookupTreeNode expected, was "
                        + child.getClass().getName());
            }
        }
        int bit = pattern & 0x01;
        if (node.get(bit) != null)
        {
            throw new IllegalStateException("Two codes conflicting in lookup tree");
        }
        node.set(bit, leaf);
    }

    /** Base class for all nodes in the lookup tree for code words. */
    private abstract static class LookupTreeNode
    {

        public abstract CodeWord getNextCodeWord(CCITTFaxG31DDecodeInputStream decoder)
                throws IOException;

    }

    /** Interface for code words. */
    private interface CodeWord
    {

        public abstract int getType();
        public abstract int execute(CCITTFaxG31DDecodeInputStream decoder) throws IOException;

    }

    /** Non-leaf nodes that hold a child node for both the 0 and 1 cases for the lookup tree. */
    private static class NonLeafLookupTreeNode extends LookupTreeNode
    {

        private LookupTreeNode zero;
        private LookupTreeNode one;

        public void set(int bit, LookupTreeNode node)
        {
            if (bit == 0)
            {
                this.zero = node;
            }
            else
            {
                this.one = node;
            }
        }

        public LookupTreeNode get(int bit)
        {
            return (bit == 0) ? this.zero : this.one;
        }

        public CodeWord getNextCodeWord(CCITTFaxG31DDecodeInputStream decoder)
                throws IOException
                {
            int bit = decoder.readBit();
            if (bit < 0)
            {
                return null;
            }
            LookupTreeNode node = get(bit);
            if (node != null)
            {
                return node.getNextCodeWord(decoder);
            }
            throw new IOException("Invalid code word encountered");
        }

    }

    /** This node represents a run length of either 0 or 1. */
    private static class RunLengthTreeNode extends LookupTreeNode implements CodeWord
    {

        private final int bit;
        private final int length;

        public RunLengthTreeNode(int bit, int length)
        {
            this.bit = bit;
            this.length = length;
        }

        public CodeWord getNextCodeWord(CCITTFaxG31DDecodeInputStream decoder) throws IOException
        {
            return this;
        }

        public int execute(CCITTFaxG31DDecodeInputStream decoder)
        {
            decoder.writeRun(this.bit, this.length);
            return length;
        }

        public int getType()
        {
            return CODE_WORD;
        }

        public String toString()
        {
            return "Run Length for " + length + " bits of " + (bit == 0 ? "white" : "black");
        }

    }

    /** Represents a make-up code word. */
    private static class MakeUpTreeNode extends LookupTreeNode implements CodeWord
    {

        private final int length;

        public MakeUpTreeNode(int length)
        {
            this.length = length;
        }

        public CodeWord getNextCodeWord(CCITTFaxG31DDecodeInputStream decoder) throws IOException
        {
            return this;
        }

        public int execute(CCITTFaxG31DDecodeInputStream decoder) throws IOException
        {
            decoder.writeNonTerminating(length);
            return length;
        }

        public int getType()
        {
            return CODE_WORD;
        }

        public String toString()
        {
            return "Make up code for length " + length;
        }

    }

    /** Represents an EOL code word. */
    private static class EndOfLineTreeNode extends LookupTreeNode implements CodeWord
    {

        public CodeWord getNextCodeWord(CCITTFaxG31DDecodeInputStream decoder) throws IOException
        {
            int bit;
            do
            {
                bit = decoder.readBit();
            } while (bit == 0); //bit 1 finishes the EOL, any number of bit 0 allowed as fillers
            if (bit < 0)
            {
                return null;
            }
            return this;
        }

        public int execute(CCITTFaxG31DDecodeInputStream decoder) throws IOException
        {
            //nop
            return 0;
        }

        public int getType()
        {
            return SIGNAL_EOL;
        }

        public String toString()
        {
            return "EOL";
        }

    }

}