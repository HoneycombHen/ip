import java.time.temporal.Temporal;

/**
 * A task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    private final Temporal from;
    private final Temporal to;

    /**
     * Creates an event and parses its start and end date or time details.
     *
     * @param description description of the event
     * @param from ISO start date or local date-time detail
     * @param to ISO end date or local date-time detail
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = DateTimeParser.parse(from);
        this.to = DateTimeParser.parse(to);
    }

    /**
     * Returns the stored event start date or local date-time.
     *
     * @return stored start date or local date-time
     */
    public Temporal getFrom() {
        return from;
    }

    /**
     * Returns the stored event end date or local date-time.
     *
     * @return stored end date or local date-time
     */
    public Temporal getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + DateTimeParser.format(from) + " to: " + DateTimeParser.format(to)
                + ")";
    }
}
