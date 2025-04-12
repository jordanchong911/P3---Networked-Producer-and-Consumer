# 📆 Media Upload System — Build & Usage Guide

This guide walks you through how to **set up**, **build**, and **run** the Producer-Consumer Media Upload System using **Java**, **Maven**, and **VS Code**.

---

## 🔧 Prerequisites

Make sure the following tools are installed on your system:

| Tool     | Minimum Version | Notes                          |
|----------|------------------|--------------------------------|
| **Java** | 17               | Required for JavaFX support    |
| **Maven**| 3.8+             | Used to build and run project  |
| **VS Code** (or IntelliJ) | –                | Recommended for running with GUI |

You can check versions using:
```bash
java -version
mvn -version
```

## 📆 Project Setup
Clone or download the repository:
```
git clone https://github.com/your-username/media-upload-system.git
cd media-upload-system
```
Ensure the following files/folders exist:

pom.xml

src/main/java/com/stdiscm/...

Optional: icon.ico for .exe builds

## 🚀 How to Build
You have two options:

🛠 Option 1: Run via VS Code (Recommended for testing)
Open the project folder in VS Code

Open the file:
```
src/main/java/com/stdiscm/LauncherMain.java
```
Right-click LauncherMain and select "Run Java" or "Run Main Class"

🛠 Option 2: Build & Run via Terminal
To build:
```
mvn clean install
```
To run the app:
```
mvn javafx:run
```

Or run the compiled fat JAR directly:
```
java -jar target/distributed_systems-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 🧑‍💻 How to Use the Application
Step 1: Choose Role
When the app launches, a GUI will ask:

❓ Do you want to be a Producer or a Consumer?

### 🟢 If you choose Producer:
You'll be asked to enter how many producer clients you want to launch.

For each producer:

You will be asked to select a folder containing video files (.mp4, .avi, .mov, .mkv).

The system will automatically upload all valid video files to the connected consumer.

### 🟠 If you choose Consumer:
You'll be asked to enter how many consumer clients you want to launch.

You'll also specify the maximum queue size (leaky bucket style).

Each consumer will:

Listen for incoming uploads

Save videos to the uploads/ folder

Show a GUI with a scrollable list of uploaded videos

Let you click on a video to preview/play it

## 🗓 Test Case Ideas
For QA testing, see our separate TEST_CASES.md for detailed test scenarios including:

Upload from multiple producers

Video format filtering

Queue limit testing

Cross-machine socket connections

## 💾 Sample Videos for Testing
You can download free test videos from:

https://sample-videos.com

https://file-examples.com

https://test-videos.co.uk

https://pexels.com/videos (for demo/demo-day quality)

## ✅ Final Notes
Make sure JavaFX is working in your environment (especially for .exe builds)

If running on separate machines, use the consumer's IP address instead of localhost

If you see firewall prompts, click "Allow access" for Java

Let us know if you need any help!

Built with ❤️ by the team.
