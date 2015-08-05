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
CHAR_NOCURLY
: ~('{' | '}')
;

mode StringMode;
CHAR_NORIGHTB
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

FCHAR_NOCURLY
: ~('{' | '}')
;

CLOSE_RF_SM: '}' -> popMode;