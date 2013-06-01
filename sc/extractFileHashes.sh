echo $1
if [ "" == "$1" ]; then echo "Needs a param - EAP zip."; exit; fi

set -e

rm -rf tmp
mkdir -p tmp
ROOT=`pwd`
cd tmp
unzip - -q ../$1
NAME=`ls -1`
cd $NAME/jboss-as
#find . -type f | xargs -d \n md5sum > ../../md5.txt
find . -name admin-console.war -prune -o -name console-mgr.sar -prune -o -name jmx-console.war -prune -o -path ./tools -prune -o -path ./docs -prune -o -type f -print0 | xargs -0 crc32 > $ROOT/crc32.txt
cd $ROOT # Back to original
rm -rf tmp
mv crc32.txt $NAME-crc32.txt
