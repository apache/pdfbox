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
package org.apache.fontbox.ttf;

/**
 * A bounds-checked cursor over a TrueType bytecode program (fpgm, prep, a glyph instruction stream, or
 * a function body). It owns the program counter so individual opcode handlers never index the array by
 * hand - this removes a whole class of off-by-one and overrun bugs and is trivially testable on its
 * own. Reads past the end throw {@link HintingException} so the per-glyph fallback can catch cleanly.
 *
 * @author Apache PDFBox
 */
class BytecodeStream
{
    private final byte[] code;
    private int ip;
    private int instructionStart;

    /**
     * @param code the bytecode program; not copied
     */
    public BytecodeStream(byte[] code)
    {
        this.code = code != null ? code : new byte[0];
    }

    /**
     * @return the underlying bytecode array (not copied); used to record function entry points
     */
    public byte[] getCode()
    {
        return code;
    }

    /**
     * @return true if there is at least one more byte to read
     */
    public boolean hasNext()
    {
        return ip < code.length;
    }

    /**
     * @return the current program-counter position
     */
    public int position()
    {
        return ip;
    }

    /**
     * Records the current position as the start of the instruction about to be read. Relative jumps
     * ({@code JMPR}/{@code JROT}/{@code JROF}) are measured from here.
     */
    public void markInstructionStart()
    {
        instructionStart = ip;
    }

    /**
     * @return the position recorded by the most recent {@link #markInstructionStart()}
     */
    public int instructionStart()
    {
        return instructionStart;
    }

    /**
     * Moves the program counter to an absolute position.
     *
     * @param position the new position, within {@code [0, length]}
     * @throws HintingException if the position is out of range
     */
    public void seek(int position)
    {
        if (position < 0 || position > code.length)
        {
            throw new HintingException(
                    "bytecode seek out of range: " + position + " of " + code.length);
        }
        ip = position;
    }

    /**
     * Advances the program counter by a relative amount (may be negative).
     *
     * @param delta the number of bytes to skip
     * @throws HintingException if the result is out of range
     */
    public void skip(int delta)
    {
        seek(ip + delta);
    }

    /**
     * Reads the next byte as an unsigned 0-255 value (an opcode, or push operand).
     *
     * @return the next unsigned byte
     * @throws HintingException if the stream is exhausted
     */
    public int nextByte()
    {
        if (ip >= code.length)
        {
            throw new HintingException("bytecode read past end at " + ip);
        }
        return code[ip++] & 0xFF;
    }

    /**
     * Reads the next two bytes as a signed big-endian 16-bit word.
     *
     * @return the next signed word
     * @throws HintingException if fewer than two bytes remain
     */
    public int nextWord()
    {
        if (ip + 1 >= code.length)
        {
            throw new HintingException("bytecode word read past end at " + ip);
        }
        int hi = code[ip++] & 0xFF;
        int lo = code[ip++] & 0xFF;
        return (short) ((hi << 8) | lo);
    }
}
