package com.eadded.universalshare.Network;

public class FileAuthNoti {
    public final String name;
    public final FileAuthEvent fileAuthEvent;
    public final String user;

    public FileAuthNoti(String name, String user, FileAuthEvent fileAuthEvent) {
        this.name = name;
        this.fileAuthEvent = fileAuthEvent;
        this.user = user;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return ((FileAuthNoti) obj).name.equals(name) && ((FileAuthNoti) obj).user.equals(user);
    }
}