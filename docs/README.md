# Bob User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Undo

Use `undo` to reverse the most recent successful state-changing command in the
current Bob session.

The command can reverse task creation, `mark`, `unmark`, or `delete`. Queries
such as `list` do not replace the undo action. Only one undo is available at a
time, and undo history is cleared when Bob restarts.

Example:

```text
todo read a book
undo
```

Bob responds:

```text
Undid the last command:
    [T][ ] read a book
```

`undo` does not accept arguments. For example, `undo now` is invalid. If there
is no successful state-changing command to reverse, Bob reports that there is
no command to undo. The resulting task list is saved using the existing
`data/Bob.txt` format.

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details
