# Paratrooper

Anti-air arcade game for Android, from 2012. You man the gun at the bottom of
the screen: drag to aim the barrel, tap to fire. Helicopters come in first and
drop paratroopers — let four of them land on the same side of the gun and
they storm it. Bombs from the bombers finish it off just as fast. Survive long
enough and the bombers arrive, then both waves at once. Tap the pause
button in the corner to take a break, tap anywhere after "Game Over" to start
again.

Package `org.softosaurus.diversantfree`, on Google Play as
[Paratrooper](https://play.google.com/store/apps/details?id=org.softosaurus.diversantfree).
There is a paid, ad-free edition at `org.softosaurus.diversant`, which the
in-game Buy button links to.

## Project layout

| Path | What |
|---|---|
| `app/` | The Gradle module: Java sources, sprites and sounds |
| `Images/` | Store graphics and PSD sources |
| `tool/play_upload.mjs` | Uploads an AAB to Play via the Android Publisher API |
| `tool/play_notes/` | Release notes per locale, picked up by the upload script |

The game loop and rendering live in `MySurfaceView`, one `SurfaceView` drawn
from its own thread; `Gun`, `Helic`, `Bomber`, `Parash`, `Bomb` and `Shot` are
the sprites. Ads are served by AdMob (`play-services-ads`) with a UMP consent
dialog where required. Target SDK 36, min SDK 23.

## Build

Requires JDK 17+ and the Android SDK (platform 36). Point `local.properties`
at the SDK (`sdk.dir=C\:/path/to/Sdk`), then:

```bash
./gradlew :app:assembleDebug
```

## Release

1. **Signing.** Copy `keystore.properties.example` to `keystore.properties`
   (git-ignored) and fill in the upload key. Use forward slashes in
   `storeFile`.
2. **Version.** Bump `versionCode` and `versionName` in `app/build.gradle`.
   Play rejects a reused `versionCode`.
3. **Release notes.** Edit `tool/play_notes/<locale>.txt`.
4. **Build and upload:**

```bash
./gradlew :app:bundleRelease
node tool/play_upload.mjs --track production
```

The upload script needs a Play service-account key: pass `--key`, set
`PLAY_SERVICE_ACCOUNT_JSON`, or keep it at
`%USERPROFILE%/.secrets/ohmyfridge-play-publisher.json`. It never lives in the
repo. Use `--status draft` to upload without rolling out.

If Play answers `signed with the wrong key`, the `storeFile` in
`keystore.properties` points at the wrong keystore; nothing is published in
that case.
