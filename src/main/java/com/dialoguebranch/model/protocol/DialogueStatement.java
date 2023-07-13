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

package com.dialoguebranch.model.protocol;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.json.JsonMapper;
import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.command.DLBActionCommand;
import com.dialoguebranch.model.command.DLBInputCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used for dialogue statements that are sent to the client in the
 * web service protocol. It mirrors the statement segments in a {@link
 * DLBNodeBody}. The main difference is that any variables have
 * been resolved and "if" and "set" commands have been executed. There are three
 * types of segments:
 *
 * <ul>
 *   <li>{@link TextSegment TextSegment}: Corresponds to a {@link DLBNodeBody.TextSegment} with
 *   variables resolved.</li>
 *   <li>{@link ActionSegment}: Contains a {@link DialogueAction DialogueAction}, which corresponds
 *   to a {@link DLBActionCommand} with variables resolved. Action segments should not occur in
 *   statements that are part of a {@link ReplyMessage ReplyMessage}.</li>
 *   <li>{@link InputSegment InputSegment}: Corresponds to a {@link DLBInputCommand} with variables
 *   resolved.</li>
 * </ul>
 *
 * @author Dennis Hofs (RRD)
 */
public class DialogueStatement {
	private List<Segment> segments = new ArrayList<>();
	
	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}
	
	public void addTextSegment(String text) {
		TextSegment segment = new TextSegment();
		segment.setText(text);
		segments.add(segment);
	}
	
	public void addInputSegment(DLBInputCommand inputCommand) {
		InputSegment segment = new InputSegment();
		segment.setInputType(inputCommand.getType());
		segment.setDescription(inputCommand.getDescription());
		segment.setParameters(inputCommand.getParameters());
		segments.add(segment);
	}
	
	public void addActionSegment(DLBActionCommand actionCommand) {
		ActionSegment segment = new ActionSegment();
		segment.setAction(new DialogueAction(actionCommand));
		segments.add(segment);
	}

	public enum SegmentType {
		TEXT,
		INPUT,
		ACTION
	}

	@JsonDeserialize(using=SegmentDeserializer.class)
	public static abstract class Segment {
		private SegmentType segmentType;
		
		protected Segment(SegmentType segmentType) {
			this.segmentType = segmentType;
		}
		
		public SegmentType getSegmentType() {
			return segmentType;
		}
	}
	
	@JsonDeserialize(using=JsonDeserializer.None.class)
	public static class TextSegment extends Segment {
		private String text;

		public TextSegment() {
			super(SegmentType.TEXT);
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	
	@JsonDeserialize(using=InputSegmentDeserializer.class)
	@JsonSerialize(using=InputSegmentSerializer.class)
	public static class InputSegment extends Segment {
		private String inputType;
		private String description = null;
		private Map<String,?> parameters = new LinkedHashMap<>();

		public InputSegment() {
			super(SegmentType.INPUT);
		}

		/**
		 * Returns the input type. This should be one of the TYPE_* constants
		 * defined in {@link DLBInputCommand}.
		 *
		 * @return the input type
		 */
		public String getInputType() {
			return inputType;
		}

		/**
		 * Sets the input type. This should be one of the TYPE_* constants
		 * defined in {@link DLBInputCommand}.
		 *
		 * @param inputType the input type
		 */
		public void setInputType(String inputType) {
			this.inputType = inputType;
		}

		/**
		 * Returns the description of this input command. For example a client can
		 * use this in input validation messages ("You did not fill in [your
		 * name]."). The description is optional and may be null.
		 *
		 * @return the description or null
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Sets the description of this input command. For example a client can use
		 * this in input validation messages ("You did not fill in [your name].").
		 * The description is optional and may be null.
		 *
		 * @param description the description or null
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * Returns the parameters. This is a map from parameter names to values.
		 * A value can be any JSON type. Any variables in parameter values have
		 * already been resolved.
		 *
		 * @return the parameters
		 */
		public Map<String, ?> getParameters() {
			return parameters;
		}

		/**
		 * Sets the parameters. This is a map from parameter names to values. A
		 * value can be any JSON type. Any variables in parameter values have
		 * already been resolved.
		 *
		 * @param parameters the parameters
		 */
		public void setParameters(Map<String, ?> parameters) {
			this.parameters = parameters;
		}
	}
	
	@JsonDeserialize(using=JsonDeserializer.None.class)
	public static class ActionSegment extends Segment {
		private DialogueAction action;
		
		public ActionSegment() {
			super(SegmentType.ACTION);
		}

		public DialogueAction getAction() {
			return action;
		}

		public void setAction(DialogueAction action) {
			this.action = action;
		}
	}
	
	public static class SegmentDeserializer extends JsonDeserializer<Segment> {
		@Override
		public Segment deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			Map<?,?> map = p.readValueAs(Map.class);
			if (!map.containsKey("segmentType")) {
				throw new JsonParseException(p,
						"Property \"segmentType\" not found");
			}
			Object typeObj = map.remove("segmentType");
			if (!(typeObj instanceof String)) {
				throw new JsonParseException(p,
						"Invalid value of property \"segmentType\": " +
						typeObj);
			}
			String typeStr = (String)typeObj;
			SegmentType type;
			try {
				type = SegmentType.valueOf(typeStr);
			} catch (IllegalArgumentException ex) {
				throw new JsonParseException(p,
						"Invalid value of property \"segmentType\": " +
						typeStr);
			}
			ObjectMapper mapper = new ObjectMapper();
			switch (type) {
			case TEXT:
				return mapper.convertValue(map, TextSegment.class);
			case INPUT:
				return mapper.convertValue(map, InputSegment.class);
			case ACTION:
				return mapper.convertValue(map, ActionSegment.class);
			default:
				throw new JsonParseException(p, "Unsupported segment type: " +
						type);
			}
		}
	}

	public static class InputSegmentSerializer extends
			JsonSerializer<InputSegment> {
		@Override
		public void serialize(InputSegment value, JsonGenerator gen,
				SerializerProvider serializers) throws IOException {
			Map<String,Object> obj = new LinkedHashMap<>();
			obj.put("segmentType", value.getSegmentType());
			obj.put("inputType", value.getInputType());
			if (value.getDescription() != null)
				obj.put("description", value.getDescription());
			for (String param : value.getParameters().keySet()) {
				obj.put(param, value.getParameters().get(param));
			}
			gen.writeObject(obj);
		}
	}

	public static class InputSegmentDeserializer extends
			JsonDeserializer<InputSegment> {
		@Override
		public InputSegment deserialize(JsonParser p,
				DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			Map<?,?> rawMap = p.readValueAs(Map.class);
			Map<String, ?> map;
			try {
				map = JsonMapper.convert(rawMap,
						new TypeReference<Map<String, ?>>() {});
			} catch (ParseException ex) {
				throw new JsonParseException(p, "Object keys are not strings");
			}
			map.remove("segmentType");
			InputSegment segment = new InputSegment();
			if (!map.containsKey("inputType")) {
				throw new JsonParseException(p,
						"Property \"inputType\" not found");
			}
			Object typeObj = map.remove("inputType");
			if (!(typeObj instanceof String)) {
				throw new JsonParseException(p,
						"Invalid value of property \"inputType\": " +
						typeObj);
			}
			String typeStr = (String)typeObj;
			segment.setInputType(typeStr);
			Object descrObj = map.remove("description");
			if (descrObj != null) {
				if (!(descrObj instanceof String)) {
					throw new JsonParseException(p,
							"Invalid value of property \"description\": " +
							descrObj);
				}
				segment.setDescription((String)descrObj);
			}
			segment.setParameters(map);
			return segment;
		}
	}
}
