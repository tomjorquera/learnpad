/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package eu.learnpad.core.impl.me;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.rest.XWikiRestException;
import org.xwiki.rest.resources.attachments.AttachmentResource;

import eu.learnpad.core.rest.RestResource;
import eu.learnpad.core.rest.XWikiRestUtils;
import eu.learnpad.exception.impl.LpRestExceptionImpl;
import eu.learnpad.me.Controller;

@Component
@Named("eu.learnpad.core.impl.me.XwikiController")
@Path("/learnpad/me")
public class XwikiController extends Controller implements XWikiRestComponent {
	private String CORE_REPOSITORY_WIKI = "xwiki";
	private String CORE_REPOSITORY_SPACE = "CoreRepository";

	@Inject
	Logger logger;

	public XwikiController() {
		this(false);
	}

	public XwikiController(boolean isBridgeInterfaceLocal) {
		if (isBridgeInterfaceLocal)
			this.bridge = new XwikiBridgeInterface();
		else
			this.bridge = new XwikiBridgeInterfaceRestResource();
	}

	@Override
	public byte[] getFeedbacks(String modelSetId) throws LpRestExceptionImpl {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putModelSet(String modelSetId, String type, byte[] modelSetFile)
			throws LpRestExceptionImpl {
		if(XWikiRestUtils.isPage(CORE_REPOSITORY_WIKI, CORE_REPOSITORY_SPACE, modelSetId) == false) {
			XWikiRestUtils.createEmptyPage(CORE_REPOSITORY_WIKI, CORE_REPOSITORY_SPACE, modelSetId);
		}
		String attachmentName = String.format("%s.%s", modelSetId, type);
		XWikiRestUtils.putAttachment(CORE_REPOSITORY_WIKI, CORE_REPOSITORY_SPACE, modelSetId, attachmentName, modelSetFile);
	}

	@Override
	public void checkModelSet(String modelSetId, String type, String value)
			throws LpRestExceptionImpl {
		// TODO Auto-generated method stub

	}
}
