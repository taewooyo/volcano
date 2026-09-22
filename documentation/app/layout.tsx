import "./main.css";
import type { Metadata } from "next";
import type { ReactNode } from "react";
import { LocaleProvider } from "@/components/locale-provider";

export const metadata: Metadata = {
  title: { default: "Volcano | Hierarchical Heatmaps", template: "%s | Volcano" },
  description: "A Kotlin Multiplatform, Compose Multiplatform heatmap SDK.",
  icons: { icon: "/icon.svg", apple: "/icon.svg" },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return <html lang="en" suppressHydrationWarning><body className="flex min-h-screen flex-col"><LocaleProvider locale="en">{children}</LocaleProvider></body></html>;
}
