import "./main.css";
import type { Metadata } from "next";
import type { ReactNode } from "react";
import { LocaleProvider } from "@/components/locale-provider";

const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "";
const siteUrl = "https://taewooyo.github.io/volcano";
const socialImageUrl = `${siteUrl}/images/volcano-hero.png`;

export const metadata: Metadata = {
  title: { default: "Volcano | Hierarchical Heatmaps", template: "%s | Volcano" },
  description: "A Kotlin Multiplatform, Compose Multiplatform heatmap SDK.",
  metadataBase: new URL(`${siteUrl}/`),
  openGraph: {
    type: "website",
    url: `${siteUrl}/`,
    siteName: "Volcano",
    title: "Volcano | Hierarchical Heatmaps",
    description: "A Kotlin Multiplatform, Compose Multiplatform heatmap SDK.",
    images: [
      {
        url: socialImageUrl,
        width: 1672,
        height: 941,
        alt: "Volcano hierarchical heatmap landscape",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Volcano | Hierarchical Heatmaps",
    description: "A Kotlin Multiplatform, Compose Multiplatform heatmap SDK.",
    images: [socialImageUrl],
  },
  // Metadata URLs are emitted verbatim by Next's static exporter, so include
  // the GitHub Pages project prefix explicitly when one is configured.
  icons: { icon: `${basePath}/icon.svg`, apple: `${basePath}/icon.svg` },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return <html lang="en" suppressHydrationWarning><body className="flex min-h-screen flex-col"><LocaleProvider locale="en">{children}</LocaleProvider></body></html>;
}
