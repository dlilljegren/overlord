import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;

public class Test {

    @org.junit.jupiter.api.Test
    public void testBid() {
        var bids = Stream.of(Bid.userA("a1"), Bid.userA("a1"), Bid.userA("a2"), Bid.userB("a1"), Bid.userB("a3"), Bid.userB("a3"), Bid.userB("a3"), Bid.userB("a3"));

        Map<String, Integer> result = bids.collect(groupingBy(Bid::getUserId, mapping(Bid::getAuctionId, collectingAndThen(toUnmodifiableSet(), Set::size))));


        result.entrySet()
                .stream()
                .map(e -> format("%s=%d", e.getKey(), e.getValue()))
                .forEach(System.out::println);

    }

    static class Bid {
        private final String auctionId;
        private final String userId;

        String getAuctionId() {
            return this.auctionId;
        }

        String getUserId() {
            return this.userId;
        }

        Bid(String auctionId, String userId) {
            this.auctionId = auctionId;
            this.userId = userId;
        }

        static Bid userA(String auctionId) {
            return new Bid(auctionId, "userA");
        }

        static Bid userB(String auctionId) {
            return new Bid(auctionId, "userB");
        }
    }
}
