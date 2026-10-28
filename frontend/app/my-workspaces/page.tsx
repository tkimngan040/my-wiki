import type { Metadata } from "next";
import AccountDashboard from "@/features/account-dashboard/AccountDashboard";

export const metadata: Metadata = {
  title: "Workspace của tôi | MyWiki",
  description: "Quản lý các Workspace và nội dung cá nhân trên MyWiki.",
};

export default function MyWorkspacesPage() {
  return <AccountDashboard />;
}
