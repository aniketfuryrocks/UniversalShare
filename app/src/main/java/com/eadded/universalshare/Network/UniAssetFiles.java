package com.eadded.universalshare.Network;

public enum UniAssetFiles {
    downloadHTML("UniAssets/downloads.html"),
    indexHTML("UniAssets/index.html"),
    blockedHTML("UniAssets/blocked.html"),
    deniedHTML("UniAssets/denied.html"),
    pendingHTML("UniAssets/pending.html"),
    wengtWrongHTML("UniAssets/sww.html"),
    uploadHTML("UniAssets/upload.html");
    private final String file;

    UniAssetFiles(String s) {
        this.file = s;
    }

    public String getFile() {
        return file;
    }
}