![](https://img.shields.io/badge/language-java-orange.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
<img src="https://img.shields.io/github/last-commit/lazycodedoggy/AutoPrintModeChoiceApp" alt="last-commit" />

# Background
AutoPrintModeChoiceApp is a desktop application specifically designed for printing reports, committed to optimizing large-scale report printing scenarios. This application optimizes the end-to-end printing process by autonomously and intelligently selecting the printer’s mode, ensuring that color mode is used only when necessary to minimize costs while guaranteeing the correct and attractive printing of reports.



The features of report printing scenarios generally include:
- The header and footer of the report are fix with a colored logo and contact information;

- Apart from the header and footer, the majority of the content consists of standard black text;

- The report may include a few colored charts and text.

  

In response to these features, AutoPrintModeChoiceApp offers a comprehensive set of intelligent printing solutions:
1. Pre-printed headers and footers: Use pre-printed paper with colored headers and footers loaded into the printer’s paper tray, allowing most pages of the report to be printed in black and white.
2. Intelligent color mode choice: The application first removes the headers and footers from each page of the report, then performs color printing only on pages containing color elements (such as charts or highlighted colored text), significantly reducing the use of colored ink.



# Install

This project use maven to compile and install. Java version >= 1.8

```
mvn clean install
```



# Usage

```
mvn package
```

Run the application in target/AutoPrintModeChoiceApp-1.0-shaded.jar



# Others

You can use command below for testing:

```
./AutoPrintModeChoice.py
```

The file 1.png is a typical color chart in report, but can be optimized to normal print mode with no impact.



For advanced printers, an alternative C# desktop application can fix driver compatibility issues, welcome to contact the email stephen4sheng@gmail.com or wx: ahuatian655418

