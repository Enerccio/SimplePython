#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
#include "sp_def.h"
#include "me_enerccio_sp_interpret_NativePythonInterpreter.h"


#define CALL_SUPER_RETURN(NAME, SIGNATURE, JNI_METHOD_NAME, ...) \
do { \
	jclass objcls = (*env)->GetObjectClass(env, self); \
	jclass super = (*env)->GetSuperclass(env, objcls); \
	jmethodID methid = (*env)->GetMethodID(env, super, NAME, SIGNATURE); \
	return (*env)->JNI_METHOD_NAME(env, self, super, methid, __VA_ARGS__); \
} while (0)

#define CALL_SUPER_NORETURN(NAME, SIGNATURE, ...) \
do { \
	jclass objcls = (*env)->GetObjectClass(env, self); \
	jclass super = (*env)->GetSuperclass(env, objcls); \
	jmethodID methid = (*env)->GetMethodID(env, super, NAME, SIGNATURE); \
	(*env)->CallNonvirtualVoidMethod(env, self, super, methid, __VA_ARGS__); \
} while (0)

typedef struct {
	jclass    opcode_class;
	jfieldID  opcode_field;
	jclass    frame_class;
	jfieldID  pc_id;
	jclass    exec_res_class;
	jclass    self_class;
	jfieldID  exec_EOF;
	jfieldID  exec_OK;
	jclass    helper_class;
	jfieldID  helper_field;
	jmethodID math_operation;
	jclass    interpreter_error_class;
} native_pi_data_t;

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_load0
(JNIEnv* env, jobject self)
{
	native_pi_data_t* nd = malloc(sizeof(native_pi_data_t));

	jclass opcode_class = (*env)->FindClass(env, "Lme/enerccio/sp/compiler/Bytecode;");
	jfieldID opcode_field = (*env)->GetFieldID(env, opcode_class, "id", "I");

	jclass frame_class = (*env)->FindClass(env, "Lme/enerccio/sp/interpret/FrameObject;");
	jfieldID pc_id = (*env)->GetFieldID(env, frame_class, "pc", "I");

	jclass exec_res_class = (*env)->FindClass(env, "me/enerccio/sp/interpret/ExecutionResult");
	jfieldID exec_EOF = (*env)->GetStaticFieldID(env, exec_res_class, "EOF", "Lme/enerccio/sp/interpret/ExecutionResult;");
	jfieldID exec_OK = (*env)->GetStaticFieldID(env, exec_res_class, "OK", "Lme/enerccio/sp/interpret/ExecutionResult;");

	jclass self_class = (*env)->GetObjectClass(env, self);
	jfieldID helper_field = (*env)->GetFieldID(env, self_class, "mathHelper", "Lme/enerccio/sp/interpret/InterpreterMathExecutorHelper;");

	jclass helper_class = (*env)->FindClass(env, "me/enerccio/sp/interpret/InterpreterMathExecutorHelper");
	// public boolean mathOperation(AbstractPythonInterpreter interpreter, FrameObject o, Stack<PythonObject> stack, Bytecode opcode)
	jmethodID math_operation = (*env)->GetMethodID(env, helper_class, "mathOperation",
		"(Lme/enerccio/sp/interpret/AbstractPythonInterpreter;Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;Lme/enerccio/sp/compiler/Bytecode;)Z");

	jclass interpreter_error_class = (*env)->FindClass(env, "Lme/enerccio/sp/errors/InterpreterError;");

	nd->opcode_class = (jclass)(*env)->NewGlobalRef(env, (jobject)opcode_class);
	nd->opcode_field = opcode_field;
	nd->frame_class = (jclass)(*env)->NewGlobalRef(env, (jobject)frame_class);
	nd->pc_id = pc_id;
	nd->exec_res_class = (jclass)(*env)->NewGlobalRef(env, (jobject)exec_res_class);
	nd->exec_EOF = exec_EOF;
	nd->self_class = (jclass)(*env)->NewGlobalRef(env, (jobject)self_class);
	nd->exec_OK = exec_OK;
	nd->helper_field = helper_field;
	nd->helper_class = (jclass)(*env)->NewGlobalRef(env, (jobject)helper_class);
	nd->math_operation = math_operation;
	nd->interpreter_error_class = (jclass)(*env)->NewGlobalRef(env, (jobject)interpreter_error_class);

	jfieldID storage_long = (*env)->GetFieldID(env, self_class, "__native_pi_data", "J");
	(*env)->SetLongField(env, self, storage_long, (long)((void*)nd));
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_finalize0
(JNIEnv* env, jobject self)
{
	jclass self_class = (*env)->GetObjectClass(env, self);
	jfieldID storage_long = (*env)->GetFieldID(env, self_class, "__native_pi_data", "J");
	native_pi_data_t* nd = (native_pi_data_t*)(*env)->GetLongField(env, self, storage_long);

	(*env)->DeleteGlobalRef(env, (jobject)nd->opcode_class);
	(*env)->DeleteGlobalRef(env, (jobject)nd->frame_class);
	(*env)->DeleteGlobalRef(env, (jobject)nd->exec_res_class);
	(*env)->DeleteGlobalRef(env, (jobject)nd->helper_class);
	(*env)->DeleteGlobalRef(env, (jobject)nd->interpreter_error_class);
	(*env)->DeleteGlobalRef(env, (jobject)nd->self_class);

	free(nd);
	(*env)->SetLongField(env, self, storage_long, (long)NULL);
}

JNIEXPORT jobject JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_doExecuteSingleInstruction
(JNIEnv* env, jobject self, jobject frame_object, jobject stack, jobject opcode)
{
	jclass self_class = (*env)->GetObjectClass(env, self);
	jfieldID storage_long = (*env)->GetFieldID(env, self_class, "__native_pi_data", "J");
	native_pi_data_t* nd = (native_pi_data_t*)(*env)->GetLongField(env, self, storage_long);
	
	int op_iv = (int)(*env)->GetIntField(env, opcode, nd->opcode_field);

	bytecode_t bytecode = (bytecode_t)op_iv;

	switch (bytecode)
	{
	case D_STARTFUNC:
	case D_RETURN: 
	{
					 (*env)->SetIntField(env, frame_object, nd->pc_id, (*env)->GetIntField(env, frame_object, nd->pc_id) + 4);
	} break;
	case NOP: break; 

	case PUSH_ENVIRONMENT: Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushEnvironment(env, self, frame_object, stack); break;
	case RESOLVE_CLOSURE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_resolveClosure(env, self, frame_object, stack); break;
	case PUSH_LOCAL_CONTEXT: Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushLocalContext(env, self, frame_object, stack); break;
	case IMPORT: Java_me_enerccio_sp_interpret_NativePythonInterpreter_importOperation(env, self, frame_object, stack); break;
	case RESOLVE_ARGS: Java_me_enerccio_sp_interpret_NativePythonInterpreter_resolveArgs(env, self, frame_object, stack); break;
	case PUSH_FRAME: Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushFrame(env, self, frame_object, stack); break;
	case PUSH_EXCEPTION:Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushException(env, self, frame_object, stack); break;
	case OPEN_LOCALS:Java_me_enerccio_sp_interpret_NativePythonInterpreter_openLocals(env, self, frame_object, stack); break;
	case PUSH_LOCALS: Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushLocals(env, self, frame_object, stack); break;
	case POP: Java_me_enerccio_sp_interpret_NativePythonInterpreter_pop(env, self, frame_object, stack); break;
	case PUSH: Java_me_enerccio_sp_interpret_NativePythonInterpreter_push(env, self, frame_object, stack); break;
	case CALL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_call(env, self, frame_object, stack); break;
	case KCALL:
	case RCALL:
		Java_me_enerccio_sp_interpret_NativePythonInterpreter_krcall(env, self, opcode, frame_object, stack);
		break; 
	case GET_ITER:
	{
					if (Java_me_enerccio_sp_interpret_NativePythonInterpreter_getIter(env, self, frame_object, stack) == JNI_TRUE)
						break;
	}
	case ECALL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_ecall(env, self, frame_object, stack); break;

	case DUP: Java_me_enerccio_sp_interpret_NativePythonInterpreter_dup(env, self, frame_object, stack); break;
	case SWAP_STACK: Java_me_enerccio_sp_interpret_NativePythonInterpreter_swapStack(env, self, frame_object, stack); break;
	case JUMPIFTRUE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfTrue(env, self, frame_object, stack); break;
	case JUMPIFFALSE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfFalse(env, self, frame_object, stack); break;
	case JUMPIFNONE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfNone(env, self, frame_object, stack); break;
	case JUMPIFNORETURN: Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfNoReturn(env, self, frame_object, stack); break;
	case GOTO: Java_me_enerccio_sp_interpret_NativePythonInterpreter_gotoOperation(env, self, frame_object, stack); break;
	case RETURN:
	{
	   Java_me_enerccio_sp_interpret_NativePythonInterpreter_returnOperation(env, self, frame_object, stack);

	   return (*env)->GetStaticObjectField(env, nd->exec_res_class, nd->exec_EOF);
	}
	case SAVE_LOCAL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveLocal(env, self, frame_object, stack); break;
	case TRUTH_VALUE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_truthValue(env, self, frame_object, stack); break;
	case MAKE_FIRST: Java_me_enerccio_sp_interpret_NativePythonInterpreter_makeFirst(env, self, frame_object, stack); break;
	case LOAD: Java_me_enerccio_sp_interpret_NativePythonInterpreter_load(env, self, frame_object, stack); break;
	case LOADGLOBAL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadGlobal(env, self, frame_object, stack); break;
	case SAVE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_save(env, self, frame_object, stack); break;
	case SAVEGLOBAL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveGlobal(env, self, frame_object, stack); break;
	case UNPACK_SEQUENCE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_unpackSequence(env, self, frame_object, stack); break;
	case LOADDYNAMIC: Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadDynamic(env, self, frame_object, stack); break;
	case SAVEDYNAMIC: Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveDynamic(env, self, frame_object, stack); break;
	case LOADBUILTIN: Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadBuiltin(env, self, frame_object, stack); break;
	case KWARG: Java_me_enerccio_sp_interpret_NativePythonInterpreter_kwarg(env, self, frame_object, stack); break;
	case UNPACK_KWARG: Java_me_enerccio_sp_interpret_NativePythonInterpreter_unpackKwargs(env, self, frame_object, stack); break;
	case MAKE_FUTURE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_makeFuture(env, self, frame_object, stack); break;
	case TEST_FUTURE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_testFuture(env, self, frame_object, stack); break;
	case LOAD_FUTURE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadFuture(env, self, frame_object, stack); break;
	case RAISE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_raise(env, self, frame_object, stack); break;
	case RERAISE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_reraise(env, self, frame_object, stack); break;
	case GETATTR: Java_me_enerccio_sp_interpret_NativePythonInterpreter_getAttr(env, self, frame_object, stack); break;
	case SETATTR: Java_me_enerccio_sp_interpret_NativePythonInterpreter_setAttr(env, self, frame_object, stack); break;
	case ISINSTANCE: Java_me_enerccio_sp_interpret_NativePythonInterpreter_isinstance(env, self, frame_object, stack); break;
	case YIELD: return Java_me_enerccio_sp_interpret_NativePythonInterpreter_yield(env, self, frame_object, stack); 
	case DEL: Java_me_enerccio_sp_interpret_NativePythonInterpreter_del(env, self, frame_object, stack); break;
	case DELATTR: Java_me_enerccio_sp_interpret_NativePythonInterpreter_delAttr(env, self, frame_object, stack); break;
	case SETUP_LOOP: Java_me_enerccio_sp_interpret_NativePythonInterpreter_setupLoop(env, self, frame_object, stack); break;
	case ACCEPT_ITER: Java_me_enerccio_sp_interpret_NativePythonInterpreter_acceptIter(env, self, frame_object, stack); break;
	case ADD:
	case SUB:
	case MUL:
	case DIV:
	case MOD:
	case AND:
	case OR:
	case XOR:
	case POW:
	case RSHIFT:
	case LSHIFT:
	case LT:
	case LE:
	case GE:
	case GT:
	case EQ:
	case NE:
	case DCOLON:
	case QM:
	case RARROW:
	default:
	{
			   /*
			   if (!mathHelper.mathOperation(this, o, stack, opcode))
			   throw new InterpreterError("unhandled bytecode "
			   + opcode.toString());
			   */
			   
			   jobject helper = (*env)->GetObjectField(env, self, nd->helper_field);
			   jboolean result = (*env)->CallNonvirtualBooleanMethod(env, helper, nd->helper_class, nd->math_operation, self, frame_object, stack, opcode);
			   if (result == JNI_FALSE)
			   {
				   
				   (*env)->ThrowNew(env, nd->interpreter_error_class, "unhandled bytecode in native interpreter");
				   return NULL;
			   }
	}
		break;
	};
	
	return (*env)->GetStaticObjectField(env, nd->exec_res_class, nd->exec_OK);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_resolveClosure
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"resolveClosure",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_openLocals
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"openLocals",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushLocals
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"pushLocals",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushEnvironment
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"pushEnvironment",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_call
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"call",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_setupLoop
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"setupLoop",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_acceptIter
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"acceptIter",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT jboolean JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_getIter
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_RETURN(
		"getIter",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)Z",
		CallNonvirtualBooleanMethod,
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_ecall
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"ecall",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_krcall
(JNIEnv* env, jobject self, jobject opcode, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"krcall",
		"(Lme/enerccio/sp/compiler/Bytecode;Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		opcode, frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_gotoOperation
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"gotoOperation",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfFalse
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"jumpIfFalse",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfTrue
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"jumpIfTrue",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfNone
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"jumpIfNone",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_jumpIfNoReturn
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"jumpIfNoReturn",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_makeFuture
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"makeFuture",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_load
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"load",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadFuture
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"loadFuture",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_testFuture
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"testFuture",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadGlobal
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"loadGlobal",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pop
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"pop",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_truthValue
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"truthValue",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_push
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"push",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_returnOperation
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"returnOperation",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_save
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"save",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_kwarg
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"kwarg",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveLocal
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"saveLocal",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveGlobal
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"saveGlobal",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_dup
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"dup",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_importOperation
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"importOperation",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_swapStack
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"swapStack",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_makeFirst
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"makeFirst",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_unpackSequence
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"unpackSequence",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_unpackKwargs
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"unpackKwargs",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushLocalContext
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"pushLocalContext",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_resolveArgs
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"resolveArgs",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_getAttr
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"getAttr",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_delAttr
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"delAttr",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_setAttr
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"setAttr",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_isinstance
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"isinstance",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_raise
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"raise",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_reraise
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"reraise",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushFrame
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"pushFrame",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_pushException
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{ 
	CALL_SUPER_NORETURN(
		"pushException",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT jobject JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_yield
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_RETURN(
		"yield",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)Lme/enerccio/sp/interpret/ExecutionResult;",
		CallNonvirtualObjectMethod,
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadDynamic
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"loadDynamic",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_saveDynamic
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"saveDynamic",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_loadBuiltin
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"loadBuiltin",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}

JNIEXPORT void JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_del
(JNIEnv* env, jobject self, jobject frame_object, jobject stack)
{
	CALL_SUPER_NORETURN(
		"del",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;)V",
		frame_object, stack
		);
}