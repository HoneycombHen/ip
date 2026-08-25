import java.util.Scanner;

public class Bob {
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

    public static void main(String[] args) {
        String banner = " ____        _     \n"
                + "| __ )  ___ | |__  \n"
                + "|  _ \\ / _ \\| '_ \\ \n"
                + "| |_) | (_) | |_) |\n"
                + "|____/ \\___/|_.__/ \n";

        String horizontalLine =
                "____________________________________________________________";

        System.out.println(horizontalLine);
        System.out.println(banner);
        System.out.println("Hello! I'm Bob.\n"
                + "What can I do for you?");
        System.out.println(horizontalLine);

        Task[] taskList = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            } else if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:\n");

                for (int i = 0; i < taskCount; i++) {
                    System.out.printf("%d.%s%n", i + 1, taskList[i]);
                }

                System.out.println(horizontalLine);
            } else if (command.equals("mark")
                    || command.startsWith("mark ")) {
                String[] parts = command.split("\\s+");
                int index = getTaskIndex(parts, taskCount, "mark");

                if (index == -1) {
                    continue;
                }

                Task task = taskList[index];
                task.setDone();

                System.out.println("Nice! I've marked this task as done:\n"
                        + "  " + task);
                System.out.println(horizontalLine);
            } else if (command.equals("unmark")
                    || command.startsWith("unmark ")) {
                String[] parts = command.split("\\s+");
                int index = getTaskIndex(parts, taskCount, "unmark");

                if (index == -1) {
                    continue;
                }

                Task task = taskList[index];
                task.setUndone();

                System.out.println("OK, I've marked this task as not done yet:\n"
                        + "  " + task);
                System.out.println(horizontalLine);
            } else {
                if (taskCount < taskList.length) {
                    taskList[taskCount] = new Task(command);
                    taskCount++;

                    System.out.println("added: " + command);
                } else {
                    System.out.println("Your task list is full.");
                }
            }
        }
    }
}