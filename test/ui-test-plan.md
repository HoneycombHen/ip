# UI Test Plan

This file is the source of truth for the `test-ui` project skill. Keep test cases in execution order and update the session record after each run.

## Run instructions

- Working directory: project root for the build; use a fresh isolated directory under `_temp` for each UI test session so existing runtime data is not overwritten. Copy `build/classes/java/main` into that directory's `build/classes/java/main` before running the documented command.
- Prerequisites: Java 25 (`java --version` and `javac --version`)
- Build command: `gradlew.bat classes`
- Run command: `java -cp build/classes/java/main Bob`
- Test fixture setup: before each test case, create an empty UTF-8 `data/Bob.txt` file in the isolated working directory unless the test case specifies its own contents. From PowerShell, run `New-Item -ItemType Directory -Force data | Out-Null` followed by `[System.IO.File]::WriteAllText((Join-Path (Get-Location) 'data/Bob.txt'), '')`. UI-008, UI-009, and UI-010 specify their own error fixtures.
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
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, upcoming, on, overdue, or bye.
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
deadline submit report /by 2019-10-15
event meeting /from 2019-10-16 /to 2019-10-17
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

[D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Tue, Oct 15 2019)
3.[E][ ] meeting (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
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
deadline submit report /by 2019-10-15
event team sync /from 2019-10-16 /to 2019-10-17
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

[D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] write report
____________________________________________________________
OK, I've marked this task as not done yet:
  [ ] write report
____________________________________________________________
Noted. I've removed this task:
    [D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] write report
2.[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Expected saved file (`data/Bob.txt`):**
```text
[T][ ] write report
[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
```

### UI-007: Load saved tasks at startup

**Aim:** Verify that Bob loads todo, deadline, and event tasks, including their done status, from `data/Bob.txt` when it starts.

**Fixture before running:** Replace `data/Bob.txt` with:
```text
[T][X] buy milk
[D][ ] submit report (by: Tue, Oct 15 2019)
[E][X] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
```

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
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
Here are the tasks in your list:

1.[T][X] buy milk
2.[D][ ] submit report (by: Tue, Oct 15 2019)
3.[E][X] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-008: Start without a data file

**Aim:** Verify that Bob starts with an empty task list when `data/Bob.txt` does not exist.

**Fixture before running:** Remove `data/Bob.txt`.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
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
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-009: Handle malformed saved data

**Aim:** Verify that Bob reports malformed saved data and continues with an empty task list.

**Fixture before running:** Replace `data/Bob.txt` with:
```text
not a valid task line
```

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-010: Handle a save failure

**Aim:** Verify that Bob reports a task-save failure and continues running when `data/Bob.txt` is a directory.

**Fixture before running:** Replace `data/Bob.txt` with a directory named `Bob.txt`.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
todo test save failure
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Oops! I couldn't save the task list.
____________________________________________________________
Got it. I've added this task:

[T][ ] test save failure
Now you have 1 tasks in the list.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Fixture cleanup:** Remove the `data/Bob.txt` directory and restore an empty `data/Bob.txt` file.

### UI-011: Parse and format date-time details

**Aim:** Verify that deadline and event date-time details are stored and displayed with a readable day and month format, while accepting both ISO separators.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline submit report /by 2019-10-15 18:00
event project meeting /from 2019-10-16T09:30 /to 2019-10-16T10:45
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

[D][ ] submit report (by: Tue, Oct 15 2019 18:00)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] project meeting (from: Wed, Oct 16 2019 09:30 to: Wed, Oct 16 2019 10:45)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[D][ ] submit report (by: Tue, Oct 15 2019 18:00)
2.[E][ ] project meeting (from: Wed, Oct 16 2019 09:30 to: Wed, Oct 16 2019 10:45)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-012: Reject invalid date and time input

**Aim:** Verify that invalid deadline and event date/time details are reported without terminating Bob or adding invalid tasks.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline submit report /by tomorrow
event project meeting /from 2026-02-30 /to 2026-03-01
todo continue working
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
Oops! A deadline's '/by' detail must be a valid date or time.
____________________________________________________________
Oops! An event's '/from' and '/to' details must be valid dates or times.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-013: Handle invalid saved date formatting

**Aim:** Verify that a structurally valid saved task with an invalid date is rejected as malformed saved data.

**Fixture before running:** Replace `data/Bob.txt` with:
```text
[D][ ] submit report (by: not-a-date)
```

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-014: List upcoming dated tasks

**Aim:** Verify that `upcoming [days]` includes deadlines and event starts in the requested range and sorts them chronologically. The distant fixture dates make this case independent of the current date.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline submit annual report /by 9999-12-31
event future meeting /from 9999-12-30 /to 9999-12-30
upcoming 3000000
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

[D][ ] submit annual report (by: Fri, Dec 31 9999)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the upcoming tasks in the next 3000000 days:

1.[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
2.[D][ ] submit annual report (by: Fri, Dec 31 9999)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-015: Validate upcoming command arguments

**Aim:** Verify that the default seven-day range works and malformed `upcoming` arguments are reported without terminating Bob.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
upcoming
upcoming nope
upcoming -1
todo continue working
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
Here are the upcoming tasks in the next 7 days:

____________________________________________________________
Oops! Please enter a valid number of days.
____________________________________________________________
Oops! Please enter a non-negative number of days.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-016: List tasks on a date

**Aim:** Verify that `on <date>` includes deadlines on the date and events spanning the date, while excluding unrelated tasks.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline submit annual report /by 9999-12-31
event multi-day meeting /from 9999-12-29 /to 9999-12-30
event other meeting /from 9999-12-31 /to 9999-12-31
on 9999-12-30
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

[D][ ] submit annual report (by: Fri, Dec 31 9999)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] multi-day meeting (from: Wed, Dec 29 9999 to: Thu, Dec 30 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] other meeting (from: Fri, Dec 31 9999 to: Fri, Dec 31 9999)
Now you have 3 tasks in the list.
____________________________________________________________
Here are the tasks on Thu, Dec 30 9999:

1.[E][ ] multi-day meeting (from: Wed, Dec 29 9999 to: Thu, Dec 30 9999)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-017: Validate on date arguments

**Aim:** Verify that missing, invalid, and date-time query arguments are reported without terminating Bob.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
on
on not-a-date
on 9999-12-30T09:00
todo continue working
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
Oops! Please use the format: on <date>.
____________________________________________________________
Oops! Please enter a valid date in the format: yyyy-MM-dd.
____________________________________________________________
Oops! Please enter a valid date in the format: yyyy-MM-dd.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-018: List overdue deadlines

**Aim:** Verify that `overdue` lists incomplete past deadlines, excludes completed deadlines and events, and sorts results by due date.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
deadline old report /by 2019-10-15
deadline future report /by 9999-12-31
deadline completed report /by 2019-10-16
event old event /from 2019-10-15 /to 2019-10-16
mark 3
overdue
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

[D][ ] old report (by: Tue, Oct 15 2019)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] future report (by: Fri, Dec 31 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] completed report (by: Wed, Oct 16 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] old event (from: Tue, Oct 15 2019 to: Wed, Oct 16 2019)
Now you have 4 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] completed report
____________________________________________________________
Here are your overdue tasks:

