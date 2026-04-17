# 🔨 BUILD INSTRUCTIONS

## 📋 Prerequisites

### Required:
- **Java JDK 11+** - Download from https://adoptium.net/
- **Maven 3.6+** - Download from https://maven.apache.org/download.cgi

### Optional (for .exe builds):
- **Launch4j** - Download from https://launch4j.sourceforge.net/

---

## 🚀 Quick Start

### Method 1: Using Build Scripts (Recommended)

**Windows:**
```cmd
build.bat
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

### Method 2: Manual Maven Build

**Build Server:**
```bash
mvn clean package
# Output: target/remote-server.jar
```

**Build Client:**
```bash
mvn clean package -Pclient
# Output: target/remote-client.jar
```

---

## 📦 Build Outputs

After successful build, you will have:

```
target/
  ├── remote-server.jar    # Server JAR (runnable)
  └── remote-client.jar    # Client JAR (runnable)

build/                     # (If Launch4j is installed)
  ├── RemoteServer.exe     # Server executable
  └── RemoteClient.exe     # Client executable
```

---

## ▶️ Running the Applications

### Run JAR files directly:

**Start Server:**
```bash
java -jar target/remote-server.jar
# Or with custom port:
java -jar target/remote-server.jar 9999
```

**Start Client:**
```bash
java -jar target/remote-client.jar
```

### Run EXE files (Windows):

```cmd
build\RemoteServer.exe
build\RemoteClient.exe
```

---

## 🏗️ Converting JAR to EXE (Manual)

If you want to manually convert JARs to EXE:

### Step 1: Install Launch4j

Download and install from: https://launch4j.sourceforge.net/

### Step 2: Run Launch4j

**For Server:**
```bash
launch4jc launch4j-server.xml
```

**For Client:**
```bash
launch4jc launch4j-client.xml
```

**Or use Launch4j GUI:**
1. Open Launch4j
2. Load `launch4j-server.xml` or `launch4j-client.xml`
3. Click "Build wrapper"

---

## 🧪 Testing the Build

### Test Server:

```bash
# Terminal 1: Start server
java -jar target/remote-server.jar

# Expected output:
# === REMOTE CONTROL SERVER ===
# Starting server...
# [timestamp] [SERVER] [INFO] Initializing server on port 8888
# ...
```

### Test Client:

```bash
# Terminal 2: Start client
java -jar target/remote-client.jar

# Client UI should open
# Enter "localhost" and port "8888", click Connect
```

---

## 🐛 Troubleshooting

### Problem: "mvn command not found"
**Solution:** Install Maven and add to PATH
```bash
# Windows: Add to PATH environment variable
# Mac: brew install maven
# Linux: sudo apt install maven
```

### Problem: "Java version mismatch"
**Solution:** Use Java 11 or higher
```bash
java -version  # Should show 11 or higher
```

### Problem: Compilation errors
**Solution:** Clean and rebuild
```bash
mvn clean
mvn package
```

### Problem: Dependencies not downloading
**Solution:** Check internet connection and Maven settings
```bash
# Force update dependencies
mvn clean install -U
```

### Problem: "Class not found" when running JAR
**Solution:** Make sure you use the correct JAR file with dependencies:
```bash
# Use: remote-server.jar (NOT remote-server-<version>.jar)
java -jar target/remote-server.jar
```

---

## 📂 Project Structure

```
nhap_mon_lap_trinh/
├── src/
│   ├── common/          # Shared code (Message, NetworkUtils, etc.)
│   ├── server/          # Server application
│   │   ├── handlers/    # Feature handlers
│   │   └── ui/          # Server UI
│   └── client/          # Client application
│       └── ui/tabs/     # Client UI tabs
├── pom.xml              # Maven configuration
├── build.sh             # Linux/Mac build script
├── build.bat            # Windows build script
├── launch4j-server.xml  # Launch4j config for server
└── launch4j-client.xml  # Launch4j config for client
```

---

## 🎯 Next Steps After Build

1. **Start Server** on one machine
2. **Start Client** on another machine (or same for testing)
3. **Connect** client to server using server's IP:PORT
4. **Test features:**
   - List Applications
   - Capture Screenshot
   - Process Management
   - File Transfer
   - System Control

---

## 📝 Notes

- **Default port:** 8888 (configurable via command line)
- **Log files:** `server.log` and `client.log`
- **Network:** Client and Server must be on same LAN (or use port forwarding)
- **Firewall:** Make sure port 8888 is allowed through firewall

---

## 🔐 Security Warning

⚠️ **This is a learning project for educational purposes.**

- Do NOT use on production systems without proper security
- Do NOT expose server to public internet
- Use only on trusted networks
- Implement proper authentication before real-world use

---

## 📚 Additional Resources

- **Maven Docs:** https://maven.apache.org/guides/
- **Launch4j Docs:** https://launch4j.sourceforge.net/docs.html
- **Java Swing Tutorial:** https://docs.oracle.com/javase/tutorial/uiswing/

---

## ✅ Build Checklist

- [ ] Java JDK 11+ installed
- [ ] Maven 3.6+ installed
- [ ] Launch4j installed (optional)
- [ ] Run `mvn clean package` successfully
- [ ] Test server JAR
- [ ] Test client JAR
- [ ] (Optional) Convert to EXE
- [ ] Test connection between client and server

---

**Happy Building! 🚀**
