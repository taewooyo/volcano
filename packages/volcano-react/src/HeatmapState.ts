import { useCallback, useMemo, useState } from "react";
import type { HeatmapNode } from "./types";
import { findPath, immutablePath, reconcileRoot, resolveExact } from "./state-model";
import type { NavigationSnapshot } from "./state-model";

/** Navigation and selection survive immutable updates with the same root ID. */
export interface HeatmapState {
  readonly visibleNode: HeatmapNode;
  readonly breadcrumbs: readonly HeatmapNode[];
  readonly navigationPath: readonly string[];
  readonly selectionPath: readonly string[] | null;
  readonly selectedNode: HeatmapNode | null;
  readonly selectedId: string | null;
  readonly canNavigateUp: boolean;
  readonly drillDown: (node: string | HeatmapNode) => boolean;
  readonly navigateToPath: (path: readonly string[]) => boolean;
  readonly navigateUp: () => boolean;
  readonly navigateTo: (nodeId: string) => boolean;
  readonly navigateToBreadcrumb: (index: number) => boolean;
  readonly reset: () => void;
  readonly select: (node: HeatmapNode) => void;
  readonly isSelected: (node: HeatmapNode) => boolean;
  readonly clearSelection: () => void;
}

export function useHeatmapState(root: HeatmapNode): HeatmapState {
  const [stored, setSnapshot] = useState<NavigationSnapshot>(() => ({ root, path: immutablePath([]), selectedPath: null, selectedNode: null }));
  const snapshot = reconcileRoot(stored, root);
  // Only persist a changed route or removed selection. A freshly allocated but equivalent
  // root must not trigger a render loop. Current selected objects are derived below.
  if (snapshot.root.id !== stored.root.id ||
      snapshot.path.length !== stored.path.length ||
      snapshot.path.some((id, index) => id !== stored.path[index]) ||
      (stored.selectedPath !== null && snapshot.selectedPath === null)) setSnapshot(snapshot);
  const { path, selectedNode } = snapshot;
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
  const navigateToPath = useCallback((nextPath: readonly string[]) => {
    const target = resolveExact(root, nextPath);
    if (!target || (nextPath.length > 0 && !target.children?.length)) return false;
    if (path.length === nextPath.length && path.every((id, index) => id === nextPath[index])) return false;
    setSnapshot((current) => ({ ...reconcileRoot(current, root), path: immutablePath(nextPath) }));
    return true;
  }, [root, path]);
  const drillDown = useCallback((node: string | HeatmapNode) => {
    if (typeof node !== "string") {
      if (!node.children?.length) return false;
      const targetPath = findPath(root, node);
      return targetPath !== null && navigateToPath(targetPath);
    }
    const target = visibleNode.children?.find((child) => child.id === node);
    return Boolean(target?.children?.length) && navigateToPath([...path, node]);
  }, [root, visibleNode, path, navigateToPath]);
  const navigateUp = useCallback(() => path.length > 0 && navigateToPath(path.slice(0, -1)), [path, navigateToPath]);
  const navigateToBreadcrumb = useCallback((index: number) => {
    if (!Number.isInteger(index) || index < 0 || index >= breadcrumbs.length - 1) return false;
    return navigateToPath(path.slice(0, index));
  }, [breadcrumbs.length, path, navigateToPath]);
  const navigateTo = useCallback((nodeId: string) => {
    const index = breadcrumbs.findIndex((node) => node.id === nodeId);
    return index >= 0 && navigateToBreadcrumb(index);
  }, [breadcrumbs, navigateToBreadcrumb]);
  const select = useCallback((node: HeatmapNode) => {
    const selectedPath = findPath(root, node);
    if (!node.children?.length && selectedPath !== null) {
      setSnapshot((current) => ({ ...reconcileRoot(current, root), selectedNode: node, selectedPath }));
    }
  }, [root]);
  const clearSelection = useCallback(() => setSnapshot((current) => ({ ...current, selectedNode: null, selectedPath: null })), []);

  return {
    visibleNode,
    breadcrumbs,
    navigationPath: path,
    selectionPath: snapshot.selectedPath,
    selectedNode,
    selectedId: selectedNode?.id ?? null,
    canNavigateUp: path.length > 0,
    drillDown,
    navigateToPath,
    navigateUp,
    navigateTo,
    navigateToBreadcrumb,
    reset: () => { navigateToPath([]); },
    select,
    isSelected: (node) => selectedNode === node,
    clearSelection,
  };
}
