package dev.abakar.agenttest.history;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import dev.abakar.agenttest.agent.AgentRun;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Un parcours enregistré en base, avec les bugs qu'il a signalés. */
@Entity
@Table(name = "agent_runs")
public class RunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String site;

    @Column(length = 2000)
    private String instruction;

    private String endedBy;

    private double costUsd;

    private int actionCount;

    @Column(length = 10000)
    private String finalText;

    private Instant createdAt;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<BugEntity> bugs = new ArrayList<>();

    protected RunEntity() {
    }

    public RunEntity(String site, AgentRun run, Instant createdAt) {
        this.site = site;
        this.instruction = run.instruction();
        this.endedBy = run.endedBy();
        this.costUsd = run.costUsd();
        this.actionCount = run.actions().size();
        this.finalText = run.finalText();
        this.createdAt = createdAt;
        run.report().bugs().forEach(bug -> bugs.add(new BugEntity(this, bug)));
    }

    public Long getId() { return id; }
    public String getSite() { return site; }
    public String getInstruction() { return instruction; }
    public String getEndedBy() { return endedBy; }
    public double getCostUsd() { return costUsd; }
    public int getActionCount() { return actionCount; }
    public String getFinalText() { return finalText; }
    public Instant getCreatedAt() { return createdAt; }
    public List<BugEntity> getBugs() { return bugs; }
}
