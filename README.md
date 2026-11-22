# Báo chữ to (APK cho Boox Note Air 5C, Android 15)

Ứng dụng đọc báo chữ to, chỉnh được, giao diện trắng đen cho e-ink.
Mặc định có: VnExpress, CafeBiz, BaoMoi, Dân Trí, Tuổi Trẻ, Thanh Niên, ZNews.
Thêm tờ báo bất kỳ ngay trong app (nút "+ Thêm báo") - không cần sửa code.

## Cách lấy file APK (không cần cài gì trên máy tính)

1. Tạo tài khoản GitHub (miễn phí), tạo repository mới (Private cũng được).
2. Giải nén file zip này, kéo TOÀN BỘ nội dung vào repository (Add file > Upload files).
   Nhớ có cả thư mục ẩn `.github` (trên Windows kéo bình thường là có; nếu thiếu,
   chọn Add file > Create new file, gõ tên `.github/workflows/build.yml` rồi dán nội dung file đó).
3. Vào tab **Actions**, chọn "Build APK" > "Run workflow" (hoặc chờ nó tự chạy sau khi upload).
4. Đợi 3-6 phút, bấm vào lần chạy xong (dấu tick xanh), kéo xuống mục **Artifacts**,
   tải `BaoChuTo-apk` (file zip chứa `app-debug.apk`).
5. Chép `app-debug.apk` sang Boox (cáp USB hoặc ứng dụng chia sẻ file), mở bằng
   trình quản lý file và cho phép "Cài ứng dụng từ nguồn này".

## Hoặc dùng Android Studio
File > Open thư mục này > đợi đồng bộ Gradle > Build > Build APK(s).
File nằm ở `app/build/outputs/apk/debug/app-debug.apk`.

## Cách dùng
- Màn hình chính: A- / A+ đổi cỡ chữ (áp dụng cho mọi màn hình), "+ Thêm báo" để thêm báo.
  Nhấn giữ tên báo: Sửa / Xoá / Khôi phục danh sách mặc định.
- Thêm báo: chỉ cần gõ trang chủ (ví dụ `thanhnien.vn`). App tự tìm RSS; nếu bạn biết link RSS thì dán vào ô thứ ba.
  Nếu trang không có RSS, bấm "Trang chủ" trong danh sách bài để duyệt trực tiếp (chữ vẫn phóng to).
- Màn hình đọc: "Đọc gọn" bỏ quảng cáo/menu chỉ giữ chữ và ảnh bài báo; "Ảnh" bật/tắt ảnh (tắt cho nhanh);
  "Lên" / "Xuống" cuộn theo cả trang (đỡ phải vuốt trên e-ink).

## Mẹo cho Boox
Cài xong, vào Boox: Cài đặt ứng dụng (App optimization) > chọn "Báo chữ to" > chế độ làm tươi
Regal / Normal để chữ nét, ít bóng mờ; A2 nhanh hơn nhưng chữ xấu hơn.

## Nếu một tờ báo không hiện danh sách bài
Link RSS mặc định có thể đã đổi. Nhấn giữ tên báo > Sửa > xoá ô RSS (để app tự tìm lại) hoặc dán link RSS đúng.
