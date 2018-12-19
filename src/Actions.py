class Actions(object):
    conceded = 0
    selfish = 0
    nice = 0
    unfortunated = 0
    silent = 0

    delta = 0

    def update(self, currentAgentDelta: float, oppenementDelta: float) -> None:
        if currentAgentDelta > self.delta and oppenementDelta > self.delta:
            self.nice += 1
        elif currentAgentDelta > self.delta and -self.delta < oppenementDelta < 0:
            self.selfish += 1
        elif -self.delta < currentAgentDelta < 0 and oppenementDelta > self.delta:
            self.conceded += 1
        elif -self.delta < currentAgentDelta < 0 and -self.delta < oppenementDelta < 0:
            self.unfortunated += 1
        else:
            self.silent += 1
