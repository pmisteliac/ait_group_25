class Actions(object):
    conceded = 0
    selfish = 0
    nice = 0
    unfortunated = 0
    fortunated = 0
    silent = 0
    lastAction = ''

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

        action = self.getAction(currentAgentDelta, oppenementDelta)
        setattr(self, action, getattr(self, action) + 1)
        self.lastAction = action

    def getAction(self, currentAgentDelta: float, oppenementDelta: float) -> str:
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
                :return string: name of action
                """

        if abs(currentAgentDelta) < self.delta and abs(oppenementDelta) < self.delta:
            return 'silent'

        if currentAgentDelta >= 0:
            if oppenementDelta > self.delta:
                return 'fortunated'
            elif abs(oppenementDelta) < self.delta:
                return 'nice'
            else:
                return 'selfish'

        if oppenementDelta < 0:
            return 'unfortunated'
        return 'conceded'

    def getLastAction(self) -> str:
        return self.lastAction

    def __str__(self):
        return ''.join(map(lambda x: '{0} = {1}, '.format(*x), self)).rstrip(', ') + '\n'

    def __iter__(self):
        yield 'conceded', self.conceded
        yield 'selfish', self.selfish
        yield 'nice', self.nice
        yield 'fortunated', self.fortunated
        yield 'unfortunated', self.unfortunated
        yield 'silent', self.silent
