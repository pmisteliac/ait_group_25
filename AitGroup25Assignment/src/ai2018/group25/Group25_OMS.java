package ai2018.group25;

import java.util.List;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.OMStrategy;

public class Group25_OMS extends OMStrategy {

	@Override
	public BidDetails getBid(List<BidDetails> bidsInRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUpdateOM() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Group 25 Opponent Model Strategy";
	}

}
