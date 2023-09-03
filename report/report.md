
# COSC 3P99 Final Report

__Project Title__: Trace Labs<br>
__Project Type__: R&D<br>
__Credit Sought__: COSC 3P99 Computing Project<br>
__Hardware__: Linux in ARM VM, Linux in Intel VM, Linux on Intel CPU<br>
__Software__: Python, Java, Eclipse Plug-in SDK<br>

## Description

The goal of this project was to develop a Trace Compass plug-in with views to visualize syscalls, performance counters, and energy consumption.

## Personal Reflection and Learnings

### Original Interest: Energy Consumption

My original interest in pursuing the COSC 3P99 project course was to acquire an understanding of energy consumption and optimization in software applications. My interest in this subject was initially piqued when my colleague at work engaged in a project aimed at optimizing a software component we had developed. He shared the potential environmental repercussions of these optimizations within the company, leaving me impressed with the results. He also authored a public blog post on the work and its impact: [Shrinking Bundles, Expanding Forests](https://www.maxiferreira.com/blog/shrinking-bundles/).

Throughout my career, I frequently encountered the adage that "it is more cost-effective to throw hardware at a problem than people." This adage often served as justification for opting for productive yet inefficient programming languages and technologies, such as Python or Ruby on Rails, over alternatives like C++ or Spring Boot, which were less productive but more efficient. Observing the environmental consequences of my colleague's efforts prompted me to question how my tool choices contributed to this significant global issue. It also instilled in me a sense of optimism, as I realized my potential to contribute, even in a small capacity, to reducing the greenhouse gas emissions of the software industry through everyday choices I make in my work.

Undoubtedly, there exist other compelling reasons to prioritize energy efficiency in software applications, ranging from designing lighter devices by reducing battery sizes to extending the intervals between charges for mobile devices. Nevertheless, my primary interest is in the potential for fostering positive environmental impacts.

In preparation for this project, I conducted preliminary research into energy, electricity, and methodologies for measuring electricity usage, both in a general sense and with specific relevance to software applications.

Below are a couple of useful resources that I found.

- [How do we measure electricity?](https://www.simplethread.com/how-do-we-measure-electricity/): This article distinguishes between energy and electricity, reviews the measurement of electricity, and examines the units utilized in this measurement, including joule, watt, watt-hour, amp-hour, volt, amp, volt-ampere, and volt-ampere reactive.
- [How do we measure energy consumption of Software?](https://www.thoughtworks.com/insights/blog/ethical-tech/calculating-software-carbon-intensity): As outlined in the article, historical discussions surrounding energy consumption and carbon emissions predominantly focused on hardware, despite software contributing to approximately three percent of global carbon emissions. This places the software industry in a comparable position to the airline industry in terms of emissions. A key takeaway from this article is the idea of Software Carbon Intensity (SCI) and the SCI Equation: `SCI = (E * I) + M per R`. In this equation, `E` is represents energy consumption, `I` corresponds to location-based marginal carbon intensity (indicating the carbon intensity energy production within the region(s) of consumption), `M` is embodied carbon (reflecting the carbon emissions generated during creation and disposal of the hardware running the software), and where `R` signifies the functional unit (for instance, per user, per device, per API request, etc.).

The following papers helped me develop an understanding of the state of research into energy efficiency and optimization in software applications.

- C. Pang, A. Hindle, B. Adams and A.E. Hassan, "What Do Programmers Know about Software Energy Consumption?", in _IEEE Software_, vol. 33, no. 3, pp. 83-89, May-June 2016.
- Gustavo Pinto, Fernando Castor, Yu David Liu, "Mining questions about software energy consumption", in _MSR 2014: Proceedings of the 11th Working Conference on Mining Software Repositories_, pp. 22-31, May 2014.
- M. Nagappan and E. Shihab, "Future Trends in Software Engineering Research for Mobile Apps", _2016 IEEE 23rd International Conference on Software Analysis, Evolution, and Reengineering (SANER)_, Osaka, Japan, 2016, pp. 21-32.

Some insights gleaned from these papers are as follows:

- Significant domains benefiting from energy efficiency advancements include mobile computing and Internet of Things (IoT) devices, primarily due to advances in battery life.
- There exists an intuitive association among software engineers between performance (i.e. speed) and energy efficiency. That is, many performance optimizations are believed to be energy optimizations.
- Software engineers possess familiarity with certain factors contributing to sub-optimal energy efficiency, such as network requests.
- In a general sense, beyond intuition and recognition of common issues, software engineers could benefit from more awareness of energy issues, including underlying causes and remedies.

### Refined Topic: Tracing and Trace Compass Plug-in

We subsequently redefined the scope of the project to focus on:

1. Using The Linux Trace Toolkit: Next Generation (LTTng) to capture Linux kernel traces in Common Trace Format (CTF).
2. Develop a Trace Compass plug-in for the Eclipse IDE capable of ingesting CTF traces and analyzing the syscall events,  performance counters, and energy information contained within.

#### Learning LTTng

The most significant hurdle in this project was installing and learning to use LTTng. I worked on a MacBook Air equipped with an Apple M2 chip, which utilizes an ARM architecture. For virtualization, I employed VM Fusion Player. The options for Linux operating systems that were compatible with this specific hardware and virtualization environment were limited. Ultimately, I installed the 64-bit ARMv8/AArch64 server image of Ubuntu 22.04 LTS and upgraded it to Ubuntu Desktop after the initial installation.

I encountered numerous stability issues while using VM Fusion Player on my MacBook Air with the M2 chip, experiencing frequent crashes. Consequently, I transitioned to using Parallels Virtual Machine, which offered a more stable environment. Parallels provided an option to download and install the ARM desktop version of Ubuntu 22.04 LTS, which I utilized. Following the [instructions for installing LTTng on Ubuntu](https://github.com/naser/tracedependency/tree/master/labs/002-install-lttng-on-ubuntu), I successfully installed most of the required software for LTTng. However, I encountered difficulties when attempting to install `lttng-modules-dkms` using the apt package manager. To resolve this issue, I followed the guidelines for [building LTTng from source](https://lttng.org/docs/v2.13/#doc-building-from-source) available on the LTTng website.

On this platform, I successfully followed the [tracedependency labs](https://github.com/naser/tracedependency), but I later encountered issues when attempting to record performance counters. Executing the command to add performance counters to the context caused the LTTng `add-context` command to hang indefinitely. Aborting the process with `ctrl+c` was ineffective, requiring a complete restart of the virtual machine. Unfortunately, I was never able to resolve this issue on this platform.

I experimented with two alternative platform configurations: Ubuntu 22.04 LTS on VMware Fusion Player running on an Intel MacBook Pro, and Ubuntu 22.04 LTS on VirtualBox, also on an Intel MacBook Pro. In both cases, I encountered similar issues to those experienced on the ARM architecture. Specifically, adding performance counters to the context for syscall trace events was unsuccessful. I suspect this issue arose because the free versions of these virtual machine platforms did not facilitate seamless interoperability with the underlying hardware, preventing CPU probing on the host machine from the virtual environment.

Ultimately, I resorted to using an older MacBook Air with an Intel chip, installing Ubuntu 22.04 LTS directly onto the hardware. After building LTTng from source, I successfully utilized the `add-context` command to add performance counter properties to kernel syscall events.

During my trials with collecting performance counters via LTTng, I discovered two key limitations of the `add-context` command. 

1. The command is irreversible.
2. If too many performance counters are added via `add-context`, or if the added counters are incompatible with the CPU architecture, the command will freeze.

I frequently had to log out of my session or restart the computer in order to continue my experiments with `add-context`.

I have provided an example of how I collected traces of kernel syscalls with performance counters in the [Trace Labs README](https://github.com/knicklabs/tracelabs#collecting-traces).

While learning how to install LTTng and perform traces, I found several valuable learning resources:

1. [How to tracing with LTTng](https://www.ibm.com/support/pages/howto-tracing-lttng)
2. [LTTng Man Pages](https://lttng.org/man/)
3. [LTTng Documentation](https://lttng.org/docs/v2.13/)
4. [LTTng: The Linux Trace Toolkit Next Generation Comprehensive User's Guide version 2.3](https://cradpdf.drdc-rddc.gc.ca/PDFS/unc246/p804561_A1b.pdf)
5. [Linux Tracing Systems & How They Fit Together](https://jvns.ca/blog/2017/07/05/linux-tracing-systems/#lttng-systemtap)

#### Viewing and Analyzing LTTng Traces

I learned that LTTng generates traces in Common Trace Format (CTF), which is a binary format. To view these traces, I found that the `babeltrace` command could be used to render them in my terminal. Alternatively, Trace Compass offered various lenses through which to interpret the traces. I could also export the traces from Trace Compass to CSV format, allowing for further analysis using spreadsheet software or scripting. However, it's worth noting that these CSV files could be large and difficult to work with.

To assist with analyzing LTTng traces, I experimented with two projects:

1. [LTTng Analyses](https://github.com/lttng/lttng-analyses)
2. [babeltrace2 Python bindings](https://babeltrace.org/docs/v2.0/python/bt2/index.html)

I created Dockerized environments for both projects to streamline the analysis process. While I was unsuccessful in utilizing LTTng Analyses to interpret the traces I gathered, I successfully analyzed the traces using Babeltrace2 Python bindings in a Dockerized Jupyter Lab environment. These environments are discussed further in the "Outcomes" section of this document.

#### Eclipse, Trace Compass and Authoring New Views for Traces

The primary objective of this project was to develop a Trace Compass plug-in, which necessitated familiarization with both Eclipse and Trace Compass. The tracedependency labs served as a valuable resource in this regard, particularly the lab focused on [scripted analysis for custom interpretation](https://github.com/naser/tracedependency/tree/master/labs/204-scripted-analysis-for-custom-instrumentation). While not directly applicable to my project, this lab provided an insightful introduction to trace analysis. It was especially helpful since it used JavaScript, a language in which I have greater proficiency compared to Java.

For my project, however, Java was the language of choice. I found the [View Tutorial](https://archive.eclipse.org/tracecompass/doc/org.eclipse.tracecompass.doc.dev/View-Tutorial.html#View_Tutorial) in the Trace Compass Developer Guide useful. It guided me through downloading the dependencies for plug-in development, creating a new plug-in, and extending the TMF view class. It also provided insight into handling signals to process events sequentially when a trace is selected, as well as executing subsequent actions once all events have been processed.

I further enhanced my understanding of plug-in development and trace analysis by reviewing source code in the [Trace Compass Incubator](https://github.com/tracecompass/tracecompass-incubator) and familiarizing myself with the [Trace Compass/Incubator Guidelines](https://wiki.eclipse.org/Trace_Compass/Incubator_Guidelines). This also gave me an understanding of how I could potentially contribute my plug-in to the larger project.

To develop my plug-in, I needed to develop competency in two fundamental building blocks from the Standard Widget Toolkit (SWT): tables and charts. My primary references for these were:

- The source code of [A table of baseball players that allows sorting](http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/Atableofbaseballplayersandallowssorting.htm)
- [SWT Chart Gallery](https://github.com/eclipse/swtchart/wiki/Gallery)
- [SWT Chart Documentation](https://github.com/eclipse/swtchart/wiki/Documentation)
- The source code of [SWT Chart examples](https://github.com/eclipse/swtchart/tree/develop/org.eclipse.swtchart.examples/src/org/eclipse/swtchart/examples)

By integrating these resources, I successfully developed the Trace Labs plug-in, which is discussed in greater detail in the "Outcomes" section of this document.

__While I had hoped to include a view on energy usage collected via Perf or custom LTTng instrumentation, I was unable to reach that point within the project's time frame.__

## Outcomes

### Trace Labs ([Source Code](https://github.com/knicklabs/tracelabs))

The primary deliverable of the COSC 3P99 Computing Project is Trace Labs, a plug-in developed for the Eclipse IDE that is fully compatible with Trace Compass. The plug-in features five distinct views, each of which are subsequently described.

Note: In all table views, clicking on a row header will sort that column in ascending order. Clicking the same row header again will toggle the sorting direction between ascending and descending.

Instructions for installing and running the plug-in can be found in the [README](https://github.com/knicklabs/tracelabs/blob/main/README.md). This README document also provides guidelines for obtaining CTF traces via LTTng that are compatible with the plug-in.

#### View: Syscalls - Table

![Syscalls - Table](./View%20-%20Syscall%20Table.png)

This view displays all syscalls in a tabular format. Each row contains the thread ID, the name of the syscall, the number of times each syscall was invoked, the sum of the durations of each call, and the average duration. Syscalls are analyzed on a per-thread basis, so the same syscall may appear in the table multiple times, with each instance tracking its occurrences on an individual thread.

The data for this table was aggregated by matching the entry and exit points for syscalls on the same thread. These were collected into lists of entries and exits. For each pair, the entry timestamp was subtracted from the exit timestamp to calculate the duration.

If I were to continue refining this view, I would consider the following enhancements:

- Incorporate the Process ID into the table.
- Add a control feature to filter the table by process ID.
- Introduce a column for minimum duration.
- Introduce a column for maximum duration.
- Introduce a column for standard deviation.

#### View: Syscalls - Aggregated Table

![Syscalls - Aggregated Table](./View%20-%20Syscall%20Aggregate%20Table.png)

This view presents all syscalls in tabular format. Each row includes the syscall name, the number of times the syscall was called, and the average duration of each call. The syscalls were analyzed across all threads, so each syscall should only appear once in the table.

The data for this table was compiled by matching entries and exits for syscalls on the same thread, collecting them as lists of entries and exits, and for each pair, subtracting the entry timestamp from the exit timestamp to derive a duration. Then the entries and exists for each syscall by name were combined across all threads, from which the average duration was derived.

If I were to continue working on this view, two improvements I would make would be:

- Add a control to filter the table by process id.
- Add a column for minimum duration.
- Add a column for maximum duration.
- Add a column for standard deviation.

#### View: Performance Counters - Table

![Performance Counters - Table](./View%20-%20Performance%20Counters%20Table.png)

This view presents the final counts for all performance counters, presented in a tabular format. Each row consists of the performance counter name and the total number of times it was counted.

The data for this table was generated by extracting the performance counter properties from the context of the last syscall event recorded in the kernel trace.

#### View: Performance Counters - Viz

![Performance Counters - Viz](./View%20-%20Performance%20Counters%20Viz.png)

This view displays the final counts for all performance counters through graphical visualization.

The data for this visualization was aggregated by extracting the performance counter properties from the context of the final syscall event captured in the kernel trace.

If I were to further enhance this view, I would focus on the following improvements:

- Improved formatting for the counts displayed on the y-axis.
- Improved formatting for the names shown on the x-axis.

#### View: Performance Counters - Growth Viz

![Performance Counters - Growth Viz](./View%20-%20Performance%20Counters%20Growth%20Viz.png)

This view shows the cumulative growth of performance counters over time as a graphical visualization.

If I were to continue working on this view, some improvements I would make would be:

- Better formatting of the counts on the y-axis
- Better formatting of the labels on the x-axis
- Create a different scale on the x-axis that used an evenly divided duration so the distance between points was constant to more accurately visualization counter growth over time.

### LTTng Analyses Env ([Source Code](https://github.com/knicklabs/lttng-analyses-env))

I discovered a tool called [LTTng Analyses](https://github.com/lttng/lttng-analyses) that could perform a variety of analyses on traces. I thought this would be valuable for cross-referencing the results from my Trace Labs project. For example, I could validate the syscall statistics generated by the Trace Labs plug-in by comparing them to the results from this tool using the same trace data.

Unfortunately, this software had several outdated dependencies, including babeltrace, which was incompatible with babeltrace2. To resolve this issue, I attempted to use Docker to set up a Dockerized environment containing all the required dependencies, intending for this environment to run the tool against traces located on the host machine's file system. Although I successfully set up a Dockerized environment capable of running LTTng Analyses, the tool failed to analyze my traces effectively. It would only report the duration of the span, without providing any additional data.

Despite my efforts, I was unable to utilize the tool as initially planned. I speculate that this could be due to the tool's compatibility with an older version of the Common Trace Format (CTF), although I have not yet tested this hypothesis.

### Trace Analysis Lab ([Source Code](https://github.com/knicklabs/trace-analysis-lab))

![Jupyter Lab](./Jupyter%20Lab.png)

I found that working with traces in the Eclipse development environment could be cumbersome due to slow compilation times. I sought a more agile solution for exploring traces. While Babeltrace was effective for quick trace viewing, I needed a lightweight method to programmatically explore and graph these traces.

Python and Jupyter notebooks emerged as an ideal solution. Although it was possible to convert CTF traces into CSV files for Python-based analysis, these CSV files could quickly become too large to manage efficiently. I learned that Python bindings were available for Babeltrace2, which meant that the events could be streamed from a trace in Python scripts. To take advantage of this, I set up a Dockerized environment with Babeltrace2 compiled with Python extensions, as well as an instance of Jupyter labs. This allowed me to use the Babeltrace2 `bt2` Python library within Jupyter notebooks to stream events from CTF traces and then use popular Python libraries like `numpy` and `matplotlib` for data manipulation and graphing.

Instructions for setting up and using this lab environment can be found in the [README](https://github.com/knicklabs/trace-analysis-lab/blob/main/README.md).

## Next Steps and Future Work

During my initial research on energy consumption, I stumbled upon the [Green Software Foundation](https://greensoftware.foundation), a non-profit organization under the Linux Foundation. They offer a free online course and exam: [Green Software Practitioner](https://learn.greensoftware.foundation) and [Green Software for Practioners (LFC131)](https://training.linuxfoundation.org/training/green-software-for-practitioners-lfc131/). I plan to enroll in both the course and the exam as a means to deepen my understanding of green computing and to make my own practices more environmentally friendly.

I'm also keen to continue directly in this line of work, focusing on research and development related to collecting and visualizing energy usage at both the kernel and userspace levels. My goal is to identify energy hotspots—specific syscalls or function calls with unusually high energy consumption. Recognizing such hotspots could facilitate the optimization of software to run more efficiently.
