# Recording the showcase

A short shot list for a new showcase GIF and screenshots, recorded on a test server.

## Set up

1. Start a test server: `./gradlew runServer` (Paper 26.3). Agree to the EULA in `run/26.3/eula.txt` the first time.
2. Join at `localhost`, then in the console: `op <your name>`.
3. Make the scene calm and bright, in chat:
   - stop the daylight cycle (`/gamerule doDaylightCycle false`; 26.x renamed the game rules, so
     tab-complete `/gamerule` to find the daylight one) and `/time set noon`
   - `/weather clear` and `/gamemode survival`
4. Build next to water: a short row of sand with sugar cane on it, and a small dispenser farm (dispensers
   facing a row of cane, wired to one button or lever).
5. Before recording: F1 hides the HUD (show it again for the hand shots, so the bone meal count is
   visible), FOV 70, smooth lighting on, a nice resource pack off (keep it vanilla).

## Shots (each 3–6 seconds)

1. **Hand, grow to max:** look at a 1-block cane, right-click once with bone meal. It shoots up to 3 with
   the green particles. HUD on, so the stack goes down by one.
2. **Dispenser farm:** a row of dispensers facing 1-block cane. Press the button and every cane grows at once.
   Side view, a few blocks back.
3. **Grow by one vs grow to max:** two canes side by side. Set `grow-by-one-block: true`, `/bsc reload`,
   then click one cane three times (1 → 2 → 3). Set it back to `false`, `/bsc reload`, click the other once
   (1 → 3). Or record them as two clips and put them next to each other.
4. *(Optional)* **Taller cane:** `max-height: 5`, `/bsc reload`, one click grows it to 5.

For the main GIF, shots 1 and 2 are enough. Shot 3 makes a good second GIF or two screenshots.

## Sizes and formats

| Where | What | Size and format |
|---|---|---|
| Modrinth description | Showcase GIF | 800×450 (16:9), 15 fps, 4–8 s loop, ideally under 5 MB. Upload it to the Modrinth gallery and use the CDN link in the description, as now. |
| Modrinth gallery | Screenshots | 1920×1080 PNG (or JPG if a PNG is very large), one per shot, with a short title. |
| Hangar description | Showcase GIF | Same GIF, linked from the Modrinth CDN (Hangar has no gallery). |
| SpigotMC description | Showcase GIF | Same GIF, linked from the Modrinth CDN. SpigotMC shows images at their own size, so keep it at 800 px wide or less. |
| All | Icon | Square PNG, 512×512 (the current icon is fine). |

Check each site's upload form for its current file size limit before uploading.

A screen recording can be turned into a small, good-looking GIF with [ffmpeg](https://ffmpeg.org/):

```bash
ffmpeg -i clip.mov -vf "fps=15,scale=800:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer" -loop 0 showcase.gif
```

If it's too big, lower `fps` to 12, the width to 640, or cut the clip shorter.

After uploading, replace the showcase link in `README.md` and `docs/store/*` with the new GIF's URL.
