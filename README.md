# MyWiki
MyWiki là hệ thống quản lý tri thức cá nhân, được phát triển nhằm hỗ trợ người dùng lưu trữ, tổ chức và liên kết thông tin một cách hiệu quả. Hệ thống hướng đến việc xây dựng một không gian làm việc linh hoạt, nơi người dùng có thể quản lý ghi chú, tài liệu, ý tưởng và thế giới truyện trong cùng một nền tảng.

## Mục tiêu
- Xây dựng một hệ thống quản lý tri thức cá nhân.
- Hỗ trợ quản lý ghi chú, tài liệu và ý tưởng.
- Hỗ trợ lưu trữ thế giới truyện (Worldbuilding) cho tiểu thuyết.
- Là dự án học tập và nghiên cứu về phát triển Backend.

## Đối tượng sử dụng
- Cá nhân có nhu cầu ghi chú và quản lý tri thức.
- Người viết truyện và xây dựng thế giới truyện.
- Sinh viên và người học muốn tổ chức tài liệu.

## Ngôn ngữ sử dụng & Kiến trúc mã nguồn
- Dự án sử dụng ngôn ngữ lập trình chính là: Java.
- Mô hình Kiến trúc Mã nguồn là: Modular Monolith

## Cấu trúc thư mục

```text
my-wiki/
│
├── docs/
│   ├── 00_Overview.md
│   ├── 01_Requirements.md
│   └── 02_System Analysis.md
│   └── 03_Project Structure.md
│
├── src/
|   ├── Auth
|   ├── User
|   ├── Workspace
|   ├── Folder
|   ├── Page
|   ├── Link
|   ├── Search
|   ├── Permission (Access Control)
|   └── Shared (Common)
│
├── .gitignore
├── LICENSE
└── README.md
```