/**
 * Represent a Minecraft world seed. Also contains information on the starting point,
 *  statistics on how it was reached, and the filter this seed matches. There's a full
 *  set of getters and setters to facilitate object reuse, but their usage is discouraged
 *  for any other use case.
 */
public class Seed {

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStructure_seeds_checked() {
        return structure_seeds_checked;
    }

    public void setStructure_seeds_checked(long structure_seeds_checked) {
        this.structure_seeds_checked = structure_seeds_checked;
    }

    public long getBiome_seeds_checked() {
        return biome_seeds_checked;
    }

    public void setBiome_seeds_checked(long biome_seeds_checked) {
        this.biome_seeds_checked = biome_seeds_checked;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * The filter to be applied to this seed. To be considered valid, this cannot be null.
     */
    private String filter = null;

    /**
     * The starting point for the search of this seed. This could a literal starting point
     *  for a linear search, or a random number seed for a random search.
     */
    private long start = 0;

    /**
     * The number of structure seeds tested to find the ending seed. If both this and the
     *  number of biome checks are zero, no search has been initiated.
     */
    private long structure_seeds_checked = 0;

    /**
     * The number of biome seeds tested to find the ending seed. If both this and the
     *  number of structure checks are zero, no search has been initiated.
     */
    private long biome_seeds_checked = 0;

    /**
     * The Minecraft world seed. May not be valid, see the structure and biome seed values
     *  for how to check.
     */
    private long seed = 0;



}
