package org.jboss.loom.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.DirectoryScanner;

/**
 *  Usage:
 * 
 *    <pre><code>files = new DirScanner("** /foo/*.bar").list( baseDir );</code></pre>
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DirScanner {

    private String pattern;
    
    public DirScanner( String pattern ) {
        if( pattern == null )
            throw new IllegalArgumentException("pattern can't be null.");
        this.pattern = pattern;
    }

    public List<String> list( File dirToScan ) throws IOException {
        if( dirToScan == null )
            throw new IllegalArgumentException("dirToScan can't be null.");

        DirectoryScanner ds = new DirectoryScanner();
        String[] includes = { this.pattern };
        //String[] excludes = {"modules\\*\\**"};
        ds.setIncludes(includes);
        //ds.setExcludes(excludes);
        ds.setBasedir( dirToScan );
        //ds.setCaseSensitive(true);
        ds.scan();

        String[] matches = ds.getIncludedFiles();
        return Arrays.asList( matches );
    }
    
    public List<File> listAsFiles( File dirToScan ) throws IOException {
        List<String> matches = this.list( dirToScan );
        List<File> files = new ArrayList(matches.size());
        for( String path : matches ) {
            files.add( new File( path ) );
        }
        return files;
    }

}// class
