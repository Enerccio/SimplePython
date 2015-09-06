#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
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

JNIEXPORT jobject JNICALL Java_me_enerccio_sp_interpret_NativePythonInterpreter_doExecuteSingleInstruction
(JNIEnv* env, jobject self, jobject frame_object, jobject stack, jobject opcode)
{
	CALL_SUPER_RETURN(
		"doExecuteSingleInstruction",
		"(Lme/enerccio/sp/interpret/FrameObject;Ljava/util/Stack;Lme/enerccio/sp/compiler/Bytecode;)Lme/enerccio/sp/interpret/ExecutionResult;",
		CallNonvirtualObjectMethod,
		frame_object, stack, opcode
		);
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