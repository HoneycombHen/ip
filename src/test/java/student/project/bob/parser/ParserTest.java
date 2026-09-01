package student.project.bob.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import student.project.bob.exception.BobException;
import student.project.bob.model.Deadline;
import student.project.bob.model.Event;
import student.project.bob.model.Todo;

/**
 * Tests command, task, and argument parsing behavior.
 */
public class ParserTest {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, upcoming, on, overdue, or bye.";

    /**
     * Verifies that every supported command prefix is classified correctly.
     */
    @Test
    public void parseCommand_supportedCommands_returnsExpectedTypes() throws BobException {
        Parser parser = new Parser();

        assertAll(
                () -> assertEquals(Command.Type.BYE, parser.parseCommand("bye").getType()),
                () -> assertEquals(
                        Command.Type.LIST, parser.parseCommand("list").getType()),
                () -> assertEquals(
                        Command.Type.UPCOMING, parser.parseCommand("upcoming 3").getType()),
                () -> assertEquals(
                        Command.Type.ON, parser.parseCommand("on 2026-01-01").getType()),
                () -> assertEquals(
                        Command.Type.OVERDUE, parser.parseCommand("overdue").getType()),
                () -> assertEquals(
                        Command.Type.MARK, parser.parseCommand("mark 1").getType()),
                () -> assertEquals(
                        Command.Type.UNMARK, parser.parseCommand("unmark 1").getType()),
                () -> assertEquals(
                        Command.Type.DELETE, parser.parseCommand("delete 1").getType()),
                () -> assertEquals(
                        Command.Type.TASK, parser.parseCommand("todo read book").getType()),
                () -> assertEquals(
                        Command.Type.TASK,
                        parser.parseCommand("deadline report /by 2026-01-01").getType()),
                () -> assertEquals(
                        Command.Type.TASK,
                        parser.parseCommand("event meeting /from 2026-01-01 /to 2026-01-02")
                                .getType()));
    }

    /**
     * Verifies that an unsupported command produces the documented error.
     */
    @Test
    public void parseCommand_unknownCommand_throwsBobException() {
        Parser parser = new Parser();

        BobException exception = assertThrows(BobException.class, () -> parser.parseCommand("unknown"));

        assertEquals(UNKNOWN_COMMAND_MESSAGE, exception.getMessage());
    }

    /**
     * Verifies that todo, deadline, and event commands create the corresponding task types.
     */
    @Test
    public void parseTask_validCommands_returnsExpectedTasks() throws BobException {
        Parser parser = new Parser();
        Todo todo = assertInstanceOf(Todo.class, parser.parseTask("todo read book"));
        Deadline deadline = assertInstanceOf(Deadline.class, parser.parseTask("deadline submit report /by 2026-01-01"));
        Event event = assertInstanceOf(
                Event.class, parser.parseTask("event team sync /from 2026-01-01T09:00 /to 2026-01-01T10:00"));

        assertAll(
                () -> assertEquals("read book", todo.getDescription()),
                () -> assertEquals("[T][ ] read book", todo.toString()),
                () -> assertEquals(LocalDate.of(2026, 1, 1), deadline.getBy()),
                () -> assertEquals("submit report", deadline.getDescription()),
                () -> assertEquals("team sync", event.getDescription()),
                () -> assertEquals("2026-01-01T09:00", event.getFrom().toString()),
                () -> assertEquals("2026-01-01T10:00", event.getTo().toString()));
    }

    /**
     * Verifies that malformed task commands produce specific validation errors.
     */
    @Test
    public void parseTask_invalidCommands_throwsBobException() {
        Parser parser = new Parser();

        assertAll(
                () -> assertEquals(
                        "A todo must have a description.",
                        assertThrows(BobException.class, () -> parser.parseTask("todo"))
                                .getMessage()),
                () -> assertEquals(
                        "A deadline needs a description and a '/by' detail.",
                        assertThrows(BobException.class, () -> parser.parseTask("deadline report"))
                                .getMessage()),
                () -> assertEquals(
                        "A deadline's '/by' detail must be a valid date or time.",
                        assertThrows(BobException.class, () -> parser.parseTask("deadline report /by not-a-date"))
                                .getMessage()),
                () -> assertEquals(
                        "An event needs a description followed by '/from' and '/to' details.",
                        assertThrows(BobException.class, () -> parser.parseTask("event meeting"))
                                .getMessage()),
                () -> assertEquals(
                        "Event description, '/from', and '/to' details are required.",
                        assertThrows(BobException.class, () -> parser.parseTask("event meeting /from 2026-01-01 /to"))
                                .getMessage()),
                () -> assertEquals(
                        "An event's '/from' and '/to' details must be valid dates or times.",
                        assertThrows(
                                        BobException.class,
                                        () -> parser.parseTask("event meeting /from 2026-02-30 /to 2026-03-01"))
                                .getMessage()),
                () -> assertEquals(
                        UNKNOWN_COMMAND_MESSAGE,
                        assertThrows(BobException.class, () -> parser.parseTask("unknown"))
                                .getMessage()));
    }

    /**
     * Verifies the default, zero, and positive upcoming-day arguments.
     */
    @Test
    public void parseUpcomingDays_validArguments_returnsRequestedDays() throws BobException {
        Parser parser = new Parser();

        assertAll(
                () -> assertEquals(7, parser.parseUpcomingDays("upcoming")),
                () -> assertEquals(0, parser.parseUpcomingDays("upcoming 0")),
                () -> assertEquals(30, parser.parseUpcomingDays("upcoming 30")));
    }

    /**
     * Verifies that malformed upcoming-day arguments produce specific errors.
     */
    @Test
    public void parseUpcomingDays_invalidArguments_throwsBobException() {
        Parser parser = new Parser();

        assertAll(
                () -> assertEquals(
                        "Please enter a valid number of days.",
                        assertThrows(BobException.class, () -> parser.parseUpcomingDays("upcoming nope"))
                                .getMessage()),
                () -> assertEquals(
                        "Please enter a non-negative number of days.",
                        assertThrows(BobException.class, () -> parser.parseUpcomingDays("upcoming -1"))
                                .getMessage()),
                () -> assertEquals(
                        "Please use the format: upcoming [number of days].",
                        assertThrows(BobException.class, () -> parser.parseUpcomingDays("upcoming 1 2"))
                                .getMessage()));
    }

    /**
     * Verifies that a valid on command returns its date.
     */
    @Test
    public void parseOnDate_validDate_returnsLocalDate() throws BobException {
        Parser parser = new Parser();

        assertEquals(LocalDate.of(2026, 1, 1), parser.parseOnDate("on    2026-01-01"));
    }

    /**
     * Verifies that missing, invalid, and date-time on arguments are rejected.
     */
    @Test
    public void parseOnDate_invalidArguments_throwsBobException() {
        Parser parser = new Parser();
        String invalidDateMessage = "Please enter a valid date in the format: yyyy-MM-dd.";

        assertAll(
                () -> assertEquals(
                        "Please use the format: on <date>.",
                        assertThrows(BobException.class, () -> parser.parseOnDate("on"))
                                .getMessage()),
                () -> assertEquals(
                        invalidDateMessage,
                        assertThrows(BobException.class, () -> parser.parseOnDate("on not-a-date"))
                                .getMessage()),
                () -> assertEquals(
                        invalidDateMessage,
                        assertThrows(BobException.class, () -> parser.parseOnDate("on 2026-01-01T09:00"))
                                .getMessage()));
    }
}
