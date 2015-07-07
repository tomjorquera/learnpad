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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import eu.learnpad.sim.BridgeInterface;
import eu.learnpad.sim.rest.data.ProcessInstanceData;
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
	public static void main(String[] args) throws IOException {

		try {

			simulator = new Simulator(ACTIVITY_CONFIG_PATH, PORT);

			// load process definitions
			simulator.processManager().addProjectDefinitions(
					DEMO_PROCESS_FOLDER + "/titolounico.bpmn20.xml");

			simulator.processManager().addProjectDefinitions(
					DEMO_PROCESS_FOLDER + "/euproject.bpmn20.xml");

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

		// REST API demo

		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target("http://localhost:8081/");

		BridgeInterface bridge = target.proxy(BridgeInterface.class);

		InputStream is = bridge.getProcessResults("model1");
		Scanner st = new java.util.Scanner(is);
		Scanner s = st.useDelimiter("\\A");
		System.out.println(s.hasNext() ? s.next() : "");
		s.close();
		st.close();
		is.close();

		System.out.println(bridge.addProcessDefinition(new File(
				"src/main/resources/process/demo-suap-1.bpmn20.xml").toURI()
				.toURL().toString()));

		Collection<String> processes = bridge.getProcessDefinitions();

		System.out.println(processes);

		String processKey = processes.iterator().next();

		bridge.addProcessInstance(new ProcessInstanceData("", processKey,
				new HashMap<String, Object>() {
					{
						put("entrepreneur", "T");

					}
				}, Arrays.asList("barnaby", "sally"),
				new HashMap<String, Collection<String>>() {
					{
						put("SUAP", Arrays.asList("barnaby"));
						put("otherOffice", Arrays.asList("sally"));
					}
				}));

		bridge.getProcessInfos("nonexistant");
	}
}
