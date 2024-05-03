package com.dialoguebranch.script.model;

import java.io.File;

public class FileStorageSource extends StorageSource {

    File sourceFile;

    public FileStorageSource(File sourceFile) {
        super();
        this.sourceFile = sourceFile;
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    @Override
    public String getDescriptor() {
        return this.sourceFile.getAbsolutePath();
    }
}
