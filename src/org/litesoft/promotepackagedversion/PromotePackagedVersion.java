package org.litesoft.promotepackagedversion;

import org.litesoft.aws.s3.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.packageversioned.*;
import org.litesoft.server.util.*;

import java.io.*;

public class PromotePackagedVersion {
    public static final String VERSION = "0.9";

    private Parameters mParameters;

    public PromotePackagedVersion( Parameters pParameters ) {
        if ( !(mParameters = pParameters).validate() ) {
            System.exit( 1 );
        }
    }

    public static void main( String[] args )
            throws Exception {
        System.out.println( "PromotePackagedVersion vs " + VERSION );
        new PromotePackagedVersion( new Parameters( new ArgsToMap( args ) ) ).process();
        System.out.println( "Done!" );
    }

    private Persister createPersister()
            throws IOException {
        ParameterBucket zBucket = mParameters.getParameterBucket();
        return new S3Persister( BucketCredentials.get( zBucket.get() ), new Bucket( zBucket.getS3Endpoint(), zBucket.get() ) );
    }

    private void process()
            throws IOException {
        System.out.println( "Promote (Bucket: " + mParameters.getBucket() + ") '" + getTarget() + "' " + getFromDeploymentGroup() + " -> " +
                            getDeploymentGroup() + "" );
        new Promoter( createPersister() ).process();
    }

    private String getTarget() {
        return mParameters.getTarget();
    }

    private String getFromDeploymentGroup() {
        return mParameters.getFromDeploymentGroup();
    }

    private String getDeploymentGroup() {
        return mParameters.getDeploymentGroup();
    }

    protected class Promoter {
        protected final Persister mPersister;
        protected final String mVersion;

        public Promoter( Persister pPersister ) {
            mPersister = pPersister;
            ParameterVersion zVersion = new ParameterVersion();
            zVersion.set( VersionFile.fromFileLines( mPersister.getTextFile( createPath( getFromDeploymentGroup() + ".txt" ) ) ).get() );
            mVersion = zVersion.get();
        }

        public String getVersion() {
            return mVersion;
        }

        public void process() {
            createDeploymentGroupVersionFile( "-" + getVersion() );
            createDeploymentGroupVersionFile( "" );
        }

        private void createDeploymentGroupVersionFile( String pSpecificVersionSuffix ) {
            String zPath = createPath( getDeploymentGroup() + pSpecificVersionSuffix + ".txt" );
            System.out.println( "  Writing: " + zPath );
            mPersister.putTextFile( zPath, Strings.toLines( getVersion() ) );
        }

        private String createPath( String pFileName ) {
            return "versioned/" + getTarget() + "/" + pFileName;
        }
    }
}
