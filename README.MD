
# Installation
* Settings
Add these rows to the bin/setenv.sh
```bash
...
export CATALINA_OPTS="$CATALINA_OPTS -Dtelegrambot.config.file=conf/InvestBot.env"
export CATALINA_OPTS="$CATALINA_OPTS -Dtelegrambot.config.path=dataTelegramBot"
...
```
* Logging
Add this row to the bin/setenv.sh
```bash
...
export CATALINA_OPTS="$CATALINA_OPTS -Dlog4j.configurationFile=conf/log4j.xml"
...
```
# Troubleshooting
* If you encounter an error like 
```bash
...
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
...
```
for specific URLs then you have to create a trust store and add there all certificates (server's, CA's, ROOT's) for the specific URL. This script can help you for adding

```bash
** Create trustStore
$JAVA_HOME/bin/keytool -genkey -alias someAlias -keyalg RSA -keystore conf/keystore.jks -validity 3650

** Import specific certificate
CERT=DigiCert.cer \
&& $JAVA_HOME/bin/keytool -import -alias $CERT -keystore conf/keystore.jks -file /tmp/certs/$CERT && ll /tmp/certs/
```
Please pay attention that you have to specify the location of the trustStore. If you use tomcat then create a the file $CATALINA_HOME/bin/setenv.sh
```bash
export CATALINA_OPTS="$CATALINA_OPTS -Djavax.net.ssl.trustStore=conf/keystore.jks"
export CATALINA_OPTS="$CATALINA_OPTS -Djavax.net.ssl.trustStorePassword=******"
```