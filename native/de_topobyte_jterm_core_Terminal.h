/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_topobyte_jterm_core_Terminal */

#ifndef _Included_de_topobyte_jterm_core_Terminal
#define _Included_de_topobyte_jterm_core_Terminal
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    test
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_test
  (JNIEnv *, jobject);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    testStringCreation
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_topobyte_jterm_core_Terminal_testStringCreation
  (JNIEnv *, jobject);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    write
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_write
  (JNIEnv *, jobject, jstring);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_start
  (JNIEnv *, jobject);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    read
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_de_topobyte_jterm_core_Terminal_read
  (JNIEnv *, jobject);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    setSize
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_setSize
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    getEraseCharacter
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_de_topobyte_jterm_core_Terminal_getEraseCharacter
  (JNIEnv *, jobject);

/*
 * Class:     de_topobyte_jterm_core_Terminal
 * Method:    getPwd
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_topobyte_jterm_core_Terminal_getPwd
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
