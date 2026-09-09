#!/usr/bin/env python3
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
TrueType hinting trace-diff harness.

Captures FreeType's per-instruction ``ttinterp`` trace for one glyph and aligns it, instruction by
instruction, against the FontBox interpreter's trace, reporting the first instruction where the
program counter, opcode, or operand stack diverges. This localizes a hinting bug to the exact
instruction far faster than comparing final outlines.

FreeType is used here only as an offline debugging oracle - never shipped or a build dependency.

Prerequisites
-------------
1. A FreeType built with tracing (the stock library has it compiled out). For example:

     curl -LO https://download.savannah.gnu.org/releases/freetype/freetype-2.13.2.tar.gz
     tar xzf freetype-2.13.2.tar.gz && cd freetype-2.13.2
     ./configure CFLAGS="-DFT_DEBUG_LEVEL_TRACE -g -O1" --disable-static
     make -j
   Then point freetype-py at objs/.libs/libfreetype.so (replace its bundled copy, or LD_PRELOAD it),
   and ``pip install freetype-py``.

2. The FontBox trace for the same glyph, produced by GlyphTraceTool:

     mvn -pl fontbox test -Dtest=GlyphTraceTool -Denforcer.skip=true \
         -Dtrace.gid=22 -Dtrace.ppem=11 -Dtrace.out=/tmp/our-trace.txt

Usage
-----
   FT2_DEBUG=ttinterp:7 python3 trace_diff.py --gid 22 --ppem 11 --ours /tmp/our-trace.txt

Both traces use a line format starting with ``<pc>  <opcode>`` and optionally carrying a point column
``Pn=(x,y)`` and/or a stack window ``# ...``. The program counter is always compared (catching
control-flow divergence). Pass --stack to also compare the operand window. To localize a *silent*
point-position divergence, pass --ft a FreeType point trace produced by ft_point_trace (which carries
``Pn=(x,y)``) and dump the FontBox trace with ``-Dtrace.point=n``; the point column is then compared.
"""
import argparse
import os
import re
import sys

PC = re.compile(r"^\s*(\d+)\s+(\S+)")
POINT = re.compile(r"P\d+=\((-?\d+),(-?\d+)\)")
STACK = re.compile(r"#(.*)$")


def parse_trace(lines):
    out = []
    for line in lines:
        m = PC.match(line)
        if not m:
            continue
        pc = int(m.group(1))
        op = m.group(2)
        pm = POINT.search(line)
        point = (int(pm.group(1)), int(pm.group(2))) if pm else None
        sm = STACK.search(line)
        stack = sm.group(1).split() if sm else []
        out.append((pc, op, point, stack))
    return out


def freetype_trace(font, gid, ppem):
    import freetype  # imported lazily so the rest of the tool works without it
    face = freetype.Face(font)
    face.set_pixel_sizes(0, ppem)
    # FT2_DEBUG must be set in the environment before FreeType is first used
    import io
    import contextlib
    # FreeType writes the trace to stderr; capture it
    err_fd = os.dup(2)
    r, w = os.pipe()
    os.dup2(w, 2)
    try:
        face.load_glyph(gid, freetype.FT_LOAD_NO_AUTOHINT | freetype.FT_LOAD_TARGET_MONO)
    finally:
        os.dup2(err_fd, 2)
        os.close(w)
    data = os.read(r, 1 << 22).decode("latin1", "replace")
    os.close(r)
    os.close(err_fd)
    return data.splitlines()


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--font", default=os.path.join(os.path.dirname(__file__), "..",
                                                   "LiberationSans-Regular.ttf"))
    ap.add_argument("--gid", type=int, required=True)
    ap.add_argument("--ppem", type=int, default=11)
    ap.add_argument("--ours", required=True, help="FontBox trace file from GlyphTraceTool")
    ap.add_argument("--ft", help="FreeType trace file (e.g. from ft_point_trace); "
                                 "if omitted, FreeType's ttinterp trace is captured via freetype-py")
    ap.add_argument("--stack", action="store_true", help="also compare the operand stack window")
    ap.add_argument("--point", action="store_true", help="compare the Pn=(x,y) point column")
    args = ap.parse_args()

    if args.ft:
        with open(args.ft, encoding="utf-8") as fh:
            ft = parse_trace(fh.readlines())
    else:
        ft = parse_trace(freetype_trace(args.font, args.gid, args.ppem))
    with open(args.ours, encoding="utf-8") as fh:
        ours = parse_trace(fh.readlines())

    # FreeType traces fpgm + prep + glyph; FontBox traces only the glyph program. Align on the tail.
    print(f"FreeType instructions: {len(ft)}   FontBox instructions: {len(ours)}")
    ft_tail = ft[-len(ours):] if len(ft) >= len(ours) else ft

    for i, (a, b) in enumerate(zip(ft_tail, ours)):
        pc_a, op_a, pt_a, st_a = a
        pc_b, op_b, pt_b, st_b = b
        mismatch = pc_a != pc_b
        if args.stack and st_a[: len(st_b)] != st_b[: len(st_a)]:
            mismatch = True
        if args.point and pt_a is not None and pt_b is not None and pt_a != pt_b:
            mismatch = True
        if mismatch:
            print(f"\nFirst divergence at aligned instruction {i} (the instruction that produced it "
                  "is the one before, where the point/stack still matched):")
            print(f"  FreeType: pc={pc_a:6d} {op_a:10s} pt={pt_a} # {' '.join(st_a)}")
            print(f"  FontBox : pc={pc_b:6d} {op_b:10s} pt={pt_b} # {' '.join(st_b)}")
            print("  (context, FreeType | FontBox):")
            for j in range(max(0, i - 4), i + 1):
                print(f"    {ft_tail[j][0]:6d} {ft_tail[j][1]:10s} {ft_tail[j][2]} | "
                      f"{ours[j][0]:6d} {ours[j][1]:10s} {ours[j][2]}")
            return 1
    print(f"\nNo divergence over {min(len(ft_tail), len(ours))} aligned instructions for the compared "
          "columns.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
