#### 1. Who's On First (WOF) Geolocation Database (for GPS Lookups)

The core GPS lookup functionality relies on a large, external Who's On First SQLite database. This file is **not committed to Git** due to its size (approx. 40GB). You need to download it manually.

* **Download the Database File:**
    1.  Go to the official Who's On First downloads page: [https://whosonfirst.org/download/](https://whosonfirst.org/download/)
    2.  Scroll down to the "Global SQLite downloads:" section.
    3.  Download a relevant **admin bundle** (e.g., `whosonfirst-data-admin-latest.sqlite.bz2`).
        * *Note:* Using this large file for the complete globe directly can lead to slow response , so download the country specific file instead.
    4.  Extract the downloaded `.bz2` file. You will get a `.sqlite` file (e.g., `whosonfirst-data-admin-latest.sqlite`).

* **Place the Database File:**
    1.  **Rename** the extracted `.sqlite` file to **`whois.db`**.
    2.  Create a new folder named `data` in your project's **root directory** (the same level as your `pom.xml` and `src` folder).
    3.  Move the renamed `whois.db` file into this `data/` folder.
        * Your project structure should look like this:
            ```
            your-project-root/
            ├── src/
            ├── data/
            │   └── whois.db  <-- YOUR WOF DATABASE GOES HERE
            ├── pom.xml
            └── ...
            ```