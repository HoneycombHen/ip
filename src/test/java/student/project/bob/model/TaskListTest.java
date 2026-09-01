package student.project.bob.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import student.project.bob.exception.BobException;

/**
 * Tests the task-number validation and conversion performed by {@link TaskList#getIndex}.
 */
public class TaskListTest {
    private static final String COMMAND_NAME = "mark";

    /**
     * Verifies that valid one-based task numbers are converted to zero-based indexes.
     */
    @Test
    public void getIndex_validTaskNumbers_returnsZeroBasedIndexes() throws BobException {
        TaskList taskList = new TaskList(List.of(new Todo("first"), new Todo("second"), new Todo("third")));

        assertAll(
                () -> assertEquals(0, taskList.getIndex(new String[] {COMMAND_NAME, "1"}, COMMAND_NAME)),
                () -> assertEquals(1, taskList.getIndex(new String[] {COMMAND_NAME, "2"}, COMMAND_NAME)),
                () -> assertEquals(2, taskList.getIndex(new String[] {COMMAND_NAME, "3"}, COMMAND_NAME)));
    }

    /**
     * Verifies that a missing or extra task number produces the command-format error.
     */
    @Test
    public void getIndex_missingOrExtraTaskNumber_throwsFormatException() {
        TaskList taskList = new TaskList(List.of(new Todo("task")));
        String expectedMessage = "Please use the format: mark <task number>.";

        assertAll(
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(new String[] {COMMAND_NAME}, COMMAND_NAME))
                                .getMessage()),
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(
                                                new String[] {COMMAND_NAME, "1", "extra"}, COMMAND_NAME))
                                .getMessage()));
    }

    /**
     * Verifies that non-numeric task numbers produce the invalid-number error.
     */
    @Test
    public void getIndex_nonNumericTaskNumber_throwsInvalidNumberException() {
        TaskList taskList = new TaskList(List.of(new Todo("task")));
        String expectedMessage = "Please enter a valid task number.";

        assertAll(
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(new String[] {COMMAND_NAME, "abc"}, COMMAND_NAME))
                                .getMessage()),
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(
                                                new String[] {COMMAND_NAME, "999999999999999999999"}, COMMAND_NAME))
                                .getMessage()));
    }

    /**
     * Verifies that task numbers outside the list range produce the task-not-found error.
     */
    @Test
    public void getIndex_outOfRangeTaskNumber_throwsTaskNotFoundException() {
        TaskList taskList = new TaskList(List.of(new Todo("first"), new Todo("second")));
        String expectedMessage = "That task number does not exist.";

        assertAll(
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(new String[] {COMMAND_NAME, "0"}, COMMAND_NAME))
                                .getMessage()),
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(new String[] {COMMAND_NAME, "-1"}, COMMAND_NAME))
                                .getMessage()),
                () -> assertEquals(
                        expectedMessage,
                        assertThrows(
                                        BobException.class,
                                        () -> taskList.getIndex(new String[] {COMMAND_NAME, "3"}, COMMAND_NAME))
                                .getMessage()));
    }

    /**
     * Verifies that no task number is valid when the task list is empty.
     */
    @Test
    public void getIndex_emptyTaskList_throwsTaskNotFoundException() {
        TaskList taskList = new TaskList();

        BobException exception = assertThrows(
                BobException.class, () -> taskList.getIndex(new String[] {COMMAND_NAME, "1"}, COMMAND_NAME));

        assertEquals("That task number does not exist.", exception.getMessage());
    }
}
