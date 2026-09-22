import { uiTranslations } from "fumadocs-ui/i18n";
import type { BaseLayoutProps } from "fumadocs-ui/layouts/shared";
import Image from "next/image";
import { i18n } from "@/lib/i18n";

export const translations = i18n
  .translations()
  .extend(uiTranslations())
  .add({
    en: { displayName: "English" },
    ko: {
      displayName: "한국어",
      "Choose a language(language switcher)": "언어 선택",
      "Choose a language(language switcher)(aria-label)": "언어 선택",
      "Next Page(pagination)": "다음 페이지",
      "Previous Page(pagination)": "이전 페이지",
      "On this page(table of contents)": "이 페이지에서",
      "Search(search trigger)": "문서 검색",
      "Search(search dialog)": "문서 검색",
      "No results found(search dialog)": "검색 결과가 없습니다",
      "Copy Text(code block)(aria-label)": "코드 복사",
      "Copied Text(code block)(aria-label)": "복사됨",
      "Toggle Theme(theme switcher)(aria-label)": "테마 전환",
      "Open Sidebar(sidebar)(aria-label)": "사이드바 열기",
      "Close Sidebar(sidebar)(aria-label)": "사이드바 닫기",
    },
  });

export function baseOptions(): BaseLayoutProps {
  return {
    nav: { title: <><Image src="/icon.svg" width={22} height={22} alt="" aria-hidden /><span className="font-semibold">Volcano</span></> },
    links: [{ text: "GitHub", url: "https://github.com/taewooyo/volcano", external: true }],
  };
}
