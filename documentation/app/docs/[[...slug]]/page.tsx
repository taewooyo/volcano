import { source } from "@/lib/source";
import { DocsBody, DocsDescription, DocsPage, DocsTitle } from "fumadocs-ui/page";
import defaultMdxComponents from "fumadocs-ui/mdx";
import { notFound } from "next/navigation";
import type { ComponentProps, FC } from "react";

interface MDXPageData {
  body: FC<ComponentProps<"div"> & { components?: Record<string, unknown> }>;
  toc: { depth: number; url: string; title: string }[];
  title: string;
  description?: string;
}

export default async function Page(props: { params: Promise<{ slug?: string[] }> }) {
  const page = source.getPage((await props.params).slug, "en");
  if (!page) notFound();
  const data = page.data as unknown as MDXPageData;
  const MDX = data.body;
  return <DocsPage toc={data.toc}><DocsTitle>{data.title}</DocsTitle><DocsDescription>{data.description}</DocsDescription><DocsBody><MDX components={defaultMdxComponents} /></DocsBody></DocsPage>;
}

export function generateStaticParams() { return source.generateParams("slug", "locale").filter((params) => params.locale === "en"); }
