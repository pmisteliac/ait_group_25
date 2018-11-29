package ai2018.group25;

import static ai2018.group25.Group25_Utils.getParams;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;
/**
 * This class uses an opponent model to determine the next bid for the opponent,
 * while taking the opponent's preferences into account. The opponent model is
 * used to select the best bid.
 * 
 */
public class Group25_OMS extends OMStrategy {
	
	//TODO might not be needed
	private static final Double UPDATE_THRESHOLD_DEFAULT = 1.1;
	/**
	 * when to stop updating the opponentmodel. Note that this value is not exactly
	 * one as a match sometimes lasts slightly longer.
	 */
	private double updateThreshold;

	/**
	 * Initializes the opponent model strategy. If a value for the parameter t is
	 * given, then it is set to this value. Otherwise, the default value is used.
	 * 
	 * @param negotiationSession state of the negotiation.
	 * @param model              opponent model used in conjunction with this
	 *                           opponent modeling strategy.
	 * @param parameters         set of parameters for this opponent model strategy.
	 */
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
		super.init(negotiationSession, model, parameters);
		
		updateThreshold = getParams("updateThreshold", UPDATE_THRESHOLD_DEFAULT, parameters);
	}

	/**
	 * Returns the bid with the highest combined utility given a set of similarly preferred
	 * bids.
	 * 
	 * @param allBids list of the bids considered for offering.
	 * @return bid to be offered to opponent.
	 */
	@Override
	public BidDetails getBid(List<BidDetails> allBids) {

		// 1. If there is only a single bid, return this bid
		if (allBids.size() == 1) {
			return allBids.get(0);
		}
		
		// 2. find bid with the highest combined utility
		double highestCombinedUtility = 0;
		BidDetails bestBid = allBids.get(0);
		
		for (BidDetails bid : allBids) {
			double opponentUtility = model.getBidEvaluation(bid.getBid());
			double myUtility = bid.getMyUndiscountedUtil();
			double combinedUtility = opponentUtility + myUtility;
			if(combinedUtility > highestCombinedUtility) {
				highestCombinedUtility = combinedUtility;
				bestBid = bid;
			}
		}
		return bestBid;
	}

	/**
	 * The opponent model may be updated, unless the time is higher than a given
	 * constant.
	 * 
	 * @return true if model may be updated.
	 */
	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("updateThreshold", UPDATE_THRESHOLD_DEFAULT, "Time after which the OM should not be updated"));
		return set;
	}

	@Override
	public String getName() {
		return "Group 25 Opponent Model Strategy";
	}
}