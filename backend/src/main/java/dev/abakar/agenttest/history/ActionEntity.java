package dev.abakar.agenttest.history;

import dev.abakar.agenttest.agent.AgentRun;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Une action exécutée pendant un parcours : la trace de ce que l'agent a fait, dans l'ordre. */
@Entity
@Table(name = "agent_actions")
public class ActionEntity {

    // Une page lue peut dépasser 10 000 caractères : on coupe avant d'écrire, pour ne pas échouer en base.
    private static final int MAX_RESULT_CHARS = 12000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id")
    private RunEntity run;

    private int position;

    private String tool;

    @Column(length = 1000)
    private String input;

    @Column(length = MAX_RESULT_CHARS)
    private String result;

    private boolean error;

    protected ActionEntity() {
    }

    ActionEntity(RunEntity run, int position, AgentRun.Action action) {
        this.run = run;
        this.position = position;
        this.tool = action.tool();
        this.input = truncate(String.valueOf(action.input()), 1000);
        this.result = truncate(action.result(), MAX_RESULT_CHARS);
        this.error = action.error();
    }

    private static String truncate(String text, int max) {
        return text.length() > max ? text.substring(0, max) : text;
    }

    public int getPosition() { return position; }
    public String getTool() { return tool; }
    public String getInput() { return input; }
    public String getResult() { return result; }
    public boolean isError() { return error; }
}
