package com.eadded.universalshare.Network;

import com.eadded.universalshare.CommonLib.CustFile;

public class FileProg {

    public final int totalProg;
    public final CustFile custFile;
    public final String user;
    public int prog = 0;
    public boolean workLoop = true;
    private FileProgChangeEvent fileProgChangeEvent;

    public FileProg(CustFile file, String user, int totalProg) {
        this.custFile = file;
        this.totalProg = totalProg;
        this.user = user;
    }

    public void fileProgChanged(int to) {
        prog = to;
        if (fileProgChangeEvent != null)
            fileProgChangeEvent.fileProgressChanged(to);
    }

    public void setOnFileProgChange(FileProgChangeEvent fileProgChangeEvent) {
        this.fileProgChangeEvent = fileProgChangeEvent;
    }

    @Override
    public boolean equals(Object obj) {
        return ((FileProg) obj).custFile.file.equals(custFile.file) && workLoop && ((FileProg) obj).user.equals(user);
    }

    public void fileProgCancel() {
        workLoop = false;
        fileProgChangeEvent.fileProgressCancel();
    }
}