package com.dialoguebranch.script.parser;

import com.dialoguebranch.model.ProjectMetaData;
import com.dialoguebranch.parser.ProjectMetaDataParser;
import com.dialoguebranch.script.model.EditableProject;
import nl.rrd.utils.exception.ParseException;

import java.io.File;
import java.io.IOException;

public class EditableProjectParser {

    public static EditableProject read(File metaDataFile) throws IOException, ParseException {

        ProjectMetaData projectMetaData = ProjectMetaDataParser.parse(metaDataFile);

        // Add file listings...

        return new EditableProject(projectMetaData);
    }



}
