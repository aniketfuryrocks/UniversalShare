package com.eadded.universalshare.Network;

import java.io.File;

public abstract class FileAuthEvent {
    public abstract void onFileAuth(File file, boolean toSave);
}