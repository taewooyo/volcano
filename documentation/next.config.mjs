import { createMDX } from "fumadocs-mdx/next";

const withMDX = createMDX();

export default withMDX({
  output: "export",
  basePath: process.env.NEXT_PUBLIC_BASE_PATH ?? "",
  images: { unoptimized: true },
  reactStrictMode: true,
});
