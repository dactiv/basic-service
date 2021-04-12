echo [INFO] install module.
cd ..
exec mvn clean source:jar install -Dmaven.test.skip=true
cd bin