/**
 *
 */
package eu.learnpad.simulator.uihandler.formhandler.activiti2formaas;

/*
 * #%L
 * LearnPAd Simulator
 * %%
 * Copyright (C) 2014 - 2015 Linagora
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.FormService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;

import eu.learnpad.simulator.uihandler.IFormHandler;

/**
 * Implements transformation between activiti tasks and <a
 * href="https://github.com/joshfire/jsonform">JsonForm</a> forms
 *
 * @author jorquera
 *
 */
public class ActivitiToFormaasFormHandler implements IFormHandler {

	// these properties are used to prefix special form properties used to map
	// roles to users. Hopefully no process will use properties with these
	// keys....
	private static final String SINGLE_USER_KEY_PREFIX = "learnpad_single_role ";
	private static final String GROUP_USER_KEY_PREFIX = "learnpad_group_role ";

	private final FormService activitiFormService;

	/**
	 * @param activitiFormService
	 */
	public ActivitiToFormaasFormHandler(FormService activitiFormService) {
		super();
		this.activitiFormService = activitiFormService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see activitipoc.IFormHandler#createFormString(java.lang.String)
	 */
	public String createFormString(String taskId) {
		FormData data = activitiFormService.getTaskFormData(taskId);
		return formDataToJSON(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see activitipoc.IFormHandler#createStartingFormString(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see activitipoc.IFormHandler#createStartingFormString(java.lang.String,
	 * java.util.Collection, java.util.Collection, java.util.Collection)
	 */
	public String createStartingFormString(String processId,
			Collection<String> singleRoles, Collection<String> groupRoles,
			Collection<String> users) {
		StartFormData data = activitiFormService.getStartFormData(processId);

		String res = formDataToJSON(data);

		// insert roles to users mapping
		JSONObject tmp = new JSONObject(res);
		int i = tmp.length();

		JSONArray userArray = new JSONArray(users);

		for (String role : singleRoles) {
			JSONObject o = new JSONObject();
			o.put("fieldType", "Multiple Radios");

			JSONObject fv = new JSONObject();

			fv.put("name", role);
			fv.put("label", SINGLE_USER_KEY_PREFIX + role);
			fv.put("radios", userArray);

			o.put("fieldValues", fv);

			tmp.put("field" + i, o);
			i++;

		}

		for (String role : groupRoles) {
			JSONObject o = new JSONObject();
			o.put("fieldType", "Multiple Checkboxes");

			JSONObject fv = new JSONObject();

			fv.put("name", role);
			fv.put("label", GROUP_USER_KEY_PREFIX + role);
			fv.put("checkboxes", userArray);

			o.put("fieldValues", fv);

			tmp.put("field" + i, o);
			i++;
		}

		return tmp.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see activitipoc.IFormHandler#parseResult(java.lang.String)
	 */
	public FormResult parseResult(String data) {
		final Map<String, Object> parameters = new HashMap<String, Object>();
		final Map<String, Collection<String>> routes = new HashMap<String, Collection<String>>();

		// remove beginning and ending " as well as \
		data = data.substring(1, data.length() - 1).replaceAll("\\\\", "");

		System.out.println(data);
		JSONObject jObject = ((JSONObject) (new JSONArray(data).get(0)))
				.getJSONObject("model");

		Iterator<?> keys = jObject.keys();
		while (keys.hasNext()) {
			// TODO dangerous cast
			String key = (String) keys.next();

			// separate properties from roles
			if (jObject.getJSONObject(key).getString("title")
					.startsWith(SINGLE_USER_KEY_PREFIX)) {
				routes.put(
						jObject.getJSONObject(key).getString("title")
								.replaceFirst(SINGLE_USER_KEY_PREFIX, ""),
						Arrays.asList(jObject.getJSONObject(key)
								.getJSONArray("value").getString(0)
								.replaceAll("[\\[\\]\"\"]", "")));
			} else if (jObject.getJSONObject(key).getString("title")
					.startsWith(GROUP_USER_KEY_PREFIX)) {
				Set<String> users = new HashSet<String>();
				JSONArray usersJ = jObject.getJSONObject(key).getJSONArray(
						"value");
				for (int i = 0; i < usersJ.length(); i++) {
					users.add(usersJ.getString(i)
							.replaceAll("[\\[\\]\"\"]", ""));
				}
				routes.put(jObject.getJSONObject(key).getString("title")
						.replaceFirst(GROUP_USER_KEY_PREFIX, ""), users);
			} else {
				JSONObject prop = jObject.getJSONObject(key);
				// clean properties
				parameters.put(prop.getString("title"), prop.getString("value")
						.replaceAll("[\\[\\]\"\"]", ""));
			}

		}

		return new FormResult() {

			public Map<String, Collection<String>> getRolesToUsersMapping() {
				return routes;
			}

			public Map<String, Object> getProperties() {
				return parameters;
			}
		};
	}

	private String formDataToJSON(FormData data) {

		String res = "{";

		res += "\"field0\": { \"fieldType\": \"Form Name\",";
		res += "\"fieldValues\": { \"name\": \"Form Data\" } }";

		int i = 1;

		for (FormProperty prop : data.getFormProperties()) {

			res += ", \"field" + i + "\": {";
			res += "\"fieldType\": " + getType(prop) + ", ";

			res += "\"fieldValues\" : {";
			if (prop.getType().getName().equals("enum")) {
				res += "\"name\": \"" + prop.getId() + "\", ";
			} else {
				res += "\"id\": \"" + prop.getId() + "\", ";
				res += "\"inputsize\": \"input-xlarge\", ";
				res += "\"required\": " + getRequired(prop) + ", ";
				res += "\"helptext\": \"" + prop.getName() + "\", ";
			}

			res += "\"label\": \"" + getTitle(prop) + "\" ";

			if (prop.getType().getName().equals("enum")
					|| prop.getType().getName().equals("boolean")) {
				res += ", \"radios\": " + getEnum(prop);
			}

			res += "}}";

			i++;
		}

		res = res + " }";

		return res;
	}

	private static String getType(FormProperty prop) {
		String res = "";

		String type = prop.getType().getName();

		if (type.equals("string")) {
			res += "\"Text Input\"";
		} else if (type.equals("long")) {
			res += "\"Text Input\"";
		} else if (type.equals("enum")) {
			res += "\"Multiple Radios\"";
		} else if (type.equals("date")) {
			res += "\"Text Input\"";
		} else if (type.equals("boolean")) {
			res += "\"Multiple Radios\"";
		}

		return res;
	}

	private static String getTitle(FormProperty prop) {
		return prop.getId();
	}

	private static String getRequired(FormProperty prop) {
		if (prop.isRequired()) {
			return "true";
		} else {
			return "false";
		}
	}

	private static String getEnum(FormProperty prop) {
		String type = prop.getType().getName();

		String res = "";
		if (type.equals("enum")) {
			// add radio buttons
			res += "[ ";

			@SuppressWarnings("unchecked")
			Map<String, String> values = (Map<String, String>) prop.getType()
					.getInformation("values");
			for (String key : values.keySet()) {
				res += "\"" + key + "\",";
			}
			// remove last comma (or space if empty)
			res = res.substring(0, res.length() - 1);
			res += " ]";
		} else if (type.equals("boolean")) {
			// add radio buttons
			res += ", \"radios\": [\"True\", \"False\"]";
		}

		return res;
	}

}
