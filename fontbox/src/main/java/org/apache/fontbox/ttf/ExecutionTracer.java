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

import java.io.PrintStream;

/**
 * A first-class, toggleable execution tracer for the interpreter. When attached to a
 * {@link TrueTypeInterpreter} it emits one line per executed instruction, just before the instruction
 * runs, in a format deliberately close to FreeType's {@code FT2_DEBUG=ttinterp} output:
 *
 * <pre>
 *   &lt;pc&gt;  &lt;MNEMONIC&gt;  # &lt;top of stack, deepest..top&gt;
 * </pre>
 *
 * The program counter and the operand stack are what matter for diffing: feeding the same glyph
 * through this tracer and through FreeType and aligning the two traces by instruction index pinpoints
 * the first instruction where control flow or an operand value diverges. See
 * {@code src/test/resources/ttf/hinting/trace_diff.py} for the diff tool.
 *
 * @author Apache PDFBox
 */
class ExecutionTracer
{
    private static final String[] MNEMONICS = buildMnemonics();

    /** Number of top-of-stack operands to print, matching FreeType's trace window. */
    private static final int STACK_WINDOW = 8;

    private final PrintStream out;
    private final int tracePoint;

    /**
     * @param out where to write trace lines
     */
    public ExecutionTracer(PrintStream out)
    {
        this(out, -1);
    }

    /**
     * @param out where to write trace lines
     * @param tracePoint a glyph-zone point index whose current coordinate is appended to each line
     * (for localizing silent point-position divergence), or -1 to omit
     */
    public ExecutionTracer(PrintStream out, int tracePoint)
    {
        this.out = out;
        this.tracePoint = tracePoint;
    }

    /**
     * @param opcode an opcode value 0-255
     * @return the mnemonic for that opcode
     */
    public static String mnemonic(int opcode)
    {
        return MNEMONICS[opcode & 0xFF];
    }

