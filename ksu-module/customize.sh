#!/system/bin/sh
SKIPUNZIP=0

ui_print "- Force AOD for Samsung One UI"
MAN="$(getprop ro.product.manufacturer)"
[ "$MAN" != "samsung" ] && [ "$MAN" != "Samsung" ] && ui_print "! Устройство не Samsung ($MAN), AOD может не заработать"

if [ -f "$MODPATH/apk/ForceAOD.apk" ]; then
  ui_print "- APK найден, установится при загрузке"
else
  ui_print "! Нет apk/ForceAOD.apk — собери xposed-module и положи в apk/"
fi
ui_print "- После перезагрузки включи ForceAOD в LSPosed (scope: SystemUI + AOD service)"

set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/action.sh" 0 0 0755
set_perm "$MODPATH/uninstall.sh" 0 0 0755
