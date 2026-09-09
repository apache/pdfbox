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
 * All mutable state for a single run of the interpreter, bundled into one object so opcode handlers
 * share a uniform {@code execute(ExecutionContext)} signature and new state can be added without
 * touching every handler. It holds the operand stack, storage area, scaled control values, the two
 * point zones, the {@link GraphicsState}, the current {@link BytecodeStream}, and the ppem the program
 * is running at.
 * <p>
 * Not all of that state is per-run. The storage area and the twilight zone are owned by the
 * {@link TrueTypeInterpreter} and handed to every context at one size, because a font may compute values
 * into them in {@code prep} and read them back from each glyph program - FreeType keeps both on the
 * {@code TT_Size} for the same reason.
 *
 * @author Apache PDFBox
 */
class ExecutionContext
{
    private final TrueTypeInterpreter interpreter;
    private final GraphicsState graphicsState;

    private final int[] stack;
    private int stackPointer;

    // owned by the interpreter and shared by every context at one size, so prep can seed them
    private final int[] storage;
    private final Zone twilightZone;

    private final int[] controlValues;
    private Zone glyphZone;

    private int ppem;
    private int pointSize;
    private int unitsPerEm;

    private BytecodeStream stream;
    private int callDepth;
    private boolean returnFromFunction;

    // Execution budget. TrueType can only loop through a backward jump or a LOOPCALL, so bounding
    // those two bounds the whole program - without them a four-byte glyph program can spin forever.
    // Both counters are per-context, and one context is one top-level program run, so they need no
    // reset. After FreeType's neg_jump_counter / loopcall_counter in TT_RunIns.
    private long negativeJumpCounter;
    private long loopCallCounter;
    private int executionBudget = -1;

    // v40 "backward compatibility" (grayscale subpixel) state. When set, point moves in the x
    // direction are suppressed so stems are not grid-fit and darkened under antialiasing, and y moves
    // are frozen once IUP has run on both axes. Only enabled for the glyph program, never fpgm/prep.
    private boolean backwardCompatibility;
    private boolean iupxCalled;
    private boolean iupyCalled;
    private boolean composite;

    /**
     * @param interpreter the owning interpreter (for function calls)
     * @param graphicsState the graphics state this run starts from
     * @param maxStackElements operand stack capacity
     * @param storage the interpreter's storage area, shared across the runs at one size
     * @param controlValues the scaled control values (F26Dot6), or null
     * @param twilightZone the interpreter's twilight zone, shared across the runs at one size
     */
    public ExecutionContext(TrueTypeInterpreter interpreter, GraphicsState graphicsState,
            int maxStackElements, int[] storage, int[] controlValues, Zone twilightZone)
    {
        this.interpreter = interpreter;
        this.graphicsState = graphicsState;
        this.stack = new int[Math.max(maxStackElements, 1)];
        this.storage = storage;
        this.controlValues = controlValues != null ? controlValues : new int[0];
        this.twilightZone = twilightZone;
    }

    /** @return the owning interpreter */
    public TrueTypeInterpreter getInterpreter()
    {
        return interpreter;
    }

    /** @return the graphics state */
    public GraphicsState getGraphicsState()
    {
        return graphicsState;
    }

    // --- operand stack ---------------------------------------------------

    /**
     * Pushes a value onto the operand stack.
     *
     * @param value the value to push
     * @throws HintingException on stack overflow
     */
    public void push(int value)
    {
        if (stackPointer >= stack.length)
        {
            throw new HintingException("interpreter stack overflow at " + stackPointer);
        }
        stack[stackPointer++] = value;
    }

    /**
     * Pops a value from the operand stack.
     *
     * @return the popped value
     * @throws HintingException on stack underflow
     */
    public int pop()
    {
        if (stackPointer <= 0)
        {
            throw new HintingException("interpreter stack underflow");
        }
        return stack[--stackPointer];
    }

    /**
     * Returns the value {@code n} positions below the top without removing it ({@code peek(0)} is the
     * top of stack).
     *
     * @param n depth below the top
     * @return the value at that depth
     * @throws HintingException if the depth is out of range
     */
    public int peek(int n)
    {
        int index = stackPointer - 1 - n;
        if (index < 0 || index >= stackPointer)
        {
            throw new HintingException("interpreter stack peek out of range: " + n);
        }
        return stack[index];
    }

    /** @return the current stack depth */
    public int getStackDepth()
    {
        return stackPointer;
    }

    /** Empties the operand stack. */
    public void clearStack()
    {
        stackPointer = 0;
    }

    // --- execution budget -------------------------------------------------

