import { NativeModule, requireNativeModule } from 'expo';
import type {
  BlockerStatus,
  BlockerStatusChangedEvent,
  CalibrationProfile,
  OverlayBounds,
  OverlayConfigInput,
} from './UnshortCore.types';

declare class UnshortCoreModule extends NativeModule<{
  blockerStatusChanged: (event: BlockerStatusChangedEvent) => void;
}> {
  canDrawOverlays(): Promise<boolean>;
  openOverlaySettings(): void;
  isAccessibilityEnabled(): Promise<boolean>;
  openAccessibilitySettings(): void;
  startBlocker(): Promise<void>;
  stopBlocker(): Promise<void>;
  getBlockerStatus(): Promise<BlockerStatus>;
  setOverlayConfig(config: OverlayConfigInput): Promise<BlockerStatus>;
  clearCalibration(): Promise<BlockerStatus>;
  captureHeuristicCalibration(): Promise<CalibrationProfile>;
  applyCalibrationProfile(profile: CalibrationProfile): Promise<CalibrationProfile>;
  exportCalibrationProfile(): Promise<CalibrationProfile>;
  getOverlayBounds(): Promise<OverlayBounds | null>;
}

export default requireNativeModule<UnshortCoreModule>('UnshortCore');
