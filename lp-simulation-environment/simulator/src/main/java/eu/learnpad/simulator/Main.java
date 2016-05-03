/**
 *
 */
package eu.learnpad.simulator;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import eu.learnpad.sim.rest.data.UserData;
import eu.learnpad.simulator.uihandler.webserver.WebServer;

/**
 *
 * @author Tom Jorquera - Linagora
 *
 */
public class Main {

	public static final String ACTIVITY_CONFIG_PATH = "activiti.cfg.xml";
	public static final String DEMO_PROCESS_FOLDER = "process";

	public static final int PORT = 8081;

	// Global reference to the simulator
	// This is a little ugly, but only needed for JavaMsgTask. This class is
	// automatically instantiated by the Activiti engine and we cannot easily
	// pass a reference to it otherwise (at least not with the current
	// architecture).
	public static Simulator simulator;

	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("serial")
	public static void main(String[] args) throws IOException {

		try {

			simulator = new Simulator(ACTIVITY_CONFIG_PATH, PORT);

			// add users
			for (String user : Arrays.asList("sally", "barnaby", "robby")) {
				simulator.userHandler().addUser(
						new UserData(user, user, "", "", "", ""));
			}

			simulator.robotHandler().addRobot("robby");

			// load process definitions
			String processId = simulator
					.processManager()
					.addProjectDefinitions(
							DEMO_PROCESS_FOLDER + "/demo-suap-1.bpmn20.xml")
					.iterator().next();

			simulator.processManager().startProjectInstance(
					simulator.processManager().getProcessDefinitionKey(
							processId), new HashMap<String, Object>() {
						{
							put("entrepreneur", "test");
							put("document", "example_document_valid");

						}
					}, Arrays.asList("robby"),
					new HashMap<String, Collection<String>>() {
						{
							put("SUAP", Arrays.asList("robby"));
							put("otherOffice", Arrays.asList("robby"));
						}
					});

			System.out.println("---\n");
			System.out.println("Demo is ready and can be accessed at http://"
					+ WebServer.getIPAddress() + ":" + PORT);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("---\n");
			System.err
					.println("Sorry, cannot launch the demo (maybe it is already running?)");
			System.exit(-1);
		}
	}
}
