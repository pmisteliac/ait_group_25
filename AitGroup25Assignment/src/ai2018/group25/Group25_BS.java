package ai2018.group25;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.misc.Range;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ai2018.group25.Group25_Utils.*;

@SuppressWarnings("deprecation")
public class Group25_BS extends OfferingStrategy {
	
	private static final Double UPPER_BOUND_UTILITY_DEFAULT = 1.0;
	private static final Double LOWER_BOUND_UTILITY_DEFAULT = 0.4;
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.95;
	private static final Double USE_OPPONENT_MODEL_MODEL_DEFAULT = 0.2;
	private static final Double RANDOMIZATION_RANGE_DEFAULT = 0.1;
	private static final Double CALC_OPPONENT_UTILITY_RANGE_DEFAULT = 0.1;
	
	private double upperBoundUtility;
	private double lowerBoundUtility;
	private double concedeMoment;
	private double useOpponentModelMoment;
	private double randomizationRange;
	private double calcOpponentUtilityRange;

	public Group25_BS() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		
		upperBoundUtility = getParams("upperBoundUtility", UPPER_BOUND_UTILITY_DEFAULT, parameters);
		lowerBoundUtility = getParams("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
		useOpponentModelMoment = getParams("useOpponentModelMoment", USE_OPPONENT_MODEL_MODEL_DEFAULT, parameters);
		randomizationRange = getParams("randomizationRange", RANDOMIZATION_RANGE_DEFAULT, parameters);
		calcOpponentUtilityRange = getParams("calcOpponentUtilityRange", CALC_OPPONENT_UTILITY_RANGE_DEFAULT, parameters);
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
		double targetUtility = calcualteRandomizedUtility();

		double currentMoment = this.negotiationSession.getTimeline().getTime();
		//start conceding after a defined moment in time
		if(currentMoment > concedeMoment) {
			targetUtility = Math.max(targetUtility - calculateTimeDiscountFactor(), this.lowerBoundUtility);
		}

		do {
			if (currentMoment > useOpponentModelMoment) {
				Range bidRange = createRange(targetUtility, calcOpponentUtilityRange);
				List<BidDetails> bidsinRange = this.negotiationSession.getOutcomeSpace().getBidsinRange(bidRange);
				this.nextBid = omStrategy.getBid(bidsinRange);
			} else {
				this.nextBid = this.negotiationSession.getOutcomeSpace().getBidNearUtility(targetUtility);
			}
			
			targetUtility += 0.01;
		} while (this.nextBid.getMyUndiscountedUtil() < this.lowerBoundUtility && this.nextBid.getMyUndiscountedUtil() < Math.min(targetUtility, upperBoundUtility));

		return this.nextBid;
	}

	private double calcualteRandomizedUtility() {
		double utilityLastBid = this.nextBid.getMyUndiscountedUtil();
		return utilityLastBid + (randomizationRange / 2) - randomizationRange * Math.random();
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
		parameterSet.add(new BOAparameter("useOpponentModelMoment", USE_OPPONENT_MODEL_MODEL_DEFAULT,
				"Moment in time when we start using the opponent model"));
		parameterSet.add(new BOAparameter("randomizationRange", RANDOMIZATION_RANGE_DEFAULT, 
				"Range within our randomized utility value stays"));
		parameterSet.add(new BOAparameter("calcOpponentUtilityRange", CALC_OPPONENT_UTILITY_RANGE_DEFAULT,
				"Range within we calculate the opponents utility to find the best bid"));
		return parameterSet;
	}
	
	@Override
	public String getName() {
		return "Group 25 Offering Strategy";
	}

	private double calculateTimeDiscountFactor() {
		return (this.nextBid.getMyUndiscountedUtil() - lowerBoundUtility) / (this.negotiationSession.getTimeline().getTotalTime() - this.negotiationSession.getTimeline().getCurrentTime());
	}
}
