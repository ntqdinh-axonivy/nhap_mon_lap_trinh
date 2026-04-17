# 🚀 Deployment Guide - Remote Control Application

## Mục tiêu

Tạo 2 app standalone (Server + Client) có thể chạy trên **BẤT KỲ MÁY NÀO** mà KHÔNG cần cài Java.

---

## 📋 Checklist yêu cầu

### ✅ Đã hoàn thành:

1. **Connection qua IP**
   - ✅ Server hiển thị tất cả IP addresses khi khởi động
   - ✅ Client có input field để nhập Server IP
   - ✅ Client có nút "Connect" để bắt đầu kết nối
   - ✅ Status indicator (Green = connected, Red = disconnected)

2. **Standalone apps (không cần Java)**
   - ✅ GitHub Actions workflow để build trên Ubuntu
   - ✅ Bundle JRE 17 vào mỗi app
   - ✅ Launch scripts cho Windows (.bat) và Linux/Mac (.sh)
   - ✅ ZIP archives sẵn sàng download

3. **Cross-platform support**
   - ✅ Windows: Double-click `.bat` file
   - ✅ Linux/Mac: Run `.sh` script
   - ✅ Cùng 1 codebase, chạy được trên mọi OS

---

## 🔄 Workflow tổng quan

```
┌─────────────────────────────────────────────────────────────┐
│  1. Developer: Push code to GitHub                          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│  2. GitHub Actions: Build on Ubuntu VM                      │
│     - Compile Java code                                     │
│     - Package JARs (server.jar + client.jar)                │
│     - Download JRE 17 from Adoptium                         │
│     - Bundle JRE + JAR + Launch scripts                     │
│     - Create ZIP archives                                   │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Download Artifacts:                                     │
│     - remote-control-server.zip (~60MB)                     │
│     - remote-control-client.zip (~60MB)                     │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ├─────────────────────┬──────────────────────┐
                 ▼                     ▼                      ▼
┌─────────────────────────┐  ┌─────────────────────┐  ┌──────────────────┐
│  4a. Deploy Server      │  │  4b. Deploy Client  │  │  5. Connect!     │
│  - Extract ZIP          │  │  - Extract ZIP      │  │  - Server shows  │
│  - Run start-server.sh  │  │  - Run start-client │  │    IP addresses  │
│  - Note IP addresses    │  │  - Enter server IP  │  │  - Client enters │
│                         │  │  - Click Connect    │  │    server IP     │
└─────────────────────────┘  └─────────────────────┘  └──────────────────┘
```

---

## 🖥️ Server: Hiển thị IP addresses

### Code đã implement:

File: `src/server/ui/ServerUI.java`

```java
private String getLocalIPAddresses() {
    StringBuilder ips = new StringBuilder();
    try {
        Enumeration<NetworkInterface> interfaces = 
            NetworkInterface.getNetworkInterfaces();
        
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                
                if (addr instanceof Inet4Address) {
                    if (ips.length() > 0) {
                        ips.append(", ");
                    }
                    ips.append(addr.getHostAddress());
                }
            }
        }
    } catch (Exception e) {
        return "Unable to detect";
    }
    
    return ips.length() > 0 ? ips.toString() : "No network interface found";
}
```

### Hiển thị trong UI:

```
┌─────────────────────────────────────────────────────┐
│       🖥️ REMOTE CONTROL SERVER                      │
│                                                     │
│   Server IPs: 192.168.1.100, 10.0.0.5             │
└─────────────────────────────────────────────────────┘
│                                                     │
│  [LOG AREA]                                         │
│  [INFO] Server listening on port 8888              │
│  [INFO] Client connected: 192.168.1.50             │
│                                                     │
└─────────────────────────────────────────────────────┘
│  Status: LISTENING on port 8888                     │
└─────────────────────────────────────────────────────┘
```

---

## 💻 Client: Nhập IP và Connect

### Code đã implement:

File: `src/client/ui/ClientUI.java`

