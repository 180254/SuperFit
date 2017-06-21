setlocal EnableDelayedExpansion
set kafka_dir=C:\Users\Adrian\Downloads\kafka_2.12-0.10.2.1\kafka_2.12-0.10.2.1

start cmd /c call "%kafka_dir%\bin\windows\zookeeper-server-start.bat" "%kafka_dir%\config\zookeeper.properties"
timeout 10

start cmd /c call "%kafka_dir%\bin\windows\kafka-server-start.bat" "%kafka_dir%\config\server.properties"
timeout 10

start cmd /c call "%kafka_dir%\bin\windows\kafka-topics.bat" --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka
timeout 10
