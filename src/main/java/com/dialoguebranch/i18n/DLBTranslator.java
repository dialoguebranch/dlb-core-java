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

package com.dialoguebranch.i18n;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBNode;
import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBVariableString;

/**
 * This class can translate {@link DLBNode}s given a translation map.
 * The translation map can be obtained from a translation file using the {@link
 * DLBTranslationParser}.
 *
 * @author Dennis Hofs (RRD)
 */
public class DLBTranslator {
	private DLBTranslationContext context;
	private Map<String,List<DLBContextTranslation>> exactTranslations;
	private Map<String,List<DLBContextTranslation>> normalizedTranslations;
	private Pattern preWhitespaceRegex;
	private Pattern postWhitespaceRegex;

	/**
	 * Constructs a new translator.
	 *
	 * @param context the translation context
	 * @param translations the translation map
	 */
	public DLBTranslator(DLBTranslationContext context,
						 Map<DLBTranslatable,List<DLBContextTranslation>> translations) {
		this.context = context;
		this.exactTranslations = new LinkedHashMap<>();
		for (DLBTranslatable key : translations.keySet()) {
			this.exactTranslations.put(key.toString().trim(),
					translations.get(key));
		}
		this.normalizedTranslations = new LinkedHashMap<>();
		for (DLBTranslatable key : translations.keySet()) {
			this.normalizedTranslations.put(getNormalizedText(key),
					translations.get(key));
		}
		preWhitespaceRegex = Pattern.compile("^\\s+");
		postWhitespaceRegex = Pattern.compile("\\s+$");
	}

