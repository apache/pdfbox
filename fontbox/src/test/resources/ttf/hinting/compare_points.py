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
Compares FontBox's grid-fitted points against FreeType's, font by font, and optionally against a
baseline FontBox dump from another build to show what a change moved.

    python3 compare_points.py /tmp/hint-compare [--ours fontbox] [--baseline baseline]

Reads every <font>.ft in the directory (from ft_points_dump.py) with the matching <font>.<ours> and,
if given, <font>.<baseline> (from HintedPointsDumpTool). Glyphs the FontBox hinter declined (NULL:
gasp, lowestRecPPEM, INSTCTRL) or whose point count differs are counted and skipped. Coordinates are
F26Dot6, so a delta of 64 is one pixel. Needs no third-party modules.
"""
import argparse
import glob
import os
from collections import Counter

LARGE = 9  # |delta| in 1/64 px from which a glyph is listed as a large divergence


def load(path):
    glyphs = {}
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            t = line.split()
            key = (int(t[0]), int(t[1]))
            char = chr(int(t[2][2:], 16))
            if t[3] == "NULL":
                glyphs[key] = (char, None)
                continue
            yi = t.index("Y")
            glyphs[key] = (char, (list(map(int, t[4:yi])), list(map(int, t[yi + 1:]))))
    return glyphs


def deltas(ours, ft):
    return ([abs(a - b) for a, b in zip(ours[0], ft[0])],
            [abs(a - b) for a, b in zip(ours[1], ft[1])])


class Stats:
    def __init__(self, ours, ft):
        self.glyphs = self.exact = self.exact_y = self.coords = self.coords_y_exact = 0
        self.max_delta = 0
        self.declined = self.mismatched = 0
        self.hist_x = Counter()
        self.hist_y = Counter()
        self.large = Counter()
        for key, (char, f) in ft.items():
            o = ours.get(key, (char, None))[1]
            if o is None:
                self.declined += 1
                continue
            if len(o[0]) != len(f[0]):
                self.mismatched += 1
                continue
            dx, dy = deltas(o, f)
            self.glyphs += 1
            self.exact += not any(dx) and not any(dy)
            self.exact_y += not any(dy)
            self.coords += len(dy)
            self.coords_y_exact += dy.count(0)
            self.max_delta = max([self.max_delta] + dx + dy)
            self.hist_x.update(min(v, LARGE) for v in dx)
            self.hist_y.update(min(v, LARGE) for v in dy)
            if max(dy + [0]) >= LARGE:
                self.large[char] += 1


def pct(a, b):
    return f"{100 * a / b:5.1f}%" if b else "  n/a "


def change(old, new, fmt):
    return fmt(new) if old is None else f"{fmt(old)} -> {fmt(new)}"


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("dir", help="directory holding the dumps")
    parser.add_argument("--ours", default="fontbox", help="suffix of the FontBox dumps")
    parser.add_argument("--baseline", help="suffix of baseline FontBox dumps to compare against")
    args = parser.parse_args()

    total_x, total_y = Counter(), Counter()
    changes = []
    print(f"{'font':26} {'glyphs':>6}  {'exact xy':>17}  {'exact y':>17}  {'y coords exact':>17}"
          f"  {'max delta':>12}")
    for ft_path in sorted(glob.glob(os.path.join(args.dir, "*.ft"))):
        name = os.path.splitext(os.path.basename(ft_path))[0]
        ours_path = os.path.join(args.dir, f"{name}.{args.ours}")
        if not os.path.exists(ours_path):
            print(f"{name:26} (no .{args.ours} dump)")
            continue
        ft = load(ft_path)
        ours = load(ours_path)
        new = Stats(ours, ft)
        old = None
        if args.baseline:
            base = load(os.path.join(args.dir, f"{name}.{args.baseline}"))
            old = Stats(base, ft)
            for key, (char, pts) in sorted(ours.items()):
                before = base.get(key, (char, None))[1]
                if pts != before and key in ft:
                    f = ft[key][1]

                    def worst(p):
                        if p is None or len(p[0]) != len(f[0]):
                            return "n/a"
                        dx, dy = deltas(p, f)
                        return str(max(dx + dy))
                    changes.append(f"  {name} '{char}' (gid {key[1]}) @{key[0]}ppem: worst delta "
                                   f"{worst(before)} -> {worst(pts)} /64")
        total_x.update(new.hist_x)
        total_y.update(new.hist_y)
        o = old
        print(f"{name:26} {new.glyphs:6}  "
              f"{change(o and (o.exact, o.glyphs), (new.exact, new.glyphs), lambda t: pct(*t)):>17}  "
              f"{change(o and (o.exact_y, o.glyphs), (new.exact_y, new.glyphs), lambda t: pct(*t)):>17}  "
              f"{change(o and (o.coords_y_exact, o.coords), (new.coords_y_exact, new.coords), lambda t: pct(*t)):>17}  "
              f"{change(o and o.max_delta, new.max_delta, str):>12}"
              + (f"  (declined {new.declined}, point count differs {new.mismatched})"
                 if new.declined or new.mismatched else ""))
        if new.large:
            top = ", ".join(f"'{c}' x{n}" for c, n in new.large.most_common(8))
            print(f"{'':26} y delta >= {LARGE}/64 in {sum(new.large.values())} glyph/ppem pairs: {top}")

    def hist(h):
        return "  ".join(f"{'>=' if k == LARGE else ''}{k}:{n}" for k, n in sorted(h.items()))
    print(f"\n|delta| histogram, x (1/64 px): {hist(total_x)}")
    print(f"|delta| histogram, y (1/64 px): {hist(total_y)}")
    if args.baseline:
        print(f"\n{len(changes)} glyph/ppem pairs changed against .{args.baseline}:")
        print("\n".join(changes) if changes else "  none")


if __name__ == "__main__":
    main()
