import { defineDocs } from "fumadocs-mdx/macro";
import { loader } from "fumadocs-core/source";
import { i18n } from "@/lib/i18n";

const docs = defineDocs({ dir: "content/docs" });

export const source = loader({
  baseUrl: "/docs",
  i18n,
  source: docs.toFumadocsSource(),
});
