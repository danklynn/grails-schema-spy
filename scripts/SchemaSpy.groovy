import net.sourceforge.schemaspy.Config

includeTargets << grailsScript("Init")
includeTargets << grailsScript("Compile")
includeTargets << grailsScript("Package")

config = new ConfigObject()
custonConfigFile = new File('mysql-custom.properties')

target(main: "The description of the script goes here!") {
    depends(classpath, compile)
    rootLoader.addURL(new File("${classesDirPath}").toURL())
    packageApp()

    mkdir(dir: "${basedir}/web-app/schemaspy")

    try {
        new net.sourceforge.schemaspy.SchemaAnalyzer().analyze(buildSchemaSpyConfig())
    } finally {
        custonConfigFile.delete()
    }    
}

Config buildSchemaSpyConfig() {
    def mysqlProps = getMySqlProperties()
    def c = new net.sourceforge.schemaspy.Config()

    Properties p = config.toProperties()
    c.user = prepareString(p, config.dataSource.username, null)
    c.password = prepareString(p, config.dataSource.password, null)

    c.outputDir = new File("${basedir}/web-app/schemaspy")
    c.adsEnabled = false

    c.dbType = 'mysql-custom'
    new File('mysql-custom.properties').withOutputStream {OutputStream os ->
        mysqlProps.store(os, null)
    }
    
    c.schema = '' //(mysqlProps.connectionSpec =~ '/([^/]+)$')[0][1]

    c
}

Properties getMySqlProperties() {
    ConfigObject props = new ConfigObject()

    Properties p = config.toProperties()

    props.description = 'MySQL'
    props.driver = config.dataSource.driverClassName
    props.driverPath = "${schemaSpyPluginDir}/lib/mysql-connector-java-5.1.10.jar".toString()
    props.connectionSpec = prepareString(p, config.dataSource.url, null)
    props.selectTableCommentsSql = "select table_name, table_comment comments from information_schema.tables where table_schema=:schema"

    props.toProperties()
}

setDefaultTarget(main)


// Borrowed from the grails-liquibase plugin. Thanks guys!
DEFAULT_PLACEHOLDER_PREFIX = '${'
DEFAULT_PLACEHOLDER_SUFFIX = '}'
def prepareString(Properties props, String strVal, String originalPlaceholder)
{
    int startIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
    while (startIndex != -1)
    {
        int endIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_SUFFIX, startIndex + DEFAULT_PLACEHOLDER_PREFIX.length());
        if (endIndex != -1)
        {
            String placeholder = strVal.substring(startIndex + DEFAULT_PLACEHOLDER_PREFIX.length(), endIndex);
            String originalPlaceholderToUse = null;

            if (originalPlaceholder != null)
            {
                originalPlaceholderToUse = originalPlaceholder;
                if (placeholder.equals(originalPlaceholder))
                {
                    throw new RuntimeException("Circular placeholder reference '" + placeholder +"' in property definitions [" + props + "]");
                }
            }
            else
            {
                originalPlaceholderToUse = placeholder;
            }

            // get the property directly, and fall back to System properties as required
            String propVal = props.getProperty(placeholder);
            if (propVal == null)
                propVal = System.getProperty(placeholder);

            if (propVal != null)
            {
                propVal = prepareString(props, propVal, originalPlaceholderToUse);
                strVal = strVal.substring(0, startIndex) + propVal + strVal.substring(endIndex + 1);
                startIndex = strVal.indexOf(DEFAULT_PLACEHOLDER_PREFIX, startIndex + propVal.length());
            }
            else
            {
                // return unprocessed value
                return strVal;
            }
        }
        else
        {
            startIndex = -1;
        }
    }
    return strVal;
}

