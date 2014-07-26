package org.litesoft.promotepackagedversion;

import org.litesoft.packageversioned.*;
import org.litesoft.server.util.*;

public class PromotePackagedVersion extends AbstractAppS3<Parameters> {
    public static final String VERSION = "0.9";

    public PromotePackagedVersion( Parameters pParameters ) {
        super( "Promote", pParameters );
    }

    public static void main( String[] args ) {
        CONSOLE.printLn( "PromotePackagedVersion vs " + VERSION );
        new PromotePackagedVersion( new Parameters( new ArgsToMap( args ) ) ).run();
    }

    protected void process() {
        new Promoter( createPersister() ).process();
    }

    protected class Promoter extends Processor {
        public Promoter( Persister pPersister ) {
            super( pPersister );
        }

        public void process() {
            extractVersionAndSet( createPath( mParameters.getFromDeploymentGroup() + ".txt" ) );
            writeDeploymentGroupVersionFiles();
        }
    }
}
