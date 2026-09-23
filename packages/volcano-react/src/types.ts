/** A heatmap node. Sibling IDs must be unique; IDs may repeat in different groups. */
export interface HeatmapNode {
  readonly id: string;
  readonly label: string;
  readonly value: number;
  readonly metric?: number;
  /** Optional consumer-owned image address. The core never fetches it. */
  readonly imageUrl?: string;
  /** CSS hex color: #RRGGBB or #RRGGBBAA. */
  readonly color?: string;
  readonly children?: readonly HeatmapNode[];
}

export interface HeatmapLayoutOptions {
  readonly width: number;
  readonly height: number;
  readonly groupHeaderHeight?: number;
  readonly maximumAbsoluteMetric?: number;
  readonly palette?: {
    readonly negative: string;
    readonly neutral: string;
    readonly positive: string;
  };
}

export interface HeatmapLayoutCell {
  readonly node: HeatmapNode;
  readonly key: string;
  readonly parentIndex: number;
  readonly depth: number;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  readonly color: string;
  readonly isLeaf: boolean;
  readonly visible: boolean;
}
