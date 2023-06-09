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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.dialoguebranch.model.DLBFileType;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.i18n.I18nLanguageFinder;
import com.dialoguebranch.i18n.DLBContextTranslation;
import com.dialoguebranch.i18n.DLBTranslatable;
import com.dialoguebranch.i18n.DLBTranslationContext;
import com.dialoguebranch.i18n.DLBTranslationParser;
import com.dialoguebranch.i18n.DLBTranslationParserResult;
import com.dialoguebranch.i18n.DLBTranslator;
import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBFileDescription;
import com.dialoguebranch.model.DLBProject;

/**
 * This class can read an entire DialogueBranch project consisting of ".dlb" dialogue files and
 * ".json" translation files as provided through the given {@link DLBFileLoader} implementation.
 *
 * @author Dennis Hofs (RRD)
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class DLBProjectParser {
	private final DLBFileLoader fileLoader;

	private final Map<DLBFileDescription, DLBDialogue> dialogues = new LinkedHashMap<>();
	private final Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>>
			translations = new LinkedHashMap<>();
	private final Map<DLBFileDescription, DLBDialogue> translatedDialogues = new LinkedHashMap<>();

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a {@link DLBProjectParser} with a given {@link DLBFileLoader} that is
	 * used to retrieve a complete set of files (".dlb" and ".json") to use in this parser.
	 * @param fileLoader the {@link DLBFileLoader} implementation.
	 */
	public DLBProjectParser(DLBFileLoader fileLoader) {
		this.fileLoader = fileLoader;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	public DLBProjectParserResult parse() throws IOException {
		DLBProjectParserResult projectParserResult = new DLBProjectParserResult(fileLoader);

		List<DLBFileDescription> files = fileLoader.listDialogueBranchFiles();

		parseFiles(files, projectParserResult);

		if (!projectParserResult.getParseErrors().isEmpty())
			return projectParserResult;

		createTranslatedDialogues(projectParserResult);

		if (!projectParserResult.getParseErrors().isEmpty())
			return projectParserResult;

		DLBProject project = new DLBProject();
		project.setDialogues(translatedDialogues);

		Map<DLBFileDescription, DLBDialogue> sourceDialogues = new LinkedHashMap<>();
		for (DLBFileDescription fileDescription : dialogues.keySet()) {
			sourceDialogues.put(fileDescription, dialogues.get(fileDescription));
		}
		project.setSourceDialogues(sourceDialogues);

		Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>> dlgTranslations =
				new LinkedHashMap<>();
		for (DLBFileDescription fileDescription : translations.keySet()) {
			dlgTranslations.put(fileDescription, translations.get(fileDescription));
		}

		project.setTranslations(dlgTranslations);
		projectParserResult.setProject(project);
		return projectParserResult;
	}

	/**
	 * Tries to parse all project files (dialogue and translation files). This method fills
	 * variables "dialogues" and "translations". Any parse errors will be added to the provided
	 * {@code readResult}.
	 *
	 * <p>It uses "dialogueFiles" and "translationFiles". They will be cleared in the end.</p>
	 *
	 * @param fileDescriptions the project files
	 * @param readResult the read result
	 * @throws IOException if a reading error occurs
	 */
	private void parseFiles(List<DLBFileDescription> fileDescriptions,
							DLBProjectParserResult readResult) throws IOException {
		Set<DLBFileDescription> fileDescriptionsSet = new HashSet<>();
		List<DLBFileDescription> dialogueFiles = new ArrayList<>();
		List<DLBFileDescription> translationFiles = new ArrayList<>();

		// Split the given fileDescriptions into dialogueFiles and translationFiles
		for (DLBFileDescription fileDescription : fileDescriptions) {
			if (fileDescription.getFileType() == DLBFileType.SCRIPT)
				dialogueFiles.add(fileDescription);
			else if (fileDescription.getFileType() == DLBFileType.TRANSLATION)
				translationFiles.add(fileDescription);
		}

		Set<String> dialogueNames = new HashSet<>();
		for (DLBFileDescription fileDescription : dialogueFiles) {
			fileDescriptionsSet.add(fileDescription);
			DLBParserResult dlgReadResult = parseDialogueFile(fileDescription);
			if (dlgReadResult.getParseErrors().isEmpty()) {
				dialogues.put(fileDescription, dlgReadResult.getDialogue());
				dialogueNames.add(dlgReadResult.getDialogue().getDialogueName());
			} else {
				getParseErrors(readResult, fileDescription).addAll(dlgReadResult.getParseErrors());
			}
		}

		if (readResult.getParseErrors().isEmpty()) {
			// validate referenced dialogues in external node pointers
			for (DLBFileDescription fileDescription : dialogues.keySet()) {
				DLBDialogue dlg = dialogues.get(fileDescription);
				for (String refName : dlg.getDialoguesReferenced()) {
					if (!dialogueNames.contains(refName)) {
						getParseErrors(readResult, fileDescription).add(
							new ParseException(String.format(
							"Found external node pointer in dialogue %s to unknown dialogue %s",
							dlg.getDialogueName(), refName)));
					}
				}
			}
		}

		for (DLBFileDescription fileDescription : translationFiles) {
			if (fileDescriptionsSet.contains(fileDescription)) {
				getParseErrors(readResult, fileDescription).add(new ParseException(
					String.format("Found both translation file \"%s\" and dialogue file \"%s.dlb\"",
					fileDescription.getFilePath(), fileDescription.getFilePath()) + ": " +
					fileDescription));
				continue;
			}
			DLBTranslationParserResult transParseResult = parseTranslationFile(fileDescription);
			if (!transParseResult.getParseErrors().isEmpty()) {
				getParseErrors(readResult, fileDescription).addAll(
						transParseResult.getParseErrors());
			}
			if (!transParseResult.getWarnings().isEmpty()) {
				getWarnings(readResult, fileDescription).addAll(transParseResult.getWarnings());
			}
			if (transParseResult.getParseErrors().isEmpty())
				translations.put(fileDescription, transParseResult.getTranslations());
		}
	}

	private List<ParseException> getParseErrors(DLBProjectParserResult readResult,
												DLBFileDescription fileDescription) {
		String path = fileDescriptionToPath(fileDescription);
		List<ParseException> errors = readResult.getParseErrors().get(path);
		if (errors != null)
			return errors;
		errors = new ArrayList<>();
		readResult.getParseErrors().put(path, errors);
		return errors;
	}

	private List<String> getWarnings(DLBProjectParserResult readResult,
									 DLBFileDescription fileDescription) {
		String path = fileDescriptionToPath(fileDescription);
		List<String> warnings = readResult.getWarnings().get(path);
		if (warnings != null)
			return warnings;
		warnings = new ArrayList<>();
		readResult.getWarnings().put(path, warnings);
		return warnings;
	}

	/**
	 * Tries to create translated dialogues for all translation files. This method fills variable
	 * "translatedDialogues" with the dialogues from "dialogues" plus translated dialogues from
	 * "translations". Any parse errors will be added to "readResult".
	 *
	 * <p>It uses "dialogues" and "translations". They will be cleared in the end.</p>
	 *
	 * @param readResult the read result
	 */
	private void createTranslatedDialogues(DLBProjectParserResult readResult) {
		for (DLBFileDescription fileDescription : dialogues.keySet()) {
			DLBDialogue dlg = dialogues.get(fileDescription);
			translatedDialogues.put(fileDescription, dlg);
		}

		for (DLBFileDescription fileDescription : translations.keySet()) {
			DLBDialogue source = findSourceDialogue(fileDescription.getDialogueName());
			if (source == null) {
				getParseErrors(readResult, fileDescription).add(new ParseException(
						"No source dialogue found for translation: " + fileDescription));
				continue;
			}
			DLBTranslator translator = new DLBTranslator(
					new DLBTranslationContext(), translations.get(fileDescription));
			DLBDialogue translated = translator.translate(source);
			translatedDialogues.put(fileDescription, translated);
		}
	}

	private DLBDialogue findSourceDialogue(String dlgName) {
		List<DLBFileDescription> matches = new ArrayList<>();
		for (DLBFileDescription fileDescription : dialogues.keySet()) {
			String currDlgName = fileDescription.getDialogueName();
			if (currDlgName.equals(dlgName))
				matches.add(fileDescription);
		}
		if (matches.isEmpty())
			return null;
		if (matches.size() == 1)
			return dialogues.get(matches.get(0));
		Map<String, DLBFileDescription> lngMap = new HashMap<>();
		for (DLBFileDescription match : matches) {
			lngMap.put(match.getLanguage(), match);
		}
		I18nLanguageFinder finder = new I18nLanguageFinder(new ArrayList<>(
				lngMap.keySet()));
		finder.setUserLocale(Locale.ENGLISH);
		String language = finder.find();
		if (language == null)
			return dialogues.get(matches.get(0));
		else
			return dialogues.get(lngMap.get(language));
	}

	private DLBParserResult parseDialogueFile(DLBFileDescription description)
			throws IOException {
		String dlgName = description.getDialogueName();
		try (DLBParser dlbParser = new DLBParser(dlgName,
				fileLoader.openFile(description))) {
			return dlbParser.readDialogue();
		}
	}

	private DLBTranslationParserResult parseTranslationFile(DLBFileDescription description)
			throws IOException {
		try (Reader reader = fileLoader.openFile(description)) {
			return DLBTranslationParser.parse(reader);
		}
	}

	private String fileDescriptionToPath(DLBFileDescription fileDescription) {
		return fileDescription.getLanguage() + "/" + fileDescription.getFilePath();
	}
}
