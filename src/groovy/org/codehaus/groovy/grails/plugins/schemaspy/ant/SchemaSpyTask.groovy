package org.codehaus.groovy.grails.plugins.schemaspy;

import org.apache.tools.ant.taskdefs.Java
import org.apache.tools.ant.types.Path
import org.apache.tools.ant.BuildException

public class SchemaSpyTask extends Java {
    private List explicitArgs = []
    private String args = ""
    private File bin

    public SchemaSpyTask() {
        setFailonerror(true)
    }

    @Override
    public void execute() {
        classname = "net.sourceforge.schemaspy.Main"
        setClasspath(getSchemaSpyClasspath())
        
        getCommandLine().createArgument().setLine("${explicitArgs.join(' ')} ${args}");

        println "Executing: ${getCommandLine()}"
        super.execute()
    }

    Path getSchemaSpyClasspath() {
        def schemaSpyClasspath = getProject().getReference("schemaspy.classpath");
        if(schemaSpyClasspath == null || !(schemaSpyClasspath instanceof Path)) {
            throw new BuildException("Please create a path with id schemaspy.classpath");
        }
        return (Path) schemaSpyClasspath;
    }

    void setSchema(String value) {explicitArgs << "-s \"${value}\" -db \"${value}\""}
    void setType(String value) {explicitArgs << "-t \"${value}\""}
    void setUsername(String value) {explicitArgs << "-u \"${value}\""}
    void setPassword(String value) {explicitArgs << "-p \"${value}\""}
    void setTargetDir(String value) {explicitArgs << "-o \"${value}\""}
    void setHost(String value) {explicitArgs << "-host \"${value}\""}

    /**
     * Set any additional arguments to the schemaSpy command line
     * @param args
     */
    public void setArgs(args) {
        this.args = args
    }

    public void setBin(File bin) {
        this.bin = bin
    }
}
