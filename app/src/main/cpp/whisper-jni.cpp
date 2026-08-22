#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_homebax_axionis_WhisperEngine_getStatus(
        JNIEnv* env,
        jobject /* thiz */) {

    return env->NewStringUTF(
            "whisper.cpp JNI funguje."
    );
}