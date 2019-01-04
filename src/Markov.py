from __future__ import annotations
from functools import reduce

import copy
import operator

from src.Actions import Actions
from inflection import camelize

class Markov(object):
    data = {}
    evidence = {}

    def update(self, name: str, actions1: Actions, actions2: Actions) -> None:
        name = Markov.getName(name)

        if name in self.data:
            self.data[name] += actions1
        else:
            self.data[name] = Data(actions1)

    @staticmethod
    def getName(name: str) -> str:
        return ''.join(filter(lambda x: x.isalpha(), camelize(name.split('.', maxsplit=1)[0])))

    def startChecking(self) -> None:
        self.evidence = Evidence(self.data.keys())

    def updateChances(self, action: str) -> None:
        if action != '':
            for i in map(lambda type: {type: getattr(self.evidence, type) + self.data[type].getEff(action)}, self.data):
                setattr(self.evidence, *i, i.get(*i))
            self.evidence /= self.evidence.getSum()

    def getChances(self, actionAgent: Actions, actionOppement: Actions):
        return map(lambda type: {type: copy.deepcopy(self.data[type]).getChance(actionAgent)}, self.data)

    def __str__(self):
        return ''\
            .join(str(self.evidence))

    def getMostLikely(self):
        random = 0.125

        t = {'random': 0, 'hardheaded': 0, 'conceder': 0, 'tft': 0}

        rCount = 0
        cCount = 0
        hCount = 0
        tCount = 0

        for i in self.evidence:
            if 'R' == i[0][0]:
              rCount += 1
            elif 'H' == i[0][0]:
                hCount += 1
            else:
                cCount += 1

        for i in self.evidence:
            if 'R' == i[0][0]:
                if i[1] > random:
                    t['random'] += i[1] / rCount
        for i in self.evidence:
            if 'H' == i[0][0]:
                t['hardheaded'] += i[1] / hCount
            elif 'R' == i[0][0]:
                if i[1] < 0.0001:
                    t['hardheaded'] += 0.2
                if i[1] < 10**-30:
                    t['hardheaded'] += 0.2
        for i in self.evidence:
            "Conceder looks more like random than the hardheaded"
            if 'C' == i[0][0]:
                t['conceder'] += i[1] / cCount
            elif 'R' == i[0][0]:
                if 0.0001 < i[1] < random:
                    t['conceder'] += 0.2
        for i in self.evidence:
            if 'T' == i[0][0]:
                if i[1] > 0.07:
                    t['tft'] += i[1]
                    tCount += 1

        t['random'] *= 4
        t['hardheaded'] *= 6
        t['conceder'] *= 10

        if tCount == 2:
            t['tft'] *= 40
        else:
            t['tft'] *= 2

        print(t)

        if 0.1 < (t['random']):
            return 'random'
        if t['hardheaded'] > t['conceder']:
            if t['tft'] < t['hardheaded']:
                return 'hardheaded'
            return 'tft'

        if t['tft'] > t['conceder']:
            return  'tft'

        return 'conceder'

# noinspection PyUnresolvedReferences
#- well I just want Python 4.
class Math(object):

    def __iadd__(self, other) -> Math:
        return self.calc(other, operator.add)

    def __iter__(self) -> iter:
        for i in self.__dict__:
            yield i, getattr(self, i)

    def __truediv__(self, other) -> Math:
        if isinstance(other, float) or isinstance(other, int):
            return self.calc(other, operator.truediv)
        else:
            return self.save(map(lambda o: {o[0][0]: 0 if o[1][1] == 0 else o[0][1] / o[1][1]}, zip(self, other)))

    def __mul__(self, other) -> Math:
        return self.calc(other, operator.mul)

    def __pow__(self, power, modulo=None) -> Math:
        return self.calc(operator, operator.pow)

    def calc(self, other, operator) -> Math:
        if isinstance(other, float) or isinstance(other, int):
            return self.save(map(lambda x: {x[0]: operator(x[1], other)}, self))
        else:
            return self.save(map(lambda o: {o[0][0]: operator(o[0][1], o[1][1])}, zip(self, other)))

    def save(self, itter) -> Data:
        for i in list(itter):
            setattr(self, *i, i.get(*i))
        return self

    def __str__(self) -> str:
        return ''.join(map(lambda x: '{0} = {1}, '.format(*x), self)).rstrip(', ') + '\n'

class Evidence(Math):

    def __init__(self, data):
        for i in data:
            setattr(self, i, 1 / len(data))

    def __str__(self):
        return '' \
            .join(map(lambda x: "{0}: {1}\n".format(*x), self))

    def getSum(self) -> float:
        return reduce(lambda x, y: x + y[1], self, 0)

# noinspection PyUnresolvedReferences
#- well I just want Python 4.
class Data(Math):

    def __init__(self, action: Actions):
        for i in action:
            setattr(self, *i)

    def getEff(self, action: str) -> float:
        """
        :param string action: Action taken by the agent.
        :return float:
        """
        return getattr(self, action) / Data.getCount(self)

    def getChance(self, action: Actions) -> Data:
        """
        :param Actions action: List of taken actions by the agent.
        :return self:
        """
        count = Data.getCount(self)
        countActions = Data.getCount(action)
        return (self / action) * (countActions / count)

    @staticmethod
    def getCount(data) -> int:
        return reduce(lambda x, y: x + y[1], data, 0)
