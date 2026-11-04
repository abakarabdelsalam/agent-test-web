package dev.abakar.agenttest.agent;

import java.time.Duration;

/** Les trois garde-fous d'un parcours autonome : nombre d'actions, durée et budget en dollars. */
public record AgentLimits(int maxActions, Duration maxDuration, double maxBudgetUsd) {
}
