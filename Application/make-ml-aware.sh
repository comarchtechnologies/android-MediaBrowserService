#!/bin/sh
# Usage:
#  add-devcert app-name.apk [install]

CN="CN=Comarch"
DEVID='31cc2508bf168f381d2990049b7da66e08f60d5f'
XML="ml-cert-metadata.xml"
GENERATOR_PATH="/usr/local/CertGenerator/certgenerator.jar"
KEYSTORE_PATH="./keystore.jks"
KEYSTORE_PASS="naukowiec"
INPUT_NAME="$1"

sign_apk() {
   IN_APK=$1
   OUT_APK=$2

   java -jar "$GENERATOR_PATH" generate-apk "$IN_APK" "$XML" "$CN" "$OUT_APK" "$DEVID"
   echo $KEYSTORE_PASS | jarsigner -verbose -keystore "$KEYSTORE_PATH" "$OUT_APK" mirrorlink
}

rm -f app-devcert.apk app-tmp.apk

sign_apk $INPUT_NAME app-tmp.apk
sign_apk app-tmp.apk app-devcert.apk

rm -f app-tmp.apk

if [ "$2" = 'install' ]; then
    adb install -r app-devcert.apk
fi
