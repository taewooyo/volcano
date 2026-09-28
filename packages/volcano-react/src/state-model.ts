import type { HeatmapNode } from "./types";

/** Internal pure state operations shared by the hook and its regression checks. */
export interface NavigationSnapshot {
  readonly root: HeatmapNode;
  readonly path: readonly string[];
  readonly selectedPath: readonly string[] | null;
  readonly selectedNode: HeatmapNode | null;
}

export function immutablePath(path: readonly string[]): readonly string[] {
  return Object.freeze([...path]);
}

export function resolveExact(root: HeatmapNode, path: readonly string[]): HeatmapNode | null {
  let node = root;
  for (const id of path) {
    const child = node.children?.find((candidate) => candidate.id === id);
    if (!child) return null;
    node = child;
  }
  return node;
}

export function findPath(root: HeatmapNode, target: HeatmapNode): readonly string[] | null {
  if (root === target) return immutablePath([]);
  const stack = [{ node: root, nextChild: 0, depth: 0 }];
  const path: string[] = [];
  while (stack.length > 0) {
    const frame = stack[stack.length - 1];
    const children = frame.node.children ?? [];
    if (frame.nextChild >= children.length) {
      stack.pop();
      path.length = Math.max(0, frame.depth - 1);
      continue;
    }
    const child = children[frame.nextChild++];
    path.length = frame.depth;
    path.push(child.id);
    if (child === target) return immutablePath(path);
    if (child.children?.length) {
      stack.push({ node: child, nextChild: 0, depth: path.length });
    } else {
      path.length = frame.depth;
    }
  }
  return null;
}

export function reconcileRoot(snapshot: NavigationSnapshot, root: HeatmapNode): NavigationSnapshot {
  if (snapshot.root === root) return snapshot;
  if (snapshot.root.id !== root.id) {
    return { root, path: immutablePath([]), selectedPath: null, selectedNode: null };
  }
  let current = root;
  const path: string[] = [];
  for (const id of snapshot.path) {
    const child = current.children?.find((node) => node.id === id && node.children?.length);
    if (!child) break;
    path.push(id);
    current = child;
  }
  const selected = snapshot.selectedPath === null ? null : resolveExact(root, snapshot.selectedPath);
  const selectedNode = selected && !selected.children?.length ? selected : null;
  return {
    root,
    path: immutablePath(path),
    selectedPath: selectedNode && snapshot.selectedPath ? immutablePath(snapshot.selectedPath) : null,
    selectedNode,
  };
}
