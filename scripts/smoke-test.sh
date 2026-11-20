#!/usr/bin/env bash
# Smoke test: boots a throwaway server for each target, checks that the plugin enables cleanly, then
# tests the plugin with no player connected, through console (RCON) commands:
#
#   - a dispenser with bone meal, powered by a redstone block, grows sugar cane to max-height
#   - at max-height it fails like vanilla and keeps its bone meal
#   - grow-by-one-block, a bigger max-height and dispenser-support: false, each set in config.yml
#     and applied with /bsc reload
#   - the dispenser uses up its last bone meal (no infinite bone meal)
#   - a broken config.yml is never overwritten and /bsc reload keeps the old settings
#   - the log has no errors, warnings or exceptions from the plugin
#
#   scripts/smoke-test.sh 26.3 1.17.1     # some targets (ids from ./gradlew -q testServers)
#   scripts/smoke-test.sh all             # every test server
#
# Each server runs in build/smoke/<id>/ with a fresh world and the default config.yml; its log is in
# build/smoke/<id>.log. Running this accepts the Minecraft EULA (https://aka.ms/MinecraftEULA) for
# those throwaway servers. Needs bash and python3. Exit code is non-zero if anything failed.
set -u
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$ROOT/build/smoke
mkdir -p "$OUT"
cd "$ROOT" || exit 2

SERVER_PORT=25590
RCON_PORT=25591
RCON_PASSWORD=smoketest
START_TIMEOUT=${START_TIMEOUT:-900}

./gradlew -q jar || { echo "build failed"; exit 1; }
SERVERS=$(./gradlew -q testServers) || { echo "couldn't list test servers"; exit 1; }

