package com.dialoguebranch.script.parser;

import com.dialoguebranch.exception.ScriptParseException;
import com.dialoguebranch.model.Constants;
import com.dialoguebranch.script.model.DialogueBranchScript;
import com.dialoguebranch.script.model.ScriptNode;
import com.dialoguebranch.script.model.ScriptNodeBody;
import com.dialoguebranch.script.model.ScriptNodeHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DialogueBranchScriptParser {

    /**
     * Attempts to create a {@link DialogueBranchScript} from the given {@link File} and with the
     * {@code languageCode} provided. The file must have the correct file extension, as defined in
     * {@link Constants#DLB_SCRIPT_FILE_EXTENSION}.
     *
     * <p>This reader will split the given input into {@link ScriptNode}s based on the {@link
     * Constants#DLB_NODE_SEPARATOR}, but otherwise does not parse these individual nodes. As such,
     * any given file should successfully parse, as long as its file extension is correct.</p>
     *
     * @param file the Dialogue Branch script file to parse.
     * @param languageCode the language code for the {@link DialogueBranchScript} to generate.
     * @return a {@link DialogueBranchScript} object containing the contents of the script file.
     * @throws IOException in case of a read error.
     * @throws ScriptParseException in case the file has the wrong extension.
     */
    public static DialogueBranchScript read(File file, String languageCode)
            throws IOException, ScriptParseException {

        // Instantiate the DialogueBranchScript object based on the given file
        DialogueBranchScript dialogueBranchScript = getDialogueBranchScript(file, languageCode);

        List<String> linesBuffer = new ArrayList<>();

        // Read through the file line-by-line
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                // When we encounter the NODE_SEPARATOR, we take what we have and create a new node
                if(line.equals(Constants.DLB_NODE_SEPARATOR)) {
                    dialogueBranchScript.addScriptNode(createNode(linesBuffer));
                    linesBuffer = new ArrayList<>();
                } else {
                    // Otherwise, we add the line to our line buffer
                    linesBuffer.add(line);
                }

            }
            if(!linesBuffer.isEmpty()) {
                // If we have some leftover stuff, try to make a node out of it
                // This can happen if the last node is not ended with a node separator
                // But we don't want to leave this data out
                dialogueBranchScript.addScriptNode(createNode(linesBuffer));
            }
        }
        return dialogueBranchScript;
    }

    /**
     * Create a {@link ScriptNode} from a list of lines. If there is a header separator, it will
     * create a node with a separate header and body.
     *
     * @param lines the list of lines to convert into a {@link ScriptNode}.
     * @return a {@link ScriptNode}, or {@code null} if the given list of Strings is empty.
     */
    private static ScriptNode createNode(List<String> lines) {

        // If there is no lines to work with, return null
        if(lines.isEmpty()) {
            return null;
        }

        // If the only line we have is a header separator, return null
        if(lines.size() == 1 && lines.contains(Constants.DLB_HEADER_SEPARATOR)) {
            return null;
        }

        // If there is no header separator, everything is considered body
        else if(!lines.contains(Constants.DLB_HEADER_SEPARATOR)) {
            ScriptNodeBody body = new ScriptNodeBody(lines);
            return new ScriptNode(null, body);
        }

        // If the header separator is the last element of the list, everything is header
        else if(lines.indexOf(Constants.DLB_HEADER_SEPARATOR) == lines.size()-1) {
            ScriptNodeHeader header = new ScriptNodeHeader(lines);
            return new ScriptNode(header, null);
        }

        // Else, split the lines into header and body
        else {
            ScriptNodeHeader header = new ScriptNodeHeader(
                    lines.subList(0, lines.indexOf(Constants.DLB_HEADER_SEPARATOR)));
            ScriptNodeBody body = new ScriptNodeBody(
                    lines.subList(lines.indexOf(Constants.DLB_HEADER_SEPARATOR)+1, lines.size()));
            return new ScriptNode(header, body);
        }

    }

    /**
     * Creates a {@link DialogueBranchScript} object with the name of the given file, if the
     * extension of the given file is correct (see {@link Constants#DLB_SCRIPT_FILE_EXTENSION}).
     *
     * @param file the File from which to create the {@link DialogueBranchScript}.
     * @return a {@link DialogueBranchScript} object with the name of the given file.
     * @throws ScriptParseException in case of an incorrect file extension.
     */
    private static DialogueBranchScript getDialogueBranchScript(File file, String languageCode)
            throws ScriptParseException {
        String extension = file.getName().substring(
                file.getName().lastIndexOf("."));

        if(!extension.equals(Constants.DLB_SCRIPT_FILE_EXTENSION))
            throw new ScriptParseException(
                    "Invalid file extension '" + extension + "', expected: '"
                            + Constants.DLB_SCRIPT_FILE_EXTENSION);

        // Create a new DialogueBranchScript with the file name as dialogueName
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        return new DialogueBranchScript(fileName, languageCode);
    }

}
