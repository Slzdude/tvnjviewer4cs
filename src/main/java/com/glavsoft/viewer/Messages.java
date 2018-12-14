package com.glavsoft.viewer;

public class Messages {
    public Messages() {
    }

    public static final void print_error(String message) {
        System.out.println("\u001b[01;31m[-]\u001b[0m " + message);
    }

    public static final void print_good(String message) {
        message = "[VNC] " + message;
        System.out.println("\u001b[01;32m[+]\u001b[0m " + message);
    }

    public static final void print_info(String message) {
        message = "[VNC] " + message;
        System.out.println("\u001b[01;34m[*]\u001b[0m " + message);
    }

    public static final void print_warn(String message) {
        message = "[VNC] " + message;
        System.out.println("\u001b[01;33m[!]\u001b[0m " + message);
    }

    public static final void print_stat(String message) {
        message = "[VNC] " + message;
        System.out.println("\u001b[01;35m[*]\u001b[0m " + message);
    }
}