	private String getNormalizedText(DLBTranslatable translatable) {
		String norm = translatable.toString().trim();
		if (norm.isEmpty())
			return norm;
		String[] words = norm.split("\\s+");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			if (i > 0)
				result.append(" ");
			result.append(words[i]);
		}
		return result.toString();
	}

	/**
	 * Translates the specified dialogue. This method creates a clone of the
	 * dialogue and then tries to fill in a translation for every translatable
	 * segment (plain text, variables and &lt;&lt;input&gt;&gt; commands).
	 *
	 * @param dialogue the dialogue
	 * @return the translated dialogue
	 */
	public DLBDialogue translate(DLBDialogue dialogue) {
		dialogue = new DLBDialogue(dialogue);
		for (DLBNode node : dialogue.getNodes()) {
			translateBody(node.getHeader().getSpeaker(),
					DLBSourceTranslatable.USER, node.getBody());
		}
		return dialogue;
	}

	/**
	 * Translates the specified node. This method creates a clone of the node
	 * and then tries to fill in a translation for every translatable segment
	 * (plain text, variables and &lt;&lt;input&gt;&gt; commands).
	 *
	 * @param node the node
	 * @return the translated node
	 */
	public DLBNode translate(DLBNode node) {
		node = new DLBNode(node);
		translateBody(node.getHeader().getSpeaker(),
				DLBSourceTranslatable.USER, node.getBody());
		return node;
	}

	private void translateBody(String speaker, String addressee,
			DLBNodeBody body) {
		DLBTranslatableExtractor extractor = new DLBTranslatableExtractor();
		List<DLBSourceTranslatable> translatables = extractor.extractFromBody(
				speaker, addressee, body);
		for (DLBSourceTranslatable translatable : translatables) {
			translateText(translatable);
		}
	}

	private void translateText(DLBSourceTranslatable text) {
		String textPlain = text.getTranslatable().toString();
		String preWhitespace = "";
		String postWhitespace = "";
		Matcher m = preWhitespaceRegex.matcher(textPlain);
		if (m.find())
			preWhitespace = m.group();
		m = postWhitespaceRegex.matcher(textPlain);
		if (m.find())
			postWhitespace = m.group();
		List<DLBContextTranslation> transList = exactTranslations.get(
				text.getTranslatable().toString().trim());
		if (transList == null) {
			transList = normalizedTranslations.get(getNormalizedText(
					text.getTranslatable()));
		}
		if (transList == null)
			return;
		DLBTranslatable translation = findContextTranslation(text, transList);
		DLBNodeBody body = text.getTranslatable().getParent();
		List<DLBNodeBody.Segment> bodySegments = new ArrayList<>(
				body.getSegments());
		List<DLBNodeBody.Segment> textSegments = text.getTranslatable()
				.getSegments();
		int insertIndex = body.getSegments().indexOf(textSegments.get(0));
		for (DLBNodeBody.Segment segment : textSegments) {
			bodySegments.remove(segment);
		}
		if (preWhitespace.length() > 0) {
			bodySegments.add(insertIndex++, new DLBNodeBody.TextSegment(
					new DLBVariableString(preWhitespace)));
		}
		List<DLBNodeBody.Segment> transSegments = translation.getSegments();
		for (DLBNodeBody.Segment transSegment : transSegments) {
			bodySegments.add(insertIndex++, transSegment);
		}
		if (postWhitespace.length() > 0) {
			bodySegments.add(insertIndex, new DLBNodeBody.TextSegment(
					new DLBVariableString(postWhitespace)));
		}
		body.clearSegments();
		for (DLBNodeBody.Segment segment : bodySegments) {
			body.addSegment(segment);
		}
	}

	private DLBTranslatable findContextTranslation(
			DLBSourceTranslatable source,
			List<DLBContextTranslation> transList) {
		DLBTranslationContext.Gender speakerGender = getGenderForSpeaker(
				source.getSpeaker());
		DLBTranslationContext.Gender addresseeGender = getGenderForSpeaker(
				source.getAddressee());
		List<DLBContextTranslation> prevFilter = transList;
		List<DLBContextTranslation> filtered = filterSpeaker(transList,
				source.getSpeaker());
		if (filtered.isEmpty())
			filtered = prevFilter;
		prevFilter = filtered;
		filtered = filterGender(transList, speakerGender, addresseeGender);
		if (filtered.isEmpty())
			filtered = prevFilter;
		return filtered.get(0).getTranslation();
	}

	private DLBTranslationContext.Gender getGenderForSpeaker(String speaker) {
		if (speaker.equals(DLBSourceTranslatable.USER))
			return context.getUserGender();
		if (context.getAgentGenders().containsKey(speaker))
			return context.getAgentGenders().get(speaker);
		return context.getDefaultAgentGender();
	}

	private List<DLBContextTranslation> filterSpeaker(
			List<DLBContextTranslation> terms, String speaker) {
		List<DLBContextTranslation> result = new ArrayList<>();
		String speakerContext = getSpeakerContext(speaker);
		for (DLBContextTranslation term : terms) {
			if (term.getContext().contains(speakerContext))
				result.add(term);
		}
		return result;
	}

	private String getSpeakerContext(String speaker) {
		if (speaker.equals(DLBSourceTranslatable.USER))
			return "_user";
		else
			return speaker;
	}

	private List<DLBContextTranslation> filterGender(
			List<DLBContextTranslation> terms,
			DLBTranslationContext.Gender speakerGender,
			DLBTranslationContext.Gender addresseeGender) {
		List<DLBContextTranslation> result = new ArrayList<>();
		if (speakerGender == null)
			speakerGender = DLBTranslationContext.Gender.MALE;
		if (addresseeGender == null)
			addresseeGender = DLBTranslationContext.Gender.MALE;
		for (DLBContextTranslation term : terms) {
			if (speakerGender == DLBTranslationContext.Gender.MALE &&
					term.getContext().contains("female_speaker")) {
				continue;
			}
			if (addresseeGender == DLBTranslationContext.Gender.MALE &&
					term.getContext().contains("female_addressee")) {
				continue;
			}
			if (speakerGender == DLBTranslationContext.Gender.FEMALE &&
					term.getContext().contains("male_speaker")) {
				continue;
			}
			if (addresseeGender == DLBTranslationContext.Gender.FEMALE &&
					term.getContext().contains("male_addressee")) {
				continue;
			}
			result.add(term);
		}
		return result;
	}
}
