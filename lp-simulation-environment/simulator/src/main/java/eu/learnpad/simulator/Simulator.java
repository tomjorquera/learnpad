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

import java.util.ArrayList;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

import eu.learnpad.simulator.IProcessEventReceiver.IProcessEventReceiverProvider;
import eu.learnpad.simulator.IProcessManager.IProcessManagerProvider;
import eu.learnpad.simulator.IRobotHandler.IRobotHandlerProvider;
import eu.learnpad.simulator.monitoring.EventDispatcherImpl;
import eu.learnpad.simulator.monitoring.activiti.ProbeEventReceiver;
import eu.learnpad.simulator.processmanager.activiti.ActivitiProcessManager;
import eu.learnpad.simulator.robot.IRobotFactory;
import eu.learnpad.simulator.robot.RobotUserEventReceiverWrapper;
import eu.learnpad.simulator.robot.activiti.ActivitiRobotInputExtractor;
import eu.learnpad.simulator.robot.activiti.simplerobot.SimpleRobotFactory;
import eu.learnpad.simulator.uihandler.formhandler.multi2jsonform.Multi2JsonFormFormHandler;
import eu.learnpad.simulator.uihandler.webserver.UIHandlerWebImpl;
import eu.learnpad.simulator.uihandler.webserver.WebServer;
import eu.learnpad.simulator.utils.BPMNExplorerRepository;

/**
 *
 */

/**
 * @author Tom Jorquera - Linagora
 *
 */
public class Simulator implements IProcessManagerProvider,
		IProcessEventReceiverProvider, IRobotHandlerProvider {

	private final IProcessManager processManager;
	private final UIHandlerWebImpl uiHandler;
	private final ProcessEngine processEngine;

	private final EventDispatcherImpl eventDispatcher;

	private final RobotUserEventReceiverWrapper<Map<String, Object>> robotEventReceiver;
	private final IRobotFactory<Map<String, Object>, Map<String, Object>> robotFactory;

	public Simulator(String activitiConfigPath, int webserverPort,
			boolean monitoringEnabled) throws Exception {

		// launch activiti process engine
		ProcessEngineConfiguration config = ProcessEngineConfiguration
				.createProcessEngineConfigurationFromInputStream(Main.class
						.getClassLoader().getResourceAsStream(
								activitiConfigPath));

		processEngine = config.buildProcessEngine();

		// create BPMN Explorer
		BPMNExplorerRepository explorerRepo = new BPMNExplorerRepository(
				processEngine.getRepositoryService());

		// create process manager
		processManager = new ActivitiProcessManager(processEngine, this,
				explorerRepo);

		// create users ui handler
		uiHandler = new UIHandlerWebImpl(new WebServer(webserverPort, "ui",
				"tasks", this), new ArrayList<String>(), this,
				new Multi2JsonFormFormHandler(explorerRepo, processEngine
						.getTaskService(), processEngine.getFormService()));

		// handle robots
		robotFactory = new SimpleRobotFactory(
				processEngine.getRepositoryService(),
				processEngine.getTaskService(), processEngine.getFormService());

		robotEventReceiver = new RobotUserEventReceiverWrapper<Map<String, Object>>(
				robotFactory, new ActivitiRobotInputExtractor(
						processEngine.getTaskService()), processManager);

		// manage events subscriptions
		eventDispatcher = new EventDispatcherImpl();
		eventDispatcher.subscribe(uiHandler);

		// add monitoring probe
		if (monitoringEnabled) {
			// register a probe to monitor events
			eventDispatcher.subscribe(new ProbeEventReceiver(processManager));
		}

		// note that the robot receiver is subscribed at the end in order to be
		// executed last
		// (this is important as the robots tend to complete tasks *very fast*)
		eventDispatcher.subscribe(robotEventReceiver);

	}

	public Simulator(String activitiConfigPath, int webserverPort)
			throws Exception {
		this(activitiConfigPath, webserverPort, true);
	}

	public void stop() {
		processEngine.close();
		uiHandler.stop();
	}

	public IUserHandler userHandler() {
		return uiHandler;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * eu.learnpad.simulator.IProcessEventReceiver.IProcessEventReceiverProvider
	 * #processEventReceiver()
	 */
	public IProcessEventReceiver processEventReceiver() {
		return eventDispatcher;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see eu.learnpad.simulator.IUserEventReceiver.IUserEventReceiverProvider#
	 * userEventReceiver()
	 */
	public IProcessManager processManager() {
		return processManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * eu.learnpad.simulator.IRobotHandler.IRobotHandlerProvider#robotHandler()
	 */
	@Override
	public IRobotHandler robotHandler() {
		return robotEventReceiver;
	}

}
