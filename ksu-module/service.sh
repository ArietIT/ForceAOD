#!/system/bin/sh
MODDIR=${0%/*}
PKG=dev.arietit.forceaod
APK="$MODDIR/apk/ForceAOD.apk"

until [ "$(getprop sys.boot_completed)" = "1" ]; do sleep 3; done
sleep 10

# автоустановка LSPosed-модуля, если ещё не стоит
if ! pm path "$PKG" >/dev/null 2>&1 && [ -f "$APK" ]; then
  cp "$APK" /data/local/tmp/ForceAOD.apk
  chmod 644 /data/local/tmp/ForceAOD.apk
  pm install -r /data/local/tmp/ForceAOD.apk
  rm -f /data/local/tmp/ForceAOD.apk
fi

# тумблер по умолчанию = включено (его же читает хук)
[ "$(settings get system force_aod)" = "null" ] && settings put system force_aod 1

apply() {
  settings put system aod_mode 1
  settings put system aod_tap_to_show_mode 0
}

# если что-то (режим энергосбережения, апдейт и т.д.) выключило AOD — вернуть
while true; do
  if [ "$(settings get system force_aod)" = "1" ]; then
    [ "$(settings get system aod_mode)" != "1" ] && apply
  fi
  sleep 30
done &
