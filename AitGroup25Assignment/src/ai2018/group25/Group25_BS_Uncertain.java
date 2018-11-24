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
import java.util.Set;

import static ai2018.group25.Group25_Utils.createRange;

public class Group25_BS_Uncertain extends OfferingStrategy {


	private OpponentModel model;

	public Group25_BS_Uncertain() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
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

		Bid bid = this.negotiationSession.getUserModel().getBidRanking().getBidOrder().get(closestBidRank - 1);

		this.nextBid = new BidDetails(bid, this.model.getBidEvaluation(bid));

		return this.nextBid;
	}


	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> parameterSet = new HashSet<BOAparameter>();
		return parameterSet;
	}
	
	@Override
	public String getName() {
		return "Group 25 Offering Strategy uncertain";
	}
}
