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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The TrueType bytecode interpreter: the VM driver and the 256-entry opcode dispatch table.
 * <p>
 * It carries the execution engine - the {@link BytecodeStream} cursor driven dispatch loop, function
 * definition and calling with a depth cap, branching, the push family, and the
 * stack/arithmetic/logical/storage and point-moving opcodes - plus the size lifecycle: {@code fpgm}
 * executed once, {@code prep} executed per ppem change with the result saved as the per-glyph
 * template. An opcode with no handler and no {@code IDEF} binding throws {@link HintingException},
 * which the caller catches per glyph and falls back to the raw outline.
 *
 * @author Apache PDFBox
 */
class TrueTypeInterpreter
{
    /** Maximum {@code CALL}/{@code LOOPCALL} nesting depth, matching FreeType. */
    static final int MAX_CALL_DEPTH = 64;

    /** A single opcode handler. */
    @FunctionalInterface
    private interface OpHandler
    {
        void execute(ExecutionContext ctx);
    }

    // opcodes referenced by the engine itself (control flow / push)
    private static final int NPUSHB = 0x40;
    private static final int NPUSHW = 0x41;
    private static final int PUSHB_BASE = 0xB0;
    private static final int PUSHW_BASE = 0xB8;
    private static final int ELSE = 0x1B;
    private static final int IF = 0x58;
    private static final int EIF = 0x59;
    private static final int FDEF = 0x2C;
    private static final int ENDF = 0x2D;

    private final OpHandler[] dispatch = new OpHandler[256];
    private final Map<Integer, FunctionDef> functions = new HashMap<>();
    private final Map<Integer, FunctionDef> instructionDefs = new HashMap<>();

    private final int maxStackElements;
    private final int unitsPerEm;

    // The storage area and twilight zone belong to the size, not to one program run: a font may seed
    // them in prep and read them back from every glyph program. FreeType keeps both on the TT_Size and
    // clears them in tt_size_run_prep, which is what setPpem does below.
    private final int[] storage;
    private final Zone twilightZone;

    private byte[] fontProgram;
    private byte[] controlValueProgram;
    private int[] rawControlValues = new int[0];
    private int[] scaledControlValues = new int[0];

    private int ppem;
    private int pointSize;
    private GraphicsState savedState;
    private ExecutionTracer tracer;

    /**
     * @param maxStackElements operand stack capacity (from maxp)
     * @param maxStorage storage area size (from maxp)
     * @param maxTwilightPoints twilight zone size (from maxp)
     * @param unitsPerEm the font's unitsPerEm (from head)
     */
    public TrueTypeInterpreter(int maxStackElements, int maxStorage, int maxTwilightPoints,
            int unitsPerEm)
    {
        this.maxStackElements = maxStackElements;
        this.unitsPerEm = unitsPerEm;
        this.storage = new int[Math.max(maxStorage, 0)];
        this.twilightZone = new Zone(Math.max(maxTwilightPoints, 0), 0);
        buildDispatch();
    }

    // --- configuration ---------------------------------------------------

    /** @param program the raw {@code fpgm} bytecode, or null */
    public void setFontProgram(byte[] program)
    {
        this.fontProgram = program;
    }

    /** @param program the raw {@code prep} bytecode, or null */
    public void setControlValueProgram(byte[] program)
    {
        this.controlValueProgram = program;
    }

    /** @param values the raw control values in font units, or null */
    public void setControlValues(int[] values)
    {
        this.rawControlValues = values != null ? values : new int[0];
    }

    /**
     * Attaches (or clears with {@code null}) an execution tracer that emits one FreeType-comparable
     * line per executed instruction. Used by the trace-diff tooling to localize divergence; off in
     * normal operation.
     *
     * @param tracer the tracer, or null to disable tracing
     */
    public void setTracer(ExecutionTracer tracer)
    {
        this.tracer = tracer;
    }

    // --- lifecycle -------------------------------------------------------

    /**
     * Runs the font program ({@code fpgm}) once, populating the function table. Safe to call when
     * there is no font program.
     */
    public void prepareFontProgram()
    {
        functions.clear();
        instructionDefs.clear();
        if (fontProgram == null || fontProgram.length == 0)
        {
            return;
        }
        ExecutionContext ctx = newContext(new GraphicsState());
        run(ctx, new BytecodeStream(fontProgram));
    }

    /**
     * Establishes a new ppem: scales the control values, clears the storage area and twilight zone, runs
     * the control value program ({@code prep}) from a default graphics state, and saves the resulting
     * state as the per-glyph template. Whatever {@code prep} leaves in storage and the twilight zone
     * stays there for every glyph hinted at this size, which is why they are cleared here and not per
     * glyph - after FreeType's {@code tt_size_run_prep}. Anything the font program wrote to storage is
     * discarded, again as FreeType does: {@code fpgm} is only meant to define functions.
     *
     * @param ppemValue the pixels-per-em to render at
     * @param pointSizeValue the point size
     */
    public void setPpem(int ppemValue, int pointSizeValue)
    {
        this.ppem = ppemValue;
        this.pointSize = pointSizeValue;
        scaleControlValues();
        Arrays.fill(storage, 0);
        twilightZone.reset();

        GraphicsState gs = new GraphicsState();
        if (controlValueProgram != null && controlValueProgram.length > 0)
        {
            ExecutionContext ctx = newContext(gs);
            run(ctx, new BytecodeStream(controlValueProgram));
        }
        savedState = gs;
    }

    private void scaleControlValues()
    {
        scaledControlValues = new int[rawControlValues.length];
        for (int i = 0; i < rawControlValues.length; i++)
        {
            scaledControlValues[i] = Fixed.scale(rawControlValues[i], ppem, unitsPerEm);
        }
    }

    /**
     * Builds a fresh execution context wired to this interpreter's sizes, scaled control values and the
     * current ppem. The stack and per-run counters are new; the storage area and twilight zone are the
     * interpreter's own, so values {@code prep} left there are visible to the glyph programs.
     *
     * @param gs the graphics state the context starts from
     * @return a new execution context
     */
    public ExecutionContext newContext(GraphicsState gs)
    {
        ExecutionContext ctx = new ExecutionContext(this, gs, maxStackElements, storage,
                scaledControlValues, twilightZone);
        ctx.setUnitsPerEm(unitsPerEm);
        ctx.setPpem(ppem);
        ctx.setPointSize(pointSize);
        return ctx;
    }

    /**
     * Test/utility entry point: runs a standalone bytecode program from the saved (post-{@code prep})
     * state, or a default state if no size has been set, and returns the resulting context so callers
     * can inspect the stack and state.
     *
     * @param program the bytecode to run
     * @param ppemValue the ppem to run at
     * @return the execution context after the program completes
     */
    public ExecutionContext executeProgram(byte[] program, int ppemValue)
    {
        this.ppem = ppemValue;
        GraphicsState gs = savedState != null ? savedState.copy() : new GraphicsState();
        ExecutionContext ctx = newContext(gs);
        run(ctx, new BytecodeStream(program));
        return ctx;
    }

    /** @return the saved post-{@code prep} graphics state, or null if no size has been set */
    public GraphicsState getSavedState()
    {
        return savedState;
    }

    /** @return the function table populated by {@code fpgm} */
    public Map<Integer, FunctionDef> getFunctions()
    {
        return functions;
    }

    // --- execution engine ------------------------------------------------

