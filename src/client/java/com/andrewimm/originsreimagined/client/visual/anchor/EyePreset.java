package com.andrewimm.originsreimagined.client.visual.anchor;

public enum EyePreset {
    STANDARD(2, 3, 2, 1, 5, 3),
    HIGH(2, 2, 2, 1, 5, 2),
    LOW(2, 4, 2, 1, 5, 4),
    WIDE(1, 3, 2, 1, 5, 3),
    NARROW(2, 3, 1, 1, 5, 3),
    DISABLED(0, 0, 0, 0, 0, 0),
    CUSTOM(2, 3, 2, 1, 5, 3);

    private final int leftX, leftY, leftWidth, leftHeight, rightX, rightY;
    EyePreset(int leftX, int leftY, int leftWidth, int leftHeight, int rightX, int rightY) { this.leftX = leftX; this.leftY = leftY; this.leftWidth = leftWidth; this.leftHeight = leftHeight; this.rightX = rightX; this.rightY = rightY; }
    public EyeAnchor left() { return new EyeAnchor(leftX, leftY, leftWidth, leftHeight, EyeLayer.BASE); }
    public EyeAnchor right() { return new EyeAnchor(rightX, rightY, leftWidth, leftHeight, EyeLayer.BASE); }
}
