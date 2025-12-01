package org.example;

import java.util.Objects;

public class Par {
    Integer x;
    Integer y;

    public Par(Integer x) {
        this.x = x;
        this.y = null; // Representa par reflexivo (x,x)
    }

    public Par(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Par par = (Par) obj;

        if (this.y == null && par.y == null) {
            return Objects.equals(x, par.x);
        }
        return Objects.equals(x, par.x) && Objects.equals(y, par.y);
    }

    @Override
    public int hashCode() {
        if (y == null) {
            return Objects.hash(x, "reflexivo");
        }
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        if (y == null) {
            return "(" + x + ")";
        }
        return "(" + x + "," + y + ")";
    }
}