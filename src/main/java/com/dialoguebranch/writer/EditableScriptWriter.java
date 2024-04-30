package com.dialoguebranch.writer;

import com.dialoguebranch.model.Constants;
import com.dialoguebranch.script.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EditableScriptWriter {

    public static void write(EditableScript editableScript) throws IOException {

        StorageSource storageSource = editableScript.getStorageSource();
        if(storageSource instanceof FileStorageSource fileStorageSource) {
            File scriptFile = fileStorageSource.getSourceFile();

            FileWriter fileWriter = new FileWriter(scriptFile);
            for(EditableNode node : editableScript.getNodes()) {

                fileWriter.write(node.getHeader().getScript());
                fileWriter.write(System.lineSeparator());
                fileWriter.write(Constants.DLB_HEADER_SEPARATOR);
                fileWriter.write(System.lineSeparator());
                fileWriter.write(node.getBody().getScript());
                fileWriter.write(System.lineSeparator());
                fileWriter.write(Constants.DLB_NODE_SEPARATOR);
                fileWriter.write(System.lineSeparator());

            }
            fileWriter.close();
        }

    }

}
