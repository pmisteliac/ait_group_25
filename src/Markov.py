from functools import reduce

import copy

from src.Actions import Actions
from inflection import camelize


class Markov(object):
    data = {}

    def update(self, name: str, actions1: Actions, actions2: Actions) -> None:
        name = Markov.getName(name)

        if name in self.data:
            self.data[name] += actions1
        else:
            self.data[name] = Data(actions1)

    @staticmethod
    def getName(name: str) -> str:
        return ''.join(filter(lambda x: x.isalpha(), camelize(name.split('.', maxsplit=1)[0])))

    def getChances(self, actionAgent: Actions, actionOppement: Actions):
        return map(lambda type: {type: copy.deepcopy(self.data[type]).getChance(actionAgent)}, self.data)

    def __str__(self):
        return ''.join(map(lambda x: "{0}: {1}\n".format(x, self.data[x]), self.data))


class Data(object):
    conceded = 0
    selfish = 0
    nice = 0
    unfortunated = 0
    fortunated = 0
    silent = 0

    def __init__(self, action: Actions):
s        self.conceded = action.conceded
        self.selfish = action.selfish
        self.nice = action.nice
        self.unfortunated = action.unfortunated
        self.fortunated = action.fortunated
        self.silent = action.silent

    def __iadd__(self, other):
        return self.save(map(lambda o: {o[0][0]: o[0][1] + o[1][1]}, zip(self, other)))

    def getChance(self, action: Actions):
        count = reduce(lambda x, y: x + y[1], self, 0)
        countActions = reduce(lambda x, y: x + y[1], action, 0)
        return (self / action) * (countActions / count)

    def __iter__(self):
        yield 'conceded', self.conceded
        yield 'selfish', self.selfish
        yield 'nice', self.nice
        yield 'fortunated', self.fortunated
        yield 'unfortunated', self.unfortunated
        yield 'silent', self.silent

    def __truediv__(self, other):
        return self.save(map(lambda o: {o[0][0]: 0 if o[1][1] == 0 else o[0][1] / o[1][1]}, zip(self, other)))

    def __mul__(self, other):
        if isinstance(other, float) or isinstance(other, int):
            return self.save(map(lambda x: {x[0]: x[1] * other}, self))
        else:
            return self.save(map(lambda o: {o[0][0]: o[0][1] * o[1][1]}, zip(self, other)))

    def save(self, itter):
        for i in list(itter):
            setattr(self, list(i.keys())[0], i.get(list(i.keys())[0]))
        return self

    def __str__(self):
        return 'Conceded = {0}, selfish = {1}, nice = {2}, fortunate = {3}, unfortunated = {4}, silent = {5}\n'.format(
            self.conceded,
            self.selfish,
            self.nice,
            self.fortunated,
            self.unfortunated,
            self.silent
        )
