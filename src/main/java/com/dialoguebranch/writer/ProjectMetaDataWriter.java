package com.dialoguebranch.writer;

import com.dialoguebranch.model.Language;
import com.dialoguebranch.model.LanguageMap;
import com.dialoguebranch.model.LanguageSet;
import com.dialoguebranch.model.ProjectMetaData;
import nl.rrd.utils.xml.XMLWriter;

import java.io.IOException;

public class ProjectMetaDataWriter {

    /**
     * Writes this {@link ProjectMetaData} to file using the given {@link XMLWriter}.
     *
     * @param writer the XML writer
     * @throws IOException if a writing error occurs
     */
    public static void writeToXMLFile(XMLWriter writer, ProjectMetaData projectMetaData) throws IOException {
        writer.writeStartElement("dlb-project");
        writer.writeAttribute("name",projectMetaData.getName());
        writer.writeAttribute("version",projectMetaData.getVersion());

        writer.writeStartElement("description");
        writer.writeCharacters(projectMetaData.getDescription());
        writer.writeEndElement(); // description

        writeLanguageMapXML(writer,projectMetaData.getLanguageMap());

        writer.writeEndElement(); // dlb-project
        writer.close();
    }

    public static void writeLanguageMapXML(XMLWriter writer, LanguageMap languageMap) throws IOException {
        writer.writeStartElement("language-map");

        for(LanguageSet languageSet : languageMap.getLanguageSets()) {
            writeLanguageSetXML(writer,languageSet);
        }

        writer.writeEndElement(); // language-map
    }

    public static void writeLanguageSetXML(XMLWriter writer, LanguageSet languageSet) throws IOException {
        writer.writeStartElement("language-set");

        writer.writeStartElement("source-language");
        writer.writeAttribute("name",languageSet.getSourceLanguage().getName());
        writer.writeAttribute("code",languageSet.getSourceLanguage().getCode());
        writer.writeEndElement(); // source-language

        for(Language language : languageSet.getTranslationLanguages()) {
            writer.writeStartElement("translation-language");
            writer.writeAttribute("name",language.getName());
            writer.writeAttribute("code",language.getCode());
            writer.writeEndElement();
        }

        writer.writeEndElement(); // language-set
    }

}
