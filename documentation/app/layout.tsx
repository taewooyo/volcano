import "./main.css";
import type { Metadata } from "next";
import type { ReactNode } from "react";
import { LocaleProvider } from "@/components/locale-provider";

const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "";

export const metadata: Metadata = {
  title: { default: "Volcano | Hierarchical Heatmaps", template: "%s | Volcano" },
  description: "A Kotlin Multiplatform, Compose Multiplatform heatmap SDK.",
  // Metadata URLs are emitted verbatim by Next's static exporter, so include
  // the GitHub Pages project prefix explicitly when one is configured.
  icons: { icon: `${basePath}/icon.svg`, apple: `${basePath}/icon.svg` },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return <html lang="en" suppressHydrationWarning><body className="flex min-h-screen flex-col"><LocaleProvider locale="en">{children}</LocaleProvider></body></html>;
}
