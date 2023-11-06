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

import com.dialoguebranch.exception.DLBNodeParseException;
import com.dialoguebranch.model.DLBDialogue;
import com.dialoguebranch.model.DLBNode;
import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBNodeHeader;
import com.dialoguebranch.model.nodepointer.DLBNodePointerInternal;
import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.io.LineColumnNumberReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DLBParser implements AutoCloseable {
	public static final String NODE_NAME_REGEX = "[A-Za-z0-9_-]+";
	public static final String DIALOGUE_NAME_REGEX =
			"(" + NODE_NAME_REGEX + "/)*" + NODE_NAME_REGEX;
	public static final String EXTERNAL_NODE_POINTER_REGEX =
			"/?" + "((..)|(" + NODE_NAME_REGEX + ")/)*" + NODE_NAME_REGEX +
			"\\." + NODE_NAME_REGEX;
	
	private String dialogueName;
	private LineColumnNumberReader reader;
	
	private DLBDialogue dialogue = null;
	private List<DLBNodeState.NodePointerToken> nodePointerTokens = null;
	
	public DLBParser(String filename) throws FileNotFoundException {
		this(new File(filename));
	}
	
	public DLBParser(File file) throws FileNotFoundException {
		init(file);
	}
	
	public DLBParser(String dialogueName, InputStream input) {
		init(dialogueName, input);
	}
	
	public DLBParser(String dialogueName, Reader reader) {
		init(dialogueName, new LineColumnNumberReader(reader));
	}
	
	private void init(File file) throws FileNotFoundException {
		String name = file.getName();
		int extSep = name.lastIndexOf('.');
		if (extSep != -1)
			name = name.substring(0, extSep);
		init(name, new FileInputStream(file));
	}
	
	private void init(String dialogueName, InputStream input) {
		init(dialogueName, new InputStreamReader(input,
				StandardCharsets.UTF_8));
	}
	
	private void init(String dialogueName, Reader reader) {
		this.dialogueName = dialogueName;
		if (reader instanceof LineColumnNumberReader) {
			this.reader = (LineColumnNumberReader)reader;
		} else if (reader instanceof BufferedReader) {
			this.reader = new LineColumnNumberReader(reader);
		} else {
			this.reader = new LineColumnNumberReader(
					new BufferedReader(reader));
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Tries to read the dialogue file. If a reading error occurs, it throws an {@link IOException}.
	 * Otherwise, it returns a result object where either the dialogue is set, or one or more parse
	 * errors are set.
	 * 
	 * @return the read result
	 * @throws IOException if a reading error occurs
	 */
	public DLBParserResult readDialogue() throws IOException {
		DLBParserResult result = new DLBParserResult();
		if (!dialogueName.matches(DIALOGUE_NAME_REGEX)) {
			result.getParseErrors().add(new ParseException(
					"Invalid dialogue name: " + dialogueName));
		}
		DLBDialogue dialogue = new DLBDialogue(dialogueName);
		this.dialogue = dialogue;
		nodePointerTokens = new ArrayList<>();
		boolean foundNodeError = false;
		ReadDLBNodeResult readResult;
		while ((readResult = readNode()) != null) {
			if (readResult.node != null) {
				dialogue.addNode(readResult.node);
			} else {
				foundNodeError = true;
				result.getParseErrors().add(readResult.parseException);
				if (!readResult.readNodeEnd)
					moveToNextNode();
			}
		}
		if (foundNodeError)
			return result;
		if (!dialogue.nodeExists("Start")) {
			result.getParseErrors().add(new LineNumberParseException(
					"Node with title \"Start\" not found",
					reader.getLineNum(), reader.getColNum()));
		}
		for (DLBNodeState.NodePointerToken pointerToken : nodePointerTokens) {
			if (!(pointerToken.pointer() instanceof DLBNodePointerInternal))
				continue;
			DLBNodePointerInternal pointer =
					(DLBNodePointerInternal)pointerToken.pointer();
			if (dialogue.nodeExists(pointer.getNodeId()))
				continue;
			BodyToken token = pointerToken.token();
			LineNumberParseException parseEx = new LineNumberParseException(
					"Found reply with pointer to non-existing node: " +
					pointer.getNodeId(), token.getLineNumber(), token.getColNumber());
			result.getParseErrors().add(createDLBNodeParseException(
					pointerToken.nodeTitle(), parseEx));
		}
		if (!result.getParseErrors().isEmpty())
			return result;
		result.setDialogue(dialogue);
		this.dialogue = null;
		nodePointerTokens = null;
		return result;
	}
	
	private static class ReadDLBNodeResult {
		public DLBNode node = null;
		public DLBNodeParseException parseException = null;
		public boolean readNodeEnd = false;
	}
	
	/**
	 * Tries to read the next node. The reader should be positioned at the start of a node. If there
	 * are no more nodes, this method returns null. If a reading error occurs, it throws an
	 * {@link IOException}. Otherwise, it returns a result object, where either "node" or
	 * "parseException" is set. The property "readNodeEnd" is set if the end of the node (===) has
	 * been read. This can be used to skip to the next node in case of a parse exception.
	 * 
	 * @return the result or null
	 * @throws IOException if a reading error occurs
	 */
	private ReadDLBNodeResult readNode() throws IOException {
		ReadDLBNodeResult result = new ReadDLBNodeResult();
		DLBNodeState nodeState = new DLBNodeState(dialogueName);
		try {
			boolean inHeader = true;
			Map<String,String> headerMap = new LinkedHashMap<>();
			int lineNum = reader.getLineNum();
			String line = readLine();
			while (line != null && inHeader) {
				if (getContent(line).equals("===")) {
					result.readNodeEnd = true;
					throw new LineNumberParseException(
							"End of header not found", lineNum, 1);
				} else if (getContent(line).equals("---")) {
					inHeader = false;
				} else {
					parseHeaderLine(headerMap, line, lineNum, nodeState);
					lineNum = reader.getLineNum();
					line = readLine();
				}
			}
			if (inHeader) {
				if (nodeState.getTitle() == null &&
						nodeState.getSpeaker() == null && headerMap.isEmpty()) {
					return null;
				}
				throw new LineNumberParseException(
						"Found incomplete node at end of file",
						reader.getLineNum(), reader.getColNum());
			}
			DLBNodeHeader header = createHeader(headerMap, lineNum, nodeState);
			boolean inBody = true;
			BodyTokenizer tokenizer = new BodyTokenizer();
			lineNum = reader.getLineNum();
			line = readLine();
			List<BodyToken> bodyTokens = new ArrayList<>();
			while (line != null && inBody) {
				if (getContent(line).equals("===")) {
					inBody = false;
					result.readNodeEnd = true;
				} else {
					bodyTokens.addAll(tokenizer.readBodyTokens(line + "\n",
							lineNum));
					lineNum = reader.getLineNum();
					line = readLine();
				}
			}
			BodyParser bodyParser = new BodyParser(nodeState);
			DLBNodeBody body = bodyParser.parse(bodyTokens, Arrays.asList(
					"action", "if", "random", "set"));
			if (header.getTitle().equalsIgnoreCase("end"))
				validateEndNode(header, body, bodyTokens);
			nodePointerTokens.addAll(nodeState.getNodePointerTokens());
			result.node = new DLBNode(header, body);
			return result;
		} catch (LineNumberParseException ex) {
			result.parseException = createDLBNodeParseException(
					nodeState.getTitle(), ex);
			return result;
		}
	}
	
	private void validateEndNode(DLBNodeHeader header, DLBNodeBody body,
								 List<BodyToken> tokens) throws LineNumberParseException {
		if (body.getSegments().isEmpty() && body.getReplies().isEmpty())
			return;
		BodyToken token = tokens.get(0);
		throw new LineNumberParseException(String.format(
				"Node \"%s\" must have an empty body", header.getTitle()),
				token.getLineNumber(), token.getColNumber());
	}
	
	/**
	 * Creates a {@link DLBNodeParseException} with message "Error in node ..." If the
	 * node title is unknown, it can be set to null.
	 * 
	 * @param nodeTitle the node title or null
	 * @param ex the parse error
	 * @return the DLBNodeParseException
	 */
	private DLBNodeParseException createDLBNodeParseException(
			String nodeTitle, LineNumberParseException ex) {
		String msg = "Error in node";
		if (nodeTitle != null)
			msg += " " + nodeTitle;
		return new DLBNodeParseException(msg + ": " + ex.getMessage(),
				nodeTitle, ex);
	}
	
	private void moveToNextNode() throws IOException {
		String line;
		while ((line = readLine()) != null) {
			if (getContent(line).equals("==="))
				return;
		}
	}

	private void parseHeaderLine(Map<String,String> headerMap, String line,
			int lineNum, DLBNodeState nodeState)
			throws LineNumberParseException {
		int commentSep = line.indexOf("//");
		if (commentSep != -1)
			line = line.substring(0, commentSep);
		if (line.trim().isEmpty())
			return;
		int sep = line.indexOf(':');
		if (sep == -1) {
			throw new LineNumberParseException(
					"Character : not found in header line", lineNum, 1);
		}
		String keyUntrimmed = line.substring(0, sep);
		String key = keyUntrimmed.trim();
		int keyIndex = 1;
		if (!key.isEmpty())
			keyIndex += skipWhitespace(keyUntrimmed, 0);
		String valueUntrimmed = line.substring(sep + 1);
		String value = valueUntrimmed.trim();
		int valueCol = sep + 2 + skipWhitespace(valueUntrimmed, 0);
		if (key.length() == 0) {
			throw new LineNumberParseException("Found empty header name",
					lineNum, 0);
		}
		if (headerMap.containsKey(key)) {
			throw new LineNumberParseException("Found duplicate header: " + key,
					lineNum, keyIndex);
		}
		if (key.equals("title")) {
			if (!value.matches(NODE_NAME_REGEX)) {
				throw new LineNumberParseException(
						"Invalid node title: " + value, lineNum, valueCol);
			}
			if (dialogue.nodeExists(value)) {
				throw new LineNumberParseException(
						"Found duplicate node title: " + value, lineNum,
						valueCol);
			}
			nodeState.setTitle(value);
		} else if (key.equals("speaker")) {
			nodeState.setSpeaker(value);
			nodeState.setSpeakerLine(lineNum);
			nodeState.setSpeakerColumn(valueCol);
		} else {
			headerMap.put(key, value);
		}
	}
	
	private DLBNodeHeader createHeader(Map<String,String> headerMap,
									   int lineNum, DLBNodeState nodeState)
			throws LineNumberParseException {
		String title = nodeState.getTitle();
		if (title == null) {
			throw new LineNumberParseException(
					"Required header \"title\" not found",
					lineNum, 1);
		}
		String speaker = nodeState.getSpeaker();
		if (nodeState.getTitle().toLowerCase().equals("end")) {
			speaker = null;
		} else {
			if (speaker == null) {
				throw new LineNumberParseException(
						"Required header \"speaker\" not found",
						lineNum, 1);
			}
			if (speaker.length() == 0) {
				throw new LineNumberParseException("Found empty speaker",
						nodeState.getSpeakerLine(),
						nodeState.getSpeakerColumn());
			}
		}
		DLBNodeHeader header = new DLBNodeHeader(title, headerMap);
		header.setSpeaker(speaker);
		return header;
	}
	
	/**
	 * Removes a possible comment, and leading and trailing white space from a
	 * string. This should not be used for body lines, because this method does
	 * not check whether a comment marker (//) is inside a string literal.
	 *
	 * @param s the string or null
	 * @return the content or null
	 */
	private String getContent(String s) {
		if (s == null)
			return null;
		int commentIndex = s.indexOf("//");
		if (commentIndex != -1)
			s = s.substring(0, commentIndex);
		return s.trim();
	}
	
	/**
	 * Reads whitespace characters from the specified index and returns the
	 * number of characters read.
	 * 
	 * @param s the string
	 * @param start the start index
	 * @return the number of whitespace characters
	 */
	public static int skipWhitespace(String s, int start) {
		int result = 0;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c))
				return result;
			result++;
		}
		return result;
	}
	
	private String readLine() throws IOException {
		StringBuilder builder = new StringBuilder();
		boolean foundCR = false;
		while (true) {
			if (foundCR) {
				Object restoreState = reader.getRestoreState(1);
				int c = reader.read();
				if (c == -1 || c == '\n')
					reader.clearRestoreState(restoreState);
				else
					reader.restoreState(restoreState);
				return builder.toString();
			}
			int c = reader.read();
			if (c == -1) {
				if (builder.length() == 0)
					return null;
				else
					return builder.toString();
			} else if (c == '\n') {
				return builder.toString();
			} else if (c == '\r') {
				foundCR = true;
			} else {
				builder.append((char)c);
			}
		}
	}
	
	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java " + DLBParser.class.getName() + " [options] <dialogue-branch-file>");
		System.out.println("    Parse a .dlb file and print a summary of the dialogue");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("-h -? --help");
		System.out.println("    Print this usage message");
	}

	public static void main(String[] args) {
		String filename = null;
		int i = 0;
		while (i < args.length) {
			String arg = args[i++];
			if (arg.equals("-h") || arg.equals("-?") || arg.equals("--help")) {
				showUsage();
				return;
			} else {
				filename = arg;
			}
		}
		if (filename == null) {
			showUsage();
			System.exit(1);
			return;
		}
		File file = new File(filename);
		if (!file.exists()) {
			System.err.println("ERROR: File not found: " + filename);
			System.exit(1);
			return;
		}
		try {
			file = file.getCanonicalFile();
		} catch (IOException ex) {}
		if (!file.isFile()) {
			System.err.println("ERROR: Path is not a file: " + file.getAbsolutePath());
			System.exit(1);
			return;
		}
		DLBParserResult readResult;
		try {
			DLBParser parser = new DLBParser(file);
			readResult = parser.readDialogue();
		} catch (IOException ex) {
			System.err.println("ERROR: Can't read file: " +
					file.getAbsolutePath() + ": " + ex.getMessage());
			System.exit(1);
			return;
		}
		if (!readResult.getParseErrors().isEmpty()) {
			System.err.println("ERROR: Failed to parse file: " +
					file.getAbsolutePath());
			for (ParseException ex : readResult.getParseErrors()) {
				System.err.println(ex.getMessage());
			}
			System.exit(1);
			return;
		}
		System.out.println("Finished parsing dialogue from file: " + file.getAbsolutePath());
		System.out.println(readResult.getDialogue());
	}
}
