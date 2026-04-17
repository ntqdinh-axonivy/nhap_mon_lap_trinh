# 🚀 GitHub Actions Build Guide

## Tổng quan

GitHub Actions workflow sẽ tự động:
1. ✅ Build cả Server và Client JAR files
2. ✅ Download JRE 17 từ Adoptium
3. ✅ Bundle JRE vào mỗi app (không cần cài Java trên máy chạy!)
4. ✅ Tạo launch scripts cho Windows/Linux/Mac
5. ✅ Đóng gói thành 2 ZIP files: `remote-control-server.zip` và `remote-control-client.zip`

## 📋 Yêu cầu

- Repository phải được push lên GitHub
- Không cần cài đặt gì trên máy local của bạn
- GitHub Actions sẽ build trên Ubuntu VM

---

## 🎯 Cách sử dụng

### Bước 1: Push code lên GitHub

```bash
# Nếu chưa có repo, tạo mới trên GitHub rồi:
git init
git add .
git commit -m "Initial commit - Remote Control App"
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

### Bước 2: Trigger build

Có 3 cách để trigger build:

#### Option A: Tự động khi push
```bash
# Mỗi khi push lên branch main/master, build sẽ tự động chạy
git push origin main
```

#### Option B: Manual trigger (Đề xuất!)
1. Vào GitHub repo của bạn
2. Click tab **Actions**
3. Chọn workflow **"Build Remote Control App with Bundled JRE"**
4. Click nút **"Run workflow"** → **"Run workflow"**
5. Đợi ~3-5 phút để build hoàn tất

#### Option C: Tạo Pull Request
- Mở PR vào branch main → build tự động chạy

### Bước 3: Download artifacts

Sau khi build xong (màu xanh ✅):

1. Click vào workflow run (màu xanh)
2. Scroll xuống phần **Artifacts**
3. Download 2 files:
   - 📦 `remote-control-server.zip` (~60MB)
   - 📦 `remote-control-client.zip` (~60MB)

---

## 📦 Cấu trúc ZIP files

### remote-control-server.zip
```
server/
├── remote-server.jar          # Server application
├── jre/                        # Bundled Java Runtime
│   ├── bin/java
│   ├── lib/
│   └── ...
├── start-server.sh            # Linux/Mac launcher
├── start-server.bat           # Windows launcher
└── README.txt
```

### remote-control-client.zip
```
client/
├── remote-client.jar          # Client application
├── jre/                        # Bundled Java Runtime
│   ├── bin/java
│   ├── lib/
│   └── ...
├── start-client.sh            # Linux/Mac launcher
├── start-client.bat           # Windows launcher
└── README.txt
```

---

## 🖥️ Cách chạy trên máy KHÔNG CÓ Java

### Trên Server (máy bị điều khiển):

**Windows:**
```cmd
1. Giải nén remote-control-server.zip
2. Double-click start-server.bat
3. Note IP address hiện trong UI
```

**Linux/Mac:**
```bash
1. unzip remote-control-server.zip
2. cd server
3. ./start-server.sh
4. Note IP address hiện trong UI
```

### Trên Client (máy điều khiển):

**Windows:**
```cmd
1. Giải nén remote-control-client.zip
2. Double-click start-client.bat
3. Nhập IP của server vào ô "Server"
4. Click "Connect"
```

**Linux/Mac:**
```bash
1. unzip remote-control-client.zip
2. cd client
3. ./start-client.sh
4. Nhập IP của server vào ô "Server"
5. Click "Connect"
```

---

## 🔍 Kiểm tra build status

### Trong GitHub Actions UI:

- 🟢 **Green check** = Build thành công
- 🔴 **Red X** = Build failed
- 🟡 **Yellow dot** = Đang build

### Xem logs:

1. Click vào workflow run
2. Click vào job **"build"**
3. Expand từng step để xem logs chi tiết

---

## ⚠️ Troubleshooting

### Build failed - Maven compilation error

```bash
# Check lại code có lỗi syntax không
mvn clean compile
```

### Build failed - Download JRE timeout

```yaml
# Trong workflow file, thử mirror khác:
# https://github.com/adoptium/temurin17-binaries/releases/
```

### Artifacts không có

- Build phải thành công (green ✅) mới có artifacts
- Artifacts retention: 30 days
- Sau 30 ngày phải build lại

### ZIP files quá lớn (>100MB)

- Normal! JRE chiếm ~50MB
- Server + Client mỗi cái ~60-70MB
- Có thể optimize bằng `jlink` (tạo minimal JRE)

---

## 🎯 Kết nối Server-Client

### Server hiển thị IP addresses:

```
Server IPs: 192.168.1.100, 10.0.0.5
```

→ Client dùng 1 trong các IP này để kết nối

### Lưu ý:

- ✅ **Cùng mạng LAN**: Dùng IP `192.168.x.x` hoặc `10.x.x.x`
- ❌ **Khác mạng**: Cần port forwarding / VPN
- ⚡ **Localhost**: Dùng `127.0.0.1` nếu test trên cùng 1 máy

### Port mặc định: **8888**

- Đảm bảo firewall không block port 8888
- Windows: Allow in Windows Defender Firewall
- Linux: `sudo ufw allow 8888`

---

## 📊 Build Statistics

Workflow thời gian:
- ⏱️ Maven build: ~1 minute
- ⏱️ Download JRE: ~30 seconds
- ⏱️ Create bundles: ~20 seconds
- ⏱️ ZIP & upload: ~1 minute
- **🕐 Total: ~3-5 minutes**

Artifact sizes:
- 📦 Server ZIP: ~60-70MB
- 📦 Client ZIP: ~60-70MB
- 💾 Total download: ~120-140MB

---

## 🚀 Advanced: Optimize JRE size với jlink

Nếu muốn giảm size xuống ~30MB:

```bash
# Tạo minimal JRE chỉ với modules cần thiết
jlink --add-modules java.base,java.desktop,java.logging,java.net.http \
      --output custom-jre \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2
```

Thêm vào workflow sau step "Download JRE".

---

## ✅ Checklist trước khi build

- [ ] Code compile thành công locally: `mvn clean package`
- [ ] `pom.xml` có đúng manifest configs
- [ ] Push all code lên GitHub
- [ ] `.github/workflows/build-release.yml` có trong repo
- [ ] Không có large binary files trong repo (>100MB)

---

## 🎉 Done!

Sau khi download 2 ZIP files:
1. ✅ Giải nén trên bất kỳ máy nào (Windows/Linux/Mac)
2. ✅ Chạy script tương ứng (.bat hoặc .sh)
3. ✅ Không cần cài Java
4. ✅ Sẵn sàng demo!

---

## 📞 Support

Nếu gặp vấn đề:
1. Check workflow logs trong GitHub Actions
2. Xem phần Troubleshooting ở trên
3. Re-run workflow: Click "Re-run all jobs"

**Happy Building! 🚀**
