import type { HeatmapNode } from "@taewooyo/heatmap-react";

/** Mirrors the Desktop/iOS market fixture and deterministic stress-feed formulas. */
export const marketMap: HeatmapNode = {
  id: "market", label: "Market", value: 1,
  children: [
    {
      id: "technology", label: "Electronic Technology", value: 1,
      children: [
        { id: "samsung", label: "Samsung", value: 48_000, metric: 2.3, imageUrl: logoUrl("Samsung") },
        { id: "sk-hynix", label: "SK Hynix", value: 31_000, metric: 1.4, imageUrl: logoUrl("SKHynix") },
        { id: "hanmi", label: "Hanmi Semi", value: 8_000, metric: -2.4, imageUrl: logoUrl("Hanmi") },
      ],
    },
    {
      id: "finance", label: "Finance", value: 1,
      children: [
        { id: "kb", label: "KB Financial", value: 44_000, metric: 2.6, imageUrl: logoUrl("KB") },
        { id: "shinhan", label: "Shinhan", value: 36_000, metric: 1.2, imageUrl: logoUrl("Shinhan") },
        { id: "hana", label: "Hana", value: 12_000, metric: -0.8, imageUrl: logoUrl("Hana") },
      ],
    },
    {
      id: "healthcare", label: "Health Technology", value: 1,
      children: [
        { id: "samsung-bio", label: "Samsung Biologics", value: 26_000, metric: 0.2, imageUrl: logoUrl("SamsungBio") },
        { id: "celltrion", label: "Celltrion", value: 19_000, metric: -1.1, imageUrl: logoUrl("Celltrion") },
        { id: "yuhan", label: "Yuhan", value: 15_000, metric: 1.6, imageUrl: logoUrl("Yuhan") },
      ],
    },
  ],
};

function logoUrl(label: string): string {
  return `https://ui-avatars.com/api/?name=${label}&background=0F172A&color=FFFFFF&bold=true&size=128`;
}

export type DemoDataMode = "normal" | "overview5k" | "raw5k";

export function withDemoMetrics(root: HeatmapNode, tick: number): HeatmapNode {
  let index = 0;
  function update(node: HeatmapNode): HeatmapNode {
    const children = node.children?.map(update);
    const metric = node.metric === undefined
      ? undefined
      : node.metric + Math.sin(tick * 0.8 + index++ * 1.7) * 5;
    return { ...node, children, metric };
  }
  return update(root);
}

export function expandForHeatmapStressTest(root: HeatmapNode): HeatmapNode {
  const sectors = root.children ?? [];
  const totalLeaves = sectors.reduce((sum, sector) => sum + (sector.children?.length ?? 0), 0);
  if (totalLeaves === 0) return root;
  let assignedLeaves = 0;
  const children = sectors.map((sector, sectorIndex) => {
    const sources = sector.children ?? [];
    const count = sectorIndex === sectors.length - 1
      ? 5_000 - assignedLeaves
      : Math.trunc(5_000 * sources.length / totalLeaves);
    if (sectorIndex !== sectors.length - 1) assignedLeaves += count;
    return {
      ...sector,
      children: Array.from({ length: count }, (_, index): HeatmapNode => {
        const source = sources[index % sources.length];
        return {
          ...source,
          id: `${source.id}-${sector.id}-${index}`,
          label: `${source.label}-${index + 1}`,
        };
      }),
    };
  });
  return { ...root, children };
}

function layoutValue(node: HeatmapNode): number {
  const childrenValue = (node.children ?? []).reduce((sum, child) => sum + layoutValue(child), 0);
  return childrenValue > 0 ? childrenValue : Math.max(node.value, 0);
}

/** Demo-only equivalent of the existing maximumChildren=12 overview transformation. */
export function toOverview(root: HeatmapNode): HeatmapNode {
  function transform(node: HeatmapNode): HeatmapNode {
    const children = node.children?.map(transform) ?? [];
    if (children.length === 0) return node;
    const ranked = [...children].sort((a, b) => layoutValue(b) - layoutValue(a));
    const retainedIds = new Set(ranked.slice(0, 12).map((child) => child.id));
    const retained = children.filter((child) => retainedIds.has(child.id));
    const omitted = children.filter((child) => !retainedIds.has(child.id));
    if (omitted.length > 0) {
      const weight = omitted.reduce((sum, child) => sum + layoutValue(child), 0);
      const measured = omitted.filter((child) => child.metric !== undefined);
      const metricWeight = measured.reduce((sum, child) => sum + layoutValue(child), 0);
      retained.push({
        id: `${children[0].id.split("::")[0]}::others`,
        label: "Others",
        value: weight,
        metric: metricWeight > 0
          ? measured.reduce((sum, child) => sum + layoutValue(child) * child.metric!, 0) / metricWeight
          : undefined,
      });
    }
    return { ...node, children: retained.sort((a, b) => layoutValue(b) - layoutValue(a)) };
  }
  return transform(root);
}
