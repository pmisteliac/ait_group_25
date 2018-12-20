#!/usr/bin/python3

import json
import sys

from src.Agent import Agent

if (len(sys.argv) == 2):
    fileName = 'data/' + str(sys.argv[1]) + '.json'
else:
    fileName = 'data/conceder_conceder.json'

with open(fileName) as file:
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
