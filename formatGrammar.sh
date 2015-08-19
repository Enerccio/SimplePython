#!/bin/bash

TMP="/tmp/pbtbsparser/"
SRC="src/me/enerccio/sp/parser"
CHECK_CLASS="me/enerccio/sp/parser"
PACKAGE="me.enerccio.sp.parser"
ANTLR="antlr4"

function compile {
	if [ "$1""Parser.java" -ot "$1"".g4" ] ; then
		echo "antlr4"
		rm "$1"*.java "$1"*.tokens
		$ANTLR -package "$PACKAGE" "$1.g4" || exit 1
	fi
}

if [[ $(uname) == MINGW* ]] ; then
	export CLASSPATH="$(pwd -W)/lib/antlr-4.5-complete.jar;$CLASSPATH"
	ANTLR="java -Xmx500M org.antlr.v4.Tool"
	GRUN="java org.antlr.v4.runtime.misc.TestRig"
else
	export CLASSPATH="/usr/share/java/antlr-complete.jar:$CLASSPATH"
fi

[ -e $TMP ] || mkdir -p $TMP

pushd $SRC

compile "formatLexer"
compile "formatParser"
compile "formatterLexer"
compile "formatterParser"
