package ai2018.group25;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.boaframework.SortedOutcomeSpace;

import java.util.Map;

@SuppressWarnings("deprecation")
public class Group25_BS extends OfferingStrategy {

	private double reservationValue;

	public Group25_BS() {

	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
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
	public String getName() {
		return "Group 25 Offering Strategy";
	}

}
