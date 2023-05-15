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

import java.io.File;
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

import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.i18n.I18nLanguageFinder;
import com.dialoguebranch.i18n.DLBContextTranslation;
import com.dialoguebranch.i18n.DLBTranslatable;
import com.dialoguebranch.i18n.DLBTranslationContext;
import com.dialoguebranch.i18n.DLBTranslationParser;
import com.dialoguebranch.i18n.DLBTranslationParserResult;
import com.dialoguebranch.i18n.DLBTranslator;
import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBDialogueDescription;
import com.dialoguebranch.model.DLBProject;

/**
 * This class can read an entire DialogueBranch project consisting of ".dlb" dialogue
 * files and ".json" translation files.
 *
 * @author Dennis Hofs (RRD)
 */
public class DLBProjectParser {
	private DLBFileLoader fileLoader;

	private Map<DLBFileDescription, DLBDialogue> dialogues =
			new LinkedHashMap<>();
	private Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>> translations =
			new LinkedHashMap<>();

	private Map<DLBDialogueDescription, DLBDialogue> translatedDialogues =
			new LinkedHashMap<>();

	public DLBProjectParser(DLBFileLoader fileLoader) {
		this.fileLoader = fileLoader;
	}

	public DLBProjectParserResult parse() throws IOException {
		DLBProjectParserResult result = new DLBProjectParserResult();
		List<DLBFileDescription> files = fileLoader.listDialogueBranchFiles();
		parseFiles(files, result);
		if (!result.getParseErrors().isEmpty())
			return result;
		createTranslatedDialogues(result);
		if (!result.getParseErrors().isEmpty())
			return result;
		DLBProject project = new DLBProject();
		project.setDialogues(translatedDialogues);
		Map<DLBDialogueDescription, DLBDialogue> sourceDialogues =
				new LinkedHashMap<>();
		for (DLBFileDescription descr : dialogues.keySet()) {
			sourceDialogues.put(fileDescriptionToDialogueDescription(descr),
					dialogues.get(descr));
		}
		project.setSourceDialogues(sourceDialogues);
		Map<DLBDialogueDescription,Map<DLBTranslatable,List<DLBContextTranslation>>> dlgTranslations =
				new LinkedHashMap<>();
		for (DLBFileDescription descr : translations.keySet()) {
			dlgTranslations.put(fileDescriptionToDialogueDescription(descr),
					translations.get(descr));
		}
		project.setTranslations(dlgTranslations);
		result.setProject(project);
		return result;
	}

	/**
	 * Tries to parse all project files (dialogue and translation files). This
	 * method fills variables "dialogues" and "translations". Any parse errors
	 * will be added to "readResult".
	 *
	 * <p>It uses "dialogueFiles" and "translationFiles". They will be cleared
	 * in the end.</p>
	 *
	 * @param files the project files
	 * @param readResult the read result
	 * @throws IOException if a reading error occurs
	 */
	private void parseFiles(List<DLBFileDescription> files,
			DLBProjectParserResult readResult) throws IOException {
		Set<DLBDialogueDescription> dlgDescrSet = new HashSet<>();
		List<DLBFileDescription> dialogueFiles = new ArrayList<>();
		List<DLBFileDescription> translationFiles = new ArrayList<>();
		for (DLBFileDescription file : files) {
			if (file.getFilePath().endsWith(".dlb"))
				dialogueFiles.add(file);
			else if (file.getFilePath().endsWith(".json"))
				translationFiles.add(file);
		}
		Set<String> dialogueNames = new HashSet<>();
		for (DLBFileDescription descr : dialogueFiles) {
			dlgDescrSet.add(fileDescriptionToDialogueDescription(descr));
			DLBParserResult dlgReadResult = parseDialogueFile(descr);
			if (dlgReadResult.getParseErrors().isEmpty()) {
				dialogues.put(descr, dlgReadResult.getDialogue());
				dialogueNames.add(dlgReadResult.getDialogue()
						.getDialogueName());
			} else {
				getParseErrors(readResult, descr).addAll(
						dlgReadResult.getParseErrors());
			}
		}
		if (readResult.getParseErrors().isEmpty()) {
			// validate referenced dialogues in external node pointers
			for (DLBFileDescription descr : dialogues.keySet()) {
				DLBDialogue dlg = dialogues.get(descr);
				for (String refName : dlg.getDialoguesReferenced()) {
					if (!dialogueNames.contains(refName)) {
						getParseErrors(readResult, descr).add(
								new ParseException(String.format(
								"Found external node pointer in dialogue %s to unknown dialogue %s",
								dlg.getDialogueName(), refName)));
					}
				}
			}
		}
		for (DLBFileDescription descr : translationFiles) {
			DLBDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			if (dlgDescrSet.contains(dlgDescr)) {
				getParseErrors(readResult, descr).add(new ParseException(
						String.format("Found both translation file \"%s\" and dialogue file \"%s.dlb\"",
						descr.getFilePath(), dlgDescr.getDialogueName()) + ": " +
						descr));
				continue;
			}
			DLBTranslationParserResult transParseResult = parseTranslationFile(
					descr);
			if (!transParseResult.getParseErrors().isEmpty()) {
				getParseErrors(readResult, descr).addAll(
						transParseResult.getParseErrors());
			}
			if (!transParseResult.getWarnings().isEmpty()) {
				getWarnings(readResult, descr).addAll(
						transParseResult.getWarnings());
			}
			if (transParseResult.getParseErrors().isEmpty())
				translations.put(descr, transParseResult.getTranslations());
		}
	}

