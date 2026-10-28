import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "MyWiki",
  description:
    "Không gian để tổ chức ý tưởng, xây dựng thế giới và kết nối những câu chuyện của bạn.",
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
