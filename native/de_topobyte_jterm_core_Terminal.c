#include <jni.h>
#include "de_topobyte_jterm_core_Terminal.h"

#define BUFSIZE 10000

#define _XOPEN_SOURCE 600
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <signal.h>
#include <pthread.h>

JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_write
  (JNIEnv * env, jobject this, jstring message)
{
    if (message == NULL) {
        printf("Message is null\n");
    } else {
        const char * msg = (*env)->GetStringUTFChars(env, message, NULL);
        //printf("This is my message: '%s'\n", msg);
        //fflush(stdout);

        jclass thisClass = (*env)->GetObjectClass(env, this);
        jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
        jint mfd = (*env)->GetIntField(env, this, fidMfd);
        //printf("write(%d, '%s', %d)\n", mfd, msg, (int)strlen(msg));
        write(mfd, msg, strlen(msg));

        (*env)->ReleaseStringUTFChars(env, message, msg);
    }
}

extern char ** environ;

JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_start
  (JNIEnv * env, jobject this, jstring jpwd)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidPid = (*env)->GetFieldID(env, thisClass, "pid", "I");
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");

    // Start virtual terminal
    char * command = "/bin/bash";

    int mfd = posix_openpt(O_RDWR | O_NOCTTY); //master
    fprintf(stderr, "master fd: %d\n", mfd);
    (*env)->SetIntField(env, this, fidMfd, mfd);
    if (grantpt(mfd) != 0) {
        fprintf(stderr, "error with grantpt\n");
    }
    if (unlockpt(mfd) != 0) {
        fprintf(stderr, "error with grantpt\n");
    }
    int flags = fcntl(mfd, F_GETFL);
    flags &= ~(O_NONBLOCK);
    if (fcntl(mfd, F_SETFL, flags) != 0) {
        fprintf(stderr, "error with fcntl\n");
    }
    // This one fails from JNI, don't know why...
    // char * pn = ptsname(mfd);
    // So we use this one:
    size_t pn_size = sizeof(char) * 100;
    char * pn = malloc(pn_size);
    if (ptsname_r(mfd, pn, pn_size) != 0) {
        fprintf(stderr, "error with ptsname\n");
    }
    fprintf(stderr, "ptsname: %s\n", pn);

    pid_t pid = vfork();
    if (pid == 0){
        close(mfd);
        setsid();
        setpgid(0, 0);
        int sfd = open(pn, O_RDWR);
        ioctl (sfd, TIOCSCTTY, 0);

        struct termios st;
        tcgetattr (sfd, &st);
        st.c_iflag &= ~(ISTRIP | IGNCR | INLCR | IXOFF);
        st.c_iflag |= (ICRNL | IGNPAR | BRKINT | IXON);
        st.c_cflag &= ~CSIZE;
        st.c_cflag |= CREAD | CS8;
        tcsetattr (sfd, TCSANOW, &st);

        /* environment */
        char ** iter; int ec = 0;
        for (iter = environ; *iter != NULL; iter++){
            ec++;
        }
        char ** envi = malloc(sizeof(char*) * (ec + 2));
        int e1 = 0, e2 = 0;
        for (iter = environ; *iter != NULL; iter++){
            char * var = *iter;
            //printf("var: %s\n", var);
            if (strcmp(var, "TERM") == 0){
            }else{
                //envi[e2] = g_strdup(environ[e1]);
                envi[e2] = malloc(sizeof(char) * strlen(environ[e1]) + 1);
                //strncpy(envi[e2], environ[e1], strlen(environ[e1]));
                strcpy(envi[e2], environ[e1]);
                e2++;
            }
            e1++;
        }
        //envi[e2++] = g_strdup("TERM=xterm");
        const char * last = "TERM=xterm";
        envi[e2] = malloc(sizeof(char) * strlen(last) + 1);
        //strncpy(envi[e2], last, strlen(last));
        strcpy(envi[e2], last);
        e2++;
        envi[e2] = NULL;

        /* working directory */
        if (jpwd != NULL) {
            const char * pwd = (*env)->GetStringUTFChars(env, jpwd, NULL);
            chdir(pwd);
            (*env)->ReleaseStringUTFChars(env, jpwd, pwd);
        }

        fprintf(stderr, "proc id: %d\n", pid);

        fprintf(stderr, "slave fd: %d\n", sfd);
        dup2(sfd, 0);
        dup2(sfd, 1);
        dup2(sfd, 2);
        if (sfd > 2) close(sfd);

        int argc = 1;
        char ** argv = malloc(sizeof(char*) * (argc+2));
        argv[0] = command;
        argv[argc] = NULL;
        execve(command, argv, envi);
    }
    fprintf(stderr, "proc id: %d\n", pid);
    (*env)->SetIntField(env, this, fidPid, pid);

    struct termios st;
    tcgetattr (mfd, &st);
    //st.c_lflag &= ~(ECHO);
    tcsetattr (mfd, TCSANOW, &st);

