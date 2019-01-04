#!/usr/bin/python3.7

import json
import sys
import os
import argparse

if sys.version_info[0] == 3 and sys.version_info[1] < 7:
    print("This script requires at least Python version 3.7")
    sys.exit(1)

parser = argparse.ArgumentParser(description='Run rl algorithm on a bidding sequence')
parser.add_argument(
    '--srcDir',
    type=str,
    nargs=1,
    help='The source directory containing the train data. Files name should be named <agent1>_<agent2> or <Agent1><Agent2>',
    default=['data']
)
parser.add_argument(
    '--testDir',
    type=str,
    nargs=1,
    help='The source directory containing the to analyse data',
    default=['test']
)

args = parser.parse_args()
src = args.srcDir[0]
testDir = args.testDir[0]

from src.Agent import Agent
from src.Markov import Markov

markov = Markov()

for _, _, files in os.walk(src, topdown=True):
    for name in files:
        print('running:', name)
        with open(os.path.join(src, name)) as file:
            run = json.load(file)
            if len(run.get('bids')) < 6:
                continue
            agent1 = Agent(list(run['issues'].keys()), run['Utility1'])
            agent2 = Agent(list(run['issues'].keys()), run['Utility2'])
            for bid in run['bids']:
                if int(bid['round']) > 100:
                    break
                if 'agent1' in bid:
                    agent1.processBid(bid['agent1'])
                    agent1.updateAction(agent2.getUtilty(bid['agent1']))
                if 'agent2' in bid:
                    agent2.processBid(bid['agent2'])
                    agent2.updateAction(agent1.getUtilty(bid['agent2']))
                if 'accept' in bid:
                    break

            markov.update(name, agent1.actions, agent2.actions)

markov.startChecking()

for _, _, files in os.walk(testDir, topdown=True):
    for name in files:
        with open(os.path.join(testDir, name)) as file:
            run = json.load(file)
            print('running: {0} len {1}'.format(name, len(run.get('bids'))))
            agent1 = Agent(list(run['issues'].keys()), run['Utility1'])
            agent2 = Agent(list(run['issues'].keys()), run['Utility2'])
            for bid in run['bids']:
                if int(bid['round']) > 100:
                    break
                if 'agent1' in bid:
                    agent1.processBid(bid['agent1'])
                    agent1.updateAction(agent2.getUtilty(bid['agent1']))
                    markov.updateChances(agent1.getLastAction())
                if 'agent2' in bid:
                    agent2.processBid(bid['agent2'])
                    agent2.updateAction(agent1.getUtilty(bid['agent2']))
                if 'accept' in bid:
                    break
            print(markov.evidence)
            print(markov.getMostLikely())
