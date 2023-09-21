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
import com.dialoguebranch.parser.*;
import nl.rrd.utils.exception.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * The CommandLineRunner is the single runnable class for the Dialogue Branch Core Java Library.
 *
 * <p>This class can be used to test various functions of the library.</p>
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class CommandLineRunner {

	/**
	 * Creates an instance of a CommandLineRunner, which serves no purpose as the CommandLineRunner
	 * class is a collection of static methods.
	 */
	public CommandLineRunner() { }

	/**
	 * Execute the command line runner, either without parameters ("interactive mode"), or by
	 * providing certain command line options for quickly executing a specific task (To Be Defined).
	 *
	 * @param args Command line arguments
	 */
	public static void main(String... args) {
		System.out.println("""
				Welcome to the DialogueBranch Command Line Runner.

				This command line tool can be used for a number of different scenarios.
				Since you haven't provided command line arguments, we will take you through an interactive menu to determine your desired scenario and parameters.""");

		System.out.println("The following scenarios are currently supported:\n");
		System.out.println("  1. Open a DialogueBranch Folder and generate a summary.");
		System.out.println("  2. Open a DialogueBranch Project (from metadata.xml) and generate " +
				           "a summary.");

		Scanner userInputScanner = new Scanner(System.in);
		System.out.print("\nChoose scenario: ");

		String scenario = userInputScanner.nextLine();
		switch (scenario) {
			case "1" -> generateProjectSummaryFromFolder();
			case "2" -> generateProjectSummaryFromXML();
			default -> {
				System.out.println("Unknown scenario '" + scenario + "', please provide a valid " +
						"number from the list provided above.");
				System.exit(0);
			}
		}
	}

	private static void generateProjectSummaryFromFolder() {

		File rootDirectory = null;
		boolean rootDirectoryValid = false;

		while(!rootDirectoryValid) {
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

		System.out.println(readResult.generateSummaryString());
	}

	private static void generateProjectSummaryFromXML() {
		File projectMetadataFile = null;
		boolean projectMetadataFileValid = false;

		// Get a pointer to the projectMetadataFile (dlb-project.xml file)
		while(!projectMetadataFileValid) {
			System.out.println("Please provide the project metadata (.xml) file of the " +
					"DialogueBranch project:");
			try {
				projectMetadataFile = askUserInputXMLFile();
				projectMetadataFileValid = true;
			} catch (DLBInvalidInputException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}

		DLBProjectParserResult readResult;
		try {
			ProjectFileLoader fileLoader = new ProjectFileLoader(projectMetadataFile);
			DLBProjectParser parser = new DLBProjectParser(fileLoader);
			readResult = parser.parse();
		} catch (IOException ex) {
			System.err.println("ERROR: Can't read DialogueBranch project from given project XML " +
					"file: " + projectMetadataFile + ": " + ex.getMessage());
			System.exit(0);
			return;
		} catch (ParseException e) {
			System.err.println("ERROR: Unable to parse Project MetaData XML file: "+e.getMessage());
			System.exit(0);
			return;
		}

		System.out.println(readResult.generateSummaryString());
	}

	// ----------------------------------------------------------
	// -------------------- Helper Functions --------------------
	// ----------------------------------------------------------

	/**
	 * Open the command line for user input and check whether the given value is a valid directory.
	 * If so, return this directory as a {@link File}, otherwise throws a
	 * {@link DLBInvalidInputException}.
	 *
	 * @return A {@link File} representing the directory that the user has provided as CLI input.
	 * @throws DLBInvalidInputException in case the given input was invalid.
	 */
	public static File askUserInputDirectory() throws DLBInvalidInputException  {
		Scanner userInputScanner = new Scanner(System.in);
		String directoryString = userInputScanner.nextLine();

		// Check if the input is not null
		if(directoryString == null)
			throw new DLBInvalidInputException("Provided input is null.");

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
			throw new DLBInvalidInputException("Error while checking whether the given input '" +
					directoryString + "' is a directory.");
		}
		if (!directory.isDirectory()) {
			throw new DLBInvalidInputException("Given path '"+ directory + "' is not " +
					"a directory.");
		}

		return directory;
	}

	/**
	 * Open the command line for user input and check whether the given value represents a valid,
	 * existing .xml file.
	 * If so, return a pointer to this file as a {@link File}, otherwise throws a
	 * {@link DLBInvalidInputException}.
	 *
	 * @return A {@link File} representing the XML file that the user has provided as CLI input.
	 * @throws DLBInvalidInputException in case the given input was invalid.
	 */
	public static File askUserInputXMLFile() throws DLBInvalidInputException {
		Scanner userInputScanner = new Scanner(System.in);
		String fileString = userInputScanner.nextLine();

		// Check if the input is not null
		if(fileString == null)
			throw new DLBInvalidInputException("Provided input is null.");

		// Check if the given input path exists
		File file = new File(fileString);
		if (!file.exists()) {
			throw new DLBInvalidInputException("Provided file '" + fileString +
					"' does not exist.");
		}

		// Check if the given input is a file
		try {
			file = file.getCanonicalFile();
		} catch (IOException ex) {
			throw new DLBInvalidInputException("Error while checking whether the given input '" +
					fileString + "' is a file.");
		}
		if (file.isDirectory()) {
			throw new DLBInvalidInputException("Given path '"+ file + "' is a " +
					"directory (not a file).");
		}

		// Check if the given input is an .xml file
		try {
			String extension = fileString.substring(fileString.lastIndexOf(".") + 1);
			if(extension.equals("xml")) return file;
			else throw new DLBInvalidInputException("The given input is not an .xml file.");
		} catch(IndexOutOfBoundsException e) {
			throw new DLBInvalidInputException("The given input is not an .xml file.");
		}
	}

}