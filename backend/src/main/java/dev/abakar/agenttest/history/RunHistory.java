package dev.abakar.agenttest.history;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import dev.abakar.agenttest.agent.AgentRun;
import dev.abakar.agenttest.history.RunDetail.BugView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Enregistre les parcours, les relit et enregistre la validation humaine des bugs. */
@Service
public class RunHistory implements RunRecorder {

    private final RunRepository runs;
    private final BugRepository bugs;
    private final Clock clock;

    public RunHistory(RunRepository runs, BugRepository bugs, Clock clock) {
        this.runs = runs;
        this.bugs = bugs;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void record(String site, AgentRun run) {
        runs.save(new RunEntity(site, run, clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<RunSummary> recent() {
        return runs.findAllByOrderByIdDesc().stream().map(RunHistory::summary).toList();
    }

    @Transactional(readOnly = true)
    public Optional<RunDetail> find(long id) {
        return runs.findById(id).map(run -> new RunDetail(
                run.getId(), run.getSite(), run.getInstruction(), run.getEndedBy(), run.getCostUsd(),
                run.getActionCount(), run.getFinalText(), run.getCreatedAt(),
                run.getBugs().stream().map(RunHistory::view).toList()));
    }

    @Transactional
    public Optional<BugView> validate(long bugId, ValidationStatus status) {
        return bugs.findById(bugId).map(bug -> {
            bug.setStatus(status);
            return view(bug);
        });
    }

    private static RunSummary summary(RunEntity run) {
        return new RunSummary(run.getId(), run.getSite(), run.getInstruction(), run.getEndedBy(),
                run.getCostUsd(), run.getActionCount(), run.getBugs().size(), run.getCreatedAt());
    }

    private static BugView view(BugEntity bug) {
        return new BugView(bug.getId(), bug.getSpec(), bug.getTitle(), bug.getExpected(), bug.getActual(),
                bug.getSeverity(), bug.getSteps(), bug.getStatus());
    }
}
