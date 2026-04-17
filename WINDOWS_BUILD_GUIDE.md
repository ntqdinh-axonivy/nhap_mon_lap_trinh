# 🪟 Windows Native Build Guide

## 🎯 Overview

Project này được configure để build **Native Windows Executables** với:
- ✅ **RemoteServer.exe** - Native .exe file
- ✅ **RemoteClient.exe** - Native .exe file  
- ✅ **Bundled JRE** - Không cần cài Java
- ✅ **Self-contained** - Chỉ extract và chạy

---

## 📦 Output Structure

### Server Bundle (~40-45MB):
```
RemoteControlServer-Windows.zip
└── RemoteServer/
    ├── RemoteServer.exe        ← Main executable
    ├── remote-server.jar       ← Backup JAR
    ├── jre/                    ← Bundled Java Runtime (35MB)
    │   ├── bin/
    │   ├── lib/
    │   └── ...
    ├── README.txt              ← User instructions
    └── START_SERVER.bat        ← Quick start script
```

### Client Bundle (~40-45MB):
```
RemoteControlClient-Windows.zip
└── RemoteClient/
    ├── RemoteClient.exe        ← Main executable
    ├── remote-client.jar       ← Backup JAR
    ├── jre/                    ← Bundled Java Runtime (35MB)
    │   ├── bin/
    │   ├── lib/
    │   └── ...
    ├── README.txt              ← User instructions
    └── START_CLIENT.bat        ← Quick start script
```

---

## 🚀 Build Process

### Method 1: GitHub Actions (Recommended)

**Auto-build on push:**
```bash
git add .
git commit -m "Build Windows native executables"
git push origin main
```

**Or manual trigger:**
1. Go to GitHub → **Actions** tab
2. Select "Build Windows Native Executables"
3. Click **"Run workflow"** → **"Run workflow"**
4. Wait ~4-6 minutes

### Method 2: Local Build (Windows only)

Nếu muốn build locally trên Windows:

```cmd
# 1. Install prerequisites
# - JDK 17
# - Maven
# - Launch4j (download from sourceforge)

# 2. Build JARs
mvn clean package -DskipTests

# 3. Create JRE bundle
jlink ^
  --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.sql,java.xml ^
  --output jre-bundle ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2

# 4. Create EXE files with Launch4j
launch4j.exe launch4j-server.xml
launch4j.exe launch4j-client.xml

# 5. Package manually
# Copy .exe + jar + jre-bundle to folders
```

---

## 📥 Download & Deploy

### Step 1: Download from GitHub Actions

1. Go to **Actions** tab
2. Click on latest successful workflow run (green ✅)
3. Scroll to **Artifacts** section
4. Download:
   - `RemoteControlServer-Windows.zip` (~40-45MB)
   - `RemoteControlClient-Windows.zip` (~40-45MB)

### Step 2: Extract on Windows machines

**On Server machine:**
```cmd
1. Right-click RemoteControlServer-Windows.zip → Extract All
2. Navigate to extracted folder
3. Double-click RemoteServer.exe (or START_SERVER.bat)
```

**On Client machine:**
```cmd
1. Right-click RemoteControlClient-Windows.zip → Extract All
2. Navigate to extracted folder
3. Double-click RemoteClient.exe (or START_CLIENT.bat)
```

### Step 3: Connect

**Server side:**
- Note the IP addresses shown in the UI (e.g., `192.168.1.100`)

**Client side:**
- Enter server IP in the "Server" field
- Port: `8888`
- Click **"Connect"**
- Status should turn green ✅

---

## 🔥 Windows Firewall

### First time running:

Windows Defender Firewall will prompt:

```
┌──────────────────────────────────────────┐
│ Windows Security Alert                   │
├──────────────────────────────────────────┤
│ Windows Defender Firewall has blocked    │
│ some features of this app.               │
│                                          │
│ Name: RemoteServer.exe                   │
│                                          │
│ [ ] Private networks                     │
│ [✓] Public networks                      │
│                                          │
│         [Allow access] [Cancel]          │
└──────────────────────────────────────────┘
```

**Action:** Click **"Allow access"**

### Manual firewall rule:

If needed, add manually:

```cmd
# Allow server on port 8888
netsh advfirewall firewall add rule name="Remote Control Server" dir=in action=allow protocol=TCP localport=8888

# Or through GUI:
# Control Panel → Windows Defender Firewall → Advanced Settings
# → Inbound Rules → New Rule → Port → TCP 8888 → Allow
```

---

## ⚙️ Launch4j Configuration

### What is Launch4j?

Launch4j wraps your JAR into a native Windows .exe with:
- ✅ Native Windows icon
- ✅ Version information
- ✅ JRE bundling support
- ✅ Better user experience

### Key settings:

**launch4j-server.xml:**
```xml
<jre>
    <path>jre</path>                      ← Look for bundled JRE
    <bundledJre64Bit>true</bundledJre64Bit>
    <bundledJreAsFallback>true</bundledJreAsFallback>
    <minVersion>11</minVersion>            ← Minimum Java version
    <runtimeBits>64</runtimeBits>          ← 64-bit only
</jre>
```

**How it works:**
1. `.exe` tries to find bundled JRE in `jre/` folder
2. If not found, looks for system Java
3. If system Java not found, prompts user to install

---

## 🎯 Advantages of Native Windows Build

