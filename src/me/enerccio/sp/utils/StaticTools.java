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
package me.enerccio.sp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.parser.formatLexer;
import me.enerccio.sp.parser.formatParser;
import me.enerccio.sp.parser.formatterLexer;
import me.enerccio.sp.parser.formatterParser;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.sequences.TupleObject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class StaticTools {
	
	/**
	 * Resolves diamons via L3 algorithm.
	 * @author Enerccio
	 *
	 */
	public static class DiamondResolver {
		private DiamondResolver(){}

		/**
		 * Resolves type hierarchy diamonds via L3. Returns linearized list of types. 
		 * @param clo
		 * @return
		 */
		public static List<ClassObject> resolveDiamonds(ClassObject clo) {
			List<ClassObject> ll = asListOfClasses(clo);
			List<ClassObject> linear = linearize(ll);
			Collections.reverse(linear);
			return linear;
		}

		private static List<ClassObject> linearize(List<ClassObject> ll) {
			List<ClassObject> merged = new ArrayList<ClassObject>();
			merged.add(ll.get(0));
			if (ll.size() == 1)
				return merged;
			List<List<ClassObject>> mergeList = new ArrayList<List<ClassObject>>();
			for (int i=1; i<ll.size(); i++)
				mergeList.add(linearize(asListOfClasses(ll.get(i))));
			for (ClassObject o : merge(mergeList, ll.subList(1, ll.size())))
				merged.add(o);
			return merged;
		}

		private static List<ClassObject> merge(List<List<ClassObject>> mergeList,
				List<ClassObject> subList) {
			mergeList.add(subList);
			
			List<ClassObject> m = new ArrayList<ClassObject>();
			while (true){
				List<ClassObject> suitable = null;
				
				outer:
				for (List<ClassObject> testee : mergeList){
					if (testee.size() == 0)
						continue;
					suitable = testee;
					ClassObject head = testee.get(0);
					for (List<ClassObject> tested : mergeList)
						if (testee != tested)
							if (tails(tested).contains(head)){
								suitable = null;
								continue outer;
							}
					if (testee != null)
						break;
				}
				if (suitable == null) {
					for (List<ClassObject> cllist : mergeList)
						if (cllist.size() != 0)
							throw new TypeError("unsuitable class hierarchy!");
					return m;
				}
				
				ClassObject head = suitable.get(0);
				m.add(head);
				for (List<ClassObject> cllist : mergeList)
					cllist.remove(head);
			}
		}

		private static List<ClassObject> tails(List<ClassObject> tested) {
			if (tested.size() == 0)
				return new ArrayList<ClassObject>();
			return tested.subList(1, tested.size());
		}

		private static List<ClassObject> asListOfClasses(ClassObject clo) {
			List<ClassObject> cl = new ArrayList<ClassObject>();
			cl.add(clo);
			for (PythonObject o : ((TupleObject) clo.getEditableFields().get(ClassObject.__BASES__).object).getObjects())
				cl.add((ClassObject) o);
			return cl;
		}
	}

	/**
	 * IO utilities
	 * @author Enerccio
	 *
	 */
	public static class IOUtils {
		private IOUtils(){}

		/**
		 * Converts InputStream into byte array
		 * @param input InputStream to grab the data from
		 * @return byte[] from the InputStream
		 * @throws IOException
		 */
		public static byte[] toByteArray(InputStream input) throws IOException {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
		    copy(input, output);
		    return output.toByteArray();
		}

		/**
		 * Copies input into output
		 * @param input is input
		 * @param output os output
		 * @return number of bytes copies
		 * @throws IOException
		 */
		public static long copy(InputStream input, OutputStream output) throws IOException {
			return copy(input, output, new byte[4096]);
		}

		/**
		 * Same as copy, but with variable sized buffer
		 * @param input is input
		 * @param output os output
		 * @param buffer buffer array
		 * @return number of bytes copies
		 * @throws IOException
		 */
		public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
			long count = 0L;
		    int n = 0;
		    while (-1 != (n = input.read(buffer))) {
		      output.write(buffer, 0, n);
		      count += n;
		    }
		    return count;
		}

		public static byte[] toByteArray(InputStream input, int c) throws IOException {
			if (c == 0)
				return new byte[0];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			long count = 0L;
		    int n = 0;
		    while (-1 != (n = input.read(buffer, 0, (int)(c-count)))) {
		      if (n == 0)
		    	  return bos.toByteArray();
		      bos.write(buffer, 0, n);
		      count += n;
		    }
			return bos.toByteArray();
		}
	}
	
	/**
	 * ParserGenerator generates various parse trees from three grammars (python, format, formatter)
	 * @author Enerccio
	 *
	 */
	public static class ParserGenerator {
		private ParserGenerator(){}
		
		public static class ThrowingErrorListener extends BaseErrorListener {
			private String source;

			public ThrowingErrorListener(String loc) {
				this.source = loc;
			}

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer,
					Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e)
					throws ParseCancellationException {
				throw new ParseCancellationException("file " + source + " line "
						+ line + ":" + charPositionInLine + " " + msg);
			}
		}

		/**
		 * Parses module provider into pythonParser
		 * @param provider
		 * @return
		 * @throws IOException 
		 * @throws Exception
		 */
		public static pythonParser parse(ModuleProvider provider) throws IOException{
			ANTLRInputStream is = new ANTLRInputStream(provider.getSource());
			pythonLexer lexer = new pythonLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener(provider.getSrcFile()));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			pythonParser parser = new pythonParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener(provider.getSrcFile()));
			return parser;
		}
		
		/**
		 * parses input stream into pythonParser
		 * @param provider
		 * @param srcFile
		 * @return
		 * @throws IOException 
		 * @throws Exception
		 */
		public static pythonParser parse(InputStream provider, String srcFile) throws IOException{
			ANTLRInputStream is = new ANTLRInputStream(provider);
			pythonLexer lexer = new pythonLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener(srcFile));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			pythonParser parser = new pythonParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener(srcFile));
			return parser;
		}

		/**
		 * Parser for format
		 * @param value format expression
		 * @return parse tree
		 */
		public static formatParser parseFormat(String value) {
			ANTLRInputStream is = new ANTLRInputStream(value);
			formatLexer lexer = new formatLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener("<input string>"));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			formatParser parser = new formatParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener("<input string>"));
			return parser;
		}

		/**
		 * Parser for formatter
		 * @param value formatter expression
		 * @return parse tree
		 */
		public static formatterParser parseFormatter(String value){
			ANTLRInputStream is = new ANTLRInputStream(value);
			formatterLexer lexer = new formatterLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener("<input string>"));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			formatterParser parser = new formatterParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener("<input string>"));
			return parser;
		}

		/**
		 * Parses string output, used in function()
		 * @param src source
		 * @return parse tree
		 */
		public static pythonParser parseStringInput(String src) {
			ANTLRInputStream is = new ANTLRInputStream(src);
			pythonLexer lexer = new pythonLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener("<generated>"));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			pythonParser parser = new pythonParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener("<generated>"));
			return parser;
		}

		/**
		 * Prepares the parser for compile function
		 * @param src text source
		 * @param value name of the file
		 * @return parse tree
		 */
		public static pythonParser parseCompileFunction(String src, String value) {
			ANTLRInputStream is = new ANTLRInputStream(src);
			pythonLexer lexer = new pythonLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener(value));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			pythonParser parser = new pythonParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener(value));
			return parser;
		}

		/**
		 * Prepares parser for eval input
		 * @param code input
		 * @return parse tree
		 */
		public static pythonParser parseEval(String code) {
			ANTLRInputStream is = new ANTLRInputStream(code);
			pythonLexer lexer = new pythonLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener("<eval>"));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			pythonParser parser = new pythonParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener("<eval>"));
			return parser;
		}
		
	}
}
