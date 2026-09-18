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
Generates TrueType hinting golden reference data from FreeType, for the Java golden tests.

FreeType is used here ONLY as an offline verification oracle - it is never shipped, linked, or made a
build dependency. The committed JSON files are plain coordinate facts about the fonts (the F26Dot6
points a correct bytecode interpreter must produce), not a derivative of FreeType. See
hinting_plan.md "Oracle licensing".

Run with freetype-py installed (`pip install freetype-py`):

    python3 generate_golden.py

It writes <font>-<ppem>.txt next to this script in a compact, dependency-free format (so the Java
test needs no JSON library). The glyphs are loaded with the monochrome hinting target so FreeType runs
the native TrueType bytecode interpreter in full-pixel (non-subpixel) mode, which is what the FontBox
interpreter implements.

Format (one file per ppem):

    font <name>
    ppem <n>
    freetype <version>
    glyph <gid> <char>
    contours <c0,c1,...>
    x <x0> <x1> ...
    y <y0> <y1> ...
    glyph ...
"""
import os

import freetype

HERE = os.path.dirname(os.path.abspath(__file__))
FONT_DIR = os.path.normpath(os.path.join(HERE, ".."))

FONTS = ["LiberationSans-Regular.ttf"]
PPEMS = [11, 13, 16, 24]
# simple, well-hinted glyphs plus common composites (accented letters: base glyph + diacritic)
CHARS = "HILEThoxn0123456789" + "áàâäãéèçñüÁÉÑÜ"

# native bytecode hinting (no autohinter), grayscale target => FreeType's v40 "minimal" subpixel
# interpreter with backward-compatibility (no x grid-fitting, y frozen post-IUP). This matches how
# PDFBox rasterizes (Java2D is always antialiased); see GlyphHinter / ExecutionContext.movePoint.
LOAD_FLAGS = freetype.FT_LOAD_NO_AUTOHINT | freetype.FT_LOAD_TARGET_NORMAL

# Prepended to every generated file: these are checked into an Apache project, and the parser in
# GoldenHintingTest ignores any line that is not "glyph "/"x "/"y ", so comments cost nothing.
LICENSE_HEADER = [
    "# Licensed to the Apache Software Foundation (ASF) under one or more",
    "# contributor license agreements.  See the NOTICE file distributed with",
    "# this work for additional information regarding copyright ownership.",
    "# The ASF licenses this file to You under the Apache License, Version 2.0",
    "# (the \"License\"); you may not use this file except in compliance with",
    "# the License.  You may obtain a copy of the License at",
    "#",
    "#      http://www.apache.org/licenses/LICENSE-2.0",
    "#",
    "# Unless required by applicable law or agreed to in writing, software",
    "# distributed under the License is distributed on an \"AS IS\" BASIS,",
    "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
    "# See the License for the specific language governing permissions and",
    "# limitations under the License.",
]


def dump_font(font_name):
    face = freetype.Face(os.path.join(FONT_DIR, font_name))
    version = ".".join(str(v) for v in freetype.version())
    for ppem in PPEMS:
        face.set_pixel_sizes(0, ppem)
        lines = list(LICENSE_HEADER)
        lines += [f"font {font_name}", f"ppem {ppem}", f"freetype {version}"]
        count = 0
        for ch in CHARS:
            gid = face.get_char_index(ord(ch))
            if gid == 0:
                continue
            face.load_glyph(gid, LOAD_FLAGS)
            outline = face.glyph.outline
            lines.append(f"glyph {gid} {ch}")
            lines.append("contours " + ",".join(str(c) for c in outline.contours))
            lines.append("x " + " ".join(str(p[0]) for p in outline.points))
            lines.append("y " + " ".join(str(p[1]) for p in outline.points))
            count += 1
        out = os.path.join(HERE, f"{os.path.splitext(font_name)[0]}-{ppem}.txt")
        with open(out, "w") as fh:
            fh.write("\n".join(lines) + "\n")
        print(f"wrote {out} ({count} glyphs)")


if __name__ == "__main__":
    for font in FONTS:
        dump_font(font)
