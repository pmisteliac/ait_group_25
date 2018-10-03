package ai2018.group25;

import static ai2018.group25.Group25_Utils.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

@SuppressWarnings("deprecation")
public class Group25_BS extends OfferingStrategy {
	
	private static final Double UPPER_BOUND_UTILITY_DEFAULT = 1.0; 
	private static final Double LOWER_BOUND_UTILITY_DEFAULT = 0.8; 
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.9; 
	
	private double upperBoundUtility;
	private double lowerBoundUtility;
	private double concedeMoment;
	
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy,
			Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		
		upperBoundUtility = getParams("upperBoundUtility", UPPER_BOUND_UTILITY_DEFAULT, parameters);
		lowerBoundUtility = getParams("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
	}

	@Override
	public BidDetails determineOpeningBid() {
		return determineNextBid();
	}

	@Override
	public BidDetails determineNextBid() {
		return null;
	}
	
	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> parameterSet = new HashSet<BOAparameter>();
		parameterSet.add(new BOAparameter("upperBoundUtility", UPPER_BOUND_UTILITY_DEFAULT ,
				"Upper bound for randomisation of utility"));
		parameterSet.add(new BOAparameter("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT ,
				"Lower bound for randomisation of utility"));
		parameterSet.add(new BOAparameter("concedeMoment", CONCEDE_MOMENT_DEFAULT ,
				"Moment in time when the agent starts to concede"));
		return parameterSet;
	}
	
	@Override
	public String getName() {
		return "Group 25 Offering Strategy";
	}

}
