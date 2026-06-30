export interface OverlayBounds {
  left: number;
  top: number;
  right: number;
  bottom: number;
  width: number;
  height: number;
}

export interface BlockerStatus {
  enabled: boolean;
  overlayGranted: boolean;
  accessibilityEnabled: boolean;
  serviceRunning: boolean;
  tabCount: number;
  shortsTabIndex: number;
  debugOverlayVisible: boolean;
  calibrationLeftPct: number | null;
  calibrationTopPct: number | null;
  calibrationWidthPct: number | null;
  calibrationHeightPct: number | null;
}

export interface OverlayConfigInput {
  tabCount?: number;
  shortsTabIndex?: number;
  debugOverlayVisible?: boolean;
  calibrationLeftPct?: number | null;
  calibrationTopPct?: number | null;
  calibrationWidthPct?: number | null;
  calibrationHeightPct?: number | null;
}

export interface CalibrationProfile {
  name: string;
  deviceModel: string;
  tabCount: number;
  shortsTabIndex: number;
  leftPct: number;
  topPct: number;
  widthPct: number;
  heightPct: number;
}

export type BlockerStatusChangedEvent = BlockerStatus;
