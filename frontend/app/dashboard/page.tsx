import type { Metadata } from "next";
import { redirect } from "next/navigation";

export const metadata: Metadata = {
  title: "Workspace cá nhân | MyWiki",
  description: "Mở không gian làm việc cá nhân trên MyWiki.",
};

export default function DashboardPage() {
  redirect("/my-workspaces");
}
