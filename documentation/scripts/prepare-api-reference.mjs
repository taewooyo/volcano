import { cp, mkdir, rm } from "node:fs/promises";
import { resolve } from "node:path";
import { promisify } from "node:util";
import { execFile } from "node:child_process";

const run = promisify(execFile);
const repository = resolve(import.meta.dirname, "../..");
const source = resolve(repository, "build/dokka/html");
const destination = resolve(import.meta.dirname, "../public/api-reference");

await run(resolve(repository, "gradlew"), [":dokkaGeneratePublicationHtml", "--no-daemon", "--console=plain"], { cwd: repository });
await rm(destination, { recursive: true, force: true });
await mkdir(destination, { recursive: true });
await cp(source, destination, { recursive: true });
console.log("Prepared Dokka API reference at /api-reference/.");
