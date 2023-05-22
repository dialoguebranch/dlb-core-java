/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
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

import com.dialoguebranch.model.DLBFileDescription;
import com.dialoguebranch.model.DLBFileType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of a {@link DLBFileLoader} that can generate a list of
 * {@link DLBFileDescription}s by finding all .dlb and .json files in a given directory. The
 * directory provided when creating this {@link DLBDirectoryFileLoader} is assumed to have one or
 * many subdirectories, representing different languages, that contain .dlb and/or .json files. For
 * example:
 * <br/>
 * <ul>
 *     <li>/directory/</li>
 *     <ul>
 *         <li>en/</li>
 *         <ul>
 *             <li>script1.dlb</li>
 *             <li>script2.dlb</li>
 *             <li>...</li>
 *         </ul>
 *         <li>pt/</li>
 *         <ul>
 *             <li>script1.json</li>
 *             <li>script2.json</li>
 *             <li>...</li>
 *         </ul>
 *     </ul>
 * </ul>
 *
 * @author Dennis Hofs (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class DLBDirectoryFileLoader implements DLBFileLoader {

	private final File rootDirectory;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a {@link DLBDirectoryFileLoader} with the given {@code rootDirectory}.
	 * @param rootDirectory the directory in which to look for languages folders with .dlb and/or
	 *                      .json files.
	 */
	public DLBDirectoryFileLoader(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the root directory for this {@link DLBDirectoryFileLoader}.
	 * @return the root directory for this {@link DLBDirectoryFileLoader}.
	 */
	public File getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public List<DLBFileDescription> listDialogueBranchFiles() {
		List<DLBFileDescription> result = new ArrayList<>();
		File[] children = rootDirectory.listFiles();
		if(children != null) {
			for (File child : children) {
				if (!child.isDirectory() || child.getName().startsWith("."))
					continue;
				String language = child.getName();
				result.addAll(listDir(language, "", child));
			}
		}
		return result;
	}

	/**
	 * Recursively generates a list of {@link DLBFileDescription} objects from all .dlb and/or .json
	 * files in the given {@code directory} (and all its subdirectories), under the given relative
	 * {@code pathName} (relative to the {@code rootDirectory} of this
	 * {@link DLBDirectoryFileLoader}. Each {@link DLBFileDescription} will have its language
	 * attribute set to the given {@code language} parameter, which is the direct subfolder of the
	 * {@code rootDirectory} under which it was found.
	 *
	 * @param language the language code, or name of the main folder.
	 * @param pathName the relative pathName in which the given {@code directory} can be found.
	 * @param directory the directory in which to look for .dlb and .json files.
	 * @return a list of all encountered .dlb and .json files as {@code DLBFileDescription}s.
	 */
	private List<DLBFileDescription> listDir(String language, String pathName, File directory) {
		List<DLBFileDescription> result = new ArrayList<>();
		File[] children = directory.listFiles();
		if(children != null) {
			for (File child : children) {
				if (child.isDirectory() && !child.getName().startsWith(".")) {
					result.addAll(listDir(language, pathName + child.getName() + "/", child));
				} else if (child.isFile()) {
					if (child.getName().endsWith(".dlb")) {
						result.add(new DLBFileDescription(
								language, pathName + child.getName(), DLBFileType.SCRIPT));
					} else if (child.getName().endsWith(".json")) {
						result.add(new DLBFileDescription(
								language, pathName + child.getName(), DLBFileType.TRANSLATION));
					}
				}
			}
		}
		return result;
	}

	@Override
	public Reader openFile(DLBFileDescription fileDescription) throws IOException {
		File file = new File(rootDirectory, fileDescription.getLanguage() + File.separator +
				fileDescription.getFilePath());
		return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
	}
}
