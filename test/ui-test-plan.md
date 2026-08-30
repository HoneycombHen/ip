# UI Test Plan

This file is the source of truth for the `test-ui` project skill. Keep test cases in execution order and update the session record after each run.

## Run instructions

- Working directory: project root
- Prerequisites: Java 25 (`java --version` and `javac --version`)
- Build command: `gradlew.bat classes`
- Run command: `java -cp build/classes/java/main Bob`
- Output comparison: compare stdout after removing trailing spaces from each line; line content and exit status must otherwise match exactly, and stderr must be empty. This normalization keeps terminal-only padding from being treated as a Markdown trailing-space violation.

## Test cases

### UI-001: Empty todo and unknown command

**Aim:** Verify that the two example-style input errors are reported clearly and that Bob continues running afterwards.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
todo
blah
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A todo must have a description.
____________________________________________________________
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-002: Structured command errors

**Aim:** Verify that malformed deadline, event, mark, and unmark commands produce specific errors without terminating the application.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline project
event meeting
mark
unmark nope
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A deadline needs a description and a '/by' detail.
____________________________________________________________
Oops! An event needs a description followed by '/from' and '/to' details.
____________________________________________________________
Oops! Please use the format: mark <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-003: Valid tasks and invalid task number

**Aim:** Verify that valid task creation and marking still work, while an out-of-range task number is handled as an error.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
todo buy milk
deadline submit report /by Friday
event meeting /from Monday /to Tuesday
mark 4
mark 9
list
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] buy milk
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][ ] meeting (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

If output is intentionally variable, document the allowed variation and comparison rule in that case.

### UI-004: Delete a task and renumber the list

**Aim:** Verify that a valid delete removes the selected task, reports the removed task, updates the task count, and renumbers the remaining tasks.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
todo read book
todo return book
delete 1
list
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[T][ ] return book
Now you have 2 tasks in the list.
____________________________________________________________
Noted. I've removed this task:
    [T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] return book
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-005: Invalid delete commands

**Aim:** Verify that delete validates the command format, task number, and task range without terminating the application.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
delete
delete nope
delete 1
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! Please use the format: delete <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-006: Save tasks after list changes

**Aim:** Verify that adding, marking, and deleting tasks save the current task list to `data/Bob.txt`.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
todo write report
deadline submit report /by Friday
event team sync /from Monday /to Tuesday
mark 1
unmark 1
delete 2
list
bye
```

**Expected output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] write report
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] team sync (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] write report
____________________________________________________________
OK, I've marked this task as not done yet:
  [ ] write report
____________________________________________________________
Noted. I've removed this task:
    [D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] write report
2.[E][ ] team sync (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Expected saved file (`data/Bob.txt`):**
```text
[T][ ] write report
[E][ ] team sync (from: Monday to: Tuesday)
```

## Test session

### Test session: 2026-08-25 20:59:29 +08:00

**Build command:** `javac -d out src/main/java/*.java` — passed with no compiler output.

#### UI-001

**Command:** `java -cp out Bob`

**Console input:**
```text
todo
blah
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A todo must have a description.
____________________________________________________________
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, or bye.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-002

**Command:** `java -cp out Bob`

**Console input:**
```text
deadline project
event meeting
mark
unmark nope
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A deadline needs a description and a '/by' detail.
____________________________________________________________
Oops! An event needs a description followed by '/from' and '/to' details.
____________________________________________________________
Oops! Please use the format: mark <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-003

**Command:** `java -cp out Bob`

**Console input:**
```text
todo buy milk
deadline submit report /by Friday
event meeting /from Monday /to Tuesday
mark 4
mark 9
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] buy milk
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][ ] meeting (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

**Overall result:** PASS — all documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-25 22:04:28 +08:00

**Plan revision:** Level 6 delete implementation and UI test cases; no commit was created.

**Build command:** `javac -d out src/main/java/*.java` — passed with no compiler output.

#### UI-001

**Command:** `java -cp out Bob`

**Console input:**
```text
todo
blah
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A todo must have a description.
____________________________________________________________
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-002

**Command:** `java -cp out Bob`

**Console input:**
```text
deadline project
event meeting
mark
unmark nope
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A deadline needs a description and a '/by' detail.
____________________________________________________________
Oops! An event needs a description followed by '/from' and '/to' details.
____________________________________________________________
Oops! Please use the format: mark <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-003

**Command:** `java -cp out Bob`

**Console input:**
```text
todo buy milk
deadline submit report /by Friday
event meeting /from Monday /to Tuesday
mark 4
mark 9
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] buy milk
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][ ] meeting (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-004

**Command:** `java -cp out Bob`

**Console input:**
```text
todo read book
todo return book
delete 1
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[T][ ] return book
Now you have 2 tasks in the list.
____________________________________________________________
Noted. I've removed this task:
    [T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] return book
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

#### UI-005

**Command:** `java -cp out Bob`

**Console input:**
```text
delete
delete nope
delete 1
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! Please use the format: delete <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exact match.

**Overall result:** PASS — all five documented cases passed; no failures occurred.

### Test session: 2026-08-30

**Plan revision:** Gradle and Spotless integration; UI behavior unchanged.

**Build command:** `gradlew.bat classes` — passed with no compiler errors.

#### UI-001

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo
blah
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A todo must have a description.
____________________________________________________________
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-002

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline project
event meeting
mark
unmark nope
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A deadline needs a description and a '/by' detail.
____________________________________________________________
Oops! An event needs a description followed by '/from' and '/to' details.
____________________________________________________________
Oops! Please use the format: mark <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-003

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo buy milk
deadline submit report /by Friday
event meeting /from Monday /to Tuesday
mark 4
mark 9
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] buy milk
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][ ] meeting (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-004

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo read book
todo return book
delete 1
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[T][ ] return book
Now you have 2 tasks in the list.
____________________________________________________________
Noted. I've removed this task:
    [T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] return book
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-005

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
delete
delete nope
delete 1
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! Please use the format: delete <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all five documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-30 21:10:54 +08:00

**Plan revision:** Increment 1 persistence implementation and UI-006 save verification.

**Build command:** `gradlew.bat classes` — passed with no compiler errors.

#### UI-001

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo
blah
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A todo must have a description.
____________________________________________________________
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-002

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline project
event meeting
mark
unmark nope
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! A deadline needs a description and a '/by' detail.
____________________________________________________________
Oops! An event needs a description followed by '/from' and '/to' details.
____________________________________________________________
Oops! Please use the format: mark <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-003

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo buy milk
deadline submit report /by Friday
event meeting /from Monday /to Tuesday
mark 4
mark 9
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] buy milk
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][ ] meeting (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-004

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo read book
todo return book
delete 1
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[T][ ] return book
Now you have 2 tasks in the list.
____________________________________________________________
Noted. I've removed this task:
    [T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] return book
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-005

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
delete
delete nope
delete 1
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Oops! Please use the format: delete <task number>.
____________________________________________________________
Oops! Please enter a valid task number.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-006

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo write report
deadline submit report /by Friday
event team sync /from Monday /to Tuesday
mark 1
unmark 1
delete 2
list
bye
```

**Actual output:**
```text
____________________________________________________________
 ____        _
| __ )  ___ | |__
|  _ \ / _ \| '_ \
| |_) | (_) | |_) |
|____/ \___/|_.__/

Hello! I'm Bob.
What can I do for you?
____________________________________________________________
Got it. I've added this task:

[T][ ] write report
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] team sync (from: Monday to: Tuesday)
Now you have 3 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] write report
____________________________________________________________
OK, I've marked this task as not done yet:
  [ ] write report
____________________________________________________________
Noted. I've removed this task:
    [D][ ] submit report (by: Friday)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] write report
2.[E][ ] team sync (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Saved file verification:** `data/Bob.txt` contained exactly:
```text
[T][ ] write report
[E][ ] team sync (from: Monday to: Tuesday)
```

**Result:** PASS — exit code 0, empty stderr, exact console match after the documented trailing-space normalization, and exact saved-file contents.

**Overall result:** PASS — all six documented cases passed; testing stopped after the final case as required.