    /**
     * Runs a bytecode stream to completion (or until an {@code ENDF} returns from a function body),
     * dispatching each opcode through the table.
     *
     * @param ctx the execution context
     * @param s the stream to run
     */
    public void run(ExecutionContext ctx, BytecodeStream s)
    {
        BytecodeStream previous = ctx.getStream();
        ctx.setStream(s);
        try
        {
            while (s.hasNext() && !ctx.isReturnFromFunction())
            {
                s.markInstructionStart();
                int opcode = s.nextByte();
                if (tracer != null)
                {
                    tracer.trace(s.instructionStart(), opcode, ctx);
                }
                dispatch[opcode].execute(ctx);
            }
        }
        finally
        {
            ctx.setStream(previous);
        }
    }

    /**
     * Calls the function with the given number, running its body until the matching {@code ENDF}.
     *
     * @param ctx the execution context
     * @param functionNumber the function to call
     * @throws HintingException if the function is undefined or the call depth is exceeded
     */
    public void callFunction(ExecutionContext ctx, int functionNumber)
    {
        FunctionDef def = functions.get(functionNumber);
        if (def == null)
        {
            throw new HintingException("call to undefined function " + functionNumber);
        }
        callBody(ctx, def);
    }

    /** Runs a function/instruction body from its entry point until the matching {@code ENDF}. */
    private void callBody(ExecutionContext ctx, FunctionDef def)
    {
        if (ctx.getCallDepth() >= MAX_CALL_DEPTH)
        {
            throw new HintingException("maximum call depth " + MAX_CALL_DEPTH + " exceeded");
        }
        ctx.enterCall();
        try
        {
            BytecodeStream body = new BytecodeStream(def.getProgram());
            body.seek(def.getEntryPoint());
            run(ctx, body);
            ctx.setReturnFromFunction(false);
        }
        finally
        {
            ctx.leaveCall();
        }
    }

    private void defineFunction(ExecutionContext ctx)
    {
        int functionNumber = ctx.pop();
        BytecodeStream s = ctx.getStream();
        functions.put(functionNumber, new FunctionDef(s.getCode(), s.position()));
        skipFunctionBody(s);
    }

    /** IDEF: binds the opcode on top of the stack to the following instructions (until ENDF). */
    private void defineInstruction(ExecutionContext ctx)
    {
        int opcode = ctx.pop();
        BytecodeStream s = ctx.getStream();
        instructionDefs.put(opcode & 0xFF, new FunctionDef(s.getCode(), s.position()));
        skipFunctionBody(s);
    }

    private void skipFunctionBody(BytecodeStream s)
    {
        while (s.hasNext())
        {
            int opcode = s.nextByte();
            if (opcode == ENDF)
            {
                return;
            }
            skipPushOperands(opcode, s);
        }
        throw new HintingException("FDEF without matching ENDF");
    }

    /**
     * On a false {@code IF}, skips forward to the matching {@code ELSE} or {@code EIF}, accounting for
     * nested {@code IF} blocks and for the inline operands of push instructions. Leaves the stream
     * positioned just after the terminator.
     */
    private void skipToElseOrEif(BytecodeStream s)
    {
        int depth = 0;
        while (s.hasNext())
        {
            int opcode = s.nextByte();
            if (opcode == IF)
            {
                depth++;
            }
            else if (opcode == EIF)
            {
                if (depth == 0)
                {
                    return;
                }
                depth--;
            }
            else if (opcode == ELSE && depth == 0)
            {
                return;
            }
            else
            {
                skipPushOperands(opcode, s);
            }
        }
        throw new HintingException("IF without matching EIF");
    }

    /**
     * After a true {@code IF} branch reaches its {@code ELSE}, skips the else-branch to the matching
     * {@code EIF}.
     */
    private void skipToEif(BytecodeStream s)
    {
        int depth = 0;
        while (s.hasNext())
        {
            int opcode = s.nextByte();
            if (opcode == IF)
            {
                depth++;
            }
            else if (opcode == EIF)
            {
                if (depth == 0)
                {
                    return;
                }
                depth--;
            }
            else
            {
                skipPushOperands(opcode, s);
            }
        }
        throw new HintingException("ELSE without matching EIF");
    }

    /** Advances the stream past the inline operands of a push opcode; a no-op for other opcodes. */
    private void skipPushOperands(int opcode, BytecodeStream s)
    {
        if (opcode == NPUSHB)
        {
            s.skip(s.nextByte());
        }
        else if (opcode == NPUSHW)
        {
            s.skip(2 * s.nextByte());
        }
        else if (opcode >= PUSHB_BASE && opcode <= PUSHB_BASE + 7)
        {
            s.skip(opcode - PUSHB_BASE + 1);
        }
        else if (opcode >= PUSHW_BASE && opcode <= PUSHW_BASE + 7)
        {
            s.skip(2 * (opcode - PUSHW_BASE + 1));
        }
    }

    // --- dispatch table --------------------------------------------------

    private void buildDispatch()
    {
        for (int i = 0; i < dispatch.length; i++)
        {
            final int opcode = i;
            dispatch[i] = ctx ->
            {
                // an opcode with no built-in handler may have been given one by IDEF
                FunctionDef def = instructionDefs.get(opcode);
                if (def != null)
                {
                    callBody(ctx, def);
                    return;
                }
                throw new HintingException(
                        String.format("unsupported TrueType opcode 0x%02X", opcode));
            };
        }

        installPushOps();
        installStackOps();
        installArithmeticOps();
        installLogicalOps();
        installFlowOps();
        installStateOps();
        installStorageAndCvtOps();
        installMiscOps();
        installVectorOps();
        installRoundOps();
        installPointOps();
        installInterpolationOps();
        installMeasureOps();
        installDeltaOps();
        installFlipOps();
    }

    private void installPushOps()
    {
        dispatch[NPUSHB] = ctx ->
        {
            int n = ctx.getStream().nextByte();
            for (int i = 0; i < n; i++)
            {
                ctx.push(ctx.getStream().nextByte());
            }
        };
        dispatch[NPUSHW] = ctx ->
        {
            int n = ctx.getStream().nextByte();
            for (int i = 0; i < n; i++)
            {
                ctx.push(ctx.getStream().nextWord());
            }
        };
        for (int k = 0; k < 8; k++)
        {
            final int count = k + 1;
            dispatch[PUSHB_BASE + k] = ctx ->
            {
                for (int i = 0; i < count; i++)
                {
                    ctx.push(ctx.getStream().nextByte());
                }
            };
            dispatch[PUSHW_BASE + k] = ctx ->
            {
                for (int i = 0; i < count; i++)
                {
                    ctx.push(ctx.getStream().nextWord());
                }
            };
        }
    }

    private void installStackOps()
    {
        dispatch[0x20] = ctx -> ctx.push(ctx.peek(0));                 // DUP
        dispatch[0x21] = ExecutionContext::pop;                       // POP
        dispatch[0x22] = ExecutionContext::clearStack;                // CLEAR
        dispatch[0x23] = ctx ->                                       // SWAP
        {
            int a = ctx.pop();
            int b = ctx.pop();
            ctx.push(a);
            ctx.push(b);
        };
        dispatch[0x24] = ctx -> ctx.push(ctx.getStackDepth());        // DEPTH
        dispatch[0x25] = ctx -> ctx.push(ctx.peek(ctx.pop() - 1));    // CINDEX
        dispatch[0x26] = ctx ->                                       // MINDEX
        {
            int k = ctx.pop();
            int[] tmp = new int[k];
            for (int i = 0; i < k; i++)
            {
                tmp[i] = ctx.pop();
            }
            for (int i = k - 2; i >= 0; i--)
            {
                ctx.push(tmp[i]);
            }
            ctx.push(tmp[k - 1]);
        };
        dispatch[0x8A] = ctx ->                                       // ROLL
        {
            int c = ctx.pop();
            int b = ctx.pop();
            int a = ctx.pop();
            ctx.push(b);
            ctx.push(c);
            ctx.push(a);
        };
    }

