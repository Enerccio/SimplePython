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
 
parser grammar formatterParser;

options {   tokenVocab = formatterLexer; }

source_stream
 : segments EOF
 ;
 
segments 
 : segment*
 ;
 
segment
 : (text | replacement_field)
 ;
 
text
 : CHAR_NOCURLY+
 ;
 
replacement_field
 : OPEN_RF field_name? (EXLM conversion)? ((COLON format_spec CLOSE_RF_SM) | CLOSE_RF)
 ;
 
field_name 
 : arg_name accessor*
 ;
 
accessor
 : (DOT attribute_name) | LIX element_index RIX
 ;
 
arg_name
 : integer | identifier 
 ;
 
integer
 : ZERO
 | DECIMAL_INTEGER
 | OCT_INTEGER
 | HEX_INTEGER
 | BIN_INTEGER
 ;
 
attribute_name
 : identifier
 ;
 
element_index
: index_string
;

index_string
: CHAR_NORIGHTB+
;

identifier
: NAME
;

conversion
: conversionType
;

conversionType
: LS
;

format_spec
: FCHAR_NOCURLY+
;