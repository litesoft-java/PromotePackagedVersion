package org.litesoft.promotepackagedversion;

import org.litesoft.packageversioned.*;
import org.litesoft.server.util.*;

/**
 * Four Parameters are needed (Keys for the Arguments):
 * - Target ("Target") e.g. "jre"
 * - DeploymentGroup ("DeploymentGroup") - Promoting 'To' group name (validated against the "DeploymentGroupSet" and can NOT be the first entry)
 * - Bucket ("Bucket") - Bucket to Publish into (See ParameterBucket for details).
 * <p/>
 * In addition, the 'From' DeploymentGroup will automatically be selected as the entry "before" the 'To' group name.
 * <p/>
 * As each Argument key starts w/ a unique letter, the 'permutations' option is active.
 * Any non-keyed values are applied in the order above (excess keyed entries are noted, excess non-keyed entries are an Error)
 */
public class Parameters extends AbstractParametersS3 {
    private Parameter<?>[] mParameters = {mTarget, mDeploymentGroup, mBucket};

    private ParameterDeploymentGroup mFromDeploymentGroup = new ParameterDeploymentGroup();

    public Parameters( ArgsToMap pArgs ) {
        prepToString( mBucket, mTarget, mFromDeploymentGroup, "->", mDeploymentGroup );
        populate( mParameters, pArgs );
    }

    public String getFromDeploymentGroup() {
        return mFromDeploymentGroup.get();
    }

    @Override
    public boolean validate() {
        if ( validate( mParameters ) ) {
            String zPrevious = DeploymentGroupSet.get().previous( getDeploymentGroup() );
            if ( zPrevious != null ) {
                mFromDeploymentGroup.set( zPrevious ); // Auto Set the 'From' DeploymentGroup
                return true;
            }
            throw new IllegalArgumentException( "Can't Promote 'to' the first '" + getDeploymentGroup() +
                                                "' DeploymentGroup.  Group is: " + DeploymentGroupSet.get() );
        }
        return false;
    }
}
