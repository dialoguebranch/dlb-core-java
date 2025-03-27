package com.dialoguebranch.script.parser;

import com.dialoguebranch.exception.FileSystemException;
import com.dialoguebranch.model.Language;
import com.dialoguebranch.script.model.EditableProject;
import com.dialoguebranch.script.model.EditableTranslation;
import com.dialoguebranch.script.model.FileStorageSource;
import com.dialoguebranch.script.model.StorageSource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class EditableTranslationParser {

    /**
     * Read an editable translation as part of the given editableProject and in the given language.
     * @param editableProject
     * @param storageSource
     * @return
     */
    public static EditableTranslation read(EditableProject editableProject,
                                           Language language,
                                           StorageSource storageSource) throws FileSystemException, IOException {
        if(storageSource instanceof FileStorageSource fileStorageSource) {
            return read(editableProject,language,fileStorageSource);
        } else {
            return null;
            // TODO: Implement for other StorageSources
        }
    }

    public static EditableTranslation read(EditableProject editableProject,
                                           Language language,
                                           FileStorageSource fileStorageSource)
            throws IOException, FileSystemException {
        String projectBasePath = editableProject.getProjectMetaData().getBasePath();

        // First, make sure that the given fileStorageSource points to a file that is part of
        // the given project.
        if(!(fileStorageSource.getSourceFile().getCanonicalPath()
                .contains(new File(projectBasePath).getCanonicalPath() + File.separator))) {
            throw new FileSystemException("Attempting to read a translation file that is not " +
                    "contained within the given Dialogue Branch project.");
        }

        String languageFolder = new File(projectBasePath).getCanonicalPath() + File.separator
                + language.getCode() + File.separator;

        // Next, check that the given fileStorageSource is in the correct language folder
        if(!(fileStorageSource.getSourceFile().getCanonicalPath()
                .contains(languageFolder))) {
            throw new FileSystemException("Attempting to read a translation file that is part of " +
                    "the project, but not in its correct language folder.");
        }

        // TODO: This is unfinished

        // Determine the full "dialogueName" (e.g. /subfolder/test/dialogue1")
        String dialogueName = fileStorageSource.getSourceFile().getCanonicalPath()
                .substring(languageFolder.length(),0);



        ObjectMapper mapper = new ObjectMapper();
        //this.contentMap = mapper.readValue(fileStorageSource.getSourceFile(), HashMap.class);

        return null;
    }
}
