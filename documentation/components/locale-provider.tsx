"use client";

import { i18nProvider } from "fumadocs-ui/i18n";
import { RootProvider } from "fumadocs-ui/provider/next";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { translations } from "@/lib/layout.shared";

type Locale = "en" | "ko";

/**
 * Fumadocs uses a leading locale segment. The legacy English /docs routes stay
 * available, while the language-aware routes use /en/docs and /ko/docs.
 */
export function LocaleProvider({ locale, children }: { locale: Locale; children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    document.documentElement.lang = locale;
  }, [locale]);

  return (
    <RootProvider
      i18n={{
        ...i18nProvider(translations, locale),
        onLocaleChange(nextLocale) {
          const englishPath = pathname.replace(/^\/ko(?=\/|$)/, "").replace(/^\/en(?=\/|$)/, "");
          router.push(`/${nextLocale}${englishPath}`);
        },
      }}
    >
      {children}
    </RootProvider>
  );
}
