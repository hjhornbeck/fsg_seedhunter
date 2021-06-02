/**
 * An interface that must be implemented by any class that wants
 *  automatic notification when a seed is stored in a SeedStorage
 *  instance.
 */
public interface SeedListener {

    /**
     * Accept the Seed offered by a SeedStorage instance. Note that this
     *  is not a thread-safe call; either perform read-only access or immediately
     *  clone the incoming object.
     * @param seed The Seed that was sent to the SeedStorage instance.
     */
    void new_seed( Seed seed );

}
