package messages;

import com.dslplatform.json.CompiledJson;
import com.google.common.base.MoreObjects;
import world.ViewBorder;
import world.ViewDefinition;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public abstract class ViewMessage {

    @CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
    public static class InitView extends ViewMessage {
        @Nonnull
        public final ViewDefinition viewDefinition;
        @Nonnull
        public final Collection<Integer> sections;

        @Nonnull
        public final Map<Integer, ViewBorder> sectionBorders;

        public InitView(ViewDefinition viewDefinition, Collection<Integer> sections, @Nonnull Map<Integer, ViewBorder> sectionBorders) {
            assert !sections.isEmpty();
            assert !sectionBorders.isEmpty();
            this.sectionBorders = sectionBorders;
            this.viewDefinition = requireNonNull(viewDefinition);
            this.sections = requireNonNull(sections);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("viewDefinition", viewDefinition)
                    .add("sectors", sections)
                    .add("sectionBorders", sectionBorders)
                    .toString();
        }
    }
}
