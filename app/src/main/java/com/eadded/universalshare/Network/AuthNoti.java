package com.eadded.universalshare.Network;

public class AuthNoti {
    public final String name;
    public final String sess;
    public final AuthEvent authEvent;

    public AuthNoti(String name, String sess, AuthEvent authEvent) {
        this.name = name;
        this.sess = sess;
        this.authEvent = authEvent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return ((AuthNoti) obj).sess.equals(sess);
    }
}