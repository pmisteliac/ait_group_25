from functools import reduce

import copy
import operator

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
        for i in action:
            setattr(self, *i)

    def __iadd__(self, other):
        return self.calc(other, operator.add)

    def getChance(self, action: Actions):
        count = reduce(lambda x, y: x + y[1], self, 0)
        countActions = reduce(lambda x, y: x + y[1], action, 0)
        return (self / action) * (countActions / count)

    def __iter__(self):
        for i in self.__dict__:
            yield i, getattr(self, i)

    def __truediv__(self, other):
        return self.save(map(lambda o: {o[0][0]: 0 if o[1][1] == 0 else o[0][1] / o[1][1]}, zip(self, other)))

    def __mul__(self, other):
        return self.calc(other, operator.mul)

    def calc(self, other, operator):
        if isinstance(other, float) or isinstance(other, int):
            return self.save(map(lambda x: {x[0]: operator(x[1], other)}, self))
        else:
            return self.save(map(lambda o: {o[0][0]: operator(o[0][1], o[1][1])}, zip(self, other)))

    def save(self, itter):
        for i in list(itter):
            setattr(self, *i, i.get(*i))
        return self

    def __str__(self):
        return ''.join(map(lambda x: '{0} = {1}, '.format(*x), self)).rstrip(', ') + '\n'
