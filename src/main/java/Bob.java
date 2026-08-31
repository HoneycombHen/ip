import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs the Bob task-management application.
 */
public class Bob {

    /**
     * Converts a command's task number into a zero-based array index.
     *
     * @param parts command words, such as ["mark", "1"]
     * @param taskCount number of tasks currently stored
     * @param commandName command being validated
     * @return the zero-based task index
     * @throws BobException if the command does not contain a valid task number
     */
    private static int getTaskIndex(String[] parts, int taskCount, String commandName) throws BobException {
        if (parts.length != 2) {
            throw new BobException("Please use the format: " + commandName + " <task number>.");
        }

        try {
            int taskNumber = Integer.parseInt(parts[1]);

            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new BobException("That task number does not exist.");
            }

            return taskNumber - 1;
        } catch (NumberFormatException e) {
            throw new BobException("Please enter a valid task number.");
        }
    }

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

            return new Deadline(parts[0].trim(), parts[1].trim());
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

            return new Event(description, from, to);
        }

        throw new BobException(
                "I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.");
    }

    /**
     * Prints the common confirmation shown after adding a task.
     *
     * @param task task that was added
     * @param taskCount updated number of tasks
     * @param horizontalLine separator used by the interface
     */
    private static void printAddedTask(Task task, int taskCount, String horizontalLine) {
        System.out.println("Got it. I've added this task:\n");
        System.out.println(task);
        System.out.printf("Now you have %d tasks in the list.%n", taskCount);
        System.out.println(horizontalLine);
    }

    public static void main(String[] args) {
        String banner = " ____        _     \n"
                + "| __ )  ___ | |__  \n"
                + "|  _ \\ / _ \\| '_ \\ \n"
                + "| |_) | (_) | |_) |\n"
                + "|____/ \\___/|_.__/ \n";
        String horizontalLine = "____________________________________________________________";
        System.out.println(horizontalLine);
        System.out.println(banner);
        System.out.println("Hello! I'm Bob.\n" + "What can I do for you?");
        System.out.println(horizontalLine);

        ArrayList<Task> taskList;
        try {
            taskList = new ArrayList<>(Storage.loadTasks());
        } catch (StorageException e) {
            taskList = new ArrayList<>();
            printStorageError("I couldn't load the saved tasks. Starting with an empty list.", horizontalLine);
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            } else if (input.equals("list")) {
                System.out.println("Here are the tasks in your list:\n");
                for (int j = 0; j < taskList.size(); j++) {
                    Task task = taskList.get(j);
                    System.out.printf("%d.%s%n", (j + 1), task);
                }
                System.out.println(horizontalLine);
            } else if (input.equals("mark") || input.startsWith("mark ")) {
                String[] parts = input.trim().split("\\s+");
                int index;
                try {
                    index = getTaskIndex(parts, taskList.size(), "mark");
                } catch (BobException e) {
                    printError(e, horizontalLine);
                    continue;
                }

                Task task = taskList.get(index);
                task.setDone();
                saveTasks(taskList, horizontalLine);

                System.out.println("Nice! I've marked this task as done:\n" + "  [X] " + task.getDescription());
                System.out.println(horizontalLine);
            } else if (input.equals("unmark") || input.startsWith("unmark ")) {
                String[] parts = input.split("\\s+");
                int index;
                try {
                    index = getTaskIndex(parts, taskList.size(), "unmark");
                } catch (BobException e) {
                    printError(e, horizontalLine);
                    continue;
                }

                Task task = taskList.get(index);
                System.out.println("OK, I've marked this task as not done yet:\n" + "  [ ] " + task.getDescription());
                task.setUndone();
                saveTasks(taskList, horizontalLine);
                System.out.println(horizontalLine);
            } else if (input.equals("delete") || input.startsWith("delete ")) {
                String[] parts = input.split("\\s+");
                int index;
                try {
                    index = getTaskIndex(parts, taskList.size(), "delete");
                } catch (BobException e) {
                    printError(e, horizontalLine);
                    continue;
                }

                Task removedTask = taskList.remove(index);
                saveTasks(taskList, horizontalLine);

                System.out.println("Noted. I've removed this task:\n" + "    " + removedTask);
                System.out.printf("Now you have %d tasks in the list.%n", taskList.size());
                System.out.println(horizontalLine);
            } else {
                Task newTask;
                try {
                    newTask = createTask(input);

                } catch (BobException e) {
                    printError(e, horizontalLine);
                    continue;
                }

                taskList.add(newTask);
                saveTasks(taskList, horizontalLine);
                printAddedTask(newTask, taskList.size(), horizontalLine);
            }
        }
    }

    /**
     * Displays an input error without terminating the command loop.
     *
     * @param exception input error to explain
     * @param horizontalLine separator used by the interface
     */
    private static void printError(BobException exception, String horizontalLine) {
        System.out.println("Oops! " + exception.getMessage());
        System.out.println(horizontalLine);
    }

    /**
     * Saves tasks and reports a storage problem without terminating Bob.
     *
     * @param taskList current task list to save
     * @param horizontalLine separator used by the interface
     */
    private static void saveTasks(ArrayList<Task> taskList, String horizontalLine) {
        try {
            Storage.saveTasks(taskList);
        } catch (StorageException e) {
            printStorageError("I couldn't save the task list.", horizontalLine);
        }
    }

    /**
     * Displays a storage error without terminating the command loop.
     *
     * @param message user-facing explanation of the storage problem
     * @param horizontalLine separator used by the interface
     */
    private static void printStorageError(String message, String horizontalLine) {
        System.out.println("Oops! " + message);
        System.out.println(horizontalLine);
    }
}
