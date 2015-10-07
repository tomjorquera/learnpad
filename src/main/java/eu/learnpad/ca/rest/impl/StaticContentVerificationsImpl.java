package eu.learnpad.ca.rest.impl;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.Italian;

import eu.learnpad.ca.analysis.AbstractAnalysisClass;
import eu.learnpad.ca.analysis.correctness.CorrectnessAnalysis;
import eu.learnpad.ca.analysis.simplicity.JuridicalJargon;
import eu.learnpad.ca.analysis.syntacticambiguity.SyntacticAmbiguity;
import eu.learnpad.ca.rest.StaticContentVerifications;
import eu.learnpad.ca.rest.data.stat.AnnotatedStaticContentAnalysis;
import eu.learnpad.ca.rest.data.stat.StaticContentAnalysis;
import eu.learnpad.exception.LpRestException;

@Path("/learnpad/ca/validatestaticcontent")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class StaticContentVerificationsImpl implements StaticContentVerifications {

	private static Map<Integer,List<AbstractAnalysisClass>> map = new HashMap<Integer,List<AbstractAnalysisClass>>();
	private static Integer id =0;


	@Path("/")
	@POST
	public String putValidateStaticContent(StaticContentAnalysis contentFile)
			throws LpRestException {
		if(contentFile.getQualityCriteria().isCorrectness()){
			id++;
			Language lang = null;
			if(contentFile.getLanguage()=="english"){
				lang = new BritishEnglish();
			}else{
				if(contentFile.getLanguage().toLowerCase().equals("italian")){
					lang = new Italian();
				}else
					if(contentFile.getLanguage().toLowerCase().equals("english uk")){
						lang = new BritishEnglish();
					}else
						if(contentFile.getLanguage().toLowerCase().equals("english us")){
							lang = new AmericanEnglish();
						}else
							lang = new BritishEnglish();
			}
			if(contentFile.getQualityCriteria().isCorrectness()){

				CorrectnessAnalysis threadcorre = new CorrectnessAnalysis(lang, contentFile);
				threadcorre.start();
				putAndCreate(id, threadcorre);

			}
			if(contentFile.getQualityCriteria().isSimplicity()){

				JuridicalJargon threadsimply = new JuridicalJargon (contentFile, lang);
				threadsimply.start();
				putAndCreate(id, threadsimply);

			}
			if(contentFile.getQualityCriteria().isNonAmbiguity()){

				SyntacticAmbiguity threadSyntacticAmbiguity = new SyntacticAmbiguity (contentFile, lang);
				threadSyntacticAmbiguity.start();
				putAndCreate(id, threadSyntacticAmbiguity);

			}
			return id.toString();
		}else
			return "Null Element send";


	}

	private void putAndCreate(int id, AbstractAnalysisClass ai){
		if(!map.containsKey(id)){
			List<AbstractAnalysisClass> lai = new ArrayList<AbstractAnalysisClass>();
			lai.add(ai);
			map.put(id, lai);
		}else{
			List<AbstractAnalysisClass> lai = map.get(id);
			lai.add(ai);
		}
	}


	@Path("/{idAnnotatedStaticContentAnalysis:\\d+}")
	@GET
	public Collection<AnnotatedStaticContentAnalysis> getStaticContentVerifications(
			@PathParam("idAnnotatedStaticContentAnalysis") String contentID) throws LpRestException {
		if(map.containsKey(Integer.valueOf(contentID))){
			ArrayList<AnnotatedStaticContentAnalysis> ar = new ArrayList<AnnotatedStaticContentAnalysis>();
			List<AbstractAnalysisClass> listanalysisInterface = map.get(Integer.valueOf(contentID));

			for(AbstractAnalysisClass analysisInterface :listanalysisInterface){
				AnnotatedStaticContentAnalysis annotatedStaticContent = analysisInterface.getAnnotatedStaticContentAnalysis();
				if(annotatedStaticContent!=null){
					annotatedStaticContent.setId(Integer.valueOf(contentID));
					ar.add(annotatedStaticContent);
				}
			}

			return ar;
		}else
			return null;
	}

	@Path("/{idAnnotatedStaticContentAnalysis:\\d+}/status")
	@GET
	public String getStatusStaticContentVerifications(@PathParam("idAnnotatedStaticContentAnalysis") String contentID)
			throws LpRestException {
		if(map.containsKey(Integer.valueOf(contentID))){
			List<AbstractAnalysisClass> listanalysisInterface  = map.get(Integer.valueOf(contentID));
			for(AbstractAnalysisClass analysisInterface :listanalysisInterface){
				if(analysisInterface.getStatus()!="OK"){
					return "IN PROGRESS";
				}
			}
			return "OK";
		}
		return "ERROR";
	}

}
