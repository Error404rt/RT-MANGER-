# RTManager

RTManager is a serious Android icon design workstation.

## Product direction

RTManager is designed to scan installed applications and icon packs, inspect launcher/adaptive icons, and provide a non-destructive editor with layers, vectors, masks, colors, geometry, effects, styles, batch operations, project persistence, and ZIP export.

The app does **not** build APK icon packs. Exported resources are intended to be consumed by downstream tools such as Renkin.

## Package

`com.rt.manger`

## Current milestone

Milestone 0: establish a buildable Android foundation and the initial editor architecture.

The current UI contains a deliberately small shell only. Real PackageManager scanning, icon-pack parsing, rendering, document persistence, and export are planned as subsequent milestones.

## Architecture

- `model`: domain models for applications and icon documents
- `ui`: Compose UI and theme
- future `core`: rendering, vector, image, color, Android integration
- future `feature`: apps, icon packs, editor, layers, styles, batch, projects, export
- future `data`: Room/project persistence and preferences

## License

Project license will be selected before external distribution.
