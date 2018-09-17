package ai2018.group25;

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
	
	private static final Double RESERVATION_VALUE = 0.5; // Still to adjust with tests
	
	private static final Double STARTS_FALLING = 0.7; // Still to adjust with tests
	
	private double reservation_value;
	private double starts_falling;


	public Group25_AS() {
	}
	
	public Group25_AS(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy, double reservation_value) {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;
		this.reservation_value = reservation_value;
	}
	
	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		this.negotiationSession = negotiationSession;
		this.offeringStrategy = offeringStrategy;

		if (parameters.get("reservation_value") != null || parameters.get("starts_falling") != null ) {
			reservation_value = parameters.get("reservation_value");
			starts_falling = parameters.get("starts_falling");
		} else {
			reservation_value = RESERVATION_VALUE;
			starts_falling = STARTS_FALLING;
		}
	}

	
	@Override
	public Actions determineAcceptability() {
		// Accepting Strategy Implementation
		
		// Initialize variable
		double myLastBidUtil = -1.0;
		double right_limit = -1.0;
		
		// Get my last bid and the bid I am planning on doing next
		if (negotiationSession.getOwnBidHistory().getLastBidDetails()!=null) { // Cover the first case
			myLastBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		}
		
		double myNextBidUtil = offeringStrategy.getNextBid().getMyUndiscountedUtil();
		
		// The Right limit is the minimum between my last and my next bid
		if (myLastBidUtil != -1.0) {
			right_limit = Math.min(myLastBidUtil, myNextBidUtil);
		} else {
			right_limit = myNextBidUtil;
		}
		
		// Get, according to time, the percentage of the interval not to accept
		double percentage_interval_rejected = what_reject(negotiationSession.getTime()); // DO THIS FUNCTION
		
		// Compute the decision limit
		double decision_limit = reservation_value + percentage_interval_rejected * ( right_limit - reservation_value );
		
		// Get the utility of the bid the opponent made, and act accordingly
		double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails().getMyUndiscountedUtil();
		
		if (lastOpponentBidUtil >= decision_limit ) {
			return Actions.Accept;
		}
		return Actions.Reject;
	}
	
	@Override
	public Set<BOAparameter> getParameterSpec() {

		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("reservation_value", RESERVATION_VALUE ,
				"Reservation Value, never accept offers below this value of utility"));
		set.add(new BOAparameter("starts_falling", STARTS_FALLING ,
				"Threshold before starting to accept bids near the reservation value"));
		return set;
	}

	@Override
	public String getName() {
		return "Group 25 Acceptance Strategy";
	}
	
	@Override
	public String printParameters() {
		return "[paramter 1: " + reservation_value + " ]";
	}
	
	private double what_reject(double normalized_time) {
		if ( normalized_time <= starts_falling ) { 
			return 1.0;
		} else {
			return ( -1/(1-starts_falling) )*normalized_time + ( 1/(1-starts_falling) ); // Still linear, transform into exponential
		}
	}
	
}

