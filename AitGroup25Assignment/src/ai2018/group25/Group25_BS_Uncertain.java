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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static ai2018.group25.Group25_Utils.createRange;
import static ai2018.group25.Group25_Utils.getParams;

public class Group25_BS_Uncertain extends OfferingStrategy {

	private static final Double LOWER_BOUND_UTILITY_DEFAULT = 0.4;
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.95;
	private static final Double RANDOMIZATION_RANGE_DEFAULT = 5.0;
	private static final Double RANGE_LIMIT = 0.85;

	private OpponentModel model;

	private double lowerBoundUtility;
	private double concedeMoment;
	private double randomizationRange;
	private double rangeLowerLimit;

	public Group25_BS_Uncertain() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		lowerBoundUtility = getParams("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
		randomizationRange = getParams("randomizationRange", RANDOMIZATION_RANGE_DEFAULT, parameters);
		rangeLowerLimit = getParams("rangeLowerLimit", RANGE_LIMIT, parameters);
		rangeLowerLimit *= this.negotiationSession.getUserModel().getBidRanking().getSize();
		Group25_Utils.init(negotiationSession);
		negotiationSession.setOutcomeSpace(new SortedOutcomeSpace(negotiationSession.getUtilitySpace()));
		this.model = Group25_Utils.getModel();
	}

	public OpponentModel getModel() {
		return model;
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
			bid = this.negotiationSession.getUserModel().getBidRanking().getMmaximalBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.nextBid = new BidDetails(bid, this.model.getBidEvaluation(bid));

		return this.nextBid;
	}

	/**
	 * Determine the next Bid.
	 * We use the last util and then add a random value between -0.05 and + 0.05 to the utility.
	 * It is made sure that the bids are never lower than our reservation value and below the calculated utility.
     *
	 * @return
	 * 	The BidDetails containing our new Bid.
	 */
	@Override
	public BidDetails determineNextBid() {
		int closestBidRank = this.negotiationSession.getUserModel().getBidRanking().getClosestBidRank(this.nextBid.getBid());

		closestBidRank += calculateDiff();

		if (closestBidRank >= this.negotiationSession.getUserModel().getBidRanking().getSize()) {
			closestBidRank = this.negotiationSession.getUserModel().getBidRanking().getSize() - 1;
		} else if (closestBidRank < rangeLowerLimit) {
			closestBidRank = (int)rangeLowerLimit;
		}

		double currentMoment = this.negotiationSession.getTimeline().getTime();
		//start conceding after a defined moment in time
		if(currentMoment > concedeMoment) {
			closestBidRank = (int) (1 - (this.lowerBoundUtility * ((1 - currentMoment)/ concedeMoment))) * this.negotiationSession.getUserModel().getBidRanking().getSize();
		}

		Bid bid = this.negotiationSession.getUserModel().getBidRanking().getBidOrder().get(closestBidRank - 1);

		this.nextBid = new BidDetails(bid, this.model.getBidEvaluation(bid));

		return this.nextBid;
	}

	private int calculateDiff()
	{
		Random rand = new Random();

		return rand.nextInt(2*(int)randomizationRange + 1) - (int)randomizationRange;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> parameterSet = new HashSet<BOAparameter>();
		parameterSet.add(new BOAparameter("lowerBoundUtility", LOWER_BOUND_UTILITY_DEFAULT ,
				"Lower bound for randomisation of utility"));
		parameterSet.add(new BOAparameter("concedeMoment", CONCEDE_MOMENT_DEFAULT ,
				"Moment in time when the agent starts to concede"));
		parameterSet.add(new BOAparameter("randomizationRange", RANDOMIZATION_RANGE_DEFAULT,
				"Range within our randomized utility value stays"));
		parameterSet.add(new BOAparameter("rangeLowerLimit", RANGE_LIMIT,
				"Range within we can calculate bids"));
		return parameterSet;
	}
	
	@Override
	public String getName() {
		return "Group 25 Offering Strategy uncertain";
	}

	private double calculateTimeDiscountFactor() {
		return (this.nextBid.getMyUndiscountedUtil() - lowerBoundUtility) / (this.negotiationSession.getTimeline().getTotalTime() - this.negotiationSession.getTimeline().getCurrentTime());
	}
}
