#include <jni.h>
#include <stdio.h>
#include "de_topobyte_jterm_Terminal.h"
 
JNIEXPORT void JNICALL Java_de_topobyte_jterm_Terminal_test
  (JNIEnv * env, jobject this)
{
    printf("Hi! This is the JNI speaking\n");

    printf("My process ID : %d\n", getpid());

    return;
}

JNIEXPORT void JNICALL Java_de_topobyte_jterm_Terminal_write
  (JNIEnv * env, jobject this, jstring message)
{
    if (message == NULL) {
        printf("Message is null\n");
    } else {
        const char * msg = (*env)->GetStringUTFChars(env, message, NULL);
        printf("This is my message: '%s'\n", msg);
    }
}
