class Actions(object):
    conceded = 0
    selfish = 0
    nice = 0
    unfortunated = 0
    fortunated = 0
    silent = 0

    delta = 0.001

    def update(self, currentAgentDelta: float, oppenementDelta: float) -> None:
        """
        Several actions can be taken by the agents:
            - Silent: Changes to the utilty are with in the abs(delta) < self.delta

            - Nice: Own utilty increase oppement doesn't change.
            - Fortunated: Own utilty increases and the oppement one too.
            - Selfish: Own utilty increases and opppement decreases.

            - Conceding: Own utilty decreases and the oppement one improves.
            - Unfortunated: Own utilty decreases and the oppenemt one too.

        :param float currentAgentDelta:
        :param float oppenementDelta:
        :return:
        """

        if abs(currentAgentDelta) < self.delta and abs(oppenementDelta) < self.delta:
            self.silent += 1
            return

        if currentAgentDelta >= 0:
            if oppenementDelta > self.delta:
                self.fortunated += 1
            elif abs(oppenementDelta) < self.delta:
                self.nice += 1
            else:
                self.selfish += 1
            return

        if oppenementDelta < 0:
            self.unfortunated += 1
        else:
            self.conceded += 1

    def __str__(self):
        return 'Conceded = {0}, selfish = {1}, nice = {2}, fortunate = {3}, unfortunated = {4}, silent = {5}\n'.format(
            *map(lambda x: x[1], self)
        )

    def __iter__(self):
        yield 'conceded', self.conceded
        yield 'selfish', self.selfish
        yield 'nice', self.nice
        yield 'fortunated', self.fortunated
        yield 'unfortunated', self.unfortunated
        yield 'silent', self.silent
