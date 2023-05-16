/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dialoguebranch.cli;

import com.dialoguebranch.exception.DLBInvalidInputException;
import com.dialoguebranch.i18n.DLBContextTranslation;
import com.dialoguebranch.i18n.DLBTranslatable;
import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBFileDescription;
import com.dialoguebranch.model.DLBProject;
import com.dialoguebranch.parser.DLBDirectoryFileLoader;
import com.dialoguebranch.parser.DLBFileLoader;
import com.dialoguebranch.parser.DLBProjectParser;
import com.dialoguebranch.parser.DLBProjectParserResult;
import nl.rrd.utils.exception.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The CommandLineRunner is the single runnable class for the Dialogue Branch Core Java Library.
 *
 * @author Harm op den Akker (Fruit Tree Labs - hopdenakker@fruittreelabs.com)
 */
public class CommandLineRunner {

	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java " + DLBProjectParser.class.getName() + " [options] <projectdir>");
		System.out.println("    Parse DialogueBranch project directory and print a summary of each dialogue");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("-h -? --help");
		System.out.println("    Print this usage message");
	}

	public static void main(String... args) {
		System.out.println("Welcome to the DialogueBranch Command Line Runner.\n\n"+
			"This command line tool can be used for a number of different scenarios.\n"+
			"Since you haven't provided command line arguments, we will take you through an " +
			"interactive menu to determine your desired scenario and parameters.");

		System.out.println("The following scenarios are currently supported:\n");
		System.out.println("  1. Load a DialogueBranch Project and create a summary.");

		Scanner userInputScanner = new Scanner(System.in);
		System.out.print("\nChoose scenario: ");

		String scenario = userInputScanner.nextLine();  // Read user input
		switch(scenario) {

			case "1":
				generateProjectSummary();
				break;
			default:
				System.out.println("Unknown scenario '"+scenario+"', please provide a valid " +
					"number from the list provided above.");
				System.exit(0);
		}
	}

	

	private static void generateProjectSummary() {

		File rootDirectory = null;
		boolean rootDirectoryValid = false;

		while(!rootDirectoryValid) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Please provide the root directory of the DialogueBranch project:");
			try {
				rootDirectory = askUserInputDirectory();
				rootDirectoryValid = true;
			} catch (DLBInvalidInputException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}

		DLBProjectParserResult readResult;
		try {
			DLBFileLoader fileLoader = new DLBDirectoryFileLoader(rootDirectory);
			DLBProjectParser parser = new DLBProjectParser(fileLoader);
			readResult = parser.parse();
		} catch (IOException ex) {
			System.err.println("ERROR: Can't read DialogueBranch project from directory: " +
				rootDirectory.getAbsolutePath() + ": " + ex.getMessage());
			System.exit(0);
			return;
		}

		// If there are any errors, print them and stop the program.
		if (!readResult.getParseErrors().isEmpty()) {
			System.err.println("DialogueBranch project in directory '"+rootDirectory+"' contains " +
					"the following errors:");
			for (String key : readResult.getParseErrors().keySet()) {
				System.err.println("ERROR: Failed to parse file: " + key);
				List<ParseException> errors = readResult.getParseErrors().get(key);
				for (ParseException ex : errors) {
					System.err.println(ex.getMessage());
				}
			}
			System.exit(0);
			return;
		}

		// If there are any warnings, print them and continue.
		if(!readResult.getWarnings().isEmpty()) {
			System.out.println("DialogueBranch project in directory '"+rootDirectory+"' contains " +
					"the following warnings:");
			for (String key : readResult.getWarnings().keySet()) {
				System.out.println("WARNING: " + key);
				List<String> errors = readResult.getWarnings().get(key);
				for (String error : errors) {
					System.out.println(error);
				}
			}
		}

		System.out.println("Finished parsing DialogueBranch project from directory: " +
				rootDirectory.getAbsolutePath());

		DLBProject project = readResult.getProject();
		System.out.println("Project Summary:");
		System.out.println("Path: "+rootDirectory);

		Map<DLBFileDescription, DLBDialogue> sourceDialogues = project.getSourceDialogues();
		System.out.println("Number of Dialogue Scripts: "+sourceDialogues.size());
		for(DLBFileDescription dialogueDescription : sourceDialogues.keySet()) {
			System.out.println("  - " +dialogueDescription);
		}

		Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>> translations =
				project.getTranslations();
		System.out.println("Number of Translation Scripts: "+translations.size());
		for(DLBFileDescription dialogueDescription : translations.keySet()) {
			System.out.println("  - " +dialogueDescription);
		}

		Map<DLBFileDescription, DLBDialogue> dialogues = readResult.getProject().getDialogues();

		for (DLBFileDescription dialogue : dialogues.keySet()) {
			System.out.println("----------");
			System.out.println("DIALOGUE " + dialogue.getFilePath() +
					" (" + dialogue.getLanguage() + ")");
			System.out.println(dialogues.get(dialogue));
		}

	}

	/**
	 * Open the command line for user input and check whether the given value is a valid directory.
	 * If so, return this directory as a {@link File}, otherwise throws a
	 * {@link DLBInvalidInputException}.
	 *
	 * @return A {@link File} representing the directory that the user has provided as CLI input.
	 * @throws DLBInvalidInputException in case the given input was invalid.
	 */
	private static File askUserInputDirectory() throws DLBInvalidInputException  {
		Scanner userInputScanner = new Scanner(System.in);
		String directoryString = userInputScanner.nextLine();

		// Check if the input is not null
		if(directoryString == null)
			throw new DLBInvalidInputException("Provided directory is null.");

		// Check if the given input path exists
		File directory = new File(directoryString);
		if (!directory.exists()) {
			throw new DLBInvalidInputException("Provided directory '" + directoryString +
					"' does not exist.");
		}

		// Check if the given input path is a directory
		try {
			directory = directory.getCanonicalFile();
		} catch (IOException ex) {
			throw new DLBInvalidInputException("Error while checking whether the given path '" +
					directoryString + "' is a directory.");
		}
		if (!directory.isDirectory()) {
			throw new DLBInvalidInputException("Given path '"+ directory.toString() + "' is not " +
					"a directory.");
		}

		return directory;
	}

}
