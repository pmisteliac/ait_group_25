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
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.95;
	
	private double upperBoundUtility;
	private double lowerBoundUtility;
	private double concedeMoment;

	public Group25_BS() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		
		upperBoundUtility = getParams("upperBoundUtility", UPPER_BOUND_UTILITY_DEFAULT, parameters);
		lowerBoundUtility = getParams("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
		negotiationSession.setOutcomeSpace(new SortedOutcomeSpace(negotiationSession.getUtilitySpace()));
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

		this.nextBid = new BidDetails(bid, this.negotiationSession.getUtilitySpace().getUtility(bid));

		return this.nextBid;
	}

	/**
	 * Determine the next Bid.
	 * We use the last util and then add a random value between -0.05 and + 0.05 to the utility.
	 * It is made sure that the bids are never lower than our reservation value and below the calculated utility.

	 * @return
	 * 	The BidDetails containing our new Bid.
	 */
	@Override
	public BidDetails determineNextBid() {
		double u = this.nextBid.getMyUndiscountedUtil() + 0.05 - 0.1 * Math.random();

		if(this.negotiationSession.getTimeline().getTime() > concedeMoment) {
			u = Math.max(u - calculateTimeDiscountFactor(), this.lowerBoundUtility);
		}

		do {
			this.nextBid = this.negotiationSession.getOutcomeSpace().getBidNearUtility(u);
			u += 0.01;
		} while (this.nextBid.getMyUndiscountedUtil() < this.lowerBoundUtility && this.nextBid.getMyUndiscountedUtil() < Math.min(u, upperBoundUtility));

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

	private double calculateTimeDiscountFactor() {
		return (this.nextBid.getMyUndiscountedUtil() - lowerBoundUtility) / (this.negotiationSession.getTimeline().getTotalTime() - this.concedeMoment);
	}
}