//    struct winsize size;
//    memset(&size, 0, sizeof(size));
//    size.ws_row = terminal -> n_rows;
//    size.ws_col = terminal -> n_cols;
//    ioctl(mfd, TIOCSWINSZ, &size);

//    char buf[BUFSIZE + PREBUFFER];
//    ssize_t c;
//    while (1){
//        c = read(mfd, &buf[PREBUFFER], BUFSIZE - 1);
//        if (c <= 0) break;
//        fflush(NULL);
//        write(1, buf, c);
//    }
}

JNIEXPORT jbyteArray JNICALL Java_de_topobyte_jterm_core_Terminal_read
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
    jint mfd = (*env)->GetIntField(env, this, fidMfd);

    char buf[BUFSIZE];
    ssize_t c;
    c = read(mfd, buf, BUFSIZE - 1);

    //fprintf(stderr, "read: %d\n", (int)c);

    if (c <= 0) {
        return NULL;
    }

    jbyteArray array = (*env)->NewByteArray(env, c);
    (*env)->SetByteArrayRegion(env, array, 0, c, buf);
    return array;
}

JNIEXPORT void JNICALL Java_de_topobyte_jterm_core_Terminal_setSize
  (JNIEnv * env, jobject this, jint width, jint height)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
    jint mfd = (*env)->GetIntField(env, this, fidMfd);

    struct winsize size;
    memset(&size, 0, sizeof(size));
    size.ws_row = height;
    size.ws_col = width;
    ioctl(mfd, TIOCSWINSZ, &size);
}

JNIEXPORT jbyte JNICALL Java_de_topobyte_jterm_core_Terminal_getEraseCharacter
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
    jint mfd = (*env)->GetIntField(env, this, fidMfd);

    struct termios tio;
    tcgetattr(mfd, &tio);
    cc_t c = tio.c_cc[VERASE];
    return c;
}

char * process_get_pwd(pid_t pid)
{
    char * file = malloc(sizeof(char) * 100);
    sprintf(file,"/proc/%d/cwd", pid);

    size_t s = 128;
    char * buf;
    while (1){
        buf = malloc(s + 1);
        size_t r = readlink(file, buf, s);
        if (r < 0){
            free(buf); buf = NULL; break;
        }
        if (r == s){
            free(buf);
            s *= 2;
            if (s > 1024 * 4){
                free(buf); buf = NULL; break;
            }
            continue;
        }
        buf[r] = '\0';
        break;
    }
    free(file);
    return buf;
}

JNIEXPORT jstring JNICALL Java_de_topobyte_jterm_core_Terminal_getPwd
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidPid = (*env)->GetFieldID(env, thisClass, "pid", "I");
    jint pid = (*env)->GetIntField(env, this, fidPid);

    char * pwd = process_get_pwd(pid);
    jstring ret = (*env)->NewStringUTF(env, pwd);
    free(pwd);
    return ret;
}
