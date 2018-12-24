package world;

public class ViewDefinition {
    public final WorldCord origo;
    public final int width;
    public final int height;

    public final int mainSection;


    public ViewDefinition(final WorldCord origo, final int mainSection, final int width, final int height) {
        this.origo = origo;
        this.width = width;
        this.height = height;
        this.mainSection = mainSection;

    }

    public static ViewDefinition create(final WorldDefinition worldDefinition, final int sectionNo, final int width, final int height) {
        var wc = worldDefinition.section.worldOrigo(sectionNo);//.add(-width/2, -height/2); //We want origo in the middle
        return new ViewDefinition(wc, sectionNo, width, height);
    }


}
