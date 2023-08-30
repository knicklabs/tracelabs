# Trace Labs

Trace Labs is a Trace Compass plugin that can be used to perform analysis on system calls and performance counters.

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

### Run in Development

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
12. From the `Show View` modal window, select `TraceLabs Aggregate SysCall Stats`, `TraceLabs Performance Counters`, `TraceLabs Performance Counters Chart`, and `TraceLabs SysCall Stats`.

After following all those steps, you should be able to see all of the views under development in the Trace Labs project.
