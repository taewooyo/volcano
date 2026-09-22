import { readdir, readFile } from "node:fs/promises";
import { join } from "node:path";

const docsDirectory = join(import.meta.dirname, "..", "content", "docs");
const entries = await readdir(docsDirectory);
const englishPages = entries.filter((name) => name.endsWith(".mdx") && !name.endsWith(".ko.mdx"));
const missingTranslations = englishPages.filter((name) => !entries.includes(name.replace(/\.mdx$/, ".ko.mdx")));

if (missingTranslations.length > 0) {
  throw new Error(`Missing Korean translations: ${missingTranslations.join(", ")}`);
}

const [englishSamples, koreanSamples] = await Promise.all([
  readFile(join(docsDirectory, "samples.mdx"), "utf8"),
  readFile(join(docsDirectory, "samples.ko.mdx"), "utf8"),
]);
const galleryAssets = (content) => [...content.matchAll(/\.\.\/gallery\/([^\s)]+)/g)].map((match) => match[1]).sort();

if (JSON.stringify(galleryAssets(englishSamples)) !== JSON.stringify(galleryAssets(koreanSamples))) {
  throw new Error("Korean Sample Gallery must contain the same committed platform captures as English.");
}

console.log(`Verified Korean parity for ${englishPages.length} documentation pages and sample captures.`);