    /**
     * The maximum number of backward jumps, and separately of {@code LOOPCALL} iterations, this run may
     * make before it is abandoned. Sized from the glyph's point count and the control value count the
     * way FreeType sizes its counters, so a legitimately loop-heavy program still completes while a
     * crafted one cannot run forever. Computed on first use, because the glyph zone is attached after
     * the context is built.
     *
     * @return the per-run budget
     */
    public int getExecutionBudget()
    {
        if (executionBudget < 0)
        {
            int points = glyphZone != null ? glyphZone.getPointCount() : 0;
            executionBudget = Math.max(50, 10 * points) + Math.max(50, controlValues.length / 10);
        }
        return executionBudget;
    }

    /**
     * Records one backward jump, failing the run once {@link #getExecutionBudget()} is exhausted.
     *
     * @throws HintingException if too many backward jumps have been made
     */
    public void countNegativeJump()
    {
        if (++negativeJumpCounter > getExecutionBudget())
        {
            throw new HintingException(
                    "too many backward jumps, limit is " + getExecutionBudget());
        }
    }

    /**
     * Adds {@code count} iterations to the {@code LOOPCALL} budget, failing before the loop is entered
     * rather than partway through it. The budget is cumulative across the run, so a program cannot slip
     * past it by issuing many small loops.
     *
     * @param count the number of iterations about to be run, always positive
     * @throws HintingException if the budget is exhausted
     */
    public void countLoopCalls(int count)
    {
        loopCallCounter += count;
        if (loopCallCounter > getExecutionBudget())
        {
            throw new HintingException("LOOPCALL runs too long, limit is " + getExecutionBudget()
                    + " iterations, asked for " + loopCallCounter);
        }
    }

    // --- storage and control values --------------------------------------

    /** @return the storage area, shared with every other run at this size */
    public int[] getStorage()
    {
        return storage;
    }

    /** @return the scaled control values in F26Dot6 */
    public int[] getControlValues()
    {
        return controlValues;
    }

    // --- zones -----------------------------------------------------------

    /** @return the twilight zone (zone 0), shared with every other run at this size */
    public Zone getTwilightZone()
    {
        return twilightZone;
    }

    /** @return the glyph zone (zone 1), or null if no glyph is loaded */
    public Zone getGlyphZone()
    {
        return glyphZone;
    }

    /** @param zone the glyph zone (zone 1) */
    public void setGlyphZone(Zone zone)
    {
        this.glyphZone = zone;
    }

    /**
     * Resolves a zone pointer (0 = twilight, 1 = glyph) to its {@link Zone}.
     *
     * @param zonePointer the zone pointer value
     * @return the corresponding zone
     * @throws HintingException if the pointer is invalid or the glyph zone is unset
     */
    public Zone getZone(int zonePointer)
    {
        if (zonePointer == 0)
        {
            return twilightZone;
        }
        if (zonePointer == 1)
        {
            if (glyphZone == null)
            {
                throw new HintingException("glyph zone referenced but not loaded");
            }
            return glyphZone;
        }
        throw new HintingException("invalid zone pointer: " + zonePointer);
    }

    // --- sizing ----------------------------------------------------------

    /** @return the active pixels-per-em */
    public int getPpem()
    {
        return ppem;
    }

    /** @param value the active pixels-per-em */
    public void setPpem(int value)
    {
        this.ppem = value;
    }

    /** @return the point size */
    public int getPointSize()
    {
        return pointSize;
    }

    /** @param value the point size */
    public void setPointSize(int value)
    {
        this.pointSize = value;
    }

    /** @return the font's unitsPerEm */
    public int getUnitsPerEm()
    {
        return unitsPerEm;
    }

    /** @param value the font's unitsPerEm */
    public void setUnitsPerEm(int value)
    {
        this.unitsPerEm = value;
    }

    // --- projection / freedom vector math --------------------------------

    /**
     * Dot product of two F2Dot14 vectors (or a coordinate against an F2Dot14 vector), returning the
     * result shifted back down by 14 bits with rounding. This is the projection primitive: projecting
     * an F26Dot6 coordinate onto an F2Dot14 unit vector yields an F26Dot6 distance.
     *
     * @param ax first vector x
     * @param ay first vector y
     * @param bx second vector x (F2Dot14)
     * @param by second vector y (F2Dot14)
     * @return the rounded dot product
     */
    public static int dot14(int ax, int ay, int bx, int by)
    {
        long product = (long) ax * bx + (long) ay * by;
        return (int) ((product + 0x2000) >> 14);
    }

    /**
     * Projects a coordinate onto the projection vector.
     *
     * @param x the x coordinate in F26Dot6
     * @param y the y coordinate in F26Dot6
     * @return the projected distance in F26Dot6
     */
    public int project(int x, int y)
    {
        UnitVector pv = graphicsState.getProjectionVector();
        return dot14(x, y, pv.getX(), pv.getY());
    }

