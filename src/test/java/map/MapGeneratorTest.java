package map;

import games.GameDefinitions;
import org.junit.jupiter.api.Test;
import world.Cord;
import world.ITerrainMap;
import world.Terrain;
import world.WorldCord;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MapGeneratorTest {


    @Test
    void generate() throws IOException {
        var worldDefinition = GameDefinitions.SMALL.worldDefinition();

        var underTest = new MapGenerator(27, worldDefinition);
        underTest.baseFrequency = 10;
        var map = underTest.generateWorldMap();
        assertFalse(map.terrainMap().isEmpty());

        var jSer = GameDefinitions.SMALL.serializer();

        var json = jSer.toJson(map);

        System.out.println(json);
        toImage(map, worldDefinition.worldWidth, worldDefinition.worldHeight);
    }

    private void toImage(ITerrainMap<WorldCord> map, int width, int height) throws IOException {
        int WIDTH = width;
        int HEIGHT = height;

        Stream<Map.Entry<WorldCord, Terrain>> terrainStream = map.terrainMap().entrySet().stream();

        var terrains = terrainStream.collect(toUnmodifiableMap(ta -> ta.getKey(), ta -> color(ta.getValue())));

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < WIDTH; x++) {
                var color = terrains.getOrDefault(Cord.at(x, y), Color.GREEN);


                //int rgb = 0x010101 * (int)((value + 1) * 127.5);
                image.setRGB(x, y, color.getRGB());
            }
        }
        image = scale2(image, 10);

        ImageIO.write(image, "png", new File("map.png"));

    }


    Color color(Terrain t) {
        if (t == Terrain.Mountain) return Color.DARK_GRAY;
        if (t == Terrain.Water) return Color.BLUE;
        if (t == Terrain.Hill) return Color.GRAY;
        return Color.GREEN;
    }

    private static BufferedImage scale2(BufferedImage before, double scale) {
        int w = before.getWidth();
        int h = before.getHeight();
        // Create a new image of the proper size
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp
                = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        Graphics2D g2 = (Graphics2D) after.getGraphics();
        // Here, you may draw anything you want into the new image, but we're
        // drawing a scaled version of the original image.
        g2.drawImage(before, scaleOp, 0, 0);
        g2.dispose();
        return after;
    }
}