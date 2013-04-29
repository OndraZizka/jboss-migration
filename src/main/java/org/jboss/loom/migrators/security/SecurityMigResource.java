package org.jboss.loom.migrators.security;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Roman Jakubco
 * Date: 4/29/13
 */
public class SecurityMigResource {
    private Set<String> fileNames = new HashSet();

    private Map<File, String> modules = new HashMap();

    private int increment = 1;

    public Set<String> getFileNames() {
        return fileNames;
    }

    public Map<File, String> getModules() {
        return modules;
    }

    public int getIncrement() {
        return increment++;
    }
}
