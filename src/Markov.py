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
        for i in self.data.keys():
            self.evidence[i] = 1 / len(self.data.keys())

    def updateChances(self, action: str) -> None:
        if action != '':
            for i in map(lambda type: {type: self.evidence[type] + self.data[type].getEff(action)}, self.data):
                self.evidence[[*i][0]] = i.get(*i)
            for i in self.evidence:
                self.evidence[i] = self.evidence[i] / sum(self.evidence.values())


    def getChances(self, actionAgent: Actions, actionOppement: Actions):
        return map(lambda type: {type: copy.deepcopy(self.data[type]).getChance(actionAgent)}, self.data)

    def __str__(self):
        return ''\
            .join(map(lambda x: "{0}: {1}\n".format(x, self.data[x]), self.data))\
            .join(map(lambda x: "{0}: {1}\n".format(x, self.evidence[x]), self.evidence))


# noinspection PyUnresolvedReferences
#- well I just want Python 4.
class Data(object):

    def __init__(self, action: Actions):
        for i in action:
            setattr(self, *i)

    def __iadd__(self, other) -> Data:
        return self.calc(other, operator.add)

    def getEff(self, action: str) -> float:
        """
        :param string action: Action taken by the agent.
        :return float:
        """
        return getattr(self, action) / Data.getCount(self)

    def getChance(self, action: Actions) -> Data:
        """
        TODO this method must return chances for each type.
        :param Actions action: List of taken actions by the agent.
        :return self:
        """
        count = Data.getCount(self)
        countActions = Data.getCount(action)
        return (self / action) * (countActions / count)

    @staticmethod
    def getCount(data) -> int:
        return reduce(lambda x, y: x + y[1], data, 0)

    def __iter__(self) -> iter:
        for i in self.__dict__:
            yield i, getattr(self, i)

    def __truediv__(self, other) -> object:
        return self.save(map(lambda o: {o[0][0]: 0 if o[1][1] == 0 else o[0][1] / o[1][1]}, zip(self, other)))

    def __mul__(self, other) -> Data:
        return self.calc(other, operator.mul)

    def calc(self, other, operator) -> Data:
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
