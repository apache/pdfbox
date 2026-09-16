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

/*
 * FreeType per-instruction point tracer - the FreeType half of the "points extension" to the
 * trace-diff harness. It single-steps the TrueType bytecode interpreter via the debug hook and prints,
 * for each instruction, the current coordinate of one glyph point. Diffing this against the FontBox
 * trace (GlyphTraceTool with -Dtrace.point) localizes a *silent* point-position divergence (one a
 * point-moving opcode produces without it ever reaching the operand stack) to a single instruction.
 *
 * FreeType is used here only as an offline debugging oracle - never shipped or a build dependency.
 *
 * Build (against a static FreeType built with the bytecode interpreter; internal headers required):
 *
 *   FT=/path/to/freetype-2.13.2
 *   ./$FT/configure CFLAGS="-DFT_DEBUG_LEVEL_TRACE -DFT_DEBUG_LEVEL_DEBUG -g -O0" \
 *       --enable-static --disable-shared --without-zlib --without-png --without-harfbuzz \
 *       --without-brotli --without-bzip2 && make -C $FT -j
 *   gcc -DFT2_BUILD_LIBRARY -I$FT/include -I$FT/src ft_point_trace.c \
 *       $FT/objs/.libs/libfreetype.a -lm -o ft_point_trace
 *
 * Run:
 *   ./ft_point_trace LiberationSans-Regular.ttf <gid> <ppem> <point>   # lines: "<pc> op=0xNN Pn=(x,y)"
 */
#include <stdio.h>
#include <stdlib.h>

#include <ft2build.h>
#include FT_FREETYPE_H
#include <freetype/ftmodapi.h>     /* FT_Set_Debug_Hook, FT_DEBUG_HOOK_TRUETYPE */
#include "truetype/ttinterp.h"     /* TT_ExecContext, TT_RunIns (internal) */

static int g_point = -1;

static FT_Error
trace_hook( void*  exec )
{
  TT_ExecContext  exc = (TT_ExecContext)exec;
  FT_Error        err = FT_Err_Ok;


  exc->instruction_trap = 1;             /* make TT_RunIns return after each instruction */

  while ( exc->IP < exc->codeSize )
  {
    long    ip = exc->IP;
    FT_Byte op = exc->code[ip];


    if ( g_point >= 0 && exc->pts.n_points > g_point )
      printf( "%06ld op=0x%02X P%d=(%ld,%ld)\n", ip, op, g_point,
              (long)exc->pts.cur[g_point].x, (long)exc->pts.cur[g_point].y );
    else
      printf( "%06ld op=0x%02X\n", ip, op );

    err = TT_RunIns( exec );
    if ( err )
      break;
  }

  return err;
}


int
main( int argc, char** argv )
{
  FT_Library  lib;
  FT_Face     face;


  if ( argc < 5 )
  {
    fprintf( stderr, "usage: %s <font.ttf> <gid> <ppem> <point>\n", argv[0] );
    return 2;
  }

  g_point = atoi( argv[4] );

  if ( FT_Init_FreeType( &lib ) )
    return 1;

  FT_Set_Debug_Hook( lib, FT_DEBUG_HOOK_TRUETYPE, (FT_DebugHook_Func)trace_hook );

  if ( FT_New_Face( lib, argv[1], 0, &face ) )
    return 1;

  FT_Set_Pixel_Sizes( face, 0, atoi( argv[3] ) );
  FT_Load_Glyph( face, atoi( argv[2] ),
                 FT_LOAD_NO_AUTOHINT | FT_LOAD_TARGET_MONO );

  FT_Done_Face( face );
  FT_Done_FreeType( lib );
  return 0;
}
