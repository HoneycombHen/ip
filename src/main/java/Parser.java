import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

/**
 * Parses user commands into task data and command arguments.
 */
public class Parser {

    /**
     * Creates a parser for Bob commands.
     */
    public Parser() {}

    /**
     * Creates a task from a todo, deadline, or event command.
     * Date and time details are parsed by the deadline and event task classes.
     *
     * @param input the trimmed user command
     * @return a new task
     * @throws BobException if the command is invalid or unknown
     */
    public Task parseTask(String input) throws BobException {
        if (input.equals("todo") || input.startsWith("todo ")) {
            String description = input.length() == "todo".length()
                    ? ""
                    : input.substring("todo ".length()).trim();
            if (description.isEmpty()) {
                throw new BobException("A todo must have a description.");
            }
            return new Todo(description);
        }

        if (input.equals("deadline") || input.startsWith("deadline ")) {
            String content = input.length() == "deadline".length()
                    ? ""
                    : input.substring("deadline ".length()).trim();
            String[] parts = content.split("\\s+/by\\s+", 2);

            if (parts.length != 2
                    || parts[0].trim().isEmpty()
                    || parts[1].trim().isEmpty()) {
                throw new BobException("A deadline needs a description and a '/by' detail.");
            }

            try {
                return new Deadline(parts[0].trim(), parts[1].trim());
            } catch (DateTimeParseException e) {
                throw new BobException("A deadline's '/by' detail must be a valid date or time.");
            }
        }

        if (input.equals("event") || input.startsWith("event ")) {
            String content = input.length() == "event".length()
                    ? ""
                    : input.substring("event ".length()).trim();
            int fromIndex = content.indexOf("/from");
            int toIndex = fromIndex < 0 ? -1 : content.indexOf("/to", fromIndex + "/from".length());

            if (fromIndex <= 0 || toIndex <= fromIndex + "/from".length()) {
                throw new BobException("An event needs a description followed by '/from' and '/to' details.");
            }

            String description = content.substring(0, fromIndex).trim();
            String from =
                    content.substring(fromIndex + "/from".length(), toIndex).trim();
            String to = content.substring(toIndex + "/to".length()).trim();

            if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                throw new BobException("Event description, '/from', and '/to' details are required.");
            }

            try {
                return new Event(description, from, to);
            } catch (DateTimeParseException e) {
                throw new BobException("An event's '/from' and '/to' details must be valid dates or times.");
            }
        }

        throw new BobException(
                "I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, upcoming, on, overdue, or bye.");
    }

    /**
     * Parses the optional number of days for an upcoming command.
     *
     * @param input the trimmed upcoming command
     * @return the requested number of days, defaulting to seven
     * @throws BobException if the command contains an invalid number of days
     */
    public int parseUpcomingDays(String input) throws BobException {
        String[] parts = input.split("\\s+");
        if (parts.length == 1) {
            return 7;
        }
        if (parts.length != 2) {
            throw new BobException("Please use the format: upcoming [number of days].");
        }

        try {
            int days = Integer.parseInt(parts[1]);
            if (days < 0) {
                throw new BobException("Please enter a non-negative number of days.");
            }
            return days;
        } catch (NumberFormatException e) {
            throw new BobException("Please enter a valid number of days.");
        }
    }

    /**
     * Parses the date supplied to an on command.
     *
     * @param input the trimmed on command
     * @return the requested date
     * @throws BobException if the command does not contain a valid date
     */
    public LocalDate parseOnDate(String input) throws BobException {
        String dateInput = input.length() <= "on".length()
                ? ""
                : input.substring("on".length()).trim();
        if (dateInput.isEmpty()) {
            throw new BobException("Please use the format: on <date>.");
        }

        try {
            Temporal temporal = DateTimeParser.parse(dateInput);
            if (temporal instanceof LocalDate date) {
                return date;
            }
        } catch (DateTimeParseException e) {
            // Report the invalid date using Bob's normal input-error flow.
        }
        throw new BobException("Please enter a valid date in the format: yyyy-MM-dd.");
    }
}
