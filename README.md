# Trace Labs

Trace Labs is a Trace Compass plugin that can be used to perform analysis on system calls and performance counters.

> This is a school project completed for COSC 3P99 at Brock University. See the [report](./report/report.md) for more details about that.

## Getting Started

### Prerequisites

1. Download the Eclipse IDE from https://www.eclipse.org/ide
2. Install Trace Compass 9.0.0 from the Eclipse Marketplace (including LTTng Kernel Analysis and LTTng Userspace Analysis)
3. Install Plug-In Development Environment Latest from the Eclipse Marketplace

Note: Eclipse Marketplace can be accessed from `Help > Eclipse Marketplace` in the application menu. You may need to restart Eclipse several times during plugin installation.

### Development

1. Clone or download this project to your computer.
2. Select `File > Open Projects from File System` in the application menu.
3. Click the `Directory...` in the `Import Projects from File System or Archive` modal window.
4. From the file explorer, choose the `tracelabs` directory copied to your computer in step one.
5. Click the `Finish` button in the `Import Projects from File System or Archive` modal window.

### Running the Plug-In

1. Click the `Run` button in the Eclipse toolbar and if prompted, select `Eclipse Application` for `Run As`.
2. From the new Eclipse window that opens, select `Window > Perspective > Open Perspective > Other` from the application menu.
3. Choose `Tracing` from the modal window then click the `Open` button.
4. From the `Project Explorer` click `Create a new Tracing project` called `Trace Labs Dev`.
5. Right-click on `Trace Labs Dev` and select `import` from the context menu.
6. From the `Import` modal window, select `Tracing > Trace Import` and click the `Next` button.
7. From the file explorer, choose a Trace directory.
8. From the `Import` modal, click the checkbox next to the name of the Trace directory then click the `Finish` button.
9. From the `Project Explorer`, select a Kernel trace from the trace that you just imported into `Trace Labs Dev`.
10. If prompted in the `Confirm Perspective Switch` to open the `LTTng Kernel perspective`, click the `Yes` button.
11. Select `Window > Show View > Other` from the application menu.
12. From the `Show View` modal window, open the `TraceLabs` folder and select `Performance Counters - Growth Viz`, `Performance Counters - Total Table`, `Performance Counters - Total Viz`, `SysCalls - Aggregated Table`, and `SysCalls - Table`.

After following all those steps, you should be able to see all of the views under development in the Trace Labs project.

### Collecting Compatible Traces

To collect traces compatible with this plug-in, you will need to run a program such as LTTng or Perf on a compatible Linux operating system. You will need to collect syscall events with fields for process id, thread id, and one or more performance counters. The resulting trace must be in Common Trace Format (CTF).

Here's an example script for using LTTng to trace syscall events on the Linux kernel while executing a `wget` command with all of the context required by this plug-in:

1. Run `lttng create` to create a new tracing session.
2. Run `lttng enable-event -k --syscall` to create a tracing rule that will capture all Linux kernel syscall events.
3. Run `lttng add-context --kernel --type=pid --type=tid` to add context fields to each event for the process id (pid) and thread id (tid).
4. Run `lttng add-context --kernel --type=perf:cpu:cpu-cycles --type=perf:cpu:cycles --type=perf:cpu:branch-instructions --type=perf:cpu:branch-misses` to add context fields to each event for performance counters.
5. Run `lttng start` to start tracing.
6. Execute a program and trace syscalls on the Linux kernel, i.e. `wget https://lttng.org`.
7. Run `lttng destroy` to stop tracing, destroy the tracing session, and write the results to disk in Common Trace Format (CTF).

Note: you can see all of the context that you can add to an event by running `lttng add-context --list`, this includes all performance counters. You cannot add context for more than four performance counters at a time.

## Packages

- `tracelabs.models`: classes for modeling trace events and collections of trace events.
- `tracelabs.ui`: Small user interface components used within views.
- `tracelabs.views`: Complete user interfaces. Each class corresponds to a single view within the plug-in.
