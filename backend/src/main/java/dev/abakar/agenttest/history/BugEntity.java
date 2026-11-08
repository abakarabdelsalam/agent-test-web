package dev.abakar.agenttest.history;

import java.util.List;

import dev.abakar.agenttest.report.BugReport;
import dev.abakar.agenttest.report.Severity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Un bug signalé par un parcours, avec la décision prise dessus. */
@Entity
@Table(name = "agent_bugs")
public class BugEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id")
    private RunEntity run;

    private String spec;

    private String title;

    @Column(length = 2000)
    private String expected;

    @Column(length = 2000)
    private String actual;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @ElementCollection
    @CollectionTable(name = "agent_bug_steps", joinColumns = @JoinColumn(name = "bug_id"))
    @OrderColumn(name = "position")
    @Column(length = 1000)
    private List<String> steps;

    // Un bug naît « en attente » : c'est à une personne de le valider ou de le rejeter.
    @Enumerated(EnumType.STRING)
    private ValidationStatus status = ValidationStatus.EN_ATTENTE;

    protected BugEntity() {
    }

    BugEntity(RunEntity run, BugReport bug) {
        this.run = run;
        this.spec = bug.spec();
        this.title = bug.title();
        this.expected = bug.expected();
        this.actual = bug.actual();
        this.severity = bug.severity();
        this.steps = List.copyOf(bug.steps());
    }

    public Long getId() { return id; }
    public String getSpec() { return spec; }
    public String getTitle() { return title; }
    public String getExpected() { return expected; }
    public String getActual() { return actual; }
    public Severity getSeverity() { return severity; }
    public List<String> getSteps() { return steps; }
    public ValidationStatus getStatus() { return status; }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }
}
