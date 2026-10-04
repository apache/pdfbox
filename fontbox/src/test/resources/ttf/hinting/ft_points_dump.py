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
Dumps FreeType's grid-fitted outline points for a range of characters and ppems, in the format
written by FontBox's HintedPointsDumpTool, so compare_points.py can line the two up.

FreeType is used here only as an offline reference implementation - never shipped, linked, or a
build dependency. Needs freetype-py (`pip install --user freetype-py`).

    python3 ft_points_dump.py --out /tmp/hint-compare font1.ttf font2.ttf ...

Writes <out>/<font name>.ft, one line per (ppem, glyph): `<ppem> <gid> U+<code> X <x...> Y <y...>`.
The glyphs are loaded like generate_golden.py: native bytecode hinting, grayscale target (FreeType's
v40 interpreter with backward compatibility), which is how PDFBox rasterizes.
"""
import argparse
import os

import freetype

# keep in step with HintedPointsDumpTool.DEFAULT_CHARS / DEFAULT_PPEMS
DEFAULT_CHARS = "".join(chr(c) for c in range(33, 127)) + "áàâäãéèçñüÁÉÑÜ"
DEFAULT_PPEMS = "9,10,11,12,13,14,16,18,20,24,32"

LOAD_FLAGS = freetype.FT_LOAD_NO_AUTOHINT | freetype.FT_LOAD_TARGET_NORMAL


def dump_font(path, out_dir, ppems, chars):
    face = freetype.Face(path)
    name = os.path.splitext(os.path.basename(path))[0]
    out_path = os.path.join(out_dir, name + ".ft")
    with open(out_path, "w", encoding="utf-8") as out:
        for ppem in ppems:
            face.set_pixel_sizes(0, ppem)
            for ch in chars:
                gid = face.get_char_index(ord(ch))
                if gid == 0:
                    continue
                face.load_glyph(gid, LOAD_FLAGS)
                points = face.glyph.outline.points
                out.write(f"{ppem} {gid} U+{ord(ch):04X} X "
                          + " ".join(str(p[0]) for p in points) + " Y "
                          + " ".join(str(p[1]) for p in points) + "\n")
    print(f"wrote {out_path}")


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--out", required=True, help="output directory")
    parser.add_argument("--ppems", default=DEFAULT_PPEMS, help="comma-separated ppems")
    parser.add_argument("--chars", default=DEFAULT_CHARS, help="characters to dump")
    parser.add_argument("fonts", nargs="+", help="TrueType font files")
    args = parser.parse_args()
    os.makedirs(args.out, exist_ok=True)
    ppems = [int(p) for p in args.ppems.split(",")]
    print(f"FreeType {'.'.join(str(v) for v in freetype.version())}")
    for path in args.fonts:
        dump_font(path, args.out, ppems, args.chars)


if __name__ == "__main__":
    main()
