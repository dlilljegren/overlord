package world;

public enum HexType {


    ODD_R {
        @Override
        public Cord toCord(Hex cube) {
            var col = cube.q + (cube.s - (cube.s & 1)) / 2;
            var row = cube.s;
            return Cord.at(col, row);
        }

        @Override
        public Hex toHex(Cord hex) {
            int q = hex.col - (hex.row - (hex.row & 1)) / 2;
            int s = hex.col;
            int r = -q - s;
            return Hex.atQRS(q, r, s);
        }
    },

    EVEN_R {
        @Override
        public Cord toCord(Hex cube) {
            int col = cube.q + (cube.s + (cube.s & 1)) / 2;
            int row = cube.s;
            return Cord.at(col, row);
        }

        @Override
        public Hex toHex(Cord hex) {
            int q = hex.col - (hex.row + (hex.row & 1)) / 2;
            int s = hex.row;
            int r = -q - s;
            return Hex.atQRS(q, r, s);
        }
    },

    ODD_Q {
        @Override
        public Cord toCord(Hex cube) {
            var col = cube.q;
            var row = cube.s + (cube.q - (cube.q & 1)) / 2;
            return Cord.at(col, row);
        }

        @Override
        public Hex toHex(Cord hex) {
            int q = hex.col;
            int s = hex.row - (hex.col - (hex.col & 1)) / 2;
            int r = -q - s;
            return Hex.atQRS(q, r, s);
        }
    },

    EVEN_Q {
        @Override
        public Cord toCord(Hex cube) {
            int col = cube.q;
            int row = cube.s + (cube.q + (cube.q & 1)) / 2;
            return Cord.at(col, row);
        }

        @Override
        public Hex toHex(Cord hex) {
            int q = hex.col;
            int s = hex.row - (hex.col + (hex.col & 1)) / 2;
            int r = -q - s;
            return Hex.atQRS(q, r, s);
        }
    };

    public abstract Cord toCord(Hex cube);

    public abstract Hex toHex(Cord hex);
}
