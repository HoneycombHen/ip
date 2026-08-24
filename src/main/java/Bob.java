import java.util.Scanner;

public class Bob {
    public static void main(String[] args) {
        String banner = " ____        _     \n"
                + "| __ )  ___ | |__  \n"
                + "|  _ \\ / _ \\| '_ \\ \n"
                + "| |_) | (_) | |_) |\n"
                + "|____/ \\___/|_.__/ \n";
        String hor_line = "____________________________________________________________";
        System.out.println(hor_line);
        System.out.println(banner);
        System.out.println("Hello! I'm Bob.\n" +
                "What can I do for you?");
        System.out.println(hor_line);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(hor_line);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(hor_line);
                break;
            }

            System.out.println(command);
            System.out.println(hor_line);
        }

    }
}
