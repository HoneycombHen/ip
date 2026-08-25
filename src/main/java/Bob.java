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
     * @return the zero-based task index, or -1 if the input is invalid
     */
    private static int getTaskIndex(String[] parts, int taskCount, String commandName) {
        if (parts.length != 2) {
            System.out.println("Please use the format: " + commandName + " <task number>");
            return -1;
        }

        try {
            int taskNumber = Integer.parseInt(parts[1]);

            if (taskNumber < 1 || taskNumber > taskCount) {
                System.out.println("That task number does not exist.");
                return -1;
            }

            return taskNumber - 1;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid task number.");
            return -1;
        }
    }

    /**
     * Creates a task from a todo, deadline, or event command.
     * Dates and times are intentionally kept as strings for this level.
     *
     * @param input the trimmed user command
     * @return a new task, or null if the command is invalid or unknown
     */
    private static Task createTask(String input) {
        if (input.startsWith("todo ")) {
            String description = input.substring("todo ".length()).trim();
            if (description.isEmpty()) {
                System.out.println("Error: A todo must have a description.");
                return null;
            }
            return new Todo(description);
        }

        if (input.startsWith("deadline ")) {
            String content = input.substring("deadline ".length()).trim();
            String[] parts = content.split("\\s+/by\\s+", 2);

            if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                System.out.println("Error: Missing '/by' tag or deadline details.");
                return null;
            }

            return new Deadline(parts[0].trim(), parts[1].trim());
        }

        if (input.startsWith("event ")) {
            String content = input.substring("event ".length()).trim();
            int fromIndex = content.indexOf("/from");
            int toIndex = fromIndex < 0 ? -1 : content.indexOf("/to", fromIndex + "/from".length());

            if (fromIndex <= 0 || toIndex <= fromIndex + "/from".length()) {
                System.out.println("Error: Please provide valid /from and /to tags in order.");
                return null;
            }

            String description = content.substring(0, fromIndex).trim();
            String from = content.substring(fromIndex + "/from".length(), toIndex).trim();
            String to = content.substring(toIndex + "/to".length()).trim();

            if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                System.out.println("Error: Event description, /from, and /to details are required.");
                return null;
            }

            return new Event(description, from, to);
        }

        System.out.println("Please use a proper command!");
        return null;
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
        System.out.println("Hello! I'm Bob.\n" +
                "What can I do for you?");
        System.out.println(horizontalLine);

        Task[] taskList = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            }

            else if (input.equals("list")) {
                System.out.println("Here are the tasks in your list:\n");
                for (int j = 0; j < taskCount; j++) {
                    Task task = taskList[j];
                    System.out.printf("%d.%s%n", (j + 1), task);
                }
                System.out.println(horizontalLine);
            }

            else if (input.equals("mark") || input.startsWith("mark ")) {
                String[] parts = input.trim().split("\\s+");
                int index = getTaskIndex(parts, taskCount, "mark");

                if (index == -1) {
                    continue;
                }

                Task task = taskList[index];
                task.setDone();

                System.out.println("Nice! I've marked this task as done:\n"
                        + "  [X] " + task.getDescription());
                System.out.println(horizontalLine);
            }

            else if (input.equals("unmark") || input.startsWith("unmark ")) {
                String[] parts = input.split("\\s+");
                int index = getTaskIndex(parts, taskCount, "unmark");

                if (index == -1) {
                    continue;
                }

                Task task = taskList[index];
                System.out.println("OK, I've marked this task as not done yet:\n" +
                        "  [ ] " + task.getDescription());
                task.setUndone();
                System.out.println(horizontalLine);
            }

            else {
                Task newTask = createTask(input);

                if (newTask == null) {
                    continue;
                }

                if (taskCount >= taskList.length) {
                    System.out.println("Your task list is full.");
                    continue;
                }

                taskList[taskCount] = newTask;
                taskCount++;
                printAddedTask(newTask, taskCount, horizontalLine);
            }
        }
    }

}
