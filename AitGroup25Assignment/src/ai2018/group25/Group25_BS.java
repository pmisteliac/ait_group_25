package ai2018.group25;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ai2018.group25.Group25_Utils.getParams;

@SuppressWarnings("deprecation")
public class Group25_BS extends OfferingStrategy {
	
	private static final Double UPPER_BOUND_UTILITY_DEFAULT = 1.0; 
	private static final Double LOWER_BOUND_UTILITY_DEFAULT = 0.8; 
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.9; 
	
	private double upperBoundUtility;
	private double lowerBoundUtility;
	private double concedeMoment;
	private double reservationValue;

	public Group25_BS() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		
		upperBoundUtility = getParams("upperBoundUtility", UPPER_BOUND_UTILITY_DEFAULT, parameters);
		lowerBoundUtility = getParams("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
		negotiationSession.setOutcomeSpace(new SortedOutcomeSpace(negotiationSession.getUtilitySpace()));
		this.reservationValue = 0.4;// this.getParameters().get("reservationValue");
	}


	/**
	 * Start the biding no information of the opponent is available to be used.
	 *
	 * @return
	 * 	The BidDetails with a close to optimal bid for us.
	 */
	@Override
	public BidDetails determineOpeningBid() {
		Bid bid = null;

		try {
			bid = this.negotiationSession.getUtilitySpace().getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			this.nextBid = new BidDetails(bid, this.negotiationSession.getUtilitySpace().getUtility(bid));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.nextBid;
	}

	/**
	 * Determine the next Bid.
	 * The idea is to have a hardheaded approach where we concede at the end and in the beginning we add a bit of randoms.

	 * @return
	 * 	The BidDetails containing our new Bid.
	 */
	@Override
	public BidDetails determineNextBid() {
		double u = this.nextBid.getMyUndiscountedUtil() + 0.05 - 0.1 * Math.random();

		if(this.negotiationSession.getTimeline().getTime() > 0.8) {
			u = Math.max(u - 0.2, this.reservationValue);
		}

		this.nextBid = this.negotiationSession.getOutcomeSpace().getBidNearUtility(u);

		return this.nextBid;
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
