from src.Actions import Actions
from src.Domain import Domain
from typing import List
Vector = List[str]


class Agent(object):
    domain = {}
    lastBid = 0.0
    currentBid = 0.0
    actions = {}

    def __init__(self, issues: Vector, weights):
        self.domain = Domain(issues, weights)
        self.actions = Actions()

    def processBid(self, bid: str) -> None:
        """
        Update the model with a newly made bid.
        :param str bid: The bid made by the agent.
        """
        self.lastBid = self.currentBid
        self.currentBid = self.getUtilty(bid)

    def updateAction(self, oppementUtiltyChange: float) -> None:
        """
        After a round has be done calculate which actions have been made by the agent.
        :param float oppementUtiltyChange:
        """
        if self.lastBid > 0:
            self.actions.update(self.currentBid - self.lastBid, oppementUtiltyChange)

    def getUtilty(self, bid: str) -> float:
        """
        Calculates the value of a single bid.
        :param bid:
        :return:
        """
        return self.domain.calculateBid(bid)
