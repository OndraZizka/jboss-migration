package org.jboss.loom.utils.compar;


import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import static org.jboss.loom.utils.compar.FileHashComparer.MatchResult.MATCH;
import static org.jboss.loom.utils.compar.FileHashComparer.MatchResult.MISMATCH;
import static org.jboss.loom.utils.compar.FileHashComparer.MatchResult.MISSING;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ComparisonResult {
    
    public final File dir;
    public File hashesFile;
    Map<Path, FileHashComparer.MatchResult> matches;

    // Counts
    private int countMatches;
    private int countMismatches;
    private int countMisses;
    private boolean recount = true;


    public ComparisonResult( File dir ) {
        this.dir = dir;
    }

    public Map<Path, FileHashComparer.MatchResult> getMatches() { return matches; }
    public ComparisonResult setMatches( Map<Path, FileHashComparer.MatchResult> matches ) {
        this.matches = matches; recount = true; return this; 
    }

    public File getHashesFile() { return hashesFile; }
    public ComparisonResult setHashes( File hashes ) {
        this.hashesFile = hashes; return this; 
    }
    


    public int getCountTotal() {
        return this.matches.size();
    }

    public int getCountMatches() {
        this.doCountIfNeeded();
        return countMatches;
    }

    public int getCountMismatches() {
        this.doCountIfNeeded();
        return countMismatches;
    }

    public int getCountMisses() {
        this.doCountIfNeeded();
        return countMisses;
    }

    private void doCountIfNeeded() {
        if( ! this.recount ) return;
        if( this.matches == null )
            throw new IllegalStateException("Nothing to count - matches were not set yet.");

        this.countMatches = this.countMismatches = this.countMisses = 0;

        for( FileHashComparer.MatchResult res : this.matches.values()) {
            switch( res ) {
                case MATCH:    this.countMatches++; break;
                case MISMATCH: this.countMismatches++; break;
                case MISSING:  this.countMisses++; break;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Hash file ")
                .append(this.hashesFile)
                .append(" against dir ").append(this.dir).append(": ");
                
        if( this.matches == null )
            return sb.append("Matches were not set yet.").toString()
                    ;
        this.doCountIfNeeded();
        return sb
            .append(this.countMatches).append(" match, ")
            .append(this.countMismatches).append(" mism, ")
            .append(this.countMisses).append(" miss")
            .toString();
    }

    
}// class
