import { redirect } from "next/navigation";

/** Static files do not resolve a directory URL to index.html in `next dev`. */
export default function ApiReferencePage() {
  redirect("/api-reference/index.html");
}
