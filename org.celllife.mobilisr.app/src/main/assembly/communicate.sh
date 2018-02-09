#!/bin/sh

COMMUNICATE_HOME=`dirname $0`
export COMMUNICATE_HOME

CATALINA_BASE=$COMMUNICATE_HOME
export CATALINA_BASE

if [ -z "$CATALINA_HOME" ]; then
    echo "Usage: Missing environment variable CATALINA_HOME... Communicate could not start!"
  exit 1
fi

JAVA_OPTS="$JAVA_OPTS -Xmx768m -Xms768m -XX:MaxPermSize=512m"
export JAVA_OPTS

case "$1" in

  start)
    $CATALINA_HOME/bin/startup.sh
    ;;
  stop)
    $CATALINA_HOME/bin/shutdown.sh
    ;;
  *)
    echo "Usage: communicate (start|stop)"
    exit 1
esac
exit $RETVAL
