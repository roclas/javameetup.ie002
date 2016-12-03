sbt osgi-bundle 
sbt clean osgi-bundle && cp ./target/scala-2.11/tcposgi_2.11-1.0.jar ~/Liferay/bundles/dxp/liferay-dxp-digital-enterprise-7.0-ga1/deploy/

don't forget to deploy 
~/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.8.jar
~/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.11.8.jar
~/.ivy2/cache/org.scala-lang.modules/scala-parser-combinators_2.11/bundles/scala-parser-combinators_2.11-1.0.4.jar
~/.ivy2/cache/com.typesafe.akka/akka-osgi_2.11/jars/akka-osgi_2.11-2.4.10.jar 
~/.ivy2/cache/com.typesafe/config/bundles/config-1.3.0.jar 
~/.ivy2/cache/com.typesafe.akka/akka-actor_2.11/jars/akka-actor_2.11-2.4.9.jar
~/.ivy2/cache/org.scala-lang.modules/scala-java8-compat_2.11/jars/scala-java8-compat_2.11-0.7.0.jar
first

for i in $(cat readme.txt| grep "^~"| sed s@~/@/home/carlos/@);do
cp $i /home/carlos/Liferay/bundles/dxp/liferay-dxp-digital-enterprise-7.0-ga1/deploy
done

