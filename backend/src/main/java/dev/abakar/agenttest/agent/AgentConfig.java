package dev.abakar.agenttest.agent;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Lit les limites et les prix dans application.properties. */
@Configuration
public class AgentConfig {

    @Bean
    AgentLimits agentLimits(
            @Value("${agent.limits.max-actions}") int maxActions,
            @Value("${agent.limits.max-duration}") Duration maxDuration,
            @Value("${agent.limits.max-budget-usd}") double maxBudgetUsd) {
        return new AgentLimits(maxActions, maxDuration, maxBudgetUsd);
    }

    @Bean
    Pricing pricing(
            @Value("${agent.claude.price-input-per-million}") double input,
            @Value("${agent.claude.price-output-per-million}") double output) {
        return new Pricing(input, output);
    }

    // Une horloge injectée permet aux tests de simuler le temps qui passe.
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
