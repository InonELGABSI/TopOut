#!/usr/bin/env bash
set -euo pipefail

AVD="${1:-Pixel_9_Pro}"
DELAY="${2:-2}"
VERBOSE="${3:-0}"          # pass 1 for set -x style

[[ $VERBOSE == 1 ]] && set -x

# 30 steps walking south‑east along Rothschild Blvd (≈ 10 m per step)
WAYPOINTS=(
  "34.782879 32.065716 22 1007 9.5"
  "34.782956 32.065640 22 1007 9.5"
  "34.783034 32.065564 22 1007 9.5"
  "34.783112 32.065488 22 1007 9.5"
  "34.783190 32.065412 22 1007 9.5"
  "34.783267 32.065336 22 1007 9.5"
  "34.783345 32.065260 22 1007 9.5"
  "34.783423 32.065184 22 1007 9.5"
  "34.783501 32.065108 22 1007 9.5"
  "34.783579 32.065032 22 1007 9.5"
  "34.783657 32.064956 22 1007 9.5"
  "34.783735 32.064880 22 1007 9.5"
  "34.783813 32.064804 22 1007 9.5"
  "34.783891 32.064728 22 1007 9.5"
  "34.783969 32.064652 22 1007 9.5"
  "34.784046 32.064576 22 1007 9.5"
  "34.784124 32.064500 22 1007 9.5"
  "34.784202 32.064424 22 1007 9.5"
  "34.784280 32.064348 22 1007 9.5"
  "34.784358 32.064272 22 1007 9.5"
  "34.784436 32.064196 22 1007 9.5"
  "34.784514 32.064120 22 1007 9.5"
  "34.784592 32.064044 22 1007 9.5"
  "34.784669 32.063968 22 1007 9.5"
  "34.784747 32.063892 22 1007 9.5"
  "34.784825 32.063816 22 1007 9.5"
  "34.784903 32.063740 22 1007 9.5"
  "34.784981 32.063664 22 1007 9.5"
  "34.785059 32.063588 22 1007 9.5"
  "34.785137 32.063512 22 1007 9.5"
)


first_emulator() { adb devices | awk '/emulator-[0-9]+/ {print $1; exit}'; }

boot_if_needed() {
  local ser=$(first_emulator || true)
  if [[ -z $ser ]]; then
    echo "Booting $AVD …"
    nohup emulator -avd "$AVD" -no-window -no-snapshot-load >/dev/null 2>&1 &
    adb wait-for-device
    ser=$(first_emulator)
  fi
  until adb -s "$ser" shell getprop sys.boot_completed | grep -q 1; do sleep 1; done
  echo "$ser"
}

SER=$(boot_if_needed)

echo "Available sensors:"
adb -s "$SER" emu sensor list || true
echo "Streaming way‑points every ${DELAY}s"

for wp in "${WAYPOINTS[@]}"; do
  read -r LON LAT ALT HPA ACCZ <<<"$wp"
  printf "→ %s,%s alt=%sm  p=%s  aZ=%s\n" "$LAT" "$LON" "$ALT" "$HPA" "$ACCZ"

  adb -s "$SER" emu geo fix "$LON" "$LAT" "$ALT"       || echo "geo fix failed"
  adb -s "$SER" emu sensor set pressure "$HPA"         || echo "pressure set failed"
  adb -s "$SER" emu sensor set acceleration 0:0:"$ACCZ"|| echo "accel set failed"

  sleep "$DELAY"
done
echo "Trip finished"
