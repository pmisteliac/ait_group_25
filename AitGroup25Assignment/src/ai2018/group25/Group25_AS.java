package ai2018.group25;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

@SuppressWarnings("deprecation")
public class Group25_AS extends AcceptanceStrategy {
	
	private static final Double PARAMETER1_DEFAULT_VALUE = 0.8;
	//dummy parameter, I added this so you see how this can be used
	private double parameter1;


	public Group25_AS() {
	}
	
	public Group25_AS(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy, double parameter1) {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		this.parameter1 = parameter1;
	}
	
	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;

		if (parameters.get("parameter1") != null) {
			parameter1 = parameters.get("parameter1");
		} else {
			parameter1 = PARAMETER1_DEFAULT_VALUE;
		}
	}

	
	@Override
	public Actions determineAcceptability() {
		//TODO implement strategy here
		double myNextBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();
		double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		
		if (lastOpponentBidUtil >= myNextBidUtil * parameter1) {
			return Actions.Accept;
		}
		return Actions.Reject;
	}
	
	@Override
	public Set<BOAparameter> getParameterSpec() {

		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("parameter1", PARAMETER1_DEFAULT_VALUE ,
				"Parameter 1 description"));
		return set;
	}

	@Override
	public String getName() {
		return "Group 25 Acceptance Strategy";
	}
	
	@Override
	public String printParameters() {
		return "[paramter 1: " + parameter1 + " ]";
	}

}
