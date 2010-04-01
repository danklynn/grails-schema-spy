package org.codehaus.groovy.grails.plugins.schemaspy;

import org.apache.tools.ant.taskdefs.Java

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
        createArg().setFile(getSchemaSpyBinFile());
        getCommandLine().createArgument().setLine("${args} ${explicitArgs.join(' ')}");

        println "Executing: ${getCommandLine()}"
        super.execute()
    }

    void setSchema(String value) {args << "-s ${value}"}
    void setType(String value) {args << "-t ${value}"}
    void setUsername(String value) {args << "-u ${value}"}
    void setPassword(String value) {args << "-p ${value}"}

    public void setArgs(args) {
        this.args = args
    }

    public void setBin(File bin) {
        this.bin = bin
    }

    private File getSchemaSpyBinFile() {
        if(bin != null) {
            return bin;
        } else if(System.getProperty("schemaSpy.bin") != null) {
            return new File(System.getProperty("schemaSpy.bin"));
        } else {
            return new File("lib/schemaSpy_4.1.1.jar");
        }
    }
}
