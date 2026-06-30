import { useState } from 'react';
import { Pressable, SafeAreaView, StatusBar, StyleSheet, Text, View } from 'react-native';
import { HomeScreen } from './src/features/onboarding/HomeScreen';
import type { Locale } from './src/i18n/strings';

export default function App() {
  const [locale, setLocale] = useState<Locale>('pt');
  const [showSettings, setShowSettings] = useState(false);

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle="light-content" />
      <View style={styles.localeRow}>
        <Pressable onPress={() => setLocale('pt')} style={[styles.localeBtn, locale === 'pt' && styles.localeActive]}>
          <Text style={styles.localeText}>PT</Text>
        </Pressable>
        <Pressable onPress={() => setLocale('en')} style={[styles.localeBtn, locale === 'en' && styles.localeActive]}>
          <Text style={styles.localeText}>EN</Text>
        </Pressable>
      </View>
      <HomeScreen
        locale={locale}
        showSettings={showSettings}
        onToggleSettings={() => setShowSettings((v) => !v)}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#0f172a' },
  localeRow: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, paddingHorizontal: 16, paddingTop: 8 },
  localeBtn: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 6, backgroundColor: '#1e293b' },
  localeActive: { backgroundColor: '#2563eb' },
  localeText: { color: '#f8fafc', fontWeight: '700' },
});
