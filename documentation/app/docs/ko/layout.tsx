import { DocsLayout } from "fumadocs-ui/layouts/docs";
import type { ReactNode } from "react";
import { LocaleProvider } from "@/components/locale-provider";
import { baseOptions } from "@/lib/layout.shared";
import { source } from "@/lib/source";

export default function KoreanDocsLayout({ children }: { children: ReactNode }) {
  return (
    <LocaleProvider locale="ko">
      <DocsLayout i18n tree={source.getPageTree("ko")} {...baseOptions()}>{children}</DocsLayout>
    </LocaleProvider>
  );
}
