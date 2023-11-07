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

package com.dialoguebranch.parser;

import com.dialoguebranch.model.FileDescriptor;
import com.dialoguebranch.model.FileType;
import com.dialoguebranch.model.DLBProjectMetaData;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.SimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProjectFileLoader implements FileLoader {

	private final File projectMetadataFile;
	private final DLBProjectMetaData projectMetaData;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a {@link ProjectFileLoader} with a given pointer to a project metadata
	 * xml file, which is immediately parsed into a {@link DLBProjectMetaData} object.
	 * @param projectMetadataFile the Dialogue Branch project metadata .xml file
	 * @throws IOException in case of a read error when parsing the project metadata file.
	 * @throws ParseException in case of a parse error when parsing the project metadata file.
	 */
	public ProjectFileLoader(File projectMetadataFile) throws IOException, ParseException {
		this.projectMetadataFile = projectMetadataFile;
		this.projectMetaData = loadProjectMetaDataFile(projectMetadataFile);
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the Dialogue Branch project metadata (.xml) {@link File} from which this
	 * {@link ProjectFileLoader} can load its resource files.
	 * @return the Dialogue Branch project metadata (.xml) {@link File}.
	 */
	public File getProjectMetadataFile() {
		return projectMetadataFile;
	}

	/**
	 * Returns the Dialogue Branch project metadata object from which this {@link ProjectFileLoader}
	 * can load its resource files.
	 * @return the Dialogue Branch Project MetaData {@link DLBProjectMetaData} object.
	 */
	public DLBProjectMetaData getProjectMetaData() {
		return projectMetaData;
	}

	// -------------------------------------------------------------------
	// -------------------- Interface Implementations --------------------
	// -------------------------------------------------------------------

	@Override
	public List<FileDescriptor> listDialogueBranchFiles() {
		List<FileDescriptor> result = new ArrayList<>();

		// Get a list of all the language folders
		List<String> supportedLanguages = projectMetaData.getSupportedLanguages();

		File rootDirectory = new File(projectMetaData.getBasePath());

		File[] children = rootDirectory.listFiles();
		if(children != null) {
			for (File child : children) {
				if (!child.isDirectory() || child.getName().startsWith("."))
					continue;
				String language = child.getName();
				if(supportedLanguages.contains(language)) {
					result.addAll(listDir(language, "", child));
				}
			}
		}
		return result;
	}

	@Override
	public Reader openFile(FileDescriptor fileDescription) throws IOException {
		File file = new File(new File(projectMetaData.getBasePath()),
				fileDescription.getLanguage() + File.separator +
						fileDescription.getFilePath());
		return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------
	// -------------------- Other Functions --------------------
	// ---------------------------------------------------------

	public static DLBProjectMetaData loadProjectMetaDataFile(File metaDataFile)
			throws IOException, ParseException {
		SimpleSAXHandler<DLBProjectMetaData> xmlHandler = DLBProjectMetaData.getXMLHandler();
		SimpleSAXParser<DLBProjectMetaData> parser = new SimpleSAXParser<>(xmlHandler);
		DLBProjectMetaData result = parser.parse(metaDataFile);
		result.setBasePath(metaDataFile.getParent());
		return result;
	}

	/**
	 * Recursively generates a list of {@link FileDescriptor} objects from all .dlb
	 * and/or .json files in the given {@code directory} (and all its subdirectories), under the
	 * given relative {@code pathName} (relative to the {@code rootDirectory} of this
	 * {@link DirectoryFileLoader}). Each {@link FileDescriptor} will have its
	 * language attribute set to the given {@code language} parameter, which is the direct
	 * sub-folder of the {@code rootDirectory} under which it was found.
	 *
	 * @param language the language code, or name of the main folder.
	 * @param pathName the relative pathName in which the given {@code directory} can be found.
	 * @param directory the directory in which to look for .dlb and .json files.
	 * @return a list of all encountered .dlb and .json files as
	 *         {@code FileDescriptor}s.
	 */
	private List<FileDescriptor> listDir(String language, String pathName,
										 File directory) {
		List<FileDescriptor> result = new ArrayList<>();
		File[] children = directory.listFiles();
		if(children != null) {
			for (File child : children) {
				if (child.isDirectory() && !child.getName().startsWith(".")) {
					result.addAll(listDir(language, pathName +
							child.getName() + "/", child));
				} else if (child.isFile()) {
					if (child.getName().endsWith(".dlb")) {
						result.add(new FileDescriptor(
								language,
								pathName + child.getName(),
								FileType.SCRIPT));
					} else if (child.getName().endsWith(".json")) {
						result.add(new FileDescriptor(
								language,
								pathName + child.getName(),
								FileType.TRANSLATION));
					}
				}
			}
		}
		return result;
	}
}