1.[D][ ] old report (by: Tue, Oct 15 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

### UI-019: Validate overdue command arguments

**Aim:** Verify that extra arguments to `overdue` are rejected without terminating Bob and that an empty overdue list is displayed correctly.

**Command:**
```text
java -cp build/classes/java/main Bob
```

**Inputs:**
```text
overdue now
overdue
todo continue working
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
Oops! Please use the format: overdue.
____________________________________________________________
Here are your overdue tasks:

____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
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

### Test session: 2026-08-30 21:27:56 +08:00

**Plan revision:** Increment 2 file-loading implementation and UI-007 startup-load verification.

**Fixture setup:** For UI-001 through UI-006, `data/Bob.txt` was reset to an empty UTF-8 file before each case. For UI-007, it contained the fixture specified in that test case.

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

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-007

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Here are the tasks in your list:

1.[T][X] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][X] team sync (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization; all three task types and completion states were loaded correctly.

**Overall result:** PASS — all seven documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 12:12:55 +08:00

**Plan revision:** Increment 3 storage error handling and UI-008 through UI-010 error-case coverage.

**Fixture setup:** The documented build command was run from the project root. Each UI case then ran from a fresh isolated directory under `_temp`, containing a copy of `build/classes/java/main`, so the repository's existing `data/Bob.txt` was not overwritten.

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

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-007

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Here are the tasks in your list:

1.[T][X] buy milk
2.[D][ ] submit report (by: Friday)
3.[E][X] team sync (from: Monday to: Tuesday)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-008

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-009

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-010

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo test save failure
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Oops! I couldn't save the task list.
____________________________________________________________
Got it. I've added this task:

[T][ ] test save failure
Now you have 1 tasks in the list.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all ten documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 14:00:00 +08:00

**Plan revision:** Level 8 increment 1 date and time parsing, storage, and display formatting.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260831-level8-increment1-final`, containing a copy of `build/classes/java/main`. Date fixtures were updated to use ISO dates and the new formatted representation.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

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
Oops! I do not recognise that command. Try todo, deadline, event, list, mark, unmark, delete, upcoming, or bye.
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
deadline submit report /by 2019-10-15
event meeting /from 2019-10-16 /to 2019-10-17
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

[D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] meeting (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Oops! That task number does not exist.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] buy milk
2.[D][ ] submit report (by: Tue, Oct 15 2019)
3.[E][ ] meeting (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
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
deadline submit report /by 2019-10-15
event team sync /from 2019-10-16 /to 2019-10-17
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

[D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] write report
____________________________________________________________
OK, I've marked this task as not done yet:
  [ ] write report
____________________________________________________________
Noted. I've removed this task:
    [D][ ] submit report (by: Tue, Oct 15 2019)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] write report
2.[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Expected saved file:**
```text
[T][ ] write report
[E][ ] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
```

**Result:** PASS — exit code 0, empty stderr, exact stdout and saved-file match after the documented trailing-space normalization.

#### UI-007

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Here are the tasks in your list:

1.[T][X] buy milk
2.[D][ ] submit report (by: Tue, Oct 15 2019)
3.[E][X] team sync (from: Wed, Oct 16 2019 to: Thu, Oct 17 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-008

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-009

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-010

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
todo test save failure
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Oops! I couldn't save the task list.
____________________________________________________________
Got it. I've added this task:

[T][ ] test save failure
Now you have 1 tasks in the list.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-011

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline submit report /by 2019-10-15 18:00
event project meeting /from 2019-10-16T09:30 /to 2019-10-16T10:45
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

[D][ ] submit report (by: Tue, Oct 15 2019 18:00)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] project meeting (from: Wed, Oct 16 2019 09:30 to: Wed, Oct 16 2019 10:45)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[D][ ] submit report (by: Tue, Oct 15 2019 18:00)
2.[E][ ] project meeting (from: Wed, Oct 16 2019 09:30 to: Wed, Oct 16 2019 10:45)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all eleven documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 14:30:00 +08:00

**Plan revision:** Increment 2 invalid date/time input and malformed saved date handling.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260831-level8-increment2-final`, containing a copy of `build/classes/java/main`. UI-013 used the documented structurally valid task with an invalid date detail.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

#### UI-001 to UI-011

**Command:** `java -cp build/classes/java/main Bob`

**Console input:** The inputs documented for UI-001 through UI-011, in order.

**Actual output:** Each case matched its expected output and the complete actual outputs are recorded in the preceding Level 8 increment 1 test session.

**Result:** PASS — UI-001 through UI-011 all passed with exit code 0, empty stderr, and exact output after the documented trailing-space normalization.

#### UI-012

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline submit report /by tomorrow
event project meeting /from 2026-02-30 /to 2026-03-01
todo continue working
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
Oops! A deadline's '/by' detail must be a valid date or time.
____________________________________________________________
Oops! An event's '/from' and '/to' details must be valid dates or times.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-013

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
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
Oops! I couldn't load the saved tasks. Starting with an empty list.
____________________________________________________________
Here are the tasks in your list:

____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all thirteen documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 15:30:00 +08:00

**Plan revision:** Increment 3 `upcoming [days]` command with default range, date filtering, chronological ordering, and argument validation.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260831-upcoming-final2`, containing a copy of `build/classes/java/main`. UI-014 used distant fixture dates so its inclusion and ordering check was independent of the current date.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

#### UI-001 to UI-013

**Command:** `java -cp build/classes/java/main Bob`

**Console input:** The inputs documented for UI-001 through UI-013, in order.

**Actual output:** Each case matched its expected output and the complete actual outputs are recorded in the preceding test sessions.

**Result:** PASS — UI-001 through UI-013 all passed with exit code 0, empty stderr, and exact output after the documented trailing-space normalization.

#### UI-014

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline submit annual report /by 9999-12-31
event future meeting /from 9999-12-30 /to 9999-12-30
upcoming 3000000
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

[D][ ] submit annual report (by: Fri, Dec 31 9999)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the upcoming tasks in the next 3000000 days:

1.[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
2.[D][ ] submit annual report (by: Fri, Dec 31 9999)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-015

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
upcoming
upcoming nope
upcoming -1
todo continue working
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
Here are the upcoming tasks in the next 7 days:

____________________________________________________________
Oops! Please enter a valid number of days.
____________________________________________________________
Oops! Please enter a non-negative number of days.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all fifteen documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 17:30:00 +08:00

**Plan revision:** Increment 4 `on <date>` command with date filtering, event-range matching, and argument validation.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260831-on-final`, containing a copy of `build/classes/java/main`. UI-016 used distant fixture dates so its date-range check was independent of the current date.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

#### UI-001 to UI-015

**Command:** `java -cp build/classes/java/main Bob`

**Console input:** The inputs documented for UI-001 through UI-015, in order.

**Actual output:** Each case matched its expected output and the complete actual outputs are recorded in the preceding test sessions.

**Result:** PASS — UI-001 through UI-015 all passed with exit code 0, empty stderr, and exact output after the documented trailing-space normalization.

#### UI-016

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline submit annual report /by 9999-12-31
event multi-day meeting /from 9999-12-29 /to 9999-12-30
event other meeting /from 9999-12-31 /to 9999-12-31
on 9999-12-30
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

[D][ ] submit annual report (by: Fri, Dec 31 9999)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] multi-day meeting (from: Wed, Dec 29 9999 to: Thu, Dec 30 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] other meeting (from: Fri, Dec 31 9999 to: Fri, Dec 31 9999)
Now you have 3 tasks in the list.
____________________________________________________________
Here are the tasks on Thu, Dec 30 9999:

1.[E][ ] multi-day meeting (from: Wed, Dec 29 9999 to: Thu, Dec 30 9999)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-017

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
on
on not-a-date
on 9999-12-30T09:00
todo continue working
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
Oops! Please use the format: on <date>.
____________________________________________________________
Oops! Please enter a valid date in the format: yyyy-MM-dd.
____________________________________________________________
Oops! Please enter a valid date in the format: yyyy-MM-dd.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all seventeen documented cases passed; testing stopped after the final case as required.

### Test session: 2026-09-01 10:00:00 +08:00

**Plan revision:** Increment 5 `overdue` command with deadline filtering, completed-task exclusion, event exclusion, and argument validation.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260901-overdue-final`, containing a copy of `build/classes/java/main`.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

#### UI-001 to UI-017

**Command:** `java -cp build/classes/java/main Bob`

**Console input:** The inputs documented for UI-001 through UI-017, in order.

**Actual output:** Each case matched its expected output and the complete actual outputs are recorded in the preceding test sessions.

**Result:** PASS — UI-001 through UI-017 all passed with exit code 0, empty stderr, and exact output after the documented trailing-space normalization.

#### UI-018

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline old report /by 2019-10-15
deadline future report /by 9999-12-31
deadline completed report /by 2019-10-16
event old event /from 2019-10-15 /to 2019-10-16
mark 3
overdue
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

[D][ ] old report (by: Tue, Oct 15 2019)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] future report (by: Fri, Dec 31 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[D][ ] completed report (by: Wed, Oct 16 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] old event (from: Tue, Oct 15 2019 to: Wed, Oct 16 2019)
Now you have 4 tasks in the list.
____________________________________________________________
Nice! I've marked this task as done:
  [X] completed report
____________________________________________________________
Here are your overdue tasks:

1.[D][ ] old report (by: Tue, Oct 15 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-019

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
overdue now
overdue
todo continue working
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
Oops! Please use the format: overdue.
____________________________________________________________
Here are your overdue tasks:

____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all nineteen documented cases passed; testing stopped after the final case as required.

### Test session: 2026-08-31 16:30:00 +08:00

**Plan revision:** Increment 3 `upcoming [days]` command with default range, date filtering, chronological ordering, and argument validation.

**Fixture setup:** The documented build command was run from the project root. Each UI case ran from a fresh isolated directory under `_temp/ui-session-20260831-upcoming-final2`, containing a copy of `build/classes/java/main`. UI-014 used distant fixture dates so its inclusion and ordering check was independent of the current date.

**Build command:** `gradlew.bat clean check` — passed with no compiler or test failures.

#### UI-001 to UI-013

**Command:** `java -cp build/classes/java/main Bob`

**Console input:** The inputs documented for UI-001 through UI-013, in order.

**Actual output:** Each case matched its expected output and the complete actual outputs are recorded in the preceding test sessions.

**Result:** PASS — UI-001 through UI-013 all passed with exit code 0, empty stderr, and exact output after the documented trailing-space normalization.

#### UI-014

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
deadline submit annual report /by 9999-12-31
event future meeting /from 9999-12-30 /to 9999-12-30
upcoming 3000000
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

[D][ ] submit annual report (by: Fri, Dec 31 9999)
Now you have 1 tasks in the list.
____________________________________________________________
Got it. I've added this task:

[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
Now you have 2 tasks in the list.
____________________________________________________________
Here are the upcoming tasks in the next 3000000 days:

1.[E][ ] future meeting (from: Thu, Dec 30 9999 to: Thu, Dec 30 9999)
2.[D][ ] submit annual report (by: Fri, Dec 31 9999)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

#### UI-015

**Command:** `java -cp build/classes/java/main Bob`

**Console input:**
```text
upcoming
upcoming nope
upcoming -1
todo continue working
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
Here are the upcoming tasks in the next 7 days:

____________________________________________________________
Oops! Please enter a valid number of days.
____________________________________________________________
Oops! Please enter a non-negative number of days.
____________________________________________________________
Got it. I've added this task:

[T][ ] continue working
Now you have 1 tasks in the list.
____________________________________________________________
Here are the tasks in your list:

1.[T][ ] continue working
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

**Result:** PASS — exit code 0, empty stderr, exact match after the documented trailing-space normalization.

**Overall result:** PASS — all fifteen documented cases passed; testing stopped after the final case as required.