    private void installArithmeticOps()
    {
        dispatch[0x60] = ctx -> binary(ctx, (a, b) -> a + b);            // ADD
        dispatch[0x61] = ctx -> binary(ctx, (a, b) -> a - b);            // SUB
        dispatch[0x62] = ctx -> binary(ctx, Fixed::div);                 // DIV
        dispatch[0x63] = ctx -> binary(ctx, Fixed::mul);                 // MUL
        dispatch[0x64] = ctx -> ctx.push(Math.abs(ctx.pop()));           // ABS
        dispatch[0x65] = ctx -> ctx.push(-ctx.pop());                    // NEG
        dispatch[0x66] = ctx -> ctx.push(Fixed.floor(ctx.pop()));        // FLOOR
        dispatch[0x67] = ctx -> ctx.push(Fixed.ceil(ctx.pop()));         // CEILING
        dispatch[0x8B] = ctx -> binary(ctx, Math::max);                  // MAX
        dispatch[0x8C] = ctx -> binary(ctx, Math::min);                  // MIN
    }

    private void installLogicalOps()
    {
        dispatch[0x50] = ctx -> binary(ctx, (a, b) -> bool(a < b));      // LT
        dispatch[0x51] = ctx -> binary(ctx, (a, b) -> bool(a <= b));     // LTEQ
        dispatch[0x52] = ctx -> binary(ctx, (a, b) -> bool(a > b));      // GT
        dispatch[0x53] = ctx -> binary(ctx, (a, b) -> bool(a >= b));     // GTEQ
        dispatch[0x54] = ctx -> binary(ctx, (a, b) -> bool(a == b));     // EQ
        dispatch[0x55] = ctx -> binary(ctx, (a, b) -> bool(a != b));     // NEQ
        dispatch[0x56] = ctx -> ctx.push(bool(((Fixed.round(ctx.pop()) >> 6) & 1) != 0)); // ODD
        dispatch[0x57] = ctx -> ctx.push(bool(((Fixed.round(ctx.pop()) >> 6) & 1) == 0)); // EVEN
        dispatch[0x5A] = ctx -> binary(ctx, (a, b) -> bool(a != 0 && b != 0)); // AND
        dispatch[0x5B] = ctx -> binary(ctx, (a, b) -> bool(a != 0 || b != 0)); // OR
        dispatch[0x5C] = ctx -> ctx.push(bool(ctx.pop() == 0));          // NOT
    }

    private void installFlowOps()
    {
        dispatch[IF] = ctx ->
        {
            if (ctx.pop() == 0)
            {
                skipToElseOrEif(ctx.getStream());
            }
        };
        dispatch[ELSE] = ctx -> skipToEif(ctx.getStream());
        dispatch[EIF] = ctx -> { /* no-op terminator */ };
        dispatch[0x1C] = ctx -> jump(ctx, ctx.pop());                    // JMPR
        dispatch[0x78] = ctx ->                                          // JROT
        {
            int e = ctx.pop();
            int offset = ctx.pop();
            if (e != 0)
            {
                jump(ctx, offset);
            }
        };
        dispatch[0x79] = ctx ->                                          // JROF
        {
            int e = ctx.pop();
            int offset = ctx.pop();
            if (e == 0)
            {
                jump(ctx, offset);
            }
        };
        dispatch[FDEF] = this::defineFunction;
        dispatch[ENDF] = ctx -> ctx.setReturnFromFunction(true);
        dispatch[0x89] = this::defineInstruction; // IDEF
        dispatch[0x2B] = ctx -> callFunction(ctx, ctx.pop());            // CALL
        dispatch[0x2A] = ctx ->                                          // LOOPCALL
        {
            int functionNumber = ctx.pop();
            int count = ctx.pop();
            // the spec calls the count unsigned; FreeType runs nothing at all when it is not positive
            if (count <= 0)
            {
                return;
            }
            // charge the whole loop up front, so an absurd count fails before a single iteration runs
            ctx.countLoopCalls(count);
            for (int i = 0; i < count; i++)
            {
                callFunction(ctx, functionNumber);
            }
        };
    }

    /**
     * Jumps to {@code offset} bytes from the start of the current instruction. A backward jump is the
     * only way TrueType bytecode can loop other than {@code LOOPCALL}, so those are counted against the
     * run's budget and the program is abandoned once it exceeds it.
     *
     * @param ctx the execution context
     * @param offset the jump offset, relative to the current instruction
     * @throws HintingException if the stream position is out of range, or too many backward jumps
     */
    private static void jump(ExecutionContext ctx, int offset)
    {
        if (offset < 0)
        {
            ctx.countNegativeJump();
        }
        BytecodeStream s = ctx.getStream();
        s.seek(s.instructionStart() + offset);
    }

    private void installStateOps()
    {
        dispatch[0x17] = ctx -> ctx.getGraphicsState().setLoop(ctx.pop());            // SLOOP
        dispatch[0x10] = ctx -> ctx.getGraphicsState().setRp0(ctx.pop());            // SRP0
        dispatch[0x11] = ctx -> ctx.getGraphicsState().setRp1(ctx.pop());            // SRP1
        dispatch[0x12] = ctx -> ctx.getGraphicsState().setRp2(ctx.pop());            // SRP2
        dispatch[0x1A] = ctx -> ctx.getGraphicsState().setMinimumDistance(ctx.pop()); // SMD
        dispatch[0x5E] = ctx -> ctx.getGraphicsState().setDeltaBase(ctx.pop());      // SDB
        dispatch[0x5F] = ctx -> ctx.getGraphicsState().setDeltaShift(ctx.pop());     // SDS
        dispatch[0x1D] = ctx -> ctx.getGraphicsState().setControlValueCutIn(ctx.pop()); // SCVTCI
        dispatch[0x1E] = ctx -> ctx.getGraphicsState().setSingleWidthCutIn(ctx.pop()); // SSWCI
        dispatch[0x1F] = ctx -> ctx.getGraphicsState().setSingleWidthValue(ctx.pop()); // SSW

        dispatch[0x18] = roundState(GraphicsState.ROUND_TO_GRID);        // RTG
        dispatch[0x19] = roundState(GraphicsState.ROUND_TO_HALF_GRID);   // RTHG
        dispatch[0x3D] = roundState(GraphicsState.ROUND_TO_DOUBLE_GRID); // RTDG
        dispatch[0x7C] = roundState(GraphicsState.ROUND_UP_TO_GRID);     // RUTG
        dispatch[0x7D] = roundState(GraphicsState.ROUND_DOWN_TO_GRID);   // RDTG
        dispatch[0x7A] = roundState(GraphicsState.ROUND_OFF);            // ROFF
        dispatch[0x76] = ctx -> ctx.getGraphicsState().setSuperRound(Fixed.ONE, ctx.pop()); // SROUND
        // S45ROUND: grid period is the 45-degree diagonal, sqrt(2)/2 px ~= 45 in F26Dot6
        dispatch[0x77] = ctx -> ctx.getGraphicsState().setSuperRound(45, ctx.pop());        // S45ROUND
        dispatch[0x13] = ctx -> ctx.getGraphicsState().setZp0(ctx.pop());            // SZP0
        dispatch[0x14] = ctx -> ctx.getGraphicsState().setZp1(ctx.pop());            // SZP1
        dispatch[0x15] = ctx -> ctx.getGraphicsState().setZp2(ctx.pop());            // SZP2
        dispatch[0x16] = ctx ->                                                      // SZPS
        {
            int zone = ctx.pop();
            GraphicsState gs = ctx.getGraphicsState();
            gs.setZp0(zone);
            gs.setZp1(zone);
            gs.setZp2(zone);
        };
    }

