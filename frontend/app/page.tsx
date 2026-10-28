import type { Metadata } from "next";
import PublicHome from "@/features/public-home/PublicHome";

export const metadata: Metadata = {
  title: "MyWiki — Không gian tri thức của bạn",
  description:
    "Khám phá tri thức, tổ chức ý tưởng và kết nối những thế giới của bạn với MyWiki.",
};

export default function HomePage() {
  return <PublicHome />;
}
