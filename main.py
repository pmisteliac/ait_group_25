#!/usr/bin/python3

import json
import sys
import os

from src.Agent import Agent
from src.Markov import Markov

if len(sys.argv) == 2:
    src = str(sys.argv[1])
else:
    src = 'data'

markov = Markov()

for root, dirs, files in os.walk(src, topdown=True):
    for name in files:
        print('running:', name)
        with open(os.path.join(src, name)) as file:
            run = json.load(file)
            agent1 = Agent(list(run['issues'].keys()), run['Utility1'])
            agent2 = Agent(list(run['issues'].keys()), run['Utility2'])
            for bid in run['bids']:
                if 'agent1' in bid:
                    agent1.processBid(bid['agent1'])
                    agent1.updateAction(agent2.getUtilty(bid['agent1']))
                if 'agent2' in bid:
                    agent2.processBid(bid['agent2'])
                    agent2.updateAction(agent1.getUtilty(bid['agent2']))
                if 'accept' in bid:
                    break

            print(str(agent1.actions))
            print(str(agent2.actions))
            markov.update(name, agent1.actions, agent2.actions)

print(markov)

for root, dirs, files in os.walk(src, topdown=True):
    for name in files:
        print('running:', name)
        with open(os.path.join(src, name)) as file:
            run = json.load(file)
            agent1 = Agent(list(run['issues'].keys()), run['Utility1'])
            agent2 = Agent(list(run['issues'].keys()), run['Utility2'])
            for bid in run['bids']:
                if 'agent1' in bid:
                    agent1.processBid(bid['agent1'])
                    agent1.updateAction(agent2.getUtilty(bid['agent1']))
                if 'agent2' in bid:
                    agent2.processBid(bid['agent2'])
                    agent2.updateAction(agent1.getUtilty(bid['agent2']))
                if 'accept' in bid:
                    for i in markov.getChances(agent1.actions, agent2.actions):
                        print('AAA: ', list(i.keys())[0], i.get(list(i.keys())[0]))
                    break