    private void installStorageAndCvtOps()
    {
        dispatch[0x43] = ctx ->                                          // RS
        {
            int index = ctx.pop();
            ctx.push(read(ctx.getStorage(), index, "storage"));
        };
        dispatch[0x42] = ctx ->                                          // WS
        {
            int value = ctx.pop();
            int index = ctx.pop();
            write(ctx.getStorage(), index, value, "storage");
        };
        dispatch[0x45] = ctx ->                                          // RCVT
        {
            int index = ctx.pop();
            ctx.push(read(ctx.getControlValues(), index, "cvt"));
        };
        dispatch[0x44] = ctx ->                                          // WCVTP
        {
            int value = ctx.pop();
            int index = ctx.pop();
            write(ctx.getControlValues(), index, value, "cvt");
        };
        dispatch[0x70] = ctx ->                                          // WCVTF
        {
            int value = ctx.pop();
            int index = ctx.pop();
            write(ctx.getControlValues(), index,
                    Fixed.scale(value, ctx.getPpem(), ctx.getUnitsPerEm()), "cvt");
        };
    }

    private void installMiscOps()
    {
        dispatch[0x4B] = ctx -> ctx.push(ctx.getPpem());                 // MPPEM
        dispatch[0x4C] = ctx -> ctx.push(ctx.getPointSize());            // MPS
        dispatch[0x4F] = ExecutionContext::pop;                          // DEBUG (pops, no-op)
        dispatch[0x7E] = ExecutionContext::pop;                          // SANGW (deprecated, pops)
        dispatch[0x7F] = ctx -> { /* AA - deprecated no-op */ };         // AA
        dispatch[0x88] = ctx ->                                          // GETINFO
        {
            int selector = ctx.pop();
            int result = 0;
            if ((selector & 0x0001) != 0)
            {
                // rasterizer version 40: FreeType's "minimal" subpixel interpreter, which we mirror
                // for grayscale antialiased rendering (lighter stems than the classic v35)
                result |= 40;
            }
            // we always render grayscale-subpixel ("lean"), non-LCD: report the subpixel bits a v40
            // grayscale rasterizer returns so fonts take their lighter ClearType-aware code paths.
            // (the grayscale bit 12 is intentionally not set: FreeType clears exc->grayscale in lean mode)
            if ((selector & 0x0040) != 0)
            {
                result |= 1 << 13; // subpixel hinting active
            }
            if ((selector & 0x0400) != 0)
            {
                result |= 1 << 17; // ClearType hinting active
            }
            if ((selector & 0x0800) != 0)
            {
                result |= 1 << 18; // subpixel positioned
            }
            if ((selector & 0x1000) != 0)
            {
                result |= 1 << 19; // grayscale ClearType
            }
            ctx.push(result);
        };
    }

    // --- vector setters --------------------------------------------------

    private void installVectorOps()
    {
        dispatch[0x00] = ctx -> setProjAndFreedomAxis(ctx, false); // SVTCA[0] y
        dispatch[0x01] = ctx -> setProjAndFreedomAxis(ctx, true);  // SVTCA[1] x
        dispatch[0x02] = ctx -> setProjectionAxis(ctx, false);     // SPVTCA[0] y
        dispatch[0x03] = ctx -> setProjectionAxis(ctx, true);      // SPVTCA[1] x
        dispatch[0x04] = ctx -> setFreedomAxis(ctx, false);        // SFVTCA[0] y
        dispatch[0x05] = ctx -> setFreedomAxis(ctx, true);         // SFVTCA[1] x
        dispatch[0x06] = ctx -> setProjectionToLine(ctx, false);   // SPVTL[0] parallel
        dispatch[0x07] = ctx -> setProjectionToLine(ctx, true);    // SPVTL[1] perpendicular
        dispatch[0x08] = ctx -> setFreedomToLine(ctx, false);      // SFVTL[0] parallel
        dispatch[0x09] = ctx -> setFreedomToLine(ctx, true);       // SFVTL[1] perpendicular
        dispatch[0x86] = ctx -> setDualProjectionToLine(ctx, false); // SDPVTL[0]
        dispatch[0x87] = ctx -> setDualProjectionToLine(ctx, true);  // SDPVTL[1]
        dispatch[0x0E] = ctx ->                                    // SFVTPV
        {
            UnitVector pv = ctx.getGraphicsState().getProjectionVector();
            ctx.getGraphicsState().getFreedomVector().set(pv.getX(), pv.getY());
        };
        dispatch[0x0A] = ctx ->                                    // SPVFS
        {
            int y = ctx.pop();
            int x = ctx.pop();
            ctx.getGraphicsState().getProjectionVector().set(x, y);
            ctx.getGraphicsState().getDualProjectionVector().set(x, y);
        };
        dispatch[0x0B] = ctx ->                                    // SFVFS
        {
            int y = ctx.pop();
            int x = ctx.pop();
            ctx.getGraphicsState().getFreedomVector().set(x, y);
        };
        dispatch[0x0C] = ctx ->                                    // GPV
        {
            UnitVector pv = ctx.getGraphicsState().getProjectionVector();
            ctx.push(pv.getX());
            ctx.push(pv.getY());
        };
        dispatch[0x0D] = ctx ->                                    // GFV
        {
            UnitVector fv = ctx.getGraphicsState().getFreedomVector();
            ctx.push(fv.getX());
            ctx.push(fv.getY());
        };
    }

    private static void setProjAndFreedomAxis(ExecutionContext ctx, boolean xAxis)
    {
        setProjectionAxis(ctx, xAxis);
        setFreedomAxis(ctx, xAxis);
    }

    private static void setProjectionAxis(ExecutionContext ctx, boolean xAxis)
    {
        int x = xAxis ? Fixed.ONE_F2DOT14 : 0;
        int y = xAxis ? 0 : Fixed.ONE_F2DOT14;
        ctx.getGraphicsState().getProjectionVector().set(x, y);
        ctx.getGraphicsState().getDualProjectionVector().set(x, y);
    }

    private static void setFreedomAxis(ExecutionContext ctx, boolean xAxis)
    {
        int x = xAxis ? Fixed.ONE_F2DOT14 : 0;
        int y = xAxis ? 0 : Fixed.ONE_F2DOT14;
        ctx.getGraphicsState().getFreedomVector().set(x, y);
    }

    private void setProjectionToLine(ExecutionContext ctx, boolean perpendicular)
    {
        UnitVector[] v = lineVectors(ctx, perpendicular);
        ctx.getGraphicsState().getProjectionVector().set(v[0].getX(), v[0].getY());
        ctx.getGraphicsState().getDualProjectionVector().set(v[1].getX(), v[1].getY());
    }

    private void setFreedomToLine(ExecutionContext ctx, boolean perpendicular)
    {
        UnitVector[] v = lineVectors(ctx, perpendicular);
        ctx.getGraphicsState().getFreedomVector().set(v[0].getX(), v[0].getY());
    }