```java
// Connection panel
JTextField hostField = new JTextField("localhost", 12);
JTextField portField = new JTextField("8888", 6);
JButton connectButton = new JButton("Connect");
JLabel statusLabel = new JLabel("● Disconnected");

connectButton.addActionListener(e -> toggleConnection());
```

### Hiển thị trong UI:

```
┌──────────────────────────────────────────────────────────────┐
│  🖥️ REMOTE CONTROL CLIENT  Server: [192.168.1.100]  Port: [8888]  [Connect]  ● Connected  │
├──────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  [Applications] [Processes] [Screenshot] [File Transfer]│ │
│  │                                                          │ │
│  │  ← TAB CONTENT AREA →                                   │ │
│  │                                                          │ │
│  └─────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────┤
│  ┌─ Logs ──────────────────────────────────────────────────┐ │
│  │ [INFO] Connecting to 192.168.1.100:8888...             │ │
│  │ [INFO] Connected successfully!                          │ │
│  │ [INFO] Sending REQUEST: LIST_APPS                       │ │
│  │ [INFO] Received RESPONSE: 15 apps found                │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔧 GitHub Actions Workflow

### File: `.github/workflows/build-release.yml`

**Trigger options:**
- ✅ Automatic: Mỗi khi push vào `main` branch
- ✅ Manual: Click "Run workflow" trong Actions tab
- ✅ Pull Request: Khi tạo PR vào `main`

**Build steps:**

1. **Checkout code** từ GitHub repo
2. **Setup JDK 17** để compile
3. **Maven build**: `mvn clean package`
4. **Download JRE 17** từ Adoptium (~50MB)
5. **Create Server bundle**:
   ```
   server/
   ├── remote-server.jar
   ├── jre/ (bundled runtime)
   ├── start-server.sh
   ├── start-server.bat
   └── README.txt
   ```
6. **Create Client bundle**:
   ```
   client/
   ├── remote-client.jar
   ├── jre/ (bundled runtime)
   ├── start-client.sh
   ├── start-client.bat
   └── README.txt
   ```
7. **ZIP archives**: 
   - `remote-control-server.zip`
   - `remote-control-client.zip`
8. **Upload artifacts** (retention: 30 days)

**Build time:** ~3-5 minutes

---

## 📦 Launch Scripts

### Windows: `start-server.bat` / `start-client.bat`

```batch
@echo off
echo === Starting Remote Control Server ===
"%~dp0jre\bin\java.exe" -jar "%~dp0remote-server.jar"
pause
```

**Cách dùng:** Double-click file `.bat`

### Linux/Mac: `start-server.sh` / `start-client.sh`

```bash
#!/bin/bash
echo "=== Starting Remote Control Server ==="
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"$SCRIPT_DIR/jre/bin/java" -jar "$SCRIPT_DIR/remote-server.jar"
```

**Cách dùng:**
```bash
chmod +x start-server.sh
./start-server.sh
```

---

## 🌐 Network Configuration

### LAN (Cùng mạng WiFi/Ethernet)

**Server:**
```
IP: 192.168.1.100
Port: 8888
```

**Client connects to:**
```
Host: 192.168.1.100
Port: 8888
```

✅ **Works immediately** nếu cùng mạng!

### Firewall Settings

**Windows Server:**
```powershell
# Allow inbound on port 8888
netsh advfirewall firewall add rule name="Remote Control Server" dir=in action=allow protocol=TCP localport=8888
```

**Linux Server:**
```bash
# UFW
sudo ufw allow 8888/tcp

# iptables
sudo iptables -A INPUT -p tcp --dport 8888 -j ACCEPT
```

### Port Forwarding (Internet qua NAT)

Nếu Server và Client ở 2 mạng khác nhau:

1. **Router của Server**: Forward port 8888 → Server internal IP
2. **Client connects to**: Router public IP + port 8888

---

## 🎯 Step-by-Step Deployment

### Bước 1: Build trên GitHub

```bash
# Push code
git add .
git commit -m "Ready for deployment"
git push origin main

