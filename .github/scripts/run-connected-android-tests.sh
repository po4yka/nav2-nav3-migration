#!/usr/bin/env sh
set -eu

sdk=""
i=1
while [ "$i" -le 30 ]; do
  sdk="$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r\n')"
  case "$sdk" in
    ''|*[!0-9]*)
      echo "Waiting for responsive emulator shell ($i/30)..."
      sleep 5
      i=$((i + 1))
      ;;
    *)
      echo "Emulator API level: $sdk"
      break
      ;;
  esac
done

case "$sdk" in
  ''|*[!0-9]*)
    echo "Emulator shell never became responsive."
    exit 1
    ;;
esac

./gradlew :app:assembleDebug :lab-testkit:connectedAndroidTest --stacktrace
