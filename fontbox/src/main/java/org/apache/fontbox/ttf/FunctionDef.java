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
 * A function defined by an {@code FDEF} instruction: the bytecode program it lives in (normally the
 * {@code fpgm}) and the offset of its first body instruction, just past the {@code FDEF}. {@code CALL}
 * and {@code LOOPCALL} run the body from this offset until the matching {@code ENDF}.
 *
 * @author Apache PDFBox
 */
class FunctionDef
{
    private final byte[] program;
    private final int entryPoint;

    /**
     * @param program the bytecode the function body lives in
     * @param entryPoint the offset of the first instruction after {@code FDEF}
     */
    public FunctionDef(byte[] program, int entryPoint)
    {
        this.program = program;
        this.entryPoint = entryPoint;
    }

    /** @return the bytecode the function body lives in */
    public byte[] getProgram()
    {
        return program;
    }

    /** @return the offset of the first body instruction */
    public int getEntryPoint()
    {
        return entryPoint;
    }
}
