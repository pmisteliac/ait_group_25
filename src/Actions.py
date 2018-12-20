class Actions(object):
    conceded = 0
    selfish = 0
    nice = 0
    unfortunated = 0
    silent = 0

    delta = 0

    def update(self, currentAgentDelta: float, oppenementDelta: float) -> None:
        print('A', currentAgentDelta)
        print('O', oppenementDelta)
        if currentAgentDelta > self.delta and oppenementDelta > self.delta:
            self.nice += 1
        elif currentAgentDelta > self.delta and -self.delta <= oppenementDelta <= 0:
            self.selfish += 1
        elif -self.delta <= currentAgentDelta <= 0 and oppenementDelta > self.delta:
            self.conceded += 1
        elif -self.delta <= currentAgentDelta <= 0 and -self.delta <= oppenementDelta <= 0:
            self.unfortunated += 1
        else:
            self.silent += 1

    def __str__(self):
        return 'Conceded = {0}, selfish = {1}, nice = {2}, unfortunated = {3}, silent = {4}\n'.format(
            self.conceded,
            self.selfish,
            self.nice,
            self.unfortunated,
            self.silent
        )
