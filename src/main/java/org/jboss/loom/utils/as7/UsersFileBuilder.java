package org.jboss.loom.utils.as7;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class UsersFileBuilder {
    private static final Logger log = LoggerFactory.getLogger( UsersFileBuilder.class );

    private AS7Config asConfig;
    
    private List<UserRecord> userRecords = new LinkedList();
    
    private String fileName = "mgmt-users.properties";
    
    
    public static class UserRecord {
        String user;
        String realm;
        String password;
        public String getHash(){
            String s = user + ":" + realm + ":" + password;
            try {
                return new String( MessageDigest.getInstance("MD5").digest( s.getBytes("UTF-8") ), "UTF-8" );
            } catch( NoSuchAlgorithmException | UnsupportedEncodingException ex ) {
                throw new IllegalStateException("MD5 impl missing???");
            }
        }

        private String getLine() {
            return user + "=" + getHash();
        }
    }

    public UsersFileBuilder( AS7Config asConfig ) {
        this.asConfig = asConfig;
    }


    public UsersFileBuilder( AS7Config asConfig, String fileName ) {
        this.asConfig = asConfig;
        this.fileName = fileName;
    }
    
    
    
    public UsersFileBuilder addUser( UserRecord record ){
        this.userRecords.add( record );
        return this;
    }
    
    public void write() throws IOException {
        File file = Utils.createPath( this.asConfig.getConfigDir(), this.fileName );
        FileWriter fw = new FileWriter( file );
        for( UserRecord userRecord : userRecords ) {
            fw.write( userRecord.getLine() );
        }
        fw.close();
    }
    

}// class
