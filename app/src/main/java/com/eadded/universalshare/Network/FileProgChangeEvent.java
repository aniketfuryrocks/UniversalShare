package com.eadded.universalshare.Network;

public abstract class FileProgChangeEvent {
    public abstract void fileProgressChanged(int to);

    public abstract void fileProgressCancel();
}
