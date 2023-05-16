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

package com.dialoguebranch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.dialoguebranch.i18n.DLBContextTranslation;
import com.dialoguebranch.i18n.DLBTranslationContext;
import com.dialoguebranch.i18n.DLBTranslator;
import nl.rrd.utils.i18n.I18nLanguageFinder;
import com.dialoguebranch.i18n.DLBTranslatable;

public class DLBProject {
	private Map<DLBFileDescription, DLBDialogue> dialogues = new LinkedHashMap<>();
	private Map<DLBFileDescription, DLBDialogue> sourceDialogues = new LinkedHashMap<>();
	private Map<DLBFileDescription,
			Map<DLBTranslatable,List<DLBContextTranslation>>> translations = new LinkedHashMap<>();

	/**
	 * Returns all available dialogues in this project. This includes source dialogues as well as
	 * translated dialogues with the default {@link DLBTranslationContext}.
	 *
	 * @return the available dialogues (source and translations with default context)
	 */
	public Map<DLBFileDescription, DLBDialogue> getDialogues() {
		return dialogues;
	}

	/**
	 * Sets all available dialogues in this project. This includes source dialogues as well as
	 * translated dialogues with the default {@link DLBTranslationContext}.
	 *
	 * @param dialogues the available dialogues (source and translations with default context)
	 */
	public void setDialogues(
			Map<DLBFileDescription, DLBDialogue> dialogues) {
		this.dialogues = dialogues;
	}

	/**
	 * Returns the source dialogues. This excludes any translations.
	 *
	 * @return the source dialogues (no translations)
	 */
	public Map<DLBFileDescription, DLBDialogue> getSourceDialogues() {
		return sourceDialogues;
	}

	/**
	 * Sets the source dialogues. This excludes any translations
	 *
	 * @param sourceDialogues the source dialogues (no translations)
	 */
	public void setSourceDialogues(
			Map<DLBFileDescription, DLBDialogue> sourceDialogues) {
		this.sourceDialogues = sourceDialogues;
	}

	/**
	 * Returns the translations of all phrases per dialogue. This method returns a map from a
	 * dialogue key to a translation map.
	 *
	 * <p>A translation map is a map from a source phrase to a list of translated phrases, with
	 * different contexts.</p>
	 *
	 * @return the translations
	 */
	public Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>>
	getTranslations() {
		return translations;
	}

	/**
	 * Sets the translations of all phrases per dialogue. This method returns a map from a dialogue
	 * key to a translation map.
	 *
	 * <p>A translation map is a map from a source phrase to a list of translated phrases,
	 * with different contexts.</p>
	 *
	 * @param translations the translations
	 */
	public void setTranslations(
			Map<DLBFileDescription,Map<DLBTranslatable,List<DLBContextTranslation>>>
					translations) {
		this.translations = translations;
	}

	/**
	 * Returns a translated dialogue for the specified translation context. This method first
	 * searches a source dialogue for the specified description (name and language). If found, no
	 * translation is needed and the source dialogue is returned. Otherwise, it searches a source
	 * dialogue with the specified dialogue name and a translation set for the specified language.
	 * If found, it translates the dialogue with the translation context, and then returns the
	 * translated dialogue.
	 *
	 * <p>If no source dialogue or translation is found, this method returns null.</p>
	 *
	 * @param dialogueDescription the dialogue description (name and language)
	 * @param context the translation context
	 * @return the translated dialogue or null
	 */
	public DLBDialogue getTranslatedDialogue(DLBFileDescription dialogueDescription,
											 DLBTranslationContext context) {
		DLBDialogue dialogue = sourceDialogues.get(dialogueDescription);
		if (dialogue != null)
			return dialogue;
		Map<DLBTranslatable,List<DLBContextTranslation>> translations =
				this.translations.get(dialogueDescription);
		if (translations == null)
			return null;
		dialogue = findSourceDialogue(dialogueDescription.getFilePath());
		if (dialogue == null)
			return null;
		DLBTranslator translator = new DLBTranslator(context, translations);
		return translator.translate(dialogue);
	}

	private DLBDialogue findSourceDialogue(String dialogueName) {
		List<DLBFileDescription> matches = new ArrayList<>();
		for (DLBFileDescription description : sourceDialogues.keySet()) {
			if (description.getFilePath().equals(dialogueName))
				matches.add(description);
		}
		if (matches.isEmpty())
			return null;
		if (matches.size() == 1)
			return dialogues.get(matches.get(0));
		Map<String, DLBFileDescription> lngMap = new HashMap<>();
		for (DLBFileDescription match : matches) {
			lngMap.put(match.getLanguage(), match);
		}
		I18nLanguageFinder finder = new I18nLanguageFinder(new ArrayList<>(lngMap.keySet()));
		finder.setUserLocale(Locale.ENGLISH);
		String language = finder.find();
		if (language == null)
			return dialogues.get(matches.get(0));
		else
			return dialogues.get(lngMap.get(language));
	}
}
