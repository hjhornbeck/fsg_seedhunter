import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A container for discovered seeds. This allows for flexibility within mining;
 *  multiple attempts can be run simultaneously with different settings with the results
 *  being fed into multiple SeedStorage containers. It also makes writing the mining code
 *  a bit easier, as it need not worry about locking or storage.
*/
public class SeedStorage {

    /**
     * Storage for all seeds that have been found.
     */
    private ConcurrentLinkedQueue<Seed> seeds;

    /**
     * Storage for any SeedListeners interested in instant notifications.
     */
    private ConcurrentLinkedQueue<SeedListener> listeners;

    /**
     * Add a SeedListener instance for notification when a new Seed arrives.
     * @param listen The SeedListener in question.
     * @return True if the instance could be added, false otherwise.
     */
    public boolean addListener( SeedListener listen ) {

        return listeners.add( listen );
    }

    /**
     * Remove a SeedListener instance from the notification queue.
     * @param listen The SeedListener in question.
     * @return True if the instance could be removed, false otherwise.
     */
    public boolean removeListener( SeedListener listen ) {

        // by default, assume failure
        boolean retVal = false;

        // iterate over all known listeners
        Iterator<SeedListener> iter = listeners.iterator();
        SeedListener target;
        while( iter.hasNext() ) {

            target = iter.next();
            if ( listen.equals(target) ) {

                // remove any matching listeners, and continue iteration
                iter.remove();
                retVal = true;
            }
        } // while

        return retVal;
    }
    /**
     * Allow seeds to be added to the queue. It is assumed that the mining
     *  process is slow, and Seeds are only given rarely. Note that even if
     *  there are SeedListeners, the Seed is not removed from the queue. Call
     *  .getSeed() explicitly to do that.
     * @param seed The Seed to be added.
     * @return True if the seed could be added, false otherwise.
     */
    public boolean addSeed( Seed seed ) {

        boolean retVal = seeds.add( seed );

        // only notify listeners if the seed could be successfully added
        if( retVal ) {
            for (SeedListener listener : listeners)
                listener.new_seed(seed);
        }
        return retVal;
    }

    /**
     * Check if this Publisher has a seed available.
     * @return True if it does, false otherwise.
     */
    public boolean hasSeed() {
        return !seeds.isEmpty();
    }

    /**
     * Get the earliest seed from this Publisher, removing it from the queue.
     * @return A Seed instance if there was a seed available, or null if there was not.
     */
    public Seed getSeed() {
        return seeds.poll();
    }
}
