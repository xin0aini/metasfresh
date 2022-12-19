J'ai du supprimer les classes fasterxml du jar, car elles entrent en conflit avec la version Metasfresh.

```shell
mkdir tmp
cd tmp
jar xvf ../sellingpartnerapi-aa-java-1.0-jar-with-dependencies.jar
rm -Rfd com/fasterxml
mv ../sellingpartnerapi-aa-java-1.0-jar-with-dependencies.jar ../sellingpartnerapi-aa-java-1.0-jar-with-dependencies.bak
jar cvf ../sellingpartnerapi-aa-java-1.0-jar-with-dependencies.jar *
```

Et l'installer dans un repo maven 
mvn install:install-file -Dfile=lib/sellingpartnerapi-aa-java-1.0-jar-with-dependencies.jar -DgroupId=com.amazon.sellingpartnerapi -DartifactId=sellingpartnerapi-aa-java -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
   