TARGETS=("$@")
[ "${1:-}" = all ] && TARGETS=($(awk '{ print $1 }' <<< "$SERVERS"))
[ ${#TARGETS[@]} -eq 0 ] && { echo "usage: $0 <id>... | all"; echo "ids:"; awk '{ print "  " $1 }' <<< "$SERVERS"; exit 2; }

# --- helpers ---------------------------------------------------------------

# rcon <command>: run one console command and print its output. SMOKE_DEBUG=1 shows every command.
rcon() {
  local out
  out=$(python3 -I "$ROOT/scripts/rcon.py" "$RCON_PORT" "$RCON_PASSWORD" "$1" 2>&1 | tail -n +2)
  [ -n "${SMOKE_DEBUG:-}" ] && printf '    > %s\n    %s\n' "$1" "$out" >&2
  printf '%s\n' "$out"
}

# The test site, inside force-loaded chunk 0,0: sugar cane at x=5 z=5 on sand (y=100) next to water,
# a dispenser at x=6 facing it, and a redstone block placed at x=7 to fire the dispenser.
CANE_Y=101

build_site() {
  rcon "forceload add 0 0" > /dev/null
  rcon "fill 2 99 2 9 100 9 minecraft:stone" > /dev/null
  rcon "fill 2 101 2 9 110 9 minecraft:air" > /dev/null
  rcon "setblock 5 100 5 minecraft:sand" > /dev/null
  rcon "setblock 5 100 6 minecraft:water" > /dev/null
  rcon "setblock 6 101 5 minecraft:dispenser[facing=west]" > /dev/null
}

# reset_cane: a single sugar cane block.
reset_cane() {
  rcon "fill 5 102 5 5 110 5 minecraft:air" > /dev/null
  rcon "setblock 5 $CANE_Y 5 minecraft:sugar_cane" > /dev/null
}

# fill_dispenser <amount>
fill_dispenser() { rcon "item replace block 6 101 5 container.0 with minecraft:bone_meal $1" > /dev/null; }

# fire: power the dispenser once, then remove the power so it can fire again.
fire() {
  rcon "setblock 7 101 5 minecraft:redstone_block" > /dev/null
  sleep 1.5
  rcon "setblock 7 101 5 minecraft:air" > /dev/null
  sleep 0.5
}

# height: how many sugar cane blocks are stacked from y=101.
height() {
  local h=0
  while [ $h -lt 9 ] && rcon "execute if block 5 $((CANE_Y + h)) 5 minecraft:sugar_cane" | grep -q 'passed'; do
    h=$((h + 1))
  done
  echo $h
}

# bone_meal: how much bone meal is in the dispenser. Before 1.20.5 the NBT says Count: 63b;
# from 1.20.5 it says count: 63, and leaves count out when it's 1.
bone_meal() {
  local nbt n
  nbt=$(rcon "data get block 6 101 5 Items")
  if ! grep -q 'bone_meal' <<< "$nbt"; then echo 0; return; fi
  n=$(grep -oE '[Cc]ount: ?[0-9]+' <<< "$nbt" | grep -oE '[0-9]+' | head -1)
  echo "${n:-1}"
}

# set_config <key> <value>: change one setting in the plugin's config.yml.
set_config() { perl -i -pe "s/^(\\s+\\Q$1\\E:).*/\$1 $2/" "$CONFIG"; }

# reload: run /bsc reload and print its reply.
reload() { rcon "bsc reload" | sed 's/§.//g'; }

PASSED=0
FAILED=0
FAILURES=()
# check <name> <expected> <actual>
check() {
  if [ "$2" = "$3" ]; then
    echo "  PASS  $1"; PASSED=$((PASSED + 1))
  else
    echo "  FAIL  $1 (expected $2, got $3)"; FAILED=$((FAILED + 1)); FAILURES+=("$ID: $1")
  fi
}

# expect_grow <name> <height> <bone meal>: fire once and check the result.
expect_fire() {
  fire
  check "$1: height" "$2" "$(height)"
  check "$1: bone meal left" "$3" "$(bone_meal)"
}

# --- one target ------------------------------------------------------------

run_target() {
  ID=$1
  local line task
  line=$(awk -v id="$ID" '$1 == id' <<< "$SERVERS")
  [ -n "$line" ] || { echo "unknown target $ID"; FAILED=$((FAILED + 1)); FAILURES+=("$ID: unknown target"); return; }
  task=$(awk '{ print $2 }' <<< "$line")

  local dir=$OUT/$ID
  LOG=$OUT/$ID.log
  CONFIG=$dir/plugins/BonemealableSugarCane/config.yml
  echo "== $ID ($(awk '{ print $3 " " $4 ", Java " $5 }' <<< "$line"))"

  # Fresh world and default config every time; downloaded libraries are kept for speed.
  mkdir -p "$dir"
  rm -rf "$dir"/world* "$dir/plugins/BonemealableSugarCane"
  echo "eula=true" > "$dir/eula.txt"
  cat > "$dir/server.properties" <<EOF
server-ip=127.0.0.1
server-port=$SERVER_PORT
online-mode=false
level-type=minecraft\:flat
generate-structures=false
spawn-protection=0
view-distance=4
simulation-distance=4
max-tick-time=-1
pause-when-empty-seconds=-1
enable-rcon=true
rcon.port=$RCON_PORT
rcon.password=$RCON_PASSWORD
management-server-port=0
EOF

  : > "$LOG"
  ( ./gradlew "$task" "-PserverDir=build/smoke/$ID" --console=plain < /dev/null >> "$LOG" 2>&1
    echo "GRADLE_EXIT=$?" >> "$LOG" ) &

  local status=timeout
  for ((i = 0; i < START_TIMEOUT; i++)); do
    if grep -q 'RCON running' "$LOG"; then status=started; break; fi
    if grep -q 'GRADLE_EXIT=' "$LOG"; then status=exited; break; fi
    sleep 1
  done
  if [ "$status" != started ]; then
    check "server started" started "$status"
    [ "$status" = timeout ] && pkill -f "build/smoke/$ID" 2>/dev/null
    return
  fi
  # Wait for the first ticks, so commands don't arrive while the server is still finishing startup.
  for ((i = 0; i < 120; i++)); do grep -q 'Done (' "$LOG" && break; sleep 1; done
  sleep 2

  check "plugin enabled" yes "$(grep -q 'Enabling BonemealableSugarCane' "$LOG" && echo yes || echo no)"
  check "/bsc version" yes "$(rcon "bsc version" | grep -q "$(grep '^version=' gradle.properties | cut -d= -f2)" && echo yes || echo no)"

  build_site
  reset_cane
  fill_dispenser 64

  # Defaults: max-height 3, grow-by-one-block false.
  expect_fire "grow to max" 3 63
  expect_fire "already at max: fails like vanilla" 3 63

  set_config grow-by-one-block true
  check "/bsc reload" yes "$(reload | grep -qi 'reloaded' && echo yes || echo no)"
  reset_cane
  expect_fire "grow by one (1st)" 2 62
  expect_fire "grow by one (2nd)" 3 61
  expect_fire "grow by one at max" 3 61

  set_config max-height 5
  reload > /dev/null
  expect_fire "max-height 5, grow by one" 4 60
  set_config grow-by-one-block false
  reload > /dev/null
  expect_fire "max-height 5, grow to max" 5 59

  reset_cane
  fill_dispenser 1
  expect_fire "last bone meal is used up" 5 0

  set_config dispenser-support false
  reload > /dev/null
  reset_cane
  fill_dispenser 64
  expect_fire "dispenser-support false: nothing grows" 1 64

  # A broken config.yml is left alone and the previous settings stay active.
  rcon "say smoke-test: broken config start" > /dev/null
  cp "$CONFIG" "$CONFIG.good"
  printf 'settings:\n  max-height: [oops\n' > "$CONFIG"
  local broken; broken=$(cat "$CONFIG")
  check "/bsc reload with a broken config.yml" yes "$(reload | grep -qi "couldn't reload" && echo yes || echo no)"
  check "broken config.yml left unchanged" yes "$([ "$(cat "$CONFIG")" = "$broken" ] && echo yes || echo no)"
  check "previous settings kept (dispenser still off)" 1 "$(reset_cane; fire; height)"
  mv "$CONFIG.good" "$CONFIG"
  reload > /dev/null
  rcon "say smoke-test: broken config end" > /dev/null

  rcon "stop" > /dev/null
  for ((i = 0; i < 180; i++)); do grep -q 'GRADLE_EXIT=' "$LOG" && break; sleep 1; done
  check "server stopped cleanly" "GRADLE_EXIT=0" "$(grep -o 'GRADLE_EXIT=[0-9]*' "$LOG")"

  # Errors and warnings from the plugin, and any exception, outside the deliberate broken-config test.
  local problems
  problems=$(sed 's/\x1b\[[0-9;]*m//g' "$LOG" \
    | awk '/smoke-test: broken config start/ { skip = 1 } !skip { print } /smoke-test: broken config end/ { skip = 0 }' \
    | grep -E 'Exception|Caused by|(WARN|ERROR|SEVERE).*BonemealableSugarCane|bonemealablesugarcane\.' | head -5)
  check "no plugin errors, warnings or exceptions in the log" "" "$problems"
}

for t in "${TARGETS[@]}"; do run_target "$t"; done

echo
echo "$PASSED passed, $FAILED failed (logs in build/smoke/)"
if [ "$FAILED" -gt 0 ]; then printf '  %s\n' "${FAILURES[@]}"; exit 1; fi
