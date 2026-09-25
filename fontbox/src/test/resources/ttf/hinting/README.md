<!---
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--->

# TrueType hinting verification tooling

These are developer/debugging tools for the FontBox TrueType bytecode interpreter
(`org.apache.fontbox.ttf.instruction`). They use **FreeType only as an offline reference
implementation** — FreeType is never linked, shipped, or a build dependency. The committed data files
are plain coordinate/trace facts, not derivatives of FreeType.

## Files

| File | Purpose |
|------|---------|
| `generate_golden.py` | Dumps FreeType's post-hinting outline points for the openly licensed test fonts to `<font>-<ppem>.txt`. These back `GoldenHintingTest`. |
| `LiberationSans-Regular-*.txt` | The committed golden coordinate data (one file per ppem). |
| `trace_diff.py` | Aligns FreeType's per-instruction trace against the FontBox interpreter's trace and reports the first divergence (program counter, operand stack, or point coordinate). |
| `ft_point_trace.c` | FreeType single-stepper: dumps one glyph point's coordinate per instruction, for localizing *silent* point-position divergence. |
| `ft_points_dump.py` | Dumps FreeType's grid-fitted points for any fonts over a range of characters and ppems. The FontBox half is the `HintedPointsDumpTool` test class. |
| `compare_points.py` | Compares those two dumps font by font, optionally against a baseline FontBox dump to show what a change moved. |
| `README.md` | This file. |

## Golden coordinate test (CI)

`GoldenHintingTest` compares the interpreter's grid-fitted points against the committed `.txt` dumps.
To regenerate the dumps after intentional changes:

```sh
pip install --user freetype-py
python3 generate_golden.py
```

## Multi-font point comparison (manual)

`GoldenHintingTest` covers one font and allows some tolerance, so it can miss a change that only
shows in other fonts. For a wider check, dump every printable ASCII character plus a few accented
(composite) letters at ppems 9-32 from both FreeType and FontBox, for any fonts you have locally, and
compare point by point. Only the dumps' coordinates are used; nothing is committed.

```sh
OUT=/tmp/hint-compare
FONTS="/usr/share/fonts/truetype/msttcorefonts/georgia.ttf /usr/share/fonts/truetype/msttcorefonts/comic.ttf"

# 1. FreeType (from this directory; pip install --user freetype-py):
python3 ft_points_dump.py --out $OUT $FONTS

# 2. FontBox, from the repository root (writes <font>.fontbox):
mvn -pl fontbox test -Dtest=HintedPointsDumpTool -Denforcer.skip=true \
    -Ddump.fonts=$(echo $FONTS | tr ' ' ',') -Ddump.out=$OUT

# 3. optional baseline: the same with the code before your change (e.g. after `git stash`),
#    written under another suffix:
mvn -pl fontbox test -Dtest=HintedPointsDumpTool -Denforcer.skip=true \
    -Ddump.fonts=$(echo $FONTS | tr ' ' ',') -Ddump.out=$OUT -Ddump.suffix=baseline

# 4. compare (no third-party modules needed):
python3 compare_points.py $OUT --baseline baseline
```

Per font, the report gives the share of glyphs matching FreeType exactly, the share of y coordinates
matching exactly, and the largest delta (64 = one pixel), along with the characters with any y delta
of 9/64 or more. With `--baseline` it shows each figure before and after, and lists every glyph and
ppem whose points changed, with the worst delta to FreeType before and after. Glyphs the hinter
declines (gasp, `lowestRecPPEM`, INSTCTRL) are counted and skipped. A glyph that differs from FreeType
here can then be traced with the trace-diff harness below.

## Trace-diff harness (manual debugging)

When a glyph's final points differ from FreeType, the trace diff localizes the cause to a single
instruction far faster than staring at coordinates. It needs a FreeType built **with tracing** (the
stock library has it compiled out):

```sh
curl -LO https://download.savannah.gnu.org/releases/freetype/freetype-2.13.2.tar.gz
tar xzf freetype-2.13.2.tar.gz && cd freetype-2.13.2
./configure CFLAGS="-DFT_DEBUG_LEVEL_TRACE -g -O1" --disable-static
make -j
# point freetype-py at the result (replace its bundled copy or LD_PRELOAD it):
cp objs/.libs/libfreetype.so.6.* "$(python3 -c 'import freetype,os;print(os.path.dirname(freetype.__file__))')/libfreetype.so"
```

Then dump the FontBox trace for a glyph and diff it:

```sh
# 1. FontBox trace (from the fontbox module dir):
mvn -pl fontbox test -Dtest=GlyphTraceTool -Denforcer.skip=true \
    -Dtrace.gid=164 -Dtrace.ppem=11 -Dtrace.out=/tmp/our-trace.txt

# 2. diff against FreeType (from this directory):
FT2_DEBUG=ttinterp:7 python3 trace_diff.py --gid 164 --ppem 11 --ours /tmp/our-trace.txt --stack
```

Both traces use the line format `<pc>  <MNEMONIC>  # <stack window, top first>`. The tool compares the
program counter (catches control-flow divergence) and, with `--stack`, the operand window (catches a
diverging computed value — this is how the `DIV`-rounding bug was found). If neither diverges, the
remaining difference is a *silent* point-position computation in a point-moving opcode (MDRP/MIRP/IP/…)
that never reaches the stack — compare final point positions to find it.

The interpreter side is driven by `TrueTypeInterpreter.setTracer(ExecutionTracer)`; it is off in
normal operation.

### Localizing a *silent* point divergence (points extension)

When the stack matches FreeType end-to-end but the final points still differ, the divergence is a
point-moving opcode computing a slightly different displacement from an input that never reaches the
stack (e.g. a CVT value). To find which instruction, compare the point coordinate itself per
instruction. `ft_point_trace.c` is the FreeType half (build instructions are in its header comment);
it needs a **static** FreeType built with the bytecode interpreter so it can link the internal
`TT_RunIns` and single-step via the debug hook.

```sh
# FreeType per-instruction point trace (point 8 of glyph 648):
./ft_point_trace ../LiberationSans-Regular.ttf 648 11 8 > /tmp/ft-pt.txt

# FontBox per-instruction point trace for the same point:
mvn -pl fontbox test -Dtest=GlyphTraceTool -Denforcer.skip=true \
    -Dtrace.gid=648 -Dtrace.ppem=11 -Dtrace.point=8 -Dtrace.out=/tmp/our-pt.txt

# diff the point column:
python3 trace_diff.py --gid 648 --ppem 11 --ours /tmp/our-pt.txt --ft /tmp/ft-pt.txt --point
```

`ft_point_trace` loads glyphs with the grayscale target by default, matching `GlyphHinter` (v40
backward compatibility: no x grid-fitting); add a fifth argument `mono` for the monochrome target.

The output names the exact instruction whose result first differs - e.g. it localized the
`a-circumflex` residual to a single `MIRP` (the circumflex height), where the point goes in equal
(80,513) and comes out (80,512) in FreeType versus (80,484) in FontBox.
