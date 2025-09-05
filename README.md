# PKES Save Editor

A web-based tool designed to upload, edit, and download `.rxdata` save files, especially for the games based on Pokemon Essentials engine. This editor provides a user-friendly interface to modify save game data directly in your browser.

---

## Features

-   **Upload `.rxdata` Files**: Easily upload your `.rxdata` save files through a simple web interface.
-   **Tree-View Editor**: Displays save data in an intuitive, collapsible tree structure, allowing for easy navigation and editing of nested values.
-   **Live Data Modification**: Edit character stats, inventory, switches, variables, and any other data stored in the save file.
-   **Download as `.rxdata`**: Save your changes and download the modified data back in the original `.rxdata` format, ready to be used in your game.

---

## How It Works

The editor uses a hybrid backend to handle the complex `.rxdata` format, which is a serialized Ruby object (`Marshal.dump`).

1.  **Upload**: The user uploads an `.rxdata` file.
2.  **Parse**: The **Spring Boot (Java)** backend uses **JRuby** to execute a Ruby script (`parser.rb`). This script reads the binary `.rxdata` file using `Marshal.load` and converts the Ruby object into a **JSON** string.
3.  **Edit**: The JSON data is sent to the **Vue.js** frontend, which renders an interactive editor. The user modifies the data in the browser.
4.  **Generate**: The user clicks 'Download'. The modified JSON data is sent back to the Spring Boot server.
5.  **Rebuild**: The server uses JRuby again to execute another Ruby script (`generator.rb`). This script takes the JSON data, reconstructs the original Ruby object structure (including instance variables), and serializes it back into the binary `.rxdata` format using `Marshal.dump`.
6.  **Download**: The server sends the newly generated `.rxdata` file to the user.

---

## Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=badge&logo=spring-boot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=badge&logo=gradle&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=badge&logo=vue.js&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=badge&logo=javascript&logoColor=black)
![Ruby](https://img.shields.io/badge/Ruby-CC342D?style=badge&logo=ruby&logoColor=white)

---

## Setup and Running

To run the project locally, you will need Java (JDK), Node.js, and npm installed.

### Backend

From the project's root directory, run the Spring Boot application using Gradle:

```bash
# On Windows
./gradlew.bat bootRun

# On macOS/Linux
./gradlew bootRun
```
The server will start on `http://localhost:8080`.

### Frontend (for development)
To run the frontend in development mode with hot-reloading:

```bash
# Navigate to the vue directory
cd src/main/vue

# Install dependencies
npm install

# Run the development server
npm run serve
```

The frontend will be available at `http://localhost:8081` (or another port if 8081 is busy) and will proxy API requests to the backend server running on port 8080.

For production, the `build.gradle` file is configured to automatically build the Vue.js application and place the static files into the Spring Boot application, so it can be served as a single, standalone `.jar` file.