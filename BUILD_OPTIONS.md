# 🏗️ Build Options - Chọn cách build phù hợp

## Tổng quan 3 options

| Option | Size | Speed | Native | Ease | Recommend |
|--------|------|-------|--------|------|-----------|
| **A. Full JRE Bundle** | ~60MB/app | Normal | ❌ Need JRE | ⭐⭐⭐⭐⭐ Easy | ✅ Best for demo |
| **B. Minimal JRE (jlink)** | ~40MB/app | Normal | ❌ Need JRE | ⭐⭐⭐⭐ Easy | ✅ Best balance |
| **C. Native .exe (Launch4j)** | ~5MB + JRE or standalone | Normal | ⚠️ Partial | ⭐⭐⭐ Medium | For Windows only |

---

## Option A: Full JRE Bundle (Workflow: `build-release.yml`)

### Đặc điểm:
- ✅ **Đơn giản nhất** - Just works
- ✅ **Cross-platform** - Windows, Linux, Mac
- ✅ **No dependencies** - JRE đi kèm
- ❌ **Lớn** - ~60-70MB per app (JRE chiếm 50MB)

### Khi nào dùng:
- ✅ Demo project cho môn học
- ✅ Máy target không có Java
- ✅ Muốn đơn giản, không phức tạp

### Cách sử dụng:

**1. Trigger workflow:**
```bash
git push origin main
# Or: GitHub → Actions → "Build Remote Control App" → Run workflow
```

**2. Download artifacts:**
- `remote-control-server.zip` (~60-70MB)
- `remote-control-client.zip` (~60-70MB)

**3. Run:**
```bash
# Windows
start-server.bat
start-client.bat

# Linux/Mac
./start-server.sh
./start-client.sh
```

**Build time:** ~3-5 minutes

---

## Option B: Minimal JRE with jlink (Workflow: `build-release.yml` - UPDATED)

### Đặc điểm:
- ✅ **Nhỏ hơn** - ~40MB per app (tiết kiệm 30%)
- ✅ **Optimized** - Chỉ bundle modules cần thiết
- ✅ **Cross-platform** - Works everywhere
- ✅ **Still simple** - Cùng workflow với Option A

### Modules bundled:
```
java.base          # Core Java
java.desktop       # Swing GUI
java.logging       # Logging
java.management    # System management
java.naming        # Naming services
java.net.http      # HTTP client
java.sql           # Database (if needed later)
java.xml           # XML parsing
```

### So sánh:
| Metric | Full JRE | Minimal JRE (jlink) |
|--------|----------|---------------------|
| **Size** | ~180MB | ~40MB |
| **Modules** | 70+ | 8 essential |
| **Boot time** | Normal | Slightly faster |
| **Features** | All | Enough for Swing |

### Cách sử dụng:
**Giống Option A** - Tôi đã update workflow `build-release.yml` để dùng jlink!

**Build time:** ~3-4 minutes (nhanh hơn vì không download full JRE)

---

## Option C: Native Executables (Workflow: `build-native.yml`)

### Đặc điểm:
- ✅ **Professional** - File .exe thật trên Windows
- ✅ **Small JAR** - 5MB JAR file
- ⚠️ **Partial native** - Vẫn cần JRE (hoặc check system Java)
- ❌ **Platform specific** - Phải build trên OS tương ứng

### Sub-options:

#### C1: Launch4j .exe (Wrap JAR)
- **Output:** `server.exe`, `client.exe`
- **Size:** ~5MB each (chỉ wrapper + JAR)
- **Requirement:** System Java 11+ hoặc bundle JRE
- **Pro:** Native icon, version info, professional
- **Con:** Vẫn cần Java runtime

#### C2: jpackage (True native installer)
- **Output:** `.msi` (Windows), `.dmg` (Mac), `.deb` (Linux)
- **Size:** ~40-60MB (bundle JRE)
- **Pro:** True installer, no Java needed
- **Con:** Phải build trên từng platform

### Khi nào dùng:
- ✅ Muốn app trông professional (có .exe)
- ✅ Windows users (Launch4j)
- ✅ Production deployment
- ❌ Không dùng cho demo nhanh (phức tạp hơn)

### Cách sử dụng:

**1. Trigger workflow:**
```bash
# Manual only (advanced)
GitHub → Actions → "Build Native Executables" → Run workflow
```

**2. Download artifacts:**
- `windows-server-exe` - Server .exe
- `windows-client-exe` - Client .exe
- `linux-server-bundle` - Linux bundle
- `linux-client-bundle` - Linux bundle

**3. Run:**
```bash
# Windows (if system Java available)
server.exe
client.exe

# Windows (if no Java) - need to bundle JRE manually
```

**Build time:** ~5-7 minutes (build trên Windows + Linux)

---

## 📊 So sánh chi tiết

### Size comparison (Server app):

