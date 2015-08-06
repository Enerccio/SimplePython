/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
 
lexer grammar formatLexer;

FILLALIGN
: ~('{' | '}') (LT | MT | EQ | EXP) 
;

LT: '<';
MT: '>';
EQ: '=';
EXP: '^';
PLUS: '+';
MINUS: '-';
SPACE: ' ';
HASH: '#';
FZERO: '0';
TYPE: 'b' | 'c' | 'd' | 'e' | 'E' | 'f' | 'g' | 'G' | 'n' | 'o' | 's' | 'x' | 'X' | '%';
COMMA: ',';
FSDOT: '.';

FDECIMAL_INTEGER
: FNON_ZERO_DIGIT FDIGIT*
| FZERO+
;

FOCT_INTEGER
: FZERO [oO] FOCT_DIGIT+
;

FHEX_INTEGER
: FZERO [xX] FHEX_DIGIT+
;

FBIN_INTEGER
: FZERO [bB]? FBIN_DIGIT+
;
 
fragment FNON_ZERO_DIGIT
: [1-9]
;

fragment FDIGIT
: [0-9]
;

fragment FOCT_DIGIT
: [0-7]
;

fragment FHEX_DIGIT
: [0-9a-fA-F]
;

fragment FBIN_DIGIT
: [01]
;