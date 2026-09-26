import { useCallback, useEffect, useMemo, useState } from "react";
import type { HeatmapNode } from "./types";

/** React state holder for drill-down navigation and leaf selection. */
export interface HeatmapState {
  readonly visibleNode: HeatmapNode;
  readonly breadcrumbs: readonly HeatmapNode[];
  readonly selectedNode: HeatmapNode | null;
  readonly selectedId: string | null;
  readonly canNavigateUp: boolean;
  readonly drillDown: (nodeId: string) => boolean;
  readonly navigateUp: () => boolean;
  readonly navigateTo: (nodeId: string) => boolean;
  readonly navigateToBreadcrumb: (index: number) => boolean;
  readonly reset: () => void;
  readonly select: (node: HeatmapNode) => void;
  readonly isSelected: (node: HeatmapNode) => boolean;
  readonly clearSelection: () => void;
}

/** Creates state with the same navigation and selection operations as Compose [HeatmapState]. */
export function useHeatmapState(root: HeatmapNode): HeatmapState {
  const [path, setPath] = useState<readonly string[]>([]);
  const [selectedNode, setSelectedNode] = useState<HeatmapNode | null>(null);
  useEffect(() => {
    setPath([]);
    setSelectedNode(null);
  }, [root]);
  const breadcrumbs = useMemo(() => {
    const nodes = [root];
    let current = root;
    for (const id of path) {
      const child = current.children?.find((node) => node.id === id);
      if (!child) break;
      nodes.push(child);
      current = child;
    }
    return nodes;
  }, [root, path]);
  const visibleNode = breadcrumbs[breadcrumbs.length - 1];
  const drillDown = useCallback((nodeId: string) => {
    const target = visibleNode.children?.find((node) => node.id === nodeId);
    if (!target?.children?.length) return false;
    setPath((current) => [...current, nodeId]);
    return true;
  }, [visibleNode]);
  const navigateUp = useCallback(() => {
    if (path.length === 0) return false;
    setPath((current) => current.slice(0, -1));
    return true;
  }, [path.length]);
  const navigateToBreadcrumb = useCallback((index: number) => {
    if (index < 0 || index >= breadcrumbs.length - 1) return false;
    setPath(path.slice(0, index));
    return true;
  }, [breadcrumbs.length, path]);
  const navigateTo = useCallback((nodeId: string) => {
    const index = breadcrumbs.findIndex((node) => node.id === nodeId);
    return index >= 0 && navigateToBreadcrumb(index);
  }, [breadcrumbs, navigateToBreadcrumb]);
  const select = useCallback((node: HeatmapNode) => {
    if (!node.children?.length) setSelectedNode(node);
  }, []);
  const clearSelection = useCallback(() => setSelectedNode(null), []);

  return {
    visibleNode,
    breadcrumbs,
    selectedNode,
    selectedId: selectedNode?.id ?? null,
    canNavigateUp: path.length > 0,
    drillDown,
    navigateUp,
    navigateTo,
    navigateToBreadcrumb,
    reset: () => setPath([]),
    select,
    isSelected: (node) => selectedNode === node,
    clearSelection,
  };
}