```
Option A: Full JRE Bundle
├─ remote-server.jar    5.2 MB
├─ Full JRE            50 MB
└─ Scripts + README     0.1 MB
   TOTAL:              55.3 MB (ZIP: ~60 MB)

Option B: Minimal JRE (jlink)
├─ remote-server.jar    5.2 MB
├─ Custom JRE          35 MB   ← Saved 15MB!
└─ Scripts + README     0.1 MB
   TOTAL:              40.3 MB (ZIP: ~40 MB)

Option C: Launch4j .exe
├─ server.exe           5.3 MB (includes JAR)
└─ JRE (if bundled)    35-50 MB (optional)
   TOTAL:              5.3 MB (no JRE) or ~40 MB (with JRE)
```

### Build time comparison:

```
Option A: Full JRE
├─ Maven compile:      1 min
├─ Download JRE:       0.5 min
├─ Bundle + ZIP:       1 min
   TOTAL:              2.5 min

Option B: Minimal JRE (jlink)
├─ Maven compile:      1 min
├─ jlink build:        1 min   ← No download!
├─ Bundle + ZIP:       1 min
   TOTAL:              3 min

Option C: Native
├─ Maven compile:      1 min
├─ Windows .exe:       2 min
├─ Linux bundle:       2 min
├─ Parallel build:     (concurrent)
   TOTAL:              ~5 min
```

---

## 🎯 Recommendation

### Cho project môn học (DEMO):

**Dùng Option B: Minimal JRE (jlink)** ✅

**Lý do:**
1. ✅ Nhỏ hơn (~40MB vs 60MB) - nhanh download
2. ✅ Vẫn đơn giản - cùng workflow
3. ✅ Professional - optimized runtime
4. ✅ No dependencies - JRE đi kèm
5. ✅ Cross-platform - works everywhere

**Workflow:** `build-release.yml` (đã update để dùng jlink)

### Cho production app sau này:

**Dùng Option C: jpackage** (khi muốn native installer)

**Lý do:**
1. ✅ True native installer (.msi, .deb, .dmg)
2. ✅ Professional - có icon, menu integration
3. ✅ Bundle JRE - transparent to user
4. ✅ Auto-update support (có thể add)

---

## 🚀 Quick Start Guide

### Recommended: Option B (Minimal JRE)

```bash
# 1. Push code
git add .
git commit -m "Final build with minimal JRE"
git push origin main

# 2. Wait for GitHub Actions (~3 min)
# Check: GitHub → Actions → Latest run

# 3. Download artifacts
# - remote-control-server.zip (~40MB)
# - remote-control-client.zip (~40MB)

# 4. Extract and run
unzip remote-control-server.zip
cd server && ./start-server.sh  # or start-server.bat

# 5. Client on another machine
unzip remote-control-client.zip
cd client && ./start-client.sh  # or start-client.bat
```

**That's it!** No Java installation needed. ✅

---

## 🔧 Advanced: Local build với jlink

Nếu muốn test locally:

```bash
# 1. Build JARs
mvn clean package -DskipTests

# 2. Create minimal JRE
jlink \
  --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.sql,java.xml \
  --output custom-jre \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --compress=2

# 3. Test
./custom-jre/bin/java -jar target/remote-server.jar

# Size check
du -sh custom-jre  # ~35-40MB
```

---

## 📦 Output structure

### Option A & B (JRE Bundle):
```
server/
├── remote-server.jar
├── jre/                    # Full JRE (50MB) or Custom JRE (35MB)
│   ├── bin/java
│   ├── lib/
│   └── ...
├── start-server.sh
├── start-server.bat
└── README.txt
```

### Option C (Native):
```
server/
├── server.exe              # Launch4j wrapper (5MB)
├── remote-server.jar       # Actual app (5MB)
└── jre/ (optional)         # If bundled
```

---

## ❓ FAQ

**Q: Option nào nhỏ nhất?**  
A: Option C (5MB) nếu không bundle JRE, nhưng cần system Java. Option B (40MB) là smallest self-contained.

**Q: Option nào đơn giản nhất?**  
A: Option A/B - cùng workflow, chỉ extract và run.

**Q: Option nào professional nhất?**  
A: Option C với jpackage - native installer với icon, menu, auto-update.

**Q: Tôi nên dùng option nào cho demo môn học?**  
A: **Option B (Minimal JRE)** - perfect balance giữa size và simplicity.

**Q: Launch4j .exe có cần Java không?**  
A: Có, nhưng có thể:
1. Check system Java (if available)
2. Bundle JRE alongside .exe
3. Prompt user to install Java

**Q: jlink có giảm functionality không?**  
A: Không! Vẫn đủ cho Swing apps. Chỉ bỏ modules không dùng (JavaFX, Security extras, etc.)

---

## ✅ Current status

**Updated:** `build-release.yml` now uses **jlink** (Option B) by default!

**Available workflows:**
1. `build-release.yml` - ✅ **Recommended** - Minimal JRE with jlink
2. `build-native.yml` - Advanced - Native .exe and installers

**To switch back to full JRE:** Just revert the jlink step to download step.

---

**🎉 Ready to build!** Choose your option and push to GitHub! 🚀
