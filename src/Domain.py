class Domain(object):
    issues = {}
    weights = {}

    def __init__(self, issues, weights):
        self.issues = issues
        self.weights = weights

    def calculateBid(self, bid: str) -> float:
        """
        :param str bid: The current bid to find return the utilty for.
        :return float: Utilty of the bid.
        """
        issues = bid.split(',')

        utilty = 0.0

        for idx, issue in enumerate(issues):
            utilty += self.weights[self.issues[idx]][issue] * self.weights[self.issues[idx]]['weight']

        return utilty
