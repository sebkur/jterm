#include <jni.h>
#include "de_topobyte_jterm_Terminal.h"

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
        fflush(stdout);

        jclass thisClass = (*env)->GetObjectClass(env, this);
        jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
        jint mfd = (*env)->GetIntField(env, this, fidMfd);
        printf("write(%d, '%s', %d)\n", mfd, msg, (int)strlen(msg));
        write(mfd, msg, strlen(msg));

        (*env)->ReleaseStringUTFChars(env, message, msg);
    }
}

JNIEXPORT jstring JNICALL Java_de_topobyte_jterm_Terminal_testStringCreation
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
    jint mfd = (*env)->GetIntField(env, this, fidMfd);
    mfd++;
    (*env)->SetIntField(env, this, fidMfd, mfd);

    const char * test = "c string in return %d";
    char * out = malloc(sizeof(char) * (strlen(test) + 20));
    sprintf(out, test, mfd);
    return (*env)->NewStringUTF(env, out);
}

extern char ** environ;

JNIEXPORT void JNICALL Java_de_topobyte_jterm_Terminal_start
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
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
        char ** env = malloc(sizeof(char*) * (ec + 2));
        int e1 = 0, e2 = 0;
        for (iter = environ; *iter != NULL; iter++){
            char * var = *iter;
            printf("var: %s\n", var);
            if (strcmp(var, "TERM") == 0){
            }else{
                //env[e2] = g_strdup(environ[e1]);
                env[e2] = malloc(sizeof(char) * strlen(environ[e1]) + 1);
                //strncpy(env[e2], environ[e1], strlen(environ[e1]));
                strcpy(env[e2], environ[e1]);
                e2++;
            }
            e1++;
        }
        //env[e2++] = g_strdup("TERM=xterm");
        const char * last = "TERM=xterm";
        env[e2] = malloc(sizeof(char) * strlen(last) + 1);
        //strncpy(env[e2], last, strlen(last));
        strcpy(env[e2], last);
        e2++;
        env[e2] = NULL;

        /* working directory */
        // TODO: chdir(terminal -> initial_pwd);

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
        execve(command, argv, env);
    }
    fprintf(stderr, "proc id: %d\n", pid);

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

JNIEXPORT jbyteArray JNICALL Java_de_topobyte_jterm_Terminal_read
  (JNIEnv * env, jobject this)
{
    jclass thisClass = (*env)->GetObjectClass(env, this);
    jfieldID fidMfd = (*env)->GetFieldID(env, thisClass, "mfd", "I");
    jint mfd = (*env)->GetIntField(env, this, fidMfd);

    char buf[BUFSIZE];
    ssize_t c;
    c = read(mfd, buf, BUFSIZE - 1);

    fprintf(stderr, "read: %d\n", (int)c);

    if (c <= 0) {
        return NULL;
    }

    jbyteArray array = (*env)->NewByteArray(env, c);
    (*env)->SetByteArrayRegion(env, array, 0, c, buf);
    return array;
}
