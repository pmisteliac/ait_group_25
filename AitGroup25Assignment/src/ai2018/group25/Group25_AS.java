package ai2018.group25;

import static ai2018.group25.Group25_Utils.*;

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

	private static final Double RESERVATION_VALUE_DEFAULT = 0.4; // Still to adjust with tests
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.9; // Still to adjust with tests
	private static final Double ALWAYS_ACCEPT_VALUE = 0.85; // Still to adjust with tests

	private double reservationValue;
	private double concedeMoment;
	private double acceptBidUtil;

	public Group25_AS() {
	}

	public Group25_AS(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			double reservationValue, double acceptBidUtil) {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		this.reservationValue = reservationValue;
		this.acceptBidUtil = acceptBidUtil;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy, OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;

		reservationValue = getParams("reservationValue", RESERVATION_VALUE_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
		acceptBidUtil = getParams("acceptBidUtil", ALWAYS_ACCEPT_VALUE, parameters);
	}

	@Override
	public Actions determineAcceptability() {
		double myLastBidUtil = -1.0;
		double rightLimit = -1.0;
		double decisionLimit;

		// Get my last bid and the bid I am planning on doing next
		if (negotiationSession.getOwnBidHistory().getLastBidDetails() != null) {
			myLastBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		}

		double myNextBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();

		// The Right limit is the minimum between my last and my next bid
		if (myLastBidUtil != -1.0) {
			rightLimit = Math.min(myLastBidUtil, myNextBidUtil);
		} else {
			rightLimit = myNextBidUtil;
		}

		// Calculate our current lowest acceptable bid.
		decisionLimit = reservationValue + calculateTimeDiscountFactor() * (rightLimit - reservationValue);
		acceptBidUtil = Math.min(acceptBidUtil, Math.max(decisionLimit, reservationValue));

		// Get the utility of the bid the opponent made, and act accordingly
		double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();

		if (lastOpponentBidUtil >= acceptBidUtil) {
			return Actions.Accept;
		}
		return Actions.Reject;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {

		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("reservation_value", RESERVATION_VALUE_DEFAULT,
				"Reservation Value, never accept offers below this value of utility"));
		set.add(new BOAparameter("starts_falling", CONCEDE_MOMENT_DEFAULT,
				"Threshold before starting to accept bids near the reservation value"));
		return set;
	}

	@Override
	public String getName() {
		return "Group 25 Acceptance Strategy";
	}

	@Override
	public String printParameters() {
		return "[reservationValue: " + reservationValue + "; concedeMoment: " + concedeMoment + " ]";
	}

	private double calculateTimeDiscountFactor() {
		double normalized_time = negotiationSession.getTime();
		if (normalized_time <= concedeMoment) {
			return 1.0;
		} else {
			return Math.min(1.0, (-1 / (1 - concedeMoment)) * normalized_time + (1 / (1 - concedeMoment)));
		}
	}

}
