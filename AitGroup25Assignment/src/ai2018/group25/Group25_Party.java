package ai2018.group25;

import java.util.HashMap;

import genius.core.boaframework.BoaParty;
import genius.core.parties.NegotiationInfo;

public class Group25_Party extends BoaParty {
	
//	public Group25_Party(AcceptanceStrategy ac, Map<String, Double> acParams, OfferingStrategy os,
//			Map<String, Double> osParams, OpponentModel om, Map<String, Double> omParams, OMStrategy oms,
//			Map<String, Double> omsParams) {
//		super(ac, acParams, os, osParams, om, omParams, oms, omsParams);
//		// TODO Auto-generated constructor stub
//	}

	private static final long serialVersionUID = 1L;

	public Group25_Party() {
		super(null, new HashMap<String, Double>(), null, new HashMap<String, Double>(), null,
				new HashMap<String, Double>(), null, new HashMap<String, Double>());
	}

	@Override
	public void init(NegotiationInfo info) {
		
		opponentModel = new Group25_OM();
		omStrategy = new Group25_OMS();
		offeringStrategy = new Group25_BS();
		acceptConditions = new Group25_AS();
		super.init(info);
	}

	@Override
	public String getDescription() {
		return "Group 25 BOA Party ";
	}
}