| Feature | Native .exe | JAR + Scripts |
|---------|-------------|---------------|
| **User Experience** | ✅ Professional | ⚠️ Technical |
| **Icon** | ✅ Custom | ❌ Java icon |
| **File Association** | ✅ Can add | ❌ No |
| **Version Info** | ✅ Shows in Properties | ❌ No |
| **Windows Integration** | ✅ Better | ⚠️ OK |
| **Distribution** | ✅ Single file* | ⚠️ Multiple files |

*Single EXE if JRE not bundled, or folder if bundled

---

## 📊 Size Breakdown

### Server Bundle:
```
RemoteServer.exe:      5.2 MB  (Launch4j wrapper + JAR)
jre/ (bundled):       35.0 MB  (Minimal JRE via jlink)
README.txt:            0.005 MB
START_SERVER.bat:      0.001 MB
----------------------------------------------
TOTAL (uncompressed): 40.2 MB
ZIP compressed:       ~42 MB
```

### Client Bundle:
```
RemoteClient.exe:      5.2 MB  (Launch4j wrapper + JAR)
jre/ (bundled):       35.0 MB  (Minimal JRE via jlink)
README.txt:            0.005 MB
START_CLIENT.bat:      0.001 MB
----------------------------------------------
TOTAL (uncompressed): 40.2 MB
ZIP compressed:       ~42 MB
```

**Total download:** ~84 MB for both apps

---

## 🧪 Testing Checklist

### ✅ Pre-build verification:
- [ ] Code compiles: `mvn clean compile`
- [ ] JARs build: `mvn clean package`
- [ ] Both server.jar and client.jar exist in `target/`

### ✅ Post-build verification:

**On Windows machine without Java:**
- [ ] Extract Server ZIP
- [ ] Double-click `RemoteServer.exe`
- [ ] Application starts without errors
- [ ] UI shows IP addresses
- [ ] No Java installation prompts

**Client test:**
- [ ] Extract Client ZIP
- [ ] Double-click `RemoteClient.exe`
- [ ] Application starts
- [ ] Can enter server IP
- [ ] Connects successfully

**Feature test:**
- [ ] List Applications works
- [ ] Screenshot capture works
- [ ] File transfer works
- [ ] System control works

---

## ⚠️ Known Limitations

### Windows Defender SmartScreen:

First time running may show:

```
┌──────────────────────────────────────────┐
│ Windows protected your PC                │
├──────────────────────────────────────────┤
│ Windows Defender SmartScreen prevented   │
│ an unrecognized app from starting.       │
│                                          │
│ [Don't run]  [More info]                 │
└──────────────────────────────────────────┘
```

**Solution:**
1. Click **"More info"**
2. Click **"Run anyway"**

**Why?** Unsigned executable. To fix:
- Purchase code signing certificate (~$200/year)
- Sign .exe with certificate
- Windows will recognize as trusted

### Antivirus False Positives:

Some antivirus may flag the .exe:
- **Reason:** Behavior similar to remote control tools
- **Solution:** Add to antivirus whitelist
- **Note:** This is expected for remote control apps

---

## 🔧 Troubleshooting

### Problem: "Java not found" error

**Solution:**
- Ensure `jre/` folder is in same directory as .exe
- Check `jre/bin/java.exe` exists
- Verify Launch4j config has `<path>jre</path>`

### Problem: .exe won't start

**Check:**
1. Right-click .exe → Properties → Details → Should show version info
2. Open CMD, run: `server.exe` → Check error message
3. Check Windows Event Viewer → Application logs

### Problem: Connection fails

**Check:**
1. Server is running (green status)
2. Server shows IP addresses
3. Client entered correct IP
4. Both on same network
5. Firewall allows port 8888

### Problem: Slow startup

**Normal behavior:**
- First launch: 3-5 seconds (JVM initialization)
- Subsequent launches: 2-3 seconds
- If slower: Check antivirus real-time scanning

---

## 📚 Related Documentation

- `BUILD_OPTIONS.md` - Compare all build methods
- `DEPLOYMENT_GUIDE.md` - General deployment guide
- `FINAL_CHECKLIST.md` - Pre-demo checklist
- `README.md` - Project overview

---

## 🎓 For Presentation

### Key talking points:

1. **Native Windows Experience**
   - "App sử dụng Launch4j để tạo native .exe"
   - "User chỉ cần double-click, không biết về Java"

2. **Self-Contained**
   - "Bundle minimal JRE 35MB (thay vì 180MB full JRE)"
   - "Sử dụng jlink để optimize modules"

3. **Professional Packaging**
   - "File .exe có version info, icon"
   - "Integration tốt với Windows"

4. **CI/CD**
   - "Build hoàn toàn trên GitHub Actions"
   - "Không cần build trên máy cá nhân"

---

## ✅ Success Criteria

Your build is successful if:

- [x] ✅ GitHub Actions build completes (green checkmark)
- [x] ✅ 2 ZIP artifacts available for download
- [x] ✅ `.exe` files run on Windows without Java
- [x] ✅ Server shows IP addresses
- [x] ✅ Client connects to server
- [x] ✅ Features work (list apps, screenshot, etc.)
- [x] ✅ Professional look (.exe instead of .jar)

---

## 🎉 You're Ready!

```bash
# Push to GitHub
git add .
git commit -m "Configure Windows native build with Launch4j"
git push origin main

# Wait for GitHub Actions (~4-6 min)
# Download artifacts
# Extract and test
# Demo! 🚀
```

**Target achieved:** Native Windows .exe with bundled JRE! 🪟✨
