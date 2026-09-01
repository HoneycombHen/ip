package student.project.bob;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import student.project.bob.exception.BobException;
import student.project.bob.model.Deadline;
import student.project.bob.model.Event;
import student.project.bob.model.Task;
import student.project.bob.model.TaskList;
import student.project.bob.parser.Command;
import student.project.bob.parser.Parser;
import student.project.bob.storage.Storage;
import student.project.bob.storage.StorageException;
import student.project.bob.ui.Ui;

/**
 * Runs the Bob task-management application.
 */
public class Bob {

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

    /**
     * Starts Bob and processes commands until the user says goodbye or input ends.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        Parser parser = new Parser();
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

            Command command;
            try {
                command = parser.parseCommand(input);
            } catch (BobException e) {
                ui.showError(e);
                continue;
            }

            switch (command.getType()) {
                case BYE -> {
                    ui.showGoodbye();
                    return;
                }
                case LIST -> ui.showTaskList(taskList.asList());
                case UPCOMING -> {
                    try {
                        int days = parser.parseUpcomingDays(command.getInput());
                        ui.showUpcomingTasks(getUpcomingTasks(taskList, days), days);
                    } catch (BobException e) {
                        ui.showError(e);
                    }
                }
                case ON -> {
                    try {
                        LocalDate date = parser.parseOnDate(command.getInput());
                        ui.showTasksOnDate(getTasksOnDate(taskList, date), date);
                    } catch (BobException e) {
                        ui.showError(e);
                    }
                }
                case OVERDUE -> {
                    if (command.getInput().equals("overdue")) {
                        ui.showOverdueTasks(getOverdueTasks(taskList));
                    } else {
                        ui.showError(new BobException("Please use the format: overdue."));
                    }
                }
                case MARK -> {
                    String[] parts = command.getInput().trim().split("\\s+");
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
                }
                case UNMARK -> {
                    String[] parts = command.getInput().split("\\s+");
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
                }
                case DELETE -> {
                    String[] parts = command.getInput().split("\\s+");
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
                }
                case TASK -> {
                    Task newTask;
                    try {
                        newTask = parser.parseTask(command.getInput());
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
