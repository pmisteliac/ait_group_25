package ai2018.group25;

import genius.core.boaframework.BoaParty;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.SessionData;
import genius.core.issue.Issue;
import genius.core.parties.NegotiationInfo;
import genius.core.persistent.PersistentDataType;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;

import java.util.HashMap;

public class Group25_Party extends BoaParty {

	private static final long serialVersionUID = 1L;

	public Group25_Party() {
		super(null, new HashMap<String, Double>(), null, new HashMap<String, Double>(), null, new HashMap<String, Double>(), null, new HashMap<String, Double>());
	}

	@Override
	public void init(NegotiationInfo var1) {
		SessionData var2 = null;
		if (var1.getPersistentData().getPersistentDataType() == PersistentDataType.SERIALIZABLE) {
			var2 = (SessionData)var1.getPersistentData().get();
		}

		if (var2 == null) {
			var2 = new SessionData();
		}

		opponentModel = new Group25_OM();
		omStrategy = new Group25_OMS();
		offeringStrategy = new Group25_BS_Uncertain();
		acceptConditions = new Group25_AS();
		negotiationSession = new NegotiationSession(var2, var1.getUtilitySpace(), var1.getTimeline(), null, var1.getUserModel());
		initStrategies();
	}

	private void initStrategies() {
		try {
			this.opponentModel.init(this.negotiationSession, new HashMap<>());
			this.omStrategy.init(this.negotiationSession, this.opponentModel, new HashMap<>());
			this.offeringStrategy.init(this.negotiationSession, this.opponentModel, this.omStrategy, new HashMap<>());
			this.acceptConditions.init(this.negotiationSession, this.offeringStrategy, this.opponentModel, new HashMap<>());
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}


	@Override
	public String getDescription() {
		return "Group 25 BOA Party";
	}

	@Override
	public AbstractUtilitySpace estimateUtilitySpace() {
		return new AdditiveUtilitySpaceFactory(getDomain()).getUtilitySpace();
	}
}
