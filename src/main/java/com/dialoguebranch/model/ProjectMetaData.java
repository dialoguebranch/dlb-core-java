/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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

import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.AbstractSimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXHandler;
import nl.rrd.utils.xml.XMLWriter;
import com.dialoguebranch.exception.DuplicateLanguageCodeException;
import com.dialoguebranch.exception.UnknownLanguageCodeException;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link ProjectMetaData} class is the object representation of a DialogueBranch metadata .xml
 * file. This object can be serialized into an XML file using an {@link XMLWriter} or be constructed
 * from an XML file using a {@link SimpleSAXHandler}. Additionally, contains methods for dynamically
 * modifying the contents of a {@link ProjectMetaData} specification while maintaining certain
 * constraints.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class ProjectMetaData {

	private String name;
	private String basePath;
	private String description;
	private String version;
	private LanguageMap languageMap;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of an empty {@link ProjectMetaData} object.
	 */
	public ProjectMetaData() { }

	/**
	 * Creates an instance of a {@link ProjectMetaData} object with the given parameters.
	 *
	 * @param name a descriptive name of the DialogueBranch project.
	 * @param basePath the folder in which this DialogueBranch project is stored.
	 * @param description a textual description of this DialogueBranch project.
	 * @param version free-form version information (e.g. v0.1.0).
	 * @param languageMap contains all the languages supported by this DialogueBranch project.
	 */
	public ProjectMetaData(String name, String basePath, String description, String version,
						   LanguageMap languageMap) {
		this.name = name;
		this.basePath = basePath;
		this.description = description;
		this.version = version;
		this.languageMap = languageMap;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the name of this {@link Project} as a String.
	 * @return the name of this {@link Project} as a String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this DialogueBranch project.
	 * @param name the name of this DialogueBranch project.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a String representation of the base path of this {@link Project}.
	 * @return a String representation of the base path of this {@link Project}.
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * Sets the base path for this DialogueBranch project as a {@link String}.
	 * @param basePath the base path for this DialogueBranch project as a {@link String}.
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	/**
	 * Returns the description of this {@link Project}.
	 * @return the description of this {@link Project}.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description text for this DialogueBranch project.
	 * @param description the description text for this DialogueBranch project.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the version of this {@link Project}.
	 * @return the version of this {@link Project}.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version string for this DialogueBranch project.
	 * @param version the version string for this DialogueBranch project.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the {@link LanguageMap} that contains a description of all
	 * languages supported in this {@link Project} and their mapping from source-
	 * to translation languages.
	 * @return the {@link LanguageMap} for this {@link ProjectMetaData}.
	 */
	public LanguageMap getLanguageMap() {
		return languageMap;
	}

	/**
	 * Sets the {@link LanguageMap} for this DialogueBranch project, containing a mapping of all
	 * supported source- and translation languages.
	 * @param languageMap the {@link LanguageMap} for this DialogueBranch project.
	 */
	public void setLanguageMap(LanguageMap languageMap) {
		this.languageMap = languageMap;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	/**
	 * Attempts to set a new language with the given {@code name} and {@code code} to the given
	 * {@link LanguageSet} as the source language in this DialogueBranch project. This method
	 * will succeed and return {@code true} if and only if a language with the given {@code code}
	 * does not exist yet in the {@link LanguageMap} of this DialogueBranch project.
	 *
	 * @param name the name of the source language to add.
	 * @param code the code of the source language to add.
	 * @param languageSet the language set to which to add the language
	 * @throws DuplicateLanguageCodeException in case a language with the given {@code code}
	 *                                           already exists in this DialogueBranch project.
	 */
	public void setSourceLanguage(String name, String code, LanguageSet languageSet)
			throws DuplicateLanguageCodeException {
		if(languageExists(code))
			throw new DuplicateLanguageCodeException("A language with the given language " +
					"code '"+code+"' is already defined in this DialogueBranch project.",code);

		languageSet.setSourceLanguage(new Language(name,code));
	}

	/**
	 * Attempts to add a new source language to this DialogueBranch project with the given
	 * {@code name} and {@code code} by creating a new {@link LanguageSet} for it. This method
	 * will fail with a {@link DuplicateLanguageCodeException} if a language with the given
	 * {@code code} already exists in this DialogueBranch project. Otherwise, it will return a
	 * pointer to the newly created {@link LanguageSet}.
	 *
	 * @param name the name of the source language to add.
	 * @param code the code of the source language to add.
	 * @throws DuplicateLanguageCodeException in case a language with the given {@code code}
	 *                                           already exists in this DialogueBranch project.
	 * @return the newly created {@link LanguageSet}
	 */
	public LanguageSet addSourceLanguage(String name, String code)
			throws DuplicateLanguageCodeException {
		if(languageExists(code))
			throw new DuplicateLanguageCodeException("A language with the given language " +
					"code '"+code+"' is already defined in this DialogueBranch project.",code);

		LanguageSet languageSet = new LanguageSet(new Language(name, code));
		languageMap.addLanguageSet(languageSet);
		return languageSet;
	}

	/**
	 * Attempts to add a new language with the given {@code name} and {@code code} to the given
	 * {@link LanguageSet} as a translation language. This method will succeed and return
	 * {@code true} if and only if a language with the given {@code code} does not exist yet
	 * in this {@link LanguageMap}.
	 *
	 * @param name the name of the language to add.
	 * @param code the code of the language to add.
	 * @param languageSet the language set to which to add the language
	 * @throws DuplicateLanguageCodeException in case a language with the given {@code code}
	 *                                           already exists in this DialogueBranch project.
	 */
	public void addTranslationLanguage(String name, String code, LanguageSet languageSet)
			throws DuplicateLanguageCodeException {
		if(languageExists(code))
			throw new DuplicateLanguageCodeException("A language with the given language " +
					"code '"+code+"' is already defined in this DialogueBranch project.",code);

		languageSet.addTranslationLanguage(new Language(name,code));
	}

	/**
	 * Checks whether a language with the given {@code languageCode} exists in this
	 * {@link LanguageMap}.
	 * @param languageCode the language code to search for
	 * @return true if the given {@code languageCode} exists, false otherwise
	 */
	public boolean languageExists(String languageCode) {
		for(LanguageSet languageSet : languageMap.getLanguageSets()) {
			if(languageSet.getSourceLanguage().getCode().equals(languageCode)) return true;
			for(Language translationLanguage : languageSet.getTranslationLanguages()) {
				if(translationLanguage.getCode().equals(languageCode)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link LanguageSet} in this DialogueBranch project for which the source
	 * language code matches the given {@code code}.
	 *
	 * @param sourceLanguageCode the language code of the source language for which to look up its
	 *                           {@link LanguageSet}.
	 * @return the {@link LanguageSet} with a source language with the given {@code code}.
	 * @throws UnknownLanguageCodeException if no language set exists with the given source
	 *                                         language code.
	 */
	public LanguageSet getLanguageSetForSourceLanguage(String sourceLanguageCode)
			throws UnknownLanguageCodeException {
		for(LanguageSet languageSet : languageMap.getLanguageSets()) {
			if(languageSet.getSourceLanguage().getCode().equals(sourceLanguageCode))
				return languageSet;
		}
		throw new UnknownLanguageCodeException("No language set found with source language '"
				+sourceLanguageCode+"'.",sourceLanguageCode);
	}

	/**
	 * Returns a list of language codes representing all the supported languages in this Dialogue
	 * Branch project.
	 * @return the list of all language codes in this project.
	 */
	public List<String> getSupportedLanguages() {
		List<String> result = new ArrayList<>();

		if(languageMap != null) {
			List<LanguageSet> languageSets = languageMap.getLanguageSets();
			if(languageSets != null) {
				for(LanguageSet languageSet : languageSets) {
					Language sourceLanguage = languageSet.getSourceLanguage();
					if(sourceLanguage != null) {
						String sourceLanguageCode = sourceLanguage.getCode();
						if(sourceLanguageCode != null) {
							if (!result.contains(sourceLanguageCode))
								result.add(sourceLanguageCode);
						}
					}
					List<Language> translationLanguages = languageSet.getTranslationLanguages();
					if(translationLanguages != null) {
						for(Language translationLanguage : translationLanguages) {
							String translationLanguageCode = translationLanguage.getCode();
							if(translationLanguageCode != null) {
								if (!result.contains(translationLanguageCode))
									result.add(translationLanguageCode);
							}
						}
					}
				}
			}
		}
		return result;
	}

	public String toString() {
		String result = "";
		result += "DialogueBranch Project Metadata:\n";
		result += "[name:"+name+"]\n";
		result += "[basePath:"+basePath+"]\n";
		result += "[description:"+description+"]\n";
		result += "[version:"+version+"]\n";
		if(languageMap != null)
			result += languageMap.toString();
		return result;
	}

	// ------------------------------------------------------
	// -------------------- XML Handling --------------------
	// ------------------------------------------------------

	/**
	 * Writes this {@link ProjectMetaData} to file using the given {@link XMLWriter}.
	 *
	 * @param writer the XML writer
	 * @throws IOException if a writing error occurs
	 */
	public void writeXML(XMLWriter writer) throws IOException {
		writer.writeStartElement("dlb-project");
		writer.writeAttribute("name",name);
		writer.writeAttribute("version",version);

		writer.writeStartElement("description");
		writer.writeCharacters(description);
		writer.writeEndElement(); // description

		languageMap.writeXML(writer);

		writer.writeEndElement(); // dlb-project
		writer.close();
	}

	/**
	 * Returns a {@link SimpleSAXHandler} that is able to parse the contents of an .xml file
	 * to a {@link ProjectMetaData} object.
	 *
	 * @return the XMl handler
	 */
	public static SimpleSAXHandler<ProjectMetaData> getXMLHandler() {
		return new XMLHandler();
	}

	/**
	 * TODO: Test error handling.
	 * TODO: Check for duplicate languages.
	 */
	private static class XMLHandler extends AbstractSimpleSAXHandler<ProjectMetaData> {

		private ProjectMetaData result;
		private int rootLevel = 0;
		private boolean inDescription = false;
		private SimpleSAXHandler<LanguageMap> languageMapHandler = null;

		@Override
		public void startElement(String name, Attributes attributes, List<String> parents) throws ParseException {

			if(rootLevel == 0) {
				if(!name.equals("dlb-project")) {
					throw new ParseException("Expected element 'dlb-project' while parsing DialogueBranch project metadata, found '"+name+"'.");
				} else {
					result = new ProjectMetaData();
					if(attributes.getValue("name") == null) {
						throw new ParseException("Missing attribute 'name' in element 'dlb-project' while parsing DialogueBranch project metadata.");
					} else {
						result.setName(attributes.getValue("name"));
					}
					if(attributes.getValue("version") != null) {
						result.setVersion(attributes.getValue("version"));
					} else {
						result.setVersion("");
					}
					rootLevel++;
				}
			} else if(rootLevel == 1) {
				if(name.equals("description")) {
					inDescription = true;
				} else if(name.equals("language-map")) {
					languageMapHandler = LanguageMap.getXMLHandler();
					languageMapHandler.startElement(name,attributes,parents);
				} else {
					if(languageMapHandler != null) {
						languageMapHandler.startElement(name,attributes,parents);
					} else {
						throw new ParseException("Unexpected element while parsing DialogueBranch project metadata: '"+name+"'");
					}
				}
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageMapHandler != null) {
				languageMapHandler.endElement(name,parents);
			} else if (name.equals("description")) inDescription = false;

			if(name.equals("language-map") && languageMapHandler != null) {
				result.setLanguageMap(languageMapHandler.getObject());
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {
			if(inDescription) {
				result.setDescription(ch);
			}
		}

		@Override
		public ProjectMetaData getObject() {
			return result;
		}
	}
}
