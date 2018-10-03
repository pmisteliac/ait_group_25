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
	private static final Double CONCEDE_MOMENT_DEFAULT = 0.7; // Still to adjust with tests

	private double reservationValue;
	private double concedeMoment;

	public Group25_AS() {
	}

	public Group25_AS(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			double reservationValue) {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		this.reservationValue = reservationValue;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;

		reservationValue = getParams("reservationValue", RESERVATION_VALUE_DEFAULT, parameters);
		concedeMoment = getParams("concedeMoment", CONCEDE_MOMENT_DEFAULT, parameters);
	}

	@Override
	public Actions determineAcceptability() {
		double myLastBidUtil = -1.0;
		double rightLimit = -1.0;
		double decisionLimit;

		// Get my last bid and the bid I am planning on doing next
		if (negotiationSession.getOwnBidHistory().getLastBidDetails() != null) { // Cover the first case
			myLastBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		}

		double myNextBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();

		// The Right limit is the minimum between my last and my next bid
		if (myLastBidUtil != -1.0) {
			rightLimit = Math.min(myLastBidUtil, myNextBidUtil);
		} else {
			rightLimit = myNextBidUtil;
		}

		if (rightLimit <= reservationValue) {
			decisionLimit = reservationValue; // Error avoidance
		} else {
			// Get, according to time, the percentage of the interval not to accept
			double percentageIntervalRejected = computeRejectionPercentage(negotiationSession.getTime());
			// Compute the decision limit
			decisionLimit = reservationValue + percentageIntervalRejected * (rightLimit - reservationValue);
		}

		// Get the utility of the bid the opponent made, and act accordingly
		double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails()
				.getMyUndiscountedUtil();

		if (lastOpponentBidUtil >= decisionLimit) {
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

	private double computeRejectionPercentage(double normalizedTime) {
		if (normalizedTime <= concedeMoment) {
			return 1.0;
		} else {
			return (-1 / (1 - concedeMoment)) * normalizedTime + (1 / (1 - concedeMoment)); // Still linear, transform
																							// into exponential
		}
	}

}
