package world;

import com.google.common.base.MoreObjects;

class NoTeam implements Team {


    @Override
    public String name() {
        return "Unassigned";
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name())
                .toString();
    }
}
