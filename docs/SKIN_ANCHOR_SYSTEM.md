# Skin anchors

`SkinAnchorManager` stores local anchor preferences in `config/origins_overhaul/skin_anchors.json`. Eye presets are `STANDARD`, `HIGH`, `LOW`, `WIDE`, `NARROW`, `DISABLED` and `CUSTOM`; each eye stores an 8x8 face rectangle and `BASE`, `OUTER` or `BOTH` layer.

The current identity key is a SHA-256 of the skin texture identifier. This prevents settings from leaking between different texture identifiers, but it is not yet a hash of downloaded PNG bytes because the 26.1.2 skin provider does not expose the raw image through the preview API. A future skin-byte cache can replace the key without changing the JSON shape.

The selection screen includes a minimal 8x8 eye-anchor editor. It never writes or modifies the source skin texture.