# Hoặc manual trigger trên GitHub Actions UI
```

### Bước 2: Download artifacts

1. Vào **Actions** tab
2. Click vào workflow run mới nhất (màu xanh ✅)
3. Scroll xuống **Artifacts**
4. Download:
   - `remote-control-server.zip`
   - `remote-control-client.zip`

### Bước 3: Deploy Server (máy bị điều khiển)

```bash
# Extract
unzip remote-control-server.zip
cd server

# Run
./start-server.sh    # Linux/Mac
# hoặc
start-server.bat     # Windows

# Lưu IP address hiện trong UI
```

### Bước 4: Deploy Client (máy điều khiển)

```bash
# Extract
unzip remote-control-client.zip
cd client

# Run
./start-client.sh    # Linux/Mac
# hoặc
start-client.bat     # Windows
```

### Bước 5: Connect!

1. Trong Client UI, nhập Server IP (ví dụ: `192.168.1.100`)
2. Port: `8888`
3. Click **"Connect"**
4. Status chuyển sang **"● Connected"** (màu xanh)
5. Bắt đầu sử dụng các tính năng!

---

## 🧪 Testing Scenarios

### Scenario 1: Test trên cùng 1 máy (localhost)

```
Server: localhost (127.0.0.1)
Client: localhost (127.0.0.1)
Port: 8888
```

✅ **Use case:** Development, testing

### Scenario 2: 2 máy trong cùng mạng LAN

```
Server: 192.168.1.100
Client: 192.168.1.50
Port: 8888
```

✅ **Use case:** Điều khiển máy trong phòng/office

### Scenario 3: Qua Internet (với port forwarding)

```
Server: behind NAT (192.168.1.100 → public IP x.x.x.x)
Client: anywhere on Internet
Connect to: x.x.x.x:8888
```

✅ **Use case:** Remote control từ xa

---

## ⚠️ Security Considerations (cho môn học)

**Hiện tại:** No authentication, no encryption

**Lý do:** Đơn giản hóa để tập trung vào networking concepts

**Nếu mở rộng sau:**
- Add username/password authentication
- Implement SSL/TLS encryption
- Add IP whitelist
- Add session tokens

**Cảnh báo:** Không sử dụng trên mạng công cộng mà không có bảo mật!

---

## 📊 File Sizes

| Component | Size | Description |
|-----------|------|-------------|
| Server JAR | ~50KB | Compiled Java code |
| Client JAR | ~50KB | Compiled Java code |
| JRE 17 | ~50MB | Java Runtime Environment |
| **Server ZIP** | **~60MB** | JAR + JRE + Scripts |
| **Client ZIP** | **~60MB** | JAR + JRE + Scripts |

**Total download:** ~120MB cho cả 2 apps

---

## ✅ Verification Checklist

Trước khi demo:

- [ ] GitHub Actions build thành công (green ✅)
- [ ] Downloaded cả 2 ZIP files
- [ ] Extracted và test trên Windows hoặc Linux/Mac
- [ ] Server khởi động và hiển thị IP addresses
- [ ] Client khởi động và hiện connection panel
- [ ] Client connect thành công đến Server
- [ ] Status indicator chuyển sang green
- [ ] Test ít nhất 1 tính năng (List Apps / Screenshot)
- [ ] Check logs trong UI

---

## 🎉 Success Criteria

✅ **App chạy được trên máy không có Java cài sẵn**
✅ **Server tự động hiển thị IP để Client biết connect vào đâu**
✅ **Client có giao diện để nhập IP và nút Connect**
✅ **Build hoàn toàn trên GitHub Actions, không cần build local**
✅ **Output là 2 ZIP files sẵn sàng deploy**

---

## 📚 Related Documentation

- `GITHUB_ACTIONS_GUIDE.md` - Chi tiết về GitHub Actions workflow
- `BUILD_INSTRUCTIONS.md` - Build manually nếu cần
- `PROJECT_SUMMARY.md` - Tổng quan project
- `ARCHITECTURE.md` - Kiến trúc kỹ thuật

---

**🚀 Ready to Deploy!**
