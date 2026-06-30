const {
  withAndroidManifest,
  withDangerousMod,
  AndroidConfig,
} = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

const { addPermission } = AndroidConfig.Permissions;

const PERMISSIONS = [
  'android.permission.SYSTEM_ALERT_WINDOW',
  'android.permission.FOREGROUND_SERVICE',
  'android.permission.FOREGROUND_SERVICE_SPECIAL_USE',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.RECEIVE_BOOT_COMPLETED',
];

function ensurePermissions(androidManifest) {
  if (!androidManifest.manifest['uses-permission']) {
    androidManifest.manifest['uses-permission'] = [];
  }
  const existing = new Set(
    androidManifest.manifest['uses-permission'].map((p) => p.$['android:name'])
  );
  for (const permission of PERMISSIONS) {
    if (!existing.has(permission)) {
      addPermission(androidManifest, permission);
    }
  }
}

function ensureApplicationChild(manifest, tagName) {
  const application = manifest.manifest.application?.[0];
  if (!application) {
    throw new Error('AndroidManifest is missing <application>');
  }
  if (!application[tagName]) {
    application[tagName] = [];
  }
  return application[tagName];
}

function hasService(services, name) {
  return services.some((s) => s.$?.['android:name'] === name);
}

function withUnshortServices(config) {
  return withAndroidManifest(config, (config) => {
    const manifest = config.modResults;
    ensurePermissions(manifest);

    const services = ensureApplicationChild(manifest, 'service');
    const receivers = ensureApplicationChild(manifest, 'receiver');

    const serviceEntries = [
      {
        $: {
          'android:name': 'expo.modules.unshortcore.BlockerForegroundService',
          'android:enabled': 'true',
          'android:exported': 'false',
          'android:foregroundServiceType': 'specialUse',
        },
        property: [
          {
            $: {
              'android:name': 'android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE',
              'android:value':
                'Maintains overlay and YouTube foreground detection for Shorts tab blocking',
            },
          },
        ],
      },
      {
        $: {
          'android:name': 'expo.modules.unshortcore.YouTubeDetectorAccessibilityService',
          'android:permission': 'android.permission.BIND_ACCESSIBILITY_SERVICE',
          'android:exported': 'true',
        },
        'intent-filter': [
          {
            action: [{ $: { 'android:name': 'android.accessibilityservice.AccessibilityService' } }],
          },
        ],
        'meta-data': [
          {
            $: {
              'android:name': 'android.accessibilityservice',
              'android:resource': '@xml/accessibility_service_config',
            },
          },
        ],
      },
    ];

    for (const entry of serviceEntries) {
      if (!hasService(services, entry.$['android:name'])) {
        services.push(entry);
      }
    }

    const receiverName = 'expo.modules.unshortcore.BootCompletedReceiver';
    if (!receivers.some((r) => r.$?.['android:name'] === receiverName)) {
      receivers.push({
        $: {
          'android:name': receiverName,
          'android:enabled': 'true',
          'android:exported': 'false',
        },
        'intent-filter': [
          {
            action: [{ $: { 'android:name': 'android.intent.action.BOOT_COMPLETED' } }],
          },
        ],
      });
    }

    return config;
  });
}

function withUnshortResources(config) {
  return withDangerousMod(config, [
    'android',
    async (config) => {
      const resDir = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main/res'
      );
      const xmlDir = path.join(resDir, 'xml');
      const valuesDir = path.join(resDir, 'values');
      fs.mkdirSync(xmlDir, { recursive: true });
      fs.mkdirSync(valuesDir, { recursive: true });

      fs.writeFileSync(
        path.join(xmlDir, 'accessibility_service_config.xml'),
        `<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowsChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagReportViewIds"
    android:canRetrieveWindowContent="true"
    android:notificationTimeout="100"
    android:packageNames="com.google.android.youtube"
    android:description="@string/a11y_service_description" />
`
      );

      const stringsPath = path.join(valuesDir, 'unshort_strings.xml');
      if (!fs.existsSync(stringsPath)) {
        fs.writeFileSync(
          stringsPath,
          `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="a11y_service_description">Detects when YouTube is open to position an overlay that blocks accidental taps on the Shorts tab. Does not read content from other apps.</string>
    <string name="blocker_notification_title">Unshort protection active</string>
    <string name="blocker_notification_text">Blocking accidental Shorts taps on YouTube</string>
    <string name="blocker_notification_channel">Unshort blocker</string>
</resources>
`
        );
      }

      return config;
    },
  ]);
}

module.exports = function withUnshortCore(config) {
  config = withUnshortServices(config);
  config = withUnshortResources(config);
  return config;
};
