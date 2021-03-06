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
package me.enerccio.sp.compiler;

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.properties.FieldPropertyObject;
import me.enerccio.sp.utils.Utils;

/**
 * PythonBytecode is PythonObject representing single instruction of the
 * interpret.
 * 
 * @author Enerccio
 *
 */
public class PythonBytecode extends PythonObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -34522216612624880L;
	/* Debug information */
	public int debugLine = -1;
	public int debugCharacter;
	public String debugFunction;
	public ModuleData debugModule = null;
	public boolean newLine = false;

	/* fields used by various bytecodes */
	public int intValue;
	public PythonObject value;
	public String stringValue;
	public Object object;
	public boolean booleanValue;

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(debugLine);
		pySerializer.serialize(debugCharacter);
		pySerializer.serialize(debugFunction);
		pySerializer.serializeJava(debugModule);
		pySerializer.serialize(intValue);
		pySerializer.serialize(value);
		pySerializer.serialize(stringValue);
		pySerializer.serializeJava(object);
		pySerializer.serialize(booleanValue);
	}

	public PythonBytecode() {
		super(true);
	}

	@Override
	public String toString() {
		String str = super.toString();
		return str.replace("\n", "\\n").replace("\t", "\\t")
				.replace("\r", "\\r");
	}

	@Override
	public void newObject() {
		super.newObject();

		try {
			Utils.putPublic(this, "int_value", new FieldPropertyObject(this,
					PythonBytecode.class, "intValue", true));
			Utils.putPublic(this, "value", new FieldPropertyObject(this,
					PythonBytecode.class, "value", true));
			Utils.putPublic(this, "string_value", new FieldPropertyObject(this,
					PythonBytecode.class, "stringValue", true));
			Utils.putPublic(this, "object", new FieldPropertyObject(this,
					PythonBytecode.class, "object", true));
			Utils.putPublic(this, "bool_value", new FieldPropertyObject(this,
					PythonBytecode.class, "booleanValue", true));
			Utils.putPublic(this, "_lineno", new FieldPropertyObject(this,
					PythonBytecode.class, "debugLine", true));
			Utils.putPublic(this, "_charno", new FieldPropertyObject(this,
					PythonBytecode.class, "debugCharacter", true));
			Utils.putPublic(this, "_function", new FieldPropertyObject(this,
					PythonBytecode.class, "debugFunction", true));
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("yellow sky", e);
		}
	}

	protected Bytecode bytecode;

	/**
	 * Returns type of the bytecode
	 * 
	 * @return
	 */
	public Bytecode getOpcode() {
		return bytecode;
	}

	public static class Nop extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838919066014L;

		{
			bytecode = Bytecode.NOP;
		}
	}

	public static class Debug extends PythonBytecode {

		public Debug(Bytecode b) {
			bytecode = b;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 438692545119066014L;
	}

	public static class MakeFirst extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4386921119191660149L;

		{
			bytecode = Bytecode.MAKE_FIRST;
		}
	}

	public static class TestFuture extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4386928389191660149L;

		{
			bytecode = Bytecode.TEST_FUTURE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}

	}

	public static class BinaryOperator extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838991066014L;

		public BinaryOperator(Bytecode b) {
			bytecode = b;
		}
	}

	public static class Del extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 538692838919066014L;

		{
			bytecode = Bytecode.DEL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s) - %s", getOpcode().toString(),
					stringValue, booleanValue);
		}
	}

	public static class DelAttr extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 638692838919066014L;

		{
			bytecode = Bytecode.DELATTR;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class ResolveClosure extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838129066014L;

		{
			bytecode = Bytecode.RESOLVE_CLOSURE;
		}
	}

	public static class LoadBuiltin extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4386928314919066014L;

		{
			bytecode = Bytecode.LOADBUILTIN;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class OpenLocals extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692848919066124L;

		{
			bytecode = Bytecode.OPEN_LOCALS;
		}
	}

	public static class PushLocals extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692848919066015L;

		{
			bytecode = Bytecode.PUSH_LOCALS;
		}
	}

	public static class Yield extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692848919066014L;

		{
			bytecode = Bytecode.YIELD;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class SaveLocal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838919066015L;

		{
			bytecode = Bytecode.SAVE_LOCAL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class Raise extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3446042551581498953L;

		{
			bytecode = Bytecode.RAISE;
		}

		@Override
		protected String doToString() {
			return String.format("%s", getOpcode().toString());
		}
	}

	public static class Reraise extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6312861151510715111L;

		{
			bytecode = Bytecode.RERAISE;
		}
	}

	public static class TruthValue extends PythonBytecode {
		private static final long serialVersionUID = -8816781867691872127L;

		{
			bytecode = Bytecode.TRUTH_VALUE;
		}

		@Override
		protected String doToString() {
			if (intValue == 1)
				return String
						.format("%s (1 - negated)", getOpcode().toString());
			else
				return String.format("%s", getOpcode().toString(), intValue);
		}
	}

	public static class AcceptIter extends PythonBytecode {
		private static final long serialVersionUID = 6332909908617239072L;

		{
			bytecode = Bytecode.ACCEPT_ITER;
		}

		@Override
		protected String doToString() {
			return String.format("%s (or jump to %s)", getOpcode().toString(),
					intValue);
		}
	}

	public static class GetIter extends PythonBytecode {
		private static final long serialVersionUID = 1894153981745123347L;

		{
			bytecode = Bytecode.GET_ITER;
		}

		@Override
		protected String doToString() {
			return String.format("%s (or jump to %s)", getOpcode().toString(),
					intValue);
		}
	}

	public static class SetupLoop extends PythonBytecode {
		private static final long serialVersionUID = 7845189234841211899L;

		{
			bytecode = Bytecode.SETUP_LOOP;
		}

		@Override
		protected String doToString() {
			return String.format("%s (jump to %s with javaiterator)",
					getOpcode().toString(), intValue);
		}
	}

	public static class PushEnvironment extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3569826021524513052L;

		{
			bytecode = Bytecode.PUSH_ENVIRONMENT;
		}
	}

	public static class PushLocalContext extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1088681858920031098L;

		{
			bytecode = Bytecode.PUSH_LOCAL_CONTEXT;
		}
	}

	public static class Return extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7601997081632281638L;

		{
			bytecode = Bytecode.RETURN;
		}

		@Override
		protected String doToString() {
			if (intValue == 1)
				return String.format("%s (%s - returns value)", getOpcode()
						.toString(), intValue);
			return String.format("%s (%s - exits frame)", getOpcode()
					.toString(), intValue);
		}

	}

	public static class Pop extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -211343001041855798L;

		{
			bytecode = Bytecode.POP;
		}
	}

	public static class Push extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2322622705042387021L;

		{
			bytecode = Bytecode.PUSH;
		}

		@Override
		protected String doToString() {
			try {
				return String.format("%s(%s)", getOpcode().toString(),
						value.toString());
			} catch (Exception e) {
				return String.format("%s(%s)", getOpcode().toString(), e);
			}
		}
	}

	public static class Call extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3080128534823197157L;

		{
			bytecode = Bytecode.CALL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class RCall extends PythonBytecode {
		private static final long serialVersionUID = 9058117934717120328L;

		{
			bytecode = Bytecode.RCALL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class KCall extends PythonBytecode {
		private static final long serialVersionUID = 9058117934717120328L;

		{
			bytecode = Bytecode.KCALL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class ECall extends PythonBytecode {
		private static final long serialVersionUID = 9058117934717120328L;

		{
			bytecode = Bytecode.ECALL;
		}
	}

	public static class Goto extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1236413271543232839L;

		{
			bytecode = Bytecode.GOTO;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class JumpIfTrue extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 941692152211238078L;

		{
			bytecode = Bytecode.JUMPIFTRUE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class JumpIfNone extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2507334203455848750L;

		{
			bytecode = Bytecode.JUMPIFNONE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class IsInstance extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2053918345821186800L;

		{
			bytecode = Bytecode.ISINSTANCE;
		}
	}

	public static class JumpIfNoReturn extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5203909925032492085L;

		{
			bytecode = Bytecode.JUMPIFNORETURN;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class JumpIfFalse extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2622975371669812361L;

		{
			bytecode = Bytecode.JUMPIFFALSE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class Load extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1903853394223900780L;

		{
			bytecode = Bytecode.LOAD;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}

	}

	public static class LoadGlobal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1567806659588614058L;

		{
			bytecode = Bytecode.LOADGLOBAL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class LoadDynamic extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1803853394223900780L;

		{
			bytecode = Bytecode.LOADDYNAMIC;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}

	}

	public static class Save extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -406468849835914035L;

		{
			bytecode = Bytecode.SAVE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class SaveGlobal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1109972611003866956L;

		{
			bytecode = Bytecode.SAVEGLOBAL;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class SaveDynamic extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -416468849835914035L;

		{
			bytecode = Bytecode.SAVEDYNAMIC;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class Dup extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8214786407927555266L;

		{
			bytecode = Bytecode.DUP;
		}

		@Override
		protected String doToString() {
			if (intValue == 0)
				return String.format("%s", getOpcode().toString());
			else
				return String.format("%s (copies %s-th from top)", getOpcode()
						.toString(), intValue);
		}

	}

	public static class PushFrame extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8927402167434919757L;

		{
			bytecode = Bytecode.PUSH_FRAME;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class PushException extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8296967861579722494L;

		{
			bytecode = Bytecode.PUSH_EXCEPTION;
		}
	}

	public static class Import extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2882854084616515036L;

		{
			bytecode = Bytecode.IMPORT;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s, %s)", getOpcode().toString(), object,
					stringValue);
		}

	}

	public static class SwapStack extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7862981632281325840L;

		{
			bytecode = Bytecode.SWAP_STACK;
		}
	}

	public static class KwArg extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7862981632281325840L;

		{
			bytecode = Bytecode.KWARG;
		}

		@Override
		protected String doToString() {
			if (object == null)
				return String.format("%s(%s)", getOpcode().toString(), null);
			StringBuilder sb = new StringBuilder();
			for (String s : (String[]) object) {
				sb.append(s);
				sb.append(", ");
			}
			if (sb.length() > 3)
				sb.delete(sb.length() - 2, sb.length());
			return String.format("%s(%s)", getOpcode().toString(), sb);
		}
	}

	public static class LoadFuture extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5415324154611824119L;

		{
			bytecode = Bytecode.LOAD_FUTURE;
		}
	}

	public static class MakeFuture extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7862981632281325840L;

		{
			bytecode = Bytecode.MAKE_FUTURE;
		}

		@Override
		protected String doToString() {
			if (object == null)
				return String.format("%s(%s)", getOpcode().toString(), null);
			StringBuilder sb = new StringBuilder();
			for (String s : (String[]) object) {
				sb.append(s);
				sb.append(", ");
			}
			if (sb.length() > 3)
				sb.delete(sb.length() - 2, sb.length());
			return String.format("%s(%s)", getOpcode().toString(), sb);
		}
	}

	public static class UnpackKwArg extends PythonBytecode {
		private static final long serialVersionUID = -985151216623131210L;

		{
			bytecode = Bytecode.UNPACK_KWARG;
		}

	}

	public static class UnpackSequence extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9049013845798886791L;

		{
			bytecode = Bytecode.UNPACK_SEQUENCE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class ResolveArgs extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1200159557331063544L;

		{
			bytecode = Bytecode.RESOLVE_ARGS;
		}
	}

	public static class GetAttr extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9210115051088548884L;

		{
			bytecode = Bytecode.GETATTR;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class SetAttr extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -127124418723016892L;

		{
			bytecode = Bytecode.SETATTR;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return getOpcode().toString();
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	@Override
	public byte getTag() {
		return Tags.PYTHON_BYTECODE;
	}
}
