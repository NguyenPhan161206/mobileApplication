#!/usr/bin/env bash
# Wrapper dùng cho nút bấm trên Desktop: chạy chương trình rồi giữ cửa sổ lại.
cd "$(dirname "$(readlink -f "$0")")"
./run.sh
echo
echo "=== Nhan Enter de dong ==="
read -r