    /**
     * Emits a trace line for the instruction about to execute.
     *
     * @param pc the program-counter position of the instruction
     * @param opcode the opcode about to run
     * @param ctx the execution context (its stack is sampled)
     */
    void trace(int pc, int opcode, ExecutionContext ctx)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%06d  %-11s", pc, MNEMONICS[opcode & 0xFF]));
        if (tracePoint >= 0)
        {
            Zone zone = ctx.getGlyphZone();
            if (zone != null && tracePoint < zone.getPointCount())
            {
                sb.append(String.format(" P%d=(%d,%d)", tracePoint,
                        zone.getCurrentX()[tracePoint], zone.getCurrentY()[tracePoint]));
            }
        }
        sb.append(" #");
        // top of stack first, matching FreeType's ttinterp window
        int window = Math.min(STACK_WINDOW, ctx.getStackDepth());
        for (int k = 0; k < window; k++)
        {
            sb.append(' ').append(ctx.peek(k));
        }
        out.println(sb);
    }

    private static String[] buildMnemonics()
    {
        String[] m = new String[256];
        for (int i = 0; i < 256; i++)
        {
            m[i] = String.format("INS_%02X", i);
        }
        // axis-variant vector setters: [y] for the even (0) code, [x] for the odd (1) code
        put(m, 0x00, "SVTCA[y]");
        put(m, 0x01, "SVTCA[x]");
        put(m, 0x02, "SPVTCA[y]");
        put(m, 0x03, "SPVTCA[x]");
        put(m, 0x04, "SFVTCA[y]");
        put(m, 0x05, "SFVTCA[x]");
        put(m, 0x06, "SPVTL[||]");
        put(m, 0x07, "SPVTL[+]");
        put(m, 0x08, "SFVTL[||]");
        put(m, 0x09, "SFVTL[+]");
        put(m, 0x0A, "SPVFS");
        put(m, 0x0B, "SFVFS");
        put(m, 0x0C, "GPV");
        put(m, 0x0D, "GFV");
        put(m, 0x0E, "SFVTPV");
        put(m, 0x0F, "ISECT");
        put(m, 0x10, "SRP0");
        put(m, 0x11, "SRP1");
        put(m, 0x12, "SRP2");
        put(m, 0x13, "SZP0");
        put(m, 0x14, "SZP1");
        put(m, 0x15, "SZP2");
        put(m, 0x16, "SZPS");
        put(m, 0x17, "SLOOP");
        put(m, 0x18, "RTG");
        put(m, 0x19, "RTHG");
        put(m, 0x1A, "SMD");
        put(m, 0x1B, "ELSE");
        put(m, 0x1C, "JMPR");
        put(m, 0x1D, "SCVTCI");
        put(m, 0x1E, "SSWCI");
        put(m, 0x1F, "SSW");
        put(m, 0x20, "DUP");
        put(m, 0x21, "POP");
        put(m, 0x22, "CLEAR");
        put(m, 0x23, "SWAP");
        put(m, 0x24, "DEPTH");
        put(m, 0x25, "CINDEX");
        put(m, 0x26, "MINDEX");
        put(m, 0x27, "ALIGNPTS");
        put(m, 0x29, "UTP");
        put(m, 0x2A, "LOOPCALL");
        put(m, 0x2B, "CALL");
        put(m, 0x2C, "FDEF");
        put(m, 0x2D, "ENDF");
        put(m, 0x2E, "MDAP[nr]");
        put(m, 0x2F, "MDAP[rnd]");
        put(m, 0x30, "IUP[y]");
        put(m, 0x31, "IUP[x]");
        put(m, 0x32, "SHP[rp2]");
        put(m, 0x33, "SHP[rp1]");
        put(m, 0x34, "SHC[rp2]");
        put(m, 0x35, "SHC[rp1]");
        put(m, 0x36, "SHZ[rp2]");
        put(m, 0x37, "SHZ[rp1]");
        put(m, 0x38, "SHPIX");
        put(m, 0x39, "IP");
        put(m, 0x3A, "MSIRP[nr]");
        put(m, 0x3B, "MSIRP[rp0]");
        put(m, 0x3C, "ALIGNRP");
        put(m, 0x3D, "RTDG");
        put(m, 0x3E, "MIAP[nr]");
        put(m, 0x3F, "MIAP[rnd]");
        put(m, 0x40, "NPUSHB");
        put(m, 0x41, "NPUSHW");
        put(m, 0x42, "WS");
        put(m, 0x43, "RS");
        put(m, 0x44, "WCVTP");
        put(m, 0x45, "RCVT");
        put(m, 0x46, "GC[cur]");
        put(m, 0x47, "GC[org]");
        put(m, 0x48, "SCFS");
        put(m, 0x49, "MD[grid]");
        put(m, 0x4A, "MD[org]");
        put(m, 0x4B, "MPPEM");
        put(m, 0x4C, "MPS");
        put(m, 0x4D, "FLIPON");
        put(m, 0x4E, "FLIPOFF");
        put(m, 0x4F, "DEBUG");
        put(m, 0x50, "LT");
        put(m, 0x51, "LTEQ");
        put(m, 0x52, "GT");
        put(m, 0x53, "GTEQ");
        put(m, 0x54, "EQ");
        put(m, 0x55, "NEQ");
        put(m, 0x56, "ODD");
        put(m, 0x57, "EVEN");
        put(m, 0x58, "IF");
        put(m, 0x59, "EIF");
        put(m, 0x5A, "AND");
        put(m, 0x5B, "OR");
        put(m, 0x5C, "NOT");
        put(m, 0x5D, "DELTAP1");
        put(m, 0x5E, "SDB");
        put(m, 0x5F, "SDS");
        put(m, 0x60, "ADD");
        put(m, 0x61, "SUB");
        put(m, 0x62, "DIV");
        put(m, 0x63, "MUL");
        put(m, 0x64, "ABS");
        put(m, 0x65, "NEG");
        put(m, 0x66, "FLOOR");
        put(m, 0x67, "CEILING");
        put(m, 0x70, "WCVTF");
        put(m, 0x71, "DELTAP2");
        put(m, 0x72, "DELTAP3");
        put(m, 0x73, "DELTAC1");
        put(m, 0x74, "DELTAC2");
        put(m, 0x75, "DELTAC3");
        put(m, 0x76, "SROUND");
        put(m, 0x77, "S45ROUND");
        put(m, 0x78, "JROT");
        put(m, 0x79, "JROF");
        put(m, 0x7A, "ROFF");
        put(m, 0x7C, "RUTG");
        put(m, 0x7D, "RDTG");
        put(m, 0x7E, "SANGW");
        put(m, 0x7F, "AA");
        put(m, 0x80, "FLIPPT");
        put(m, 0x81, "FLIPRGON");
        put(m, 0x82, "FLIPRGOFF");
        put(m, 0x85, "SCANCTRL");
        put(m, 0x86, "SDPVTL[||]");
        put(m, 0x87, "SDPVTL[+]");
        put(m, 0x88, "GETINFO");
        put(m, 0x89, "IDEF");
        put(m, 0x8A, "ROLL");
        put(m, 0x8B, "MAX");
        put(m, 0x8C, "MIN");
        put(m, 0x8D, "SCANTYPE");
        put(m, 0x8E, "INSTCTRL");
        for (int k = 0; k < 4; k++)
        {
            put(m, 0x68 + k, "ROUND[" + k + "]");
            put(m, 0x6C + k, "NROUND[" + k + "]");
        }
        for (int k = 0; k < 8; k++)
        {
            put(m, 0xB0 + k, "PUSHB[" + (k + 1) + "]");
            put(m, 0xB8 + k, "PUSHW[" + (k + 1) + "]");
        }
        for (int op = 0xC0; op <= 0xDF; op++)
        {
            put(m, op, "MDRP[" + Integer.toHexString(op & 0x1F) + "]");
        }
        for (int op = 0xE0; op <= 0xFF; op++)
        {
            put(m, op, "MIRP[" + Integer.toHexString(op & 0x1F) + "]");
        }
        return m;
    }

    private static void put(String[] m, int opcode, String name)
    {
        m[opcode] = name;
    }
}
