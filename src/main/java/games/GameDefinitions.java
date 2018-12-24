package games;

import map.BasicBaseGenerator;
import map.IBaseGenerator;
import world.SectionDefinition;
import world.Team;
import world.Teams;
import world.WorldDefinition;

import java.time.Duration;
import java.util.stream.Stream;

public class GameDefinitions {

    private final static Duration STANDARD_TIMEOUT = Duration.ofSeconds(2);
    private final static Duration CURSOR_BROADCAST = Duration.ofMillis(250);


    public static GameDefinition SMALL = new SmallGame();
    public static GameDefinition SMALL_HEX = new SmallHexGame();


    static class SmallGame implements GameDefinition {
        private final WorldDefinition worldDefinition;
        private final Duration sectionCycle;
        private final Duration zoneOfControlCycle;
        private final IJson jsoner;

        private final IBaseGenerator baseGenerator;

        SmallGame() {
            worldDefinition = new WorldDefinition(5, 5, 5, 5, 27,
                    new Rules() {
                    },
                    SectionDefinition.create(21, 21, SectionDefinition.GridType.SQUARE)
            );
            this.sectionCycle = Duration.ofMinutes(5);
            this.zoneOfControlCycle = Duration.ofMillis(10000);
            this.jsoner = new JsonDslImpl();

            this.baseGenerator = new BasicBaseGenerator(worldDefinition, 3, 3);
        }

        @Override
        public WorldDefinition worldDefinition() {
            return this.worldDefinition;
        }

        @Override
        public Stream<Team> availableTeams() {
            return Stream.of(
                    Teams.teamForName("Blue"),
                    Teams.teamForName("Red")
            );
        }

        @Override
        public Duration combatCycle() {
            return this.sectionCycle;
        }

        @Override
        public Duration zoneOfControlCycle() {
            return this.zoneOfControlCycle;
        }

        @Override
        public Duration standardTimeout() {
            return STANDARD_TIMEOUT;
        }

        @Override
        public IJson serializer() {
            return jsoner;
        }

        @Override
        public Duration cursorBroadcastCycle() {
            return CURSOR_BROADCAST;
        }

        @Override
        public IBaseGenerator baseGenerator() {
            return baseGenerator;
        }

    }

    static class SmallHexGame extends SmallGame {
        private final WorldDefinition worldDefinition;

        SmallHexGame() {
            worldDefinition = new WorldDefinition(3, 2, 5, 5, 27, new Rules() {
            }, SectionDefinition.create(21, 21, SectionDefinition.GridType.HEX));
        }

        @Override
        public WorldDefinition worldDefinition() {
            return this.worldDefinition;
        }
    }
}
