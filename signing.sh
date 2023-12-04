# Inject maven signing key
echo -e '\n' >> gradle.properties
echo $GPG_SIGNING_KEY \
| awk 'NR == 1 { print "SIGNING_KEY=" } 1' ORS='\\n' \
>> gradle.properties