	private List<ParseException> getParseErrors(
			DLBProjectParserResult readResult, DLBFileDescription descr) {
		String path = fileDescriptionToPath(descr);
		List<ParseException> errors = readResult.getParseErrors().get(path);
		if (errors != null)
			return errors;
		errors = new ArrayList<>();
		readResult.getParseErrors().put(path, errors);
		return errors;
	}

	private List<String> getWarnings(DLBProjectParserResult readResult,
									 DLBFileDescription descr) {
		String path = fileDescriptionToPath(descr);
		List<String> warnings = readResult.getWarnings().get(path);
		if (warnings != null)
			return warnings;
		warnings = new ArrayList<>();
		readResult.getWarnings().put(path, warnings);
		return warnings;
	}

	/**
	 * Tries to create translated dialogues for all translation files. This
	 * method fills variable "translatedDialogues" with the dialogues from
	 * "dialogues" plus translated dialogues from "translations". Any parse
	 * errors will be added to "readResult".
	 *
	 * <p>It uses "dialogues" and "translations". They will be cleared in the
	 * end.</p>
	 *
	 * @param readResult the read result
	 */
	private void createTranslatedDialogues(DLBProjectParserResult readResult) {
		for (DLBFileDescription descr : dialogues.keySet()) {
			DLBDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			DLBDialogue dlg = dialogues.get(descr);
			translatedDialogues.put(dlgDescr, dlg);
		}
		for (DLBFileDescription descr : translations.keySet()) {
			DLBDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			DLBDialogue source = findSourceDialogue(
					dlgDescr.getDialogueName());
			if (source == null) {
				getParseErrors(readResult, descr).add(new ParseException(
						"No source dialogue found for translation: " +
						descr));
				continue;
			}
			DLBTranslator translator = new DLBTranslator(
					new DLBTranslationContext(), translations.get(descr));
			DLBDialogue translated = translator.translate(source);
			translatedDialogues.put(dlgDescr, translated);
		}
	}

	private DLBDialogue findSourceDialogue(String dlgName) {
		List<DLBFileDescription> matches = new ArrayList<>();
		for (DLBFileDescription descr : dialogues.keySet()) {
			String currDlgName = fileNameToDialogueName(descr.getFilePath());
			if (currDlgName.equals(dlgName))
				matches.add(descr);
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
		String dlgName = fileNameToDialogueName(description.getFilePath());
		try (DLBParser dlbParser = new DLBParser(dlgName,
				fileLoader.openFile(description))) {
			return dlbParser.readDialogue();
		}
	}

	private DLBTranslationParserResult parseTranslationFile(
			DLBFileDescription description) throws IOException {
		try (Reader reader = fileLoader.openFile(description)) {
			return DLBTranslationParser.parse(reader);
		}
	}

	private DLBDialogueDescription fileDescriptionToDialogueDescription(
			DLBFileDescription descr) {
		DLBDialogueDescription result = new DLBDialogueDescription();
		result.setLanguage(descr.getLanguage());
		result.setDialogueName(fileNameToDialogueName(descr.getFilePath()));
		return result;
	}

	private String fileNameToDialogueName(String fileName) {
		if (fileName.endsWith(".dlb")) {
			return fileName.substring(0,fileName.length() - 4);
		} else if(fileName.endsWith(".json")) {
			return fileName.substring(0, fileName.length() - 5);
		}
		else
			return fileName;
	}

	private String fileDescriptionToPath(DLBFileDescription descr) {
		return descr.getLanguage() + "/" + descr.getFilePath();
	}
}
