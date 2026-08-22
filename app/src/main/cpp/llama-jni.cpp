#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_homebax_axionis_LlamaEngine_getStatus(
        JNIEnv* env,
        jobject /* thiz */) {

    return env->NewStringUTF(
            "llama.cpp JNI funguje."
    );
}