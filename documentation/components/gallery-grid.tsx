import type { ReactNode } from "react";

export function GalleryGrid({ children }: { children: ReactNode }) {
  return <div className="gallery-grid">{children}</div>;
}
