package map;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.*;

import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class MapGenerator {

    private static final Logger L = LogManager.getLogger(MapGenerator.class);
    final WorldDefinition worldDefinition;

    final OpenSimplexNoise noise1;
    final long seed;

    double baseFrequency = 5;

    public MapGenerator(long seed, WorldDefinition worldDefinition) {
        this.worldDefinition = worldDefinition;
        this.seed = seed;
        this.noise1 = new OpenSimplexNoise(seed);


    }

    int rows() {
        return this.worldDefinition.worldTotalRows();
    }

    int cols() {
        return this.worldDefinition.worldTotalColumns();
    }

    public void setBaseFrequency(double frequency) {
        baseFrequency = frequency;
    }


    public java.util.Map<Integer, ITerrainMap<Cord>> generateSectionMaps() {
        var worldMap = generateWorldMap();
        L.info("World map by seed [{}] [{}]", seed, worldMap.mapInfo());


        var s = IntStream.range(0, worldDefinition.totalNoOfSectors()).mapToObj(sectionNo -> {
                    var all = worldDefinition.sectionDefinition.all().collect(toSet());
                    var worldToSection = worldDefinition.world.toSection(sectionNo);


                    var terrainInSection = worldMap.terrainMap()
                            .entrySet()
                            .stream()
                            .filter(at -> all.contains(worldToSection.apply(at.getKey())))//Expensive way to find the cords for the sections
                            .collect(toUnmodifiableMap(e -> worldToSection.apply(e.getKey()), e -> e.getValue()));

                    return Maps.immutableEntry(sectionNo, ITerrainMap.create(terrainInSection));
                }
        );

        return s.collect(toUnmodifiableMap(e -> e.getKey(), e -> e.getValue()));
    }


    ITerrainMap<WorldCord> generateWorldMap() {

        double width = cols();
        double height = rows();

        var stats = IntStream.range(0, rows() * cols()).mapToDouble(i -> {
                    var c = i % cols();
                    var r = i / cols();
                    double nx = 1.0 * c / width - 0.5, ny = 1.0 * r / height - 0.5;

                    return noise(nx, ny);
                }
        ).summaryStatistics();

        L.info("Stats :[{}]", stats);

        Map<WorldCord, Terrain> map = Maps.newHashMap();
        IntStream.range(0, rows() * cols()).forEach(i -> {
            var c = i % cols();
            var r = i / cols();
            double nx = 1.0 * c / width - 0.5, ny = 1.0 * r / height - 0.5;
            var noise = noise(nx, ny);

            var terrain = toTerrain(noise);
            if (terrain != Terrain.Normal) {
                map.put(WorldCord.at(c, r), terrain);
            }
        });

        return ITerrainMap.create(map);
    }


    double noise(double nx, double ny) {
        var octaves =
                singleNoise(baseFrequency * nx, baseFrequency * ny) +
                        0.50 * singleNoise(baseFrequency * 2 * nx, baseFrequency * 2 * ny) +
                        0.25 * singleNoise(baseFrequency * 4 * nx, baseFrequency * 4 * ny);


        return octaves;
        //return Math.pow(octaves,0.5);
    }


    double singleNoise(double nx, double ny) {
        return normalize(noise1.eval(nx, ny));
    }

    double normalize(double val) {
        final double MAX_VAL = 0.888;

        val = val + MAX_VAL;

        val = val / 2 / MAX_VAL;
        assert val > 0 && val < 1;
        return val;
    }


    Terrain toTerrain(double val) {
        if (val < 0.4) return Terrain.Water;

        if (val > 1.1) return Terrain.Hill;
        if (val > 1.3) return Terrain.Mountain;
        return Terrain.Normal;
    }


}
