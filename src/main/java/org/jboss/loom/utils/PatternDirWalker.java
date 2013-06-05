package org.jboss.loom.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;

/**
 *  Not finished, I used Ant's org.apache.tools.ant.DirectoryScanner instead.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class PatternDirWalker {
    //private static final Logger log = LoggerFactory.getLogger( PatternDirWalker.class );
    
    private String pattern;
    private List segments;
    private PathMatcher mat;

    public PatternDirWalker( String pattern ) {
        this.pattern = pattern;
        this.segments = parseSegments(pattern);
        this.mat = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }
    
    public List<File> list( File dirToScan ) throws IOException{
        
        return new DirectoryWalker() {
            List<File> files = new LinkedList();

            @Override protected void handleFile( File file, int depth, Collection results ) throws IOException {
                if( PatternDirWalker.this.mat.matches( file.toPath()) )
                    results.add( file );
            }
            
            public List<File> findMatchingFiles( File dirToWalk ) throws IOException {
                this.walk( dirToWalk, this.files );
                return this.files;
            }
        }.findMatchingFiles( dirToScan );
        
    }// list()

    private List<Segment> parseSegments( String pattern ) {
        String[] parts = StringUtils.split("/", pattern);
        List<Segment> segs = new ArrayList(parts.length);
        for( String part : parts ) {
            Segment seg = new Segment(part);
            segs.add( seg );
        }
        return segs;
    }
    
    class Segment {
        public final String pat;
        private Segment( String pat ) {
            this.pat = pat;
        }
    }

}// class
