#!/usr/bin/env bash
# Biên dịch và chạy chương trình Kotlin Lap chỉ với 1 cú nhấp.
# Không cần gõ lệnh: chỉ cần double-click file "Chay Kotlin Lap.desktop".
set -e
export PATH="/snap/bin:$PATH"

cd "$(dirname "$(readlink -f "$0")")"

OUT_DIR="/tmp/opencode/kotlin_lap_classes"
rm -rf "$OUT_DIR"

echo ">> Đang biên dịch..."
kotlinc exercise.kt exercises/*.kt -d "$OUT_DIR"

echo ">> Đang chạy..."
kotlin -cp "$OUT_DIR" ExerciseKt
