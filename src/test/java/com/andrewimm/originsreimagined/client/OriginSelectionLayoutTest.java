package com.andrewimm.originsreimagined.client;

public final class OriginSelectionLayoutTest {
    public static void main(String[] args) {
        for (int[] size : new int[][]{{320, 240}, {854, 480}, {2560, 1080}}) {
            OriginSelectionLayout layout = OriginSelectionLayout.calculate(size[0], size[1]);
            assert layout.advantages().x() >= 0;
            assert layout.disadvantages().x() + layout.disadvantages().width() <= size[0] + 2;
            assert layout.confirm().x() >= 0 && layout.confirm().x() + layout.confirm().width() <= size[0];
            assert layout.confirm().y() + layout.confirm().height() <= size[1];
        }
    }
}
