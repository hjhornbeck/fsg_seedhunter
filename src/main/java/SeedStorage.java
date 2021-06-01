import java.util.LinkedList;

/**
 * A container for discovered seeds. This allows for flexibility within the hunt;
 *  multiple hunts can be run simultaneously with different settings with the results
 *  being fed into multiple SeedStorage containers. It also makes writing the hunt code
 *  a bit easier, as it need not worry about locking or storage.
*/
public class SeedStorage {

    /**
     * Store
     */
    private LinkedList queue;

}