    /**
     * Projects a coordinate onto the dual projection vector (used to measure original, unhinted
     * positions).
     *
     * @param x the x coordinate in F26Dot6
     * @param y the y coordinate in F26Dot6
     * @return the projected distance in F26Dot6
     */
    public int dualProject(int x, int y)
    {
        UnitVector dv = graphicsState.getDualProjectionVector();
        return dot14(x, y, dv.getX(), dv.getY());
    }

    /**
     * Returns the current (hinted) projected distance from point {@code p0} in {@code zone0} to point
     * {@code p1} in {@code zone1}, measured along the projection vector.
     */
    public int projectedDistance(Zone zone1, int p1, Zone zone0, int p0)
    {
        return project(zone1.getCurrentX()[p1] - zone0.getCurrentX()[p0],
                zone1.getCurrentY()[p1] - zone0.getCurrentY()[p0]);
    }

    /**
     * Returns the original (unhinted) projected distance from point {@code p0} in {@code zone0} to
     * point {@code p1} in {@code zone1}, measured along the dual projection vector.
     */
    public int dualProjectedDistance(Zone zone1, int p1, Zone zone0, int p0)
    {
        return dualProject(zone1.getOriginalX()[p1] - zone0.getOriginalX()[p0],
                zone1.getOriginalY()[p1] - zone0.getOriginalY()[p0]);
    }

    /**
     * Moves a point by the given projected distance along the freedom vector, touching the axes the
     * freedom vector acts on. The displacement is {@code distance * freedom / (freedom . projection)},
     * which reduces to {@code distance} when both vectors are the same axis.
     *
     * @param zone the zone holding the point
     * @param point the point index
     * @param distance the projected distance to move, in F26Dot6
     */
    public void movePoint(Zone zone, int point, int distance)
    {
        UnitVector fv = graphicsState.getFreedomVector();
        UnitVector pv = graphicsState.getProjectionVector();
        int fDotP = dot14(fv.getX(), fv.getY(), pv.getX(), pv.getY());
        if (fDotP == 0)
        {
            fDotP = Fixed.ONE_F2DOT14;
        }
        if (fv.getX() != 0)
        {
            // backward-compatibility (v40 grayscale): never grid-fit in the x direction, so horizontal
            // stems keep their natural sub-pixel position and are not darkened by antialiasing
            if (!backwardCompatibility)
            {
                zone.getCurrentX()[point] += Fixed.mulDiv(distance, fv.getX(), fDotP);
            }
            zone.getTouchedX()[point] = true;
        }
        if (fv.getY() != 0)
        {
            // y moves are allowed until IUP has run on both axes; afterwards the glyph is frozen
            if (!(backwardCompatibility && iupxCalled && iupyCalled))
            {
                zone.getCurrentY()[point] += Fixed.mulDiv(distance, fv.getY(), fDotP);
            }
            zone.getTouchedY()[point] = true;
        }
    }

    /** @return whether v40 backward-compatibility (grayscale subpixel) movement rules are active */
    public boolean isBackwardCompatibility()
    {
        return backwardCompatibility;
    }

    /** @param value whether to apply v40 backward-compatibility movement rules (glyph program only) */
    public void setBackwardCompatibility(boolean value)
    {
        this.backwardCompatibility = value;
    }

    /** Marks IUP[x] as having run; resets each program run. */
    public void setIupxCalled()
    {
        this.iupxCalled = true;
    }

    /** Marks IUP[y] as having run; resets each program run. */
    public void setIupyCalled()
    {
        this.iupyCalled = true;
    }

    /** @return whether IUP has run on both axes (the glyph is frozen for backward compatibility) */
    public boolean isIupDone()
    {
        return iupxCalled && iupyCalled;
    }

    /** @return whether the running program belongs to a composite glyph */
    public boolean isComposite()
    {
        return composite;
    }

    /** @param value whether the running program belongs to a composite glyph */
    public void setComposite(boolean value)
    {
        this.composite = value;
    }

    // --- execution cursor and call state ---------------------------------

    /** @return the current bytecode stream */
    public BytecodeStream getStream()
    {
        return stream;
    }

    /** @param stream the current bytecode stream */
    public void setStream(BytecodeStream stream)
    {
        this.stream = stream;
    }

    /** @return the current call nesting depth */
    public int getCallDepth()
    {
        return callDepth;
    }

    /** Increments the call nesting depth. */
    public void enterCall()
    {
        callDepth++;
    }

    /** Decrements the call nesting depth. */
    public void leaveCall()
    {
        callDepth--;
    }

    /** @return true if an {@code ENDF} asked the current function body to return */
    public boolean isReturnFromFunction()
    {
        return returnFromFunction;
    }

    /** @param value whether the current function body should return */
    public void setReturnFromFunction(boolean value)
    {
        this.returnFromFunction = value;
    }
}