    private void setDualProjectionToLine(ExecutionContext ctx, boolean perpendicular)
    {
        UnitVector[] v = lineVectors(ctx, perpendicular);
        ctx.getGraphicsState().getProjectionVector().set(v[0].getX(), v[0].getY());
        ctx.getGraphicsState().getDualProjectionVector().set(v[1].getX(), v[1].getY());
    }

    /**
     * Pops two point numbers and returns {current-based, original-based} unit vectors along (or
     * perpendicular to) the line between them. The first point is taken from zp2, the second from zp1.
     */
    private UnitVector[] lineVectors(ExecutionContext ctx, boolean perpendicular)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int p2 = ctx.pop();
        int p1 = ctx.pop();
        Zone z1 = ctx.getZone(gs.getZp2());
        Zone z2 = ctx.getZone(gs.getZp1());
        UnitVector current = UnitVector.normalize(z2.getCurrentX()[p2] - z1.getCurrentX()[p1],
                z2.getCurrentY()[p2] - z1.getCurrentY()[p1]);
        UnitVector original = UnitVector.normalize(z2.getOriginalX()[p2] - z1.getOriginalX()[p1],
                z2.getOriginalY()[p2] - z1.getOriginalY()[p1]);
        if (perpendicular)
        {
            current = current.perpendicular();
            original = original.perpendicular();
        }
        return new UnitVector[] { current, original };
    }

    // --- rounding opcodes ------------------------------------------------

    private void installRoundOps()
    {
        for (int k = 0; k < 4; k++)
        {
            dispatch[0x68 + k] = ctx -> ctx.push(ctx.getGraphicsState().round(ctx.pop())); // ROUND[ab]
            dispatch[0x6C + k] = ctx -> ctx.push(ctx.pop());                                // NROUND[ab]
        }
    }

    // --- point movement --------------------------------------------------

    private void installPointOps()
    {
        dispatch[0x0F] = this::doIsect;              // ISECT
        dispatch[0x2E] = ctx -> doMDAP(ctx, false);  // MDAP[0] no round
        dispatch[0x2F] = ctx -> doMDAP(ctx, true);   // MDAP[1] round
        dispatch[0x3E] = ctx -> doMIAP(ctx, false);  // MIAP[0] no round
        dispatch[0x3F] = ctx -> doMIAP(ctx, true);   // MIAP[1] round + cut-in
        dispatch[0x3A] = ctx -> doMSIRP(ctx, false); // MSIRP[0]
        dispatch[0x3B] = ctx -> doMSIRP(ctx, true);  // MSIRP[1] set rp0
        dispatch[0x3C] = this::doAlignRp;            // ALIGNRP
        dispatch[0x27] = this::doAlignPts;           // ALIGNPTS
        dispatch[0x29] = this::doUtp;                // UTP
        dispatch[0x38] = this::doShpix;              // SHPIX
        dispatch[0x32] = ctx -> doShp(ctx, false);   // SHP[0] rp2/zp1
        dispatch[0x33] = ctx -> doShp(ctx, true);    // SHP[1] rp1/zp0
        dispatch[0x34] = ctx -> doShc(ctx, false);   // SHC[0]
        dispatch[0x35] = ctx -> doShc(ctx, true);    // SHC[1]
        dispatch[0x36] = ctx -> doShz(ctx, false);   // SHZ[0]
        dispatch[0x37] = ctx -> doShz(ctx, true);    // SHZ[1]
        for (int op = 0xC0; op <= 0xDF; op++)
        {
            final int code = op;
            dispatch[op] = ctx -> doMDRP(ctx, code); // MDRP[abcde]
        }
        for (int op = 0xE0; op <= 0xFF; op++)
        {
            final int code = op;
            dispatch[op] = ctx -> doMIRP(ctx, code); // MIRP[abcde]
        }
    }

    /**
     * ISECT: moves a point to the intersection of line A (a0,a1 in zp1) and line B (b0,b1 in zp0).
     * Mirrors FreeType's Ins_ISECT, including the parallel-lines fallback to the four-point average.
     */
    private void doIsect(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int b1 = ctx.pop();
        int b0 = ctx.pop();
        int a1 = ctx.pop();
        int a0 = ctx.pop();
        int point = ctx.pop();
        Zone za = ctx.getZone(gs.getZp1());
        Zone zb = ctx.getZone(gs.getZp0());
        Zone zp = ctx.getZone(gs.getZp2());

        int a0x = za.getCurrentX()[a0];
        int a0y = za.getCurrentY()[a0];
        int dax = za.getCurrentX()[a1] - a0x;
        int day = za.getCurrentY()[a1] - a0y;
        int b0x = zb.getCurrentX()[b0];
        int b0y = zb.getCurrentY()[b0];
        int dbx = zb.getCurrentX()[b1] - b0x;
        int dby = zb.getCurrentY()[b1] - b0y;
        int dx = b0x - a0x;
        int dy = b0y - a0y;

        int discriminant = Fixed.mulDiv(dax, -dby, 0x40) + Fixed.mulDiv(day, dbx, 0x40);
        int dotproduct = Fixed.mulDiv(dax, dbx, 0x40) + Fixed.mulDiv(day, dby, 0x40);

        // reject grazing intersections of nearly parallel lines, as FreeType does
        if (Math.abs((long) discriminant * 0x40) > Math.abs((long) dotproduct))
        {
            int val = Fixed.mulDiv(dx, -dby, 0x40) + Fixed.mulDiv(dy, dbx, 0x40);
            zp.getCurrentX()[point] = a0x + Fixed.mulDiv(val, dax, discriminant);
            zp.getCurrentY()[point] = a0y + Fixed.mulDiv(val, day, discriminant);
        }
        else
        {
            // parallel: average of the four line points
            zp.getCurrentX()[point] = (a0x + za.getCurrentX()[a1] + b0x + zb.getCurrentX()[b1]) / 2 / 2;
            zp.getCurrentY()[point] = (a0y + za.getCurrentY()[a1] + b0y + zb.getCurrentY()[b1]) / 2 / 2;
        }
        zp.getTouchedX()[point] = true;
        zp.getTouchedY()[point] = true;
    }

    private void doMDAP(ExecutionContext ctx, boolean round)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int point = ctx.pop();
        Zone zone = ctx.getZone(gs.getZp0());
        int cur = ctx.project(zone.getCurrentX()[point], zone.getCurrentY()[point]);
        int distance = round ? gs.round(cur) : cur;
        ctx.movePoint(zone, point, distance - cur);
        gs.setRp0(point);
        gs.setRp1(point);
    }

    private void doMIAP(ExecutionContext ctx, boolean round)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int cvtIndex = ctx.pop();
        int point = ctx.pop();
        Zone zone = ctx.getZone(gs.getZp0());
        int[] cvt = ctx.getControlValues();
        int value = cvtIndex >= 0 && cvtIndex < cvt.length ? cvt[cvtIndex] : 0;

        if (gs.getZp0() == 0)
        {
            // twilight point: establish its position from the control value along the projection
            UnitVector pv = gs.getProjectionVector();
            zone.getOriginalX()[point] = Fixed.mul14(value, pv.getX());
            zone.getOriginalY()[point] = Fixed.mul14(value, pv.getY());
            zone.getCurrentX()[point] = zone.getOriginalX()[point];
            zone.getCurrentY()[point] = zone.getOriginalY()[point];
        }
        int cur = ctx.project(zone.getCurrentX()[point], zone.getCurrentY()[point]);
        if (round)
        {
            if (Math.abs(value - cur) > gs.getControlValueCutIn())
            {
                value = cur;
            }
            value = gs.round(value);
        }
        ctx.movePoint(zone, point, value - cur);
        gs.setRp0(point);
        gs.setRp1(point);
    }

    private void doMSIRP(ExecutionContext ctx, boolean setRp0)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int distance = ctx.pop();
        int point = ctx.pop();
        Zone zp1 = ctx.getZone(gs.getZp1());
        Zone zp0 = ctx.getZone(gs.getZp0());
        int rp0 = gs.getRp0();
        int curDist = ctx.projectedDistance(zp1, point, zp0, rp0);
        ctx.movePoint(zp1, point, distance - curDist);
        gs.setRp1(rp0);
        gs.setRp2(point);
        if (setRp0)
        {
            gs.setRp0(point);
        }
    }

    private void doAlignRp(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        Zone zp1 = ctx.getZone(gs.getZp1());
        Zone zp0 = ctx.getZone(gs.getZp0());
        int rp0 = gs.getRp0();
        forEachLoopPoint(ctx, point ->
        {
            int dist = ctx.projectedDistance(zp1, point, zp0, rp0);
            ctx.movePoint(zp1, point, -dist);
        });
    }

    private void doAlignPts(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int p2 = ctx.pop();
        int p1 = ctx.pop();
        Zone zp1 = ctx.getZone(gs.getZp1());
        Zone zp0 = ctx.getZone(gs.getZp0());
        int distance = ctx.projectedDistance(zp0, p1, zp1, p2);
        // move both points to the midpoint of their projected positions
        ctx.movePoint(zp1, p2, distance / 2);
        ctx.movePoint(zp0, p1, -(distance - distance / 2));
    }

    private void doUtp(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        Zone zone = ctx.getZone(gs.getZp0());
        UnitVector fv = gs.getFreedomVector();
        int point = ctx.pop();
        if (fv.getX() != 0)
        {
            zone.getTouchedX()[point] = false;
        }
        if (fv.getY() != 0)
        {
            zone.getTouchedY()[point] = false;
        }
    }

    private void doShpix(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int amount = ctx.pop();
        Zone zp2 = ctx.getZone(gs.getZp2());
        forEachLoopPoint(ctx, point -> ctx.movePoint(zp2, point, amount));
    }

    private void doShp(ExecutionContext ctx, boolean useRp1)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int ref = referencePoint(gs, useRp1);
        Zone refZone = referenceZone(ctx, useRp1);
        int shift = referenceShift(ctx, refZone, ref);
        Zone zp2 = ctx.getZone(gs.getZp2());
        forEachLoopPoint(ctx, point ->
        {
            if (!(refZone == zp2 && point == ref))
            {
                ctx.movePoint(zp2, point, shift);
            }
        });
    }

    private void doShc(ExecutionContext ctx, boolean useRp1)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int ref = referencePoint(gs, useRp1);
        Zone refZone = referenceZone(ctx, useRp1);
        int shift = referenceShift(ctx, refZone, ref);
        int contour = ctx.pop();
        Zone zp2 = ctx.getZone(gs.getZp2());
        int[] ends = zp2.getContourEnds();
        if (contour < 0 || contour >= ends.length)
        {
            return;
        }
        int start = contour == 0 ? 0 : ends[contour - 1] + 1;
        for (int i = start; i <= ends[contour]; i++)
        {
            // FreeType's SHC does not move the reference point itself (it has already moved)
            if (!(refZone == zp2 && i == ref))
            {
                ctx.movePoint(zp2, i, shift);
            }
        }
    }

    private void doShz(ExecutionContext ctx, boolean useRp1)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int ref = referencePoint(gs, useRp1);
        Zone refZone = referenceZone(ctx, useRp1);
        int shift = referenceShift(ctx, refZone, ref);
        int zoneNumber = ctx.pop();
        Zone zone = ctx.getZone(zoneNumber);
        for (int i = 0; i < zone.getPointCount(); i++)
        {
            if (!(refZone == zone && i == ref))
            {
                ctx.movePoint(zone, i, shift);
            }
        }
    }

    private static int referencePoint(GraphicsState gs, boolean useRp1)
    {
        return useRp1 ? gs.getRp1() : gs.getRp2();
    }

    private static Zone referenceZone(ExecutionContext ctx, boolean useRp1)
    {
        GraphicsState gs = ctx.getGraphicsState();
        return ctx.getZone(useRp1 ? gs.getZp0() : gs.getZp1());
    }

    /** The projected distance the reference point (rp1 in zp0, or rp2 in zp1) has been moved. */
    private static int referenceShift(ExecutionContext ctx, Zone refZone, int ref)
    {
        return ctx.project(refZone.getCurrentX()[ref] - refZone.getOriginalX()[ref],
                refZone.getCurrentY()[ref] - refZone.getOriginalY()[ref]);
    }

    private void doMDRP(ExecutionContext ctx, int op)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int flags = op & 0x1F;
        boolean setRp0 = (flags & 0x10) != 0;
        boolean useMin = (flags & 0x08) != 0;
        boolean round = (flags & 0x04) != 0;
        int point = ctx.pop();
        Zone zp1 = ctx.getZone(gs.getZp1());
        Zone zp0 = ctx.getZone(gs.getZp0());
        int rp0 = gs.getRp0();

        int orgDist = ctx.dualProjectedDistance(zp1, point, zp0, rp0);
        orgDist = applySingleWidth(gs, orgDist);
        int distance = round ? gs.round(orgDist) : orgDist;
        distance = applyMinimumDistance(gs, useMin, orgDist, distance);

        int curDist = ctx.projectedDistance(zp1, point, zp0, rp0);
        ctx.movePoint(zp1, point, distance - curDist);
        gs.setRp1(rp0);
        gs.setRp2(point);
        if (setRp0)
        {
            gs.setRp0(point);
        }
    }

    private void doMIRP(ExecutionContext ctx, int op)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int flags = op & 0x1F;
        boolean setRp0 = (flags & 0x10) != 0;
        boolean useMin = (flags & 0x08) != 0;
        boolean round = (flags & 0x04) != 0;
        // the CVT entry number is on top of the stack, the point number below it
        int cvtIndex = ctx.pop();
        int point = ctx.pop();
        int[] cvt = ctx.getControlValues();
        int cvtValue = cvtIndex >= 0 && cvtIndex < cvt.length ? cvt[cvtIndex] : 0;
        cvtValue = applySingleWidth(gs, cvtValue);

        Zone zp1 = ctx.getZone(gs.getZp1());
        Zone zp0 = ctx.getZone(gs.getZp0());
        int rp0 = gs.getRp0();
        int orgDist = ctx.dualProjectedDistance(zp1, point, zp0, rp0);

        // auto-flip the control value to match the sign of the original distance
        if (gs.isAutoFlip() && (orgDist ^ cvtValue) < 0)
        {
            cvtValue = -cvtValue;
        }
        int distance;
        if (round)
        {
            // the control value cut-in only applies when both points are in the same zone
            if (gs.getZp0() == gs.getZp1()
                    && Math.abs(cvtValue - orgDist) > gs.getControlValueCutIn())
            {
                cvtValue = orgDist;
            }
            distance = gs.round(cvtValue);
        }
        else
        {
            distance = cvtValue;
        }
        distance = applyMinimumDistance(gs, useMin, orgDist, distance);

        int curDist = ctx.projectedDistance(zp1, point, zp0, rp0);
        ctx.movePoint(zp1, point, distance - curDist);
        gs.setRp1(rp0);
        gs.setRp2(point);
        if (setRp0)
        {
            gs.setRp0(point);
        }
    }

    private static int applySingleWidth(GraphicsState gs, int distance)
    {
        if (Math.abs(distance - gs.getSingleWidthValue()) < gs.getSingleWidthCutIn())
        {
            return distance >= 0 ? gs.getSingleWidthValue() : -gs.getSingleWidthValue();
        }
        return distance;
    }

    private static int applyMinimumDistance(GraphicsState gs, boolean useMin, int orgDist,
            int distance)
    {
        if (!useMin)
        {
            return distance;
        }
        int md = gs.getMinimumDistance();
        if (orgDist >= 0)
        {
            return distance < md ? md : distance;
        }
        return distance > -md ? -md : distance;
    }

    // --- interpolation ---------------------------------------------------

    private void installInterpolationOps()
    {
        dispatch[0x30] = ctx -> doIup(ctx, false); // IUP[0] y
        dispatch[0x31] = ctx -> doIup(ctx, true);  // IUP[1] x
        dispatch[0x39] = this::doIp;               // IP
    }

    private void doIup(ExecutionContext ctx, boolean xAxis)
    {
        // record that IUP ran on this axis; under backward-compatibility, once both axes are done the
        // glyph is frozen against further y moves (see ExecutionContext.movePoint)
        if (xAxis)
        {
            ctx.setIupxCalled();
        }
        else
        {
            ctx.setIupyCalled();
        }
        // IUP always operates on the glyph zone, directly on the x or y coordinate
        Zone zone = ctx.getZone(1);
        int[] cur = xAxis ? zone.getCurrentX() : zone.getCurrentY();
        int[] org = xAxis ? zone.getOriginalX() : zone.getOriginalY();
        boolean[] touched = xAxis ? zone.getTouchedX() : zone.getTouchedY();
        int[] ends = zone.getContourEnds();
        int start = 0;
        for (int end : ends)
        {
            interpolateContour(cur, org, touched, start, end);
            start = end + 1;
        }
    }

    private static void interpolateContour(int[] cur, int[] org, boolean[] touched, int start,
            int end)
    {
        if (end < start)
        {
            return;
        }
        int firstTouched = -1;
        int touchedCount = 0;
        for (int i = start; i <= end; i++)
        {
            if (touched[i])
            {
                if (firstTouched < 0)
                {
                    firstTouched = i;
                }
                touchedCount++;
            }
        }
        if (touchedCount == 0)
        {
            return;
        }
        if (touchedCount == 1)
        {
            int delta = cur[firstTouched] - org[firstTouched];
            if (delta != 0)
            {
                for (int i = start; i <= end; i++)
                {
                    if (i != firstTouched)
                    {
                        cur[i] = org[i] + delta;
                    }
                }
            }
            return;
        }
        // walk the contour cyclically, interpolating the untouched run between each touched pair
        int t1 = firstTouched;
        int seen = 0;
        for (int step = 1; step <= end - start + 1 && seen < touchedCount; step++)
        {
            int i = start + (firstTouched - start + step) % (end - start + 1);
            if (touched[i])
            {
                int u = t1 + 1 > end ? start : t1 + 1;
                while (u != i)
                {
                    interpolatePoint(cur, org, t1, i, u);
                    u = u + 1 > end ? start : u + 1;
                }
                t1 = i;
                seen++;
            }
        }
    }

    private static void interpolatePoint(int[] cur, int[] org, int t1, int t2, int u)
    {
        int orgLo;
        int orgHi;
        int curLo;
        int curHi;
        if (org[t1] <= org[t2])
        {
            orgLo = org[t1];
            curLo = cur[t1];
            orgHi = org[t2];
            curHi = cur[t2];
        }
        else
        {
            orgLo = org[t2];
            curLo = cur[t2];
            orgHi = org[t1];
            curHi = cur[t1];
        }
        if (org[u] <= orgLo)
        {
            cur[u] = org[u] + (curLo - orgLo);
        }
        else if (org[u] >= orgHi)
        {
            cur[u] = org[u] + (curHi - orgHi);
        }
        else if (orgHi == orgLo)
        {
            cur[u] = org[u] + (curLo - orgLo);
        }
        else
        {
            cur[u] = curLo + Fixed.mulDiv(org[u] - orgLo, curHi - curLo, orgHi - orgLo);
        }
    }

    private void doIp(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        Zone z0 = ctx.getZone(gs.getZp0());
        Zone z1 = ctx.getZone(gs.getZp1());
        Zone z2 = ctx.getZone(gs.getZp2());
        int rp1 = gs.getRp1();
        int rp2 = gs.getRp2();
        // Measure the original positions in unscaled font units (FreeType's orus) so the interpolation
        // ratio keeps full precision; the scaled F26Dot6 originals round each coordinate and can shift
        // an interpolated point by a unit, which a later rounding opcode then amplifies to a whole pixel.
        // Exception: twilight-zone points have no font-unit source, so their unscaled coordinates are
        // (0,0); using them would collapse every original distance to zero. When any zone here is the
        // twilight zone, FreeType measures the scaled originals instead, so we do the same.
        boolean twilight = gs.getZp0() == 0 || gs.getZp1() == 0 || gs.getZp2() == 0;
        int curRp1 = ctx.project(z0.getCurrentX()[rp1], z0.getCurrentY()[rp1]);
        int orgRp1 = twilight ? ctx.dualProject(z0.getOriginalX()[rp1], z0.getOriginalY()[rp1])
                : ctx.dualProject(z0.getUnscaledX()[rp1], z0.getUnscaledY()[rp1]);
        int curRp2 = ctx.project(z1.getCurrentX()[rp2], z1.getCurrentY()[rp2]);
        int orgRp2 = twilight ? ctx.dualProject(z1.getOriginalX()[rp2], z1.getOriginalY()[rp2])
                : ctx.dualProject(z1.getUnscaledX()[rp2], z1.getUnscaledY()[rp2]);
        int orgRange = orgRp2 - orgRp1;
        int curRange = curRp2 - curRp1;
        forEachLoopPoint(ctx, point ->
        {
            int orgP = twilight ? ctx.dualProject(z2.getOriginalX()[point], z2.getOriginalY()[point])
                    : ctx.dualProject(z2.getUnscaledX()[point], z2.getUnscaledY()[point]);
            int curP = ctx.project(z2.getCurrentX()[point], z2.getCurrentY()[point]);
            int newP;
            if (orgRange == 0)
            {
                newP = curRp1 + (orgP - orgRp1);
            }
            else
            {
                newP = curRp1 + Fixed.mulDiv(orgP - orgRp1, curRange, orgRange);
            }
            ctx.movePoint(z2, point, newP - curP);
        });
    }

    // --- measurement -----------------------------------------------------

    private void installMeasureOps()
    {
        dispatch[0x46] = ctx -> doGc(ctx, false); // GC[0] current
        dispatch[0x47] = ctx -> doGc(ctx, true);  // GC[1] original
        dispatch[0x48] = this::doScfs;            // SCFS
        dispatch[0x49] = ctx -> doMd(ctx, false); // MD[0] grid-fitted
        dispatch[0x4A] = ctx -> doMd(ctx, true);  // MD[1] original
    }

    private void doGc(ExecutionContext ctx, boolean original)
    {
        GraphicsState gs = ctx.getGraphicsState();
        Zone zone = ctx.getZone(gs.getZp2());
        int point = ctx.pop();
        if (original)
        {
            ctx.push(ctx.dualProject(zone.getOriginalX()[point], zone.getOriginalY()[point]));
        }
        else
        {
            ctx.push(ctx.project(zone.getCurrentX()[point], zone.getCurrentY()[point]));
        }
    }

    private void doScfs(ExecutionContext ctx)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int value = ctx.pop();
        int point = ctx.pop();
        Zone zone = ctx.getZone(gs.getZp2());
        int cur = ctx.project(zone.getCurrentX()[point], zone.getCurrentY()[point]);
        ctx.movePoint(zone, point, value - cur);
    }

    private void doMd(ExecutionContext ctx, boolean original)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int p2 = ctx.pop();
        int p1 = ctx.pop();
        Zone zp0 = ctx.getZone(gs.getZp0());
        Zone zp1 = ctx.getZone(gs.getZp1());
        // FreeType measures project(zp0[p1] - zp1[p2]); p1 is the deeper operand, p2 the top
        if (original)
        {
            ctx.push(ctx.dualProjectedDistance(zp0, p1, zp1, p2));
        }
        else
        {
            ctx.push(ctx.projectedDistance(zp0, p1, zp1, p2));
        }
    }

    // --- delta exceptions ------------------------------------------------

    private void installDeltaOps()
    {
        dispatch[0x5D] = ctx -> doDeltaP(ctx, 0); // DELTAP1
        dispatch[0x71] = ctx -> doDeltaP(ctx, 1); // DELTAP2
        dispatch[0x72] = ctx -> doDeltaP(ctx, 2); // DELTAP3
        dispatch[0x73] = ctx -> doDeltaC(ctx, 0); // DELTAC1
        dispatch[0x74] = ctx -> doDeltaC(ctx, 1); // DELTAC2
        dispatch[0x75] = ctx -> doDeltaC(ctx, 2); // DELTAC3
    }

    private void doDeltaP(ExecutionContext ctx, int band)
    {
        GraphicsState gs = ctx.getGraphicsState();
        Zone zone = ctx.getZone(gs.getZp0());
        int n = ctx.pop();
        for (int i = 0; i < n; i++)
        {
            int point = ctx.pop();
            int arg = ctx.pop();
            if (deltaTargetPpem(gs, arg, band) == ctx.getPpem()
                    && deltaPointAllowed(ctx, zone, point))
            {
                ctx.movePoint(zone, point, decodeDelta(arg & 0x0F, gs.getDeltaShift()));
            }
        }
    }

    /**
     * Backward-compatibility (v40 grayscale) gate for DELTAP: once IUP has run the delta is dropped,
     * and before IUP it is applied only to points already touched in y (or, for composites, when the
     * freedom vector has a y component). Outside backward-compatibility mode the delta always applies.
     * This keeps DELTAP from nudging untouched points off their interpolated grayscale positions.
     */
    private static boolean deltaPointAllowed(ExecutionContext ctx, Zone zone, int point)
    {
        if (!ctx.isBackwardCompatibility())
        {
            return true;
        }
        if (ctx.isIupDone())
        {
            return false;
        }
        boolean touchedY = point >= 0 && point < zone.getTouchedY().length
                && zone.getTouchedY()[point];
        return touchedY
                || (ctx.isComposite() && ctx.getGraphicsState().getFreedomVector().getY() != 0);
    }

    private void doDeltaC(ExecutionContext ctx, int band)
    {
        GraphicsState gs = ctx.getGraphicsState();
        int[] cvt = ctx.getControlValues();
        int n = ctx.pop();
        for (int i = 0; i < n; i++)
        {
            int cvtIndex = ctx.pop();
            int arg = ctx.pop();
            if (deltaTargetPpem(gs, arg, band) == ctx.getPpem() && cvtIndex >= 0
                    && cvtIndex < cvt.length)
            {
                cvt[cvtIndex] += decodeDelta(arg & 0x0F, gs.getDeltaShift());
            }
        }
    }

    private static int deltaTargetPpem(GraphicsState gs, int arg, int band)
    {
        return ((arg >> 4) & 0x0F) + gs.getDeltaBase() + band * 16;
    }

    private static int decodeDelta(int steps, int deltaShift)
    {
        int relative = steps < 8 ? steps - 8 : steps - 7; // 0..15 -> -8..-1, 1..8
        int unit = Fixed.ONE >> deltaShift;               // 1 / 2^deltaShift of a pixel
        return relative * unit;
    }

    // --- flip and scan-conversion ----------------------------------------

    private void installFlipOps()
    {
        dispatch[0x4D] = ctx -> ctx.getGraphicsState().setAutoFlip(true);  // FLIPON
        dispatch[0x4E] = ctx -> ctx.getGraphicsState().setAutoFlip(false); // FLIPOFF
        dispatch[0x80] = ctx ->                                            // FLIPPT
        {
            boolean[] onCurve = ctx.getZone(1).getOnCurve();
            forEachLoopPoint(ctx, point -> onCurve[point] = !onCurve[point]);
        };
        dispatch[0x81] = ctx -> flipRange(ctx, true);                      // FLIPRGON
        dispatch[0x82] = ctx -> flipRange(ctx, false);                     // FLIPRGOFF
        dispatch[0x85] = ctx -> ctx.getGraphicsState().setScanControl(ctx.pop()); // SCANCTRL
        dispatch[0x8D] = ctx -> ctx.getGraphicsState().setScanType(ctx.pop());    // SCANTYPE
        dispatch[0x8E] = ctx ->                                            // INSTCTRL
        {
            int selector = ctx.pop();
            int value = ctx.pop();
            if (selector == 3)
            {
                // native-ClearType fonts use INSTCTRL(L,3) to waive backward compatibility and program
                // points to the grid directly; L==4 turns the v40 movement restrictions off
                ctx.setBackwardCompatibility(value != 4);
            }
            else
            {
                ctx.getGraphicsState().setInstructControl(value & selector);
            }
        };
    }

    private static void flipRange(ExecutionContext ctx, boolean onCurve)
    {
        boolean[] flags = ctx.getZone(1).getOnCurve();
        int high = ctx.pop();
        int low = ctx.pop();
        for (int i = low; i <= high && i < flags.length; i++)
        {
            if (i >= 0)
            {
                flags[i] = onCurve;
            }
        }
    }

    // --- loop helper -----------------------------------------------------

    @FunctionalInterface
    private interface PointConsumer
    {
        void accept(int point);
    }

    /** Processes the graphics-state loop count of points, popping one per iteration, then resets the
     * loop counter to 1. */
    private static void forEachLoopPoint(ExecutionContext ctx, PointConsumer consumer)
    {
        int loop = ctx.getGraphicsState().getLoop();
        for (int i = 0; i < loop; i++)
        {
            consumer.accept(ctx.pop());
        }
        ctx.getGraphicsState().setLoop(1);
    }

    // --- handler helpers -------------------------------------------------

    @FunctionalInterface
    private interface IntBinaryOp
    {
        int apply(int a, int b);
    }

    private static void binary(ExecutionContext ctx, IntBinaryOp op)
    {
        int b = ctx.pop();
        int a = ctx.pop();
        ctx.push(op.apply(a, b));
    }

    private static int bool(boolean value)
    {
        return value ? 1 : 0;
    }

    private static OpHandler roundState(int state)
    {
        return ctx -> ctx.getGraphicsState().setRoundState(state);
    }

    private static int read(int[] array, int index, String name)
    {
        if (index < 0 || index >= array.length)
        {
            throw new HintingException(name + " index out of range: " + index);
        }
        return array[index];
    }

    private static void write(int[] array, int index, int value, String name)
    {
        if (index < 0 || index >= array.length)
        {
            throw new HintingException(name + " index out of range: " + index);
        }
        array[index] = value;
    }
}
