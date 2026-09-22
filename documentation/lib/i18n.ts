import { defineI18n } from "fumadocs-core/i18n";

/** Locale configuration shared by the content loader and documentation UI. */
export const i18n = defineI18n({
  defaultLanguage: "en",
  fallbackLanguage: "en",
  languages: ["en", "ko"],
});
