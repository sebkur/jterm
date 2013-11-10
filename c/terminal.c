/* JTerm - a java terminal emulator
 *
 * Copyright (C) 2013  Sebastian Kuerten
 *
 * This file is part of JTerm.
 *
 * JTerm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTerm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VexTerm.  If not, see <http://www.gnu.org/licenses/>.
 */

#define BUFSIZE 10000
#define PREBUFFER 10

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

#include <glib.h>

extern char ** environ;

int main(int argc, char *argv[])
{
	char * command = "/bin/bash";

 	fprintf(stderr, "proc id: %d\n", getpid());

	int mfd = posix_openpt(O_RDWR | O_NOCTTY); //master
	fprintf(stderr, "master fd: %d\n", mfd);
	grantpt(mfd);
	unlockpt(mfd);
	int flags = fcntl(mfd, F_GETFL);
	flags &= ~(O_NONBLOCK);
	fcntl(mfd, F_SETFL, flags);
	char * pn = ptsname(mfd);
	fprintf(stderr, "ptsname: %s\n", pn);

	pid_t pid = vfork();
	if (pid == 0){
		close(mfd);
		setsid();
		setpgid(0, 0);
		int sfd = open(pn, O_RDWR);
		fprintf(stderr, "slave fd: %d\n", sfd);
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
			if (strcmp(var, "TERM") == 0){
			}else{
				//env[e2] = g_strdup(environ[e1]);
                env[e2] = malloc(sizeof(char) * strlen(environ[e1]));
                strncpy(env[e2], environ[e1], strlen(environ[e1]));
				e2++;
			}
			e1++;
		}
		//env[e2++] = g_strdup("TERM=xterm");
        const char * last = "TERM=xterm";
        env[e2] = malloc(sizeof(char) * strlen(last));
        strncpy(env[e2], last, strlen(last));
        e2++;
		env[e2] = NULL;

		/* working directory */
		// TODO: chdir(terminal -> initial_pwd);

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
		exit(0);
	}
	fprintf(stderr, "proc id: %d\n", pid);

	struct termios st;
	tcgetattr (mfd, &st);
	//st.c_lflag &= ~(ECHO);
	tcsetattr (mfd, TCSANOW, &st);

	struct winsize size;
	memset(&size, 0, sizeof(size));
//	size.ws_row = terminal -> n_rows;
//	size.ws_col = terminal -> n_cols;
//	ioctl(mfd, TIOCSWINSZ, &size);

	char buf[BUFSIZE + PREBUFFER];

	ssize_t c;
	while (1){
		c = read(mfd, &buf[PREBUFFER], BUFSIZE - 1);
		if (c <= 0) break;
		fflush(NULL);
		write(1, buf, c);
	}
}
