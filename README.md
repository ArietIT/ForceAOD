# Force AOD (Samsung One UI 9)

Две части:
- `xposed-module/` — LSPosed-модуль (хуки в SystemUI и com.samsung.android.app.aodservice)
- `ksu-module/` — модуль KernelSU Next (ставит APK, ставит настройки, кнопка Action = вкл/выкл)

## Сборка
1. Открой `xposed-module/` в Android Studio → Build APK (debug), или `gradle assembleDebug`.
2. Скопируй `app/build/outputs/apk/debug/app-debug.apk` в `ksu-module/apk/ForceAOD.apk`.
3. `cd ksu-module && zip -r ../ForceAOD-ksu.zip .`

## Установка
1. KernelSU Next → Модули → установить ForceAOD-ksu.zip → ребут.
2. LSPosed → Модули → ForceAOD → включить, scope: SystemUI + AOD service. Перезагрузка.
3. Кнопка Action у модуля в KSU Next — вкл/выкл принудительный режим.

## Отладка
В `Hook.java` поставь `PROBE = true`, пересобери, смотри `adb logcat -s ForceAOD`, пока экран гаснет:
увидишь, какие ключи Settings.System читает aodservice, и добавь нужные в `FORCE`.
