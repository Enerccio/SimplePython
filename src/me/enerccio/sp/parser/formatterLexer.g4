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
 
lexer grammar formatterLexer;

OPEN_RF: '{' -> pushMode(RuleMode);

TEXT_NOCURLY
: CHAR_NOCURLY+
;

fragment CHAR_NOCURLY
: '{{' | '}}' | ~('{' | '}')
;

mode StringMode;

TEXT_NORIGHTB
: CHAR_NORIGHTB+
;

fragment CHAR_NORIGHTB
: ~(']')
;

RIX: ']' -> popMode;

mode RuleMode;

EXLM: '!' ;
COLON: ':' -> mode(SpecMode);
DOT: '.';
CLOSE_RF: '}' -> popMode;
LIX: '[' -> pushMode(StringMode);
LS: 's';
ZERO: '0';
 
DECIMAL_INTEGER
: NON_ZERO_DIGIT DIGIT*
| ZERO+
;

OCT_INTEGER
: ZERO [oO] OCT_DIGIT+
;

HEX_INTEGER
: ZERO [xX] HEX_DIGIT+
;

BIN_INTEGER
: ZERO [bB]? BIN_DIGIT+
;
 
fragment NON_ZERO_DIGIT
: [1-9]
;

fragment DIGIT
: [0-9]
;

fragment OCT_DIGIT
: [0-7]
;

fragment HEX_DIGIT
: [0-9a-fA-F]
;

fragment BIN_DIGIT
: [01]
;
 
NAME
: NAME_CONTENT
;
 
NAME_CONTENT
: ID_START ID_CONTINUE*
;
 
fragment ID_START
: '_'
| [A-Z]
| [a-z]
;
 
fragment ID_CONTINUE
: ID_START
| [0-9]
;
 
mode SpecMode;

FTEXT_NOCURLY
: FCHAR_NOCURLY+
;

fragment FCHAR_NOCURLY
: ~('{' | '}')
;

OPEN_RFL: '{' -> pushMode(SpecRuleMode);
CLOSE_RF_SM: '}' -> popMode;

mode SpecRuleMode ;

FDOT: '.';
CLOSE_RFL: '}' -> popMode;
FLIX: '[' -> pushMode(SpecStringMode);
FZERO: '0';
 
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
 
FNAME
: NAME_CONTENT
;
 
FNAME_CONTENT
: FID_START FID_CONTINUE*
;
 
fragment FID_START
: '_'
| [A-Z]
| [a-z]
;
 
fragment FID_CONTINUE
: ID_START
| [0-9]
;

mode SpecStringMode;

FTEXT_NORIGHTB
: FCHAR_NORIGHTB+
;

fragment FCHAR_NORIGHTB
: ~(']')
;

FRIX: ']' -> popMode;