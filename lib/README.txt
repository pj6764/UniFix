MySQL Connector/J (JDBC Driver) Instructions
==============================================

To compile and run UniFix, you need the MySQL Connector/J driver.

1. AUTOMATIC DOWNLOAD (PowerShell):
   Run the following command in PowerShell:
   powershell -ExecutionPolicy Bypass -File lib/download_driver.ps1

2. MANUAL DOWNLOAD:
   - Download mysql-connector-j-8.3.0.jar (or any 8.x/9.x version) from Maven Central:
     https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar
     OR from official MySQL downloads:
     https://dev.mysql.com/downloads/connector/j/
   - Place the `.jar` file directly inside this `lib/` directory.

3. VERIFY:
   Ensure `lib/mysql-connector-j-8.3.0.jar` is present before compiling with:
   javac -cp "lib/*;src" -d bin src/Main.java src/config/*.java src/models/*.java src/dao/*.java src/ui/*.java
