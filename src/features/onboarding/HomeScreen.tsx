import { useCallback, useEffect, useState } from 'react';
import {
  AppState,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from 'react-native';
import * as Notifications from 'expo-notifications';
import { UnshortNative, type BlockerStatus } from '../../native/UnshortCore';
import { t, type Locale } from '../../i18n/strings';
import defaultProfiles from '../../../community-profiles/default-profiles.json';
import type { CalibrationProfile } from '../../../modules/unshort-core/src/UnshortCore.types';

interface Props {
  locale: Locale;
  showSettings: boolean;
  onToggleSettings: () => void;
}

async function ensureNotificationPermission(): Promise<boolean> {
  if (Platform.OS !== 'android') return true;
  const { status: existing } = await Notifications.getPermissionsAsync();
  if (existing === 'granted') return true;
  const { status } = await Notifications.requestPermissionsAsync();
  return status === 'granted';
}

export function HomeScreen({ locale, showSettings, onToggleSettings }: Props) {
  const [status, setStatus] = useState<BlockerStatus | null>(null);
  const [notifGranted, setNotifGranted] = useState(false);
  const [tabCount, setTabCount] = useState('5');
  const [shortsIndex, setShortsIndex] = useState('1');
  const [debugOverlay, setDebugOverlay] = useState(false);
  const [importJson, setImportJson] = useState('');
  const [exportedJson, setExportedJson] = useState('');

  const refresh = useCallback(async () => {
    const [blockerStatus, notifications] = await Promise.all([
      UnshortNative.getBlockerStatus(),
      Notifications.getPermissionsAsync(),
    ]);
    setStatus(blockerStatus);
    setTabCount(String(blockerStatus.tabCount));
    setShortsIndex(String(blockerStatus.shortsTabIndex));
    setDebugOverlay(blockerStatus.debugOverlayVisible);
    setNotifGranted(notifications.status === 'granted');
  }, []);

  useEffect(() => {
    refresh();
    const sub = AppState.addEventListener('change', (state) => {
      if (state === 'active') refresh();
    });
    const listener = UnshortNative.addStatusListener((next) => setStatus(next));
    return () => {
      sub.remove();
      listener.remove();
    };
  }, [refresh]);

  const allGranted =
    status?.overlayGranted && status?.accessibilityEnabled && notifGranted;

  const handleActivate = async () => {
    if (!allGranted) return;
    await UnshortNative.startBlocker();
    await refresh();
  };

  const handleDeactivate = async () => {
    await UnshortNative.stopBlocker();
    await refresh();
  };

  const handleSaveSettings = async () => {
    await UnshortNative.setOverlayConfig({
      tabCount: Number(tabCount) || 5,
      shortsTabIndex: Number(shortsIndex) || 1,
      debugOverlayVisible: debugOverlay,
    });
    await refresh();
  };

  const handleExport = async () => {
    const profile = await UnshortNative.exportCalibrationProfile();
    setExportedJson(JSON.stringify(profile, null, 2));
  };

  const handleImport = async () => {
    const profile = JSON.parse(importJson) as CalibrationProfile;
    await UnshortNative.applyCalibrationProfile(profile);
    await refresh();
  };

  if (!status) {
    return (
      <View style={styles.centered}>
        <Text>Loading…</Text>
      </View>
    );
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>{t(locale, 'appTitle')}</Text>
      <Text style={styles.subtitle}>{t(locale, 'appSubtitle')}</Text>

      <StatusCard
        title={t(locale, 'stepOverlayTitle')}
        body={t(locale, 'stepOverlayBody')}
        granted={status.overlayGranted}
        grantedLabel={t(locale, 'stepOverlayGranted')}
        actionLabel={t(locale, 'stepOverlayAction')}
        onAction={() => UnshortNative.openOverlaySettings()}
      />

      <StatusCard
        title={t(locale, 'stepA11yTitle')}
        body={t(locale, 'stepA11yBody')}
        granted={status.accessibilityEnabled}
        grantedLabel={t(locale, 'stepA11yGranted')}
        actionLabel={t(locale, 'stepA11yAction')}
        onAction={() => UnshortNative.openAccessibilitySettings()}
      />

      <StatusCard
        title={t(locale, 'stepNotifTitle')}
        body={t(locale, 'stepNotifBody')}
        granted={notifGranted}
        grantedLabel={t(locale, 'stepNotifGranted')}
        actionLabel={t(locale, 'stepNotifAction')}
        onAction={() => ensureNotificationPermission().then(refresh)}
      />

      <Text style={styles.note}>{t(locale, 'privacyNote')}</Text>
      <Text style={styles.note}>{t(locale, 'oemNote')}</Text>

      <View style={styles.row}>
        <Text style={styles.statusBadge}>
          {status.enabled ? t(locale, 'protectionActive') : t(locale, 'protectionInactive')}
        </Text>
        <Pressable style={styles.secondaryButton} onPress={refresh}>
          <Text style={styles.secondaryButtonText}>{t(locale, 'refreshStatus')}</Text>
        </Pressable>
      </View>

      {status.enabled ? (
        <Pressable style={[styles.button, styles.deactivate]} onPress={handleDeactivate}>
          <Text style={styles.buttonText}>{t(locale, 'deactivate')}</Text>
        </Pressable>
      ) : (
        <Pressable
          style={[styles.button, !allGranted && styles.buttonDisabled]}
          onPress={handleActivate}
          disabled={!allGranted}
        >
          <Text style={styles.buttonText}>
            {allGranted ? t(locale, 'activate') : t(locale, 'allPermissionsRequired')}
          </Text>
        </Pressable>
      )}

      <Pressable style={styles.linkButton} onPress={onToggleSettings}>
        <Text style={styles.linkText}>{t(locale, 'settings')}</Text>
      </Pressable>

      {showSettings && (
        <View style={styles.settingsBox}>
          <Text style={styles.sectionTitle}>{t(locale, 'settings')}</Text>
          <LabeledInput label={t(locale, 'tabCount')} value={tabCount} onChangeText={setTabCount} />
          <LabeledInput
            label={t(locale, 'shortsIndex')}
            value={shortsIndex}
            onChangeText={setShortsIndex}
          />
          <View style={styles.switchRow}>
            <Text>{t(locale, 'debugOverlay')}</Text>
            <Switch value={debugOverlay} onValueChange={setDebugOverlay} />
          </View>
          <Pressable style={styles.secondaryButton} onPress={handleSaveSettings}>
            <Text style={styles.secondaryButtonText}>Save</Text>
          </Pressable>

          <Text style={styles.sectionTitle}>{t(locale, 'calibration')}</Text>
          <Pressable
            style={styles.secondaryButton}
            onPress={() => UnshortNative.captureHeuristicCalibration().then(refresh)}
          >
            <Text style={styles.secondaryButtonText}>{t(locale, 'captureHeuristic')}</Text>
          </Pressable>
          <Pressable style={styles.secondaryButton} onPress={() => UnshortNative.clearCalibration().then(refresh)}>
            <Text style={styles.secondaryButtonText}>{t(locale, 'clearCalibration')}</Text>
          </Pressable>
          <Pressable style={styles.secondaryButton} onPress={handleExport}>
            <Text style={styles.secondaryButtonText}>{t(locale, 'exportProfile')}</Text>
          </Pressable>
          {exportedJson ? <Text style={styles.jsonPreview}>{exportedJson}</Text> : null}

          <Text style={styles.sectionTitle}>{t(locale, 'importProfile')}</Text>
          <TextInput
            style={styles.textArea}
            multiline
            value={importJson}
            onChangeText={setImportJson}
            placeholder='{"leftPct":0.2,"topPct":0.92,...}'
          />
          <Pressable style={styles.secondaryButton} onPress={handleImport}>
            <Text style={styles.secondaryButtonText}>{t(locale, 'importProfile')}</Text>
          </Pressable>

          <Text style={styles.sectionTitle}>{t(locale, 'communityProfiles')}</Text>
          {(defaultProfiles as CalibrationProfile[]).map((profile) => (
            <Pressable
              key={profile.name}
              style={styles.profileCard}
              onPress={() => UnshortNative.applyCalibrationProfile(profile).then(refresh)}
            >
              <Text style={styles.profileName}>{profile.name}</Text>
              <Text style={styles.profileMeta}>{profile.deviceModel}</Text>
              <Text style={styles.profileAction}>{t(locale, 'applyProfile')}</Text>
            </Pressable>
          ))}
        </View>
      )}
    </ScrollView>
  );
}

function StatusCard({
  title,
  body,
  granted,
  grantedLabel,
  actionLabel,
  onAction,
}: {
  title: string;
  body: string;
  granted: boolean;
  grantedLabel: string;
  actionLabel: string;
  onAction: () => void;
}) {
  return (
    <View style={styles.card}>
      <Text style={styles.cardTitle}>{title}</Text>
      <Text style={styles.cardBody}>{body}</Text>
      {granted ? (
        <Text style={styles.granted}>{grantedLabel}</Text>
      ) : (
        <Pressable style={styles.secondaryButton} onPress={onAction}>
          <Text style={styles.secondaryButtonText}>{actionLabel}</Text>
        </Pressable>
      )}
    </View>
  );
}

function LabeledInput({
  label,
  value,
  onChangeText,
}: {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
}) {
  return (
    <View style={styles.inputRow}>
      <Text style={styles.inputLabel}>{label}</Text>
      <TextInput style={styles.input} value={value} onChangeText={onChangeText} keyboardType="number-pad" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingBottom: 48, backgroundColor: '#0f172a' },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#0f172a' },
  title: { fontSize: 32, fontWeight: '700', color: '#f8fafc', marginBottom: 4 },
  subtitle: { fontSize: 16, color: '#94a3b8', marginBottom: 20 },
  card: {
    backgroundColor: '#1e293b',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  cardTitle: { color: '#e2e8f0', fontSize: 16, fontWeight: '600', marginBottom: 6 },
  cardBody: { color: '#94a3b8', marginBottom: 10, lineHeight: 20 },
  granted: { color: '#4ade80', fontWeight: '600' },
  note: { color: '#64748b', fontSize: 13, marginVertical: 6, lineHeight: 18 },
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginVertical: 12 },
  statusBadge: { color: '#f8fafc', fontWeight: '600' },
  button: {
    backgroundColor: '#2563eb',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: { backgroundColor: '#334155' },
  deactivate: { backgroundColor: '#b91c1c' },
  buttonText: { color: '#fff', fontWeight: '700' },
  secondaryButton: {
    backgroundColor: '#334155',
    borderRadius: 8,
    paddingVertical: 10,
    paddingHorizontal: 12,
    alignSelf: 'flex-start',
    marginTop: 8,
  },
  secondaryButtonText: { color: '#e2e8f0', fontWeight: '600' },
  linkButton: { marginTop: 16, alignItems: 'center' },
  linkText: { color: '#60a5fa', fontWeight: '600' },
  settingsBox: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#334155' },
  sectionTitle: { color: '#f1f5f9', fontSize: 18, fontWeight: '700', marginTop: 16, marginBottom: 8 },
  switchRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginVertical: 8 },
  inputRow: { marginBottom: 8 },
  inputLabel: { color: '#94a3b8', marginBottom: 4 },
  input: {
    backgroundColor: '#1e293b',
    color: '#f8fafc',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  textArea: {
    backgroundColor: '#1e293b',
    color: '#f8fafc',
    borderRadius: 8,
    minHeight: 100,
    padding: 12,
    textAlignVertical: 'top',
  },
  jsonPreview: { color: '#cbd5e1', fontSize: 12, marginTop: 8, fontFamily: 'monospace' },
  profileCard: {
    backgroundColor: '#1e293b',
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
  },
  profileName: { color: '#f8fafc', fontWeight: '600' },
  profileMeta: { color: '#94a3b8', fontSize: 12 },
  profileAction: { color: '#60a5fa', marginTop: 6, fontWeight: '600' },
});
