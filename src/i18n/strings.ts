export const strings = {
  en: {
    appTitle: 'Unshort',
    appSubtitle: 'Block accidental YouTube Shorts taps',
    stepOverlayTitle: '1. Draw over other apps',
    stepOverlayBody:
      'Unshort needs permission to draw a small invisible overlay only over the YouTube Shorts tab.',
    stepOverlayAction: 'Open overlay settings',
    stepOverlayGranted: 'Overlay permission granted',
    stepA11yTitle: '2. Accessibility service',
    stepA11yBody:
      'We detect when the official YouTube app is in the foreground. We only listen to YouTube — no other apps are read.',
    stepA11yAction: 'Open accessibility settings',
    stepA11yGranted: 'Accessibility service enabled',
    stepNotifTitle: '3. Notifications',
    stepNotifBody: 'A persistent notification keeps protection active while YouTube is open.',
    stepNotifAction: 'Allow notifications',
    stepNotifGranted: 'Notifications allowed',
    activate: 'Activate protection',
    deactivate: 'Deactivate protection',
    protectionActive: 'Protection active',
    protectionInactive: 'Protection inactive',
    settings: 'Advanced settings',
    tabCount: 'Bottom tab count',
    shortsIndex: 'Shorts tab index (0-based)',
    debugOverlay: 'Show debug overlay (red)',
    calibration: 'Calibration',
    captureHeuristic: 'Capture from heuristic',
    clearCalibration: 'Clear calibration',
    exportProfile: 'Export profile JSON',
    importProfile: 'Import profile JSON',
    communityProfiles: 'Community profiles',
    applyProfile: 'Apply',
    refreshStatus: 'Refresh status',
    privacyNote:
      'Unshort does not log keystrokes or read content from apps other than YouTube.',
    oemNote:
      'Some manufacturers aggressively kill background apps. If protection stops, disable battery optimization for Unshort.',
    allPermissionsRequired: 'Grant all permissions before activating.',
  },
  pt: {
    appTitle: 'Unshort',
    appSubtitle: 'Bloqueie toques acidentais no Shorts do YouTube',
    stepOverlayTitle: '1. Sobrepor a outros apps',
    stepOverlayBody:
      'O Unshort precisa desenhar um overlay invisível apenas sobre a aba Shorts do YouTube.',
    stepOverlayAction: 'Abrir configurações de sobreposição',
    stepOverlayGranted: 'Permissão de sobreposição concedida',
    stepA11yTitle: '2. Serviço de acessibilidade',
    stepA11yBody:
      'Detectamos quando o app oficial do YouTube está em primeiro plano. Só escutamos o YouTube — nenhum outro app é lido.',
    stepA11yAction: 'Abrir configurações de acessibilidade',
    stepA11yGranted: 'Serviço de acessibilidade ativo',
    stepNotifTitle: '3. Notificações',
    stepNotifBody:
      'Uma notificação persistente mantém a proteção ativa enquanto o YouTube estiver aberto.',
    stepNotifAction: 'Permitir notificações',
    stepNotifGranted: 'Notificações permitidas',
    activate: 'Ativar proteção',
    deactivate: 'Desativar proteção',
    protectionActive: 'Proteção ativa',
    protectionInactive: 'Proteção inativa',
    settings: 'Configurações avançadas',
    tabCount: 'Quantidade de abas inferiores',
    shortsIndex: 'Índice da aba Shorts (base 0)',
    debugOverlay: 'Mostrar overlay de debug (vermelho)',
    calibration: 'Calibração',
    captureHeuristic: 'Capturar da heurística',
    clearCalibration: 'Limpar calibração',
    exportProfile: 'Exportar perfil JSON',
    importProfile: 'Importar perfil JSON',
    communityProfiles: 'Perfis da comunidade',
    applyProfile: 'Aplicar',
    refreshStatus: 'Atualizar status',
    privacyNote:
      'O Unshort não registra teclas nem lê conteúdo de apps além do YouTube.',
    oemNote:
      'Alguns fabricantes encerram apps em segundo plano. Se a proteção parar, desative a otimização de bateria para o Unshort.',
    allPermissionsRequired: 'Conceda todas as permissões antes de ativar.',
  },
} as const;

export type Locale = keyof typeof strings;

export function t(locale: Locale, key: keyof (typeof strings)['en']): string {
  return strings[locale][key];
}
