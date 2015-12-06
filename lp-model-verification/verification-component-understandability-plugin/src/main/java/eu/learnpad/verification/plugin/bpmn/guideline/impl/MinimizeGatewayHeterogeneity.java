package eu.learnpad.verification.plugin.bpmn.guideline.impl;



import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SubProcess;


public class MinimizeGatewayHeterogeneity extends abstractGuideline {

	public MinimizeGatewayHeterogeneity(Definitions diagram) {
		super(diagram);
		this.id = "9";
		this.Description = "The modeler should minimize the heterogeneity of gateway types. The use of several type of gateway may cause confusion against the simplicity of using few main type of gateways.";
		this.Name = "Minimize Gateway Heterogeneity";


	}
	@Override
	protected void findGL(Definitions diagram) {
		StringBuilder ret = new StringBuilder("");

		int neg=0;
		int npg=0;
		int nig=0;
		int ncg=0;
		for (RootElement rootElement : diagram.getRootElements()) {
			if (rootElement instanceof Process) {
				Process process = (Process) rootElement;
				//System.out.format("Found a process: %s\n", process.getName());

				IDProcess = process.getId();
				for (FlowElement fe : process.getFlowElements()) {
					if(fe instanceof SubProcess){
						SubProcess sub = (SubProcess) fe;
						//System.out.format("Found a SubProcess: %s\n", sub.getName());
						this.searchSubProcess(sub);
					}else
						if (fe instanceof Gateway) {
							Gateway gateway = (Gateway) fe;
							if(gateway instanceof ExclusiveGateway){
								neg++;
							}else
								if(gateway instanceof ParallelGateway){
									npg++;
								}else
									if(gateway instanceof InclusiveGateway){
										nig++;
									}else
										if(gateway instanceof ComplexGateway){
											ncg++;
										}
						}
				}
			}
		}
		int sum = neg+npg+nig+ncg;
		long geg = (neg/sum);
		long gpg = (npg/sum);
		long gig = (nig/sum);
		long gcg = (ncg/sum);
		long sum2= geg+gpg+gig+gcg;
		if (sum2>0.92) {
			this.Suggestion += "Minimize Gateway Heterogeneity " + ret;
			this.status = false;
		}else{
			this.status = true;
			this.Suggestion += "Well done!";
		}
	}

	protected void searchSubProcess(SubProcess sub){
		StringBuilder temp = new StringBuilder();
		
		int neg=0;
		int npg=0;
		int nig=0;
		int ncg=0;
		for ( FlowElement fe : sub.getFlowElements()) {
			if(fe instanceof SubProcess){
				SubProcess ssub = (SubProcess) fe;
				//System.out.format("Found a SubProcess: %s\n", ssub.getName());
				this.searchSubProcess(ssub);
			}else
				if (fe instanceof Gateway) {
					Gateway gateway = (Gateway) fe;
					if(gateway instanceof ExclusiveGateway){
						neg++;
					}else
						if(gateway instanceof ParallelGateway){
							npg++;
						}else
							if(gateway instanceof InclusiveGateway){
								nig++;
							}else
								if(gateway instanceof ComplexGateway){
									ncg++;
								}
				}
		}
		int sum = neg+npg+nig+ncg;
		long geg = (neg/sum);
		long gpg = (npg/sum);
		long gig = (nig/sum);
		long gcg = (ncg/sum);
		long sum2= geg+gpg+gig+gcg;
		if (sum2>0.92) {

			this.Suggestion += "\nMinimize Gateway Heterogeneity SubProcess "+sub.getName()+" " + temp;
			this.status = false;
		}

	}

}
