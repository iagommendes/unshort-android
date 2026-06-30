import UnshortCore from '../../modules/unshort-core/src/UnshortCoreModule';
import type {
  BlockerStatus,
  CalibrationProfile,
  OverlayConfigInput,
} from '../../modules/unshort-core/src/UnshortCore.types';

export type { BlockerStatus, CalibrationProfile, OverlayConfigInput };

export const UnshortNative = {
  canDrawOverlays: () => UnshortCore.canDrawOverlays(),
  openOverlaySettings: () => UnshortCore.openOverlaySettings(),
  isAccessibilityEnabled: () => UnshortCore.isAccessibilityEnabled(),
  openAccessibilitySettings: () => UnshortCore.openAccessibilitySettings(),
  startBlocker: () => UnshortCore.startBlocker(),
  stopBlocker: () => UnshortCore.stopBlocker(),
  getBlockerStatus: () => UnshortCore.getBlockerStatus(),
  setOverlayConfig: (config: OverlayConfigInput) => UnshortCore.setOverlayConfig(config),
  clearCalibration: () => UnshortCore.clearCalibration(),
  captureHeuristicCalibration: () => UnshortCore.captureHeuristicCalibration(),
  applyCalibrationProfile: (profile: CalibrationProfile) =>
    UnshortCore.applyCalibrationProfile(profile),
  exportCalibrationProfile: () => UnshortCore.exportCalibrationProfile(),
  getOverlayBounds: () => UnshortCore.getOverlayBounds(),
  addStatusListener: (listener: (status: BlockerStatus) => void) =>
    UnshortCore.addListener('blockerStatusChanged', listener),
};
