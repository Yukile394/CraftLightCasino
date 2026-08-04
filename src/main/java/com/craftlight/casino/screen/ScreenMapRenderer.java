package com.craftlight.casino.screen;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * Her ekran hucresi (1 harita = 1 blok) icin Silvera temali bir arka plan cizer.
 * col/row, ekranin 3x3 gridindeki bu hucrenin konumunu belirtir (0-2, sol-ust'ten baslar).
 * Gercek zamanli yayin/goruntu entegrasyonu icin ileride bu sinifin updatePixels() metodu
 * bir WebSocket/harici kaynaktan gelen piksel verisiyle beslenebilir.
 */
public class ScreenMapRenderer extends MapRenderer {

    private static final int SIZE = 128;

    private final int col; // 0,1,2 -> sol, orta, sag
    private final int row; // 0,1,2 -> ust, orta, alt
    private boolean rendered = false;

    public ScreenMapRenderer(int col, int row) {
        super(true); // contextual = true, her oyuncuda ayni gorunur ve sadece bir kez cizilir
        this.col = col;
        this.row = row;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (rendered) return;
        rendered = true;

        byte silver = MapPalette.matchColor(192, 192, 192);
        byte blue = MapPalette.matchColor(77, 163, 255);
        byte white = MapPalette.matchColor(255, 255, 255);
        byte dark = MapPalette.matchColor(40, 40, 40);

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                byte color = dark;
                // Ince gumus cerceve
                boolean isBorder = x < 3 || y < 3 || x >= SIZE - 3 || y >= SIZE - 3;
                if (isBorder) {
                    color = silver;
                } else if ((x / 8 + y / 8) % 2 == 0) {
                    color = MapPalette.matchColor(30, 30, 32);
                } else {
                    color = dark;
                }
                canvas.setPixel(x, y, color);
            }
        }

        // Merkez hucrede (1,1) Silvera logosu/yazisi icin basit bir vurgu cizgisi
        if (col == 1 && row == 1) {
            for (int x = 20; x < SIZE - 20; x++) {
                canvas.setPixel(x, SIZE / 2 - 1, blue);
                canvas.setPixel(x, SIZE / 2, white);
                canvas.setPixel(x, SIZE / 2 + 1, blue);
            }
        }
    }
}
