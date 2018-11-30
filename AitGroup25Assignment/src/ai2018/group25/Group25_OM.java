package ai2018.group25;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Objective;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

import static ai2018.group25.Group25_Utils.getParams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Group25_OM extends OpponentModel {

	private static final Double LEARN_COEF_DEFAULT = 0.2;

	/*
	 * the learning coefficient is the weight that is added each turn to the issue
	 * weights which changed. It's a trade-off between concession speed and
	 * accuracy.
	 */
	private double learnCoef;
	/*
	 * value which is added to a value if it is found. Determines how fast the value
	 * weights converge.
	 */
	private int learnValueAddition;
	private int amountOfIssues;
	private double goldenValue;
	private boolean uncertain;

	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		this.negotiationSession = negotiationSession;
		learnCoef = getParams("learnCoef", LEARN_COEF_DEFAULT, parameters);
		learnValueAddition = 1;
		uncertain = this.negotiationSession.getUserModel() != null;
		if (uncertain) {
			opponentUtilitySpace = new AdditiveUtilitySpace(this.negotiationSession.getUserModel().getDomain());
			amountOfIssues = negotiationSession.getUserModel().getDomain().getIssues().size();
		} else {
			opponentUtilitySpace = (AdditiveUtilitySpace) negotiationSession.getUtilitySpace().copy();
			amountOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
		}
		/*
		 * This is the value to be added to weights of unchanged issues before
		 * normalization. Also the value that is taken as the minimum possible weight,
		 * (therefore defining the maximum possible also).
		 */
		goldenValue = learnCoef / amountOfIssues;

		initializeModel();
	}

	@Override
	public void updateModel(Bid opponentBid, double time) {
		if (negotiationSession.getOpponentBidHistory().size() < 2) {
			return;
		}
		int numberOfUnchanged = 0;
		BidDetails oppBid = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - 1);
		BidDetails prevOppBid = negotiationSession.getOpponentBidHistory().getHistory()
				.get(negotiationSession.getOpponentBidHistory().size() - 2);
		HashMap<Integer, Integer> lastDiffSet = determineDifference(prevOppBid, oppBid);

		// count the number of changes in value
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0)
				numberOfUnchanged++;
		}

		// The total sum of weights before normalization.
		double totalSum = 1D + goldenValue * numberOfUnchanged;
		// The maximum possible weight
		double maximumWeight = 1D - (amountOfIssues) * goldenValue / totalSum;

		// re-weighing issues while making sure that the sum remains 1
		for (Integer i : lastDiffSet.keySet()) {
			Objective issue = opponentUtilitySpace.getDomain().getObjectivesRoot().getObjective(i);
			double weight = opponentUtilitySpace.getWeight(i);
			double newWeight;

			if (lastDiffSet.get(i) == 0 && weight < maximumWeight) {
				newWeight = (weight + goldenValue) / totalSum;
			} else {
				newWeight = weight / totalSum;
			}
			opponentUtilitySpace.setWeight(issue, newWeight);
		}

		// Then for each issue value that has been offered last time, a constant
		// value is added to its corresponding ValueDiscrete.
		try {
			for (Map.Entry<Objective, Evaluator> e : opponentUtilitySpace.getEvaluators()) {
				EvaluatorDiscrete value = (EvaluatorDiscrete) e.getValue();
				IssueDiscrete issue = ((IssueDiscrete) e.getKey());
				/*
				 * add constant learnValueAddition to the current preference of the value to
				 * make it more important
				 */
				ValueDiscrete issuevalue = (ValueDiscrete) oppBid.getBid().getValue(issue.getNumber());
				ValueDiscrete prevIssueValue = (ValueDiscrete) prevOppBid.getBid().getValue(issue.getNumber());

				Integer eval = learnValueAddition + value.getEvaluationNotNormalized(issuevalue);

				if (issuevalue == prevIssueValue) {
					eval = Math.max(eval, 8 * value.getEvaluationNotNormalized(prevIssueValue) / 10);
				}

				value.setEvaluation(issuevalue, eval);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public double getBidEvaluation(Bid bid) {
		double result = 0;
		try {
			if (uncertain) {
				result = (double) this.negotiationSession.getUserModel().getBidRanking().getClosestBidRank(bid);
			} else {
				result = opponentUtilitySpace.getUtility(bid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getName() {
		return "HardHeaded Frequency Model group 25";
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("learnCoef", LEARN_COEF_DEFAULT,
				"The learning coefficient determines how quickly the issue weights are learned"));
		return set;
	}

	/**
	 * Init to flat weight and flat evaluation distribution
	 */
	private void initializeModel() {
		double commonWeight = 1D / amountOfIssues;

		for (Map.Entry<Objective, Evaluator> e : opponentUtilitySpace.getEvaluators()) {

			opponentUtilitySpace.unlock(e.getKey());
			e.getValue().setWeight(commonWeight);
			try {
				// set all value weights to one (they are normalized when calculating the
				// utility)
				for (ValueDiscrete vd : ((IssueDiscrete) e.getKey()).getValues()) {
					((EvaluatorDiscrete) e.getValue()).setEvaluation(vd, 1);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Determines the difference between bids. For each issue, it is determined if
	 * the value changed. If this is the case, a 1 is stored in a hashmap for that
	 * issue, else a 0.
	 *
	 * @param first  bid of the opponent
	 * @param second bid
	 * @return
	 */
	private HashMap<Integer, Integer> determineDifference(BidDetails first, BidDetails second) {

		HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();
		try {
			for (Issue i : opponentUtilitySpace.getDomain().getIssues()) {
				Value value1 = first.getBid().getValue(i.getNumber());
				Value value2 = second.getBid().getValue(i.getNumber());
				diff.put(i.getNumber(), (value1.equals(value2)) ? 0 : 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return diff;
	}

}
