import java.util.Scanner;

public class Bob {
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

        String[] taskList = new String[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            }

            else if (command.equals("list")) {
                for (int j = 0; j < taskCount; j++) {
                    System.out.printf("%d. %s\n", (j + 1), taskList[j]);
                }
                System.out.println(horizontalLine);
            }

            else {
                if (taskCount < taskList.length) {
                    taskList[taskCount] = command;
                    taskCount++;
                    System.out.println("added: " + command);
                } else {
                    System.out.println("Your task list is full.");
                }
            }
        }

    }
}
