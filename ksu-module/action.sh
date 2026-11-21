#!/system/bin/sh
# Кнопка Action в KernelSU Next: переключает принудительный AOD
CUR="$(settings get system force_aod)"
if [ "$CUR" = "1" ]; then
  settings put system force_aod 0
  echo "Force AOD: ВЫКЛ (хук и watcher не вмешиваются)"
else
  settings put system force_aod 1
  settings put system aod_mode 1
  settings put system aod_tap_to_show_mode 0
  echo "Force AOD: ВКЛ"
fi
