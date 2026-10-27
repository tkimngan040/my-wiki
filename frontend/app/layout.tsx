import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Tạo tài khoản | MyWiki",
  description: "Đăng ký tài khoản MyWiki để bắt đầu xây dựng thế giới của bạn.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body>{children}</body>
    </html>
  );
}
