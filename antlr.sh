#!/bin/bash

TMP="/tmp/pbtbsparser/"
SRC="src/me/enerccio/sp/parser"
CHECK_JAVA="pythonParser.java"
CHECK_CLASS="me/enerccio/sp/parser"
PACKAGE="me.enerccio.sp.parser"
G="python.g4"
ANTLR="antlr4"
GRUN="grun"

if [[ $(uname) == MINGW* ]] ; then
	export CLASSPATH="$(pwd -W)/lib/antlr-4.5-complete.jar;$CLASSPATH"
	ANTLR="java -Xmx500M org.antlr.v4.Tool"
	GRUN="java org.antlr.v4.runtime.misc.TestRig"
else
	export CLASSPATH="/usr/share/java/antlr-complete.jar:$CLASSPATH"
fi

[ -e $TMP ] || mkdir -p $TMP

pushd $SRC
if [ $CHECK_JAVA -ot "$G" ] ; then
	echo "antlr4"
	rm *.java *.tokens
	$ANTLR -package "$PACKAGE" "$G" || exit 1
fi

if [ ! -e $TMP$CHECK_CLASS -o $TMP$CHECK_CLASS -ot $CHECK_JAVA ] ; then
	echo "javac"
	javac -d $TMP python*.java|| exit 1
fi

popd
cp test/x.spy $TMP/x.spy
cd $TMP

$GRUN me.enerccio.sp.parser.python file_input -encoding 'utf-8' $1 x.spy
