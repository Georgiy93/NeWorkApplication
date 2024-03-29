#!/usr/bin/env sh

# ... [License info remains unchanged]

##############################################################################
##
## Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ]; do
ls=$(ls -ld "$PRG")
link=`expr "$ls" : '.*-> \(.*\)$'`
if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
else
PRG=`dirname "$PRG"`"/$link"
fi
        done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
        APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

        APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn() {
    echo "$*"
}

die() {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
        CYGWIN*) cygwin=true;;
Darwin*) darwin=true;;
MINGW*) msys=true;;
NONSTOP*) nonstop=true;;
esac

        CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# ... [Rest of the code]

# This last section (collecting args and exec) remains unchanged.
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"
echo "JAVACMD contains: $JAVACMD"
JAVACMD=${JAVACMD:-java}
exec "$JAVACMD" "$@"

