package org.litesoft.promotepackagedversion;

import org.litesoft.commonfoundation.base.*;
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
    private ParameterDeploymentGroup mFromDeploymentGroup = new ParameterDeploymentGroup();

    private boolean mMulti;
    private String mNotDeploymentGroup;

    public Parameters( ArgsToMap pArgs ) {
        mDeploymentGroup = new ParameterDeploymentGroup() {
            @Override
            public boolean acceptable( String pValue ) {
                return "!".equals( pValue ) ||
                       (ConstrainTo.notNull( pValue ).startsWith( "!" ) ?
                        super.acceptable( pValue.substring( 1 ) ) :
                        super.acceptable( pValue ));
            }
        };
        prepToString( mBucket, mTarget, mFromDeploymentGroup, "->", mDeploymentGroup );
        populate( new Parameter[]{mTarget, mDeploymentGroup, mBucket}, pArgs );
    }

    public String getFromDeploymentGroup() {
        return mFromDeploymentGroup.get();
    }

    public boolean nextPair() {
        if ( mMulti ) {
            String zFrom = mFromDeploymentGroup.get();
            if ( !zFrom.equals( mNotDeploymentGroup ) ) {
                mDeploymentGroup.set( zFrom );
                return updateFromDeploymentGroup();
            }
        }
        return false;
    }

    @Override
    public boolean validate() {
        if ( validate( mTarget, mBucket ) ) {
            preprocessDeploymentGroup();
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

    private boolean preprocessDeploymentGroup() {
        DeploymentGroupSet zSet = DeploymentGroupSet.get();

        String zGroup = getDeploymentGroup();
        if ( "!".equals( zGroup ) ) {
            mMulti = true;
            mDeploymentGroup.set( zSet.last() );
        } else if ( ConstrainTo.notNull( zGroup ).startsWith( "!" ) ) {
            mMulti = true;
            mNotDeploymentGroup = zSet.assertMember( zGroup.substring( 1 ) );
            mDeploymentGroup.set( zSet.last() );
        } else {
            mMulti = false;
            mNotDeploymentGroup = null;
        }
        return validate( mDeploymentGroup ) && updateFromDeploymentGroup(); // Note Left to Right!
    }

    private boolean updateFromDeploymentGroup() {
        String zPrevious = DeploymentGroupSet.get().previous( getDeploymentGroup() );
        if ( zPrevious != null ) {
            mFromDeploymentGroup.set( zPrevious ); // Auto Set the 'From' DeploymentGroup
            return true;
        }
        return false;
    }
}
