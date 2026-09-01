import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Runs the Bob task-management application.
 */
public class Bob {

    /**
     * Creates a task from a todo, deadline, or event command.
     * Date and time details are parsed by the deadline and event task classes.
     *
     * @param input the trimmed user command
     * @return a new task
     * @throws BobException if the command is invalid or unknown
     */
    private static Task createTask(String input) throws BobException {
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
    private static int getUpcomingDays(String input) throws BobException {
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
    private static LocalDate getOnDate(String input) throws BobException {
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

    /**
     * Selects tasks whose relevant date falls within the upcoming date range.
     *
     * @param taskList tasks to search
     * @param days number of days after today to include
     */
    private static ArrayList<Task> getUpcomingTasks(TaskList taskList, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return taskList.stream()
                .filter(task -> isUpcoming(task, today, endDate))
                .sorted(Comparator.comparing(Bob::getTaskDateTime))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Selects deadlines and events scheduled on a date.
     *
     * @param taskList tasks to search
     * @param date date to search for
     */
    private static ArrayList<Task> getTasksOnDate(TaskList taskList, LocalDate date) {
        return taskList.stream()
                .filter(task -> isOnDate(task, date))
                .sorted(Comparator.comparing(Bob::getTaskDateTime))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Checks whether a deadline or event is scheduled on a date.
     *
     * @param task task to inspect
     * @param date date to check
     * @return true if the task occurs on the date
     */
    private static boolean isOnDate(Task task, LocalDate date) {
        if (task instanceof Deadline) {
            return getTaskDateTime(task).toLocalDate().equals(date);
        }
        if (task instanceof Event event) {
            LocalDate fromDate = getTaskDateTime(event).toLocalDate();
            LocalDate toDate = getDateTime(event.getTo()).toLocalDate();
            return !date.isBefore(fromDate) && !date.isAfter(toDate);
        }
        return false;
    }

    /**
     * Converts a stored temporal value to a local date-time for comparisons.
     *
     * @param temporal date or local date-time to convert
     * @return local date-time representation
     */
    private static LocalDateTime getDateTime(Temporal temporal) {
        if (temporal instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return ((LocalDate) temporal).atStartOfDay();
    }

    /**
     * Selects incomplete deadlines that are past their due date or time.
     *
     * @param taskList tasks to search
     */
    private static ArrayList<Task> getOverdueTasks(TaskList taskList) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        return taskList.stream()
                .filter(task -> isOverdue(task, today, now))
                .sorted(Comparator.comparing(Bob::getTaskDateTime))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Checks whether a task is an incomplete deadline that has passed.
     *
     * @param task task to inspect
     * @param today current local date
     * @param now current local date-time
     * @return true if the task is an overdue deadline
     */
    private static boolean isOverdue(Task task, LocalDate today, LocalDateTime now) {
        if (!(task instanceof Deadline deadline) || task.isDone()) {
            return false;
        }

        Temporal temporal = deadline.getBy();
        if (temporal instanceof LocalDate date) {
            return date.isBefore(today);
        }
        return ((LocalDateTime) temporal).isBefore(now);
    }

    /**
     * Checks whether a deadline or event starts within the requested date range.
     *
     * @param task task to inspect
     * @param startDate first date in the range
     * @param endDate last date in the range
     * @return true if the task's relevant date is in the range
     */
    private static boolean isUpcoming(Task task, LocalDate startDate, LocalDate endDate) {
        if (!(task instanceof Deadline) && !(task instanceof Event)) {
            return false;
        }

        LocalDate taskDate = getTaskDateTime(task).toLocalDate();
        return !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
    }

    /**
     * Returns the date and time used to order a deadline or event.
     *
     * @param task task whose date or time should be returned
     * @return the deadline date or event start date and time
     */
    private static LocalDateTime getTaskDateTime(Task task) {
        Temporal temporal = task instanceof Deadline deadline ? deadline.getBy() : ((Event) task).getFrom();
        if (temporal instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return ((LocalDate) temporal).atStartOfDay();
    }

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList taskList;
        try {
            taskList = new TaskList(Storage.loadTasks());
        } catch (StorageException e) {
            taskList = new TaskList();
            ui.showStorageError("I couldn't load the saved tasks. Starting with an empty list.");
        }

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            if (input.equals("bye")) {
                ui.showGoodbye();
                break;
            } else if (input.equals("list")) {
                ui.showTaskList(taskList.asList());
            } else if (input.equals("upcoming") || input.startsWith("upcoming ")) {
                try {
                    int days = getUpcomingDays(input);
                    ui.showUpcomingTasks(getUpcomingTasks(taskList, days), days);
                } catch (BobException e) {
                    ui.showError(e);
                }
            } else if (input.equals("on") || input.startsWith("on ")) {
                try {
                    LocalDate date = getOnDate(input);
                    ui.showTasksOnDate(getTasksOnDate(taskList, date), date);
                } catch (BobException e) {
                    ui.showError(e);
                }
            } else if (input.equals("overdue") || input.startsWith("overdue ")) {
                if (input.equals("overdue")) {
                    ui.showOverdueTasks(getOverdueTasks(taskList));
                } else {
                    ui.showError(new BobException("Please use the format: overdue."));
                }
            } else if (input.equals("mark") || input.startsWith("mark ")) {
                String[] parts = input.trim().split("\\s+");
                int index;
                try {
                    index = taskList.getIndex(parts, "mark");
                } catch (BobException e) {
                    ui.showError(e);
                    continue;
                }

                Task task = taskList.get(index);
                task.setDone();
                saveTasks(taskList, ui);

                ui.showMarkedTask(task);
            } else if (input.equals("unmark") || input.startsWith("unmark ")) {
                String[] parts = input.split("\\s+");
                int index;
                try {
                    index = taskList.getIndex(parts, "unmark");
                } catch (BobException e) {
                    ui.showError(e);
                    continue;
                }

                Task task = taskList.get(index);
                ui.showUnmarkedTask(task);
                task.setUndone();
                saveTasks(taskList, ui);
                ui.showSeparator();
            } else if (input.equals("delete") || input.startsWith("delete ")) {
                String[] parts = input.split("\\s+");
                int index;
                try {
                    index = taskList.getIndex(parts, "delete");
                } catch (BobException e) {
                    ui.showError(e);
                    continue;
                }

                Task removedTask = taskList.remove(index);
                saveTasks(taskList, ui);

                ui.showDeletedTask(removedTask, taskList.size());
            } else {
                Task newTask;
                try {
                    newTask = createTask(input);

                } catch (BobException e) {
                    ui.showError(e);
                    continue;
                }

                taskList.add(newTask);
                saveTasks(taskList, ui);
                ui.showAddedTask(newTask, taskList.size());
            }
        }
    }

    /**
     * Saves tasks and reports a storage problem without terminating Bob.
     *
     * @param taskList current task list to save
     * @param ui user interface used to report a storage problem
     */
    private static void saveTasks(TaskList taskList, Ui ui) {
        try {
            Storage.saveTasks(taskList.asList());
        } catch (StorageException e) {
            ui.showStorageError("I couldn't save the task list.");
        }
    }
}
