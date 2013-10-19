for i in `find -name '*.java'` ; do

  echo $i;

  echo "text"|cat - src/main/licenseHeader.txt $i > /tmp/out && mv /tmp/out $i

done

