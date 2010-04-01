includeTargets << grailsScript("Init")
includeTargets << grailsScript("Compile")
includeTargets << grailsScript("Package")

config = new ConfigObject()

target(main: "The description of the script goes here!") {
    depends(classpath, compile)
    rootLoader.addURL(new File("${classesDirPath}").toURL())
    packageApp()

    taskdef (name: 'schema-spy', classname: 'org.codehaus.groovy.grails.plugins.schemaspy.SchemaSpyTask')


    Properties p = config.toProperties()
    def driverClassName = config.dataSource.driverClassName
    def username = prepareString(p, config.dataSource.username, null)
    def password = prepareString(p, config.dataSource.password, null)
    def url = prepareString(p, config.dataSource.url, null)

    def dbType = 'hsqldb'
    def schema = (url =~ /\/(.+)$/)
    println "Matcher: ${schema}, ${url}"

    'schema-spy'(type: dbType, username: username, password: password, schema: schema)
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

